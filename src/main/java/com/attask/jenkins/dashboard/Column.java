package com.attask.jenkins.dashboard;

/**
 * Holds the data needed to render a column in the view.
 *
 * User: joeljohnson
 * Date: 2/9/12
 * Time: 10:29 AM
 */
public class Column {
	public static Column EMPTY = new Column();

	private final String name;
	private final int failureCount;
	private final String url;
	private final String buildStatusUrl;
	private final boolean isEmpty;
	private final boolean isPassed;

	private Column(String name, int failureCount, String url, String buildStatusUrl, boolean isEmpty) {
		this.name = name;
		this.failureCount = failureCount;
		this.url = url;
		this.buildStatusUrl = buildStatusUrl;
		this.isEmpty = isEmpty;
		this.isPassed = this.buildStatusUrl.contains("blue.png");
	}

	public Column(String name, int failureCount, String url, String buildStatusUrl) {
		this(name, failureCount, url, buildStatusUrl, false);
	}

	private Column() {
		this("", -1, "", "", true);
	}

	public String getName() {
		return name;
	}

	public int getFailureCount() {
		return failureCount;
	}

	public String getUrl() {
		return url;
	}
	
	public String getBuildStatusUrl() {
		return buildStatusUrl;
	}

	public boolean isPassed() {
		return isPassed;
	}

	@Override
	public String toString() {
		return name + " (" +url+ ")";
	}

	public boolean isEmpty() {
		return isEmpty;
	}
}
