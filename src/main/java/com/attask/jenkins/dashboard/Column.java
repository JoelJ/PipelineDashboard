package com.attask.jenkins.dashboard;

/**
 * Holds the data needed to render a column in the view.
 *
 * User: joeljohnson
 * Date: 2/9/12
 * Time: 10:29 AM
 */
public class Column {
	public static Column EMPTY = new Column("", "", "");

	private final String name;
	private final String url;
	private final String buildStatusUrl;

	public Column(String name, String url, String buildStatusUrl) {
		this.name = name;
		this.url = url;
		this.buildStatusUrl = buildStatusUrl;
	}

	public String getName() {
		return name;
	}

	public String getUrl() {
		return url;
	}
	
	public String getBuildStatusUrl() {
		return buildStatusUrl;
	}

	@Override
	public String toString() {
		return name + " (" +url+ ")";
	}
}
