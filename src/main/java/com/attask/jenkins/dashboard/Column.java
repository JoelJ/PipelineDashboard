package com.attask.jenkins.dashboard;

import hudson.Util;
import hudson.model.Build;
import hudson.model.Run;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

import java.util.GregorianCalendar;

/**
 * Holds the data needed to render a column in the view.
 *
 * User: joeljohnson
 * Date: 2/9/12
 * Time: 10:29 AM
 */
@ExportedBean
public class Column {
	private final String buildId;
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

	private Column(Run build, JobColumn columnHeader, String name, int failureCount, String url, String buildStatusUrl, boolean isEmpty, long timestamp) {
		this.buildId = build == null ? null : build.getExternalizableId();
		this.columnHeader = columnHeader;
		this.name = name;
		this.failureCount = failureCount;
		this.url = url;
		this.buildStatusUrl = buildStatusUrl;
		this.isEmpty = isEmpty;
		this.isPassed = this.buildStatusUrl.contains("blue.png");
		this.timestamp = timestamp;
	}

	public Column(Run build, JobColumn columnHeader, String name, int failureCount, String url, String buildStatusUrl, long timestamp) {
		this(build, columnHeader, name, failureCount, url, buildStatusUrl, false, timestamp);
	}

	private Column(JobColumn column) {
		this(null, column, "", -1, "", "", true, -1);
	}

	@Exported
	public Run getBuild() {
		if(buildId == null) {
			return null;
		}
		return Run.fromExternalizableId(buildId);
	}

	@Exported
	public JobColumn getColumnHeader() {
		return columnHeader;
	}

	@Exported
	public String getName() {
		return name;
	}

	@Exported
	public int getFailureCount() {
		return failureCount;
	}

	@Exported
	public String getUrl() {
		return url;
	}

	@Exported
	public String getBuildStatusUrl() {
		return buildStatusUrl;
	}

	@Exported
	public boolean isPassed() {
		return isPassed;
	}

	@Override
	public String toString() {
		return name + " (" +url+ ")";
	}

	@Exported
	public boolean isEmpty() {
		return isEmpty;
	}

	@Exported
	public long getTimestamp() {
		return timestamp;
	}

	public String getSinceTimestamp() {
		long duration = new GregorianCalendar().getTimeInMillis() - getTimestamp();
		return Util.getPastTimeString(duration);
	}
}
