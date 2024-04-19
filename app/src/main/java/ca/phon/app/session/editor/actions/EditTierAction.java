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
package ca.phon.app.session.editor.actions;

import ca.phon.app.session.editor.EditorEventManager;
import ca.phon.app.session.editor.SessionEditor;
import ca.phon.app.session.editor.undo.*;
import ca.phon.app.session.editor.view.tierManagement.*;
import ca.phon.formatter.*;
import ca.phon.ipa.IPATranscript;
import ca.phon.orthography.Orthography;
import ca.phon.orthography.mor.GraspTierData;
import ca.phon.orthography.mor.MorTierData;
import ca.phon.session.*;
import ca.phon.ui.fonts.FontPreferences;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * Show edit tier dialog for given tier view item.
 */
public class EditTierAction extends SessionEditorAction {

	private final TierViewItem tierItem;

	private final static String TXT = "Edit tier ";

	private final static ImageIcon ICON =
			IconManager.getInstance().getFontIcon("edit", IconSize.SMALL, UIManager.getColor("Button.foreground"));

	public EditTierAction(SessionEditor editor, TierViewItem item) {
		this(editor.getSession(), editor.getEventManager(), editor.getUndoSupport(), item);
	}

	public EditTierAction(Session session, EditorEventManager eventManager, SessionEditUndoSupport undoSupport, TierViewItem item) {
		super(session, eventManager, undoSupport);
		this.tierItem = item;

		putValue(NAME, TXT + item.getTierName());
		putValue(SMALL_ICON, ICON);
	}

	@Override
	public void hookableActionPerformed(ActionEvent e) {
		final SessionFactory factory = SessionFactory.newFactory();
			final SystemTierType systemTierType = SystemTierType.tierFromString(tierItem.getTierName());

			TierDescription depTierDesc  = getSession().getTier(tierItem.getTierName());
			if(depTierDesc != null) {
				final Font transcriptFont = 
						(tierItem.getTierFont().equals("default") ? 
								FontPreferences.getTierFont() :
									Font.decode(tierItem.getTierFont()));

				TierEditorDialog tierDialog = new TierEditorDialog(getSession(),true);
				TierInfoEditor tierEditor = tierDialog.getTierEditor();
				tierEditor.setTierName(tierItem.getTierName());
				tierEditor.setTierFont(transcriptFont);
				tierEditor.setBlind(depTierDesc.isBlind());
				if(UserTierType.Mor.getPhonTierName().equals(depTierDesc.getName()) ||
						UserTierType.Gra.getPhonTierName().equals(depTierDesc.getName()) ||
						UserTierType.Trn.getPhonTierName().equals(depTierDesc.getName()) ||
						UserTierType.Grt.getPhonTierName().equals(depTierDesc.getName()))
					tierEditor.setAligned(true);
				else
					tierEditor.setAligned(!depTierDesc.isExcludeFromAlignment());
				tierEditor.setVisible(tierItem.isVisible());
				String tierType = "User defined";
				if(depTierDesc.getDeclaredType() == Orthography.class)
					tierType = "CHAT";
				else if(depTierDesc.getDeclaredType() == IPATranscript.class)
					tierType = "IPA";
				else if(depTierDesc.getDeclaredType() == MorTierData.class)
					tierType = "Morphology";
				else if(depTierDesc.getDeclaredType() == GraspTierData.class)
					tierType = "GRASP";
				tierEditor.setTierType(tierType);

				tierDialog.add(tierEditor);
				tierDialog.setTitle("New Tier");
				tierDialog.setModal(true);
				tierDialog.pack();
				
				if(tierDialog.showDialog()) {
					final Formatter<Font> fontFormatter = FormatterFactory.createFormatter(Font.class);
					final String defaultFont = fontFormatter.format(FontPreferences.getTierFont());
					String fontString = fontFormatter.format(tierEditor.getTierFont());
					if(defaultFont.equals(fontString))
						fontString = "default";
					final TierViewItem newViewItem = factory.createTierViewItem(
							tierEditor.getTierName(), tierEditor.isVisible(), fontString, tierItem.isTierLocked());
					final TierDescription newTierDesc = tierEditor.createTierDescription();
					final TierViewItemEdit tierViewItemEdit = new TierViewItemEdit(getSession(), getEventManager(), tierItem, newViewItem, depTierDesc, newTierDesc);
					getUndoSupport().postEdit(tierViewItemEdit);
				} // if (showDialog())
			} // if (depTierDesc != null)
	}

}
