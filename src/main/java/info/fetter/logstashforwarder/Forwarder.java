package info.fetter.logstashforwarder;

import static org.apache.log4j.Level.*;

import java.io.IOException;

import info.fetter.logstashforwarder.config.ConfigurationManager;
import info.fetter.logstashforwarder.config.FilesSection;
import info.fetter.logstashforwarder.protocol.LumberjackClient;
import info.fetter.logstashforwarder.util.AdapterException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.RootLogger;

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

public class Forwarder {
	private static Logger logger = Logger.getLogger(Forwarder.class);
	private static int spoolSize = 1024;
	private static int idleTimeout = 5000;
	private static String config;
	private static ConfigurationManager configManager;
	private static FileWatcher watcher;
	private static FileReader reader;
	private static Level logLevel = INFO;
	private static ProtocolAdapter adapter;

	public static void main(String[] args) {
		try {
			parseOptions(args);
			BasicConfigurator.configure();
			RootLogger.getRootLogger().setLevel(logLevel);
			//			Logger.getLogger(FileReader.class).addAppender((Appender)RootLogger.getRootLogger().getAllAppenders().nextElement());
			//			Logger.getLogger(FileReader.class).setLevel(TRACE);
			//			Logger.getLogger(FileReader.class).setAdditivity(false);
			watcher = new FileWatcher();
			configManager = new ConfigurationManager(config);
			configManager.readConfiguration();
			for(FilesSection files : configManager.getConfig().getFiles()) {
				for(String path : files.getPaths()) {
					watcher.addFilesToWatch(path, new Event(files.getFields()), FileWatcher.ONE_DAY);
				}
			}
			reader = new FileReader(spoolSize);
			connectToServer();
			infiniteLoop();
		} catch(Exception e) {
			e.printStackTrace();
			System.exit(3);
		}
	}

	private static void infiniteLoop() throws IOException, InterruptedException {
		while(true) {
			try {
				watcher.checkFiles();
				while(watcher.readFiles(reader) == spoolSize);
				Thread.sleep(idleTimeout);
			} catch(AdapterException e) {
				try {
					logger.error("Lost server connection");
					Thread.sleep(configManager.getConfig().getNetwork().getTimeout() * 1000);
					connectToServer();
				} catch(Exception ex) {
					logger.error("Failed to reconnect to server : " + ex.getMessage());
				}
			}
		}
	}
	
	private static void connectToServer() throws NumberFormatException, IOException {
		String[] serverAndPort = configManager.getConfig().getNetwork().getServers().get(0).split(":");
		adapter = new LumberjackClient(configManager.getConfig().getNetwork().getSslCA(),serverAndPort[0],Integer.parseInt(serverAndPort[1]));
		reader.setAdapter(adapter);
	}

	@SuppressWarnings("static-access")
	static void parseOptions(String[] args) {
		Options options = new Options();
		Option helpOption = new Option("help", "print this message");
		Option quiet = new Option("quiet", "operate in quiet mode - only emit errors to log");
		Option debug = new Option("debug", "operate in debug mode");
		Option trace = new Option("trace", "operate in trace mode");

		Option spoolSizeOption = OptionBuilder.withArgName("number of events")
				.hasArg()
				.withDescription("event count spool threshold - forces network flush")
				.create("spoolsize");
		Option idleTimeoutOption = OptionBuilder.withArgName("")
				.hasArg()
				.withDescription("time between file reads in seconds")
				.create("idletimeout");
		Option configOption = OptionBuilder.withArgName("config file")
				.hasArg()
				.isRequired()
				.withDescription("path to logstash-forwarder configuration file")
				.create("config");

		options.addOption(helpOption)
		.addOption(idleTimeoutOption)
		.addOption(spoolSizeOption)
		.addOption(quiet)
		.addOption(debug)
		.addOption(trace)
		.addOption(configOption);	
		CommandLineParser parser = new GnuParser();
		try {
			CommandLine line = parser.parse(options, args);
			if(line.hasOption("spoolsize")) {
				spoolSize = Integer.parseInt(line.getOptionValue("spoolsize"));
			}
			if(line.hasOption("idletimeout")) {
				idleTimeout = Integer.parseInt(line.getOptionValue("idletimeout"));
			}
			if(line.hasOption("config")) {
				config = line.getOptionValue("config");
			}
			if(line.hasOption("quiet")) {
				logLevel = ERROR;
			}
			if(line.hasOption("debug")) {
				logLevel = DEBUG;
			}
			if(line.hasOption("trace")) {
				logLevel = TRACE;
			}
		} catch(ParseException e) {
			printHelp(options);
			System.exit(1);;
		} catch(NumberFormatException e) {
			System.err.println("Value must be an integer");
			printHelp(options);
			System.exit(2);;
		}
	}

	private static void printHelp(Options options) {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("logstash-forwarder", options);
	}

}
