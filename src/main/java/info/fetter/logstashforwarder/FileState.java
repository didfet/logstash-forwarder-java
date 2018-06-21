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

import info.fetter.logstashforwarder.util.LogFile;
import info.fetter.logstashforwarder.util.RandomAccessFile;
import info.fetter.logstashforwarder.util.NamedPipe;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
//import java.io.RandomAccessFile;



import org.apache.commons.lang.builder.ToStringBuilder;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Map;

public class FileState {
	@JsonIgnore
	private File file;
	private String directory;
	private String fileName;
	@JsonIgnore
	private long lastModified;
	@JsonIgnore
	private long size;
	@JsonIgnore
	private boolean deleted = false;
	private long signature;
	private int signatureLength;
	@JsonIgnore
	private boolean changed = false;
	@JsonIgnore
	private LogFile logFile;
	private long pointer = 0;
	@JsonIgnore
	private FileState oldFileState;
	@JsonIgnore
	private Event fields;
	@JsonIgnore
	private Multiline multiline;
	@JsonIgnore
	private Filter filter;
	@JsonIgnore
	private boolean matchedToNewFile = false;

	public FileState() {
	}

	public FileState(File file) throws IOException {
		this.file = file;
		directory = file.getCanonicalFile().getParent();
		fileName = file.getName();
		if (file.isFile()) {
			logFile = new RandomAccessFile(file.getPath(), "r");
		} else {
			logFile = new NamedPipe(file);
		}
		lastModified = file.lastModified();
		size = file.length();
	}

	private void setFileFromDirectoryAndName() throws FileNotFoundException {
		file = new File(directory + File.separator + fileName);
		if(file.exists()) {
			logFile = null;
			lastModified = file.lastModified();
			size = file.length();
		} else {
			deleted = true;
		}
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

	public String getDirectory() {
		return directory;
	}

	public void setDirectory(String directory) throws FileNotFoundException {
		this.directory = directory;
		if(fileName != null && directory != null) {
			setFileFromDirectoryAndName();
		}
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) throws FileNotFoundException {
		this.fileName = fileName;
		if(fileName != null && directory != null) {
			setFileFromDirectoryAndName();
		}
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

	public LogFile getLogFile() {
		return logFile;
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
		oldFileState.setMatchedToNewFile(true);
	}

	public void deleteOldFileState() {
		try {
			oldFileState.getLogFile().close();
			oldFileState = null;
		} catch(Exception e) {}
	}

	public Event getFields() {
		return fields;
	}

	public void setFields(Event fields) {
		this.fields = fields;
	}

	public Multiline getMultiline() {
		return multiline;
	}

	public void setMultiline(Multiline multiline) {
		this.multiline = multiline;
	}

	public Filter getFilter() {
		return filter;
	}

	public void setFilter(Filter filter) {
		this.filter = filter;
	}

	public boolean isMatchedToNewFile() {
		return matchedToNewFile;
	}

	public void setMatchedToNewFile(boolean matchedToNewFile) {
		this.matchedToNewFile = matchedToNewFile;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).
				append("fileName", fileName).
				append("directory", directory).
				append("pointer", pointer).
				append("signature", signature).
				append("signatureLength", signatureLength).
				toString();
	}

}
