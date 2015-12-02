package ca.phon.app.opgraph.nodes.query;

import java.awt.Component;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import ca.gedge.opgraph.OpContext;
import ca.gedge.opgraph.app.GraphDocument;
import ca.gedge.opgraph.app.extensions.NodeSettings;
import ca.gedge.opgraph.exceptions.ProcessingException;
import ca.phon.query.report.datasource.TableDataSource;

/**
 * Count the total number of tokens and distinct tokens
 * for each included column. Values may also be grouped
 * by a column.
 *
 */
public class TokenCountNode extends TableOpNode implements NodeSettings {
	
	private InventorySettingsPanel settingsPanel = null;
	
	public TokenCountNode() {
		super();
		
		putExtension(InventorySettings.class, new InventorySettings());
	}
	
	@Override
	public void operate(OpContext context) throws ProcessingException {
		
	}
	
	private Map<GroupKey, List<TokenInfo>> countTokens(TableDataSource table) {
		Map<GroupKey, List<TokenInfo>> retVal = new LinkedHashMap<>();
		
		
		
		return retVal;
	}
	
	public InventorySettings getInventorySettings() {
		return getExtension(InventorySettings.class);
	}
	
	@Override
	public Component getComponent(GraphDocument document) {
		if(settingsPanel == null) {
			settingsPanel = new InventorySettingsPanel(getInventorySettings());
		}
		return settingsPanel;
	}

	@Override
	public Properties getSettings() {
		final Properties props = new Properties();
		return props;
	}

	@Override
	public void loadSettings(Properties properties) {
		
	}
	
	private class GroupKey {
		
	}
	
	private class TokenInfo {
		int columnIdx;
		int totalTokens;
		Set<Object> distinctTokens = new LinkedHashSet<>();
	}

}
