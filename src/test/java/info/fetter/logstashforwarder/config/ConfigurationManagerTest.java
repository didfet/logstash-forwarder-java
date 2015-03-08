package info.fetter.logstashforwarder.config;

import static org.apache.log4j.Level.DEBUG;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.RootLogger;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

public class ConfigurationManagerTest {
	Logger logger = Logger.getLogger(ConfigurationManagerTest.class);

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		BasicConfigurator.configure();
		RootLogger.getRootLogger().setLevel(DEBUG);
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		BasicConfigurator.resetConfiguration();
	}
	
	@Test
	public void testReadConfig1() throws JsonParseException, JsonMappingException, IOException {
		ConfigurationManager manager = new ConfigurationManager(new File(ConfigurationManagerTest.class.getClassLoader().getResource("config1.json").getFile()));
		manager.readConfiguration();
		logger.debug(manager.getConfig().toString());
	}
}
