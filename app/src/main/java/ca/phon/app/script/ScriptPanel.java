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
package ca.phon.app.script;

import ca.phon.app.log.LogUtil;
import ca.phon.script.*;
import ca.phon.script.params.*;
import ca.phon.script.params.ui.ParamPanelFactory;

import javax.swing.*;
import java.awt.*;
import java.beans.*;

/**
 * Component for {@link PhonScript} forms.
 *
 * To listen for changes in the script and available
 * parameters add a property change listener to this
 * panel.  The property of the script will be <code>SCRIPT_PROP</code>
 * while the property of the individual script parameters
 * will be <code>PARAM_PREFIX+&lt;paramName&gt;</code>.
 */
public class ScriptPanel extends JPanel implements Scrollable {

	private static final long serialVersionUID = 3335240056447554685L;

	/**
	 * Property for the script text.  This is sent when the editor is no longer displayed.
	 *
	 */
	public static final String SCRIPT_PROP = ScriptPanel.class.getName() + ".script";

	/**
	 * Property prefix for script parameters
	 */
	public static final String PARAM_PREFIX = ScriptPanel.class.getName() + ".param";

	public static final String SCRIPT_PARAMS = ScriptPanel.class.getName() + ".scriptParams";

	/**
	 * Script object
	 */
	private PhonScript script;
	
	private ScriptParameters scriptParams;

	/**
	 * Create a new empty script panel
	 */
	public ScriptPanel() {
		this(new BasicScript(""));
	}

	/**
	 * Constructor
	 *
	 * @param script
	 */
	public ScriptPanel(PhonScript script) {
		this.script = script;

		init();
	}
	
	public void setScript(PhonScript script) {
		PhonScript oldScript = this.script;
		this.script = script;

		try {
			updateParams();
		} catch (PhonScriptException e) {
			LogUtil.severe(e);
		}
		super.firePropertyChange(SCRIPT_PROP, oldScript, this.script);
	}

	public PhonScript getScript() {
		return this.script;
	}

	public void updateParams() throws PhonScriptException {
		removeAll();
		var oldParams = this.scriptParams;

		final PhonScriptContext ctx = script.getContext();
		ScriptParameters scriptParams = ctx.getScriptParameters(ctx.getEvaluatedScope());

		if(scriptParams != this.scriptParams) {
			scriptParams.forEach( (ScriptParam param) -> {
				param.addPropertyChangeListener(paramListener);
			});
			this.scriptParams = scriptParams;
		}

		final ParamPanelFactory factory = new ParamPanelFactory(() -> {
			JPanel panel = new JPanel();
//			panel.setBackground(Color.WHITE);
			return panel;
		});
		scriptParams.accept(factory);
		add(factory.getForm(), BorderLayout.CENTER);
		
		super.firePropertyChange(SCRIPT_PARAMS, oldParams, scriptParams);
	}

	public ScriptParameters getScriptParameters() {
		return this.scriptParams;
	}
	
	private void init() {
		setLayout(new BorderLayout());

		try {
			updateParams();
		} catch (PhonScriptException e1) {
			LogUtil.severe(e1);
		}
	}

	/**
	 * Check script params
	 *
	 * @return <code>true</code> if script params all validate,
	 *  <code>false</code> otherwise
	 */
	public boolean checkParams() {
		ScriptParameters params;
		try {
			params = getScript().getContext().getScriptParameters(getScript().getContext().getEvaluatedScope());
		} catch (PhonScriptException e) {
			LogUtil.severe(e);
			return false;
		}
		for(ScriptParam sp:params) {
			if(sp instanceof StringScriptParam) {
				final StringScriptParam stringParam = (StringScriptParam)sp;
				if(!stringParam.isValidate()) {
					return false;
				}
			}
		}
		return true;
	}

	private PropertyChangeListener paramListener = (PropertyChangeEvent evt) -> {
		String paramPropName = PARAM_PREFIX + "_" + evt.getPropertyName();
		firePropertyChange(paramPropName, evt.getOldValue(), evt.getNewValue());
	};

	@Override
	public Dimension getPreferredScrollableViewportSize() {
		return null;
	}

	@Override
	public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
		return 10;
	}

	@Override
	public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
		return 100;
	}

	@Override
	public boolean getScrollableTracksViewportWidth() {
		return true;
	}

	@Override
	public boolean getScrollableTracksViewportHeight() {
		return false;
	}

}
