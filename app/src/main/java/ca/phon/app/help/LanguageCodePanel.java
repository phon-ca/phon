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
package ca.phon.app.help;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;

import org.jdesktop.swingx.JXTable;

import ca.phon.util.LanguageEntry;
import ca.phon.util.LanguageParser;

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
