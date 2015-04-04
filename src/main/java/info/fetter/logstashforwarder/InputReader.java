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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.log4j.Logger;

public class InputReader extends Reader {
	private static Logger logger = Logger.getLogger(InputReader.class);
	private BufferedReader reader;
	private long position = 0;
	private Event fields;

	public InputReader(int spoolSize, InputStream in, Event fields) {
		super(spoolSize);
		reader = new BufferedReader(new InputStreamReader(in));
		this.fields = fields;
		stringBuilder = new StringBuilder(STRINGBUILDER_INITIAL_CAPACITY);
	}
	
	public int readInput() throws AdapterException, IOException {
		int eventCount = 0;
		logger.trace("Reading stdin");
		
		eventCount += readLines();
		
		if(eventCount > 0) {
			adapter.sendEvents(eventList);
		}
		
		eventList.clear();
		return eventCount;
	}
	
	private int readLines() throws IOException {
		int lineCount = 0;
		String line;
		while(lineCount < spoolSize && (line = readLine()) != null) {
			position += line.length();
			lineCount++;
			addEvent("stdin", fields, position, line);
		}
		return lineCount;
	}
	
	private String readLine() throws IOException {
		int ch;
		boolean seenCR = false;
		String line;
		while(reader.ready()) {
			ch=reader.read();
			switch(ch) {
			case '\n':
				line = stringBuilder.toString();
				stringBuilder.setLength(0);
				return line;
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
