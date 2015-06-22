package ca.phon.app.query.opgraph;

import java.awt.Component;
import java.lang.reflect.InvocationTargetException;
import java.util.Properties;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import ca.gedge.opgraph.InputField;
import ca.gedge.opgraph.OpContext;
import ca.gedge.opgraph.OpNode;
import ca.gedge.opgraph.OpNodeInfo;
import ca.gedge.opgraph.app.GraphDocument;
import ca.gedge.opgraph.app.extensions.NodeSettings;
import ca.gedge.opgraph.exceptions.ProcessingException;
import ca.phon.app.log.BufferPanel;
import ca.phon.app.log.BufferWindow;
import ca.phon.formatter.Formatter;
import ca.phon.formatter.FormatterFactory;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

@OpNodeInfo(
		name="Print to Buffer",
		category="Report",
		description="Print given data to the buffer specified in settings.",
		showInLibrary=true
)
public class PrintBufferNode extends OpNode implements NodeSettings {
	
	private InputField dataField =
			new InputField("data", "Data to print", false, true, Object.class);
	
	
	private final static String APPEND_TO_BUFFER_PROP = "appendToBuffer";
	
	private boolean appendToBuffer = true;
	
	private JCheckBox appendToBufferBox;
	
	private final static String BUFFER_NAME_PROP = "bufferName";
	
	private String bufferName = "default";
	
	private JTextField bufferNameField;
	
	public PrintBufferNode() {
		super();
		
		putField(dataField);

		putExtension(NodeSettings.class, this);
	}

	@Override
	public void operate(OpContext context) throws ProcessingException {
		final Object data = context.get(dataField);
		if(data == null) throw new ProcessingException("Data cannot be null");
		
		final String bufferName = getBufferName();
		final boolean append = isAppendToBuffer();
		
		@SuppressWarnings({ "unchecked", "rawtypes" })
		Runnable onEDT = () -> {
			final BufferWindow bufferWindow = BufferWindow.getInstance();
			
			BufferPanel bufferPanel = bufferWindow.getBuffer(bufferName);
			if(bufferPanel == null) {
				bufferPanel = bufferWindow.createBuffer(bufferName);
			}
			
			if(!append) {
				bufferPanel.getLogBuffer().setText("");
			}

			StringBuffer bufferValue = new StringBuffer(bufferPanel.getLogBuffer().getText());
			
			if(data instanceof String) {
				bufferValue.append((String)data);
			} else {
				final Formatter formatter = FormatterFactory.createFormatter(data.getClass());
				if(formatter == null) {
					bufferValue.append(data.toString());
				} else {
					bufferValue.append(formatter.format(data));
				}
			}
			
			bufferPanel.getLogBuffer().setText(bufferValue.toString());
			
			if(!bufferWindow.isVisible()) {
				bufferWindow.setSize(600, 800);
				bufferWindow.centerWindow();
				bufferWindow.setVisible(true);
			}
		};
		if(SwingUtilities.isEventDispatchThread())
			onEDT.run();
		else {
			try {
				SwingUtilities.invokeAndWait(onEDT);
			} catch (InvocationTargetException | InterruptedException e) {
				throw new ProcessingException(e);
			}
		}
	}

	private JPanel createSettingsPanel() {
		final FormLayout layout = new FormLayout(
				"right:pref, 3dlu, fill:pref:grow",
				"pref, pref");
		final CellConstraints cc = new CellConstraints();
		final JPanel panel = new JPanel(layout);
	
		final String currentName = getBufferName();
		bufferNameField = new JTextField();
		bufferNameField.setText(currentName);
		
		final boolean currentAppend = isAppendToBuffer();
		appendToBufferBox = new JCheckBox("Append");
		appendToBufferBox.setSelected(currentAppend);
		
		panel.add(new JLabel("Buffer Name"), cc.xy(1,1));
		panel.add(bufferNameField, cc.xy(3, 1));
		panel.add(appendToBufferBox, cc.xy(3, 2));
		
		return panel;
	}
	
	public String getBufferName() {
		return (bufferNameField != null ? bufferNameField.getText() : bufferName);
	}
	
	public boolean isAppendToBuffer() {
		return (appendToBufferBox != null ? appendToBufferBox.isSelected() : appendToBuffer);
	}
	
	@Override
	public Component getComponent(GraphDocument document) {
		return createSettingsPanel();
	}

	@Override
	public Properties getSettings() {
		final Properties props = new Properties();
		
		props.put(APPEND_TO_BUFFER_PROP, isAppendToBuffer());
		props.put(BUFFER_NAME_PROP, getBufferName());
		
		return props;
	}

	@Override
	public void loadSettings(Properties properties) {
		if(properties.containsKey(APPEND_TO_BUFFER_PROP)) {
			appendToBuffer = Boolean.parseBoolean(properties.getProperty(APPEND_TO_BUFFER_PROP));
		}
		if(properties.containsKey(BUFFER_NAME_PROP)) {
			bufferName = properties.getProperty(BUFFER_NAME_PROP);
		}
	}

}
