/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2017, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
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
package ca.phon.app.query;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rtextarea.RTextScrollPane;

import ca.phon.query.script.QueryScript;
import ca.phon.script.PhonScript;
import ca.phon.script.PhonScriptContext;
import ca.phon.script.PhonScriptException;
import ca.phon.script.params.ScriptParam;
import ca.phon.script.params.ScriptParameters;
import ca.phon.script.params.StringScriptParam;
import ca.phon.script.params.ui.ParamPanelFactory;
import ca.phon.ui.action.PhonActionEvent;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;

/**
 * A container for a query script with displays the
 * form for script parameters.  The script can also
 * be edited, during which the UI form is not available.
 *
 * To listen for changes in the script and available
 * parameters add a property change listener to this
 * panel.  The property of the script will be <code>SCRIPT_PROP</code>
 * while the property of the individual script parameters
 * will be <code>PARAM_PREFIX+&lt;paramName&gt;</code>.
 */
public class ScriptPanel extends JPanel {

	private static final long serialVersionUID = 3335240056447554685L;

	private static final Logger LOGGER = Logger
			.getLogger(ScriptPanel.class.getName());

	/**
	 * Property for the script text.  This is sent when the editor is no longer displayed.
	 *
	 */
	public static final String SCRIPT_PROP = ScriptPanel.class.getName() + ".script";

	/**
	 * Property prefix for script parameters
	 */
	public static final String PARAM_PREFIX = ScriptPanel.class.getName() + ".param";

	public static final String CURRENT_COMPONENT = ScriptPanel.class.getName() + ".component";

	/**
	 * Script object
	 */
	private PhonScript script;

	private JPanel cardPanel;
	private CardLayout cardLayout;

	/**
	 * Script editor
	 */
	private String scriptEditorId = "scriptEditor";
	private RSyntaxTextArea scriptEditor;

	/**
	 * Current form panel
	 */
	private final String paramPanelId = "paramPanel";
	private JPanel paramPanel;
	private JToggleButton scriptViewButton;

	/**
	 * Button panels
	 */
	private JComponent formBtnPanel;

	/**
	 * Button actions
	 */
	private Action viewScriptAction;
	private Action viewFormAction;

	private String oldScript = new String();

	/**
	 */
	public ScriptPanel() {
		this(new QueryScript(""));
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

	/**
	 * Action performed by the 'Edit Script' button
	 * in the form panel.
	 */
	public Action getViewScriptAction() {
		if(viewScriptAction == null) {
			viewScriptAction = new PhonUIAction(this, "onViewScript");
			viewScriptAction.putValue(PhonUIAction.NAME, "View script");
		}
		return viewScriptAction;
	}

	/**
	 * Action perform by the 'Save Script'
	 * button in the text area panel.
	 */
	public Action getViewFormAction() {
		if(viewFormAction == null) {
			viewFormAction = new PhonUIAction(this, "onViewForm");
			viewFormAction.putValue(PhonUIAction.NAME, "View form");
		}
		return viewFormAction;
	}

	/**
	 * Turn on/off button panels (useful to override
	 * default usage.
	 * @param visible
	 */
	public void setButtonPanelsVisible(boolean visible) {
		formBtnPanel.setVisible(visible);
		revalidate();
	}

	public void setScript(PhonScript script) {
		PhonScript oldScript = this.script;
		this.script = script;

		try {
			updateParamPanel();
		} catch (PhonScriptException e) {
			LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}
		scriptEditor.getDocument().removeDocumentListener(scriptDocListener);
		scriptEditor.setText(script.getScript());
		scriptEditor.setCaretPosition(0);
		scriptEditor.getDocument().addDocumentListener(scriptDocListener);
		super.firePropertyChange(SCRIPT_PROP, oldScript, this.script);
	}

	public PhonScript getScript() {
		return this.script;
	}

	private void updateParamPanel() throws PhonScriptException {
		paramPanel.removeAll();

		final PhonScriptContext ctx = script.getContext();
		ScriptParameters scriptParams = new ScriptParameters();
			scriptParams = ctx.getScriptParameters(ctx.getEvaluatedScope());

		final ParamPanelFactory factory = new ParamPanelFactory();
		scriptParams.accept(factory);
		scriptParams.forEach( (ScriptParam param) -> {
			param.addPropertyChangeListener(paramListener);
		});

		final JPanel form = factory.getForm();
		paramPanel.add(form, BorderLayout.CENTER);

		paramPanel.revalidate();
		paramPanel.repaint();
	}

	private void init() {
		setLayout(new BorderLayout());

		cardPanel = new JPanel();
		cardLayout = new CardLayout();
		cardPanel.setLayout(cardLayout);

		paramPanel = new JPanel(new BorderLayout());
		try {
			updateParamPanel();
		} catch (PhonScriptException e1) {
			LOGGER.log(Level.SEVERE, e1.getLocalizedMessage(), e1);
		}
		cardPanel.add(new JScrollPane(paramPanel), paramPanelId);

		// setup editor and save button
		scriptEditor = new RSyntaxTextArea();
		scriptEditor.setText(script.getScript());
		scriptEditor.setColumns(20);
		scriptEditor.setCaretPosition(0);
		RTextScrollPane scriptScroller = new RTextScrollPane(scriptEditor);
		scriptEditor.setSyntaxEditingStyle("text/javascript");
		scriptEditor.getDocument().addDocumentListener(scriptDocListener);
		cardPanel.add(scriptScroller, scriptEditorId);

		ImageIcon viewIcon =
				IconManager.getInstance().getIcon("apps/accessories-text-editor", IconSize.SMALL);
		scriptViewButton = new JToggleButton(viewIcon);
		scriptViewButton.setSelected(false);
		scriptViewButton.setToolTipText("Toggle script/form");
		scriptViewButton.putClientProperty("JButton.buttonType", "textured");
		scriptViewButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				boolean showEditor = scriptViewButton.isSelected();

				if(showEditor) {
					showScript();
				} else {
					showForm();
				}
				scriptViewButton.setSelected(showEditor);
			}

		});

		final FlowLayout layout = new FlowLayout(FlowLayout.RIGHT);
		layout.setVgap(0);

		formBtnPanel = new JPanel(layout);
		formBtnPanel.add(scriptViewButton);

		add(cardPanel, BorderLayout.CENTER);
		add(formBtnPanel, BorderLayout.SOUTH);
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
			LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
			return false;
		}
		for(ScriptParam sp:params) {
			if(sp.getParamType().equals("string")) {
				final StringScriptParam stringParam = (StringScriptParam)sp;
				if(!stringParam.isValidate()) {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Switches display to the script's form
	 */
	public void showForm() {
		if(oldScript != null && !oldScript.equals(getScript().getScript())) {
			try {
				updateParamPanel();
				firePropertyChange(SCRIPT_PROP, oldScript, getScript().getScript());
			} catch (PhonScriptException e) {
				Toolkit.getDefaultToolkit().beep();
				LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
				return;
			}
		}
		cardLayout.show(cardPanel, paramPanelId);
		firePropertyChange(CURRENT_COMPONENT, scriptEditor, paramPanel);
	}

	/**
	 * Switches display to the script editor.
	 *
	 */
	public void showScript() {
		oldScript = getScript().getScript();

		cardLayout.show(cardPanel, scriptEditorId);
		firePropertyChange(CURRENT_COMPONENT, paramPanel, scriptEditor);
	}

	/*
	 * UI Actions
	 */
	public void onViewScript(PhonActionEvent pae) {
		showScript();
	}

	public void onViewForm(PhonActionEvent pae) {
		showForm();
	}

	private PropertyChangeListener paramListener = (PropertyChangeEvent evt) -> {
		String paramPropName = PARAM_PREFIX + "_" + evt.getPropertyName();
		firePropertyChange(paramPropName, evt.getOldValue(), evt.getNewValue());
	};

	/**
	 * Listener for script document changes and updated underlying script
	 */
	private DocumentListener scriptDocListener = new DocumentListener() {

		@Override
		public void removeUpdate(DocumentEvent e) {
			script.delete(e.getOffset(), e.getOffset()+e.getLength());
		}

		@Override
		public void insertUpdate(DocumentEvent e) {
			try {
				String insertedText = e.getDocument().getText(e.getOffset(), e.getLength());
				script.insert(e.getOffset(), insertedText);
			} catch (BadLocationException e1) {
			}
		}

		@Override
		public void changedUpdate(DocumentEvent e) {
		}

	};

}
