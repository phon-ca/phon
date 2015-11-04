package ca.phon.app.opgraph.nodes.query;

import ca.gedge.opgraph.OpContext;
import ca.gedge.opgraph.OpNodeInfo;
import ca.gedge.opgraph.exceptions.ProcessingException;
import ca.phon.query.report.datasource.TableDataSource;

@OpNodeInfo(
		name="Sort",
		description="Sort table",
		category="Report"
)
public class SortNode extends TableOpNode {
	
	public SortNode() {
		super();
		
		putExtension(SortNodeSettings.class, new SortNodeSettings());
	}
	
	@Override
	public void operate(OpContext context) throws ProcessingException {
		final TableDataSource table = (TableDataSource)context.get(tableInput);
		
		
	}

}
