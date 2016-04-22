package ca.phon.app.opgraph.nodes.query;

import ca.gedge.opgraph.InputField;
import ca.gedge.opgraph.OpContext;
import ca.gedge.opgraph.OpNodeInfo;
import ca.gedge.opgraph.exceptions.ProcessingException;
import ca.phon.query.report.datasource.DefaultTableDataSource;
import ca.phon.query.report.datasource.TableDataSource;

@OpNodeInfo(
	name="Append Table",
	description="Append rows from another table",
	category="Report",
	showInLibrary=true
)
public class AppendTableNode extends TableOpNode {
	
	public InputField table1InputField = 
			new InputField("table1", "Table 1", false, true, TableDataSource.class);
	
	public InputField table2InputField =
			new InputField("table2", "Table 2", false, true, TableDataSource.class);
	
	private boolean preferTable1ColumnNames = true;
	
	public AppendTableNode() {
		super();
		
		removeField(tableInput);
		
		putField(table1InputField);
		putField(table2InputField);
	}
	
	public boolean isPreferTable1ColumnNames() {
		return this.preferTable1ColumnNames;
	}

	@Override
	public void operate(OpContext context) throws ProcessingException {
		final DefaultTableDataSource table1 = (DefaultTableDataSource)context.get(table1InputField);
		final DefaultTableDataSource table2 = (DefaultTableDataSource)context.get(table2InputField);
		
		final DefaultTableDataSource outputTable = new DefaultTableDataSource(table1);
		outputTable.append(table2);
		
		// setup column names
		int numCols = Math.max(table1.getColumnCount(), table2.getColumnCount());
		for(int col = 0; col < numCols; col++) {
			// TODO
		}
		
		context.put(tableOutput, outputTable);
	}

}
