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

import java.awt.event.*;

import javax.swing.*;

import org.apache.commons.lang3.*;

import ca.phon.app.session.editor.*;
import ca.phon.app.session.editor.undo.*;
import ca.phon.app.session.editor.view.tier_management.*;
import ca.phon.session.*;
import ca.phon.ui.toast.*;
import ca.phon.util.icons.*;

public class NewTierAction extends TierManagementAction {

	private static final long serialVersionUID = -25622911440669271L;
	
	private final static String CMD_NAME = "New tier...";
	
	private final static String SHORT_DESC = "Add new tier to session.";
	
	private final static ImageIcon ICON =
			IconManager.getInstance().getIcon("actions/list-add", IconSize.SMALL);

	public NewTierAction(SessionEditor editor, TierOrderingEditorView view) {
		super(editor, view);
		
		putValue(NAME, CMD_NAME);
		putValue(SMALL_ICON, ICON);
		putValue(SHORT_DESCRIPTION, SHORT_DESC);
	}

	@Override
	public void hookableActionPerformed(ActionEvent e) {
		TierEditorDialog newTierDialog = new TierEditorDialog(false);
		TierInfoEditor tierEditor = newTierDialog.getTierEditor();
		newTierDialog.add(tierEditor);
		newTierDialog.setTitle("New Tier");
		newTierDialog.setModal(true);
		newTierDialog.pack();
		
		if(newTierDialog.showDialog()) {
			final SessionEditor editor = getEditor();
			final Session session = editor.getSession();
			// get tier info
			String tierName = tierEditor.getTierName();
			tierName = StringUtils.strip(tierName);
			if(tierName.length() == 0) {
				return;
			}
			
			boolean tierExists = false;
			if(SystemTierType.isSystemTier(tierName)) {
				tierExists = true;
			} else {
				for(TierDescription td:session.getUserTiers()) {
					if(td.getName().equals(tierName)) {
						tierExists = true;
						break;
					}
				}
			}
			
			if(tierExists){
				final Toast toast = ToastFactory.makeToast("A tier with name " + tierEditor.getTierName() + " already exists.");
				toast.start(tierEditor);
				return;
			}
			
			// create tier
			final TierDescription tierDescription = tierEditor.createTierDescription();
			final TierViewItem tierViewItem = tierEditor.createTierViewItem();
			
			final AddTierEdit edit = new AddTierEdit(editor, tierDescription, tierViewItem);
			editor.getUndoSupport().postEdit(edit);
		}
	}

}
