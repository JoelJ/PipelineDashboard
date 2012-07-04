package com.attask.jenkins.codereviewer;

import hudson.model.AbstractProject;
import hudson.model.Run;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * User: Joel Johnson
 * Date: 7/3/12
 * Time: 4:56 PM
 */
public class MirroredCodeReviewAction extends BaseCodeReviewAction {
	private final String buildId;
	private final String projectToMirror;
	private final String buildDescriptionToMirror;

	public MirroredCodeReviewAction(Run build, String projectToMirror, String buildDescriptionToMirror) {
		this.buildId = build.getExternalizableId();
		this.projectToMirror = projectToMirror;
		this.buildDescriptionToMirror = buildDescriptionToMirror;
	}

	@Override
	public void doVerify(StaplerRequest request, StaplerResponse response, @QueryParameter(required = true) Review.Status status, @QueryParameter(required = true) String message) throws IOException {
		CodeReviewAction action = findMirroredAction();
		if(action == null) {
			throw new IOException(projectToMirror + " doesn't have an reviewable build that matches " + buildDescriptionToMirror);
		}
		action.doVerify(request, response, status, message);
	}

	@Override
	public void doReview(StaplerRequest request, StaplerResponse response, @QueryParameter(required = true) Review.Status status, @QueryParameter(required = true) String message) throws IOException {
		CodeReviewAction action = findMirroredAction();
		if(action == null) {
			throw new IOException(projectToMirror + " doesn't have an reviewable build that matches " + buildDescriptionToMirror);
		}
		action.doReview(request, response, status, message);
	}

	public Run findBuild() {
		return Run.fromExternalizableId(getBuildId());
	}

	public Run findBuildToMirror() {
		AbstractProject project = AbstractProject.findNearest(getProjectToMirror());
		if(project != null) {
			Pattern pattern = Pattern.compile(getBuildDescriptionToMirror());
			//noinspection unchecked
			for (Run run : (List<Run>)project.getBuilds()) {
				//don't compare against the build this action belongs to.
				if(!run.getExternalizableId().equals(getBuildId())) {
					Matcher matcher = pattern.matcher(run.getDescription());
					if(matcher.find()) {
						return run;
					}
				}
			}
		}
		return null;
	}

	private CodeReviewAction findMirroredAction() {
		Run buildToMirror = findBuildToMirror();
		if(buildToMirror == null) {
			return null;
		}
		CodeReviewAction action = buildToMirror.getAction(CodeReviewAction.class);
		if(action == null) {
			return null;
		}
		return action;
	}

	public String getBuildId() {
		return buildId;
	}

	public String getProjectToMirror() {
		return projectToMirror;
	}

	public String getBuildDescriptionToMirror() {
		return buildDescriptionToMirror;
	}

	@Override
	public List<Review> getReviewList() {
		CodeReviewAction mirroredAction = findMirroredAction();
		if(mirroredAction == null) {
			return Collections.emptyList();
		}
		return mirroredAction.getReviewList();
	}

	@Override
	public List<Review> getVerifyList() {
		CodeReviewAction mirroredAction = findMirroredAction();
		if(mirroredAction == null) {
			return Collections.emptyList();
		}
		return mirroredAction.getVerifyList();
	}

	/**
	 * Calculates whether or not the build should or can be accepted based on the verifications.
	 * @return The result is calculated based on the number of positive reviews and the number of reviews required.
	 * If any reviews have a rejected status, the entire build is rejected.
	 * Neutral statuses are ignored.
	 */
	public Review.Status calculateVerifiedStatus() {
		CodeReviewAction mirroredAction = findMirroredAction();
		if(mirroredAction == null) {
			return Review.Status.NotReviewed;
		}
		return mirroredAction.calculateVerifiedStatus();
	}

	/**
	 * Calculates whether or not the build should or can be accepted based on the user reviews.
	 * @return The result is calculated based on the number of positive reviews and the number of reviews required.
	 * If any reviews have a rejected status, the entire build is rejected.
	 * Neutral statuses are ignored.
	 */
	public Review.Status calculateReviewStatus() {
		CodeReviewAction mirroredAction = findMirroredAction();
		if(mirroredAction == null) {
			return Review.Status.NotReviewed;
		}
		return mirroredAction.calculateReviewStatus();
	}
}
