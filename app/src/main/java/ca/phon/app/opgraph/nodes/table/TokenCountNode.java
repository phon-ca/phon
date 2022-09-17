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
package ca.phon.app.opgraph.nodes.table;

import ca.phon.opgraph.OpContext;
import ca.phon.opgraph.app.GraphDocument;
import ca.phon.opgraph.app.extensions.NodeSettings;
import ca.phon.opgraph.exceptions.ProcessingException;
import ca.phon.query.report.datasource.TableDataSource;

import java.awt.*;
import java.util.List;
import java.util.*;

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
