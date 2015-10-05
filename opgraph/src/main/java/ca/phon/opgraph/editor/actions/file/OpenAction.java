package ca.phon.opgraph.editor.actions.file;

import java.awt.event.ActionEvent;

import ca.phon.opgraph.editor.OpgraphEditor;
import ca.phon.opgraph.editor.OpgraphFileFilter;
import ca.phon.opgraph.editor.actions.OpgraphEditorAction;
import ca.phon.ui.nativedialogs.FileFilter;
import ca.phon.ui.nativedialogs.NativeDialogEvent;
import ca.phon.ui.nativedialogs.OpenDialogProperties;

public class OpenAction extends OpgraphEditorAction {

	private static final long serialVersionUID = 1416397464535529114L;
	
	private final static String TXT = "Open...";
	
	private final static String DESC = "Open graph";

	public OpenAction(OpgraphEditor editor) {
		super(editor);
		
		putValue(NAME, TXT);
		putValue(SHORT_DESCRIPTION, DESC);
	}

	@Override
	public void hookableActionPerformed(ActionEvent arg0) {
		final FileFilter[] filters = new FileFilter[] { new OpgraphFileFilter(), FileFilter.allFilesFilter };
		final OpenDialogProperties props = new OpenDialogProperties();
		props.setParentWindow(getEditor());
		props.setCanChooseFiles(true);
		props.setCanChooseDirectories(false);
		props.setAllowMultipleSelection(false);
		props.setTitle("Open Graph");
		props.setListener( (e) -> {
			if(e.getDialogResult() == NativeDialogEvent.OK_OPTION) {
				
			}
		});
	}

}
