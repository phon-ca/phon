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
package ca.phon.app.opgraph.nodes.table;

import ca.phon.app.opgraph.wizard.NodeWizard;
import ca.phon.ipa.IPATranscript;
import ca.phon.opgraph.*;
import ca.phon.opgraph.app.GraphDocument;
import ca.phon.opgraph.app.extensions.NodeSettings;
import ca.phon.opgraph.exceptions.ProcessingException;
import ca.phon.orthography.Orthography;
import ca.phon.query.TableUtils;
import ca.phon.query.report.datasource.*;
import ca.phon.session.SystemTierType;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 *
 */
@OpNodeInfo(name="Homophony Analysis", category="IPA Table Analysis",
description="Homophony Analysis (IPA Target)")
public class HomophonyAnalysis extends TableOpNode implements NodeSettings {

	private JCheckBox ignoreDiacriticsBox;

	private JPanel settingsPanel;

	private boolean ignoreDiacritics = false;

	public HomophonyAnalysis() {
		super();

		putExtension(NodeSettings.class, this);
	}

	@Override
	public void operate(OpContext context) throws ProcessingException {
		final TableDataSource table = (TableDataSource)context.get(tableInput);

		int orthoIdx = super.getColumnIndex(table, SystemTierType.Orthography.getName());
		if(orthoIdx < 0) {
			throw new ProcessingException(null, "Table has no " + SystemTierType.Orthography.getName() + " column.");
		}

		int ipaTidx = super.getColumnIndex(table, SystemTierType.IPATarget.getName());
		if(ipaTidx < 0) {
			throw new ProcessingException(null, "Table has no " + SystemTierType.IPATarget.getName() + " column.");
		}

		// group by session if info is available
		int sessionIdx = super.getColumnIndex(table, "Session");

		final Map<GroupKey, Map<String, Integer>> orthoTokens = new LinkedHashMap<>();
		final Map<GroupKey, Map<IPATranscript, Integer>> ipaTargetTokens = new LinkedHashMap<>();

		boolean ignoreDiacritics =
				(context.containsKey(NodeWizard.IGNORE_DIACRITICS_GLOBAL_OPTION) ?
						(Boolean)context.get(NodeWizard.IGNORE_DIACRITICS_GLOBAL_OPTION) : isIgnoreDiacritics());

		for(int row = 0; row < table.getRowCount(); row++) {
			checkCanceled();

			final Object groupVal =
					(sessionIdx >= 0 ? table.getValueAt(row, sessionIdx) : "*");
			final GroupKey groupKey = new GroupKey(groupVal);
			Map<String, Integer> orthoTokenCount =
					orthoTokens.get(groupKey);
			if(orthoTokenCount == null) {
				orthoTokenCount = new HashMap<>();
				orthoTokens.put(groupKey, orthoTokenCount);
			}
			Orthography ortho =
					Orthography.class.cast(table.getValueAt(row, orthoIdx));
			Integer orthoCount = orthoTokenCount.get(ortho.toString());
			if(orthoCount == null) {
				orthoCount = 0;
			}
			++orthoCount;
			orthoTokenCount.put(ortho.toString(), orthoCount);

			Map<IPATranscript, Integer> ipaTargetCount =
					ipaTargetTokens.get(groupKey);
			if(ipaTargetCount == null) {
				ipaTargetCount = new HashMap<>();
				ipaTargetTokens.put(groupKey, ipaTargetCount);
			}
			IPATranscript ipaT =
					IPATranscript.class.cast(table.getValueAt(row, ipaTidx));
			if(ignoreDiacritics) {
				ipaT = ipaT.removePunctuation().stripDiacritics();
			}
			Integer ipaTCount = ipaTargetCount.get(ipaT);
			if(ipaTCount == null) {
				ipaTCount = 0;
			}
			++ipaTCount;
			ipaTargetCount.put(ipaT, ipaTCount);
		}

		final DefaultTableDataSource outputTable = new DefaultTableDataSource();

		for(GroupKey groupKey:orthoTokens.keySet()) {
			checkCanceled();

			final Object[] rowData = new Object[7];

			int homophonousTypes = 0;
			int lexicalTypes = 0;
			int homophonousTokens = 0;
			int phoneticTokens = 0;

			// # of distinct IPA Target forms
			Map<IPATranscript, Integer> ipaTCounts = ipaTargetTokens.get(groupKey);
			homophonousTypes = ipaTCounts.keySet().size();
			homophonousTokens = homophonousTypes;
			phoneticTokens = ipaTCounts.values().stream()
					.collect(Collectors.summingInt( i -> i ));

			// # of distinct Orthographic forms
			Map<String, Integer> orthoCounts = orthoTokens.get(groupKey);
			lexicalTypes = orthoCounts.keySet().size();

			// append row to table
			rowData[0] = groupKey.key;
			rowData[1] = homophonousTypes;
			rowData[2] = lexicalTypes;
			rowData[3] = (float)homophonousTypes / (float)lexicalTypes;
			rowData[4] = homophonousTokens;
			rowData[5] = phoneticTokens;
			rowData[6] = (float)homophonousTokens / (float)phoneticTokens;
			outputTable.addRow(rowData);
		}

		outputTable.setColumnTitle(0, "Session");
		outputTable.setColumnTitle(1, "Homophonous Types");
		outputTable.setColumnTitle(2, "Lexical Types");
		outputTable.setColumnTitle(3, "Ratio (Homophonous/Lexical)");
		outputTable.setColumnTitle(4, "Homophonous Tokens");
		outputTable.setColumnTitle(5, "Phonetic Tokens");
		outputTable.setColumnTitle(6, "Ratio (Homophonous/Phonetic)");

		context.put(tableOutput, outputTable);
	}

	public void setIgnoreDiacritics(boolean ignoreDiacritics) {
		this.ignoreDiacritics = ignoreDiacritics;
		if(this.ignoreDiacriticsBox != null)
			this.ignoreDiacriticsBox.setSelected(ignoreDiacritics);;
	}

	public boolean isIgnoreDiacritics() {
		return (this.ignoreDiacriticsBox != null ? this.ignoreDiacriticsBox.isSelected() : this.ignoreDiacritics);
	}

	@Override
	public Component getComponent(GraphDocument document) {
		if(settingsPanel == null) {
			settingsPanel = new JPanel();
			settingsPanel.setLayout(new GridBagLayout());

			final GridBagConstraints gbc = new GridBagConstraints();
			gbc.insets = new Insets(2, 2, 5, 2);
			gbc.anchor = GridBagConstraints.EAST;
			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbc.weightx = 1.0;
			gbc.gridx = 0;
			gbc.gridy = 0;

			ignoreDiacriticsBox = new JCheckBox("Ignore diacritics");
			ignoreDiacriticsBox.setSelected(ignoreDiacritics);
			settingsPanel.add(ignoreDiacriticsBox, gbc);

			settingsPanel.setBorder(BorderFactory.createTitledBorder("Settings"));
		}
		return settingsPanel;
	}

	@Override
	public Properties getSettings() {
		final Properties retVal = new Properties();
		retVal.put("ignoreDiacritics", Boolean.toString(ignoreDiacritics));
		return retVal;
	}

	@Override
	public void loadSettings(Properties properties) {
		if(properties.contains("ignoreDiacritics"))
			setIgnoreDiacritics(Boolean.parseBoolean(properties.getProperty("ignoreDiacritics")));
	}

	private class GroupKey implements Comparable<GroupKey> {
		Object key;

		public GroupKey(Object key) {
			this.key = key;
		}

		@Override
		public boolean equals(Object o2) {
			if(!(o2 instanceof GroupKey)) return false;
			return TableUtils.checkEquals(key, ((GroupKey)o2).key,
					false,
					isIgnoreDiacritics());
		}

		@Override
		public String toString() {
			return TableUtils.objToString(key, isIgnoreDiacritics());
		}

		@Override
		public int hashCode() {
			return toString().hashCode();
		}

		@Override
		public int compareTo(GroupKey k2) {
			return toString().compareTo(k2.toString());
		}

	}

}
