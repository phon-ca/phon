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
package ca.phon.app.session.editor.view.record_data.actions;

import java.awt.event.ActionEvent;

import javax.swing.undo.CompoundEdit;

import ca.phon.app.session.editor.undo.MergeGroupEdit;
import ca.phon.app.session.editor.view.record_data.RecordDataEditorView;
import ca.phon.session.Record;
import ca.phon.ui.nativedialogs.MessageDialogProperties;
import ca.phon.ui.nativedialogs.NativeDialogs;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;

public class MergeAllGroupsCommand extends RecordDataEditorViewAction {

	private static final long serialVersionUID = 7674607989381956414L;

	private final static String ICON = "actions/group_merge";

	private final RecordDataEditorView editor;
	
	public MergeAllGroupsCommand(RecordDataEditorView editor) {
		super(editor);
		this.editor = editor;

		putValue(NAME, "Merge all groups");
		putValue(SMALL_ICON, IconManager.getInstance().getIcon(ICON, IconSize.SMALL));
	}
		
	@Override
	public void actionPerformed(ActionEvent e) {
		final Record r = getRecord();
		if(r.numberOfGroups() > 1) {
			// confirm
			final MessageDialogProperties props = new MessageDialogProperties();
			props.setRunAsync(false);
			props.setTitle("Merge All Groups");
			props.setHeader("Merge all groups");
			props.setMessage("Merge all groups for current record?");
			props.setOptions(MessageDialogProperties.okCancelOptions);
			final int retVal = NativeDialogs.showMessageDialog(props);
			if(retVal != 0) return;
			
			final CompoundEdit cmpEdit = new CompoundEdit(){

				@Override
				public String getUndoPresentationName() {
					return "Undo merge all groups";
				}

				@Override
				public String getRedoPresentationName() {
					return "Redo merge all groups";
				}
				
			};
			while(r.numberOfGroups() > 1) {
				final MergeGroupEdit edit = new MergeGroupEdit(getEditorView().getEditor(), r, 0);
				edit.doIt();
				cmpEdit.addEdit(edit);
			}
			cmpEdit.end();
			
			getEditorView().getEditor().getUndoSupport().postEdit(cmpEdit);
		}
	}

}
