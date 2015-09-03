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

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class Event {
	private Map<String,byte[]> keyValues = new HashMap<String,byte[]>(10);
	
	public Event() {
	}
	
	public Event(Event event) {
		if(event != null) {
			keyValues.putAll(event.keyValues);
		}
	}
	
	public Event(Map<String,String> fields) throws UnsupportedEncodingException {
		for(String key : fields.keySet()) {
			addField(key, fields.get(key));
		}
	}
	
	public Event addField(String key, byte[] value) {
		keyValues.put(key, value);
		return this;
	}
	
	public Event addField(String key, String value) throws UnsupportedEncodingException {
		keyValues.put(key, value.getBytes());
		return this;
	}
	
	public Event addField(String key, long value) throws UnsupportedEncodingException {
		keyValues.put(key, String.valueOf(value).getBytes());
		return this;
	}
	
	public Map<String,byte[]> getKeyValues() {
		return keyValues;
	}
	
	public byte[] getValue(String fieldName) {
		return keyValues.get(fieldName);
	}
}
