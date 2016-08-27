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
import info.fetter.logstashforwarder.config.Parameters;
import info.fetter.logstashforwarder.config.ParametersManager;
import info.fetter.logstashforwarder.protocol.LumberjackClient;
import info.fetter.logstashforwarder.util.AdapterException;

import org.apache.log4j.Appender;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Layout;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.RollingFileAppender;
import org.apache.log4j.spi.RootLogger;

public class Forwarder {
	private static Logger logger = Logger.getLogger(Forwarder.class);
	private static ConfigurationManager configManager;
	private static FileWatcher fileWatcher;
	private static InputWatcher inputWatcher;
	private static ProtocolAdapter adapter;
	private static Random random = new Random();
	private static Parameters parameters;
	private static int networkTimeout = 15000;

	public static void main(String[] args) {
		try {
			parameters = ParametersManager.parseOptions(args);
			setupLogging();
			fileWatcher = new FileWatcher();
			fileWatcher.setParameters(parameters);
			inputWatcher = new InputWatcher();
			configManager = new ConfigurationManager(parameters.getConfigFile());
			configManager.readConfiguration();
			for(FilesSection files : configManager.getConfig().getFiles()) {
				inputWatcher.addFilesToWatch(files);
				fileWatcher.addFilesToWatch(files);
			}
			fileWatcher.initialize();
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
				fileWatcher.checkFiles();
				while(fileWatcher.readFiles() == parameters.getSpoolSize());
				while(inputWatcher.readFiles() == parameters.getSpoolSize());
				Thread.sleep(parameters.getIdleTimeout());
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
				fileWatcher.getReader().setAdapter(adapter);
				inputWatcher.getReader().setAdapter(adapter);
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



	private static void setupLogging() throws IOException {
		Appender appender;
		Layout layout = new PatternLayout("%d %p %c{1} - %m%n");
		if(parameters.getLogfile() == null) {
			appender = new ConsoleAppender(layout);
		} else {
			RollingFileAppender rolling = new RollingFileAppender(layout, parameters.getLogfile(), true);
			rolling.setMaxFileSize(parameters.getLogfileSize());
			rolling.setMaxBackupIndex(parameters.getLogfileNumber());
			appender = rolling;
		}
		BasicConfigurator.configure(appender);
		RootLogger.getRootLogger().setLevel(parameters.getLogLevel());
		if(parameters.isDebugWatcherSelected()) {
			Logger.getLogger(FileWatcher.class).addAppender(appender);
			Logger.getLogger(FileWatcher.class).setLevel(DEBUG);
			Logger.getLogger(FileWatcher.class).setAdditivity(false);
		}
		//			Logger.getLogger(FileReader.class).addAppender((Appender)RootLogger.getRootLogger().getAllAppenders().nextElement());
		//			Logger.getLogger(FileReader.class).setLevel(TRACE);
		//			Logger.getLogger(FileReader.class).setAdditivity(false);
	}

}
