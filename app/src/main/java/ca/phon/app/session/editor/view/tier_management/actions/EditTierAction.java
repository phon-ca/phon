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
package ca.phon.app.session.editor.view.tier_management.actions;

import java.awt.Font;
import java.awt.event.ActionEvent;

import javax.swing.undo.CompoundEdit;

import ca.phon.app.session.editor.SessionEditor;
import ca.phon.app.session.editor.undo.*;
import ca.phon.app.session.editor.view.tier_management.*;
import ca.phon.formatter.*;
import ca.phon.session.*;
import ca.phon.ui.fonts.FontPreferences;

public class EditTierAction extends TierManagementAction {

	private static final long serialVersionUID = -3730161807560410262L;
	
	private final TierViewItem tierItem;

	public EditTierAction(SessionEditor editor, TierOrderingEditorView view,
			TierViewItem item) {
		super(editor, view);
		this.tierItem = item;
		
		putValue(NAME, "Edit tier " + item.getTierName());
	}

	@Override
	public void hookableActionPerformed(ActionEvent e) {
		final SessionFactory factory = SessionFactory.newFactory();
			final SystemTierType systemTierType = SystemTierType.tierFromString(tierItem.getTierName());
			
			TierDescription depTierDesc  = null;
			if(systemTierType != null) {
				depTierDesc = factory.createTierDescription(systemTierType.getName(), systemTierType.isGrouped());
			} else {
				final SessionEditor editor = getEditor();
				final Session session = editor.getSession();
				
				for(int i = 0; i < session.getUserTierCount(); i++) {
					final TierDescription td = session.getUserTier(i);
					if(td.getName().equals(tierItem.getTierName())) {
						depTierDesc = td;
						break;
					}
				}
			}
			
			if(depTierDesc != null) {
				final Font transcriptFont = 
						(tierItem.getTierFont().equals("default") ? 
								FontPreferences.getTierFont() :
									Font.decode(tierItem.getTierFont()));

				TierEditorDialog tierDialog = new TierEditorDialog(true);
				TierInfoEditor tierEditor = tierDialog.getTierEditor();
				tierEditor.setGrouped(depTierDesc.isGrouped());
				tierEditor.setTierName(tierItem.getTierName());
				tierEditor.setTierFont(transcriptFont);
				
				tierDialog.add(tierEditor);
				tierDialog.setTitle("New Tier");
				tierDialog.setModal(true);
				tierDialog.pack();
				
				if(tierDialog.showDialog()) {
					final CompoundEdit edit = new CompoundEdit();
					// change of tier name
					if(!depTierDesc.getName().equals(tierEditor.getTierName())) {
						String oldTierName = depTierDesc.getName();
						
						final TierNameEdit tierNameEdit = new TierNameEdit(getEditor(), tierEditor.getTierName(), oldTierName);
						tierNameEdit.doIt();
						edit.addEdit(tierNameEdit);
					}
					
					final Formatter<Font> fontFormatter = FormatterFactory.createFormatter(Font.class);
					final String fontString = fontFormatter.format(tierEditor.getTierFont());
					final TierViewItem newViewItem = factory.createTierViewItem(
							tierEditor.getTierName(), tierEditor.isVisible(), fontString, tierItem.isTierLocked());
					final TierViewItemEdit tierViewItemEdit = new TierViewItemEdit(getEditor(), tierItem, newViewItem);
					tierViewItemEdit.doIt();
					edit.addEdit(tierViewItemEdit);
					
					edit.end();
					getEditor().getUndoSupport().postEdit(edit);
				} // if (showDialog())
			} // if (depTierDesc != null)
	}

}
