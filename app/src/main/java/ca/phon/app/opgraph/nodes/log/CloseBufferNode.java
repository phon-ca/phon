package ca.phon.app.opgraph.nodes.log;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.SwingUtilities;

import ca.gedge.opgraph.InputField;
import ca.gedge.opgraph.OpContext;
import ca.gedge.opgraph.OpNode;
import ca.gedge.opgraph.OpNodeInfo;
import ca.gedge.opgraph.exceptions.ProcessingException;
import ca.phon.app.log.BufferPanelContainer;

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
