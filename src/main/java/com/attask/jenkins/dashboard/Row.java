package com.attask.jenkins.dashboard;

import hudson.model.AbstractBuild;
import hudson.model.Run;
import hudson.model.User;
import hudson.scm.ChangeLogSet;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

import java.util.ArrayList;
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
	private final List<String> committers;

	public Row(Date date, String name, String displayName, List<Column> columns, boolean currentUser, boolean hasMultiple) {
		if(date == null) throw new RuntimeException("date cannot be null");
		this.date = date;
		this.name = name;
		this.displayName = displayName;
		this.columns = columns;
		this.highlighted = currentUser;
		this.hasMultiple = hasMultiple;
		this.committers = new ArrayList<String>();

		populateCommitters();
	}

	private void populateCommitters() {
		Run build = getFirst().getBuild();
		//noinspection unchecked
		if(build instanceof AbstractBuild) {
			ChangeLogSet changeSet = ((AbstractBuild) build).getChangeSet();
			if(changeSet != null) {
				for (Object changeObj : changeSet) {
					ChangeLogSet.Entry change = (ChangeLogSet.Entry)changeObj;
					User committer = change.getAuthor();
					if(committer != null) {
						this.committers.add(committer.getId());
						this.committers.add(committer.getFullName());
					}
				}
			}
		}
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

	@Exported
	public List<String> getCommitters() {
		return committers;
	}

	/**
	 * Creates a flat string of all the committers
	 * @return
	 */
	public String generateFlatCommitterList() {
		StringBuilder sb = new StringBuilder();
		for (String committer : getCommitters()) {
			sb.append(committer).append(" ");
		}
		return sb.toString().trim();
	}

	public boolean isPassed() {
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
