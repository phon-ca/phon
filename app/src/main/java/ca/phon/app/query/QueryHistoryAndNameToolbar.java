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

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.io.*;
import java.lang.ref.*;
import java.util.*;
import java.util.List;
import java.util.stream.*;

import javax.swing.*;
import javax.swing.event.*;

import ca.phon.app.log.*;
import ca.phon.app.query.actions.*;
import ca.phon.project.*;
import ca.phon.query.history.*;
import ca.phon.query.script.*;
import ca.phon.script.*;
import ca.phon.script.params.*;
import ca.phon.script.params.history.*;
import ca.phon.ui.*;
import ca.phon.ui.action.*;
import ca.phon.ui.menu.*;
import ca.phon.util.icons.*;

/**
 * Utility panel for controlling query history for a {@link QueryScript}.
 * This component is intended to be placed alongside a {@link ScriptPanel}
 * instance.
 * 
 */
public class QueryHistoryAndNameToolbar extends JToolBar {
	
	private final static String STOCK_QUERY_PREFIX = "param_set_";
	
//	private JLabel label;

//	private JButton firstButton;
//	
//	private JButton lastButton;
//	
//	private JButton nextButton;
//	
//	private JButton prevButton;
	
	private JButton clearButton;
	
	private DropDownButton historyButton;
	
	private DropDownButton saveButton;
	
	private DefaultComboBoxModel<String> queryNameModel;
	private JComboBox<String> queryNameBox;
	
	private int currentIndex = -1;
	
	private final QueryHistoryManager queryHistoryManager;
	
	private QueryHistoryManager stockQueryManager;
	
	private final WeakReference<ScriptPanel> scriptPanelRef;
	
	public QueryHistoryAndNameToolbar(QueryHistoryManager queryHistoryManager, ScriptPanel scriptPanel) {
		super();
		
		this.queryHistoryManager = queryHistoryManager;
		this.scriptPanelRef = new WeakReference<ScriptPanel>(scriptPanel);
		
		
		loadStockQueries();
		init();
		
		setFloatable(false);
		
		this.queryHistoryManager.addParamHistoryListener( (e) -> updateLabelFromCurrentHash() );
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
	
	private final PropertyChangeListener paramListener = new PropertyChangeListener() {
		
		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			updateLabelFromCurrentHash();
		}
		
	};
	
	private void addParamListeners() {
		ScriptParameters scriptParams = getScriptPanel().getScriptParameters();
		if(scriptParams == null) return;
		
		for(ScriptParam param:scriptParams) {
			param.addPropertyChangeListener(paramListener);
		}
	}
	
	private void init() {
		PhonUIAction clearAct = new PhonUIAction(this, "onClear");
		clearAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Clear query settings (reset to default)");
		clearAct.putValue(PhonUIAction.SMALL_ICON, IconManager.getInstance().getIcon("actions/draw-eraser", IconSize.SMALL));
		clearButton = new JButton(clearAct);
		
		// query history menu
		final Action showHistoryAct = new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {}
		};
		ImageIcon historyIcn = IconManager.getInstance().getIcon("misc/history-clock-button-black", IconSize.SMALL);
		showHistoryAct.putValue(PhonUIAction.SMALL_ICON, historyIcn);
		showHistoryAct.putValue(PhonUIAction.NAME, "Query history");
		showHistoryAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Show query history");
		showHistoryAct.putValue(DropDownButton.ARROW_ICON_GAP, 2);
		showHistoryAct.putValue(DropDownButton.ARROW_ICON_POSITION, SwingConstants.BOTTOM);
		
		ScriptParameters scriptParams = getScriptPanel().getScriptParameters();
		getScriptPanel().addPropertyChangeListener(ScriptPanel.SCRIPT_PARAMS, (e) -> {
			addParamListeners();
		});
		addParamListeners();
		
		final QueryHistoryList paramHistoryView = new QueryHistoryList(scriptParams, stockQueryManager, queryHistoryManager);
		paramHistoryView.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		paramHistoryView.addMouseListener(new MouseInputAdapter() {
			@Override
			public void mouseClicked(MouseEvent me) {
				if(me.getButton() == MouseEvent.BUTTON1 && me.getClickCount() == 2) {
					final ParamSetType paramSetType = paramHistoryView.getSelectedValue();
					if(paramSetType != null) {
						loadFromParamSet(paramSetType);
						updateLabelFromCurrentHash();
						paramHistoryView.clearSelection();
						historyButton.getButtonPopup().hide();
					}
				}
			}
		});
		
		paramHistoryView.addKeyListener(new KeyListener() {
			
			@Override
			public void keyTyped(KeyEvent e) {
				
			}
			
			@Override
			public void keyReleased(KeyEvent e) {
				
			}
			
			@Override
			public void keyPressed(KeyEvent e) {
				if(e.getKeyCode() == KeyEvent.VK_ENTER) {
					final ParamSetType paramSetType = paramHistoryView.getSelectedValue();
					if(paramSetType != null) {
						loadFromParamSet(paramSetType);
						updateLabelFromCurrentHash();
						paramHistoryView.clearSelection();
						historyButton.getButtonPopup().hide();
					}
				}
			}
			
		});
		
		paramHistoryView.setVisibleRowCount(6);
		showHistoryAct.putValue(DropDownButton.BUTTON_POPUP, new JScrollPane(paramHistoryView));
		historyButton = new DropDownButton(showHistoryAct);
		historyButton.setOnlyPopup(true);
		
		historyButton.getButtonPopup().addPropertyChangeListener(ButtonPopup.POPUP_VISIBLE, (e) -> {
			if(Boolean.parseBoolean(e.getNewValue().toString())) {
				paramHistoryView.scrollRectToVisible(new Rectangle(0, 0, 0, 0));
			}
		});
		
		// query name
		queryNameModel = new DefaultComboBoxModel<>();
		queryNameBox = new JComboBox<>(queryNameModel);
		queryNameBox.setToolTipText("Select query by name");
		update();
		
		// save menu
		final JPopupMenu saveMenu = new JPopupMenu();
		
		Action saveAct = new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {}
		};
		saveAct.putValue(PhonUIAction.SMALL_ICON, IconManager.getInstance().getIcon("actions/document-save", IconSize.SMALL));
		saveAct.putValue(DropDownButton.ARROW_ICON_GAP, 2);
		saveAct.putValue(DropDownButton.ARROW_ICON_POSITION, SwingConstants.BOTTOM);
		saveAct.putValue(DropDownButton.BUTTON_POPUP, saveMenu);
		
		saveButton = new DropDownButton(saveAct);
		saveButton.setOnlyPopup(true);
		saveButton.getButtonPopup().addPropertyChangeListener(ButtonPopup.POPUP_VISIBLE, (e) -> {
			if(Boolean.parseBoolean(e.getNewValue().toString())) {
				saveMenu.removeAll();
				setupSaveMenu(new MenuBuilder(saveMenu));
			}
		});
		
		setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridheight = 1;
		gbc.gridwidth = 1;
		gbc.fill = GridBagConstraints.NONE;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		
		super.add(clearButton, gbc);
		gbc.gridx++;
		super.add(new JSeparator(SwingConstants.VERTICAL));
		gbc.gridx++;
		super.add(historyButton, gbc);
		gbc.gridx++;
		super.add(new JSeparator(SwingConstants.VERTICAL), gbc);
		gbc.gridx++;
		
		super.add(new JLabel("Query name:"), gbc);
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1.0;
		gbc.gridx++;
		super.add(queryNameBox, gbc);
		gbc.fill = GridBagConstraints.NONE;
		gbc.weightx = 0.0;
		gbc.gridx++;
		super.add(saveButton, gbc);
		
		updateLabelFromCurrentHash();
	}
	
	public JComboBox<String> getQueryNameBox() {
		return this.queryNameBox;
	}
	
	public ParamSetType currentQuery() {
		if(currentIndex < 0 || currentIndex >= queryHistoryManager.size()) return null;
		return queryHistoryManager.getParamSet(currentIndex);
	}
	
	public void setupSaveMenu(MenuBuilder builder) {
		// add save/rename item only if not in stock queries
		QueryScript queryScript = (QueryScript)getScriptPanel().getScript();
		try {
			if(stockQueryManager.getParamSet(queryScript) == null) {
				SaveQueryAction saveQueryAct = new SaveQueryAction(stockQueryManager, queryHistoryManager, (QueryScript)getScriptPanel().getScript());

				ParamSetType historyParamSet = queryHistoryManager.getParamSet(queryScript);
				if(historyParamSet != null && historyParamSet.getName() != null && historyParamSet.getName().length() > 0) {
					saveQueryAct.putValue(SaveQueryAction.NAME, "Rename query...");
				} else {
					saveQueryAct.putValue(SaveQueryAction.NAME, "Name query...");
				}
				
				builder.addItem(".", saveQueryAct);
				builder.addSeparator(".", "save_query");
			}
		} catch (PhonScriptException e) {
			LogUtil.warning(e);
		}
		
		CommonModuleFrame cmf = CommonModuleFrame.getCurrentFrame();
		if(cmf != null && cmf.getExtension(Project.class) != null) {
			ExportQueryAction projectLibraryAct = new ExportQueryAction(queryScript, 
					QueryScriptLibrary.projectScriptFolder(cmf.getExtension(Project.class)), getQueryName());
			projectLibraryAct.putValue(PhonUIAction.NAME, "Save in project library...");
			builder.addItem(".", projectLibraryAct);
		}
		
		ExportQueryAction userLibraryAct = new ExportQueryAction(queryScript, QueryScriptLibrary.USER_SCRIPT_FOLDER, getQueryName());
		userLibraryAct.putValue(PhonUIAction.NAME, "Save in user library...");
		builder.addItem(".", userLibraryAct);
		
		ExportQueryAction otherLocationAct = new ExportQueryAction(queryScript, null, getQueryName());
		otherLocationAct.putValue(PhonUIAction.NAME, "Save as...");
		builder.addItem(".", otherLocationAct);
	}
	
	public void onClear() {
		getScriptPanel().getScript().resetContext();
		try {
			getScriptPanel().updateParams();
		} catch (PhonScriptException e) {
			LogUtil.warning(e);
		}
		updateLabelFromCurrentHash();
	}
	
	public void onSave() {
		// do nothing
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
		final ScriptParameters scriptParams = getScriptPanel().getScriptParameters();
		updateLabelFromHash(scriptParams.getHashString());
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
			final ScriptParameters scriptParams = getScriptPanel().getScriptParameters();
			final String currentHash = scriptParams.getHashString();
			final ParamSetType stockSet = stockQueryManager.getParamSet(currentHash);
			if(stockSet != null) {
				retVal = stockSet.getName();
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
		queryNameBox.setSelectedItem(getQueryName());
		queryNameBox.addItemListener(itemListener);
	}
	
	private void update() {
		if(queryHistoryManager.size() == 0) {
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
