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

public class Configuration {
	private NetworkSection network;
	private List<FilesSection> files;

	public NetworkSection getNetwork() {
		return network;
	}

	public void setNetwork(NetworkSection network) {
		this.network = network;
	}

	public List<FilesSection> getFiles() {
		return files;
	}

	public void setFiles(List<FilesSection> files) {
		this.files = files;
	}
	
	@Override
	public String toString() {
	     return new ToStringBuilder(this).
	    	       append("network", network).
	    	       append("files", files).
	    	       toString();
	}
	
}
