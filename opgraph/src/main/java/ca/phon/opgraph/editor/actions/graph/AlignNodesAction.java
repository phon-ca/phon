package ca.phon.opgraph.editor.actions.graph;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Collection;

import javax.swing.ImageIcon;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;

import ca.gedge.opgraph.OpNode;
import ca.gedge.opgraph.app.GraphDocument;
import ca.gedge.opgraph.app.edits.graph.AlignNodesEdit;
import ca.phon.opgraph.editor.OpgraphEditor;
import ca.phon.opgraph.editor.actions.OpgraphEditorAction;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;

public class AlignNodesAction extends OpgraphEditorAction {

	private static final long serialVersionUID = 7052293033950635497L;

	private final int side;
	
	public AlignNodesAction(OpgraphEditor editor, int side) {
		super(editor);
		
		this.side = side;
		
		putValue(NAME, getName());
		putValue(ACCELERATOR_KEY, getKeystroke());
		putValue(SMALL_ICON, getIcon());
	}
	
	public KeyStroke getKeystroke() {
		int key = 0;
		switch(side) {
		case SwingConstants.TOP:
			key = KeyEvent.VK_UP;
			break;
			
		case SwingConstants.BOTTOM:
			key = KeyEvent.VK_DOWN;
			break;
			
		case SwingConstants.LEFT:
			key = KeyEvent.VK_LEFT;
			break;
			
		case SwingConstants.RIGHT:
			key = KeyEvent.VK_RIGHT;
			break;
		}
		return KeyStroke.getKeyStroke(key, KeyEvent.ALT_MASK);
	}
	
	public ImageIcon getIcon() {
		ImageIcon retVal = null;
		switch(side) {
		case SwingConstants.TOP:
			retVal = IconManager.getInstance().getIcon("actions/align-vertical-top-2", IconSize.SMALL);
			break;
			
		case SwingConstants.BOTTOM:
			retVal = IconManager.getInstance().getIcon("actions/align-vertical-bottom-2", IconSize.SMALL);
			break;
			
		case SwingConstants.LEFT:
			retVal = IconManager.getInstance().getIcon("actions/align-horizontal-left", IconSize.SMALL);
			break;
			
		case SwingConstants.RIGHT:
			retVal = IconManager.getInstance().getIcon("actions/align-horizontal-right-2", IconSize.SMALL);
			break;
		}
		return retVal;
	}
	
	public String getName() {
		final StringBuffer sb = new StringBuffer();
		sb.append("Align ");
		switch(side) {
		case SwingConstants.TOP:
			sb.append("top");
			break;
			
		case SwingConstants.BOTTOM:
			sb.append("bottom");
			break;
			
		case SwingConstants.LEFT:
			sb.append("left");
			break;
			
		case SwingConstants.RIGHT:
			sb.append("right");
			break;
			
		default:
			break;
		}
		return sb.toString();
	}

	@Override
	public void hookableActionPerformed(ActionEvent arg0) {
		final GraphDocument document = getEditor().getModel().getDocument();
		if(document != null) {
			final Collection<OpNode> selectedNodes = document.getSelectionModel().getSelectedNodes();
			if(selectedNodes.size() > 1) {
				document.getUndoSupport().postEdit(new AlignNodesEdit(selectedNodes, side));
			}
		}
	}

}
