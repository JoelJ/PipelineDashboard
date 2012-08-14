package com.attask.jenkins.codereviewer;

import hudson.model.AbstractBuild;
import hudson.model.Cause;
import hudson.model.Run;
import hudson.model.User;
import hudson.scm.ChangeLogSet;
import hudson.tasks.Mailer;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * User: Joel Johnson
 * Date: 7/3/12
 * Time: 2:14 PM
 */
@ExportedBean
public class CodeReviewAction extends BaseCodeReviewAction {
	private int requiredReviews;
	private int requiredVerifies;
    private String[] checklistItems;

    public static Logger LOGGER = Logger.getLogger(CodeReviewAction.class.getSimpleName());
	private final String buildId;
	private final List<Review> reviewList;
	private final List<Review> verifyList;
    private final Set<User> userList;

	public CodeReviewAction(Run build, int requiredReviews, int requiredVerifies,String[] checkListItems) {
		this.buildId = build.getExternalizableId();
		this.requiredReviews = requiredReviews;
		this.requiredVerifies = requiredVerifies;
        this.checklistItems =checkListItems;
		this.reviewList = new ArrayList<Review>();
		this.verifyList = new ArrayList<Review>();
        this.userList=new HashSet<User>();
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
        if (message != null && !message.isEmpty()) {
            addReview(status, message, new Date(), User.current());
        }
        response.sendRedirect("..");
	}

    @Override
    public void doDeleteReview(StaplerRequest request, StaplerResponse response, @QueryParameter(required = true) String id) throws IOException {
        deleteR(id);

        response.sendRedirect("..");
    }

    @Override
    public void doDeleteVerification(StaplerRequest request, StaplerResponse response, @QueryParameter(required = true) String id) throws IOException {
        deleteV(id);

        response.sendRedirect("..");
    }

    public void deleteR(String id){
        delete(reviewList, id);
    }

    public void deleteV(String id){
        delete(verifyList,id);
    }

    public void delete(List<Review> reviews, String id){
        for(Review review: reviews){
            if(review.getId().equals(id)){
                reviews.remove(review);
                return;
            }
        }
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
	public void addReview(Review.Status status, String message, Date date, User author) throws IOException{
		Review reviewToEmail=addReview(reviewList, date, status, message, author);
        sendEmail(reviewToEmail);
    }

    private void sendEmail(Review review){

        AbstractBuild build = (AbstractBuild) findBuild();
        Cause.UserCause cause = (Cause.UserCause) build.getCause(Cause.UserCause.class);
        if(cause!=null){
            User user = User.get(cause.getUserName());
            try {
                sendMailToUser(build, user,review);
            } catch (MessagingException e) {
                LOGGER.log(Level.SEVERE,"Sending Email to triggerer BROKE!!");
                e.printStackTrace();
            }
        }
        ChangeLogSet changeSet = build.getChangeSet();
        for (Object o  : changeSet) {
            ChangeLogSet.Entry change = (ChangeLogSet.Entry) o;
            User user = change.getAuthor();
            try {
                sendMailToUser(build, user, review);
            } catch (MessagingException e) {
                LOGGER.log(Level.SEVERE,"Sending emails BROKE!");
                e.printStackTrace();
            }

        }
    }

    private void sendMailToUser(AbstractBuild build, User user, Review review) throws MessagingException {
        if(!userList.contains(user)){
            userList.add(user);
            String email = user.getProperty(Mailer.UserProperty.class).getAddress();
            LOGGER.log(Level.FINE,"Sending email!");
            MimeMessage msg=new MimeMessage(Mailer.descriptor().createSession());
            msg.setFrom(new InternetAddress(Mailer.descriptor().getAdminAddress()));
            String datUrl = Jenkins.getInstance().getRootUrl() + build.getUrl();
            setMailMessage(build, review, msg, datUrl);
            msg.setSentDate(new Date());
            msg.setRecipient(Message.RecipientType.TO, new InternetAddress(email));
            msg.setSubject("Build: " + build.getId());

            Transport.send(msg);

        }
    }

    private void setMailMessage(AbstractBuild build, Review review, MimeMessage msg, String url) throws MessagingException {
        StringBuilder messageBuilder=new StringBuilder();
        messageBuilder.append("<style>.review {border: 1px solid black;padding-left: 5px;padding-right: 5px;padding-top: 5px;padding-bottom: 5px;margin-top: 5px;margin-bottom: 5px;max-width: 500px;}.review .message {\n" +
                "    display: block;\n" +
                "}\n" +
                "\n" +
                ".review .author {\n" +
                "    display: block;\n" +
                "    font-style: italic;\n" +
                "}\n" +
                "\n" +
                ".review .date {\n" +
                "    font-size: smaller;\n" +
                "    margin-left: 5px;\n" +
                "}\n" +
                "\n" +
                ".review .author::before {\n" +
                "    content: \"~\";\n" +
                "}\n" +
                "\n" +
                ".review.Accepted {\n" +
                "    border-color: black;\n" +
                "}\n" +
                ".review.Accepted .status {\n" +
                "    color: green;\n" +
                "    font-weight: bold;\n" +
                "}\n" +
                "\n" +
                ".review.Rejected {\n" +
                "    border-color: red;\n" +
                "}\n" +
                "\n" +
                ".review.Rejected .status {\n" +
                "    color: red;\n" +
                "    font-weight: bold;\n" +
                "}\n" +
                "\n" +
                ".review.NotReviewed {\n" +
                "    border-color: lightgray;\n" +
                "    color: gray;\n" +
                "}\n" +
                "\n" +
                ".review.NotReviewed .status {\n" +
                "    font-weight: bold;\n" +
                "}</style>");

        messageBuilder.append("<br/>");
        messageBuilder.append("Build ");
        messageBuilder.append(build.getFullDisplayName());
        messageBuilder.append(" just got reviewed!");
        messageBuilder.append("<div class=\"review ");
        messageBuilder.append(review.getStatus());
        messageBuilder.append("\"><span class=\"status\">");
        messageBuilder.append(review.getStatus());
        messageBuilder.append("</span> <span class=\"date\">");
        messageBuilder.append(review.getDate()).append("</span><span class=\"message\"><pre>");
        messageBuilder.append(review.getMessage());
        messageBuilder.append("</pre></span><span class=\"author\">");
        messageBuilder.append(review.getAuthor()).append("</span></div>");

        msg.setContent(messageBuilder.toString()+"<br/>Click here to see the review!<br/><a href=" + url + "\">" + build.getFullDisplayName() + "</a>", "text/html");
    }

    private Review addReview(List<Review> toAddTo, Date date, Review.Status status, String message, User author) throws IOException {
		if(date == null) {
			date = new Date();
		}
		if(author == null) {
			author = User.current();
		}
		Review review = new Review(date, status, message, author);
		toAddTo.add(0, review);
		findBuild().save();
        return review;
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

    public String[] getChecklistItems() {
        return checklistItems;
    }
}
