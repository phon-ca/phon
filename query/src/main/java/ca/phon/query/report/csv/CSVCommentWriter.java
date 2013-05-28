/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2008 The Phon Project, Memorial University <http://phon.ling.mun.ca>
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
package ca.phon.query.report.csv;

import au.com.bytecode.opencsv.CSVWriter;
import ca.phon.engines.search.report.design.CommentSection;
import ca.phon.util.StringUtils;

/**
 * Write out comments to the csv report.
 * Lines which start with '||' are considiered
 * to be divided into cells. Cells are divided again
 * by the '|' char.
 *
 */
public class CSVCommentWriter implements CSVSectionWriter {
	
	/**
	 * Section data
	 */
	private CommentSection invData;
	
	public CSVCommentWriter(CommentSection info) {
		this.invData = info;
	}
	
	@Override
	public void writeSection(CSVWriter writer, int indentLevel) {
		final String data = (invData.getValue() == null ? "" : invData.getValue());
		// break comments into lines
		// use tab in lines as a cell divider
		final String[] lines = data.split("(\r)?\n");
		
		for(String line:lines) {
			line = StringUtils.strip(line);
			String[] outputLine = { line };
			if(line.startsWith("||")) {
				line = line.substring(2);
				
				outputLine = line.split("\\|");
			}
			
			if(indentLevel > 0) {
				String[] indentedOutput = new String[outputLine.length+indentLevel];
				for(int i = 0; i < indentLevel; i++) indentedOutput[i] = "";
				for(int i = indentLevel; i < indentedOutput.length; i++) {
					indentedOutput[i] = outputLine[i-indentLevel];
				}
				writer.writeNext(indentedOutput);
			} else {
				writer.writeNext(outputLine);
			}
		}
	}

}
