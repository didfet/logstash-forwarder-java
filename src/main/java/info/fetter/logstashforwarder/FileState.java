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
import java.io.RandomAccessFile;

public class FileState {
	private File file;
	private String filePath;
	private long lastModified;
	private long size;
	private boolean deleted = false;
	private long signature;
	private int signatureLength;
	private boolean changed = false;
	private RandomAccessFile randomAccessFile;
	private long pointer = 0;
	private FileState oldFileState;
	private String fileName;
	
	public FileState(File file) throws IOException {
		this.file = file;
		filePath = file.getCanonicalPath();
		fileName = file.getName();
		randomAccessFile = new RandomAccessFile(file, "r");
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
	
	public boolean isDeleted() {
		return deleted;
	}
	
	public void setDeleted() {
		deleted = true;
	}
	
	public boolean hasChanged() {
		return changed;
	}
	
	public void setChanged(boolean changed) {
		this.changed = changed;
	}

	public long getSignature() {
		return signature;
	}

	public void setSignature(long signature) {
		this.signature = signature;
	}

	public RandomAccessFile getRandomAccessFile() {
		return randomAccessFile;
	}

	public long getPointer() {
		return pointer;
	}
	
	public void setPointer(long pointer) {
		this.pointer = pointer;
	}

	public int getSignatureLength() {
		return signatureLength;
	}

	public void setSignatureLength(int signatureLength) {
		this.signatureLength = signatureLength;
	}

	public FileState getOldFileState() {
		return oldFileState;
	}

	public void setOldFileState(FileState oldFileState) {
		this.oldFileState = oldFileState;
	}

	public String getFileName() {
		return fileName;
	}
	
}
