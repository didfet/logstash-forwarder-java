package info.fetter.logstashforwarder.config;

import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;

public class Configuration {
	private NetworkSection network;
	private List<FilesSection> files;
	/**
	 * @return the network
	 */
	public NetworkSection getNetwork() {
		return network;
	}
	/**
	 * @param network the network to set
	 */
	public void setNetwork(NetworkSection network) {
		this.network = network;
	}
	/**
	 * @return the files
	 */
	public List<FilesSection> getFiles() {
		return files;
	}
	/**
	 * @param files the files to set
	 */
	public void setFiles(List<FilesSection> files) {
		this.files = files;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
	     return new ToStringBuilder(this).
	    	       append("network", network).
	    	       append("files", files).
	    	       toString();
	}
	
}
