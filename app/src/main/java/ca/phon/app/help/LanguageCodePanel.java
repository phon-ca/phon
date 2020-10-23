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
package ca.phon.app.help;

import java.awt.*;
import java.awt.event.*;
import java.util.regex.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;

import org.jdesktop.swingx.*;

import ca.phon.util.*;

/**
 * UI for viewing and selecting a language from
 * the ISO-639-3 language code list.
 */
public class LanguageCodePanel extends JPanel {

	private static final long serialVersionUID = 1248170201949741399L;

	/**
	 * Search field
	 */
	private JTextField filterField;
	
	/**
	 * Apply filter button
	 * 
	 */
	private JButton applyFilterBtn;
	
	/**
	 * Table
	 */
	private JXTable languageTable;
	
	/**
	 * Currently selected language code
	 */
	private LanguageEntry selectedEntry = null;
	

	/**
	 * Language entries
	 */
	private LanguageParser languageEntries = LanguageParser.getInstance();
	
	
	/** 
	 * Constructor
	 * 
	 */
	public LanguageCodePanel() {
		super();
		
		init();
	}
	
	private void init() {
		setLayout(new BorderLayout());
		
		// setup text field
		filterField = new JTextField();
		filterField.setAction(new ApplyFilterAction());
		filterField.getDocument().addDocumentListener(new LanguageDocumentListener());
		
		add(filterField, BorderLayout.NORTH);
		
		// setup table
		languageTable = new JXTable(new LanguageTableModel());
		languageTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		languageTable.getSelectionModel().addListSelectionListener(new LanguageSelectionListener());
		languageTable.packAll();
		JScrollPane languageScroller = new JScrollPane(languageTable);
		add(languageScroller, BorderLayout.CENTER);
	}
	
	/**
	 * Table model
	 */
	private class LanguageTableModel extends AbstractTableModel {

		@Override
		public int getColumnCount() {
			return 2;
		}

		@Override
		public int getRowCount() {
			return languageEntries.getLanguages().size();
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			LanguageEntry entry = languageEntries.getLanguages().get(rowIndex);
			
			String retVal = "";
			if(columnIndex == 0) {
				retVal = entry.getProperty(LanguageEntry.ID_639_3);
			} else if(columnIndex == 1) {
				retVal = entry.getProperty(LanguageEntry.REF_NAME);
			}
			
			return retVal;
		}
		
		@Override
		public String getColumnName(int colIndex) {
			String retVal = "";
			
			if(colIndex == 0) {
				retVal = "ISO-639-3";
			} else if(colIndex == 1) {
				retVal = "Language Name";
			}
			
			return retVal;
		}
		
	}
	
	/**
	 * Language selection listener
	 */
	private class LanguageSelectionListener implements ListSelectionListener {

		@Override
		public void valueChanged(ListSelectionEvent e) {
			int selectedRow = languageTable.getSelectedRow();
			if(selectedRow >= 0) {
				selectedRow = languageTable.convertRowIndexToModel(selectedRow);
				
				LanguageEntry le = languageEntries.getLanguages().get(selectedRow);
				selectedEntry = le;
			}
		}
		
	}
	
	
	/**
	 * Text field listener
	 */
	private class LanguageDocumentListener implements DocumentListener {

		@Override
		public void changedUpdate(DocumentEvent e) {
			updateFilter();
		}

		@Override
		public void insertUpdate(DocumentEvent e) {
			updateFilter();
		}

		@Override
		public void removeUpdate(DocumentEvent e) {
			updateFilter();
		}
		
		public void updateFilter() {
			(new ApplyFilterAction()).actionPerformed(null);
		}
		
	}
	
	/**
	 * Filter action 
	 *
	 */
	private class ApplyFilterAction extends AbstractAction {

		@Override
		public void actionPerformed(ActionEvent e) {
			
			try {
				Pattern p = Pattern.compile(filterField.getText());
				

				
				RowFilter<Object, Object> filter = RowFilter.regexFilter(filterField.getText());
				languageTable.setRowFilter(filter);
			} catch (PatternSyntaxException pe) {
				
			}
		}
		
	}
}
