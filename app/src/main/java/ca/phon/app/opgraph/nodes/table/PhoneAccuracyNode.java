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

import java.awt.Component;
import java.text.ParseException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;

import javax.swing.JCheckBox;
import javax.swing.JPanel;

import org.jdesktop.swingx.VerticalLayout;

import ca.phon.app.log.LogUtil;
import ca.phon.app.opgraph.wizard.NodeWizard;
import ca.phon.ipa.CompoundPhone;
import ca.phon.ipa.IPAElement;
import ca.phon.ipa.IPATranscript;
import ca.phon.ipa.IPATranscriptBuilder;
import ca.phon.ipa.Phone;
import ca.phon.ipa.alignment.PhoneAligner;
import ca.phon.ipa.alignment.PhoneMap;
import ca.phon.ipa.tree.IpaTernaryTree;
import ca.phon.opgraph.InputField;
import ca.phon.opgraph.OpContext;
import ca.phon.opgraph.OpNodeInfo;
import ca.phon.opgraph.app.GraphDocument;
import ca.phon.opgraph.app.extensions.NodeSettings;
import ca.phon.opgraph.exceptions.ProcessingException;
import ca.phon.query.db.Result;
import ca.phon.query.db.ResultValue;
import ca.phon.query.report.datasource.DefaultTableDataSource;
import ca.phon.syllable.SyllableConstituentType;
import ca.phon.util.Range;

/**
 * Accurate, Substitutions, Deletions Node (PhoneAccuracyNode)
 * 
 * Create a table with the aforementioned columns calculated
 * from the IPA Target, IPA Actual, and Alignment columns of the input table.
 * While optional, syllabifaction info can be provided for the IPA Target and IPA Actual
 * values using the Target Syllabification and Acutal Syllabification columns respectively.
 * A position column is also included the value of which will be a
 * combination of the constituent type along with the index position
 * of the phone within the search result. Epenthesized phones are given a position
 * endwing with '+' where the index is the position of the last target phone position.
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
 * | IPA Target | Phone | Position | Count | Accurate | Substitutions | Deletions | Epenthesis |
 * |------------|-------|----------|-------|----------|---------------|-----------|------------|
 * | br         | b     | O1       | 5     | 2        | 2             | 1         | 0          |
 * | br         | r     | O2       | 5     | 5        | 0             | 0         | 0          |
 * | br         | s     | +O0      | 1     | 0        | 0             | 1         | 1          |
 * | br         | l     | +O1      | 1     | 0        | 0             | 1         | 1          |
 * | br         | t     | +O2      | 1     | 0        | 0             | 1         | 1          |
 * 
 */
@OpNodeInfo(category="Table", description="", name="Phone Accuracy", showInLibrary=true)
public final class PhoneAccuracyNode extends TableOpNode implements NodeSettings {
	
	private boolean _ignoreDiacritics = false;
	
	private boolean includeEpenthesis = false;
	
	private JPanel settingsPanel;
	private JCheckBox ignoreDiacriticsBox;
	private JCheckBox includeEpenthesisBox;
	
	public PhoneAccuracyNode() {
		super();
		
		putExtension(NodeSettings.class, this);
	}
	
	@Override
	public void operate(OpContext context) throws ProcessingException {
		final DefaultTableDataSource inputTable = (DefaultTableDataSource)context.get(super.tableInput);
		final DefaultTableDataSource outputTable = new DefaultTableDataSource();
		
		boolean ignoreDiacritics = isIgnoreDiacritics();
		// global settings have hightest priority
		if(context.containsKey(NodeWizard.IGNORE_DIACRITICS_GLOBAL_OPTION) && !context.get(NodeWizard.IGNORE_DIACRITICS_GLOBAL_OPTION).equals("default")) {
			ignoreDiacritics = (Boolean)context.get(NodeWizard.CASE_SENSITIVE_GLOBAL_OPTION);
		}
		boolean includeEpenthesis = isIncludeEpenthesis();
	
		// find required input columns
		final int ipaTargetCol = inputTable.getColumnIndex("IPA Target");
		final int ipaActualCol = inputTable.getColumnIndex("IPA Actual");
		final int alignmentCol = inputTable.getColumnIndex("Alignment");
		
		// optional columns (required for accurate positional information)
		final int resultCol = inputTable.getColumnIndex("Result");
		final int targetSyllCol = inputTable.getColumnIndex("Target Syllabification (Group)");
		final int actualSyllCol = inputTable.getColumnIndex("Actual Syllabification (Group)");
		
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
		if(includeEpenthesis)
			outputTable.setColumnTitle(col++, "Epenthesis");
		
		// perform counts, store information in 3-level map
		final IpaTernaryTree<Map<String, Map<String, Counts>>> asdInfo = new IpaTernaryTree<>();
		
		for(int row = 0; row < inputTable.getRowCount(); row++) {
			var rowData = inputTable.getRow(row);
			
			IPATranscript ipaTarget = (IPATranscript)rowData[ipaTargetCol];
			IPATranscript ipaActual = (IPATranscript)rowData[ipaActualCol];
			
			// get syllabified versions of transcripts if available
			Result result = null;
			if(resultCol >= 0)
				result = (Result)rowData[resultCol];
			if(result != null && targetSyllCol >= 0) {
				var rv = result.getResultValue("IPA Target");
				if(rv.isPresent()) {
					try {
						var ipaT = IPATranscript.parseIPATranscript((String)rowData[targetSyllCol]);
						var testIPA = getResultPhones(ipaT, rv.get());
						
						if(ipaTarget.toString().equals(testIPA.toString())) {
							ipaTarget = testIPA;
						}
					} catch (ParseException e) {
						LogUtil.severe(e);
					}
				}
			}
			
			if(result != null && actualSyllCol >= 0) {
				var rv = result.getResultValue("IPA Actual");
				if(rv.isPresent()) {
					try {
						var ipaA = IPATranscript.parseIPATranscript((String)rowData[actualSyllCol]);
						var testIPA = getResultPhones(ipaA, rv.get());
						
					try {	
						if(ipaActual.toString().equals(testIPA.toString())) {
							ipaActual = testIPA;
						}
					} catch (NullPointerException e) {
						System.out.println(e);
					}
					} catch (ParseException e) {
						LogUtil.severe(e);
					}
				}
			}
			
			// reconstruct alignment
			final String alignmentTxt = (String)rowData[alignmentCol];
			PhoneMap alignment = null;
			if(alignmentTxt.length() == 0) {
				alignment = (new PhoneAligner()).calculatePhoneAlignment(ipaTarget, ipaActual);
			} else {
				alignment = PhoneMap.fromString(ipaTarget, ipaActual, alignmentTxt);
			}
			
			var transcriptInfo = asdInfo.get(ipaTarget);
			if(transcriptInfo == null) {
				transcriptInfo = new LinkedHashMap<>();
				asdInfo.put(ipaTarget, transcriptInfo);
			}
			
			Map<SyllableConstituentType, Integer> scTypeCounts = new HashMap<>();
			for(SyllableConstituentType scType:SyllableConstituentType.values()) scTypeCounts.put(scType, 0);
			int lastIdx = 0;
			for(int i = 0; i < alignment.getAlignmentLength(); i++) {
				final IPAElement ipaTEle = alignment.getTopAlignmentElements().get(i);
				final IPAElement ipaAEle = alignment.getBottomAlignmentElements().get(i);
				
				final IPAElement ipaEle = (ipaTEle != null ? ipaTEle : ipaAEle);
				var eleInfo = transcriptInfo.get(ipaEle.toString());
				if(eleInfo == null) {
					eleInfo = new HashMap<>();
					transcriptInfo.put(ipaEle.toString(), eleInfo);
				}
				
				int typeIdx = scTypeCounts.get(ipaEle.getScType()) + 1;
				String position = ipaEle.getScType().getIdChar() + ("" + typeIdx);
				
				Counts currentCount = new Counts();
				++currentCount.count;
				if(ipaTEle != null && ipaAEle != null) {
					if(compareElements(ipaTEle, ipaAEle, ignoreDiacritics)) {
						++currentCount.accurate;
					} else {
						++currentCount.substitions;
					}
					scTypeCounts.put(ipaEle.getScType(), typeIdx);
				} else if(ipaTEle != null && ipaAEle == null) {
					++currentCount.deletions;
					scTypeCounts.put(ipaEle.getScType(), typeIdx);
				} else if(ipaTEle == null && ipaAEle != null) {
					if(isIncludeEpenthesis()) {
						++currentCount.epenthesis;
						position = ipaEle.getScType().getIdChar() + ("" + lastIdx) + "+";
					} else {
						// ignore
						continue;
					}
				}
				lastIdx = typeIdx;
				
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
					if(includeEpenthesis)
						rowData[col++] = Integer.valueOf(counts.epenthesis);
					outputTable.addRow(rowData);
				}
			}
		}
		
		context.put(super.tableOutput, outputTable);
	}
	
	private boolean compareElements(IPAElement ele1, IPAElement ele2, boolean ignoreDiacritics) {
		if(ignoreDiacritics) {
			final IPATranscript testIPA1 = 
					(new IPATranscriptBuilder()).append(ele1).toIPATranscript().stripDiacritics();
			final IPATranscript testIPA2 = 
					(new IPATranscriptBuilder()).append(ele2).toIPATranscript().stripDiacritics();
			return testIPA1.toString().matches(testIPA2.toString());
		} else {
			return ele1.toString().equals(ele2.toString());
		}
	}
	
	private IPATranscript getResultPhones(IPATranscript ipa, ResultValue rv) {
		// nothing to extract
		if(ipa.length() == 0)
			return new IPATranscript();
		
		// empty range
		var stringRange = rv.getRange();
		if(stringRange.getStart() == stringRange.getEnd())
			return new IPATranscript();
		
		var ipaRange = new Range(ipa.ipaIndexOf(stringRange.getStart()), 
				ipa.ipaIndexOf(stringRange.getLast()),
				false);
		
		try {
			var retVal = ipa.subsection(ipaRange.getFirst(), ipaRange.getLast()+1);
			return retVal;
		} catch (Exception e) {
			LogUtil.warning(e);
			return new IPATranscript();
		}
	}
	
	private class Counts {
		int count = 0;
		int accurate = 0;
		int substitions = 0;
		int deletions = 0;
		int epenthesis = 0;
		
		public Counts plus(Counts c) {
			Counts retVal = new Counts();
			retVal.count = count + c.count;
			retVal.accurate = accurate + c.accurate;
			retVal.substitions = substitions + c.substitions;
			retVal.deletions = deletions + c.deletions;
			retVal.epenthesis = epenthesis + c.epenthesis;
			return retVal;
		}
	}
	
	public boolean isIgnoreDiacritics() {
		return (this.ignoreDiacriticsBox != null ? this.ignoreDiacriticsBox.isSelected() : this._ignoreDiacritics);
	}
	
	public void setIgnoreDiacritics(boolean ignoreDiacritics) {
		this._ignoreDiacritics = ignoreDiacritics;
		if(this.ignoreDiacriticsBox != null)
			this.ignoreDiacriticsBox.setSelected(ignoreDiacritics);
	}

	public boolean isIncludeEpenthesis() {
		return (this.includeEpenthesisBox != null ? this.includeEpenthesisBox.isSelected() : this.includeEpenthesis);
	}
	
	public void setIncludeEpenthesis(boolean includeEpenthesis) {
		this.includeEpenthesis = includeEpenthesis;
		if(this.includeEpenthesisBox != null)
			this.includeEpenthesisBox.setSelected(includeEpenthesis);
	}
	
	@Override
	public Component getComponent(GraphDocument document) {
		if(settingsPanel == null) {
			settingsPanel = new JPanel(new VerticalLayout());
			
			ignoreDiacriticsBox = new JCheckBox("Ignore diacritics");
			ignoreDiacriticsBox.setSelected(_ignoreDiacritics);
			
			includeEpenthesisBox = new JCheckBox("Include epenthesis");
			includeEpenthesisBox.setSelected(includeEpenthesis);
			
			settingsPanel.add(ignoreDiacriticsBox);
			settingsPanel.add(includeEpenthesisBox);
		}
		return settingsPanel;
	}
	
	@Override
	public Properties getSettings() {
		Properties props = new Properties();
		props.setProperty("ignoreDiacritics", Boolean.toString(isIgnoreDiacritics()));
		props.setProperty("includeEpenthesis", Boolean.toString(isIncludeEpenthesis()));
		return props;
	}

	@Override
	public void loadSettings(Properties properties) {
		setIgnoreDiacritics(Boolean.parseBoolean(properties.getProperty("ignoreDiacritics", "false")));
		setIncludeEpenthesis(Boolean.parseBoolean(properties.getProperty("includeEpenthesis", "false")));
	}
	
}
