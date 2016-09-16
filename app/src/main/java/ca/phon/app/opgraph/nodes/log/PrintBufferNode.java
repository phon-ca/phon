/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2016, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
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

import java.awt.Component;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.jdesktop.swingx.VerticalLayout;

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
import ca.phon.app.log.LogBuffer;
import ca.phon.formatter.FormatterUtil;
import ca.phon.query.report.datasource.DefaultTableDataSource;

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
	
	private boolean showTable = true;
	
	private JPanel settingsPanel;
	private JCheckBox showTableBox;
	
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
			
			if(data instanceof DefaultTableDataSource) {
				bufferPanel.putExtension(BirtBufferPanelExtension.class, 
						new BirtBufferPanelExtension(bufferPanel, (DefaultTableDataSource)data));
			}
			
			if(!append) {
				bufferPanel.getLogBuffer().setText("");
			}
			
			try (final PrintWriter out = new PrintWriter(
					new OutputStreamWriter(bufferPanel.getLogBuffer().getStdOutStream(), "UTF-8"))) {
				out.print(dataVal);
				out.flush();
				
				if(isShowTable()) {
					out.print(LogBuffer.ESCAPE_CODE_PREFIX + BufferPanel.SHOW_TABLE_CODE);
					out.flush();
				}
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

	public boolean isShowTable() {
		return (showTableBox != null ? showTableBox.isSelected() : this.showTable);
	}
	
	public void setShowTable(boolean showTable) {
		this.showTable = showTable;
		if(this.showTableBox != null)
			this.showTableBox.setSelected(showTable);
	}
	
	@Override
	public Component getComponent(GraphDocument document) {
		if(settingsPanel == null) {
			settingsPanel = new JPanel(new VerticalLayout());
			
			showTableBox = new JCheckBox("Show table");
			showTableBox.setSelected(this.showTable);
			settingsPanel.add(showTableBox);
		}
		return settingsPanel;
	}

	@Override
	public Properties getSettings() {
		final Properties retVal = new Properties();
		
		retVal.setProperty("showTable", Boolean.toString(isShowTable()));
		
		return retVal;
	}

	@Override
	public void loadSettings(Properties properties) {
		setShowTable(Boolean.parseBoolean(properties.getProperty("showTable", "true")));
	}
	
}
