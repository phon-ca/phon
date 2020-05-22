package ca.phon.app.opgraph.editor.actions.debug;

import java.awt.event.ActionEvent;

import javax.swing.SwingUtilities;

import ca.phon.app.opgraph.editor.OpgraphEditor;
import ca.phon.app.opgraph.editor.actions.OpgraphEditorAction;
import ca.phon.app.opgraph.wizard.NodeWizard;
import ca.phon.app.opgraph.wizard.WizardExtension;
import ca.phon.opgraph.OpNode;
import ca.phon.opgraph.Processor;
import ca.phon.opgraph.ProcessorEvent;
import ca.phon.opgraph.app.GraphDocument;
import ca.phon.opgraph.exceptions.ProcessingException;
import ca.phon.worker.PhonWorker;

public class StepToAction extends OpgraphEditorAction {

	private OpNode stepToNode = null;
	
	private final static String TXT = "Step to current node";
	
	public StepToAction(OpgraphEditor editor) {
		this(editor, null);
	}
	
	public StepToAction(OpgraphEditor editor, OpNode stepToNode) {
		super(editor);
		
		this.stepToNode = stepToNode;
		
		putValue(NAME, TXT);
	}
	
	
	@Override
	public void hookableActionPerformed(ActionEvent arg0) {
		final GraphDocument document = getEditor().getModel().getDocument();
		final OpNode node = (stepToNode != null ? stepToNode : getEditor().getModel().getCanvas().getSelectionModel().getSelectedNode());
		Runnable inBg = () -> {
			if(document != null) {
				final Processor context = 
						(document.getProcessingContext() == null ? new Processor(document.getRootGraph()) : document.getProcessingContext());
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
				
//				final WizardExtension wizardExt = document.getRootGraph().getExtension(WizardExtension.class);
//				if(wizardExt != null) {
//					final NodeWizard nodeWizard = wizardExt.createWizard(context);
//					nodeWizard.pack();
//					nodeWizard.setSize(1024, 768);
//					nodeWizard.setVisible(true);
//				} else {
					if(context.hasNext()) {
						try {
							context.stepToNode(node);
							//SwingUtilities.invokeLater( () -> document.updateDebugState(context) );
						} catch (ProcessingException pe) {
							document.updateDebugState(
									(pe.getContext() != null ? pe.getContext() : context));
							// bring Debug view to front
							getEditor().showView("Debug");
						} 
					}
				}
//			}
		};
		if(node != null) 
			PhonWorker.getInstance().invokeLater(inBg);
	}

}
