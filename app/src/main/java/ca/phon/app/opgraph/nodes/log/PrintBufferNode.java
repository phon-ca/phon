package ca.phon.app.opgraph.nodes.log;

import java.awt.Component;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.SwingUtilities;

import ca.gedge.opgraph.InputField;
import ca.gedge.opgraph.OpContext;
import ca.gedge.opgraph.OpNode;
import ca.gedge.opgraph.OpNodeInfo;
import ca.gedge.opgraph.app.GraphDocument;
import ca.gedge.opgraph.app.extensions.NodeSettings;
import ca.gedge.opgraph.exceptions.ProcessingException;
import ca.phon.app.log.BufferPanel;
import ca.phon.app.log.BufferPanelContainer;
import ca.phon.app.log.BufferWindow;
import ca.phon.formatter.FormatterUtil;

@OpNodeInfo(
		name="Print to Buffer",
		category="Report",
		description="Print given data to the buffer specified in settings.",
		showInLibrary=true
)
public class PrintBufferNode extends OpNode implements NodeSettings {
	
	private final static Logger LOGGER = Logger.getLogger(PrintBufferNode.class.getName());
	
	public static final String BUFFERS_KEY = "_buffers";
	
	private InputField dataField =
			new InputField("data", "Data to print", false, true, Object.class);
	
	private InputField bufferNameField = 
			new InputField("buffer", "Buffer name", true, true, String.class);
	
	private InputField appendField = 
			new InputField("append", "Append to buffer", true, true, Boolean.class);
	
	public PrintBufferNode() {
		super();
		
		putField(dataField);
		putField(bufferNameField);
		putField(appendField);
		
		putExtension(NodeSettings.class, this);
	}

	@Override
	public void operate(OpContext context) throws ProcessingException {
		final Object data = context.get(dataField);
		if(data == null) throw new ProcessingException(null, "Data cannot be null");
		
		final String bufferName = getBufferName(context);
		final boolean append = isAppendToBuffer(context);

		final String dataVal = FormatterUtil.format(data);
		
		final AtomicReference<BufferPanelContainer> bufferPanelContainerRef =
				new AtomicReference<BufferPanelContainer>((BufferPanelContainer)context.get(BUFFERS_KEY));

		Runnable onEDT = () -> {
			if(bufferPanelContainerRef.get() == null) {
				final BufferWindow bufferWindow = BufferWindow.getInstance();
				if(!bufferWindow.isVisible()) {
					bufferWindow.setSize(968, 600);
					bufferWindow.centerWindow();
					bufferWindow.setVisible(true);
				}
				bufferPanelContainerRef.set(bufferWindow);
			}
			
			final BufferPanelContainer bpc = bufferPanelContainerRef.get();
			
			BufferPanel bufferPanel = bpc.getBuffer(bufferName);
			if(bufferPanel == null) {
				bufferPanel = bpc.createBuffer(bufferName);
			}
			
			if(!append) {
				bufferPanel.getLogBuffer().setText("");
			}
			
			try (final PrintWriter out = new PrintWriter(
					new OutputStreamWriter(bufferPanel.getLogBuffer().getStdOutStream(), "UTF-8"))) {
				out.print(dataVal);
				out.flush();
			} catch (IOException e) {
				LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
			}
			
		};
		if(SwingUtilities.isEventDispatchThread())
			onEDT.run();
		else {
			try {
				SwingUtilities.invokeAndWait(onEDT);
			} catch (InvocationTargetException | InterruptedException e) {
				throw new ProcessingException(null, e);
			}
		}
	}

	public String getBufferName(OpContext ctx) {
		return (ctx.containsKey(bufferNameField) ? ctx.get(bufferNameField).toString() : "default");
	}
	
	public boolean isAppendToBuffer(OpContext ctx) {
		return (ctx.containsKey(appendField) ? (Boolean)ctx.get(appendField) : false);
	}

	@Override
	public Component getComponent(GraphDocument document) {
		return null;
	}

	@Override
	public Properties getSettings() {
		return new Properties();
	}

	@Override
	public void loadSettings(Properties properties) {
		
	}
	
}
