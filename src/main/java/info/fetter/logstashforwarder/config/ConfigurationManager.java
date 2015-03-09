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

import java.io.File;
import java.io.IOException;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ConfigurationManager {
	private File configFile;
	private Configuration config;
	private ObjectMapper mapper;
	
	public ConfigurationManager(String configFilePath) {
		this(new File(configFilePath));
	}
	
	public ConfigurationManager(File file) {
		configFile = file;
		mapper = new ObjectMapper();
		mapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
		mapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
	}
	
	public void readConfiguration() throws JsonParseException, JsonMappingException, IOException {
		config = mapper.readValue(configFile, Configuration.class);
	}
	
	public void writeConfiguration() {
	}

	/**
	 * @return the config
	 */
	public Configuration getConfig() {
		return config;
	}
	
}
