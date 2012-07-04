package com.attask.jenkins.codereviewer;

import hudson.model.Run;
import hudson.model.User;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * User: Joel Johnson
 * Date: 7/3/12
 * Time: 2:14 PM
 */
@ExportedBean
public class CodeReviewAction extends BaseCodeReviewAction {
	private int requiredReviews;
	private int requiredVerifies;

	private final String buildId;
	private final List<Review> reviewList;
	private final List<Review> verifyList;

	public CodeReviewAction(Run build, int requiredReviews, int requiredVerifies) {
		this.buildId = build.getExternalizableId();
		this.requiredReviews = requiredReviews;
		this.requiredVerifies = requiredVerifies;
		this.reviewList = new ArrayList<Review>();
		this.verifyList = new ArrayList<Review>();
	}

	@Override
	public void doVerify(StaplerRequest request, StaplerResponse response,
						 @QueryParameter(required = true) Review.Status status, @QueryParameter(required = true) String message) throws IOException {
		addVerification(status, message, new Date(), User.getUnknown());

		response.sendRedirect("..");
	}

	@Override
	public void doReview(StaplerRequest request, StaplerResponse response,
						@QueryParameter(required = true) Review.Status status, @QueryParameter(required = true) String message) throws IOException {
		addReview(status, message, new Date(), User.current());

		response.sendRedirect("..");
	}

	/**
	 * Adds a new verification to the code review.
	 * Verifications are meant to be used by other jobs to report the status.
	 */
	public void addVerification(Review.Status status, String message, Date date, User author) throws IOException {
		addReview(verifyList, date, status, message, author);
	}

	/**
	 * Adds a new review to the code review.
	 * Reviews are meant to be done by human users.
	 */
	public void addReview(Review.Status status, String message, Date date, User author) throws IOException {
		addReview(reviewList, date, status, message, author);
	}

	private void addReview(List<Review> toAddTo, Date date, Review.Status status, String message, User author) throws IOException {
		if(date == null) {
			date = new Date();
		}
		if(author == null) {
			author = User.current();
		}
		Review review = new Review(date, status, message, author);
		toAddTo.add(review);
		findBuild().save();
	}

	public Run findBuild() {
		return Run.fromExternalizableId(getBuildId());
	}

	/**
	 * Calculates whether or not the build should or can be accepted based on the verifications.
	 * @return The result is calculated based on the number of positive reviews and the number of reviews required.
	 * If any reviews have a rejected status, the entire build is rejected.
	 * Neutral statuses are ignored.
	 */
	public Review.Status calculateVerifiedStatus() {
		return calculateStatus(verifyList, requiredVerifies);
	}

	/**
	 * Calculates whether or not the build should or can be accepted based on the user reviews.
	 * @return The result is calculated based on the number of positive reviews and the number of reviews required.
	 * If any reviews have a rejected status, the entire build is rejected.
	 * Neutral statuses are ignored.
	 */
	public Review.Status calculateReviewStatus() {
		return calculateStatus(reviewList, requiredReviews);
	}

	private Review.Status calculateStatus(List<Review> reviewList, int requiredPositiveReviews) {
		int totalPositive = 0;
		for (Review review : reviewList) {
			switch (review.getStatus()) {
				case Accepted:
					totalPositive++;
					break;
				case Rejected:
					return Review.Status.Rejected;
				case NotReviewed:
					break;
			}
		}

		return totalPositive >= requiredPositiveReviews ? Review.Status.Accepted : Review.Status.NotReviewed;
	}

	@Exported
	public int getRequiredReviews() {
		return requiredReviews;
	}

	@Exported
	public int getRequiredVerifies() {
		return requiredVerifies;
	}

	@Override
	@Exported(visibility = 2)
	public List<Review> getReviewList() {
		return reviewList;
	}

	@Override
	@Exported(visibility = 2)
	public List<Review> getVerifyList() {
		return verifyList;
	}

	public String getBuildId() {
		return buildId;
	}
}
