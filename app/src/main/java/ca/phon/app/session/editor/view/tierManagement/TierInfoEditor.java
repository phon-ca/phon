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
package ca.phon.app.session.editor.view.tierManagement;

import ca.phon.formatter.*;
import ca.phon.ipa.IPATranscript;
import ca.phon.orthography.Orthography;
import ca.phon.orthography.mor.GraspTierData;
import ca.phon.orthography.mor.MorTierData;
import ca.phon.session.*;
import ca.phon.session.tierdata.TierData;
import ca.phon.ui.FlatButton;
import ca.phon.ui.FontFormatter;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.ui.dialogs.JFontPanel;
import ca.phon.ui.fonts.FontPreferences;
import ca.phon.util.PrefHelper;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;
import com.jgoodies.forms.layout.*;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.text.ParseException;
import java.util.HashMap;

/**
 * Displays a UI for editing tier name,
 * grouping and font.  Grouping is disabled
 * if in 'EDIT' mode.
 * 
 */
public class TierInfoEditor extends JPanel {

	/* UI */
	private JTextField nameField;

	private JLabel chatNameLabel;

	private JCheckBox visibleBox;

	private JCheckBox lockedBox;

	private JCheckBox blindBox;

	private JCheckBox alignedBox;

	private JComboBox<String> typeBox;

	private JButton selectFontButton;

	private Session session;

	private boolean editMode = false;
	
	public TierInfoEditor(Session session, boolean editMode) {
		super();
		
		this.session = session;
		this.editMode = editMode;

		init();
	}

	public Session getSession() {
		return this.session;
	}
	
	private void init() {
		setLayout(new FormLayout(
				"right:pref, 3dlu, fill:pref:grow",
				"pref, 3dlu, pref, 3dlu, pref, 3dlu, pref, 3dlu, pref, 3dlu, pref, 3dlu, pref, 3dlu, pref"));
		final CellConstraints cc = new CellConstraints();

		int row = 1;
		add(new JLabel("Name:"), cc.xy(1, row));
		nameField = new JTextField();
		add(nameField, cc.xy(3, row));
		nameField.getDocument().addDocumentListener(new DocumentListener() {

			@Override
			public void insertUpdate(DocumentEvent e) {
				updateChatName();
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				updateChatName();
			}

			@Override
			public void changedUpdate(DocumentEvent e) {

			}

		});

		row += 2;
		chatNameLabel = new JLabel();
		final JLabel chatTierNameLabel = new JLabel("CHAT name:");
		chatTierNameLabel.setToolTipText("Name of tier in the CHAT transcription format");
		add(chatTierNameLabel, cc.xy(1, row));
		add(chatNameLabel, cc.xy(3, row));

		row += 2;
		add(new JLabel("Type:"), cc.xy(1, row));
		typeBox = new JComboBox<>();
		typeBox.addItem("User defined");
		typeBox.addItem("CHAT");
		typeBox.addItem("IPA");
		typeBox.addItem("Morphology");
		typeBox.addItem("GRASP");
		typeBox.setSelectedIndex(0);
		add(typeBox, cc.xy(3, row));

		row += 2;
		lockedBox = new JCheckBox("Prevent changes to tier in transcript view");
		lockedBox.setSelected(false);
		add(new JLabel("Locked:"), cc.xy(1, row));
		add(lockedBox, cc.xy(3, row));

		row += 2;
		visibleBox = new JCheckBox("Show tier in transcript view");
		visibleBox.setSelected(true);
		add(new JLabel("Visible:"), cc.xy(1, row));
		add(visibleBox, cc.xy(3, row));

		row += 2;
		blindBox = new JCheckBox("Include tier in blind transcription");
		blindBox.setSelected(false);
		add(new JLabel("Blind:"), cc.xy(1, row));
		add(blindBox, cc.xy(3, row));

		row += 2;
		alignedBox = new JCheckBox("Include tier in cross tier alignment");
		alignedBox.setSelected(false);
		add(new JLabel("Aligned:"), cc.xy(1, row));
		add(alignedBox, cc.xy(3, row));

		row += 2;
		final FontFormatter fontFormatter = new FontFormatter();
		final PhonUIAction<Void> selectFontAct = PhonUIAction.runnable(this::onSelectFont);
		selectFontAct.putValue(PhonUIAction.NAME, fontFormatter.format(FontPreferences.getTierFont()));
		selectFontAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Select font for tier...");
		selectFontAct.putValue(FlatButton.ICON_FONT_NAME_PROP, IconManager.GoogleMaterialDesignIconsFontName);
		selectFontAct.putValue(FlatButton.ICON_NAME_PROP, "text_format");
		selectFontAct.putValue(FlatButton.ICON_SIZE_PROP, IconSize.MEDIUM);
		selectFontButton = new FlatButton(selectFontAct);
		((FlatButton)selectFontButton).setPadding(0);
		add(new JLabel("Font:"), cc.xy(1, row));
		add(selectFontButton, cc.xy(3, row));
	}

	private void onSelectFont() {
		final JFontPanel fontPanel = new JFontPanel(getTierFont());
		final JOptionPane optionPane = new JOptionPane(fontPanel, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
		final JDialog dialog = optionPane.createDialog(this, "Select font");
		dialog.setResizable(true);
		dialog.pack();
		dialog.setSize(600, 500);
		dialog.setVisible(true);

		if(optionPane.getValue() != null && optionPane.getValue().equals(JOptionPane.OK_OPTION)) {
			setTierFont(fontPanel.getSelectedFont());
		}
	}

	public JTextField getNameField() {
		return this.nameField;
	}

	public String getTierName() {
		return nameField.getText();
	}

	private void updateChatName() {
		final String name = nameField.getText().trim();
		if(name.isEmpty()) {
			chatNameLabel.setText("");
		} else {
			final SystemTierType sysTierType = SystemTierType.tierFromString(name);
			final UserTierType userTierType = UserTierType.fromPhonTierName(name);
			if (sysTierType != null || userTierType != null) {
				chatNameLabel.setText((sysTierType != null ? sysTierType.getChatTierName() : userTierType.getChatTierName()));
			} else {
				chatNameLabel.setText(UserTierType.determineCHATTierName(session, name));
			}
		}
	}

	public void setTierName(String name) {
		nameField.setText(name);
		final SystemTierType sysTierType = SystemTierType.tierFromString(name);
		final UserTierType userTierType = UserTierType.fromPhonTierName(name);
		if(sysTierType != null || userTierType != null) {
			nameField.setEditable(false);
			alignedBox.setEnabled(false);
			typeBox.setEnabled(false);
		}
		updateChatName();
	}

	public Font getTierFont() {
        try {
            return new FontFormatter().parse(selectFontButton.getText());
        } catch (ParseException e) {
			return FontPreferences.getTierFont();
        }
    }
	
	public void setTierFont(Font font) {
		selectFontButton.setText(new FontFormatter().format(font));
	}

	public void setTierFont(String fontString) {
		if("default".equals(fontString)) {
			setTierFont(FontPreferences.getTierFont());
		} else {
			try {
				setTierFont(new FontFormatter().parse(fontString));
			} catch (ParseException e) {
				setTierFont(FontPreferences.getTierFont());
			}
		}
	}
	
	public void useDefaultFont() {
		selectFontButton.setText(new FontFormatter().format(FontPreferences.getTierFont()));
	}

	public JCheckBox getVisibleBox() {
		return visibleBox;
	}

	public boolean isVisible() {
		return visibleBox.isSelected();
	}

	public void setVisible(boolean visible) {
		visibleBox.setSelected(visible);
	}

	public JCheckBox getLockedBox() {
		return lockedBox;
	}

	public boolean isLocked() {
		return lockedBox.isSelected();
	}

	public void setLocked(boolean locked) {
		lockedBox.setSelected(locked);
	}

	public JCheckBox getBlindBox() {
		return blindBox;
	}

	public boolean isBlind() {
		return blindBox.isSelected();
	}

	public void setBlind(boolean blind) {
		blindBox.setSelected(blind);
	}

	public JCheckBox getAlignedBox() {
		return alignedBox;
	}

	public boolean isAligned() {
		return alignedBox.isSelected();
	}

	public void setAligned(boolean aligned) {
		alignedBox.setSelected(aligned);
	}

	public JComboBox<String> getTypeBox() {
		return typeBox;
	}

	public String getTierType() {
		return (String)typeBox.getSelectedItem();
	}

	public void setTierType(String type) {
		typeBox.setSelectedItem(type);
	}

	public void setTierType(Class<?> declaredType) {
		if(declaredType == Orthography.class) {
			setTierType("CHAT");
		} else if(declaredType == IPATranscript.class) {
			setTierType("IPA");
		} else if(declaredType == MorTierData.class) {
			setTierType("Morphology");
		} else if(declaredType == GraspTierData.class) {
			setTierType("GRASP");
		} else {
			setTierType("User defined");
		}
	}

	public boolean isEditMode() {
		return editMode;
	}
	
	public void setEditMode(boolean editMode) {
		this.editMode = editMode;
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
		final Class<?> declaredType = switch (getTierType()) {
			case "CHAT" -> Orthography.class;
			case "IPA" -> IPATranscript.class;
			case "Morphology" -> MorTierData.class;
			case "GRASP" -> GraspTierData.class;
			default -> TierData.class;
		};
		final SessionFactory factory = SessionFactory.newFactory();
		return factory.createTierDescription(getTierName().trim(), declaredType, new HashMap<>(), !isAligned(), isBlind());
	}
	
}
