package com.attask.jenkins.codereviewer;

import com.attask.jenkins.dashboard.Column;
import com.attask.jenkins.dashboard.CustomColumn;
import com.attask.jenkins.dashboard.Row;
import com.attask.jenkins.dashboard.Table;
import hudson.Extension;
import hudson.model.Run;
import hudson.model.User;

/**
 * User: Joel Johnson
 * Date: 7/3/12
 * Time: 2:13 PM
 */
@Extension
public class ReviewColumn extends CustomColumn {
	private Run currentBuild;

    public Run getCurrentBuild() {
        return currentBuild;
    }

    @Override
	public String getHeaderName() {
		return "Review";
	}

	@Override
	public void beforeTable(Table table) {
	}

	@Override
	public void beforeRow(Row row) {
		Column column = row.getColumns().get(0);
		Run build = column.getBuild();
		if(build != null) {
			currentBuild = build;
		} else {
			currentBuild = null;
		}
	}

	/**
	 * Called by cell.jelly
	 */
	public BaseCodeReviewAction getReviewAction() {
		if(currentBuild != null) {
			BaseCodeReviewAction action = currentBuild.getAction(BaseCodeReviewAction.class);
			if(action != null) {
				return action;
			}
		}
		return null;
	}

	public String getReviewTitle() {
		String result = "";
		BaseCodeReviewAction reviewAction = getReviewAction();
		if(reviewAction != null) {
			for (Review review : reviewAction.getReviewList()) {
				Review.Status status = review.getStatus();
				User author = review.getAuthor();
				String fullName;
				if(author == null) {
					fullName = "Some Fool";
				} else {
					fullName = author.getFullName();
					if("SYSTEM".equals(fullName)) {
						fullName = "Jenkins";
					}
				}
				String message = "Reviewed and " + status + " by " + fullName + " - \""+ review.getMessage() + "\"";
				if(status == Review.Status.Rejected) {
					return message;
				} else if(status == Review.Status.Accepted || result.isEmpty()) {
					result = message;
				}
			}
		}
		return result;
	}

	public String getVerifyTitle() {
		String result = "";
		BaseCodeReviewAction reviewAction = getReviewAction();
		if(reviewAction != null) {
			for (Review review : reviewAction.getVerifyList()) {
				Review.Status status = review.getStatus();
				String message = "Verified as " + status + " - \""+ review.getMessage() + "\"";
				if(status == Review.Status.Rejected) {
					return message;
				} else if(status == Review.Status.Accepted || result.isEmpty()) {
					result = message;
				}
			}
		}
		return result;
	}

	@Override
	public void afterRow(Row row) {
	}

	@Override
	public void afterTable(Table table) {
	}
}
