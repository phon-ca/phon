/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2016, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
 * Dept of Linguistics, Memorial University <https://phon.ca>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ca.phon.query.report.datasource;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;

import au.com.bytecode.opencsv.CSVWriter;
import ca.phon.formatter.Formatter;
import ca.phon.formatter.FormatterFactory;
import ca.phon.formatter.FormatterType;
import ca.phon.ipa.IPATranscript;

@FormatterType(value=TableDataSource.class)
public class TableDataSourceFormatter implements Formatter<TableDataSource> {

	// used only when parsing
	private boolean useFirstColumnAsHeader = true;

	public TableDataSourceFormatter() {
		super();
	}

	public TableDataSourceFormatter(boolean useFirstColumnAsHeader) {
		super();

		this.useFirstColumnAsHeader = useFirstColumnAsHeader;
	}

	@Override
	public String format(TableDataSource table) {
		final ByteArrayOutputStream bout = new ByteArrayOutputStream();
		final PrintWriter writer = new PrintWriter(bout);

		try (final CSVWriter csvWriter = new CSVWriter(writer)) {
			String[] row = new String[table.getColumnCount()];
			for(int i = 0; i < table.getColumnCount(); i++)
				row[i] = table.getColumnTitle(i);
			csvWriter.writeNext(row);

			for(int rowIdx = 0; rowIdx < table.getRowCount(); rowIdx++) {
				for(int i = 0; i < table.getColumnCount(); i++) {
					Object val = table.getValueAt(rowIdx, i);

					if(val == null) {
						row[i] = "";
					} else {
						// insert 'null' if we have an empty IPA transcript
						if(val instanceof IPATranscript && ((IPATranscript)val).length() == 0) {
							row[i] = "\u00d8";
						} else {
							@SuppressWarnings("unchecked")
							final Formatter<Object> formatter =
									(Formatter<Object>)FormatterFactory.createFormatter(val.getClass());
							row[i] = (formatter != null ? formatter.format(val) : val.toString());
						}
					}
				}
				csvWriter.writeNext(row);
			}
		} catch (IOException e) {

		}

		String retVal = new String();
		try {
			retVal = bout.toString("UTF-8");
		} catch (UnsupportedEncodingException e) {}
		return retVal;
	}

	@Override
	public TableDataSource parse(String text) throws ParseException {
		throw new ParseException(text, -1);
	}

}
