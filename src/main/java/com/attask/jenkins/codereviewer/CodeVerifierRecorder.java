package com.attask.jenkins.codereviewer;

import hudson.EnvVars;
import hudson.Extension;
import hudson.Launcher;
import hudson.model.*;
import hudson.tasks.*;
import hudson.tasks.test.AbstractTestResultAction;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.IOException;
import java.util.Date;

/**
 * User: Joel Johnson
 * Date: 7/3/12
 * Time: 3:20 PM
 */
public class CodeVerifierRecorder extends Recorder {
	private final String projectName;
	private final String buildNumber;
	private final int failuresBeforeNoReview;
	private final int failuresBeforeReject;

	@DataBoundConstructor
	public CodeVerifierRecorder(String projectName, String buildNumber, int failuresBeforeNoReview, int failuresBeforeReject) {
		this.projectName = projectName;
		this.buildNumber = buildNumber;
		this.failuresBeforeNoReview = failuresBeforeNoReview;
		this.failuresBeforeReject = failuresBeforeReject;
	}

	@Override
	public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
		EnvVars envVars = build.getEnvironment(listener);

		AbstractProject project = AbstractProject.findNearest(envVars.expand(projectName));
		if(project == null) {
			listener.error(projectName + " is not a valid project.");
			return false;
		}
		int buildNum;
		try {
			buildNum = Integer.parseInt(envVars.expand(buildNumber));
		} catch (NumberFormatException e) {
			listener.error("buildNumber must evaluate to an integer");
			e.printStackTrace(listener.getLogger());
			return false;
		}
		Run buildByNumber = project.getBuildByNumber(buildNum);
		if(buildByNumber == null) {
			listener.error(projectName + " #" + buildNum + " is not a valid build.");
			return false;
		}

		CodeReviewAction action = buildByNumber.getAction(CodeReviewAction.class);
		if(action == null) {
			listener.error(buildByNumber.getFullDisplayName() + " is not reviewable.");
			return false;
		}

		Review.Status status;
		String message;

		String buildDisplayName = build.getFullDisplayName();
		if(build.getResult() == Result.SUCCESS) {
			status = Review.Status.Accepted;
			message = "Build " + buildDisplayName + " succeeded.";
		} else if(build.getResult() == Result.UNSTABLE) {
			int failureCount = findFailureCount(build);
			if(failureCount < 0) {
				status = Review.Status.NotReviewed;
				message = "The build was marked " + build.getResult() + " on " + buildDisplayName + " but doesn't have a failure count. Not verifying.";
			} else if(failureCount < failuresBeforeNoReview) {
				status = Review.Status.Accepted;
				message = "There were " + failureCount + " failures on "+ buildDisplayName +". This is within an acceptable range (" + failuresBeforeNoReview + ").";
			} else if(failureCount < failuresBeforeReject) {
				status = Review.Status.NotReviewed;
				message = "There were " + failureCount + " failures on "+ buildDisplayName +". This is within a range of concern (" + failuresBeforeNoReview + " - " + failuresBeforeReject + "), but isn't horrible. ";
			} else {
				status = Review.Status.Rejected;
				message = "There were " + failureCount + " failures on " + buildDisplayName + ". This has exceeded the number of acceptable failures (" + failuresBeforeReject + ").";
			}
		} else {
			status = Review.Status.Rejected;
			message = "The result was " + build.getResult() + " for " + buildDisplayName;
		}

		action.addVerification(status, message, new Date(), null);

		return true;
	}

	private int findFailureCount(AbstractBuild build) {
		AbstractTestResultAction testResultAction = build.getAction(AbstractTestResultAction.class);
		if(testResultAction != null) {
			return testResultAction.getFailCount();
		}
		return -1;
	}

	public String getProjectName() {
		return projectName;
	}

	public String getBuildNumber() {
		return buildNumber;
	}

	public int getFailuresBeforeNoReview() {
		return failuresBeforeNoReview;
	}

	public int getFailuresBeforeReject() {
		return failuresBeforeReject;
	}

	public BuildStepMonitor getRequiredMonitorService() {
		return BuildStepMonitor.NONE;
	}

	@Extension
	public static class DescriptorImpl extends BuildStepDescriptor<Publisher> {
		@Override
		public boolean isApplicable(Class<? extends AbstractProject> jobType) {
			return true;
		}

		@Override
		public String getDisplayName() {
			return "Verify a code review";
		}
	}
}
