package ca.phon.app.query;

import java.awt.Toolkit;
import java.lang.ref.WeakReference;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.jdesktop.swingx.HorizontalLayout;

import ca.phon.app.log.LogUtil;
import ca.phon.app.session.editor.SegmentedButtonBuilder;
import ca.phon.query.history.ParamType;
import ca.phon.query.history.ParamsType;
import ca.phon.query.history.QueryHistoryType;
import ca.phon.query.history.QueryInfoType;
import ca.phon.query.script.QueryScript;
import ca.phon.script.PhonScriptException;
import ca.phon.script.params.ScriptParameters;
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
	
	private JLabel label;

	private JButton firstButton;
	
	private JButton lastButton;
	
	private JButton nextButton;
	
	private JButton prevButton;
	
	private int currentIndex = -1;
	
	private final QueryHistoryType queryHistory;
	
	private final WeakReference<ScriptPanel> scriptPanelRef;
	
	public QueryHistoryPanel(QueryHistoryType queryHistory, ScriptPanel scriptPanel) {
		super();
		
		this.queryHistory = queryHistory;
		this.scriptPanelRef = new WeakReference<ScriptPanel>(scriptPanel);
		
		init();
	}
	
	public QueryHistoryType getQueryHistory() {
		return this.queryHistory;
	}
	
	public ScriptPanel getScriptPanel() {
		return scriptPanelRef.get();
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
		goFirstAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "View oldest query in history");
		
		final PhonUIAction goLastAct = new PhonUIAction(this, "gotoLast");
		goLastAct.putValue(PhonUIAction.SMALL_ICON, IconManager.getInstance().getIcon("actions/go-last", IconSize.SMALL));
		goLastAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "View most recent query in history");
		
		SegmentedButtonBuilder<JButton> segButtonBuilder = new SegmentedButtonBuilder<>(JButton::new);
		ButtonGroup bg = new ButtonGroup();
		List<JButton> buttons = segButtonBuilder.createSegmentedRoundRectButtons(4, bg);
		
		label = new JLabel();
		label.setIcon(IconManager.getInstance().getIcon("misc/history-clock-button-black", IconSize.SMALL));
		label.setToolTipText("Query History");
		updateLabel();
		
		firstButton = buttons.get(0);
		firstButton.setAction(goFirstAct);
		
		prevButton = buttons.get(1);
		prevButton.setAction(historyPrevAct);
	
		nextButton = buttons.get(2);
		nextButton.setAction(historyNextAct);
		
		lastButton = buttons.get(3);
		lastButton.setAction(goLastAct);
		
		setLayout(new HorizontalLayout());
		add(label);
		add(firstButton);
		add(prevButton);
		add(nextButton);
		add(lastButton);
	}
	
	public void gotoFirst() {
		gotoIndex(0);
	}
	
	public void gotoLast() {
		gotoIndex(queryHistory.getQuery().size()-1);
	}
	
	public void goPrevious() {
		if(queryHistory.getQuery().size() == 0) return;
		gotoIndex(currentIndex > 0 ? currentIndex - 1 : 0);
	}
	
	public void goNext() {
		if(queryHistory.getQuery().size() == 0) return;
		gotoIndex(currentIndex < queryHistory.getQuery().size()-1 ? currentIndex + 1 : queryHistory.getQuery().size()-1);
	}

	public void gotoIndex(int index) {
		if(index < 0 || index >= getQueryHistory().getQuery().size()) return;

		currentIndex = index;
		loadParamsFromQueryInfo(queryHistory.getQuery().get(currentIndex));
		updateLabel();
	}
	
	public void gotoHash(String hash) {
		for(int i = 0; i < getQueryHistory().getQuery().size(); i++) {
			final QueryInfoType queryInfo = getQueryHistory().getQuery().get(i);
			if(queryInfo.getHash().equals(hash)) {
				gotoIndex(i);
				break;
			}
		}
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
		currentIndex = -1;
		for(int i = 0; i < getQueryHistory().getQuery().size(); i++) {
			final QueryInfoType queryInfo = getQueryHistory().getQuery().get(i);
			if(queryInfo.getHash().equals(hash)) {
				currentIndex = i;
				break;
			}
		}
		updateLabel();
	}
	
	private void updateLabel() {
		if(queryHistory.getQuery().size() > 0)
			label.setText(String.format("%2d/%2d", (currentIndex+1), queryHistory.getQuery().size()));
		else 
			label.setText(String.format("%2d/%2d", 0, 0));
	}
	
	public void loadParamsFromQueryInfo(QueryInfoType queryInfo) {
		final QueryScript queryScript = (QueryScript)getScriptPanel().getScript();
		queryScript.resetContext();
		
		final Map<String, Object> paramMap = new LinkedHashMap<>();
		final ParamsType previousParams = queryInfo.getParams();
		for(ParamType paramType:previousParams.getParam()) {
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
	
}
