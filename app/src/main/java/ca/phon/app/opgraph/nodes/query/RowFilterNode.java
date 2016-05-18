package ca.phon.app.opgraph.nodes.query;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.mozilla.javascript.Scriptable;

import ca.gedge.opgraph.OpContext;
import ca.gedge.opgraph.OpNodeInfo;
import ca.gedge.opgraph.exceptions.ProcessingException;
import ca.phon.query.report.datasource.DefaultTableDataSource;
import ca.phon.script.PhonScript;
import ca.phon.script.PhonScriptContext;
import ca.phon.script.PhonScriptException;

@OpNodeInfo(
		name="Filter Rows",
		category="Report",
		description="Filter rows in table",
		showInLibrary=true
)
public class RowFilterNode extends TableScriptNode {
	
	private final static Logger LOGGER = Logger.getLogger(RowFilterNode.class.getName());

	private final static String DEFAULT_SCRIPT = "function filterRow(table, row) {\n\treturn true;\n}\n";
	
	public RowFilterNode() {
		super(DEFAULT_SCRIPT);
	}
	
	@Override
	public void operate(OpContext context) throws ProcessingException {
		final PhonScript phonScript = getScript();
		final PhonScriptContext scriptContext = phonScript.getContext();
		
		final DefaultTableDataSource table = (DefaultTableDataSource)super.getInputTable(context);
		
		final DefaultTableDataSource outputTable = new DefaultTableDataSource();
		
		try {
			final Scriptable scope = scriptContext.getEvaluatedScope();
			scriptContext.installParams(scope);
			
			for(int row = 0; row < table.getRowCount(); row++) {
				checkCanceled();
				
				Object isFilterRow = scriptContext.callFunction(scope, "filterRow", 
						table, row);
				
				boolean filterRow = (isFilterRow == null ? true : 
					Boolean.valueOf(isFilterRow.toString()));
				if(filterRow) {
					// add row to output table
					outputTable.addRow(table.getRow(row));
				}
			}
		} catch (PhonScriptException e) {
			LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}
		
		if(outputTable.getRowCount() > 0) {
			for(int i = 0; i < table.getColumnCount(); i++) {
				outputTable.setColumnTitle(i, table.getColumnTitle(i));
			}
		}
		
		context.put(tableOutput, outputTable);
	}
	
	

}
