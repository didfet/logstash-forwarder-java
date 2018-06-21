package info.fetter.logstashforwarder;

/*
 * Copyright 2015 Didier Fetter
 * Copyright 2017 Alberto González Palomo https://sentido-labs.com
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
import info.fetter.logstashforwarder.util.LogFile;
import info.fetter.logstashforwarder.util.RandomAccessFile;
import info.fetter.logstashforwarder.util.NamedPipe;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
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
	ByteBuffer bufferedLines = ByteBuffer.allocate(BYTEBUFFER_CAPACITY);

	public FileReader(int spoolSize) {
		super(spoolSize);
	}

	public int readFiles(Collection<FileState> fileList) throws AdapterException {
		int eventCount = 0;
		if(logger.isTraceEnabled()) {
			logger.trace("Reading " + fileList.size() + " file(s)");
		}
		pointerMap = new HashMap<File,Long>(fileList.size(),1);
		for(FileState state : fileList) {
			eventCount += readFile(state, spoolSize - eventCount);
		}
		if(eventCount > 0) {
			try {
				adapter.sendEvents(eventList);
			} catch(AdapterException e) {
				eventList.clear(); // Be sure no events will be sent twice after reconnect
				throw e;
			}
		}
		for(FileState state : fileList) {
			state.setPointer(pointerMap.get(state.getFile()));
		}
		eventList.clear();
		return eventCount; // Return number of events sent to adapter
	}

	private int readFile(FileState state, int spaceLeftInSpool) {
		File file = state.getFile();
		long pointer = state.getPointer();
		int numberOfEvents = 0;
		try {
			if(state.isDeleted() || state.getLogFile() == null) { // Don't try to read this file
				if(logger.isTraceEnabled()) {
					logger.trace("File : " + file + " has been deleted");
				}
			} else if(state.getLogFile().isEmpty()) {
				if(logger.isTraceEnabled()) {
					logger.trace("File : " + file + " is empty");
				}
			} else {
				int eventListSizeBefore = eventList.size();
				if(logger.isTraceEnabled()) {
					logger.trace("File : " + file + " pointer : " + pointer);
					logger.trace("Space left in spool : " + spaceLeftInSpool);
				}
				if(isCompressedFile(state)) {
					pointer = file.length();
				} else {
					pointer = readLines(state, spaceLeftInSpool);
				}
				numberOfEvents = eventList.size() - eventListSizeBefore;
			}
		} catch(IOException e) {
			logger.warn("Exception raised while reading file : " + state.getFile(), e);
		}
		pointerMap.put(file, pointer);
		return numberOfEvents; // Return number of events read
	}

	private boolean isCompressedFile(FileState state) {
		LogFile logFile = state.getLogFile();
		if (!(logFile instanceof RandomAccessFile)) return false;
		RandomAccessFile reader = (RandomAccessFile) logFile;

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
			logger.warn("Exception raised while reading file : " + state.getFile(), e);
		}
		return false;
	}

	private static byte[] extractBytes(ByteBuffer byteBuffer)
	{
		byte[] bytes = new byte[byteBuffer.position()];
		byteBuffer.rewind();
		byteBuffer.get(bytes);
		byteBuffer.clear();
		return bytes;
	}

	private static void copyLineToBuffer(byte[] line, ByteBuffer byteBuffer) {
		if(line.length <= byteBuffer.remaining()) {
			byteBuffer.put(line);
		} else {
			byteBuffer.put(line, 0, byteBuffer.remaining());
		}
	}

	private long readLines(FileState state, int spaceLeftInSpool) {
		LogFile reader = state.getLogFile();
		long pos = state.getPointer();
		Multiline multiline = state.getMultiline();
		if (spaceLeftInSpool < 1) return pos;
		try {
			reader.seek(pos);
			byte[] line = readLine(reader);
			bufferedLines.clear();

			if(multiline != null && multiline.isPrevious()) {
				spaceLeftInSpool--;
			}
			while (line != null && spaceLeftInSpool > 0) {
				if(logger.isDebugEnabled()) {
					logger.debug("-- Read line : " + new String(line));
					logger.debug("-- Space left in spool : " + spaceLeftInSpool);
				}
				pos = reader.getFilePointer();
				if(multiline == null) {
					addEvent(state, pos, line);
					spaceLeftInSpool--;
				}
				else {
					if(logger.isDebugEnabled()) {
						logger.debug("-- Multiline : " + multiline + " matches " + multiline.isPatternFound(line));
					}
					if(multiline.isPatternFound(line)) {
						// buffer the line
						if(bufferedLines.position() > 0 && bufferedLines.hasRemaining()) {
							bufferedLines.put(Multiline.JOINT);
						}
						copyLineToBuffer(line, bufferedLines);
					}
					else {
						if(multiline.isPrevious()) {
							// did not match, so new event started
							if (bufferedLines.position() > 0) {
								addEvent(state, pos, extractBytes(bufferedLines));
								spaceLeftInSpool--;
							}
							copyLineToBuffer(line, bufferedLines);
						}
						else {
							// did not match, add the current line
							if(bufferedLines.position() > 0) {
								if(bufferedLines.hasRemaining()) {
									bufferedLines.put(Multiline.JOINT);
								}
								copyLineToBuffer(line, bufferedLines);
								addEvent(state, pos, extractBytes(bufferedLines));
								spaceLeftInSpool--;
							}
							else {
								addEvent(state, pos, line);
								spaceLeftInSpool--;
							}
						}
					}
				}
				if (spaceLeftInSpool > 0) line = readLine(reader);
			}
			if(bufferedLines.position() > 0) {
				addEvent(state, pos, extractBytes(bufferedLines)); // send any buffered lines left
			}
			reader.seek(pos); // Ensure we can re-read if necessary
		} catch(IOException e) {
			logger.warn("Exception raised while reading file : " + state.getFile(), e);
		}
		return pos;
	}

	private byte[] readLine(LogFile reader) throws IOException {
		byteBuffer.clear();
		int ch;
		boolean seenCR = false;
		while((ch=reader.read()) != -1) {
			switch(ch) {
			case '\n':
				return extractBytes(byteBuffer);
			case '\r':
				seenCR = true;
				break;
			default:
				if (seenCR) {
					if(byteBuffer.hasRemaining()) {
						byteBuffer.put((byte) '\r');
					}
					seenCR = false;
				}
				if(byteBuffer.hasRemaining()) {
					byteBuffer.put((byte)ch);
				}
			}
		}
		return null;
	}

}
