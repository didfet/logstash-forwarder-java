package info.fetter.logstashforwarder;

import java.io.IOException;

import info.fetter.logstashforwarder.config.FilesSection;
import info.fetter.logstashforwarder.config.Parameters;
import info.fetter.logstashforwarder.util.AdapterException;

public interface Watcher {
	public void addFilesToWatch(FilesSection files);
	public Reader getReader();
	public int readFiles() throws AdapterException, IOException;
	public void setParameters(Parameters parameters);
}
