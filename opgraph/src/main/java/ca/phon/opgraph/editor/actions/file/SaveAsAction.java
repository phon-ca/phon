package ca.phon.opgraph.editor.actions.file;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.KeyStroke;

import ca.phon.opgraph.editor.OpgraphEditor;
import ca.phon.opgraph.editor.actions.OpgraphEditorAction;
import ca.phon.ui.toast.ToastFactory;

public class SaveAsAction extends OpgraphEditorAction {
	
	private final static Logger LOGGER = Logger.getLogger(SaveAsAction.class.getName());

	private static final long serialVersionUID = -3563703815236430754L;
	
	public final static String TXT = "Save as...";
	
	public final static String DESC = "Save graph to file";
	
	public final static KeyStroke KS = KeyStroke.getKeyStroke(KeyEvent.VK_S,
			Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() | KeyEvent.SHIFT_MASK);

	public SaveAsAction(OpgraphEditor editor) {
		super(editor);
	
		putValue(NAME, TXT);
		putValue(SHORT_DESCRIPTION, DESC);
		putValue(ACCELERATOR_KEY, KS);
	}

	@Override
	public void hookableActionPerformed(ActionEvent arg0) {
		if(getEditor().chooseFile()) {
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

}
