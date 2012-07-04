package com.attask.jenkins.codereviewer;

import com.attask.jenkins.dashboard.CustomColumn;
import com.attask.jenkins.dashboard.Row;
import com.attask.jenkins.dashboard.Table;

/**
 * User: Joel Johnson
 * Date: 7/3/12
 * Time: 2:13 PM
 */
public class ReviewColumn extends CustomColumn {
	@Override
	public String getHeaderName() {
		return "Review";
	}

	@Override
	public void beforeTable(Table table) {
	}

	@Override
	public void beforeRow(Row row) {
	}

	@Override
	public void afterRow(Row row) {
	}

	@Override
	public void afterTable(Table table) {
	}
}
