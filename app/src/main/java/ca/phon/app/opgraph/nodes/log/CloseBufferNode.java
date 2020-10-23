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
package ca.phon.app.opgraph.nodes.log;

import java.lang.reflect.*;
import java.util.concurrent.atomic.*;

import javax.swing.*;

import ca.phon.app.log.*;
import ca.phon.opgraph.*;
import ca.phon.opgraph.exceptions.*;

/**
 * Close the buffer with given name.
 * 
 */
@OpNodeInfo(name="Close Buffer", description="Close buffer with given name", category=BufferNodeConstants.BUFFER_NODE_LIBRARY_CATEGORY, showInLibrary=true)
public class CloseBufferNode extends OpNode {
	
	private final InputField bufferNameField = 
			new InputField("buffer", "Buffer name", false, true, String.class);

	public CloseBufferNode() {
		super();
		
		putField(bufferNameField);
	}

	@Override
	public void operate(OpContext context) throws ProcessingException {
		final String bufferInput = (String)context.get(bufferNameField);
		
		final AtomicReference<BufferPanelContainer> bufferPanelContainerRef =
				new AtomicReference<BufferPanelContainer>((BufferPanelContainer)context.get(BufferNodeConstants.BUFFER_CONTEXT_KEY));
		final Runnable onEDT = () -> bufferPanelContainerRef.get().removeBuffer(bufferInput);
		if(SwingUtilities.isEventDispatchThread())
			onEDT.run();
		else
			try {
				SwingUtilities.invokeAndWait(onEDT);
			} catch (InvocationTargetException | InterruptedException e) {
				throw new ProcessingException(null, e);
			}
	}
	
}
