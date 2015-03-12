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
import org.apache.commons.io.monitor.FileAlterationListener;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.apache.log4j.Logger;

public class FileWatcher implements FileAlterationListener {
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

	public void addFilesToWatch(String fileToWatch) {
		try {
			if(fileToWatch.equals("-")) {
				watchStdIn();
			} else if(fileToWatch.contains("*")) {
				watchWildCardFiles(fileToWatch);
			} else {
				watchFile(fileToWatch);
			}
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void checkFiles() throws IOException {
		logger.debug("Checking files");
		printWatchMap();
		for(FileAlterationObserver observer : observerList) {
			observer.checkAndNotify();
		}
		processModifications();

		printWatchMap();
	}

	private void processModifications() throws IOException {
		removeMarkedFilesFromWatchMap();

		for(File file : changedWatchMap.keySet()) {
			FileState state = changedWatchMap.get(file);
			logger.trace("Checking file : " + file.getCanonicalPath());

			// Determine if file is still the same
			logger.trace("Determine if file name has not changed");
			FileState oldState = watchMap.get(file);
			if(oldState != null) {
				if(oldState.getSize() > state.getSize()) {
					// File is shorter, can't be the same
					logger.trace("File shorter : file can't be the same");
				} else {
					if(oldState.getSignatureLength() == state.getSignatureLength() && oldState.getSignature() == state.getSignature()) {
						// File is the same
						state.setOldFileState(oldState);
						logger.trace("Same signature size and value : file is the same");
					} else if(oldState.getSignatureLength() < state.getSignatureLength()){
						// Compute the signature on the new file
						long signature = FileSigner.computeSignature(state.getRandomAccessFile(), oldState.getSignatureLength());
						if(signature == oldState.getSignature()) {
							// File is the same
							state.setOldFileState(oldState);
							logger.trace("Same signature : file is the same");
						} else {
							// File can't be the same
							logger.trace("Signature different : file can't be the same");
						}
					} else if(oldState.getSignatureLength() > state.getSignatureLength()){
						// File can't be the same
						logger.trace("Signature shorter : file can't be the same");
					}
				}
			}

			// Determine if there was a file with the same size and last modification date in the same directory and with a different name
			logger.trace("Determine if file has just been renamed");
			if(state.getOldFileState() == null) {
				for(File otherFile : changedWatchMap.keySet()) {
					FileState otherState = watchMap.get(otherFile);
					if(otherState != null
							&& state.getLastModified() == otherState.getLastModified()
							&& state.getSize() == otherState.getSize() 
							&& state.getFilePath().equals(otherState.getFilePath())
							&& ! state.getFileName().equals(otherState.getFileName())) {
						logger.trace("Comparing to : " + otherFile.getCanonicalPath());
						// Assume file has been renamed
						state.setOldFileState(otherState);
						logger.trace("Same directory, same size and last modification date : file has been renamed to : " + otherFile.getCanonicalPath());
					}
				}
			}

			// Determine if file has been renamed and appended
			logger.trace("Determine if file has been renamed and appended");
			if(state.getOldFileState() == null) {
				for(File otherFile : changedWatchMap.keySet()) {
					FileState otherState = watchMap.get(otherFile);
					if(otherState != null && state.getSize() > otherState.getSize() && state.getFilePath().equals(otherState.getFilePath())) {
						logger.trace("Comparing to : " + otherFile.getCanonicalPath());
						// File in the same directory which was smaller
						if(otherState.getSignatureLength() == state.getSignatureLength() && otherState.getSignature() == state.getSignature()) {
							// File is the same
							state.setOldFileState(otherState);
							logger.trace("Same signature size and value : file is the same");
						} else if(otherState.getSignatureLength() < state.getSignatureLength()){
							// Compute the signature on the new file
							long signature = FileSigner.computeSignature(state.getRandomAccessFile(), otherState.getSignatureLength());
							if(signature == otherState.getSignature()) {
								// File is the same
								state.setOldFileState(otherState);
								logger.trace("Same signature : file is the same");
							} else {
								// File can't be the same
								logger.trace("Signature different : file can't be the same");
							}
						} else if(otherState.getSignatureLength() > state.getSignatureLength()){
							// File can't be the same
							logger.trace("Signature shorter : file can't be the same");
						}
					}
				}
			}
		}

		// Refresh file state
		logger.trace("refreshing file state");
		for(File file : changedWatchMap.keySet()) {
			logger.trace("Refreshing file : " + file.getCanonicalPath());
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

		// Replacing old state
		logger.trace("Replacing old state");
		for(File file : changedWatchMap.keySet()) {
			logger.trace("Replacing file : " + file.getCanonicalPath());
			FileState state = changedWatchMap.get(file);
			watchMap.put(file, state);
		}

	}

	private void watchFile(String fileToWatch) throws Exception {
		logger.info("Watching file : " + new File(fileToWatch).getCanonicalPath());
		String directory = FilenameUtils.getFullPath(fileToWatch);
		String fileName = FilenameUtils.getName(fileToWatch); 
		IOFileFilter fileFilter = FileFilterUtils.and(
				FileFilterUtils.fileFileFilter(),
				FileFilterUtils.nameFileFilter(fileName),
				new LastModifiedFileFilter(deadTime));
		initializeWatchMap(new File(directory), fileFilter);
	}

	private void watchWildCardFiles(String filesToWatch) throws Exception {
		logger.info("Watching wildcard files : " + filesToWatch);
		String directory = FilenameUtils.getFullPath(filesToWatch);
		String wildcard = FilenameUtils.getName(filesToWatch);
		logger.trace("Directory : " + new File(directory).getCanonicalPath() + ", wildcard : " + wildcard);
		IOFileFilter fileFilter = FileFilterUtils.and(
				FileFilterUtils.fileFileFilter(),
				new WildcardFileFilter(wildcard),
				new LastModifiedFileFilter(deadTime));
		initializeWatchMap(new File(directory), fileFilter);
	}

	private void watchStdIn() {
		logger.info("Watching stdin : not implemented yet");
	}

	private void initializeWatchMap(File directory, IOFileFilter fileFilter) throws Exception {
		FileAlterationObserver observer = new FileAlterationObserver(directory, fileFilter);
		observer.addListener(this);
		observerList.add(observer);
		observer.initialize();
		for(File file : FileUtils.listFiles(directory, fileFilter, null)) {
			addFileToWatchMap(watchMap, file);
		}
	}

	private void addFileToWatchMap(Map<File,FileState> map, File file) {
		try {
			FileState state = new FileState(file);
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

	public void onFileChange(File file) {
		try {
			logger.debug("Change detected on file : " + file.getCanonicalPath());
			addFileToWatchMap(changedWatchMap, file);
		} catch (IOException e) {
			logger.error("Caught IOException : " + e.getMessage());
		}	
	}

	public void onFileCreate(File file) {
		try {
			logger.debug("Create detected on file : " + file.getCanonicalPath());
			addFileToWatchMap(changedWatchMap, file);
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
				logger.trace("\tFile : " + state.getFilePath() + " marked for deletion : " + state.isDeleted());
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
				logger.trace("\tFile : " + state.getFilePath() + " removed");
			}
		}
	}

	public void onDirectoryChange(File directory) {
		// Do nothing
	}

	public void onDirectoryCreate(File directory) {
		// Do nothing
	}

	public void onDirectoryDelete(File directory) {
		// Do nothing
	}

	public void onStart(FileAlterationObserver observer) {
		// TODO Auto-generated method stub
	}

	public void onStop(FileAlterationObserver observer) {
		// TODO Auto-generated method stub
	}
}
