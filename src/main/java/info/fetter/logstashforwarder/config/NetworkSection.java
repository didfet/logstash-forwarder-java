package info.fetter.logstashforwarder.config;

import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;

import com.fasterxml.jackson.annotation.JsonProperty;

public class NetworkSection {
	private List<String> servers;
	@JsonProperty("ssl certificate")
	private String sslCertificate;
	@JsonProperty("ssl ca")
	private String sslCA;
	@JsonProperty("ssl key")
	private String sslKey;
	private int timeout;
	/**
	 * @return the servers
	 */
	public List<String> getServers() {
		return servers;
	}
	/**
	 * @param servers the servers to set
	 */
	public void setServers(List<String> servers) {
		this.servers = servers;
	}
	/**
	 * @return the sslCertificate
	 */
	public String getSslCertificate() {
		return sslCertificate;
	}
	/**
	 * @param sslCertificate the sslCertificate to set
	 */
	public void setSslCertificate(String sslCertificate) {
		this.sslCertificate = sslCertificate;
	}
	/**
	 * @return the sslCA
	 */
	public String getSslCA() {
		return sslCA;
	}
	/**
	 * @param sslCA the sslCA to set
	 */
	public void setSslCA(String sslCA) {
		this.sslCA = sslCA;
	}
	/**
	 * @return the timeout
	 */
	public int getTimeout() {
		return timeout;
	}
	/**
	 * @param timeout the timeout to set
	 */
	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}
	
	/**
	 * @return the sslKey
	 */
	public String getSslKey() {
		return sslKey;
	}
	/**
	 * @param sslKey the sslKey to set
	 */
	public void setSslKey(String sslKey) {
		this.sslKey = sslKey;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
	     return new ToStringBuilder(this).
	    	       append("servers", servers).
	    	       append("sslCertificate", sslCertificate).
	    	       append("sslCA", sslCA).
	    	       append("timeout", timeout).
	    	       toString();
	}
}
