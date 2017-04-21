/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2017, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
 * Dept of Linguistics, Memorial University <https://phon.ca>
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ca.phon.app.opgraph.nodes.log;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.SwingUtilities;

import ca.gedge.opgraph.InputField;
import ca.gedge.opgraph.OpContext;
import ca.gedge.opgraph.OpNode;
import ca.gedge.opgraph.OpNodeInfo;
import ca.gedge.opgraph.OutputField;
import ca.gedge.opgraph.exceptions.ProcessingException;
import ca.phon.app.log.BufferPanel;
import ca.phon.app.log.BufferPanelContainer;

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
