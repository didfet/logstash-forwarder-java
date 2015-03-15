package info.fetter.logstashforwarder;

/*
 * Copyright 2015 Didier Fetter
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

import info.fetter.logstashforwarder.util.LastModifiedFileFilter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.apache.log4j.Logger;

public class FileWatcher {
	private static final Logger logger = Logger.getLogger(FileWatcher.class);
	private List<FileAlterationObserver> observerList = new ArrayList<FileAlterationObserver>();
	private static final int ONE_DAY = 24 * 3600 * 1000;
	private long deadTime;
	private Map<File,FileState> watchMap = new HashMap<File,FileState>();
	private Map<File,FileState> changedWatchMap = new HashMap<File,FileState>();
	private static int MAX_SIGNATURE_LENGTH = 1024;

	public FileWatcher(long deadTime) {
		this.deadTime = deadTime;
	}

	public FileWatcher() {
		this(ONE_DAY);
	}

	public void addFilesToWatch(String fileToWatch, Event fields) {
		try {
			if(fileToWatch.equals("-")) {
				addStdIn(fields);
			} else if(fileToWatch.contains("*")) {
				addWildCardFiles(fileToWatch, fields);
			} else {
				addSingleFile(fileToWatch, fields);
			}
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void checkFiles() throws IOException {
		logger.debug("Checking files");
		logger.trace("==============");
		for(FileAlterationObserver observer : observerList) {
			observer.checkAndNotify();
		}
		processModifications();
		printWatchMap();
	}
	
	public void readFiles() {
		logger.debug("Reading files");
		logger.trace("==============");
	}

	private void processModifications() throws IOException {

		for(File file : changedWatchMap.keySet()) {
			FileState state = changedWatchMap.get(file);
			if(logger.isTraceEnabled()) {
				logger.trace("Checking file : " + file.getCanonicalPath());
				logger.trace("-- Last modified : " + state.getLastModified());
				logger.trace("-- Size : " + state.getSize());
				logger.trace("-- Directory : " + state.getDirectory());
				logger.trace("-- Filename : " + state.getFileName());
			}

			logger.trace("Determine if file has just been written to");
			FileState oldState = watchMap.get(file);
			if(oldState != null) {
				if(oldState.getSize() > state.getSize()) {
					logger.trace("File shorter : file can't be the same");
				} else {
					if(oldState.getSignatureLength() == state.getSignatureLength() && oldState.getSignature() == state.getSignature()) {
						state.setOldFileState(oldState);
						logger.trace("Same signature size and value : file is the same");
						continue;
					} else if(oldState.getSignatureLength() < state.getSignatureLength()){
						long signature = FileSigner.computeSignature(state.getRandomAccessFile(), oldState.getSignatureLength());
						if(signature == oldState.getSignature()) {
							state.setOldFileState(oldState);
							logger.trace("Same signature : file is the same");
							continue;
						} else {
							logger.trace("Signature different : file can't be the same");
						}
					} else if(oldState.getSignatureLength() > state.getSignatureLength()){
						logger.trace("Signature shorter : file can't be the same");
					}
				}
			}

			if(state.getOldFileState() == null) {
				logger.trace("Determine if file has been renamed and/or written to");
				for(File otherFile : watchMap.keySet()) {
					FileState otherState = watchMap.get(otherFile);
					if(otherState != null && state.getSize() >= otherState.getSize() && state.getDirectory().equals(otherState.getDirectory())) {
						if(logger.isTraceEnabled()) {
							logger.trace("Comparing to : " + otherFile.getCanonicalPath());
						}
						if(otherState.getSignatureLength() == state.getSignatureLength() && otherState.getSignature() == state.getSignature()) {
							state.setOldFileState(otherState);
							logger.trace("Same signature size and value : file is the same");
							break;
						} else if(otherState.getSignatureLength() < state.getSignatureLength()){
							long signature = FileSigner.computeSignature(state.getRandomAccessFile(), otherState.getSignatureLength());
							if(signature == otherState.getSignature()) {
								state.setOldFileState(otherState);
								logger.trace("Same signature : file is the same");
								break;
							} else {
								logger.trace("Signature different : file can't be the same");
							}
						} else if(otherState.getSignatureLength() > state.getSignatureLength()){
							logger.trace("Signature shorter : file can't be the same");
						}
					}
				}
			}
		}

		logger.trace("Refreshing file state");
		for(File file : changedWatchMap.keySet()) {
			if(logger.isTraceEnabled()) {
				logger.trace("Refreshing file : " + file.getCanonicalPath());
			}
			FileState state = changedWatchMap.get(file);
			FileState oldState = state.getOldFileState();
			if(oldState == null) {
				logger.trace("File has been truncated or created, not retrieving pointer");
			} else {
				logger.trace("File has not been truncated or created, retrieving pointer");
				state.setPointer(oldState.getPointer());
				oldState.getRandomAccessFile().close();
			}
		}

		logger.trace("Replacing old state");
		for(File file : changedWatchMap.keySet()) {
			FileState state = changedWatchMap.get(file);
			watchMap.put(file, state);
		}

		// Truncating changedWatchMap
		changedWatchMap.clear();

		removeMarkedFilesFromWatchMap();
	}

	private void addSingleFile(String fileToWatch, Event fields) throws Exception {
		logger.info("Watching file : " + new File(fileToWatch).getCanonicalPath());
		String directory = FilenameUtils.getFullPath(fileToWatch);
		String fileName = FilenameUtils.getName(fileToWatch); 
		IOFileFilter fileFilter = FileFilterUtils.and(
				FileFilterUtils.fileFileFilter(),
				FileFilterUtils.nameFileFilter(fileName),
				new LastModifiedFileFilter(deadTime));
		initializeWatchMap(new File(directory), fileFilter, fields);
	}

	private void addWildCardFiles(String filesToWatch, Event fields) throws Exception {
		logger.info("Watching wildcard files : " + filesToWatch);
		String directory = FilenameUtils.getFullPath(filesToWatch);
		String wildcard = FilenameUtils.getName(filesToWatch);
		logger.trace("Directory : " + new File(directory).getCanonicalPath() + ", wildcard : " + wildcard);
		IOFileFilter fileFilter = FileFilterUtils.and(
				FileFilterUtils.fileFileFilter(),
				new WildcardFileFilter(wildcard),
				new LastModifiedFileFilter(deadTime));
		initializeWatchMap(new File(directory), fileFilter, fields);
	}

	private void addStdIn(Event fields) {
		logger.error("Watching stdin : not implemented yet");
	}

	private void initializeWatchMap(File directory, IOFileFilter fileFilter, Event fields) throws Exception {
		FileAlterationObserver observer = new FileAlterationObserver(directory, fileFilter);
		FileModificationListener listener = new FileModificationListener(this, fields);
		observer.addListener(listener);
		observerList.add(observer);
		observer.initialize();
		for(File file : FileUtils.listFiles(directory, fileFilter, null)) {
			addFileToWatchMap(watchMap, file, fields);
		}
	}

	private void addFileToWatchMap(Map<File,FileState> map, File file, Event fields) {
		try {
			FileState state = new FileState(file);
			state.setFields(fields);
			int signatureLength = (int) (state.getSize() > MAX_SIGNATURE_LENGTH ? MAX_SIGNATURE_LENGTH : state.getSize());
			state.setSignatureLength(signatureLength);
			long signature = FileSigner.computeSignature(state.getRandomAccessFile(), signatureLength);
			state.setSignature(signature);
			logger.trace("Setting signature of size : " + signatureLength + " on file : " + file + " : " + signature);
			map.put(file, state);
		} catch(IOException e) {
			logger.error("Caught IOException : " + e.getMessage());
		}
	}

	public void onFileChange(File file, Event fields) {
		try {
			logger.debug("Change detected on file : " + file.getCanonicalPath());
			addFileToWatchMap(changedWatchMap, file, fields);
		} catch (IOException e) {
			logger.error("Caught IOException : " + e.getMessage());
		}	
	}

	public void onFileCreate(File file, Event fields) {
		try {
			logger.debug("Create detected on file : " + file.getCanonicalPath());
			addFileToWatchMap(changedWatchMap, file, fields);
		} catch (IOException e) {
			logger.error("Caught IOException : " + e.getMessage());
		}
	}

	public void onFileDelete(File file) {
		try {
			logger.debug("Delete detected on file : " + file.getCanonicalPath());
			watchMap.get(file).setDeleted();
		} catch (IOException e) {
			logger.error("Caught IOException : " + e.getMessage());
		}
	}

	private void printWatchMap() {
		if(logger.isTraceEnabled()) {
			logger.trace("WatchMap contents : ");
			for(File file : watchMap.keySet()) {
				FileState state = watchMap.get(file);
				logger.trace("\tFile : " + state.getDirectory() + " marked for deletion : " + state.isDeleted());
			}
		}
	}

	private void removeMarkedFilesFromWatchMap() throws IOException {
		logger.trace("Removing deleted files from watchMap");
		List<File> markedList = null;
		for(File file : watchMap.keySet()) {
			FileState state = watchMap.get(file);
			if(state.isDeleted()) {
				if(markedList == null) {
					markedList = new ArrayList<File>();
				}
				markedList.add(file);	
			}
		}
		if(markedList != null) {
			for(File file : markedList) {
				FileState state = watchMap.remove(file);
				state.getRandomAccessFile().close();
				logger.trace("\tFile : " + file.getCanonicalFile() + " removed");
			}
		}
	}

	public void close() throws IOException {
		logger.debug("Closing all files");
		for(File file : watchMap.keySet()) {
			FileState state = watchMap.get(file);
			state.getRandomAccessFile().close();
		}
	}

}
