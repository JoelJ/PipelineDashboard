package com.attask.jenkins.codereviewer;

import hudson.EnvVars;
import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.tasks.BuildWrapper;
import hudson.tasks.BuildWrapperDescriptor;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.IOException;

/**
 * User: Joel Johnson
 * Date: 7/3/12
 * Time: 3:20 PM
 */
public class CodeReviewBuildWrapper extends BuildWrapper {
	private final String requiredReviews;
	private final String requiredVerifies;
    private final String[] checkListItems;

	@DataBoundConstructor
	public CodeReviewBuildWrapper(String requiredReviews, String requiredVerifies,String checklistItems) {
		this.requiredReviews = requiredReviews;
		this.requiredVerifies = requiredVerifies;
        this.checkListItems=makeCheckListItems(checklistItems);
	}

    private String[] makeCheckListItems(String checkListItems) {
        if(!checkListItems.isEmpty())
            return checkListItems.split("\n");
        return null;
    }

    @Override
	public Environment setUp(AbstractBuild build, Launcher launcher, BuildListener listener) throws IOException, InterruptedException {
		EnvVars envVars = build.getEnvironment(listener);
		String requiredReviewsExpanded = envVars.expand(this.requiredReviews);
		String requiredVerifiesExpanded = envVars.expand(this.requiredVerifies);

		int requiredReviews = parseInt(requiredReviewsExpanded, listener);
		int requiredVerifies = parseInt(requiredVerifiesExpanded, listener);

		build.addAction(new CodeReviewAction(build, requiredReviews, requiredVerifies,checkListItems));
		return new Environment() {
			@Override
			public boolean tearDown(AbstractBuild build, BuildListener listener) throws IOException, InterruptedException {
				return true;
			}
		};
	}

	private int parseInt(String string, BuildListener listener) {
		int requiredVerifies;
		try {
			requiredVerifies = Integer.parseInt(string);
		} catch(NumberFormatException e) {
			listener.error(string + " was not a valid integer. Defaulting to 1.");
			requiredVerifies = 1;
		}
		return requiredVerifies;
	}

	public String getRequiredReviews() {
		return requiredReviews;
	}

	public String getRequiredVerifies() {
		return requiredVerifies;
	}

    public String[] getCheckListItems() {
        return checkListItems;
    }

    @Extension
	public static class DescriptorImpl extends BuildWrapperDescriptor {
		@Override
		public boolean isApplicable(AbstractProject<?, ?> item) {
			return true;
		}

		@Override
		public String getDisplayName() {
			return "Code Review";
		}
	}
}
