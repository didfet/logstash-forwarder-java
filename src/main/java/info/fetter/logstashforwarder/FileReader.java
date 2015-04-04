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

import info.fetter.logstashforwarder.util.AdapterException;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

public class FileReader extends Reader {
	private static Logger logger = Logger.getLogger(FileReader.class);
	private static final byte[] ZIP_MAGIC = new byte[] {(byte) 0x50, (byte) 0x4b, (byte) 0x03, (byte) 0x04};
	private static final byte[] LZW_MAGIC = new byte[] {(byte) 0x1f, (byte) 0x9d};
	private static final byte[] LZH_MAGIC = new byte[] {(byte) 0x1f, (byte) 0xa0};
	private static final byte[] GZ_MAGIC = new byte[] {(byte) 0x1f, (byte) 0x8b, (byte) 0x08};
	private static final byte[][] MAGICS = new byte[][] {ZIP_MAGIC, LZW_MAGIC, LZH_MAGIC, GZ_MAGIC};
	private Map<File,Long> pointerMap;

	public FileReader(int spoolSize) {
		super(spoolSize);
	}

	public int readFiles(Collection<FileState> fileList) throws AdapterException {
		int eventCount = 0;
		stringBuilder = new StringBuilder(STRINGBUILDER_INITIAL_CAPACITY);
		if(logger.isTraceEnabled()) {
			logger.trace("Reading " + fileList.size() + " file(s)");
		}
		pointerMap = new HashMap<File,Long>(fileList.size(),1);
		for(FileState state : fileList) {
			eventCount += readFile(state, spoolSize - eventCount);
		}
		if(eventCount > 0) {
			adapter.sendEvents(eventList);
		}
		for(FileState state : fileList) {
			state.setPointer(pointerMap.get(state.getFile()));
		}
		eventList.clear();
		return eventCount; // Return number of events sent to adapter
	}

	private int readFile(FileState state, int spaceLeftInSpool) {
		int eventListSizeBefore = eventList.size();
		File file = state.getFile();
		long pointer = state.getPointer();
		if(logger.isTraceEnabled()) {
			logger.trace("File : " + file + " pointer : " + pointer);
			logger.trace("Space left in spool : " + spaceLeftInSpool);
		}
		if(isCompressedFile(state)) {
			pointer = file.length();
		} else {
			pointer = readLines(state, spaceLeftInSpool);
		}
		pointerMap.put(file, pointer);
		return eventList.size() - eventListSizeBefore; // Return number of events read
	}

	private boolean isCompressedFile(FileState state) {
		RandomAccessFile reader = state.getRandomAccessFile();
		try {
			for(byte[] magic : MAGICS) {
				byte[] fileBytes = new byte[magic.length]; 
				reader.seek(0);
				int read = reader.read(fileBytes);
				if (read != magic.length) {
					continue;
				}
				if(Arrays.equals(magic, fileBytes)) {
					logger.debug("Compressed file detected : " + state.getFile());
					return true;
				}
			}
		} catch(IOException e) {
			logger.warn("Exception raised while reading file : " + state.getFile());
			e.printStackTrace();
		}
		return false;
	}

	private long readLines(FileState state, int spaceLeftInSpool) {
		RandomAccessFile reader = state.getRandomAccessFile();
		long pos = state.getPointer();
		try {
			reader.seek(pos);
			String line = readLine(reader);
			while (line != null && spaceLeftInSpool > 0) {
				if(logger.isTraceEnabled()) {
					logger.trace("-- Read line : " + line);
					logger.trace("-- Space left in spool : " + spaceLeftInSpool);
				}
				pos = reader.getFilePointer();
				addEvent(state, pos, line);
				line = readLine(reader);
				spaceLeftInSpool--;
			}
			reader.seek(pos); // Ensure we can re-read if necessary
		} catch(IOException e) {
			logger.warn("Exception raised while reading file : " + state.getFile());
			e.printStackTrace();
		}
		return pos;
	}

	private String readLine(RandomAccessFile reader) throws IOException {
		stringBuilder.setLength(0);
		int ch;
		boolean seenCR = false;
		while((ch=reader.read()) != -1) {
			switch(ch) {
			case '\n':
				return stringBuilder.toString();
			case '\r':
				seenCR = true;
				break;
			default:
				if (seenCR) {
					stringBuilder.append('\r');
					seenCR = false;
				}
				stringBuilder.append((char)ch); // add character, not its ascii value
			}
		}
		return null;
	}

}
