package ca.phon.opgraph.editor.actions.file;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import ca.phon.opgraph.editor.OpgraphEditor;
import ca.phon.opgraph.editor.actions.OpgraphEditorAction;
import ca.phon.ui.toast.ToastFactory;

public class SaveAction extends OpgraphEditorAction {
	
	private final static Logger LOGGER = Logger.getLogger(SaveAction.class.getName());

	private static final long serialVersionUID = -3563703815236430754L;

	public SaveAction(OpgraphEditor editor) {
		super(editor);
		
		putValue(NAME, "Save");
		putValue(SHORT_DESCRIPTION, "Save graph");
	}

	@Override
	public void hookableActionPerformed(ActionEvent arg0) {
		// call save on the editor
		try {
			getEditor().saveData();
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
			ToastFactory.makeToast(e.getLocalizedMessage()).start(getEditor());
			Toolkit.getDefaultToolkit().beep();
		}
	}

}
