package com.attask.jenkins.dashboard;

import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.AbstractProject;
import hudson.model.Descriptor;
import hudson.model.Project;
import hudson.util.FormValidation;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

import java.io.IOException;
import java.util.*;

/**
 * User: Joel Johnson
 * Date: 6/8/12
 * Time: 10:37 AM
 */
@ExportedBean
public class JobColumn extends AbstractDescribableImpl<JobColumn> {
	private String jobName;
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
	@Exported
	public String getJobName() {
		return jobName;
	}

	/**
	 * @return The name to display in the column. This is useful in cases where the Job name is too long and you want it to be abbreviated.
	 */
	@Exported
	public String getAlias() {
		if(alias != null && !alias.isEmpty()) {
			return alias;
		}
		return jobName;
	}

	/**
	 * @return If true this job is hidden by default.
	 */
	@Exported
	public boolean isHidden() {
		return hidden;
	}

	/**
	 * @return If true, this job will be used to determine if the whole row passed.
	 */
	@Exported
	public boolean isRequired() {
		return required;
	}

	public static List<JobColumn> parseFromRequest(Map<String, String[]> parameterMap) {
		String[] jobNames = parameterMap.get("_.jobName");
		String[] aliases = parameterMap.get("_.alias");
		String[] hidden = parameterMap.get("hidden");
		String[] required = parameterMap.get("required");

		assert jobNames.length == aliases.length;
		assert jobNames.length == hidden.length;
		assert jobNames.length == required.length;

		List<Map<String, Object>> builder = new ArrayList<Map<String, Object>>();
		for(int i = 0; i < jobNames.length; i++) {
			builder.add(new HashMap<String, Object>());
			builder.get(i).put("jobName", jobNames[i]);
			builder.get(i).put("alias", aliases[i]);
			builder.get(i).put("hidden", Boolean.parseBoolean(hidden[i]));
			builder.get(i).put("required", Boolean.parseBoolean(required[i]));
		}

		List<JobColumn> jobColumns = new ArrayList<JobColumn>();
		for (Map<String, Object> map : builder) {
			jobColumns.add(new JobColumn((String)map.get("jobName"), (String)map.get("alias"), (Boolean)map.get("hidden"), (Boolean)map.get("required")));
		}
		return jobColumns;
	}

	public void setJobName(String newName) {
		this.jobName = newName;
	}

	public String toString() {
		return getJobName() + "(" + getAlias() + ")";
	}

	public static JobColumn findColumnHeader(String projectName, Collection<JobColumn> jobColumns) {
		JobColumn columnHeader = null;
		for (JobColumn jobColumn : jobColumns) {
			if(projectName.equals(jobColumn.getJobName())) {
				columnHeader = jobColumn;
				break;
			}
		}
		assert columnHeader != null : "column header cannot be null;";
		return columnHeader;
	}

	@Extension
	public static final class DescriptorImpl extends Descriptor<JobColumn> {
		@Override
		public String getDisplayName() {
			return "Job";
		}
	}
}
