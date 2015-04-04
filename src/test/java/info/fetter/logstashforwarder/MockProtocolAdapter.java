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

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

public class MockProtocolAdapter implements ProtocolAdapter {
	private static Logger logger = Logger.getLogger(MockProtocolAdapter.class);
	private List<Event> lastEvents;

	public int sendEvents(List<Event> eventList) {
		for(Event event : eventList) {
			logger.trace("Event :");
			for(String key : event.getKeyValues().keySet()) {
				logger.trace("-- " + key  + ":" + new String(event.getKeyValues().get(key)));
			}
		}
		lastEvents = new ArrayList<Event>(eventList);
		return eventList.size();
	}
	
	public List<Event> getLastEvents() {
		return lastEvents;
	}

	public void close() {
		// not implemented
	}

	public String getServer() {
		// TODO Auto-generated method stub
		return "";
	}

	public int getPort() {
		// TODO Auto-generated method stub
		return 0;
	}

}
