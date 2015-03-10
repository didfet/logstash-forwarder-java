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

	private void watchFile(String fileToWatch) throws Exception {
		logger.info("Watching file : " + new File(fileToWatch).getCanonicalPath());
		String directory = FilenameUtils.getFullPath(fileToWatch);
		String fileName = FilenameUtils.getName(fileToWatch); 
		IOFileFilter fileFilter = FileFilterUtils.and(
				FileFilterUtils.fileFileFilter(),
				FileFilterUtils.nameFileFilter(fileName),
				new LastModifiedFileFilter(deadTime));
		updateWatchMap(new File(directory), fileFilter);
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
		updateWatchMap(new File(directory), fileFilter);
	}

	private void watchStdIn() {
		logger.info("Watching stdin : not implemented yet");
	}

	private void updateWatchMap(File directory, IOFileFilter fileFilter) throws Exception {
		FileAlterationObserver observer = new FileAlterationObserver(directory, fileFilter);
		observer.addListener(this);
		observerList.add(observer);
		observer.initialize();
		for(File file : FileUtils.listFiles(directory, fileFilter, null)) {
			addFileToWatchMap(file);
		}
	}

	private void addFileToWatchMap(File file) {
		try {
			FileState state = new FileState(file);
			state.refresh();
			watchMap.put(file, state);
		} catch(IOException e) {
			logger.error("Caught IOException : " + e.getMessage());
		}
	}

	private void removeFileFromWatchMap(File file) {
		watchMap.remove(file);
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

	public void onFileChange(File file) {
		logger.debug("Change detected on file : " + file.getAbsolutePath());	
	}

	public void onFileCreate(File file) {
		logger.debug("Create detected on file : " + file.getAbsolutePath());
		addFileToWatchMap(file);
	}

	public void onFileDelete(File file) {
		logger.debug("Delete detected on file : " + file.getAbsolutePath());
		removeFileFromWatchMap(file);
	}

	public void onStart(FileAlterationObserver observer) {
		// TODO Auto-generated method stub
	}

	public void onStop(FileAlterationObserver observer) {
		// TODO Auto-generated method stub
	}

	public void checkFiles() {
		logger.debug("Checking files");
		for(FileAlterationObserver observer : observerList) {
			observer.checkAndNotify();
		}
	}

}
