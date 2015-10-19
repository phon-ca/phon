package ca.phon.opgraph.editor.actions.graph;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Collection;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.KeyStroke;

import ca.gedge.opgraph.OpNode;
import ca.gedge.opgraph.app.AutoLayoutManager;
import ca.gedge.opgraph.app.GraphDocument;
import ca.gedge.opgraph.app.edits.graph.DeleteNodesEdit;
import ca.phon.opgraph.editor.OpgraphEditor;
import ca.phon.opgraph.editor.actions.OpgraphEditorAction;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;

public class AutoLayoutAction extends OpgraphEditorAction {

	private static final long serialVersionUID = -2540261955364824184L;
	
	public final static String TXT = "Layout nodes";
	
	public final static String DESC = "Automatically layout nodes in graph";
	
	public final static KeyStroke KS = KeyStroke.getKeyStroke(KeyEvent.VK_L, 
			Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
	
	public final static ImageIcon ICON = 
			IconManager.getInstance().getIcon("actions/distribute-horizontal-margin", IconSize.SMALL);

	public AutoLayoutAction(OpgraphEditor editor) {
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
			final AutoLayoutManager layoutManager = new AutoLayoutManager();
			final JComponent canvasView = getEditor().getModel().getView("Canvas");
			layoutManager.setPreferredWidth(canvasView.getSize().width);
			layoutManager.layoutGraph(document.getGraph());
			document.getUndoSupport().postEdit(layoutManager.getUndoableEdit());
		}
	}

}
