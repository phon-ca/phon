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
