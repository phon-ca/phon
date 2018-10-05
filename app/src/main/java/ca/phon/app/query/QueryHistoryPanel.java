/*
 * Copyright (C) 2012-2018 Gregory Hedlund & Yvan Rose
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
package ca.phon.app.query;

import java.awt.BorderLayout;
import java.awt.Toolkit;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.jdesktop.swingx.HorizontalLayout;

import ca.phon.app.log.LogUtil;
import ca.phon.app.session.editor.SegmentedButtonBuilder;
import ca.phon.query.history.QueryHistoryManager;
import ca.phon.query.script.QueryName;
import ca.phon.query.script.QueryScript;
import ca.phon.script.PhonScriptException;
import ca.phon.script.params.ScriptParameters;
import ca.phon.script.params.history.ObjectFactory;
import ca.phon.script.params.history.ParamSetType;
import ca.phon.script.params.history.ParamType;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;

/**
 * Utility panel for controlling query history for a {@link QueryScript}.
 * This component is intended to be placed alongside a {@link ScriptPanel}
 * instance.
 * 
 */
public class QueryHistoryPanel extends JPanel {
	
	private final static String STOCK_QUERY_PREFIX = "param_set_";
	
	private JLabel label;

	private JButton firstButton;
	
	private JButton lastButton;
	
	private JButton nextButton;
	
	private JButton prevButton;
	
	private DefaultComboBoxModel<String> queryNameModel;
	private JComboBox<String> queryNameBox;
	
	private int currentIndex = -1;
	
	private final QueryHistoryManager queryHistoryManager;
	
	private QueryHistoryManager stockQueryManager;
	
	private final WeakReference<ScriptPanel> scriptPanelRef;
	
	public QueryHistoryPanel(QueryHistoryManager queryHistoryManager, ScriptPanel scriptPanel) {
		super();
		
		this.queryHistoryManager = queryHistoryManager;
		this.scriptPanelRef = new WeakReference<ScriptPanel>(scriptPanel);
		
		loadStockQueries();
		init();
	}
	
	private void loadStockQueries() {
		final QueryName qn = getScriptPanel().getScript().getExtension(QueryName.class);
		
		if(qn != null) {
			final InputStream stockQueryInputStream = getClass().getResourceAsStream(STOCK_QUERY_PREFIX + qn.getName() + ".xml");
			if(stockQueryInputStream != null) {
				try {
					stockQueryManager = new QueryHistoryManager(stockQueryInputStream);
				} catch (IOException e) {
					LogUtil.severe(e);
				}
			}
		}
		if(stockQueryManager == null) {
			final ObjectFactory factory = new ObjectFactory();
			stockQueryManager = new QueryHistoryManager(factory.createParamHistoryType());
		}
	}
	
	public int getCurrentIndex() {
		return currentIndex;
	}
	
	public QueryHistoryManager getQueryHistoryManager() {
		return this.queryHistoryManager;
	}
	
	public ScriptPanel getScriptPanel() {
		return scriptPanelRef.get();
	}
	
	public QueryHistoryManager getStockQueries() {
		return this.stockQueryManager;
	}
	
	private void init() {
		final PhonUIAction historyPrevAct = new PhonUIAction(this, "goPrevious");
		historyPrevAct.putValue(PhonUIAction.SMALL_ICON, IconManager.getInstance().getIcon("actions/go-previous", IconSize.SMALL));
		historyPrevAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "View previous entry in query history");
		
		final PhonUIAction historyNextAct = new PhonUIAction(this, "goNext");
		historyNextAct.putValue(PhonUIAction.SMALL_ICON, IconManager.getInstance().getIcon("actions/go-next", IconSize.SMALL));
		historyNextAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "View next entry in query history");
		
		final PhonUIAction goFirstAct = new PhonUIAction(this, "gotoFirst");
		goFirstAct.putValue(PhonUIAction.SMALL_ICON, IconManager.getInstance().getIcon("actions/go-first", IconSize.SMALL));
		goFirstAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "View oldest entry in query history");
		
		final PhonUIAction goLastAct = new PhonUIAction(this, "gotoLast");
		goLastAct.putValue(PhonUIAction.SMALL_ICON, IconManager.getInstance().getIcon("actions/go-last", IconSize.SMALL));
		goLastAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "View most recent entry in query history");
		
		SegmentedButtonBuilder<JButton> segButtonBuilder = new SegmentedButtonBuilder<>(JButton::new);
		ButtonGroup bg = new ButtonGroup();
		List<JButton> buttons = segButtonBuilder.createSegmentedRoundRectButtons(4, bg);
		
		label = new JLabel();
		label.setIcon(IconManager.getInstance().getIcon("misc/history-clock-button-black", IconSize.SMALL));
		label.setToolTipText("Query History");
		
		queryNameModel = new DefaultComboBoxModel<>();
		queryNameBox = new JComboBox<>(queryNameModel);
		queryNameBox.setToolTipText("Select query by name");
		queryNameBox.setVisible(queryHistoryManager.getNamedParamSets().size() > 0);
		update();
		
		firstButton = buttons.get(0);
		firstButton.setAction(goFirstAct);
		
		prevButton = buttons.get(1);
		prevButton.setAction(historyPrevAct);
	
		nextButton = buttons.get(2);
		nextButton.setAction(historyNextAct);
		
		lastButton = buttons.get(3);
		lastButton.setAction(goLastAct);
		
		JPanel buttonPanel = new JPanel(new HorizontalLayout(0));
		buttonPanel.add(firstButton);
		buttonPanel.add(prevButton);
		buttonPanel.add(nextButton);
		buttonPanel.add(lastButton);
		
		JPanel leftPanel = new JPanel(new HorizontalLayout(5));
		leftPanel.add(label);
		leftPanel.add(buttonPanel);

		JPanel namePanel = new JPanel(new BorderLayout());
		namePanel.add(queryNameBox, BorderLayout.CENTER);
		
		setLayout(new BorderLayout());
		add(leftPanel, BorderLayout.WEST);
		add(namePanel, BorderLayout.CENTER);
	}
	
	public ParamSetType currentQuery() {
		if(currentIndex < 0 || currentIndex >= queryHistoryManager.size()) return null;
		return queryHistoryManager.getParamSet(currentIndex);
	}
	
	public void gotoFirst() {
		gotoIndex(0);
	}
	
	public void gotoLast() {
		gotoIndex(queryHistoryManager.size()-1);
	}
	
	public void goPrevious() {
		if(queryHistoryManager.size() == 0) return;
		gotoIndex(currentIndex > 0 ? currentIndex - 1 : 0);
	}
	
	public void goNext() {
		if(queryHistoryManager.size() == 0) return;
		gotoIndex(currentIndex < queryHistoryManager.size()-1 ? currentIndex + 1 : queryHistoryManager.size()-1);
	}

	public void gotoIndex(int index) {
		if(index < 0 || index >= getQueryHistoryManager().size()) return;

		currentIndex = index;
		loadFromParamSet(queryHistoryManager.getParamSet(currentIndex));
		update();
	}
	
	public void gotoHash(String hash) {
		gotoIndex(queryHistoryManager.indexOf(hash));
	}
	
	public void updateLabelFromCurrentHash() {
		try {
			final ScriptParameters scriptParams = 
					getScriptPanel().getScript().getContext().getScriptParameters(getScriptPanel().getScript().getContext().getEvaluatedScope());
			updateLabelFromHash(scriptParams.getHashString());
		} catch (PhonScriptException e) {
			LogUtil.severe(e);
		}
	}
	
	public void updateLabelFromHash(String hash) {
		currentIndex = queryHistoryManager.indexOf(hash);
		update();
	}
	
	/**
	 * Returns the name of the current query.
	 * 
	 * @return query name or <code>null</code> if not set
	 */
	public String getQueryName() {
		String retVal = null;
		if(currentIndex >= 0 && currentIndex < queryHistoryManager.size()) {
			final ParamSetType paramSet = queryHistoryManager.getParamSet(currentIndex);
			retVal = paramSet.getName();
		}
		if(retVal == null) {
			try {
				final ScriptParameters scriptParams = 
						getScriptPanel().getScript().getContext().getScriptParameters(getScriptPanel().getScript().getContext().getEvaluatedScope());
				final String currentHash = scriptParams.getHashString();
				final ParamSetType stockSet = stockQueryManager.getParamSet(currentHash);
				if(stockSet != null) {
					retVal = stockSet.getName();
				}
			} catch (PhonScriptException e) {
				LogUtil.warning(e.getLocalizedMessage(), e);
			}
		}
		return retVal;
	}
	
	private void updateComboBox() {
		queryNameBox.removeItemListener(itemListener);
		List<String> queryNames = 
				queryHistoryManager.getNamedParamSets().stream()
					.map( (ps) -> ps.getName() )
					.collect( Collectors.toList() );
		List<String> stockQueryNames = 
				stockQueryManager.getNamedParamSets().stream()
					.map( (ps) -> ps.getName() )
					.collect( Collectors.toList() );
		queryNames.addAll(stockQueryNames);
		Collections.sort(queryNames);
		queryNameModel.removeAllElements();
		queryNames.forEach( (qn) -> queryNameModel.addElement(qn) );
		queryNameBox.setVisible(queryNames.size() > 0);
		queryNameBox.setSelectedItem(getQueryName());
		queryNameBox.addItemListener(itemListener);
	}
	
	private void update() {
		if(queryHistoryManager.size() > 0) {
			label.setText(String.format("%2d/%2d", (currentIndex+1), queryHistoryManager.size()));
		} else {
			label.setText(String.format("%2d/%2d", 0, 0));
			queryNameBox.setSelectedItem(null);
		}
		updateComboBox();
	}
	
	public void loadFromParamSet(ParamSetType queryInfo) {
		final QueryScript queryScript = (QueryScript)getScriptPanel().getScript();
		queryScript.resetContext();
		
		final Map<String, Object> paramMap = new LinkedHashMap<>();
		for(ParamType paramType:queryInfo.getParam()) {
			paramMap.put(paramType.getId(), paramType.getValue());
		}
		
		try {
			ScriptParameters scriptParams = queryScript.getContext().getScriptParameters(
					queryScript.getContext().getEvaluatedScope());
			scriptParams.loadFromMap(paramMap);
		} catch (PhonScriptException e) {
			LogUtil.severe(e);
			Toolkit.getDefaultToolkit().beep();
		}
		
		getScriptPanel().setScript(queryScript);
	}
	
	private final ItemListener itemListener = new ItemListener() {
		
		@Override
		public void itemStateChanged(ItemEvent e) {
			if(e.getStateChange() == ItemEvent.SELECTED) {
				final String selectedItem = (String)queryNameBox.getSelectedItem();
				if(selectedItem != null) {
					final ParamSetType paramSet = queryHistoryManager.getParamSetByName(selectedItem);
					if(paramSet != null)
						gotoHash(paramSet.getHash());
					else {
						final ParamSetType stockSet = stockQueryManager.getParamSetByName(selectedItem);
						if(stockSet != null) {
							loadFromParamSet(stockSet);
							updateLabelFromCurrentHash();
						}
					}
				}
			}
		}
		
	};	
	
}
