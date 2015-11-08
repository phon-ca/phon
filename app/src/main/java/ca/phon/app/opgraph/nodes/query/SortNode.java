package ca.phon.app.opgraph.nodes.query;

import java.awt.Component;
import java.util.Properties;

import ca.gedge.opgraph.OpContext;
import ca.gedge.opgraph.OpNodeInfo;
import ca.gedge.opgraph.app.GraphDocument;
import ca.gedge.opgraph.app.components.NodeSettingsPanel;
import ca.gedge.opgraph.app.extensions.NodeSettings;
import ca.gedge.opgraph.exceptions.ProcessingException;
import ca.phon.query.report.datasource.TableDataSource;

@OpNodeInfo(
		name="Sort",
		description="Sort table",
		category="Report"
)
public class SortNode extends TableOpNode implements NodeSettings {
	
	private SortNodeSettingsPanel nodeSettingsPanel;
	
	public SortNode() {
		super();
		
		putExtension(SortNodeSettings.class, new SortNodeSettings());
		putExtension(NodeSettings.class, this);
	}
	
	@Override
	public void operate(OpContext context) throws ProcessingException {
		final TableDataSource table = (TableDataSource)context.get(tableInput);
		
		
	}
	
	public SortNodeSettings getSortSettings() {
		return getExtension(SortNodeSettings.class);
	}

	@Override
	public Component getComponent(GraphDocument document) {
		if(nodeSettingsPanel == null) {
			nodeSettingsPanel = new SortNodeSettingsPanel(getSortSettings());
		}
		return nodeSettingsPanel;
	}

	@Override
	public Properties getSettings() {
		return new Properties();
	}

	@Override
	public void loadSettings(Properties properties) {
		
	}

}
