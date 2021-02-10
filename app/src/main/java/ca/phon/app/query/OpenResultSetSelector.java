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
package ca.phon.app.query;

import java.awt.*;
import java.util.*;
import java.util.List;

import javax.swing.*;
import javax.swing.table.*;

import org.jdesktop.swingx.*;

import ca.phon.query.db.*;
import ca.phon.session.*;
import ca.phon.util.*;

/**
 * Display available {@link ResultSet}s for a session.
 * Open results are taken from all open {@link QueryAndReportWizard} windows.
 * 
 */
public class OpenResultSetSelector extends JPanel {
	
	private final Session session;
	
	private JXTable resultSetTable;
	
	public OpenResultSetSelector(Session session) {
		super();
		this.session = session;
		
		init();
	}
	
	private void init() {
		var resultSets = QueryAndReportWizard.findOpenResultSets(session);
		resultSetTable = new JXTable(new ResultSetTableModel(resultSets));
		resultSetTable.setVisibleRowCount(3);
		
		var scroller = new JScrollPane(resultSetTable);
		
		setLayout(new BorderLayout());
		add(scroller, BorderLayout.CENTER);
	}
	
	public JXTable getResultSetTable() {
		return this.resultSetTable;
	}
	
	public List<ResultSet> getSelectedResultSets() {
		List<ResultSet> retVal = new ArrayList<>();
		ResultSetTableModel tableModel = (ResultSetTableModel)resultSetTable.getModel();
		for(int selectedRow:resultSetTable.getSelectedRows()) {
			retVal.add(tableModel.resultSets.get(selectedRow).getObj2().getObj2());
		}
		return retVal;
	}
	
	public class ResultSetTableModel extends AbstractTableModel {
		
		List<Tuple<QueryAndReportWizard, Tuple<String, ResultSet>>> resultSets;
		
		public ResultSetTableModel(List<Tuple<QueryAndReportWizard, Tuple<String, ResultSet>>> resultSets) {
			super();
			this.resultSets = resultSets;
		}
		
		@Override
		public int getRowCount() {
			return this.resultSets.size();
		}

		@Override
		public int getColumnCount() {
			return 3;
		}
		
		@Override
		public String getColumnName(int col) {
			switch(col) {
			case 0:
				return "Window";
				
			case 1:
				return "Query";
				
			case 2:
				return "# of results";
				
			default:
				return super.getColumnName(col);
			}
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			var tuple = resultSets.get(rowIndex);
			switch(columnIndex) {
			case 0:
				return tuple.getObj1().getTitle();
				
			case 1:
				return tuple.getObj2().getObj1();
				
			case 2:
				return tuple.getObj2().getObj2().numberOfResults(false);
				
			default:
				return "";
			}
		}
		
	}
	
}
