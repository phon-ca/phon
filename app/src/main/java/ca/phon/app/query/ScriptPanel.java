/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2008 The Phon Project, Memorial University <http://phon.ling.mun.ca>
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
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import ca.phon.script.PhonScriptContext;
import ca.phon.script.PhonScriptException;
import ca.phon.script.params.ScriptParameters;
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
	 * Property for the script text
	 */
	public static final String SCRIPT_PROP = ScriptPanel.class.getName() + ".script";
	
	/**
	 * Property prefix for script parameters
	 */
	public static final String PARAM_PREFIX = ScriptPanel.class.getName() + ".param";
	
	/**
	 * Script object
	 */
	private EditableQueryScript script;
	
	/**
	 * Script editor
	 */
	private RSyntaxTextArea scriptEditor;
	private JPanel scriptPanel;
	
	/**
	 * Current form panel
	 */
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
	public ScriptPanel(QueryScript script) {
		this.script = new EditableQueryScript(script);
	
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
	
	public void setScript(QueryScript script) {
		QueryScript oldScript = this.script;
//		this.script = script;
//		for(ScriptParam param:oldScript.getScriptParams())
//			param.removeListener(paramListener);
		updateParamPanel();
//		scriptEditor.setDocument(new RSyntaxQueryDocument(this.script));
		scriptEditor.getDocument().removeDocumentListener(scriptDocListener);
		scriptEditor.setText(script.getScript());
		scriptEditor.setCaretPosition(0);
		scriptEditor.getDocument().addDocumentListener(scriptDocListener);
		super.firePropertyChange(SCRIPT_PROP, true, false);
	}
	
	public QueryScript getScript() {
		return this.script;
	}
	
	private void updateParamPanel() {
		paramPanel.removeAll();
//		ScriptParam[] params = script.getScriptParams();
//		for(ScriptParam p:params)
//			p.addListener(paramListener);
		final PhonScriptContext ctx = script.getContext();
		ScriptParameters scriptParams = new ScriptParameters();
		try {
			scriptParams = ctx.getScriptParameters(ctx.getEvaluatedScope());
		} catch (PhonScriptException e) {
			LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}
		
		final ParamPanelFactory factory = new ParamPanelFactory();
		scriptParams.accept(factory);
		
		final JPanel form = factory.getForm();
		JScrollPane formScroller = new JScrollPane(form);
		formScroller.getVerticalScrollBar().setUnitIncrement(10);
		paramPanel.add(formScroller, BorderLayout.CENTER);
		paramPanel.revalidate();
		paramPanel.repaint();
	}
	
	private void init() {
		setLayout(new BorderLayout());
		
		paramPanel = new JPanel(new BorderLayout());
		updateParamPanel();
		add(paramPanel, BorderLayout.CENTER);
		showingForm = true;
		
		// setup editor and save button
		scriptPanel = new JPanel(new BorderLayout());
		scriptEditor = new RSyntaxTextArea();
		scriptEditor.setText(script.getScript());
		scriptEditor.setColumns(20);
		scriptEditor.setCaretPosition(0);
		RTextScrollPane scriptScroller = new RTextScrollPane(scriptEditor);
		scriptEditor.setSyntaxEditingStyle("text/javascript");
		scriptEditor.getDocument().addDocumentListener(scriptDocListener);

		scriptPanel.add(scriptScroller, BorderLayout.CENTER);
		
		ImageIcon viewIcon = 
				IconManager.getInstance().getIcon("apps/accessories-text-editor", IconSize.SMALL);
		scriptViewButton = new JToggleButton(viewIcon);
		scriptViewButton.setSelected(!isShowingForm());
		scriptViewButton.setToolTipText("Toggle script/form");
		scriptViewButton.putClientProperty("JButton.buttonType", "textured");
		scriptViewButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if(isShowingForm()) {
					showScript();
					
				} else {
					showForm();
				}
				scriptViewButton.setSelected(!isShowingForm());
			}
		});
		
		final FlowLayout layout = new FlowLayout(FlowLayout.RIGHT);
		layout.setVgap(0);
		
		formBtnPanel = new JPanel(layout);
		formBtnPanel.add(scriptViewButton);
		add(formBtnPanel, BorderLayout.SOUTH);
	}
	
	private boolean showingForm = false;
	/**
	 * Switches display to the script's form
	 */
	public void showForm() {
		// update script params
//		for(ScriptParam param:script.getScriptParams()) 
//			param.removeListener(paramListener);
//		script.updateScriptParams();
		// swap editor with updated param panel
		remove(scriptPanel);
		add(paramPanel, BorderLayout.CENTER);
		updateParamPanel();
		revalidate();
		showingForm = true;
	}
	
	/**
	 * Switches display to the script editor.
	 * 
	 */
	public void showScript() {
		// swap param panel with script editor
		remove(paramPanel);
		add(scriptPanel);
		revalidate();
		repaint();
		showingForm = false;
	}
	
	public boolean isShowingForm() {
		return showingForm;
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
	
//	public void onCancelEdit(PhonActionEvent pae) {
//		String origScript = script.getScript(false);
//		String newScript = scriptEditor.getText();
//		if(!origScript.equals(newScript)) {
//			int retVal = 
//				NativeDialogs.showOkCancelDialogBlocking(
//						CommonModuleFrame.getCurrentFrame(), "", 
//						"Cancel editing", "Discard changes to script?");
//			if(retVal == NativeDialogEvent.CANCEL_OPTION) {
//				return;
//			}
//		}
//		
//		scriptEditor.setText(script.getScript(false));
//		
//		// swap editor with updated param panel
//		remove(scriptPanel);
//		add(paramPanel, BorderLayout.CENTER);
//		updateParamPanel();
//		revalidate();
//	}
	
//	/**
//	 * Listener for all params
//	 */
//	private ParamListener paramListener = new ParamListener() {
//
//		@Override
//		public void onParamValueChanged(String paramid, Object oldvalue,
//				Object newvalue) {
//			String paramPropName = PARAM_PREFIX + "_" + paramid;
//			firePropertyChange(paramPropName, oldvalue, newvalue);
//		}
//		
//	};
	
	/**
	 * Listener for script document changes
	 */
	private DocumentListener scriptDocListener = new DocumentListener() {
		
		@Override
		public void removeUpdate(DocumentEvent e) {
			script.delete(e.getOffset(), e.getOffset()+e.getLength());
			firePropertyChange(SCRIPT_PROP, true, false);
		}
		
		@Override
		public void insertUpdate(DocumentEvent e) {
			try {
				String insertedText = e.getDocument().getText(e.getOffset(), e.getLength());
				script.insert(e.getOffset(), insertedText);
				firePropertyChange(SCRIPT_PROP, true, false);
			} catch (BadLocationException e1) {
			}
		}
		
		@Override
		public void changedUpdate(DocumentEvent e) {
		}
	};
	
}
