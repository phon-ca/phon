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
package ca.phon.app.session.editor.view.tier_management.actions;

import java.awt.*;
import java.awt.event.*;

import javax.swing.undo.*;

import ca.phon.app.session.editor.*;
import ca.phon.app.session.editor.undo.*;
import ca.phon.app.session.editor.view.tier_management.*;
import ca.phon.formatter.*;
import ca.phon.session.*;
import ca.phon.ui.fonts.*;

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
