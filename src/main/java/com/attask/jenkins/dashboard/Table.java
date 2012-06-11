package com.attask.jenkins.dashboard;

import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

import java.util.Collections;
import java.util.List;

/**
 * User: joeljohnson
 * Date: 2/27/12
 * Time: 9:22 AM
 */
@ExportedBean
public class Table {
	public static Table EMPTY_TABLE = new Table(Collections.<Row>emptyList(), null);

	private final Row lastSuccessfulRow;
	private final List<Row> rows;

	public Table(List<Row> rows, Row lastSuccessfulRow) {
		this.rows = Collections.unmodifiableList(rows == null ? Collections.<Row>emptyList() : rows);
		this.lastSuccessfulRow = lastSuccessfulRow;
	}

	/**
	 * @return the last row found that had all successful builds. Can be null.
	 */
	@Exported
	public Row getLastSuccessfulRow() {
		return lastSuccessfulRow;
	}

	/**
	 * @return All the rows in the table. Not null.
	 */
	@Exported
	public List<Row> getRows() {
		return rows;
	}
}
