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

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;


public abstract class Reader {
	protected ProtocolAdapter adapter;
	protected int spoolSize = 0;
	protected List<Event> eventList;
	protected final int BYTEBUFFER_CAPACITY = 1024 * 1024;
	protected ByteBuffer byteBuffer = ByteBuffer.allocate(BYTEBUFFER_CAPACITY);
	private String hostname;
	{
		try {
			hostname = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			throw new RuntimeException(e);
		}
	}

	protected Reader(int spoolSize) {
		this.spoolSize = spoolSize;
		eventList = new ArrayList<Event>(spoolSize);
	}

	protected void addEvent(FileState state, long pos, String line) throws IOException {
		addEvent(state.getFile().getCanonicalPath(), state.getFields(), pos, line);
	}

	protected void addEvent(FileState state, long pos, byte[] line) throws IOException {
		addEvent(state.getFile().getCanonicalPath(), state.getFields(), pos, line);
	}

	protected void addEvent(String fileName, Event fields, long pos, byte[] line) throws IOException {
		Event event = new Event(fields);
		event.addField("file", fileName)
		.addField("offset", pos)
		.addField("line", line)
		.addField("host", hostname);
		eventList.add(event);
	}

	protected void addEvent(String fileName, Event fields, long pos, String line) throws IOException {
		Event event = new Event(fields);
		event.addField("file", fileName)
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
