package info.fetter.logstashforwarder;

import info.fetter.logstashforwarder.config.FilesSection;
import info.fetter.logstashforwarder.config.Parameters;
import info.fetter.logstashforwarder.util.AdapterException;

import java.io.IOException;

import org.apache.log4j.Logger;

public class InputWatcher implements Watcher {
	private static final Logger logger = Logger.getLogger(InputWatcher.class);
	private Event stdinFields;
	private boolean stdinConfigured = false;
	private InputReader reader;

	public void addFilesToWatch(FilesSection files) {
		for(String path : files.getPaths()) {
			if(path.equals("-")) {
				logger.error("Watching stdin");
				stdinFields = new Event(files.getFields());
				stdinConfigured = true;
			}
		}
	}

	private int readStdin(InputReader reader) throws AdapterException, IOException {
		if(stdinConfigured) {
			logger.debug("Reading stdin");
			reader.setFields(stdinFields);
			int numberOfLinesRead = reader.readInput();
			return numberOfLinesRead;
		} else {
			return 0;
		}
	}

	public Reader getReader() {
		return reader;
	}

	public int readFiles() throws AdapterException, IOException {
		return readStdin(reader);
	}

	public void setParameters(Parameters parameters) {
		reader  = new InputReader(parameters.getSpoolSize(), System.in);
	}
}
