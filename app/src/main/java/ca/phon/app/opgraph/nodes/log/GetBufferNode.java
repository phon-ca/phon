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
