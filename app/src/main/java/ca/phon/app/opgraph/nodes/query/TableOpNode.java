package ca.phon.app.opgraph.nodes.query;


import java.util.ArrayList;
import java.util.List;

import ca.gedge.opgraph.InputField;
import ca.gedge.opgraph.OpContext;
import ca.gedge.opgraph.OpNode;
import ca.gedge.opgraph.OutputField;
import ca.phon.query.report.datasource.TableDataSource;

public abstract class TableOpNode extends OpNode {
	
	protected InputField tableInput = new InputField("table", "Input table", false,
			true, TableDataSource.class);
	
	protected OutputField tableOutput = new OutputField("table", "Output table",
			true, TableDataSource.class);
	
	public TableOpNode() {
		super();
		
		putField(tableInput);
		putField(tableOutput);
	}
	
	public TableDataSource getInputTable(OpContext context) {
		return (TableDataSource)context.get(tableInput);
	}
	
	public void setTableOutput(OpContext context, TableDataSource tbl) {
		context.put(tableOutput, tbl);
	}
	
	public int getColumnIndex(TableDataSource table, String column) {
		column = column.trim();
		int cIdx = -1;
		for(int j = 0; j < table.getColumnCount(); j++) {
			if(table.getColumnTitle(j).equalsIgnoreCase(column)) {
				cIdx = j;
				break;
			}
		}
		if(cIdx < 0) {
			// attempt to parse as integer
			if(column.matches("[0-9]+")) {
				cIdx = Integer.parseInt(column);
				if(cIdx >= table.getColumnCount())
					cIdx = -1;
			}
		}
		return cIdx;
	}
	
	public int[] getColumnIndices(TableDataSource table, List<String> columns) {
		List<Integer> list = new ArrayList<>();
	
		for(int i = 0; i < columns.size(); i++) {
			int colIdx = getColumnIndex(table, columns.get(i));
			if(colIdx >= 0 && colIdx < table.getColumnCount())
				list.add(colIdx);
		}
		
		int retVal[] = new int[list.size()];
		for(int i = 0 ; i < list.size(); i++) retVal[i] = list.get(i);
		return retVal;
	}
	
}
