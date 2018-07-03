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
package ca.phon.script.params.ui;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.beans.*;
import java.net.URL;
import java.util.List;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.text.StyleConstants;

import org.fife.ui.rsyntaxtextarea.AbstractTokenMakerFactory;
import org.fife.ui.rsyntaxtextarea.RSyntaxDocument;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SquiggleUnderlineHighlightPainter;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rsyntaxtextarea.SyntaxScheme;
import org.fife.ui.rsyntaxtextarea.TokenMakerFactory;
import org.fife.ui.rsyntaxtextarea.parser.ExtendedHyperlinkListener;
import org.fife.ui.rsyntaxtextarea.parser.ParseResult;
import org.fife.ui.rsyntaxtextarea.parser.Parser;
import org.fife.ui.rsyntaxtextarea.parser.ParserNotice;
import org.fife.ui.rtextarea.RTextArea;
import org.fife.ui.rtextarea.ToolTipSupplier;
import org.jdesktop.swingx.*;
import org.jdesktop.swingx.JXCollapsiblePane.Direction;
import org.jdesktop.swingx.painter.MattePainter;
import org.jdesktop.swingx.painter.Painter;

import ca.phon.script.params.*;
import ca.phon.script.params.EnumScriptParam.ReturnValue;
import ca.phon.ui.text.PhonexTokenMaker;
import ca.phon.ui.text.PromptedTextField;
import ca.phon.ui.text.PromptedTextField.FieldState;

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
		retVal.setVisible(param.getVisible());

		param.addPropertyChangeListener(ScriptParam.VISIBLE_PROP, (e) -> { retVal.setVisible(param.getVisible()); } );

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
		action.putValue(ScriptParamAction.SELECTED_KEY,
				(boolScriptParam.getValue(paramId) != null ? (Boolean)boolScriptParam.getValue(paramId) : boolScriptParam.getDefaultValue(paramId)));

		final JCheckBox retVal = new JCheckBox(action);
		retVal.setEnabled(boolScriptParam.isEnabled());
		retVal.setVisible(boolScriptParam.getVisible());

		installParamListener(retVal, boolScriptParam);
		boolScriptParam.addPropertyChangeListener(paramId, (e) -> {
			retVal.setSelected(Boolean.parseBoolean(boolScriptParam.getValue(paramId).toString()));
		});

		return retVal;
	}

	public JComponent createEnumScriptParamComponent(EnumScriptParam enumScriptParam) {
		if(enumScriptParam.getType().equals("radiobutton")) {
			return createRadiobuttonEnumScriptParamComponent(enumScriptParam);
		} else {
			return createComboboxEnumScriptParamComponent(enumScriptParam);
		}
	}

	public JPanel createRadiobuttonEnumScriptParamComponent(EnumScriptParam enumScriptParam) {
		final RadiobuttonEnumPanel retVal = new RadiobuttonEnumPanel(enumScriptParam);

		retVal.setEnabled(enumScriptParam.isEnabled());
		retVal.setVisible(enumScriptParam.getVisible());

		installParamListener(retVal, enumScriptParam);

		return retVal;
	}

	/**
	 * Create a combobox component for enum script parameters.
	 *
	 * @param enumScriptParam
	 *
	 * @return combo box
	 */
	public JComboBox<ReturnValue> createComboboxEnumScriptParamComponent(EnumScriptParam enumScriptParam) {
		final String paramId = enumScriptParam.getParamIds().iterator().next();

		final EnumScriptParamListener listener =
				new EnumScriptParamListener(enumScriptParam, paramId);

		final ReturnValue[] choices = enumScriptParam.getChoices();
		final JComboBox<ReturnValue> retVal = new JComboBox<>(choices);

		if(enumScriptParam.getValue(paramId) != null) {
			final ReturnValue val = (ReturnValue)enumScriptParam.getValue(paramId);
			retVal.setSelectedIndex(val.getIndex());
		} else {
			retVal.setSelectedIndex(enumScriptParam.getDefaultChoice());
		}

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
		retVal.setText(labelScriptParam.getText());

		installLabelParamListener(retVal, labelScriptParam);

		return retVal;
	}

	/**
	 * Create a multi-bool script param component.  This is
	 * several checkboxs grouped in columns
	 *
	 * @param multiBoolScriptParam
	 * @return panel contaning all checkboxes
	 */
	public MultiboolPanel createMultiBoolScriptParamComponent(MultiboolScriptParam multiBoolScriptParam) {
		final MultiboolPanel retVal = new MultiboolPanel(multiBoolScriptParam);

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
				(stringScriptParam.getValue(paramId) != null  ? stringScriptParam.getValue(paramId).toString() : stringScriptParam.getDefaultValue(paramId).toString());
		final PromptedTextField retVal = new PromptedTextField();
		retVal.setText(initialText);
		retVal.setPrompt(stringScriptParam.getPrompt());

		final StringScriptParamListener listener = new StringScriptParamListener(stringScriptParam, paramId, retVal);
		retVal.getDocument().addDocumentListener(listener);

		retVal.setVisible(stringScriptParam.getVisible());
		retVal.setEnabled(stringScriptParam.isEnabled());

		installParamListener(retVal, stringScriptParam);
		installStringParamListener(retVal, stringScriptParam);

		return retVal;
	}
	
	/**
	 * Pattern field
	 * 
	 * @param patternScriptParam
	 * @return text field with syntax highlighting
	 */
	public RSyntaxTextArea createPatternScriptParamComponent(PatternScriptParam patternScriptParam) {
		final String paramId = patternScriptParam.getParamIds().iterator().next();
		
		final String initialText =
				(patternScriptParam.getValue(paramId) != null  ? patternScriptParam.getValue(paramId).toString() : patternScriptParam.getDefaultValue(paramId).toString());
		final RSyntaxTextArea retVal = new RSyntaxTextArea(initialText);
		retVal.setToolTipSupplier( (textArea, evt) -> patternScriptParam.getTooltipText() );
		retVal.setSyntaxEditingStyle(patternScriptParam.getFormat());
		
		final PatternScriptParamListener listener = new PatternScriptParamListener(patternScriptParam, paramId, retVal);
		retVal.getDocument().addDocumentListener(listener);
		
		int lc = retVal.getLineCount();
		int numVisibleLines = Math.min(patternScriptParam.getMaxRows(), Math.max(lc, patternScriptParam.getMinRows()));
		retVal.setRows(numVisibleLines);
		
		installParamListener(retVal, patternScriptParam);
		installPatternParamListener(retVal, patternScriptParam);
		
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

		installParamListener(panel, separatorScriptParam);
		installSeparatorParamListener(panel, separatorScriptParam);

		return panel;
	}

	/**
	 * Create the toggle button for a separator
	 * @param name
	 * @param cp
	 * @return
	 */
	public JXButton createToggleButton(String name, JXCollapsiblePane cp, SeparatorScriptParam param) {
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

			@Override
			public Dimension getPreferredSize() {
				return new Dimension(0, 20);
			}
		};

		btn.setHorizontalAlignment(SwingConstants.LEFT);
		btn.putClientProperty("JComponent.sizeVariant", "small");
		btn.setBorderPainted(false);
		btn.setBackgroundPainter(new Painter<JXButton>() {

			@Override
			public void paint(Graphics2D g, JXButton object, int width, int height) {
				MattePainter mp = new MattePainter(Color.lightGray);
				mp.paint(g, object, width, height);
			}
		});

		btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

		installParamListener(btn, param);

		return btn;
	}

	private void installParamListener(JComponent comp, ScriptParam param) {
		final ScriptParamComponentListener listener = new ScriptParamComponentListener(comp);
		param.addPropertyChangeListener(listener);
	}

	private void installSeparatorParamListener(final JXCollapsiblePane panel, final SeparatorScriptParam param) {
		param.addPropertyChangeListener(SeparatorScriptParam.COLLAPSED_PROP, new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				panel.setCollapsed(param.isCollapsed());
			}
		});
	}
	
	private void installPatternParamListener(final RSyntaxTextArea textArea, final PatternScriptParam param) {
//		param.addPropertyChangeListener(StringScriptParam.VALIDATE_PROP, (e) -> {
//			if(!(Boolean)e.getNewValue()) {
//				textArea.setForeground(Color.red);
//			} else {
//				textArea.setForeground(Color.black);
//			}
//		});
		
		param.addPropertyChangeListener(PatternScriptParam.FORMAT_PROP, (e) -> {
			textArea.setSyntaxEditingStyle(param.getFormat());
		});
		param.addPropertyChangeListener(param.getParamId(), (e) -> {
			String val = param.getValue(param.getParamId()).toString();
			if(!textArea.getText().equals(val)) {
				textArea.setText(param.getValue(param.getParamId()).toString());
			}
		});
	}

	private void installStringParamListener(final PromptedTextField textField, final StringScriptParam param) {
		param.addPropertyChangeListener(StringScriptParam.PROMPT_PROP, new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				textField.setPrompt(param.getPrompt());
			}
		});
		param.addPropertyChangeListener(StringScriptParam.VALIDATE_PROP, new PropertyChangeListener() {

			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				if((Boolean)evt.getNewValue()) {
					textField.setState(FieldState.INPUT);
				} else {
					textField.setState(FieldState.UNDEFINED);
				}
			}
		});
		param.addPropertyChangeListener(StringScriptParam.TOOLTIP_TEXT_PROP, new PropertyChangeListener() {

			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				textField.setToolTipText((String)evt.getNewValue());
			}
		});
		param.addPropertyChangeListener(param.getParamId(), new PropertyChangeListener() {

			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				String val = param.getValue(param.getParamId()).toString();
				if(!textField.getText().equals(val)) {
					textField.setText(param.getValue(param.getParamId()).toString());
				}
			}

		});
	}

	private void installLabelParamListener(final JLabel label, final LabelScriptParam param) {
		param.addPropertyChangeListener(LabelScriptParam.LABEL_TEXT_PROP, new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				label.setText(param.getText());
			}
		});
	}
}
