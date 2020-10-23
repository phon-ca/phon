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
package ca.phon.app.session.editor.view.tier_management;

import java.awt.*;

import javax.swing.*;

import com.jgoodies.forms.layout.*;

import ca.phon.formatter.*;
import ca.phon.session.*;
import ca.phon.ui.action.*;
import ca.phon.ui.dialogs.*;
import ca.phon.ui.fonts.*;
import ca.phon.util.*;

/**
 * Displays a UI for editing tier name,
 * grouping and font.  Grouping is disabled
 * if in 'EDIT' mode.
 * 
 */
public class TierInfoEditor extends JPanel {

	private static final long serialVersionUID = 733398380982206648L;

	/* UI */
	private JTextField nameField;
	
	private JCheckBox groupBox;
	
	private JFontPanel fontPanel;
	
	private JButton useDefaultFontButton;
	
	private boolean editMode = false;
	
	public TierInfoEditor() {
		this(false);
	}
	
	public TierInfoEditor(boolean editMode) {
		super();
		
		this.editMode = editMode;
		
		init();
	}
	
	private void init() {
		FormLayout layout = new FormLayout(
				"5dlu, pref, 3dlu, fill:pref:grow, 5dlu",
				"pref, 3dlu, pref, 3dlu, pref, pref");
		CellConstraints cc = new CellConstraints();
		
		setLayout(layout);
		
		add(new JLabel("Tier Name"), cc.xy(2,1));
		nameField = new JTextField();
		add(nameField, cc.xy(4, 1));
		
		groupBox = new JCheckBox("Grouped (word-aligned)");
		groupBox.setEnabled(!editMode);
		add(groupBox, cc.xy(4, 3));
		
		final PhonUIAction defaultFontAction = new PhonUIAction(this, "useDefaultFont");
		defaultFontAction.putValue(PhonUIAction.NAME, "Use default font");
		defaultFontAction.putValue(PhonUIAction.SHORT_DESCRIPTION, "Use default tier font set in Preferences");
		useDefaultFontButton = new JButton(defaultFontAction);
		
		fontPanel = new JFontPanel();
		fontPanel.setSelectedFont(
				FontPreferences.getTierFont());
		
		add(new JLabel("Font"), cc.xy(2, 5));
		add(useDefaultFontButton, cc.xy(4, 5));
		add(fontPanel, cc.xy(4, 6));
	}
	
	public String getTierName() {
		return nameField.getText();
	}
	
	public void setTierName(String name) {
		nameField.setText(name);
		
		if(SystemTierType.isSystemTier(name)) {
			nameField.setEditable(false);
		}
	}
	
	public Font getTierFont() {
		return fontPanel.getSelectedFont();
	}
	
	public void setTierFont(Font font) {
		fontPanel.setSelectedFont(font);
	}
	
	public boolean isGrouped() {
		return groupBox.isSelected();
	}
	
	public void setGrouped(boolean grouped) {
		groupBox.setSelected(grouped);
	}
	
	public void useDefaultFont() {
		fontPanel.setSelectedFont(FontPreferences.getTierFont());
	}

	public boolean isEditMode() {
		return editMode;
	}
	
	public void setEditMode(boolean editMode) {
		this.editMode = editMode;
		groupBox.setEnabled(!editMode);
	}
	
	public TierViewItem createTierViewItem() {
		final SessionFactory factory = SessionFactory.newFactory();
		final Font selectedFont = getTierFont();
		final Formatter<Font> fontFormatter = FormatterFactory.createFormatter(Font.class);
		String fontString = 
				(fontFormatter == null ? selectedFont.toString() : fontFormatter.format(selectedFont));
		if(fontString.equals(PrefHelper.get(FontPreferences.TIER_FONT, FontPreferences.DEFAULT_TIER_FONT))) {
			fontString = "default";
		}
		return factory.createTierViewItem(getTierName().trim(), isVisible(), fontString);
	}
	
	public TierDescription createTierDescription() {
		final SessionFactory factory = SessionFactory.newFactory();
		return factory.createTierDescription(getTierName().trim(), isGrouped(), TierString.class);
	}
	
}
