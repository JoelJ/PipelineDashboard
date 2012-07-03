package com.attask.jenkins.dashboard;

import hudson.ExtensionList;
import hudson.ExtensionPoint;
import jenkins.model.Jenkins;

/**
 * Defines a custom column.
 * Every request uses it's own instance of the CustomColumn. So you can store state on a per-request basis.
 * For example, you can track pass/failure rate per row, then display statistics at the bottom of the table.
 * What is rendered is found in the cell.jelly file. Override this file to do custom drawing.
 * To set/override styles you can add a top.jelly.
 * To add content to the bottom of the page, you can add a bottom.jelly.
 *
 * User: Joel Johnson
 * Date: 7/2/12
 * Time: 3:15 PM
 */
public abstract class CustomColumn implements ExtensionPoint {
	public CustomColumn() {}

	/**
	 * What should be displayed in the column header
	 */
	public abstract String getHeaderName();

	/**
	 * Called just before the table and top.jelly is rendered
	 * @param table The instance of the table to be rendered
	 */
	public abstract void beforeTable(Table table);

	/**
	 * Called just before the row and cell.jelly is rendered
	 * @param row The row to be rendered.
	 */
	public abstract void beforeRow(Row row);

	/**
	 * Called just after the row and cell.jelly was rendered
	 * @param row The row that was rendered.
	 */
	public abstract void afterRow(Row row);

	/**
	 * Called right after the table and bottom.jelly is rendered
	 * @param table The instance of the table that was rendered
	 */
	public abstract void afterTable(Table table);

	/**
	 * All registered {@link CustomColumn}s.
	 */
	public static ExtensionList<CustomColumn> all() {
		return Jenkins.getInstance().getExtensionList(CustomColumn.class);
	}
}
