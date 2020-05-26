package ca.phon.app.opgraph.editor.actions.debug;

import java.awt.event.ActionEvent;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

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

public class StepToAction extends OpgraphDebugAction {

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
		
		Stack<OpNode> nodeStack = new Stack<OpNode>();
		List<OpNode> nodePath = document.getRootGraph().getNodePath(node.getId());
		Collections.reverse(nodePath);
		nodePath.forEach(nodeStack::add);
		
		Runnable inBg =	new Runnable() {
			public void run() {
				if(document != null && node != null) {
					final Processor context = getProcessor(document);
					while(!nodeStack.isEmpty() && context.hasNext()) {
						if(context.getNodeToProcess() == nodeStack.peek()) {
							nodeStack.pop();
							if(!nodeStack.isEmpty()) {
								context.stepInto();
							}
						} else {
							context.step();
						}
					}
					SwingUtilities.invokeLater( () -> {
						document.updateDebugState(context);
						getEditor().getModel().getCanvas().updateDebugState(context);
					});
				}
			}
		};
		getOpgraphThread().invokeLater(inBg);
	}

}
