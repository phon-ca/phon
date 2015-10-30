package ca.phon.opgraph.editor.actions.debug;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.ImageIcon;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import ca.gedge.opgraph.Processor;
import ca.gedge.opgraph.app.GraphDocument;
import ca.phon.opgraph.editor.OpgraphEditor;
import ca.phon.opgraph.editor.actions.OpgraphEditorAction;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;
import ca.phon.worker.PhonWorker;

public class StepAction extends OpgraphEditorAction {

	private static final long serialVersionUID = 173598233933353961L;

	public static final String TXT = "Step";
	
	public static final String DESC = "Step to next node";
	
	public final static KeyStroke KS = KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT,
			Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
	
	public final static ImageIcon ICON = 
			IconManager.getInstance().getIcon("actions/debug-step-over", IconSize.SMALL);
	
	public StepAction(OpgraphEditor editor) {
		super(editor);
		
		putValue(NAME, TXT);
		putValue(SHORT_DESCRIPTION, DESC);
		putValue(SMALL_ICON, ICON);
		putValue(ACCELERATOR_KEY, KS);
	}

	@Override
	public void hookableActionPerformed(ActionEvent arg0) {
		final GraphDocument document = getEditor().getModel().getDocument();
		Runnable inBg = () -> {
			if(document != null) {
				final Processor context =
						(document.getProcessingContext() != null 
							? document.getProcessingContext() 
							: new Processor(document.getGraph()));
				document.setProcessingContext(context);
				
				context.getContext().setDebug(true);
				getEditor().getModel().setupContext(context.getContext());
	
				if(context.hasNext()) {
					context.step();
					
					SwingUtilities.invokeLater( () -> document.updateDebugState(context) );
				}
			}
		};
		PhonWorker.getInstance().invokeLater(inBg);
	}

}
