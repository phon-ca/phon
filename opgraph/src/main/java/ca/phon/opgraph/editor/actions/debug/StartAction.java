package ca.phon.opgraph.editor.actions.debug;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.ImageIcon;
import javax.swing.KeyStroke;

import ca.gedge.opgraph.Processor;
import ca.gedge.opgraph.app.GraphDocument;
import ca.gedge.opgraph.exceptions.BreakpointEncountered;
import ca.gedge.opgraph.exceptions.ProcessingException;
import ca.phon.opgraph.editor.OpgraphEditor;
import ca.phon.opgraph.editor.actions.OpgraphEditorAction;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;

public class StartAction extends OpgraphEditorAction {

	private static final long serialVersionUID = 954743923704331642L;

	public static final String TXT = "Run";
	
	public static final String DESC = "Start/continue graph execution";
	
	public static final KeyStroke KS = KeyStroke.getKeyStroke(KeyEvent.VK_F7, 0);
	
	public static final ImageIcon ICON = 
			IconManager.getInstance().getIcon("actions/system-run-5", IconSize.SMALL);
	
	public StartAction(OpgraphEditor editor) {
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
			Processor context = document.getProcessingContext();
			if(context == null) {
				context = new Processor(document.getGraph());
				document.setProcessingContext(context);
			}
			context.getContext().setDebug(true);

			if(context.hasNext()) {
				try {
					context.stepAll();
					document.updateDebugState(context);
				} catch (ProcessingException pe) {
					document.updateDebugState(
							(pe.getContext() != null ? pe.getContext() : context));
				} 
			}
		}
	}

}
