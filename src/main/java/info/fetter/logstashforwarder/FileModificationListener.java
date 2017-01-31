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

import java.io.File;

import org.apache.commons.io.monitor.FileAlterationListener;
import org.apache.commons.io.monitor.FileAlterationObserver;

public class FileModificationListener implements FileAlterationListener {
	private Event fields;
	private FileWatcher watcher;
	private Multiline multiline;

	public FileModificationListener(FileWatcher watcher, Event fields, Multiline multiline) {
		this.watcher = watcher;
		this.fields = fields;
		this.multiline = multiline;
	}

	public void onDirectoryChange(File file) {
		// Not implemented
	}

	public void onDirectoryCreate(File file) {
		// Not implemented
	}

	public void onDirectoryDelete(File file) {
		// Not implemented
	}

	public void onFileChange(File file) {
		watcher.onFileChange(file, fields, multiline);
	}

	public void onFileCreate(File file) {
		watcher.onFileCreate(file, fields, multiline);
	}

	public void onFileDelete(File file) {
		watcher.onFileDelete(file);
	}

	public void onStart(FileAlterationObserver file) {
		// Not implemented
	}

	public void onStop(FileAlterationObserver file) {
		// Not implemented
	}

}
