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

import static org.apache.log4j.Level.DEBUG;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.RootLogger;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

public class RegistrarTest {
	Logger logger = Logger.getLogger(RegistrarTest.class);

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
	public void testReadState1() throws JsonParseException, JsonMappingException, IOException {
		FileState[] states = Registrar.readStateFromJson(new File(RegistrarTest.class.getClassLoader().getResource("state1.json").getFile()));
		for(FileState state : states) {
			logger.debug("Loaded state : " + state);
		}
	}
	
	@Test
	public void testWriteState2() throws JsonGenerationException, JsonMappingException, IOException {
		FileState state1 = new FileState();
		state1.setDirectory("/directory1");
		state1.setFileName("file1");
		state1.setPointer(1234);
		state1.setSignature(123456);
		state1.setSignatureLength(135);
		FileState state2 = new FileState();
		state2.setDirectory("/directory2");
		state2.setFileName("file2");
		state2.setPointer(4321);
		state2.setSignature(654321);
		state2.setSignatureLength(531);
		List<FileState> stateList = new ArrayList<FileState>(2);
		stateList.add(state1);
		stateList.add(state2);
		File file = new File("state2.json");
		logger.debug("Writing to file : " + file.getCanonicalPath());
		Registrar.writeStateToJson(file, stateList);
	}
}
