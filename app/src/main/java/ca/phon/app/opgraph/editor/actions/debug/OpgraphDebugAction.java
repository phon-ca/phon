package ca.phon.app.opgraph.editor.actions.debug;

import java.util.*;

import javax.swing.*;

import ca.phon.app.opgraph.editor.*;
import ca.phon.app.opgraph.editor.actions.*;
import ca.phon.extensions.*;
import ca.phon.opgraph.*;
import ca.phon.opgraph.app.*;
import ca.phon.worker.*;

public abstract class OpgraphDebugAction extends OpgraphEditorAction {

	public OpgraphDebugAction(OpgraphEditor editor) {
		super(editor);
	}

	/**
	 * Return the opgraph execution thread for the debug action.
	 * 
	 * If the thread does not exist it is created.
	 * 
	 * @return opgraph exec thread
	 */
	protected PhonWorker getOpgraphThread() {
		Optional<PhonWorker> workerOptional = ExtensionSupport.getOptionalExtension(getEditor(), PhonWorker.class);
		if(workerOptional.isEmpty()) {
			PhonWorker opgraphWorker = PhonWorker.createWorker();
			opgraphWorker.setName("Opgraph Worker");
			opgraphWorker.setFinishWhenQueueEmpty(false);
			opgraphWorker.start();
			
			getEditor().putExtension(PhonWorker.class, opgraphWorker);
			return opgraphWorker;
		} else {
			return workerOptional.get();
		}
	}
	
	/**
	 * Get current processor for given document
	 * 
	 * @return processor
	 */
	protected Processor getProcessor(GraphDocument document) {
		if(document.getProcessingContext() == null) {
			final Processor ctx = new Processor(document.getRootGraph());
			document.setProcessingContext(ctx);
			ctx.getContext().setDebug(true);
			SwingUtilities.invokeLater( () -> getEditor().getModel().setupContext(ctx.getContext()) );
		}
		return document.getProcessingContext();
	}
	
}
