package com.attask.jenkins.dashboard;

import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

import java.util.Date;
import java.util.List;

/**
 * Holds the data needed to render a row of the view.
 *
 * User: joeljohnson
 * Date: 2/9/12
 * Time: 9:56 AM
 */
@ExportedBean
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

	@Exported
	public Date getDate() {
		return date;
	}

	@Exported
	public String getName() {
		return name;
	}

	@Exported
	public String getDisplayName() {
		return displayName;
	}

	@Exported
	public List<Column> getColumns() {
		return columns;
	}

	@Override
	public String toString() {
		return name;
	}

	@Exported
	public boolean isHighlighted() {
		return highlighted;
	}

	public boolean isPassed(List<JobColumn> jobColumns) {
		for (Column column : columns) {
			if(column.isEmpty()) {
				if(column.getColumnHeader().isRequired()) {
					return false;
				}
			} else if(!column.isPassed()) {
				return false;
			}
		}
		return true;
	}

	@Exported
	public boolean getHasMultiple() {
		return hasMultiple;
	}

	@Exported
	public Column getFirst() {
		for (Column column : columns) {
			if(!column.isEmpty()) {
				return column;
			}
		}
		return null;
	}
}
