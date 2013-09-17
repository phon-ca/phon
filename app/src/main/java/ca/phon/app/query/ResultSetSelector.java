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
package ca.phon.app.query;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.RowFilter;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableRowSorter;

import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.decorator.HighlighterFactory;

import ca.phon.project.Project;
import ca.phon.query.db.Query;
import ca.phon.query.db.QueryManager;
import ca.phon.query.db.ResultSet;
import ca.phon.query.db.ResultSetManager;


/**
 * Displays a list of searches in a checkbox tree with 
 * the query id being the root.
 *
 */
public class ResultSetSelector extends JPanel {
	
	/** The Query */
	private Query query;
	
	/* UI */
	/** Checkbox tree */
	private JXTable table;
	private ResultSetTableModel model;
	private TableRowSorter<ResultSetTableModel> tableSorter;

	private JCheckBox selectAllButton;
	
	private JCheckBox hideEmptyBox;
	
	private Project project;
	
	/** Constructor */
	public ResultSetSelector(Project project, Query q) {
		super();
		
		this.query = q;
		this.project = project;
		
		init();
	}
	
	private void init() {
		this.setLayout(new BorderLayout());
		
		model = new ResultSetTableModel(project, query);
		tableSorter = new TableRowSorter<ResultSetTableModel>(model);
		model.addTableModelListener(new SearchTableModelListener());
		table = new JXTable(model);
		table.addHighlighter(HighlighterFactory.createSimpleStriping());
		table.setRowSorter(tableSorter);
//		table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		table.setColumnControlVisible(true);
		table.getColumn(0).setPreferredWidth(20);
		table.getColumn(2).setPreferredWidth(20);
//		table.getColumnModel().removeColumn(table.getColumn(1));
//		table.packAll();
		
		JScrollPane pane = new JScrollPane(table);
		add(pane, BorderLayout.CENTER);
		
		selectAllButton = new JCheckBox("Select all");
		selectAllButton.setSelected(true);
		selectAllButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				Boolean selected = selectAllButton.isSelected();
				if(selected) {
					selectAllButton.setText("Deselect all");
				} else {
					selectAllButton.setText("Select all");
				}
				model.selectAll(selected);
			}
			
		});
		
		hideEmptyBox = new JCheckBox("Hide empty result sets");
		hideEmptyBox.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				if(hideEmptyBox.isSelected()) {
					final RowFilter<ResultSetTableModel, Integer>
						filter = RowFilter.regexFilter("[1-9][0-9]*", ResultSetTableModel.Columns.ResultCount.ordinal());
					tableSorter.setRowFilter(filter);
				} else {
					tableSorter.setRowFilter(null);
				}
			}
			
		});
		
		final JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEADING));
		topPanel.add(selectAllButton);
		topPanel.add(hideEmptyBox);
		
		add(topPanel, BorderLayout.NORTH);
	}
	
	/**
	 * Select all searches with number of results > 0.
	 */
	public void selectSearchesWithResults() {
		final QueryManager qManager = QueryManager.getSharedInstance();
		final ResultSetManager rManager = qManager.createResultSetManager();
		for(ResultSet s:rManager.getResultSetsForQuery(project, query)) {
			if(s.size() > 0) {
				model.setSelected(s, true);
			}
		}
	}
	
	public void selectAll() {
		final QueryManager qManager = QueryManager.getSharedInstance();
		final ResultSetManager rManager = qManager.createResultSetManager();
		for(ResultSet s:rManager.getResultSetsForQuery(project, query)) {
			model.setSelected(s, true);
		}
		selectAllButton.setSelected(true);
		selectAllButton.setText("Deselect all");
//		model.selectAll(true);
	}
	
	public ResultSet[] getSelectedSearches() {
		final ResultSet[] selected = model.getSelectedSearches();
		final List<ResultSet> retVal = new ArrayList<ResultSet>();
		retVal.addAll(Arrays.asList(selected));
		
		if(hideEmptyBox.isSelected()) {
			for(ResultSet rs:selected) {
				if(rs.getResults().size() == 0) {
					retVal.remove(rs);
				}
			}
		}
		return retVal.toArray(new ResultSet[0]);
	}
	
	public void setSelected(ResultSet s, boolean v) {
		model.setSelected(s, v);
	}
	
	private class SearchTableModelListener implements TableModelListener {

		@Override
		public void tableChanged(TableModelEvent arg0) {
			ResultSetSelector.super.firePropertyChange("_SELECTION_", true, false);
		}
		
	}
}
