/*
 * Copyright (C) 2005-2020 Gregory Hedlund & Yvan Rose
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at

 *    http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ca.phon.query.report.csv;

import org.apache.commons.lang3.*;

import au.com.bytecode.opencsv.*;
import ca.phon.query.report.io.*;

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
