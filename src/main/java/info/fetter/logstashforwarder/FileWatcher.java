package info.fetter.logstashforwarder;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.io.monitor.FileAlterationListener;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.apache.log4j.Logger;

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

public class FileWatcher implements FileAlterationListener {
	private static final Logger logger = Logger.getLogger(FileWatcher.class);
	private List<FileAlterationObserver> observerList = new ArrayList<FileAlterationObserver>();
	static final int ONE_DAY = 24 * 3600 * 1000;
	private long cutoff;
	
	public FileWatcher(long deadTime) {
		cutoff = System.currentTimeMillis() - deadTime;
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
		logger.info("Watching file : " + new File(fileToWatch).getAbsolutePath());
		String directory = FilenameUtils.getFullPath(fileToWatch);
		String fileName = FilenameUtils.getName(fileToWatch); 
		IOFileFilter fileFilter = FileFilterUtils.and(
				FileFilterUtils.fileFileFilter(),
				FileFilterUtils.nameFileFilter(fileName),
				FileFilterUtils.ageFileFilter(cutoff, false));
		FileAlterationObserver observer = new FileAlterationObserver(new File(directory), fileFilter);
		observer.addListener(this);
		observerList.add(observer);
		observer.initialize();
	}

	private void watchWildCardFiles(String filesToWatch) throws Exception {
		logger.info("Watching wildcard files : " + filesToWatch);
		String directory = FilenameUtils.getFullPath(filesToWatch);
		String wildcard = FilenameUtils.getName(filesToWatch);
		logger.trace("Directory : " + new File(directory).getAbsolutePath() + ", wildcard : " + wildcard);
		IOFileFilter fileFilter = FileFilterUtils.and(
				FileFilterUtils.fileFileFilter(),
				new WildcardFileFilter(wildcard),
				FileFilterUtils.ageFileFilter(cutoff, false));
		FileAlterationObserver observer = new FileAlterationObserver(new File(directory), fileFilter);
		observer.addListener(this);
		observerList.add(observer);
		observer.initialize();
	}



	private void watchStdIn() {
		logger.info("Watching stdin : not implemented yet");
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
	}

	public void onFileDelete(File file) {
		logger.debug("Delete detected on file : " + file.getAbsolutePath());
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
