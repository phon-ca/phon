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
package ca.phon.csv2phon.wizard;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.jdesktop.swingx.JXList;

import au.com.bytecode.opencsv.CSVReader;
import ca.phon.csv2phon.io.ColumnMapType;
import ca.phon.csv2phon.io.FileType;
import ca.phon.csv2phon.io.ImportDescriptionType;
import ca.phon.csv2phon.io.ObjectFactory;
import ca.phon.fontconverter.TranscriptConverter;
import ca.phon.ui.decorations.DialogHeader;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * Setup column mapping from CSV column name to Phon tier.
 *
 */
public class ColumnMapStep extends CSVImportStep {
	
	/**
	 * Possible tier placements
	 * 
	 */
	private enum TierType {
		None, // don't import
		Speaker,
		Orthography,
		IPATarget,
		IPAActual,
		Segment,
		Notes,
		Other; // dep tier
		
		private String[] titles = {
				"Don't import",
				"Speaker:Name",
				"Orthography",
				"IPA Target",
				"IPA Actual",
				"Segment",
				"Notes",
				"New tier"
		};
		
		public String getTitle() {
			return titles[ordinal()];
		}
	};
	
	/* UI */
	private DialogHeader header;
	
	private JXList columnList;
	
//	private JComboBox importTypeBox;
	private JPanel settingsPanel;
	private CardLayout settingsLayout;
	
	private String base;
	
	public ColumnMapStep() {
		super();
		
		init();
	}
	
	public String getBase() {
		return base;
	}
	
	public void setBase(String base) {
		this.base = base;
	}
	
	private void init() {
		setLayout(new BorderLayout());
		
		header = new DialogHeader("CSV Import", "Set up data mapping from csv record to Phon record.");
		add(header, BorderLayout.NORTH);
		
		columnList = new JXList();
		columnList.setBorder(BorderFactory.createTitledBorder("CSV Column"));
		columnList.setPreferredSize(new Dimension(200, 0));
		add(new JScrollPane(columnList), BorderLayout.WEST);
		columnList.addListSelectionListener(new ListSelectionListener() {

			@Override
			public void valueChanged(ListSelectionEvent e) {
				if(columnList.getSelectedValue() != null) {
					String csvCol = columnList.getSelectedValue().toString();
	//				System.out.println(csvCol);
					settingsLayout.show(settingsPanel, csvCol);
	//				settingsPanel.revalidate();
				}
			}
			
		});
		
		settingsPanel = new JPanel();
		add(settingsPanel, BorderLayout.CENTER);
		
		settingsLayout = new CardLayout();
		settingsPanel.setLayout(settingsLayout);
	}
	
	private ColumnMapType getMapping(String colName) {
		ColumnMapType retVal = null;
		
		for(ColumnMapType mapping:getSettings().getColumnmap()) {
			if(mapping.getCsvcolumn().equals(colName)) {
				retVal = mapping;
			}
		}
		
		return retVal;
	}
	
	/**
	 * Returns the columns from the first csv file
	 * to be imported.
	 * 
	 */
	private String[] getCSVColumns() {
		String[] retVal = new String[0];
		
		String firstCSVFile = null;
		for(FileType ft:getSettings().getFile()) {
			if(ft.isImport()) {
				firstCSVFile = ft.getLocation();
				break;
			}
		}
		
		if(firstCSVFile != null) {
			// open file and get column line
			try {
				CSVReader reader = new CSVReader(new FileReader(base + File.separator + firstCSVFile));
				retVal = reader.readNext();
				reader.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		return retVal;
	}
	
	private void setupDefaultColumnMaping() {
		String[] csvCols = getCSVColumns();
		
		// first prune existing entries
		for(ColumnMapType mapping:getSettings().getColumnmap().toArray(new ColumnMapType[0])) {
			boolean iscol = false;
			for(String csvCol:csvCols) {
				if(mapping.getCsvcolumn().equals(csvCol)) {
					iscol = true;
					break;
				}
			}
			if(!iscol) {
				getSettings().getColumnmap().remove(mapping);
			}
		}
		
		ObjectFactory factory = new ObjectFactory();
		for(String csvCol:csvCols) {
			ColumnMapType mapping = getMapping(csvCol);
			if(mapping == null) {
				mapping = factory.createColumnMapType();
				mapping.setCsvcolumn(csvCol);
				
				for(TierType tt:TierType.values()) {
					if(csvCol.equalsIgnoreCase(tt.getTitle())) {
						mapping.setPhontier(tt.getTitle());
						break;
					}
				}
				
				if(mapping.getPhontier() == null) {
					mapping.setPhontier(csvCol);
					mapping.setGrouped(false);
				}
				getSettings().getColumnmap().add(mapping);
			}
		}
	}
	
	@Override
	public void setSettings(ImportDescriptionType settings) {
		super.setSettings(settings);
		
		setupDefaultColumnMaping();
		
		// setup settings layout
		for(ColumnMapType mapping:getSettings().getColumnmap()) {
			ImportTypePanel mappingPanel = new ImportTypePanel(mapping.getCsvcolumn());
			settingsPanel.add(mappingPanel, mapping.getCsvcolumn());
		}
		columnList.setModel(new ColumnListModel());
		columnList.setSelectedIndex(0);
	}


	private class ColumnListModel extends AbstractListModel {

		@Override
		public Object getElementAt(int index) {
			ColumnMapType mapping = getSettings().getColumnmap().get(index);
			return mapping.getCsvcolumn();
		}

		@Override
		public int getSize() {
			return getSettings().getColumnmap().size();
		}
		
	}


	private class ImportTypePanel extends JPanel {
		
		/* Card layout for various settings */
		private CardLayout optionsLayout;
		private JPanel optionsPanel;
		
		private JComboBox importTypeBox;
		
		/* UI controls for options */
		private JComboBox targetFilterBox;
		private JComboBox actualFilterBox;
		private JComboBox targetSyllabifierBox;
		private JComboBox actualSyllabifierBox;
		private JTextField depTierField;
		private JCheckBox depGroupedBox;
		
		private String colName;
		
		public ImportTypePanel(String colName) {
			this.colName = colName;
			
			init();
		}
		
		private void init() {
			
			setLayout(new BorderLayout());
			
			JPanel topPanel = new JPanel(new BorderLayout());
			topPanel.setBorder(BorderFactory.createTitledBorder("Tier"));
			topPanel.add(new JLabel("Select tier for data import:"), BorderLayout.NORTH);
			
			importTypeBox = new JComboBox(TierType.values());
			importTypeBox.setRenderer(new DefaultListCellRenderer() {

				@Override
				public Component getListCellRendererComponent(JList list,
						Object value, int index, boolean isSelected,
						boolean cellHasFocus) {
					TierType type = (TierType)value;
					
					JLabel retVal = (JLabel)super.getListCellRendererComponent(list, value, index, isSelected,
							cellHasFocus);
					retVal.setText(type.getTitle());
					
					return retVal;
				}
				
			});
			
			importTypeBox.addItemListener(new ItemListener() {

				@Override
				public void itemStateChanged(ItemEvent e) {
					if(e.getStateChange() == ItemEvent.SELECTED) {
						TierType type = (TierType)importTypeBox.getSelectedItem();
//						System.out.println(type);
						optionsLayout.show(optionsPanel, type.getTitle());
						
						if(type.getTitle().equals(TierType.Other)) {
							ColumnMapType mapping = getMapping(colName);
							if(mapping != null) {
								mapping.setPhontier(depTierField.getText());
							}
						} else {
							ColumnMapType mapping = getMapping(colName);
							if(mapping != null) {
								mapping.setPhontier(type.getTitle());
							}
						}
					}
				}
				
			});
			topPanel.add(importTypeBox, BorderLayout.SOUTH);
		
			// setup UI
			ColumnMapType mapping = getMapping(colName);
			
			optionsPanel = new JPanel();
			optionsPanel.setBorder(BorderFactory.createTitledBorder("Options"));
			optionsLayout = new CardLayout();
			optionsPanel.setLayout(optionsLayout);
			
			List<String> converterNames = new ArrayList<String>();
			converterNames.add("");
			converterNames.addAll(TranscriptConverter.getAvailableConverterNames());
			
			ItemListener filterListener = new ItemListener() {

				@Override
				public void itemStateChanged(ItemEvent e) {
					if(e.getStateChange() == ItemEvent.SELECTED) {
						JComboBox box = (JComboBox)e.getSource();
						String v = box.getSelectedItem().toString();
						
						ColumnMapType mapping = getMapping(colName);
						if(mapping != null) {
							mapping.setFilter(v);
						}
					}
				}
				
			};
			targetFilterBox = new JComboBox(converterNames.toArray(new String[0]));
			targetFilterBox.addItemListener(filterListener);
			actualFilterBox = new JComboBox(converterNames.toArray(new String[0]));
			actualFilterBox.addItemListener(filterListener);
			
			ItemListener syllabifierListener = new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent e) {
					if(e.getStateChange() == ItemEvent.SELECTED) {
						JComboBox comboBox = (JComboBox)e.getSource();
						String syllabifier = (String)comboBox.getSelectedItem();
						ColumnMapType mapping = getMapping(colName);
						if(mapping != null) {
							mapping.setSyllabifier(syllabifier);
						}
					}
				}
			};
			List<String> syllabifiers = new ArrayList<String>();
			syllabifiers.add("");
			syllabifiers.addAll(Syllabifier.getAvailableSyllabifiers());
			targetSyllabifierBox = new JComboBox(syllabifiers.toArray(new String[0]));
			targetSyllabifierBox.addItemListener(syllabifierListener);
			actualSyllabifierBox = new JComboBox(syllabifiers.toArray(new String[0]));
			actualSyllabifierBox.addItemListener(syllabifierListener);
			
			depTierField = new JTextField();
			depTierField.getDocument().addDocumentListener(new DocumentListener() {

				@Override
				public void changedUpdate(DocumentEvent e) {
					updateTierName();
				}

				@Override
				public void insertUpdate(DocumentEvent e) {
					updateTierName();
				}

				@Override
				public void removeUpdate(DocumentEvent e) {
					updateTierName();
				}
				
				public void updateTierName() {
					ColumnMapType mapping = getMapping(colName);
					if(mapping != null && importTypeBox.getSelectedItem() == TierType.Other) {
						mapping.setPhontier(depTierField.getText());
					}
				}
			});
			depGroupedBox = new JCheckBox();
			depGroupedBox.addChangeListener(new ChangeListener() {

				@Override
				public void stateChanged(ChangeEvent e) {
					ColumnMapType mapping = getMapping(colName);
					if(mapping != null) {
						mapping.setGrouped(depGroupedBox.isSelected());
					}
				}
				
			});
			
			// setup card layout
			String simpleColLayout = "right:pref, 3dlu, fill:pref:grow";
			CellConstraints cc = new CellConstraints();
			
			JPanel nothingPanel = new JPanel();
			optionsPanel.add(nothingPanel, TierType.None.getTitle());
			
			// speaker
			JPanel speakerPanel = new JPanel(new BorderLayout());
			speakerPanel.add(new JLabel("<html><p>A new participant will be created if name is not found in given participant list.</p></html>"),
					BorderLayout.NORTH);
			optionsPanel.add(speakerPanel, TierType.Speaker.getTitle());
			
			// orthography
			JPanel orthoPanel = new JPanel();
			optionsPanel.add(orthoPanel, TierType.Orthography.getTitle());
			
			// IPA Target
			JPanel targetPanel = new JPanel();
			targetPanel.setLayout(new FormLayout(simpleColLayout, "pref, 3dlu, pref"));
			targetPanel.add(new JLabel("Transcription filter:"), cc.xy(1,1));
			targetPanel.add(targetFilterBox, cc.xy(3, 1));
			targetPanel.add(new JLabel("Syllabifier:"), cc.xy(1,3));
			targetPanel.add(targetSyllabifierBox, cc.xy(3,3));
			optionsPanel.add(targetPanel, TierType.IPATarget.getTitle());
			
			// IPA Actual
			JPanel actualPanel = new JPanel();
			actualPanel.setLayout(new FormLayout(simpleColLayout, "pref, 3dlu, pref"));
			actualPanel.add(new JLabel("Transcription filter:"), cc.xy(1,1));
			actualPanel.add(actualFilterBox, cc.xy(3, 1));
			actualPanel.add(new JLabel("Syllabifier:"), cc.xy(1,3));
			actualPanel.add(actualSyllabifierBox, cc.xy(3,3));
			optionsPanel.add(actualPanel, TierType.IPAActual.getTitle());
			
			// Segment
			JPanel segmentPanel = new JPanel(new BorderLayout());
			segmentPanel.add(new JLabel("<html><p>Values need to be in format mmm:ss.uuu-mmm:ss.uuu</p></html>"),
					BorderLayout.NORTH);
			optionsPanel.add(segmentPanel, TierType.Segment.getTitle());
			
			// Notes
			JPanel notesPanel = new JPanel();
			optionsPanel.add(notesPanel, TierType.Notes.getTitle());
			
			// Dependent Tiers
			JPanel otherPanel = new JPanel(new FormLayout(simpleColLayout, "pref, pref"));
			otherPanel.add(new JLabel("Grouped:"), cc.xy(1,1));
			otherPanel.add(depGroupedBox, cc.xy(3, 1));
			otherPanel.add(new JLabel("Tier Name:"), cc.xy(1, 2));
			otherPanel.add(depTierField, cc.xy(3, 2));
			optionsPanel.add(otherPanel, TierType.Other.getTitle());
			
			if(mapping != null) {
				// setup value for ui controls
				String importAs = mapping.getPhontier();
				
				if(TierType.None.getTitle().equals(importAs)) {
					importTypeBox.setSelectedItem(TierType.None);
				} else if(TierType.Orthography.getTitle().equals(importAs)) {
					importTypeBox.setSelectedItem(TierType.Orthography);
				} else if(TierType.Speaker.getTitle().equals(importAs)) {
					importTypeBox.setSelectedItem(TierType.Speaker);
				} else if(TierType.IPATarget.getTitle().equals(importAs)) {
					importTypeBox.setSelectedItem(TierType.IPATarget);
					
					// setup filter if it exists
					if(mapping.getFilter() != null) {
						targetFilterBox.setSelectedItem(mapping.getFilter());
					}
				} else if(TierType.IPAActual.getTitle().equals(importAs)) {
					importTypeBox.setSelectedItem(TierType.IPAActual);
					
					if(mapping.getFilter() != null) {
						actualFilterBox.setSelectedItem(mapping.getFilter());
					}
				} else if(TierType.Notes.getTitle().equals(importAs)) {
					importTypeBox.setSelectedItem(TierType.Notes);
				} else if(TierType.Segment.getTitle().equals(importAs)) {
					importTypeBox.setSelectedItem(TierType.Segment);
				} else {
					importTypeBox.setSelectedItem(TierType.Other);
					
					if(mapping.isGrouped() != null) {
						depGroupedBox.setSelected(mapping.isGrouped());
						depTierField.setText(importAs);
					}
				}
			}
			
			super.add(topPanel, BorderLayout.NORTH);
			super.add(optionsPanel, BorderLayout.CENTER);
		}
		
	}
	
	
}
