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
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.RootLogger;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class InputReaderTest {
	Logger logger = Logger.getLogger(InputReaderTest.class);

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
	public void testInputReader1() throws IOException, InterruptedException, AdapterException {
		PipedInputStream in = new PipedInputStream();
		PipedOutputStream out = new PipedOutputStream(in);
		PrintWriter writer = new PrintWriter(out);
		InputReader reader = new InputReader(2, in, null);
		reader.setAdapter(new MockProtocolAdapter());

		reader.readInput();

		writer.println("line1");
		writer.flush();
		reader.readInput();

		writer.print("line2");
		writer.flush();
		reader.readInput();

		writer.println();
		writer.flush();
		reader.readInput();

		writer.println("line3");
		writer.println("line4");
		writer.println("line5");
		writer.flush();
		reader.readInput();

		reader.readInput();
		reader.readInput();

		while(in.available() > 0) {
			logger.trace("read : " + in.read());
		}

		writer.close();
	}
}
