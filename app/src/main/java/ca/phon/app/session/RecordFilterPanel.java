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
package ca.phon.app.session;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SortOrder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

import org.apache.commons.lang3.StringUtils;
import org.jdesktop.swingx.JXTable;
import org.joda.time.DateTime;

import ca.phon.project.Project;
import ca.phon.query.db.Query;
import ca.phon.query.db.QueryManager;
import ca.phon.query.db.ResultSet;
import ca.phon.query.db.ResultSetManager;
import ca.phon.query.db.ResultSetRecordFilter;
import ca.phon.session.AbstractRecordFilter;
import ca.phon.session.DateFormatter;
import ca.phon.session.Participant;
import ca.phon.session.ParticipantRecordFilter;
import ca.phon.session.RangeRecordFilter;
import ca.phon.session.Record;
import ca.phon.session.RecordFilter;
import ca.phon.session.Session;
import ca.phon.ui.AbstractVerifier;
import ca.phon.ui.PhonGuiConstants;
import ca.phon.ui.VerifierListener;
import ca.phon.ui.toast.Toast;
import ca.phon.ui.toast.ToastFactory;
import ca.phon.util.Tuple;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * Displays options for identify records by range, 
 * speaker, or search results.
 * 
 * 
 */
public class RecordFilterPanel extends JPanel {
	
	private final static Logger LOGGER = Logger.getLogger(RangeRecordFilter.class.getName());

	/** The transcript */
	private Session t;
	
	/* UI */
	private ButtonGroup radioGrp;
	private JRadioButton allBtn;
	private JRadioButton rangeBtn;
	private JRadioButton speakerBtn;
	private JRadioButton searchBtn;
	
	private JTextField rangeField;
	private JXTable speakerTbl;
	private JXTable searchTbl;
	

	private List<Participant> selectedParticipants =
		new ArrayList<Participant>();
	
	private ResultSet selectedSearch;
	
	private Project project;
	
	/**
	 * Constructor
	 */
	public RecordFilterPanel(Project project, Session t) {
		this.project = project;
		if(t != null)
			setSession(t);
	}	
	
	public RecordFilterPanel(Project project) {
		this.project = project;
	}

	public void setSession(Session session) {
		this.t = session;
		init();
	}
	
	private void init() {
		FormLayout layout = new FormLayout(
				"0px, fill:pref:grow",
				"pref, 1dlu, pref, pref, 1dlu, pref, pref, 1dlu, pref, pref");
		CellConstraints cc = new CellConstraints();
		setLayout(layout);
		
		radioGrp = new ButtonGroup();
		
		ButtonAction bAct = new ButtonAction();
		
		allBtn = new JRadioButton("All records");
		allBtn.setSelected(true);
		allBtn.addActionListener(bAct);
		radioGrp.add(allBtn);
		
		rangeBtn = new JRadioButton("Specific records");
		rangeBtn.addActionListener(bAct);
		radioGrp.add(rangeBtn);
		
		speakerBtn = new JRadioButton("Records for participant(s)");
		speakerBtn.addActionListener(bAct);
		radioGrp.add(speakerBtn);
		
		searchBtn = new JRadioButton("Records from search results");
		searchBtn.addActionListener(bAct);
		radioGrp.add(searchBtn);
		
		rangeField = new JTextField();
		rangeField.setText("1.." + t.getRecordCount());
		rangeField.setInputVerifier(new RangeVerifier());
		rangeField.setEnabled(false);
		
		speakerTbl = new JXTable(new ParticipantsTableModel());
		speakerTbl.setVisibleRowCount(2);
		speakerTbl.setEnabled(false);
		
		searchTbl = new JXTable(new SearchTableModel());
		searchTbl.setVisibleRowCount(4);
		searchTbl.setEnabled(false);
		searchTbl.getColumn(1).setCellRenderer(new DateCellRenderer());
		searchTbl.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		searchTbl.getSelectionModel().addListSelectionListener(new SearchListener());
		if(searchTbl.getModel().getRowCount() > 0) {
			searchTbl.getSelectionModel().setSelectionInterval(0, 0);
		}
		searchTbl.setSortOrder(1, SortOrder.DESCENDING);
		
		// add components
		add(allBtn, cc.xyw(1, 1, 2));
		add(rangeBtn, cc.xyw(1, 3, 2));
		add(rangeField, cc.xy(2, 4));
		add(speakerBtn, cc.xyw(1, 6, 2));
		add(new JScrollPane(speakerTbl), cc.xy(2, 7));
		add(searchBtn, cc.xyw(1, 9, 2));
		add(new JScrollPane(searchTbl), cc.xy(2, 10));
	}
	
	
	/**
	 * Table model for speakers
	 */
	private class ParticipantsTableModel extends AbstractTableModel {
		
		public ParticipantsTableModel() {
			super();
			
		}

		@Override
		public int getColumnCount() {
			return 3;
		}

		@Override
		public int getRowCount() {
			return t.getParticipantCount();
		}
		
		@Override
		public String getColumnName(int col) {
			String retVal = "";
			
			if(col == 0) retVal = " ";
			else if(col == 1) retVal = "Name";
			else if(col == 2) retVal = "Birthday";
			
			return retVal;
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			Object retVal = new String();
			Participant p = getSession().getParticipant(rowIndex);
			if(columnIndex == 0) {
				if(selectedParticipants.contains(p)) {
					retVal = Boolean.TRUE;
				} else {
					retVal = Boolean.FALSE;
				}
			} else if(columnIndex == 1) {
				if(p.getName() == null) return p.getId();
				retVal = p.getName();
			} else if(columnIndex == 2) {
				if(p.getBirthDate() != null) {
					final DateTime bDay = p.getBirthDate();
					retVal = DateFormatter.dateTimeToString(bDay);
				}
			} 
			return retVal;
		}

		@Override
		public Class<?> getColumnClass(int col) {
			Class<?> retVal = null;
			
			if(col == 0) retVal = Boolean.class;
			else if(col == 1) retVal = String.class;
			else if(col == 2) retVal = String.class;
			
			return retVal;
		}
		
		@Override
		public boolean isCellEditable(int row, int col) {
			boolean retVal = false;
			
			if(col == 0) retVal = true;
			
			return retVal;
		}

		@Override
		public void setValueAt(Object value, int rowIndex, int columnIndex) {
			Participant p = getSession().getParticipant(rowIndex);
			
			Boolean selected = (Boolean)value;
			if(selected) {
				if(!selectedParticipants.contains(p))
					selectedParticipants.add(p);
			} else {
				selectedParticipants.remove(p);
			}
		}
		
		
	}

	/**
	 * Table model for searches
	 */
	private class SearchTableModel extends AbstractTableModel {
		
		List<FilterPanelCellValue> searches;
		
		public SearchTableModel() {
			searches = new ArrayList<RecordFilterPanel.FilterPanelCellValue>();
			final ResultSetManager rsManager = QueryManager.getSharedInstance().createResultSetManager();
			for(Query q:rsManager.getQueries(project)) {
				for(ResultSet rs:rsManager.getResultSetsForQuery(project, q)) {
					if(rs.getCorpus().equals(t.getCorpus()) && rs.getSession().equals(getSession().getName())) {
						searches.add(new FilterPanelCellValue(q, rs));
					}
				}
			}
		}

		@Override
		public int getColumnCount() {
			return 3;
		}

		@Override
		public int getRowCount() {
			return searches.size();
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			Object retVal = "";
			
			final FilterPanelCellValue rowValue = searches.get(rowIndex);
			if(columnIndex == 0) {
				retVal = rowValue.getObj1().getName();
			} else if (columnIndex == 1) {
				retVal = rowValue.getQuery().getDate();
			} else if (columnIndex == 2) {
				retVal = rowValue.getResultSet().size();
			}
			
			return retVal;
		}
		
		@Override
		public String getColumnName(int col) {
			String retVal = "";
			
			if(col == 0) retVal = "Query";
			else if(col == 1) retVal = "Date";
			else if(col == 2) retVal = "# of Results";
			
			return retVal;
		}
		
		@Override
		public Class<?> getColumnClass(int col) {
			Class<?> retVal = String.class;
			
			if(col == 0) retVal = String.class;
			else if(col == 1) retVal = DateTime.class;
			else if(col == 2) retVal = Integer.class;
			
			return retVal;
		}
		
	}
	
	/**
	 * Table cell class
	 */
	private class FilterPanelCellValue extends Tuple<Query, ResultSet> {
		
		public FilterPanelCellValue(Query q, ResultSet rs) {
			super(q, rs);
		}
		
		public Query getQuery() { return this.getObj1(); }
		
		public ResultSet getResultSet() { return this.getObj2(); }
		
	}
	
	/**
	 * Action for enabling/disabling components
	 */
	private class ButtonAction implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			if(allBtn.isSelected()) {
				rangeField.setEnabled(false);
				speakerTbl.setEnabled(false);
				searchTbl.setEnabled(false);
			} else if (rangeBtn.isSelected()) {
				rangeField.setEnabled(true);
				speakerTbl.setEnabled(false);
				searchTbl.setEnabled(false);
			} else if(speakerBtn.isSelected()) {
				rangeField.setEnabled(false);
				speakerTbl.setEnabled(true);
				searchTbl.setEnabled(false);
			} else if(searchBtn.isSelected()) {
				rangeField.setEnabled(false);
				speakerTbl.setEnabled(false);
				searchTbl.setEnabled(true);
			}
		}
		
	}
	
	/**
	 * Range field validator
	 */
	private class RangeVerifier extends AbstractVerifier implements VerifierListener {
		
		/** Error message */
		private String err = "";
		
		/** Range regex */
		private String rangeRegex = "([0-9]+)(?:\\.\\.([0-9]+))?";
		
		public RangeVerifier() {
			this.addVerificationListener(this);
		}

		@Override
		public boolean verification(JComponent c) {
			boolean retVal = true;
			
			Pattern p = Pattern.compile(rangeRegex);
			if(c == rangeField) {
				
				// don't validate if we are not enabled
				if(!rangeField.isEnabled()) return true;
				
				String rangeString = rangeField.getText();
				String[] ranges = rangeString.split(",");
				
				for(String range:ranges) {
					range = StringUtils.strip(range);
					Matcher m = p.matcher(range);
					
					if(m.matches()) {
						
						// make sure range is valid
						if(m.group(2) == null) {
							String idxStr = m.group(1);
							Integer idx = Integer.parseInt(idxStr);
							if(idx < 0 || idx > getSession().getRecordCount()) {
								err = "Record out of bounds '" + idx + "'";
								retVal = false;
								break;
							}
						} else {
							String firstStr = m.group(1);
							String secStr = m.group(2);
							
							Integer first = Integer.parseInt(firstStr);
							Integer second = Integer.parseInt(secStr);
							if(first > second) {
								err = "Invalid range  '" + range + "'";
								retVal = false;
								break;
							} else if(
									first > getSession().getRecordCount() || second > getSession().getRecordCount()) {
								err = "Range out of bounds '" + range + "'";
								retVal = false;
								break;
							}
						}
						
					} else {
						err = "Invalid range string '" + range + "'";
						retVal = false;
						break;
					}
					
				}
			} else {
				retVal = false;
			}
			
			return retVal;
		}

		@Override
		public void verificationFailed(JComponent comp) {
			final Toast toast = ToastFactory.makeToast(err);
			toast.setMessageBackground(PhonGuiConstants.PHON_ORANGE);
			toast.start(comp);
		}

		@Override
		public void verificationPassed(JComponent comp) {
			comp.setBackground(Color.white);
		}

		@Override
		public void verificationReset(JComponent comp) {
			comp.setBackground(Color.white);
		}
		
	}
	
	/**
	 * Date column formatter
	 */
	private class DateCellRenderer extends DefaultTableCellRenderer {

		
		@Override
		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus, int row,
				int column) {
			JLabel retVal = (JLabel)super.getTableCellRendererComponent(table, value, 
					isSelected, hasFocus, row, column);
			
			DateTime d = (DateTime)value;
			
			String dateStr = DateFormatter.dateTimeToString(d);
			retVal.setText(dateStr);
			
//			if(!table.isEnabled()) {
				retVal.setEnabled(table.isEnabled());
//			}
			
			return retVal;
		}
		
	}
	
	/**
	 * Search table listener
	 */
	private class SearchListener implements ListSelectionListener {

		@Override
		public void valueChanged(ListSelectionEvent e) {
			int rowIdx = searchTbl.getSelectedRow();
			if(rowIdx >= 0) {
				rowIdx = searchTbl.convertRowIndexToModel(rowIdx);
				selectedSearch = ((SearchTableModel)searchTbl.getModel()).searches.get(rowIdx).getResultSet();
			}
		}
		
	}
	
	/**
	 * Get the defined filter
	 */
	public RecordFilter getRecordFilter() {
		RecordFilter retVal = null;
		
		if(allBtn.isSelected()) {
			retVal = new AbstractRecordFilter() {

				@Override
				public boolean checkRecord(Record utt) {
					return true;
				}
				
			};
		} else if(rangeBtn.isSelected()) {
			try {
				retVal = new RangeRecordFilter(t, rangeField.getText());
			} catch (ParseException e) {
				LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
			}
		} else if(speakerBtn.isSelected()) {
			retVal = new ParticipantRecordFilter(selectedParticipants);
		} else if(searchBtn.isSelected()) {
			retVal = new ResultSetRecordFilter(t, selectedSearch);
		}
		
		return retVal;
	}
	
	public Session getSession() {
		return t;
	}
	
	/**
	 * Validate panel
	 */
	public boolean validatePanel() {
		boolean retVal = false;
		
		if(allBtn.isSelected()) {
			retVal = true;
		} else if(speakerBtn.isSelected()) {
			// make sure at least one participant is selected
			if(selectedParticipants.size() == 0) {
				final Toast toast = ToastFactory.makeToast("Choose at least one participant.");
				toast.start(speakerBtn);
			} else {
				retVal = true;
			}
		} else if(rangeBtn.isSelected()) {
			// validate range field
			retVal = rangeField.getInputVerifier().verify(rangeField);
		} else if(searchBtn.isSelected()) {
			// make sure selected search has data
			if(selectedSearch.getResults().size() == 0) {
				final Toast toast = ToastFactory.makeToast("Selected search has no data.");
				toast.start(searchBtn);
			} else {
				retVal = true;
			}
		}
		
		return retVal;
	}
	
}
