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

import static org.apache.log4j.Level.DEBUG;
import static org.junit.Assert.*;

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
		for(FilesSection files : manager.getConfig().getFiles()) {
			logger.debug("File Section");
			for(String path : files.getPaths()) {
				logger.debug(" - Path : " + path);
			}
			logger.debug(" - Multiline : " + files.getMultiline());
			logger.debug(" - Dead time : " + files.getDeadTimeInSeconds());
			if(files.getDeadTime().equals("24h")) {
				assertEquals(86400, files.getDeadTimeInSeconds());
			} else if(files.getDeadTime().equals("12h")) {
				assertEquals(43200, files.getDeadTimeInSeconds());
			} else if(files.getDeadTime().equals("8h32m50s")) {
				assertEquals(30770, files.getDeadTimeInSeconds());
			}
		}
	}
}
