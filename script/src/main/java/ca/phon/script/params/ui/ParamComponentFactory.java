package ca.phon.script.params.ui;

import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Insets;

import javax.swing.Action;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;

import org.jdesktop.swingx.JXButton;
import org.jdesktop.swingx.JXCollapsiblePane;
import org.jdesktop.swingx.JXCollapsiblePane.Direction;
import org.jdesktop.swingx.VerticalLayout;
import org.jdesktop.swingx.painter.MattePainter;
import org.jdesktop.swingx.painter.Painter;

import ca.phon.script.params.BooleanScriptParam;
import ca.phon.script.params.EnumScriptParam;
import ca.phon.script.params.LabelScriptParam;
import ca.phon.script.params.MultiboolScriptParam;
import ca.phon.script.params.ScriptParam;
import ca.phon.script.params.SeparatorScriptParam;
import ca.phon.script.params.StringScriptParam;
import ca.phon.ui.PromptedTextField;

/**
 * Factory responsible for creating components for script parameter
 * forms.
 *
 */
public class ParamComponentFactory {
	
	/**
	 * Create the label for the param
	 */
	public JLabel createParamLabel(ScriptParam param) {
		final JLabel retVal = new JLabel();
		retVal.setText(param.getParamDesc());
		return retVal;
	}

	/**
	 * Create a checkbox component for boolean script parameters.
	 * 
	 * @param boolScriptParam
	 * @return checkbox component for script parameter
	 */
	public JCheckBox createBooleanParamComponent(BooleanScriptParam boolScriptParam) {
		final String paramId = boolScriptParam.getParamIds().iterator().next();
		
		final BooleanScriptParamAction action = 
				new BooleanScriptParamAction(boolScriptParam, paramId);
		action.putValue(ScriptParamAction.NAME, boolScriptParam.getLabelText());
		action.putValue(ScriptParamAction.SELECTED_KEY, boolScriptParam.getDefaultValue(paramId));
		
		final JCheckBox retVal = new JCheckBox(action);
		retVal.setEnabled(boolScriptParam.isEnabled());
		retVal.setVisible(boolScriptParam.getVisible());
		
		installParamListener(retVal, boolScriptParam);
		
		return retVal;
	}
	
	/**
	 * Create a combobox component for enum script parameters.
	 * 
	 * @param enumScriptParam
	 * 
	 * @return combo box
	 */
	public JComboBox createEnumScriptParamComponent(EnumScriptParam enumScriptParam) {
		final String paramId = enumScriptParam.getParamIds().iterator().next();
		
		final EnumScriptParamListener listener = 
				new EnumScriptParamListener(enumScriptParam, paramId);
		
		final JComboBox retVal = new JComboBox(enumScriptParam.getChoices());
		retVal.addItemListener(listener);
		retVal.setEnabled(enumScriptParam.isEnabled());
		retVal.setVisible(enumScriptParam.getVisible());
		
		installParamListener(retVal, enumScriptParam);
		
		return retVal;
	}
	
	/**
	 * Create a label
	 * 
	 * @param labelScriptParam
	 * @return label
	 */
	public JLabel createLabelScriptParamComponent(LabelScriptParam labelScriptParam) {
		final JLabel retVal = new JLabel();
		retVal.setText(labelScriptParam.getLabelText());
		return retVal;
	}
	
	/**
	 * Create a multi-bool script param component.  This is 
	 * several checkboxs grouped in columns
	 * 
	 * @param multiBoolScriptParam
	 * @return panel contaning all checkboxes
	 */
	public JPanel createMultiBoolScriptParamComponent(MultiboolScriptParam multiBoolScriptParam) {
		final JPanel retVal = new JPanel();
		
		final GridLayout gl = new GridLayout(0, multiBoolScriptParam.getNumCols());
		retVal.setLayout(gl);
		
		for(String paramId:multiBoolScriptParam.getParamIds()) {
			final BooleanScriptParamAction action = 
					new BooleanScriptParamAction(multiBoolScriptParam, paramId);
			action.putValue(ScriptParamAction.NAME, multiBoolScriptParam.getLabelText(paramId));
			action.putValue(ScriptParamAction.SELECTED_KEY, multiBoolScriptParam.getDefaultValue(paramId));
			
			final JCheckBox checkBox = new JCheckBox(action);
			retVal.add(checkBox);
		}
		
		retVal.setEnabled(multiBoolScriptParam.isEnabled());
		retVal.setVisible(multiBoolScriptParam.getVisible());
		
		installParamListener(retVal, multiBoolScriptParam);
		
		return retVal;
	}
	
	/**
	 * Create a text field 
	 * 
	 * @param stringScriptParam
	 * @return prompted text field
	 */
	public PromptedTextField createStringScriptParamComponent(StringScriptParam stringScriptParam) {
		final String paramId = stringScriptParam.getParamIds().iterator().next();
		
		final String initialText =
				(stringScriptParam.getDefaultValue(paramId) != null ? stringScriptParam.getDefaultValue(paramId).toString() : null);
		final PromptedTextField retVal = new PromptedTextField();
		retVal.setText(initialText);
		final StringScriptParamListener listener = new StringScriptParamListener(stringScriptParam, paramId, retVal);
		retVal.getDocument().addDocumentListener(listener);
		
		installParamListener(retVal, stringScriptParam);
		
		return retVal;
	}
	
	/**
	 * Creates a new collapsible container for a script param group.
	 * @param separatorScriptParam
	 * @return container
	 */
	public JXCollapsiblePane createSeparatorScriptParamComponent(SeparatorScriptParam separatorScriptParam) {
		final JXCollapsiblePane panel = new JXCollapsiblePane(Direction.DOWN);
		panel.setAnimated(false);
		panel.setLayout(new VerticalLayout());
		panel.setCollapsed(separatorScriptParam.isCollapsed());
		
		return panel;
	}
	
	/**
	 * Create the toggle button for a separator
	 * @param name
	 * @param cp
	 * @return
	 */
	public JXButton createToggleButton(String name, JXCollapsiblePane cp) {
		Action toggleAction = cp.getActionMap().get(JXCollapsiblePane.TOGGLE_ACTION);
		
		// use the collapse/expand icons from the JTree UI
		toggleAction.putValue(JXCollapsiblePane.COLLAPSE_ICON,
		                      UIManager.getIcon("Tree.expandedIcon"));
		toggleAction.putValue(JXCollapsiblePane.EXPAND_ICON,
		                      UIManager.getIcon("Tree.collapsedIcon"));
		toggleAction.putValue(Action.NAME, name);

		@SuppressWarnings("serial")
		JXButton btn = new JXButton(toggleAction) {
			@Override
			public Insets getInsets() {
				Insets retVal = super.getInsets();
				
				retVal.top = 0;
				retVal.bottom = 0;
				
				return retVal;
			}
		};
		
		btn.setHorizontalAlignment(SwingConstants.LEFT);
		
		btn.setBackgroundPainter(new Painter<JXButton>() {
			
			@Override
			public void paint(Graphics2D g, JXButton object, int width, int height) {
				MattePainter mp = new MattePainter(ca.phon.ui.PhonGuiConstants.PHON_SELECTED);
				mp.paint(g, object, width, height);
			}
		});
		
		btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		
		return btn;
	}
	
	private void installParamListener(JComponent comp, ScriptParam param) {
		final ScriptParamComponentListener listener = new ScriptParamComponentListener(comp);
		param.addPropertyChangeListener(listener);
	}
	
}
