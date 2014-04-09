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
package ca.phon.app.session.editor.view.tier_management;

import java.awt.Font;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import ca.phon.app.prefs.PhonProperties;
import ca.phon.formatter.Formatter;
import ca.phon.formatter.FormatterFactory;
import ca.phon.session.SessionFactory;
import ca.phon.session.SystemTierType;
import ca.phon.session.TierDescription;
import ca.phon.session.TierViewItem;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.ui.dialogs.JFontPanel;
import ca.phon.util.PrefHelper;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

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
				PrefHelper.getFont(PhonProperties.IPA_TRANSCRIPT_FONT,
						Font.decode(PhonProperties.DEFAULT_IPA_TRANSCRIPT_FONT)));
		
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
		fontPanel.setSelectedFont(PrefHelper.getFont(PhonProperties.IPA_TRANSCRIPT_FONT,
				Font.decode(PhonProperties.DEFAULT_IPA_TRANSCRIPT_FONT)));
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
		final String fontString = (fontFormatter == null ? selectedFont.toString() : fontFormatter.format(selectedFont));
		return factory.createTierViewItem(getTierName(), isVisible(), fontString);
	}
	
	public TierDescription createTierDescription() {
		final SessionFactory factory = SessionFactory.newFactory();
		// TODO allow for different types
		return factory.createTierDescription(getTierName(), isGrouped(), String.class);
	}
	
}
