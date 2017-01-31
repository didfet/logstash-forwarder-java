package info.fetter.logstashforwarder.config;

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

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.lang.builder.ToStringBuilder;

import com.fasterxml.jackson.annotation.JsonProperty;
import info.fetter.logstashforwarder.Multiline;
import java.io.UnsupportedEncodingException;

public class FilesSection {
	private List<String> paths;
	private Map<String,String> fields;
	@JsonProperty("dead time")
	private String deadTime = "24h";
	private Multiline multiline;

	public List<String> getPaths() {
		return paths;
	}

	public void setPaths(List<String> paths) {
		this.paths = paths;
	}

	public Map<String, String> getFields() {
		return fields;
	}

	public void setFields(Map<String, String> fields) {
		this.fields = fields;
	}

	public String getDeadTime() {
		return deadTime;
	}

	public long getDeadTimeInSeconds() {
		long deadTimeInSeconds = 0;
		String remaining = deadTime;

		if(deadTime.contains("h")) {
			String[] splitByHour = deadTime.split("h",2);
			if(splitByHour.length > 1) {
				remaining = splitByHour[1];
			}
			deadTimeInSeconds += Integer.parseInt(splitByHour[0]) * 3600;
		}
		if(remaining.contains("m")) {
			String[] splitByMinute = remaining.split("m",2);
			if(splitByMinute.length > 1) {
				remaining = splitByMinute[1];
			}
			deadTimeInSeconds += Integer.parseInt(splitByMinute[0]) * 60;
		}
		if(remaining.contains("s")) {
			String[] splitBySecond = remaining.split("s",2);
			deadTimeInSeconds += Integer.parseInt(splitBySecond[0]);
		}
		return deadTimeInSeconds;
	}

	public void setDeadTime(String deadTime) {
		this.deadTime = deadTime;
	}

	public Multiline getMultiline() {
		return multiline;
	}

	public void setMultiline(Map<String, String> multilineMap) throws UnsupportedEncodingException {
		this.multiline = new Multiline(multilineMap);
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).
				append("paths", paths).
				append("fields", fields).
				append("dead time", deadTime).
				append("multiline", multiline).
				toString();
	}
}
