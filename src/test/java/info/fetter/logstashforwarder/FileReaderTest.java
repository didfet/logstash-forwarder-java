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

import static org.apache.log4j.Level.*;
import info.fetter.logstashforwarder.util.AdapterException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.RootLogger;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class FileReaderTest {
	Logger logger = Logger.getLogger(FileReaderTest.class);

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		BasicConfigurator.configure();
		RootLogger.getRootLogger().setLevel(TRACE);
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		BasicConfigurator.resetConfiguration();
	}

	@Test
	public void testFileReader1() throws IOException, InterruptedException, AdapterException {
		FileReader reader = new FileReader(2);
		reader.setAdapter(new MockProtocolAdapter());
		List<FileState> fileList = new ArrayList<FileState>(1);
		File file1 = new File("testFileReader1.txt");
		FileUtils.write(file1, "testFileReader1 line1\n");
		FileUtils.write(file1, " nl line12\n", true);
		FileUtils.write(file1, "testFileReader1 line2\n", true);
		FileUtils.write(file1, "testFileReader1 line3\n", true);
		Thread.sleep(500);
		FileState state = new FileState(file1);
		fileList.add(state);
		state.setFields(new Event().addField("testFileReader1", "testFileReader1"));
		Map<String, String> m = new HashMap<String, String>();
		m.put("pattern", " nl");
		m.put("negate", "false");
		state.setMultiline(new Multiline(m));
		reader.readFiles(fileList);
		reader.readFiles(fileList);
		reader.readFiles(fileList);
		//FileUtils.forceDelete(file1);
	}

        @Test
	public void testFileReader2() throws IOException, InterruptedException, AdapterException {
		FileReader reader = new FileReader(2);
		reader.setAdapter(new MockProtocolAdapter());
		List<FileState> fileList = new ArrayList<FileState>(1);
		File file1 = new File("testFileReader1.txt");
		FileUtils.write(file1, "testFileReader1 line1\n");
		FileUtils.write(file1, " nl line12\n", true);
		FileUtils.write(file1, "testFileReader1 line2\n", true);
		FileUtils.write(file1, "testFileReader1 line3\n", true);
		Thread.sleep(500);
		FileState state = new FileState(file1);
		fileList.add(state);
		state.setFields(new Event().addField("testFileReader1", "testFileReader1"));
		Map<String, String> m = new HashMap<String, String>();
		m.put("pattern", "testFileReader1");
		m.put("negate", "true");
		state.setMultiline(new Multiline(m));
		reader.readFiles(fileList);
		reader.readFiles(fileList);
		reader.readFiles(fileList);
		//FileUtils.forceDelete(file1);
	}
}
