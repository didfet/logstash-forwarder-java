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
import java.util.Map;
import java.util.regex.Pattern;
import org.apache.commons.lang.builder.ToStringBuilder;

public class Multiline {
	public enum WhatType { Previous, Next };
	public static byte JOINT = (byte) ' ';

	private Pattern pattern = null;
	private boolean negate = false;
	private WhatType what = WhatType.Previous;

	public Multiline() {
	}

	public Multiline(Multiline event) {
		if(event != null) {
			this.negate = event.negate;
			this.pattern = event.pattern;
			this.what = event.what;
		}
	}

	public Multiline(Map<String,String> fields) throws UnsupportedEncodingException {
		String strPattern = "";
		for(String key : fields.keySet()) {
			if ("pattern".equals(key))
				strPattern = fields.get(key);
			else if ("negate".equals(key))
				negate = Boolean.parseBoolean(fields.get(key));
			else if ("what".equals(key))
				what = WhatType.valueOf(fields.get(key));
			else
				throw new UnsupportedEncodingException(key + " not supported");
		}
		pattern = Pattern.compile(strPattern);

	}

	public Pattern getPattern() {
		return pattern;
	}

	public boolean isNegate() {
		return negate;
	}

	public WhatType getWhat() {
		return what;
	}

	public boolean isPrevious() {
		return what == WhatType.Previous;
	}

	public boolean isPatternFound (byte[] line) {
		boolean result = pattern.matcher(new String(line)).find();
		if (negate) return !result;
		return result;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).
			append("pattern", pattern).
			append("negate", negate).
			append("what", what).
			toString();
	}
}
