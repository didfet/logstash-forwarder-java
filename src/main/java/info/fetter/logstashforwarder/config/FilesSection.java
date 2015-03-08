package info.fetter.logstashforwarder.config;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang.builder.ToStringBuilder;

public class FilesSection {
	private List<String> paths;
	private Map<String,String> fields;
	/**
	 * @return the paths
	 */
	public List<String> getPaths() {
		return paths;
	}
	/**
	 * @param paths the paths to set
	 */
	public void setPaths(List<String> paths) {
		this.paths = paths;
	}
	/**
	 * @return the fields
	 */
	public Map<String, String> getFields() {
		return fields;
	}
	/**
	 * @param fields the fields to set
	 */
	public void setFields(Map<String, String> fields) {
		this.fields = fields;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
	     return new ToStringBuilder(this).
	    	       append("paths", paths).
	    	       append("fields", fields).
	    	       toString();
	}
}
