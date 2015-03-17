package info.fetter.logstashforwarder;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

public class FileReader {
	private static Logger logger = Logger.getLogger(FileReader.class);
	private ProtocolAdapter adapter;
	private int spoolSize = 0;
	private List<Event> eventList;
	private Map<File,Long> pointerMap;
	private String hostname;
	{
		try {
			hostname = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			throw new RuntimeException(e);
		}
	}

	public FileReader(int spoolSize) {
		this.spoolSize = spoolSize;
		eventList = new ArrayList<Event>(spoolSize);
	}

	public int readFiles(Collection<FileState> fileList) throws IOException {
		int eventCount = 0;
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

	private int readFile(FileState state, int spaceLeftInSpool) throws IOException {
		int eventListSizeBefore = eventList.size();
		File file = state.getFile();
		long pointer = state.getPointer();
		if(logger.isTraceEnabled()) {
			logger.trace("File : " + file.getCanonicalPath() + " pointer : " + pointer);
			logger.trace("Space left in spool : " + spaceLeftInSpool);
		}
		pointer = readLines(state, spaceLeftInSpool);
		pointerMap.put(file, pointer);
		return eventList.size() - eventListSizeBefore; // Return number of events read
	}

	private long readLines(FileState state, int spaceLeftInSpool) throws IOException {
		RandomAccessFile reader = state.getRandomAccessFile();
		long pos = state.getPointer();
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
		return pos;
	}

	private String readLine(RandomAccessFile reader) throws IOException {
		StringBuffer sb  = new StringBuffer();
		int ch;
		boolean seenCR = false;
		while((ch=reader.read()) != -1) {
			switch(ch) {
			case '\n':
				return sb.toString();
			case '\r':
				seenCR = true;
				break;
			default:
				if (seenCR) {
					sb.append('\r');
					seenCR = false;
				}
				sb.append((char)ch); // add character, not its ascii value
			}
		}
		return null;
	}

	private void addEvent(FileState state, long pos, String line) throws IOException {
		Event event = new Event(state.getFields());
		event.addField("file", state.getFile().getCanonicalPath())
		.addField("offset", pos)
		.addField("line", line)
		.addField("host", hostname);
		eventList.add(event);
	}

	public ProtocolAdapter getAdapter() {
		return adapter;
	}

	public void setAdapter(ProtocolAdapter adapter) {
		this.adapter = adapter;
	}

}
