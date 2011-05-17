package org.stanwood.media.util;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is used to render a table to text
 */
public class TextTable {

	private String[] columnNames;
	private List<String[]>rows = new ArrayList<String[]>();
	private int widths[];

	/**
	 * The constructor
	 * @param columnNames The names of the columns
	 */
	public TextTable(String columnNames[]) {
		this.columnNames = columnNames;
		this.widths = new int[columnNames.length];
		updateWidths(columnNames);
	}

	private void updateWidths(String[] row) {
		for (int column=0;column<row.length;column++) {
			if (row[column].length()>widths[column]) {
				widths[column] = row[column].length();
			}
		}
	}

	/**
	 * Used to add a row to the table. The number of values must be the same
	 * as the number of columns in the table.
	 * @param row The values in the row
	 */
	public void addRow(String row[]) {
		if (row.length > columnNames.length || row.length < columnNames.length) {
			throw new IllegalArgumentException("row must be the same size as the number of column names");
		}
		rows.add(row);
		updateWidths(row);
	}

	/**
	 * Used to print the table to a buffer
	 * @param buffer The buffer
	 */
	public void printTable(StringBuilder buffer) {
		printHeader(buffer);
		printRows(buffer);
		printFooter(buffer);
	}

	protected void printFooter(StringBuilder buffer) {
	}

	protected void printRows(StringBuilder buffer) {
		for (String row[] : rows) {
			printRow(row,buffer);
		}
	}

	protected void printRow(String[] row, StringBuilder buffer) {
		boolean first = true;
		for (int column=0;column<row.length;column++) {
			if (!first) {
				buffer.append(" | ");
			}

			appendWithPadding(row[column],column,buffer);
			first = false;
		}
		buffer.append("\n");
	}

	private int getMaxRowWidth() {
		int width = 0;
		for (int w : widths) {
			width += w;
		}
		width+=(widths.length-1) * 3;
		return width;
	}

	protected void printHeader(StringBuilder buffer) {
		printRow(columnNames,buffer);
		for (int i=0;i<getMaxRowWidth();i++) {
			buffer.append("=");
		}
		buffer.append("\n");
	}

	private void appendWithPadding(String value,int columnNumber,StringBuilder buffer) {
		StringBuilder padded = new StringBuilder(value);
		while (padded.length()<widths[columnNumber]) {
			padded.append(" ");
		}
		buffer.append(padded);
	}



}
