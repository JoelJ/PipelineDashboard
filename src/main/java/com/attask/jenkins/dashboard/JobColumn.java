package com.attask.jenkins.dashboard;

/**
 * User: Joel Johnson
 * Date: 6/8/12
 * Time: 10:37 AM
 */
public class JobColumn {
	private final String jobName;
	private final String alias;
	private final boolean hidden;
	private final boolean required;

	public JobColumn(String jobName, String alias, boolean hidden, boolean required) {
		this.jobName = jobName;
		this.alias = alias;
		this.hidden = hidden;
		this.required = required;
	}

	/**
	 * @return The actual name of the jenkins job.
	 */
	public String getJobName() {
		return jobName;
	}

	/**
	 * @return The name to display in the column. This is useful in cases where the Job name is too long and you want it to be abbreviated.
	 */
	public String getAlias() {
		return alias;
	}

	/**
	 * @return If true this job is hidden by default.
	 */
	public boolean isHidden() {
		return hidden;
	}

	/**
	 * @return If true, this job will be used to determine if the whole row passed.
	 */
	public boolean isRequired() {
		return required;
	}
}
