package info.fetter.logstashforwarder;

import java.util.HashMap;
import java.util.Map;

public class Event {
	private Map<String,byte[]> keyValues = new HashMap<String,byte[]>(10);
	
	public void addField(String key, byte[] value) {
		keyValues.put(key, value);
	}
	
	public void addField(String key, String value) {
		keyValues.put(key, value.getBytes());
	}
	
	public Map<String,byte[]> getKeyValues() {
		return keyValues;
	}
}
