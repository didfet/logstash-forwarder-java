package info.fetter.logstashforwarder;

/*
 * Copyright 2017 Alberto González Palomo http://sentido-labs.com
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

public class Filter {
	private Pattern pattern = null;
	private boolean negate = false;
	private String charset = "UTF-8";

	public Filter() {
	}

	public Filter(Filter event) {
		if(event != null) {
			this.negate = event.negate;
			this.pattern = event.pattern;
		}
	}

	public Filter(Map<String,String> fields) throws UnsupportedEncodingException {
		String strPattern = "";
		for(String key : fields.keySet()) {
			if ("pattern".equals(key))
				strPattern = fields.get(key);
			else if ("negate".equals(key))
				negate = Boolean.parseBoolean(fields.get(key));
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

	public boolean accept (byte[] line) {
		try {
			return accept(new String(line, charset));
		} catch (UnsupportedEncodingException e) {
			System.err.println("ERROR: unsupported encoding " + charset);
			System.err.flush();// In case we crash at new String(line),
			// because the behaviour if the bytes are not decodable with
			// the platform's default encoding is undefined.
			// Last ditch effort, decode with the platform's encoding:
			return accept(new String(line));
		}
	}

	public boolean accept (String line) {
		boolean result = pattern.matcher(line).find();
		if (negate) return !result;
		return result;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).
			append("pattern", pattern).
			append("negate", negate).
			toString();
	}
}
