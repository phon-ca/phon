package ca.phon.app.opgraph.nodes.query;


import ca.gedge.opgraph.InputField;
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
	
	public int[] getColumnIndices(TableDataSource table, String columns) {
		if(columns == null || columns.trim().length() == 0) return new int[0];
		
		String[] cols = columns.trim().split(";");
		int[] retVal = new int[cols.length];
	
		for(int i = 0; i < cols.length; i++) {
			int cIdx = -1;
			for(int j = 0; j < table.getColumnCount(); j++) {
				if(table.getColumnTitle(j).equalsIgnoreCase(cols[i])) {
					cIdx = j;
					break;
				}
			}
			if(cIdx < 0) {
				// attempt to parse as integer
				if(cols[i].matches("[0-9]+")) {
					cIdx = Integer.parseInt(cols[i]);
					if(cIdx >= table.getColumnCount())
						cIdx = -1;
				}
			}
			retVal[i] = cIdx;
		}
		
		return retVal;
	}
	
}
