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
package ca.phon.app.opgraph.nodes.table;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

import ca.gedge.opgraph.OpContext;
import ca.gedge.opgraph.OpNodeInfo;
import ca.gedge.opgraph.app.GraphDocument;
import ca.gedge.opgraph.app.extensions.NodeSettings;
import ca.gedge.opgraph.exceptions.ProcessingException;
import ca.phon.ipa.IPATranscript;
import ca.phon.ipa.LevenshteinDistance;
import ca.phon.ipa.tree.IpaTernaryTree;
import ca.phon.query.TableUtils;
import ca.phon.query.report.datasource.DefaultTableDataSource;
import ca.phon.query.report.datasource.TableDataSource;
import ca.phon.session.SystemTierType;

/**
 * 
 */
@OpNodeInfo(name="IPA Variability", category="Report", description="Calculate IPA variability between IPA Target and IPA Actual")
public class IPAVariabilityNode extends TableOpNode implements NodeSettings {
	
	private JCheckBox ignoreDiacriticsBox;
	
	private JPanel settingsPanel;
	
	private boolean ignoreDiacritics = true;
	
	public IPAVariabilityNode() {
		super();
		
		putExtension(NodeSettings.class, this);
	}

	@Override
	public void operate(OpContext context) throws ProcessingException {
		final TableDataSource table = (TableDataSource)context.get(tableInput);
		
		int ipaTidx = super.getColumnIndex(table, SystemTierType.IPATarget.getName());
		if(ipaTidx < 0) {
			throw new ProcessingException(null, "Table has no " + SystemTierType.IPATarget.getName() + " column.");
		}
		
		int ipaAidx = super.getColumnIndex(table, SystemTierType.IPAActual.getName());
		if(ipaAidx < 0) {
			throw new ProcessingException(null, "Table has no " + SystemTierType.IPAActual.getName() + " column.");
		}
		
		// group by session if info is available
		// TODO make this an option
		int sessionIdx = super.getColumnIndex(table, "Session");
		
		boolean ignoreDiacritics = isIgnoreDiacritics();
		if(context.containsKey("ignoreDiacritics")) {
			ignoreDiacritics = (boolean)context.get("ignoreDiacritics");
		}
		
		final Map<GroupKey, IpaTernaryTree<List<IPATranscript>>> tokenCounts =
				new LinkedHashMap<>();
		for(int row = 0; row < table.getRowCount(); row++) {
			checkCanceled();
			
			final Object groupVal = 
					(sessionIdx >= 0 ? table.getValueAt(row, sessionIdx) : "*");
			final GroupKey groupKey = new GroupKey(groupVal, ignoreDiacritics);
			IpaTernaryTree<List<IPATranscript>> tokenCount = 
					tokenCounts.get(groupKey);
			if(tokenCount == null) {
				tokenCount = new IpaTernaryTree<>();
				tokenCounts.put(groupKey, tokenCount);
			}
			
			IPATranscript ipaT = 
					IPATranscript.class.cast(table.getValueAt(row, ipaTidx));
			IPATranscript ipaA =
					IPATranscript.class.cast(table.getValueAt(row, ipaAidx));
			if(ignoreDiacritics) {
				ipaT = ipaT.removePunctuation().stripDiacritics();
				ipaA = ipaA.removePunctuation().stripDiacritics();
			}
			
			List<IPATranscript> productions = tokenCount.get(ipaT);
			if(productions == null) {
				productions = new ArrayList<>();
				tokenCount.put(ipaT, productions);
			}
			productions.add(ipaA);
		}
		
		final DefaultTableDataSource outputTable = new DefaultTableDataSource();
		
		for(GroupKey groupKey:tokenCounts.keySet()) {
			final Object[] rowData = new Object[7];
			// produce table
			int numRepatedWords = 0;
			int numAllCorrect = 0;
			int numOneOrMoreCorrect = 0;
			int numSameError = 0;
			int numDifferentErrors = 0;
			float sumOfAvgDistances = 0;
			
			final IpaTernaryTree<List<IPATranscript>> tokenCount = tokenCounts.get(groupKey);
			final List<IPATranscript> repeatedTokens = 
					tokenCount.keySet().stream()
						.filter( (ipa) -> tokenCount.get(ipa).size() > 1 )
						.collect(Collectors.toList());
			numRepatedWords = repeatedTokens.size();
			
			for(IPATranscript ipa:repeatedTokens) {
				checkCanceled();
				
				int numCorrect = 0;
				
				final List<IPATranscript> productions = tokenCount.get(ipa);
				final Set<IPATranscript> distinctProductions = new LinkedHashSet<>(productions);
				for(IPATranscript production:tokenCount.get(ipa)) {
					if(TableUtils.checkEquals(ipa, production, false, ignoreDiacritics)) {
						++numCorrect;
					}
				}
				
				if(numCorrect == productions.size()) {
					++numAllCorrect;
				} else {
					if(numCorrect > 0 && numCorrect < productions.size()) {
						++numOneOrMoreCorrect;
					}
					distinctProductions.remove(ipa);
					if(distinctProductions.size() == 1) {
						++numSameError;
					} else {
						++numDifferentErrors;
					}
					
					int totalDistance = 0;
					for(IPATranscript production:productions) {
						totalDistance += LevenshteinDistance.distance(ipa, production);
					}
					float avg = ((float)totalDistance)/((float)productions.size());
					sumOfAvgDistances += avg;
				}
			}
			
			// append row to table
			rowData[0] = groupKey.key;
			rowData[1] = numRepatedWords;
			rowData[2] = numAllCorrect;
			rowData[3] = numOneOrMoreCorrect;
			rowData[4] = numSameError;
			rowData[5] = numDifferentErrors;
			rowData[6] = sumOfAvgDistances / numRepatedWords;
			outputTable.addRow(rowData);
		}
		
		outputTable.setColumnTitle(0, "Session");
		outputTable.setColumnTitle(1, "# Repeated IPA Target");
		outputTable.setColumnTitle(2, "# All Correct");
		outputTable.setColumnTitle(3, "# One or More Correct");
		outputTable.setColumnTitle(4, "# Same Error");
		outputTable.setColumnTitle(5, "# Different Errors");
		outputTable.setColumnTitle(6, "Avg Distance");
		
		context.put(tableOutput, outputTable);
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
	
	public boolean isIgnoreDiacritics() {
		return (this.ignoreDiacriticsBox != null ? this.ignoreDiacriticsBox.isSelected() : this.ignoreDiacritics);
	}
	
	public void setIgnoreDiacritics(boolean ignoreDiacritics) {
		this.ignoreDiacritics = ignoreDiacritics;
		if(this.ignoreDiacriticsBox != null)
			this.ignoreDiacriticsBox.setSelected(ignoreDiacritics);
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
			ignoreDiacritics = Boolean.parseBoolean(properties.getProperty("ignoreDiacritics"));
	}

	private class GroupKey implements Comparable<GroupKey> {
		Object key;
		
		boolean ignoreDiacritics;
		
		public GroupKey(Object key, boolean ignoreDiacritics) {
			this.key = key;
			this.ignoreDiacritics = ignoreDiacritics;
		}
		
		@Override
		public boolean equals(Object o2) {
			if(!(o2 instanceof GroupKey)) return false;
			return TableUtils.checkEquals(key, ((GroupKey)o2).key, 
					false,
					this.ignoreDiacritics);
		}
		
		@Override
		public String toString() {
			return TableUtils.objToString(key, this.ignoreDiacritics);
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
