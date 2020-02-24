package ca.phon.app.query;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.table.AbstractTableModel;

import org.jdesktop.swingx.JXTable;

import ca.phon.query.db.QueryManager;
import ca.phon.query.db.ResultSet;
import ca.phon.query.db.ResultSetManager;
import ca.phon.session.Session;
import ca.phon.session.SessionPath;
import ca.phon.ui.CommonModuleFrame;
import ca.phon.util.Tuple;

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
		var resultSets = findResultSets();
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
	
	/**
	 * Returns a map of QueryAndReportWizard to
	 * @return
	 */
	private List<Tuple<QueryAndReportWizard, Tuple<String, ResultSet>>> findResultSets() {
		List<Tuple<QueryAndReportWizard, Tuple<String, ResultSet>>> retVal = new ArrayList<>();
		for(CommonModuleFrame cmf:CommonModuleFrame.getOpenWindows()) {
			if(cmf instanceof QueryAndReportWizard) {
				QueryAndReportWizard wizard = (QueryAndReportWizard)cmf;
				var openQueries = wizard.getQueryRunners();
				for(String queryName:openQueries.keySet()) {
					var runnerPanel = openQueries.get(queryName);
					
					final QueryManager queryManager = QueryManager.getSharedInstance();
					final ResultSetManager rsManager = queryManager.createResultSetManager();
					
					var sessionPath = new SessionPath(session.getCorpus(), session.getName());
					var rs = rsManager.getResultSetsForQuery(runnerPanel.getTempProject(), runnerPanel.getQuery())
							.stream().filter( (currentRs) -> currentRs.getSessionPath().equals(sessionPath.toString()) )
							.findAny();
					if(rs.isPresent()) {
						var tuple = new Tuple<>(wizard, new Tuple<>(queryName, rs.get()));
						retVal.add(tuple);
					}
				}
			}
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
