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
package ca.phon.app.query.report;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

import ca.phon.query.report.io.InventorySection;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * Inventory section panel
 *
 */
public class InventorySectionPanel extends SectionPanel<InventorySection> {

	private static final long serialVersionUID = -211182730571261600L;

	/**
	 * Help text
	 */
	private final static String INFO_TEXT = 
		"<html><body>" +
		"<i>Inventory</i>" +
		"<p>Outputs a count of distinct result values for a result set.</p>" +
		"</body></html>";
	
	/*
	 * UI
	 */
	private JCheckBox caseSensitiveBox;
	private JCheckBox ignoreDiacriticsBox;
	
	private JCheckBox groupByFormatBox;
	
	private JCheckBox includeResultValueBox;
	private JCheckBox includeMetadataBox;
	
	private JCheckBox includeExcludedBox;
	
	private JPanel optionsPanel;
	
	public JPanel getOptionsPanel() {
		return this.optionsPanel;
	}
	
	public InventorySectionPanel(InventorySection section) {
		super(section);
		
		init();
	}
	
	private void init() {
		super.setInformationText(getClass().getName()+".info", INFO_TEXT);
		
		InventorySection invData = getSection();
		
		caseSensitiveBox = new JCheckBox("Case sensitive");
		caseSensitiveBox.setSelected(invData.isCaseSensitive());
		caseSensitiveBox.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				getSection().setCaseSensitive(caseSensitiveBox.isSelected());
				
			}
		});
		
		ignoreDiacriticsBox = new JCheckBox("Ignore diacritics (i.e., diacritics are removed from result values)");
		ignoreDiacriticsBox.setSelected(invData.isIgnoreDiacritics());
		ignoreDiacriticsBox.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				getSection().setIgnoreDiacritics(ignoreDiacriticsBox.isSelected());
			}
			
		});
		
		groupByFormatBox = new JCheckBox("Group results by format (e.g., for 'Data Tiers' queries this will group results by tier)");
		groupByFormatBox.setSelected(invData.isGroupByFormat());
		groupByFormatBox.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				getSection().setGroupByFormat(groupByFormatBox.isSelected());
			}
		});
		
		includeResultValueBox = new JCheckBox("Include result value (i.e., data matched in the result set)");
		includeResultValueBox.setSelected(invData.isIncludeResultValue());
		includeResultValueBox.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				getSection().setIncludeResultValue(includeResultValueBox.isSelected());
			}
		});
		
		includeMetadataBox = new JCheckBox("Include metadata in result value when counting");
		includeMetadataBox.setSelected(invData.isIncludeMetadata());
		includeMetadataBox.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				getSection().setIncludeMetadata(includeMetadataBox.isSelected());	
			}
		});
		
		includeExcludedBox = new JCheckBox("Include excluded results");
		includeExcludedBox.setSelected(invData.isIncludeExcluded());
		includeExcludedBox.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				getSection().setIncludeExcluded(includeExcludedBox.isSelected());
			}
		});
		
		JPanel resultFormatPanel = new JPanel(new GridLayout(0, 1));
		resultFormatPanel.setBorder(BorderFactory.createTitledBorder("Result Data"));
		resultFormatPanel.add(includeResultValueBox);
		resultFormatPanel.add(includeMetadataBox);
		resultFormatPanel.add(includeExcludedBox);
		
		JPanel dataOptionsPanel = new JPanel(new GridLayout(0, 1));
		dataOptionsPanel.setBorder(BorderFactory.createTitledBorder("Options"));
		dataOptionsPanel.add(caseSensitiveBox);
		dataOptionsPanel.add(ignoreDiacriticsBox);
		
		JPanel groupingOptions = new JPanel(new GridLayout(0, 1));
		groupingOptions.setBorder(BorderFactory.createTitledBorder("Grouping"));
		groupingOptions.add(groupByFormatBox);
		
		FormLayout layout = new FormLayout(
				"fill:pref:grow", "pref, pref, pref");
		CellConstraints cc = new CellConstraints();
		optionsPanel = new JPanel(layout);
		optionsPanel.add(dataOptionsPanel, cc.xy(1,2));
		optionsPanel.add(resultFormatPanel, cc.xy(1,1));
		optionsPanel.add(groupingOptions, cc.xy(1,3));
		
		add(optionsPanel, BorderLayout.CENTER);
		
	}

}
