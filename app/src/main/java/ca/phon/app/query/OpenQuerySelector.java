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

import ca.phon.project.*;
import ca.phon.query.db.*;
import ca.phon.ui.*;

/**
 * Display a list of all available queries from any open
 * {@link QueryAndReportWizard} windows.
 *
 */
public class OpenQuerySelector extends JPanel {

	private final Project filterProject;
	
	private JXTable queryTable;
	
	public OpenQuerySelector() {
		this(null);
	}
	
	public OpenQuerySelector(Project project) {
		super();
		
		this.filterProject = project;
		init();
	}
	
	private void init() {
		var queries = findOpenQueries();
		queryTable = new JXTable(new QueryTableModel(queries));
		queryTable.setVisibleRowCount(5);
		
		var scroller = new JScrollPane(queryTable);
		
		setLayout(new BorderLayout());
		add(scroller, BorderLayout.CENTER);
	}
	
	public List<QueryInfo> getSelectedQueries() {
		List<QueryInfo> retVal = new ArrayList<>();
		QueryTableModel tableModel = (QueryTableModel)queryTable.getModel();
		for(int selectedRow:queryTable.getSelectedRows()) {
			retVal.add(tableModel.queries.get(selectedRow));
		}
		return retVal;
	}
	
	private List<QueryInfo> findOpenQueries() {
		List<QueryInfo> retVal = new ArrayList<>();
		
		for(CommonModuleFrame cmf:CommonModuleFrame.getOpenWindows()) {
			if(cmf instanceof QueryAndReportWizard) {
				QueryAndReportWizard wizard = (QueryAndReportWizard)cmf;
				
				Project wizardProj = wizard.getExtension(Project.class);
				boolean includeProject = (this.filterProject == null);
				if(this.filterProject != null) {
					includeProject = this.filterProject == wizardProj;
				}
				if(includeProject) {
					var openQueries = wizard.getQueryRunners();
					for(String queryName:openQueries.keySet()) {
						QueryRunnerPanel qr = openQueries.get(queryName);
						
						QueryInfo info = new QueryInfo();
						info.project = wizardProj;
						info.wizard = wizard;
						info.query = qr.getQuery();
						info.queryName = queryName;
						info.tempProject = qr.getTempProject();
						retVal.add(info);
					}
				}
			}
		}
		
		return retVal;
	}
	
	public class QueryInfo {
		Project project;
		QueryAndReportWizard wizard;
		Query query;
		String queryName;
		Project tempProject;
		public Project getProject() {
			return project;
		}
		public QueryAndReportWizard getWizard() {
			return wizard;
		}
		public Query getQuery() {
			return query;
		}
		public String getQueryName() {
			return queryName;
		}
		public Project getTempProject() {
			return tempProject;
		}
	}
	
	public class QueryTableModel extends AbstractTableModel {
		
		List<QueryInfo> queries;
		
		public QueryTableModel(List<QueryInfo> queries) {
			super();
			this.queries = queries;
		}

		@Override
		public int getRowCount() {
			return queries.size();
		}

		@Override
		public int getColumnCount() {
			return 3;
		}
		
		@Override
		public String getColumnName(int col) {
			switch(col) {
			case 0:
				return "Project";
				
			case 1:
				return "Window";
				
			case 2:
				return "Query";
				
			default:
				return super.getColumnName(col);
			}
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			QueryInfo info = queries.get(rowIndex);
			
			switch(columnIndex) {
			case 0:
				return info.project.getName();
				
			case 1:
				return info.wizard.getTitle();
				
			case 2:
				return info.queryName;
				
			default:
				return "";
			}
		}
		
	}
	
}
