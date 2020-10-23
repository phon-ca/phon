/*
 * Copyright (C) 2005-2020 Gregory Hedlund & Yvan Rose
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at

 *    http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
