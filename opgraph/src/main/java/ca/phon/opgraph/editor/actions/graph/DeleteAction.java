package ca.phon.opgraph.editor.actions.graph;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Collection;

import javax.swing.ImageIcon;
import javax.swing.KeyStroke;

import ca.gedge.opgraph.OpNode;
import ca.gedge.opgraph.app.GraphDocument;
import ca.gedge.opgraph.app.edits.graph.DeleteNodesEdit;
import ca.phon.opgraph.editor.OpgraphEditor;
import ca.phon.opgraph.editor.actions.OpgraphEditorAction;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;

public class DeleteAction extends OpgraphEditorAction {

	private static final long serialVersionUID = -2540261955364824184L;
	
	public final static String TXT = "Delete nodes";
	
	public final static String DESC = "Delete selected nodes";
	
	public final static KeyStroke KS = KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0);
	
	public final static ImageIcon ICON = 
			IconManager.getInstance().getIcon("actions/format-remove-node", IconSize.SMALL);

	public DeleteAction(OpgraphEditor editor) {
		super(editor);
		
		putValue(NAME, TXT);
		putValue(SHORT_DESCRIPTION, DESC);
		putValue(ACCELERATOR_KEY, KS);
		putValue(SMALL_ICON, ICON);
	}

	@Override
	public void hookableActionPerformed(ActionEvent arg0) {
		final GraphDocument document = getEditor().getModel().getDocument();
		if(document != null) {
			final Collection<OpNode> nodes = document.getSelectionModel().getSelectedNodes();
			document.getUndoSupport().postEdit(new DeleteNodesEdit(document.getGraph(), nodes));
		}
	}

}
