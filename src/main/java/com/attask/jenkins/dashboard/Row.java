package com.attask.jenkins.dashboard;

import java.util.Date;
import java.util.List;

/**
 * Holds the data needed to render a row of the view.
 *
 * User: joeljohnson
 * Date: 2/9/12
 * Time: 9:56 AM
 */
public class Row {
	private final Date date;
	private final String name;
	private final String displayName;
	private final List<Column> columns;
	private final boolean highlighted;
	private final boolean hasMultiple;

	public Row(Date date, String name, String displayName, List<Column> columns, boolean currentUser, boolean hasMultiple) {
		if(date == null) throw new RuntimeException("date cannot be null");
		this.date = date;
		this.name = name;
		this.displayName = displayName;
		this.columns = columns;
		this.highlighted = currentUser;
		this.hasMultiple = hasMultiple;
	}
	
	public Date getDate() {
		return date;
	}

	public String getName() {
		return name;
	}
	
	public String getDisplayName() {
		return displayName;
	}

	public List<Column> getColumns() {
		return columns;
	}

	@Override
	public String toString() {
		return name;
	}

	public boolean isHighlighted() {
		return highlighted;
	}

	public boolean isPassed() {
		for (Column column : columns) {
			if(!column.isEmpty() && !column.isPassed()) {
				return false;
			}
		}
		return true;
	}

	public boolean getHasMultiple() {
		return hasMultiple;
	}
}
