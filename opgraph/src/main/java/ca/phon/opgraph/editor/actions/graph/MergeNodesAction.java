package ca.phon.opgraph.editor.actions.graph;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Collection;

import javax.swing.ImageIcon;
import javax.swing.KeyStroke;

import ca.gedge.opgraph.OpNode;
import ca.gedge.opgraph.app.GraphDocument;
import ca.gedge.opgraph.app.components.canvas.GraphCanvasSelectionModel;
import ca.gedge.opgraph.nodes.menu.edits.CreateMacroEdit;
import ca.phon.opgraph.editor.OpgraphEditor;
import ca.phon.opgraph.editor.actions.OpgraphEditorAction;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;

/**
 * Create a new macro node from selection
 *
 */
public class MergeNodesAction extends OpgraphEditorAction {

	private static final long serialVersionUID = -3332016622776612070L;
	
	private final static String TXT = "Merge nodes";
	
	private final static String DESC = "Merge selected nodes into new macro node";
	
	private final static KeyStroke KS = 
			KeyStroke.getKeyStroke(KeyEvent.VK_M, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() | KeyEvent.SHIFT_MASK);
	
	private final static ImageIcon ICON =
			IconManager.getInstance().getIcon("actions/format-join-node", IconSize.SMALL);

	public MergeNodesAction(OpgraphEditor editor) {
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
			final GraphCanvasSelectionModel selectionModel = document.getSelectionModel();
			final Collection<OpNode> selectedNodes = selectionModel.getSelectedNodes();
			document.getUndoSupport().postEdit(new CreateMacroEdit(document.getGraph(), selectedNodes));
		}
	}

}
