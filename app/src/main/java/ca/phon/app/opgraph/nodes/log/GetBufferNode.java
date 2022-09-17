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

import ca.phon.app.log.*;
import ca.phon.opgraph.*;
import ca.phon.opgraph.exceptions.ProcessingException;

import javax.swing.*;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Node for reading the current text of a given buffer.
 *
 */
@OpNodeInfo(name="Get Buffer", description="Get text contents of given buffer.", category=BufferNodeConstants.BUFFER_NODE_LIBRARY_CATEGORY, showInLibrary=true)
public class GetBufferNode extends OpNode {
	
	private final InputField bufferNameField = 
			new InputField("buffer", "Buffer name", true, true, String.class);
	
	private final OutputField bufferTextField =
			new OutputField("text", "Buffer data as text", true, String.class);
	
	public GetBufferNode() {
		super();
		
		putField(bufferNameField);
		putField(bufferTextField);
	}

	@Override
	public void operate(OpContext context) throws ProcessingException {
		final String bufferInput =
				(context.get(bufferNameField) != null ? (String)context.get(bufferNameField) : "default");
		
		final AtomicReference<BufferPanelContainer> bufferPanelContainerRef =
				new AtomicReference<BufferPanelContainer>((BufferPanelContainer)context.get(BufferNodeConstants.BUFFER_CONTEXT_KEY));
		final Runnable onEDT = () -> {
			final BufferPanel bufferPanel = bufferPanelContainerRef.get().getBuffer(bufferInput);
			if(bufferPanel != null) {
				context.put(bufferTextField, bufferPanel.getLogBuffer().getText());
			} else {
				context.put(bufferTextField, "");
			}
		};
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
