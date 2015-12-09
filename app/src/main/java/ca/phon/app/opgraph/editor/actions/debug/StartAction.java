package ca.phon.app.opgraph.editor.actions.debug;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.ImageIcon;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import ca.gedge.opgraph.Processor;
import ca.gedge.opgraph.ProcessorEvent;
import ca.gedge.opgraph.app.GraphDocument;
import ca.gedge.opgraph.exceptions.ProcessingException;
import ca.phon.app.opgraph.editor.OpgraphEditor;
import ca.phon.app.opgraph.editor.actions.OpgraphEditorAction;
import ca.phon.app.opgraph.wizard.NodeWizard;
import ca.phon.app.opgraph.wizard.WizardExtension;
import ca.phon.ui.wizard.WizardFrame;
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
				context.addProcessorListener( (pe) -> {
					SwingUtilities.invokeLater( () -> {
					if(pe.getType() == ProcessorEvent.Type.BEGIN_NODE) {
						getEditor().getStatusBar().getProgressBar().setIndeterminate(true);
						getEditor().getStatusBar().getProgressLabel().setText(pe.getNode().getName());
					} else if(pe.getType() == ProcessorEvent.Type.FINISH_NODE) {
						getEditor().getStatusBar().getProgressBar().setIndeterminate(false);
					} else if(pe.getType() == ProcessorEvent.Type.COMPLETE) {
						getEditor().getStatusBar().getProgressBar().setIndeterminate(false);
						getEditor().getStatusBar().getProgressLabel().setText("");
						(new StopAction(getEditor())).actionPerformed(arg0);
					}
					});
				});
				
				context.getContext().setDebug(true);
				getEditor().getModel().setupContext(context.getContext());
				
				final WizardExtension wizardExt = document.getGraph().getExtension(WizardExtension.class);
				if(wizardExt != null) {
					final NodeWizard nodeWizard = wizardExt.createWizard(context);
					nodeWizard.pack();
					nodeWizard.setSize(1024, 768);
					nodeWizard.setVisible(true);
				} else {
					if(context.hasNext()) {
						try {
							context.stepAll();
							SwingUtilities.invokeLater( () -> document.updateDebugState(context) );
						} catch (ProcessingException pe) {
							document.updateDebugState(
									(pe.getContext() != null ? pe.getContext() : context));
							// bring Debug view to front
							getEditor().showView("Debug");
						} 
					}
				}
			}
		};
		PhonWorker.getInstance().invokeLater(inBg);
	}

}
