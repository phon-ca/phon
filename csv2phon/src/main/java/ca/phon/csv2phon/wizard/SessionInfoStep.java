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
import java.io.File;
import java.io.FilenameFilter;
import java.text.ParseException;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.AbstractTableModel;

import org.jdesktop.swingx.JXTable;

import ca.phon.csv2phon.io.FileType;
import ca.phon.csv2phon.io.ImportDescriptionType;
import ca.phon.csv2phon.io.ObjectFactory;
import ca.phon.ui.decorations.DialogHeader;

/**
 * Panel for selecting destination corpus as well as
 * session name and date.
 *
 */
public class SessionInfoStep extends CSVImportStep {
	
	/** UI */
	private DialogHeader header;
	
	/* Corpus selection */
	private JTextField corpusField;
	private JLabel corpusLbl;
	
	/* Session table */
	private JXTable sessionTable;
//	private SessionTableModel tableModel;
	private String base;
	
	/** Constructor */
	public SessionInfoStep() {
		super();
		
		init();
	}
	
	@Override
	public void setSettings(ImportDescriptionType settings) {
		super.setSettings(settings);
		
		if(corpusField.getText().length() > 0) {
			settings.setCorpus(corpusField.getText());
		}
		
		SessionTableModel tableModel = new SessionTableModel();
		sessionTable.setModel(tableModel);
		sessionTable.packAll();
		
		sessionTable.getColumn(3).setCellEditor(new SessionDateEditor(new JTextField()));
	}

	private void init() {
		setLayout(new BorderLayout());
		header = new DialogHeader("CSV Import", "Select files for import and destination corpus.");
		add(header, BorderLayout.NORTH);
		
		JPanel corpusPanel = new JPanel();
		corpusPanel.setBorder(BorderFactory.createTitledBorder("Destination Corpus"));
		corpusPanel.setLayout(new BorderLayout());
		corpusField = new JTextField();
		corpusField.getDocument().addDocumentListener(new CorpusFieldListener());
		
		corpusLbl = new JLabel("Corpus will be created if it does not exist.");
		corpusPanel.add(corpusField, BorderLayout.NORTH);
		corpusPanel.add(corpusLbl, BorderLayout.SOUTH);
		
		JPanel sessionPanel = new JPanel();
		sessionPanel.setBorder(BorderFactory.createTitledBorder("Files to Import"));
		sessionPanel.setLayout(new BorderLayout());
		// setup table
//		tableModel = new SessionTableModel();
		sessionTable = new JXTable();
		sessionTable.setColumnControlVisible(true);
		sessionPanel.add(new JLabel("Select files to import and set session name, date and media."),
				BorderLayout.NORTH);
		sessionPanel.add(new JScrollPane(sessionTable), BorderLayout.CENTER);
		
		JPanel centerPanel = new JPanel();
		centerPanel.setLayout(new BorderLayout());
		centerPanel.add(corpusPanel, BorderLayout.NORTH);
		centerPanel.add(sessionPanel, BorderLayout.CENTER);
		
		add(centerPanel, BorderLayout.CENTER);
	}
	
	@Override
	public boolean validateStep() {
		boolean retVal = true;
		
//		// make sure at least the corpus name is given
		if(getSettings().getCorpus() == null || 
				getSettings().getCorpus().length() == 0) {
			retVal = false;
		}
		
		return retVal;
	}
	
	public String getBase() {
		return base;
	}
	
	public void setBase(String base) {
		this.base = base;
		
		File f = new File(base);
		corpusField.setText(f.getName());
	}
	
	private class CorpusFieldListener implements DocumentListener {

		@Override
		public void changedUpdate(DocumentEvent e) {
			updateCorpusEntry();
		}

		@Override
		public void insertUpdate(DocumentEvent e) {
			updateCorpusEntry();
		}

		@Override
		public void removeUpdate(DocumentEvent e) {
			updateCorpusEntry();
		}
		
		private void updateCorpusEntry() {
			String t = corpusField.getText();
			getSettings().setCorpus(t);
		}
		
	}
	
	/** 
	 * Table model for session information.
	 */
	private class SessionTableModel extends AbstractTableModel {
		
		
		public SessionTableModel() {
			setupFileTypes();
		}
		
		/**
		 * Setup filetype list and defaults
		 */
		private void setupFileTypes() {
			// first, prune out entries for files which no longer exist
			File baseDir = new File(getBase());
			for(FileType ft:getSettings().getFile().toArray(new FileType[0])) {
				File csvFile = new File(baseDir, ft.getLocation());
				if(!csvFile.exists()) {
					getSettings().getFile().remove(ft);
				}
			}
			
			ObjectFactory factory = new ObjectFactory();
			PhonDateFormat pdf = new PhonDateFormat(PhonDateFormat.YEAR_LONG);
			// next, add entries for all csv files found in base
			File[] csvFiles = getCSVFiles();
			for(File csvFile:csvFiles) {
				FileType ft = getFileInfo(csvFile);
				if(ft == null) {
					// create a new file type object
					// and fill with defaults
					ft = factory.createFileType();
					ft.setImport(true);
					ft.setLocation(csvFile.getName());
					// try to find a date in the name or use today
					ft.setDate(pdf.format(findDate(ft.getLocation())));
					ft.setMedia("");
					ft.setSession(csvFile.getName().substring(0, 
							csvFile.getName().indexOf(".csv")));
					getSettings().getFile().add(ft);
				}
			}
		}
		
		/**
		 * Attempt to generate a date from the given 
		 * string.  This method will look for the first
		 * occurance of the pattern '[0-9]{4}-[0-9]{2}-[0-9]{2}'
		 * 
		 * @return the date found in the string or ${today}
		 */
		private Calendar findDate(String s) {
			Pattern p = Pattern.compile("[0-9]{4}-[0-9]{2}-[0-9]{2}");
			Matcher m = p.matcher(s);
			
			Calendar retVal = Calendar.getInstance();
			
			if(m.find()) {
				String dateStr = 
					s.substring(m.start(), m.end());
				PhonDateFormat pdf = new PhonDateFormat(PhonDateFormat.YEAR_LONG);
				
				try {
					retVal = (Calendar)pdf.parseObject(dateStr);
				} catch (ParseException e) {
					e.printStackTrace();
				}
			}
			
			return retVal;
		}
		
		/*
		 * Table columns:
		 *  - selected
		 *  - filename
		 *  - session name
		 *  - session date
		 *  - media
		 */

		@Override
		public int getColumnCount() {
			return 4;
		}
		
		@Override
		public String getColumnName(int col) {
			String retVal = super.getColumnName(col);
			
			if(col == 0) {
				retVal = " ";
			} else if(col == 1) {
				retVal = "File";
			} else if(col == 2) {
				retVal = "Session Name";
			} else if(col == 3) {
				retVal = "Session Date";
			} else if(col == 4) {
				retVal = "Media";
			}
			
			return retVal;
		}
		
		/**
		 * Return all the csv files found in baseDir
		 * (non-recursive)
		 */
		private File[] getCSVFiles() {
			File baseDir = new File(getBase());
			return baseDir.listFiles(new FilenameFilter() {

				@Override
				public boolean accept(File dir, String name) {
					boolean retVal = false;
					
					retVal = name.toLowerCase().endsWith(".csv");
					
					return retVal;
				}
				
			});
		}

		@Override
		public int getRowCount() {
			return getSettings().getFile().size();
		}
		
		@Override
		public boolean isCellEditable(int row, int col) {
			boolean retVal = true;
			
			// can't edit filename
			if(col == 1) retVal = false;
			
			return retVal;
		}
		
		@Override
		public Class<?> getColumnClass(int col) {
			Class<?> retVal = String.class;
			
			if(col == 0) {
				retVal = Boolean.class;
			} else if(col == 3) {
				retVal = Calendar.class;
			}
			
			return retVal;
		}
		
		private FileType getFileInfo(File f) {
			FileType retVal = null;
			
			for(FileType ft:getSettings().getFile()) {
				if(ft.getLocation().equals(f.getName())) {
					retVal = ft;
				}
			}
			
			return retVal;
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			File f = getCSVFiles()[rowIndex];
			FileType ft = getFileInfo(f);

			Object retVal = null;
			
			if(ft == null) return retVal;
			
			if(columnIndex == 0) {
				retVal = ft.isImport();
			} else if(columnIndex == 1) {
				retVal = ft.getLocation();
			} else if(columnIndex == 2) {
				retVal = ft.getSession();
			} else if(columnIndex == 3) {
				// date
				retVal = ft.getDate();
			} else if(columnIndex == 4) {
				retVal = ft.getMedia();
			}
			
			return retVal;
		}

		@Override
		public void setValueAt(Object value, int rowIndex, int columnIndex) {
			File f = getCSVFiles()[rowIndex];
			FileType ft = getFileInfo(f);
			
			if(ft == null) return;
			
			if(columnIndex == 0) {
				ft.setImport((Boolean)value);
			} else if(columnIndex == 2) {
				ft.setSession(value.toString());
			} else if(columnIndex == 3) {
				// convert to date
				PhonDateFormat pdf = new PhonDateFormat(PhonDateFormat.YEAR_LONG);
				try {
					Calendar setDate = (Calendar)pdf.parseObject(value.toString());
					
					// and back to string
					ft.setDate(pdf.format(setDate));
				} catch (ParseException e) {
					e.printStackTrace();
				}
				
			}
		}
	}
	
	private class SessionDateEditor extends DefaultCellEditor {

		public SessionDateEditor(JTextField textField) {
			super(textField);
			
			textField.setDocument(new CalendarDocument(Calendar.getInstance()));
		}
		
	}

}
