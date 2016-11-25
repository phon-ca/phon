/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2016, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
 * Dept of Linguistics, Memorial University <https://phon.ca>
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ca.phon.app.opgraph.nodes.table;

import java.awt.Component;
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
