package com.attask.jenkins.dashboard;

import hudson.Util;

import java.util.GregorianCalendar;

/**
 * Holds the data needed to render a column in the view.
 *
 * User: joeljohnson
 * Date: 2/9/12
 * Time: 10:29 AM
 */
public class Column {
	private final JobColumn columnHeader;
	private final String name;
	private final int failureCount;
	private final String url;
	private final String buildStatusUrl;
	private final boolean isEmpty;
	private final boolean isPassed;
	private final long timestamp;

	public static Column getEmpty(JobColumn jobHeader) {
		return new Column(jobHeader);
	}

	private Column(JobColumn columnHeader, String name, int failureCount, String url, String buildStatusUrl, boolean isEmpty, long timestamp) {
		this.columnHeader = columnHeader;
		this.name = name;
		this.failureCount = failureCount;
		this.url = url;
		this.buildStatusUrl = buildStatusUrl;
		this.isEmpty = isEmpty;
		this.isPassed = this.buildStatusUrl.contains("blue.png");
		this.timestamp = timestamp;
	}

	public Column(JobColumn columnHeader, String name, int failureCount, String url, String buildStatusUrl, long timestamp) {
		this(columnHeader, name, failureCount, url, buildStatusUrl, false, timestamp);
	}

	private Column(JobColumn column) {
		this(column, "", -1, "", "", true, -1);
	}

	public JobColumn getColumnHeader() {
		return columnHeader;
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

	public long getTimestamp() {
		return timestamp;
	}

	public String getSinceTimestamp() {
		long duration = new GregorianCalendar().getTimeInMillis() - getTimestamp();
		return Util.getPastTimeString(duration);
	}
}
