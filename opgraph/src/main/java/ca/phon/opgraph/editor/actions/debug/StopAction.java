package ca.phon.opgraph.editor.actions.debug;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.ImageIcon;
import javax.swing.KeyStroke;

import ca.gedge.opgraph.app.GraphDocument;
import ca.phon.opgraph.editor.OpgraphEditor;
import ca.phon.opgraph.editor.actions.OpgraphEditorAction;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;

public class StopAction extends OpgraphEditorAction {

	private static final long serialVersionUID = -3449043672596585549L;

	public static final String TXT = "Stop";
	
	public static final String DESC = "Stop debugger";
	
	public final static KeyStroke KS = KeyStroke.getKeyStroke(KeyEvent.VK_F9, 0);
	
	public final static ImageIcon ICON = 
			IconManager.getInstance().getIcon("actions/media-playback-stop-7", IconSize.SMALL);
	
	public StopAction(OpgraphEditor editor) {
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
			document.setProcessingContext(null);
		}
	}

}
