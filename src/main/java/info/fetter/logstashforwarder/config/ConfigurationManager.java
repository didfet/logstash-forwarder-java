package info.fetter.logstashforwarder.config;

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
