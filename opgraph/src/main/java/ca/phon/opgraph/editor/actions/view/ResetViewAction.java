package ca.phon.opgraph.editor.actions.view;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.KeyStroke;

import ca.phon.opgraph.editor.OpgraphEditor;
import ca.phon.opgraph.editor.actions.OpgraphEditorAction;

public class ResetViewAction extends OpgraphEditorAction {

	private static final long serialVersionUID = 2681118382414781020L;

	public final static String TXT = "Reset view layout";
	
	public final static String DESC = "Rest view layout to default";
	
	public final static KeyStroke KS = KeyStroke.getKeyStroke(KeyEvent.VK_R,
			Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() | KeyEvent.ALT_MASK);
	
	public ResetViewAction(OpgraphEditor editor) {
		super(editor);
		
		putValue(NAME, TXT);
		putValue(SHORT_DESCRIPTION, DESC);
		putValue(ACCELERATOR_KEY, KS);
	}

	@Override
	public void hookableActionPerformed(ActionEvent arg0) {
		getEditor().resetView();
	}

}
