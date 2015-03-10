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
import java.io.IOException;

public class FileState {
	private File file;
	private String filePath;
	private long lastModified;
	private long size;
	
	public FileState(File file) throws IOException {
		this.file = file;
		filePath = file.getCanonicalPath();
	}
	
	public void refresh() {
		lastModified = file.lastModified();
		size = file.length();
	}

	public File getFile() {
		return file;
	}

	public long getLastModified() {
		return lastModified;
	}

	public long getSize() {
		return size;
	}
	
	public String getFilePath() {
		return filePath;
	}
	
}
