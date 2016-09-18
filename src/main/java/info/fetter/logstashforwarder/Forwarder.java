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

import java.io.IOException;
import java.util.List;
import java.util.Random;

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
import org.apache.log4j.Appender;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Layout;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.RollingFileAppender;
import org.apache.log4j.spi.RootLogger;

public class Forwarder {
	private static final String SINCEDB = ".logstash-forwarder-java";
	private static Logger logger = Logger.getLogger(Forwarder.class);
	private static int spoolSize = 1024;
	private static int idleTimeout = 5000;
	private static int networkTimeout = 15000;
	private static String config;
	private static ConfigurationManager configManager;
	private static FileWatcher watcher;
	private static FileReader fileReader;
	private static InputReader inputReader;
	private static Level logLevel = INFO;
	private static boolean debugWatcherSelected = false;
	private static ProtocolAdapter adapter;
	private static Random random = new Random();
	private static int signatureLength = 4096;
	private static boolean tailSelected = false;
	private static String logfile = null;
	private static String logfileSize = "10MB";
	private static int logfileNumber = 5;
	private static String sincedbFile = SINCEDB;

	public static void main(String[] args) {
		try {
			parseOptions(args);
			setupLogging();
			watcher = new FileWatcher();
			watcher.setMaxSignatureLength(signatureLength);
			watcher.setTail(tailSelected);
			watcher.setSincedb(sincedbFile);
			configManager = new ConfigurationManager(config);
			configManager.readConfiguration();
			for(FilesSection files : configManager.getConfig().getFiles()) {
				for(String path : files.getPaths()) {
					watcher.addFilesToWatch(path, new Event(files.getFields()), files.getDeadTimeInSeconds() * 1000, files.getMultiline());
				}
			}
			watcher.initialize();
			fileReader = new FileReader(spoolSize);
			inputReader = new InputReader(spoolSize, System.in);
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
				while(watcher.readFiles(fileReader) == spoolSize);
				while(watcher.readStdin(inputReader) == spoolSize);
				Thread.sleep(idleTimeout);
			} catch(AdapterException e) {
				logger.error("Lost server connection");
				Thread.sleep(networkTimeout);
				connectToServer();
			}
		}
	}

	private static void connectToServer() {
		int randomServerIndex = 0;
		List<String> serverList = configManager.getConfig().getNetwork().getServers();
		networkTimeout = configManager.getConfig().getNetwork().getTimeout() * 1000;
		if(adapter != null) {
			try {
				adapter.close();
			} catch(AdapterException e) {
				logger.error("Error while closing connection to " + adapter.getServer() + ":" + adapter.getPort());
			} finally {
				adapter = null;
			}
		}
		while(adapter == null) {
			try {
				randomServerIndex = random.nextInt(serverList.size());
				String[] serverAndPort = serverList.get(randomServerIndex).split(":");
				logger.info("Trying to connect to " + serverList.get(randomServerIndex));
				adapter = new LumberjackClient(configManager.getConfig().getNetwork().getSslCA(),serverAndPort[0],Integer.parseInt(serverAndPort[1]), networkTimeout);
				fileReader.setAdapter(adapter);
				inputReader.setAdapter(adapter);
			} catch(Exception ex) {
				if(logger.isDebugEnabled()) {
					logger.error("Failed to connect to server " + serverList.get(randomServerIndex) + " : ", ex);
				} else {
					logger.error("Failed to connect to server " + serverList.get(randomServerIndex) + " : " + ex.getMessage());
				}
				try {
					Thread.sleep(networkTimeout);
				} catch (InterruptedException e) {
					logger.error(e.getMessage());
				}
			}
		}
	}

	@SuppressWarnings("static-access")
	static void parseOptions(String[] args) {
		Options options = new Options();
		Option helpOption = new Option("help", "print this message");
		Option quietOption = new Option("quiet", "operate in quiet mode - only emit errors to log");
		Option debugOption = new Option("debug", "operate in debug mode");
		Option debugWatcherOption = new Option("debugwatcher", "operate watcher in debug mode");
		Option traceOption = new Option("trace", "operate in trace mode");
		Option tailOption = new Option("tail", "read new files from the end");

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
		Option signatureLengthOption = OptionBuilder.withArgName("signature length")
				.hasArg()
				.withDescription("Maximum length of file signature")
				.create("signaturelength");
		Option logfileOption = OptionBuilder.withArgName("logfile name")
				.hasArg()
				.withDescription("Logfile name")
				.create("logfile");
		Option logfileSizeOption = OptionBuilder.withArgName("logfile size")
				.hasArg()
				.withDescription("Logfile size (default 10M)")
				.create("logfilesize");
		Option logfileNumberOption = OptionBuilder.withArgName("number of logfiles")
				.hasArg()
				.withDescription("Number of logfiles (default 5)")
				.create("logfilenumber");
		Option sincedbOption = OptionBuilder.withArgName("sincedb file")
				.hasArg()
				.withDescription("Sincedb file name")
				.create("sincedb");

		options.addOption(helpOption)
		.addOption(idleTimeoutOption)
		.addOption(spoolSizeOption)
		.addOption(quietOption)
		.addOption(debugOption)
		.addOption(debugWatcherOption)
		.addOption(traceOption)
		.addOption(tailOption)
		.addOption(signatureLengthOption)
		.addOption(configOption)
		.addOption(logfileOption)
		.addOption(logfileNumberOption)
		.addOption(logfileSizeOption)
		.addOption(sincedbOption);
		
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
			if(line.hasOption("signaturelength")) {
				signatureLength = Integer.parseInt(line.getOptionValue("signaturelength"));
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
			if(line.hasOption("debugwatcher")) {
				debugWatcherSelected = true;
			}
			if(line.hasOption("tail")) {
				tailSelected = true;
			}
			if(line.hasOption("logfile")) {
				logfile = line.getOptionValue("logfile");
			}
			if(line.hasOption("logfilesize")) {
				logfileSize = line.getOptionValue("logfilesize");
			}
			if(line.hasOption("logfilenumber")) {
				logfileNumber = Integer.parseInt(line.getOptionValue("logfilenumber"));
			}
			if(line.hasOption("sincedb")) {
				sincedbFile = line.getOptionValue("sincedb");
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

	private static void setupLogging() throws IOException {
		Appender appender;
		Layout layout = new PatternLayout("%d %p %c{1} - %m%n");
		if(logfile == null) {
			appender = new ConsoleAppender(layout);
		} else {
			RollingFileAppender rolling = new RollingFileAppender(layout, logfile, true);
			rolling.setMaxFileSize(logfileSize);
			rolling.setMaxBackupIndex(logfileNumber);
			appender = rolling;
		}
		BasicConfigurator.configure(appender);
		RootLogger.getRootLogger().setLevel(logLevel);
		if(debugWatcherSelected) {
			Logger.getLogger(FileWatcher.class).addAppender(appender);
			Logger.getLogger(FileWatcher.class).setLevel(DEBUG);
			Logger.getLogger(FileWatcher.class).setAdditivity(false);
		}
		//			Logger.getLogger(FileReader.class).addAppender((Appender)RootLogger.getRootLogger().getAllAppenders().nextElement());
		//			Logger.getLogger(FileReader.class).setLevel(TRACE);
		//			Logger.getLogger(FileReader.class).setAdditivity(false);
	}

}
