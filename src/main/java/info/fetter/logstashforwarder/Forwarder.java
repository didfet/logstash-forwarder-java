package info.fetter.logstashforwarder;

import info.fetter.logstashforwarder.config.ConfigurationManager;
import info.fetter.logstashforwarder.config.FilesSection;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

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
	private static int spoolSize = 1024;
	private static int idleTimeout = 5000;
	private static String config;
	private static ConfigurationManager configManager;
	private static FileWatcher watcher = new FileWatcher();
	private static FileReader reader;

	static void main(String[] args) {
		try {
			parseOptions(args);
			configManager = new ConfigurationManager(config);
			configManager.readConfiguration();
			for(FilesSection files : configManager.getConfig().getFiles()) {
				for(String path : files.getPaths()) {
					watcher.addFilesToWatch(path, new Event(files.getFields()), FileWatcher.ONE_DAY);
				}
			}
			reader = new FileReader(spoolSize);
		} catch(Exception e) {
			System.err.println(e.getMessage());
			System.exit(3);
		}
	}

	@SuppressWarnings("static-access")
	static void parseOptions(String[] args) {
		Options options = new Options();
		Option helpOption = new Option("help", "print this message");

		Option spoolSizeOption = OptionBuilder.withArgName("number of events")
				.hasArg()
				.withDescription("event count spool threshold - forces network flush")
				.create("spool-size");
		Option idleTimeoutOption = OptionBuilder.withArgName("")
				.hasArg()
				.withDescription("time between file reads in seconds")
				.create("idle-timeout");
		Option configOption = OptionBuilder.withArgName("config file")
				.hasArg()
				.isRequired()
				.withDescription("path to logstash-forwarder configuration file")
				.create("config");

		options.addOption(helpOption).addOption(idleTimeoutOption).addOption(spoolSizeOption).addOption(configOption);	
		CommandLineParser parser = new BasicParser();
		try {
			CommandLine line = parser.parse(options, args);
			if(line.hasOption("spool-size")) {
				spoolSize = Integer.parseInt(line.getOptionValue("spool-size"));
			}
			if(line.hasOption("idle-timeout")) {
				idleTimeout = Integer.parseInt(line.getOptionValue("idle-timeout"));
			}
			if(line.hasOption("config")) {
				config = line.getOptionValue("config");
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
