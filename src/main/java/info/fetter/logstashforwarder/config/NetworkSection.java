package info.fetter.logstashforwarder.config;

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

	public List<String> getServers() {
		return servers;
	}

	public void setServers(List<String> servers) {
		this.servers = servers;
	}

	public String getSslCertificate() {
		return sslCertificate;
	}

	public void setSslCertificate(String sslCertificate) {
		this.sslCertificate = sslCertificate;
	}

	public String getSslCA() {
		return sslCA;
	}

	public void setSslCA(String sslCA) {
		this.sslCA = sslCA;
	}

	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}
	
	public String getSslKey() {
		return sslKey;
	}

	public void setSslKey(String sslKey) {
		this.sslKey = sslKey;
	}

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
