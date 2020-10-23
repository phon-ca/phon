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
package ca.phon.app.session;

import java.awt.*;
import java.awt.event.*;
import java.text.*;
import java.time.*;
import java.util.*;
import java.util.List;
import java.util.regex.*;

import javax.swing.*;
import javax.swing.table.*;

import org.apache.commons.lang3.*;
import org.jdesktop.swingx.*;

import ca.phon.app.query.*;
import ca.phon.project.*;
import ca.phon.query.db.*;
import ca.phon.session.*;
import ca.phon.session.Record;
import ca.phon.session.filter.*;
import ca.phon.session.format.*;
import ca.phon.ui.*;
import ca.phon.ui.toast.*;
import ca.phon.util.*;
import ca.phon.util.icons.*;

/**
 * Displays options for identify records by range, 
 * speaker, or search results.
 */
public class RecordFilterPanel extends JPanel {
	
	private final static org.apache.logging.log4j.Logger LOGGER = org.apache.logging.log4j.LogManager.getLogger(RangeRecordFilter.class.getName());

	/** The transcript */
	private Session t;
	
	/* UI */
	private ButtonGroup radioGrp;
	private JRadioButton allBtn;
	private JRadioButton rangeBtn;
	private JRadioButton speakerBtn;
	
	private JPanel queryOptsPanel;
	private JRadioButton queryBtn;
	private OpenResultSetSelector resultSetSelector;
	
	private JTextField rangeField;
	private JXTable speakerTbl;

	private List<Participant> selectedParticipants =
		new ArrayList<Participant>();
	
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
		radioGrp = new ButtonGroup();
		
		ButtonAction bAct = new ButtonAction();
		
		allBtn = new JRadioButton("All records");
		allBtn.setSelected(true);
		allBtn.addActionListener(bAct);
		radioGrp.add(allBtn);
		
		JPanel rangePanel = new JPanel(new HorizontalLayout(5));
		rangeBtn = new JRadioButton("Record range");
		rangeBtn.addActionListener(bAct);
		radioGrp.add(rangeBtn);
		
		rangeField = new JTextField();
		rangeField.setText("1.." + t.getRecordCount());
		rangeField.setInputVerifier(new RangeVerifier());
		rangeField.setColumns(10);
		rangeField.setEnabled(false);
		
		rangePanel.add(rangeBtn);
		rangePanel.add(rangeField);
		
		JPanel speakerPanel = new JPanel(new VerticalLayout());
		speakerBtn = new JRadioButton("Records for participant(s)");
		speakerBtn.addActionListener(bAct);
		radioGrp.add(speakerBtn);
		
		speakerTbl = new JXTable(new ParticipantsTableModel());
		speakerTbl.setVisibleRowCount(3);
		speakerTbl.setEnabled(false);
		
		speakerPanel.add(speakerBtn);
		speakerPanel.add(new JScrollPane(speakerTbl));

		queryBtn = new JRadioButton("Records from query results");
		queryBtn.addActionListener(bAct);
		radioGrp.add(queryBtn);
		
		resultSetSelector = new OpenResultSetSelector(getSession());
		resultSetSelector.getResultSetTable().setVisibleRowCount(3);
		resultSetSelector.getResultSetTable().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		queryOptsPanel = new JPanel(new VerticalLayout());
		queryOptsPanel.add(queryBtn);
		queryOptsPanel.add(resultSetSelector);
		
		setLayout(new VerticalLayout(5));
		add(allBtn);
		add(rangePanel);
		add(speakerPanel);
		add(queryOptsPanel);
	}
	
	/**
	 * Table model for speakers
	 */
	private class ParticipantsTableModel extends AbstractTableModel {
		
		public ParticipantsTableModel() {
			super();
			
			for(int i = 0; i < t.getParticipantCount(); i++) {
				selectedParticipants.add(t.getParticipant(i));
			}
		}

		@Override
		public int getColumnCount() {
			return 4;
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
			else if(col == 2) retVal = "Role";
			else if(col == 3) retVal = "Age";
			
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
				retVal = p.getRole();
			} else if(columnIndex == 3) {
				if(p.getBirthDate() != null) {
					retVal = AgeFormatter.ageToString(p.getAge(getSession().getDate()));
				}
			}
			return retVal;
		}

		@Override
		public Class<?> getColumnClass(int col) {
			Class<?> retVal = null;
			
			if(col == 0) retVal = Boolean.class;
			else if(col == 1) retVal = String.class;
			else if(col == 2) retVal = ParticipantRole.class;
			else if(col == 3) retVal = String.class;
			
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
	
	public JPanel getQueryOptionsPanel() {
		return this.queryOptsPanel;
	}

	/**
	 * Table model for searches
	 */
	private class SearchTableModel extends AbstractTableModel {
		
		List<FilterPanelCellValue> searches;
		
		List<Integer> openQueryRows = new ArrayList<>();
		
		public SearchTableModel() {
			searches = new ArrayList<RecordFilterPanel.FilterPanelCellValue>();
			
			int rowIdx = 0;
			// report any currently open result sets
			for(CommonModuleFrame cmf:CommonModuleFrame.getOpenWindows()) {
				if(cmf instanceof ResultSetEditor) {
					ResultSetEditor editor = (ResultSetEditor)cmf;
					// check session
					final Session editorSession = editor.getSession();
					if(editorSession != null && editorSession.getCorpus().equals(t.getCorpus()) 
							&& editorSession.getName().equals(t.getName())) {
						// add result set to list
						searches.add(new FilterPanelCellValue(editor.getQuery(), editor.getResultSet()));
						openQueryRows.add(rowIdx);
					}
				}
			}
			
			final ResultSetManager rsManager = QueryManager.getSharedInstance().createResultSetManager();
			for(Query q:rsManager.getQueries(project)) {
				for(ResultSet rs:rsManager.getResultSetsForQuery(project, q)) {
					if(rs.getCorpus().equals(t.getCorpus()) && rs.getSession().equals(getSession().getName())) {
						searches.add(new FilterPanelCellValue(q, rs));
					}
				}
			}
		}
		
		public boolean isOpenQuery(int row) {
			return openQueryRows.contains(row);
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
			else if(col == 1) retVal = LocalDate.class;
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
				resultSetSelector.getResultSetTable().setEnabled(false);
			} else if (rangeBtn.isSelected()) {
				rangeField.setEnabled(true);
				speakerTbl.setEnabled(false);
				resultSetSelector.getResultSetTable().setEnabled(false);
			} else if(speakerBtn.isSelected()) {
				rangeField.setEnabled(false);
				speakerTbl.setEnabled(true);
				resultSetSelector.getResultSetTable().setEnabled(false);
			} else if(queryBtn.isSelected()) {
				rangeField.setEnabled(false);
				speakerTbl.setEnabled(false);
				resultSetSelector.getResultSetTable().setEnabled(true);
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
			
			LocalDateTime d = (LocalDateTime)value;
			
			String dateStr = DateFormatter.dateTimeToString(d);
			retVal.setText(dateStr);
			
//			if(!table.isEnabled()) {
				retVal.setEnabled(table.isEnabled());
//			}
			
			return retVal;
		}
		
	}
	
	
	/**
	 * Query name formatter.  Displays a star next to starred queries and will show
	 * open queries in italics
	 */
	private class QueryNameCellRenderer extends DefaultTableCellRenderer {
		
		@Override
		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus, int row,
				int column) {
			JLabel retVal = (JLabel)super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

			SearchTableModel tblModel = (SearchTableModel)table.getModel();
			FilterPanelCellValue cellVal = tblModel.searches.get(row);
			
			if(cellVal.getQuery().isStarred()) {
				final ImageIcon icon = IconManager.getInstance()
						.getIcon("misc/metal-star-on", IconSize.SMALL);
				retVal.setIcon(icon);
			} else {
				retVal.setIcon(null);
			}
			
			if(tblModel.isOpenQuery(row)) {
				retVal.setFont(retVal.getFont().deriveFont(Font.ITALIC));
			}
			
			return retVal;
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
				LOGGER.error( e.getLocalizedMessage(), e);
			}
		} else if(speakerBtn.isSelected()) {
			retVal = new ParticipantRecordFilter(selectedParticipants);
		} else if(queryBtn.isSelected() && resultSetSelector.getSelectedResultSets().size() > 0) {
			retVal = new ResultSetRecordFilter(t, resultSetSelector.getSelectedResultSets().get(0));
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
		} else if(queryBtn.isSelected()) {
			// make sure selected search has data
			if(resultSetSelector.getSelectedResultSets().size() == 0 || 
					resultSetSelector.getSelectedResultSets().get(0).numberOfResults(true) == 0) {
				final Toast toast = ToastFactory.makeToast("No results selected.");
				toast.start(queryBtn);
			} else {
				retVal = true;
			}
		}
		
		return retVal;
	}
	
}
