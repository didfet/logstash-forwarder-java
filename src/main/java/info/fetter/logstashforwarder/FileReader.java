package info.fetter.logstashforwarder;

import java.util.ArrayList;
import java.util.List;

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
	private static final int DEFAULT_SPOOL_SIZE = 1024;
	private ProtocolAdapter adapter;
	private int spoolSize = DEFAULT_SPOOL_SIZE;
	private List<Event> eventList;
	
	public FileReader(int spoolSize) {
		this.spoolSize = spoolSize;
		eventList = new ArrayList<Event>(spoolSize);
	}
	
	public int readFiles(List<FileState> fileList) {
		// TODO: Read files and send events until there's nothing left to read or spool size reached
		int eventCounter = 0;
		for(FileState state : fileList) {
			eventCounter += readFile(state, spoolSize - eventCounter);
		}
		return 0; // Return number of events sent to adapter
	}

	private int readFile(FileState state, int spaceLeftInSpool) {
		// TODO Read file
		return 0; // Return number of events read
	}

	public ProtocolAdapter getAdapter() {
		return adapter;
	}

	public void setAdapter(ProtocolAdapter adapter) {
		this.adapter = adapter;
	}

}
