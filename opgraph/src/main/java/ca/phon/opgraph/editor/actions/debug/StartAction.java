package ca.phon.opgraph.editor.actions.debug;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.ImageIcon;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import ca.gedge.opgraph.Processor;
import ca.gedge.opgraph.app.GraphDocument;
import ca.gedge.opgraph.exceptions.ProcessingException;
import ca.phon.opgraph.editor.OpgraphEditor;
import ca.phon.opgraph.editor.actions.OpgraphEditorAction;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;
import ca.phon.worker.PhonWorker;

public class StartAction extends OpgraphEditorAction {

	private static final long serialVersionUID = 954743923704331642L;

	public static final String TXT = "Run";
	
	public static final String DESC = "Start/continue graph execution";
	
	public static final KeyStroke KS = KeyStroke.getKeyStroke(KeyEvent.VK_F7, 0);
	
	public static final ImageIcon ICON = 
			IconManager.getInstance().getIcon("actions/media-playback-start-7", IconSize.SMALL);
	
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
		Runnable inBg = () -> {
			if(document != null) {
				final Processor context = 
						(document.getProcessingContext() == null ? new Processor(document.getGraph()) : document.getProcessingContext());
				document.setProcessingContext(context);
				
				context.getContext().setDebug(true);
				getEditor().getModel().setupContext(context.getContext());
				
				if(context.hasNext()) {
					try {
						context.stepAll();
						SwingUtilities.invokeLater( () -> document.updateDebugState(context) );
					} catch (ProcessingException pe) {
						document.updateDebugState(
								(pe.getContext() != null ? pe.getContext() : context));
					} 
				}
			}
		};
		PhonWorker.getInstance().invokeLater(inBg);
	}

}
