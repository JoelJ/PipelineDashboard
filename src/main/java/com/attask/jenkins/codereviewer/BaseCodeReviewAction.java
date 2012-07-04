package com.attask.jenkins.codereviewer;

import hudson.model.Action;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.export.Exported;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * User: Joel Johnson
 * Date: 7/3/12
 * Time: 4:51 PM
 */
public abstract class BaseCodeReviewAction implements Action {
	public abstract void doVerify(StaplerRequest request, StaplerResponse response, @QueryParameter(required = true) Review.Status status, @QueryParameter(required = true) String message) throws IOException;
	public abstract void doReview(StaplerRequest request, StaplerResponse response, @QueryParameter(required = true) Review.Status status, @QueryParameter(required = true) String message) throws IOException;
	public abstract List<Review> getReviewList();
	public abstract List<Review> getVerifyList();
	public abstract Review.Status calculateVerifiedStatus();
	public abstract Review.Status calculateReviewStatus();

	public List<Review.Status> getStatuses() {
		return Arrays.asList(Review.Status.values());
	}

	public String getIconFileName() {
		return "/plugin/PipelineDashboard/codereview.png";
	}

	public String getDisplayName() {
		return "Code Review";
	}

	public String getUrlName() {
		return "codeReview";
	}
}
