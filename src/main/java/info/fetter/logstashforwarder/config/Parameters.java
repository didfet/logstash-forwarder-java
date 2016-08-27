package info.fetter.logstashforwarder.config;

import static org.apache.log4j.Level.INFO;

import org.apache.log4j.Level;

public class Parameters {
	private final String SINCEDB = ".logstash-forwarder-java";
	private int spoolSize = 1024;
	private int idleTimeout = 5000;
	private Level logLevel = INFO;
	private int signatureLength = 4096;
	private boolean tailSelected = false;
	private String logfile = null;
	private String logfileSize = "10MB";
	private int logfileNumber = 5;
	private String sincedbFile = SINCEDB;
	private String configFile = null;
	private boolean debugWatcherSelected = false;
	
	/**
	 * @return the spoolSize
	 */
	public int getSpoolSize() {
		return spoolSize;
	}
	/**
	 * @param spoolSize the spoolSize to set
	 */
	public void setSpoolSize(int spoolSize) {
		this.spoolSize = spoolSize;
	}
	/**
	 * @return the idleTimeout
	 */
	public int getIdleTimeout() {
		return idleTimeout;
	}
	/**
	 * @param idleTimeout the idleTimeout to set
	 */
	public void setIdleTimeout(int idleTimeout) {
		this.idleTimeout = idleTimeout;
	}
	/**
	 * @return the logLevel
	 */
	public Level getLogLevel() {
		return logLevel;
	}
	/**
	 * @param logLevel the logLevel to set
	 */
	public void setLogLevel(Level logLevel) {
		this.logLevel = logLevel;
	}
	/**
	 * @return the signatureLength
	 */
	public int getSignatureLength() {
		return signatureLength;
	}
	/**
	 * @param signatureLength the signatureLength to set
	 */
	public void setSignatureLength(int signatureLength) {
		this.signatureLength = signatureLength;
	}
	/**
	 * @return the tailSelected
	 */
	public boolean isTailSelected() {
		return tailSelected;
	}
	/**
	 * @param tailSelected the tailSelected to set
	 */
	public void setTailSelected(boolean tailSelected) {
		this.tailSelected = tailSelected;
	}
	/**
	 * @return the logfile
	 */
	public String getLogfile() {
		return logfile;
	}
	/**
	 * @param logfile the logfile to set
	 */
	public void setLogfile(String logfile) {
		this.logfile = logfile;
	}
	/**
	 * @return the logfileSize
	 */
	public String getLogfileSize() {
		return logfileSize;
	}
	/**
	 * @param logfileSize the logfileSize to set
	 */
	public void setLogfileSize(String logfileSize) {
		this.logfileSize = logfileSize;
	}
	/**
	 * @return the logfileNumber
	 */
	public int getLogfileNumber() {
		return logfileNumber;
	}
	/**
	 * @param logfileNumber the logfileNumber to set
	 */
	public void setLogfileNumber(int logfileNumber) {
		this.logfileNumber = logfileNumber;
	}
	/**
	 * @return the sincedbFile
	 */
	public String getSincedbFile() {
		return sincedbFile;
	}
	/**
	 * @param sincedbFile the sincedbFile to set
	 */
	public void setSincedbFile(String sincedbFile) {
		this.sincedbFile = sincedbFile;
	}
	/**
	 * @return the configFile
	 */
	public String getConfigFile() {
		return configFile;
	}
	/**
	 * @param configFile the configFile to set
	 */
	public void setConfigFile(String configFile) {
		this.configFile = configFile;
	}
	/**
	 * @return the debugWatcherSelected
	 */
	public boolean isDebugWatcherSelected() {
		return debugWatcherSelected;
	}
	/**
	 * @param debugWatcherSelected the debugWatcherSelected to set
	 */
	public void setDebugWatcherSelected(boolean debugWatcherSelected) {
		this.debugWatcherSelected = debugWatcherSelected;
	}
	
}
