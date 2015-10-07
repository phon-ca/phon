package ca.phon.opgraph.editor.actions.graph;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Collection;

import javax.swing.KeyStroke;

import ca.gedge.opgraph.OpNode;
import ca.gedge.opgraph.app.GraphDocument;
import ca.gedge.opgraph.app.edits.graph.AutoLayoutEdit;
import ca.gedge.opgraph.app.edits.graph.DeleteNodesEdit;
import ca.phon.opgraph.editor.OpgraphEditor;
import ca.phon.opgraph.editor.actions.OpgraphEditorAction;

public class AutoLayoutAction extends OpgraphEditorAction {

	private static final long serialVersionUID = -2540261955364824184L;
	
	public final static String TXT = "Layout nodes";
	
	public final static String DESC = "Automatically layout nodes in graph";
	
	public final static KeyStroke KS = KeyStroke.getKeyStroke(KeyEvent.VK_L, 
			Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());

	public AutoLayoutAction(OpgraphEditor editor) {
		super(editor);
		
		putValue(NAME, TXT);
		putValue(SHORT_DESCRIPTION, DESC);
		putValue(ACCELERATOR_KEY, KS);
	}

	@Override
	public void hookableActionPerformed(ActionEvent arg0) {
		final GraphDocument document = getEditor().getModel().getDocument();
		if(document != null) {
			document.getUndoSupport().postEdit(new AutoLayoutEdit(document.getGraph()));
		}
	}

}
