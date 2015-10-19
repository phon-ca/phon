package ca.phon.opgraph.editor.actions.graph;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.ImageIcon;
import javax.swing.KeyStroke;

import ca.gedge.opgraph.OpNode;
import ca.gedge.opgraph.app.GraphDocument;
import ca.gedge.opgraph.app.components.canvas.GraphCanvasSelectionModel;
import ca.gedge.opgraph.nodes.general.MacroNode;
import ca.gedge.opgraph.nodes.menu.edits.ExplodeMacroEdit;
import ca.phon.opgraph.editor.OpgraphEditor;
import ca.phon.opgraph.editor.actions.OpgraphEditorAction;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;

public class ExpandMacroAction extends OpgraphEditorAction {

	private static final long serialVersionUID = -8878893185206047166L;
	
	private final static String TXT = "Expand macro";
	
	private final static String DESC = "Replace selected macro node with subgraph contents";
	
	private final static KeyStroke KS = 
			KeyStroke.getKeyStroke(KeyEvent.VK_E, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() | KeyEvent.SHIFT_MASK);
	
	private final static ImageIcon ICON =
			IconManager.getInstance().getIcon("actions/format-break-node", IconSize.SMALL);

	public ExpandMacroAction(OpgraphEditor editor) {
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
			final OpNode selected = selectionModel.getSelectedNode();
			if(selected != null && (selected instanceof MacroNode))
				document.getUndoSupport().postEdit(new ExplodeMacroEdit(document.getGraph(), (MacroNode)selected));
		}
	}

}
