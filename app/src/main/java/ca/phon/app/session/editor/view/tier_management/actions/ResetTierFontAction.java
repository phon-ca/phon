/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2015 The Phon Project, Memorial University <https://phon.ca>
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

import java.awt.event.ActionEvent;

import ca.phon.app.session.editor.SessionEditor;
import ca.phon.app.session.editor.undo.TierViewItemEdit;
import ca.phon.app.session.editor.view.tier_management.TierOrderingEditorView;
import ca.phon.session.SessionFactory;
import ca.phon.session.TierViewItem;

public class ResetTierFontAction extends TierManagementAction {

	private static final long serialVersionUID = -8360846647372033809L;
	
	private final static String CMD_NAME = "Reset font";
	
	private final static String SHORT_DESC = "Reset tier font to default";
	
	private final TierViewItem item;

	public ResetTierFontAction(SessionEditor editor, TierOrderingEditorView view,
			TierViewItem item) {
		super(editor, view);
		this.item = item;
		
		putValue(NAME, CMD_NAME);
		putValue(SHORT_DESCRIPTION, SHORT_DESC);
	}

	@Override
	public void hookableActionPerformed(ActionEvent e) {
		final SessionFactory factory = SessionFactory.newFactory();
		final TierViewItem newItem = factory.createTierViewItem(item.getTierName(), item.isVisible(), "default", item.isTierLocked());
		
		final TierViewItemEdit edit = new TierViewItemEdit(getEditor(), item, newItem);
		getEditor().getUndoSupport().postEdit(edit);
	}

}
