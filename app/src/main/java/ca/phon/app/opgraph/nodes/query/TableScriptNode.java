package ca.phon.app.opgraph.nodes.query;

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JPanel;

import ca.gedge.opgraph.app.GraphDocument;
import ca.gedge.opgraph.app.extensions.NodeSettings;
import ca.phon.app.query.ScriptPanel;
import ca.phon.script.BasicScript;
import ca.phon.script.PhonScript;
import ca.phon.script.PhonScriptException;
import ca.phon.script.params.ScriptParam;
import ca.phon.script.params.ScriptParameters;

/**
 * Base class for script operations on tables.
 *
 */
public abstract class TableScriptNode extends TableOpNode implements NodeSettings {
	
	private final static Logger LOGGER = Logger.getLogger(TableScriptNode.class.getName());

	// script
	private PhonScript script;
	
	// UI
	private JPanel settingsPanel;
	private ScriptPanel scriptPanel = new ScriptPanel();
	
	public TableScriptNode() {
		this("");
	}

	public TableScriptNode(String script) {
		this(new BasicScript(script));
		putExtension(NodeSettings.class, this);
	}
	
	public TableScriptNode(PhonScript script) {
		super();
		this.script = script;
	}
	
	public PhonScript getScript() {
		return this.script;
	}
	
	public ScriptPanel getScriptPanel() {
		return this.scriptPanel;
	}
	
	@Override
	public Component getComponent(GraphDocument document) {
		if(settingsPanel == null) {
			settingsPanel = createSettingsPanel();
		}
		return settingsPanel;
	}
	
	protected JPanel createSettingsPanel() {
		JPanel retVal = new JPanel(new BorderLayout());
		
		scriptPanel = new ScriptPanel(getScript());
		retVal.add(scriptPanel, BorderLayout.CENTER);
		
		return retVal;
	}
	
	@Override
	public Properties getSettings() {
		final Properties retVal = new Properties();
		
		retVal.setProperty("__script", getScript().getScript());
		
		try {
			final ScriptParameters scriptParams = getScript().getContext().getScriptParameters(
					getScript().getContext().getEvaluatedScope());
			for(ScriptParam param:scriptParams) {
				if(param.hasChanged()) {
					for(String paramId:param.getParamIds()) {
						retVal.setProperty(paramId, param.getValue(paramId).toString());
					}
				}
			}
		} catch (PhonScriptException e) {
			LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}
		
		
		return retVal;
	}

	@Override
	public void loadSettings(Properties properties) {
		if(properties.containsKey("__script")) {
			this.script = new BasicScript(properties.getProperty("__script"));
			if(scriptPanel != null)
				scriptPanel.setScript(this.script);
			
			try {
				final ScriptParameters scriptParams = getScript().getContext().getScriptParameters(
						getScript().getContext().getEvaluatedScope());
				for(ScriptParam param:scriptParams) {
					for(String paramId:param.getParamIds()) {
						if(properties.containsKey(paramId)) {
							param.setValue(paramId, properties.get(paramId));
						}
					}
				}
			} catch (PhonScriptException e) {
				LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);	
			}
		}
	}
	
}
