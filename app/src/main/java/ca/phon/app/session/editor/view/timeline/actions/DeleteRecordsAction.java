package ca.phon.app.session.editor.view.timeline.actions;

import ca.phon.app.session.editor.SessionEditor;
import ca.phon.app.session.editor.actions.DeleteRecordAction;
import ca.phon.app.session.editor.undo.DeleteRecordEdit;
import ca.phon.app.session.editor.view.timeline.TimelineRecordTier;
import ca.phon.app.session.editor.view.timeline.TimelineView;
import ca.phon.ui.nativedialogs.MessageDialogProperties;
import ca.phon.ui.nativedialogs.NativeDialogs;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;

import java.awt.event.ActionEvent;

public class DeleteRecordsAction extends TimelineAction  {

    private final static String CMD_NAME = "Delete record(s)";

    private final static String SHORT_DESC = "Delete currently selected records";

    private final static String ICON = "misc/record-delete";

    public DeleteRecordsAction(TimelineView view) {
        super(view);

        putValue(NAME, CMD_NAME);
        putValue(SHORT_DESCRIPTION, SHORT_DESC);
        putValue(SMALL_ICON, IconManager.getInstance().getIcon(ICON, IconSize.SMALL));
    }

    @Override
    public void hookableActionPerformed(ActionEvent ae) {
        TimelineRecordTier recordTier = getView().getRecordTier();
        SessionEditor editor = getView().getEditor();

        int[] recordsToDelete = recordTier.getSelectionModel().getSelectedIndices();

        final MessageDialogProperties props = new MessageDialogProperties();
        props.setRunAsync(false);
        props.setParentWindow(editor);
        props.setTitle("Delete record" + (recordsToDelete.length > 0 ? "s" : ""));
        props.setHeader("Confirm delete record" + (recordsToDelete.length > 0 ? "s" : ""));
        props.setMessage("Delete record " + String.format("%d", editor.getCurrentRecordIndex()+1)
                + (recordsToDelete.length > 0 ? String.format("and %d others", recordsToDelete.length-1) : "") + "?");
        props.setOptions(MessageDialogProperties.okCancelOptions);
        int retVal = NativeDialogs.showMessageDialog(props);
        if(retVal == 1) return;

        editor.getUndoSupport().beginUpdate();
        for(int i = recordsToDelete.length-1; i >= 0; i--) {
            int recordIdx = recordsToDelete[i];
            final DeleteRecordEdit edit = new DeleteRecordEdit(editor, recordIdx);
            editor.getUndoSupport().postEdit(edit);
        }
        editor.getUndoSupport().endUpdate();
    }

}
