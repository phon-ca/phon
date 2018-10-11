/*
 * Copyright (C) 2012-2018 Gregory Hedlund & Yvan Rose
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
package ca.phon.app.opgraph.nodes.table;

import java.util.HashMap;
import java.util.Map;

import ca.phon.ipa.IPAElement;
import ca.phon.ipa.IPATranscript;
import ca.phon.ipa.alignment.PhoneMap;
import ca.phon.ipa.tree.IpaTernaryTree;
import ca.phon.opgraph.OpContext;
import ca.phon.opgraph.OpNodeInfo;
import ca.phon.opgraph.exceptions.ProcessingException;
import ca.phon.query.report.datasource.DefaultTableDataSource;

/**
 * Accurate, Substitutions, Deletions Node (ASDNode)
 * 
 * Create a table with the aforementioned columns calculated
 * from the IPA Target, IPA Actual, and Alignment columns of the input table.
 * A position column is also included the value of which will be a
 * combination of the constituent type along with the index position
 * of the phone within the search result. Epenthesized phones are given a position
 * starting with '+' where the index is the position of the last target phone position.
 * 
 * For example:
 * 
 * Given input rows (all onset clusters):
 * 
 * | IPA Target | IPA Actual | Alignment        |
 * |------------|------------|------------------|
 * | br         | r          | b->0, r->r       |
 * | br         | kr         | b->k, r->r       |
 * | br         | slr        | 0->s, b->l, r->r |
 * | br         | blr        | b->b, 0->l, r->r |
 * | br         | brt        | b->b, r->r, 0->t |
 * 
 * The table rows produced would be:
 * 
 * | IPA Target | Phone | Position | Count | Accurate | Substitutions | Deletions |
 * |------------|-------|----------|-------|----------|---------------|-----------|
 * | br         | b     | O1       | 5     | 2        | 2             | 1         |
 * | br         | r     | O2       | 5     | 5        | 0             | 0         |
 * | br         | s     | +O0      | 1     | 0        | 0             | 1         |
 * | br         | l     | +O1      | 1     | 0        | 0             | 1         |
 * | br         | t     | +O2      | 1     | 0        | 0             | 1         |
 * 
 */
@OpNodeInfo(category="Table", description="", name="Accurate, Substitutions, Deletions", showInLibrary=true)
public final class ASDNode extends TableOpNode {
	
	public ASDNode() {
		super();
	}
	
	@Override
	public void operate(OpContext context) throws ProcessingException {
		final DefaultTableDataSource inputTable = (DefaultTableDataSource)context.get(super.tableInput);
		final DefaultTableDataSource outputTable = new DefaultTableDataSource();
	
		// find required input columns
		final int ipaTargetCol = inputTable.getColumnIndex("IPA Target");
		final int ipaActualCol = inputTable.getColumnIndex("IPA Actual");
		final int alignmentCol = inputTable.getColumnIndex("Alignment");
		
		if(ipaTargetCol < 0) 
			throw new ProcessingException(null, "Input table missing required IPA Target column");
		if(ipaActualCol < 0)
			throw new ProcessingException(null, "Input table missing required IPA Actual column");
		if(alignmentCol < 0)
			throw new ProcessingException(null, "Input table missing required Alignment column");
		
		// setup output table columns
		int col = 0;
		outputTable.setColumnTitle(col++, "IPA Target");
		outputTable.setColumnTitle(col++, "Phone");
		outputTable.setColumnTitle(col++, "Position");
		outputTable.setColumnTitle(col++, "Count");
		outputTable.setColumnTitle(col++, "Accurate");
		outputTable.setColumnTitle(col++, "Substitutions");
		outputTable.setColumnTitle(col++, "Deletions");
		
		// perform counts, store information in 3-level map
		final IpaTernaryTree<Map<String, Map<String, Counts>>> asdInfo = new IpaTernaryTree<>();
		
		for(int row = 0; row < inputTable.getRowCount(); row++) {
			var rowData = inputTable.getRow(row);
			
			final IPATranscript ipaTarget = (IPATranscript)rowData[ipaTargetCol];
			final IPATranscript ipaActual = (IPATranscript)rowData[ipaActualCol];
			final String alignmentTxt = (String)rowData[alignmentCol];
			final PhoneMap alignment = PhoneMap.fromString(ipaTarget, ipaActual, alignmentTxt);
			
			var transcriptInfo = asdInfo.get(ipaTarget);
			if(transcriptInfo == null) {
				transcriptInfo = new HashMap<>();
				asdInfo.put(ipaTarget, transcriptInfo);
			}
			
			for(int i = 0; i < alignment.getAlignmentLength(); i++) {
				final IPAElement ipaTEle = alignment.getTopAlignmentElements().get(i);
				final IPAElement ipaAEle = alignment.getBottomAlignmentElements().get(i);
				
				final IPAElement ipaEle = (ipaTEle != null ? ipaTEle : ipaAEle);
				var eleInfo = transcriptInfo.get(ipaEle.toString());
				if(eleInfo == null) {
					eleInfo = new HashMap<>();
					transcriptInfo.put(ipaEle.toString(), eleInfo);
				}
				
				String position = ipaEle.getScType().getIdChar() + ("" + (i+1));
				Counts currentCount = new Counts();
				++currentCount.count;
				if(ipaTEle != null && ipaAEle != null) {
					if(ipaTEle.toString().equals(ipaAEle.toString())) {
						++currentCount.accurate;
					} else {
						++currentCount.substitions;
					}
				} else if(ipaTEle != null && ipaAEle == null) {
					++currentCount.deletions;
				} else if(ipaTEle == null && ipaAEle != null) {
					++currentCount.deletions;
					position = ipaEle.getScType().getIdChar() + ("" + i) + "+";
				}
				
				var posInfo = eleInfo.get(position);
				if(posInfo == null) {
					posInfo = currentCount;
				} else {
					posInfo = posInfo.plus(currentCount);
				}
				eleInfo.put(position, posInfo);
			}
		}
		
		for(var ipa:asdInfo.keySet()) {
			var phoneInfo = asdInfo.get(ipa);
			for(var phone:phoneInfo.keySet()) {
				var positionInfo = phoneInfo.get(phone);
				for(var position:positionInfo.keySet()) {
					var counts = positionInfo.get(position);
					col = 0; 
					var rowData = new Object[outputTable.getColumnCount()];
					rowData[col++] = ipa;
					rowData[col++] = phone;
					rowData[col++] = position;
					rowData[col++] = Integer.valueOf(counts.count);
					rowData[col++] = Integer.valueOf(counts.accurate);
					rowData[col++] = Integer.valueOf(counts.substitions);
					rowData[col++] = Integer.valueOf(counts.deletions);
					outputTable.addRow(rowData);
				}
			}
		}
		
		context.put(super.tableOutput, outputTable);
	}
	
	private class Counts {
		int count = 0;
		int accurate = 0;
		int substitions = 0;
		int deletions = 0;
		
		public Counts plus(Counts c) {
			Counts retVal = new Counts();
			retVal.count = count + c.count;
			retVal.accurate = accurate + c.accurate;
			retVal.substitions = substitions + c.substitions;
			retVal.deletions = deletions + c.deletions;
			return retVal;
		}
	}
	
}
