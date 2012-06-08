package com.attask.jenkins.dashboard;

import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.AbstractProject;
import hudson.model.Descriptor;
import hudson.model.Project;
import hudson.util.FormValidation;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import java.io.IOException;

/**
 * User: Joel Johnson
 * Date: 6/8/12
 * Time: 10:37 AM
 */
public class JobColumn extends AbstractDescribableImpl<JobColumn> {
	private final String jobName;
	private final String alias;
	private final boolean hidden;
	private final boolean required;

	@DataBoundConstructor
	public JobColumn(String jobName, String alias, boolean hidden, boolean required) {
		super();
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

	@Extension
	public static final class DescriptorImpl extends Descriptor<JobColumn> {
		@Override
		public String getDisplayName() {
			return "Job";
		}

		public FormValidation doCheckStackName(@QueryParameter String value) throws IOException {
			if (value == null || value.length() == 0) {
				return FormValidation.error("Empty job name");
			}

			AbstractProject nearest = Project.findNearest(value);
			if(nearest == null) {
				if(value.contains("$")) {
					return FormValidation.warning(value + " is not a valid Jenkins job. But it appears a variable is being used.");
				} else {
					return FormValidation.error(value + " is not a valid Jenkins job");
				}
			}

			return FormValidation.ok();
		}
	}
}
