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
import ca.phon.formatter.FormatterUtil;
import ca.phon.opgraph.*;
import ca.phon.opgraph.app.GraphDocument;
import ca.phon.opgraph.app.extensions.NodeSettings;
import ca.phon.opgraph.exceptions.ProcessingException;
import ca.phon.query.report.datasource.DefaultTableDataSource;
import ca.phon.ui.HidablePanel;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rtextarea.RTextScrollPane;
import org.jdesktop.swingx.VerticalLayout;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

@OpNodeInfo(
		name="Print to Buffer",
		category=BufferNodeConstants.BUFFER_NODE_LIBRARY_CATEGORY,
		description="Print given data to the buffer specified in settings.",
		showInLibrary=true
)
public class PrintBufferNode extends OpNode implements NodeSettings {

	private final static org.apache.logging.log4j.Logger LOGGER = org.apache.logging.log4j.LogManager.getLogger(PrintBufferNode.class.getName());

	private InputField dataField =
			new InputField("data", "Data to print", true, false, Object.class);

	private InputField bufferNameField =
			new InputField("buffer", "Buffer name", true, true, String.class);

	private InputField appendField =
			new InputField("append", "Append to buffer", true, true, Boolean.class);
	
	private InputField insertAtBeginningField = 
			new InputField("insertAtBeginning", "Insert at beginning of buffer", true, true, Boolean.class);

	private OutputField bufferNameOutputField =
			new OutputField("buffer", "Buffer name, this may differ from the input buffer name if not appending and another buffer with the same name exists.",
					true, String.class);

	private final static String DEFAULT_TEMPLATE = "$DATA";
	private String dataTemplate = DEFAULT_TEMPLATE;

	private boolean showTable = true;
	private boolean showText = false;
	private boolean showHTML = false;
	
	private boolean showBuffer = true;

	private JPanel settingsPanel;

	private ButtonGroup showAsGroup = new ButtonGroup();
	private JRadioButton showAsText;
	private JRadioButton showAsCSV;
	private JRadioButton showAsHTML;
	
	private JCheckBox showBufferBox;

	private RSyntaxTextArea dataInputArea;

	public PrintBufferNode() {
		super();
		
		putField(dataField);
		putField(bufferNameField);
		putField(appendField);
		putField(insertAtBeginningField);
		putField(bufferNameOutputField);

		putExtension(NodeSettings.class, this);
	}

	@Override
	public void operate(OpContext context) throws ProcessingException {
		final Object data = (context.get(dataField) != null ? context.get(dataField) : "");

		final String bufferInput = getBufferName(context);
		final boolean append = isAppendToBuffer(context);

		final String templateVal = getDataTemplate();
		final String dataVal = FormatterUtil.format(data);

		final StringBuffer dataBuffer = new StringBuffer();
		if(templateVal == null || templateVal.trim().length() == 0 || templateVal.equals(DEFAULT_TEMPLATE)) {
			dataBuffer.append(dataVal);
		} else {
			dataBuffer.append(templateVal.replace("$DATA", dataVal));
		}

		final AtomicReference<BufferPanelContainer> bufferPanelContainerRef =
				new AtomicReference<BufferPanelContainer>((BufferPanelContainer)context.get(BufferNodeConstants.BUFFER_CONTEXT_KEY));
		// update buffer name based on current list of buffers
		final Collection<String> currentBuffers = bufferPanelContainerRef.get().getBufferNames();
		String tempName = bufferInput;
		if(!append) {
			int idx = 0;
			while(currentBuffers.contains(tempName)) {
				tempName = bufferInput + "(" + (++idx) + ")";
			}
		}
		final String bufferName = tempName;
		context.put(bufferNameOutputField, bufferName);

		Runnable onEDT = () -> {
			if(bufferPanelContainerRef.get() == null) {
				final BufferWindow bufferWindow = BufferWindow.getBufferWindow();
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
				bufferPanel = bpc.createBuffer(bufferName, isShowBuffer());
			} else {
				if(isShowBuffer())
					bpc.selectBuffer(bufferName);
			}

			if(data instanceof DefaultTableDataSource) {
				bufferPanel.putExtension(DefaultTableDataSource.class,
						(DefaultTableDataSource)data);
				bufferPanel.setUserObject(data);
			}

			final StringBuffer currentContent = new StringBuffer();
			currentContent.append(bufferPanel.getLogBuffer().getText());
			
			if(!append || isInsertAtBeginning(context)) {
				bufferPanel.getLogBuffer().setText("");
			}

			try (final PrintWriter out = new PrintWriter(
					new OutputStreamWriter(bufferPanel.getLogBuffer().getStdOutStream(), "UTF-8"))) {
				out.print(dataBuffer.toString());
				out.flush();
				
				if(isInsertAtBeginning(context)) {
					out.print(currentContent.toString());
					out.flush();
				}

				if(isShowText()) {
					out.print(LogBuffer.ESCAPE_CODE_PREFIX + BufferPanel.SHOW_BUFFER_CODE);
					out.flush();
				} else if(isShowTable()) {
					out.print(LogBuffer.ESCAPE_CODE_PREFIX + BufferPanel.SHOW_TABLE_CODE);
					out.flush();
				} else if(isShowHTML()) {
					out.print(LogBuffer.ESCAPE_CODE_PREFIX + BufferPanel.SHOW_HTML_CODE);
					out.flush();
				}
			} catch (IOException e) {
				LOGGER.error( e.getLocalizedMessage(), e);
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
		return (ctx.get(bufferNameField) != null ? ctx.get(bufferNameField).toString() : "default");
	}

	public boolean isAppendToBuffer(OpContext ctx) {
		return (ctx.get(appendField) != null ? (Boolean)ctx.get(appendField) : false);
	}

	public boolean isInsertAtBeginning(OpContext ctx) {
		return (ctx.get(insertAtBeginningField) != null ? (Boolean)ctx.get(insertAtBeginningField) : false);
	}
	
	public String getDataTemplate() {
		return (this.dataInputArea != null ? this.dataInputArea.getText() : this.dataTemplate);
	}

	public void setDataTemplate(String dataTemplate) {
		this.dataTemplate = dataTemplate;
		if(this.dataInputArea != null)
			this.dataInputArea.setText(dataTemplate);
	}

	public boolean isShowText() {
		return (showAsText != null ? showAsText.isSelected() : this.showText);
	}

	public void setShowText(boolean showText) {
		this.showText = showText;
		if(showText) {
			this.showTable = false;
			this.showHTML = false;
		} else if((!showTable && !showHTML) && !(this.showTable ^ this.showHTML)) {
			this.showTable = true;
			this.showHTML = false;
		}

		if(this.showAsText != null)
			this.showAsText.setSelected(showText);
	}

	public boolean isShowTable() {
		return (showAsCSV != null ? showAsCSV.isSelected() : this.showTable);
	}

	public void setShowTable(boolean showTable) {
		this.showTable = showTable;
		if(showTable) {
			this.showText = false;
			this.showHTML = false;
		} else if((!showText && !showHTML) && !(this.showText ^ this.showHTML)) {
			this.showText = true;
			this.showHTML = false;
		}

		if(this.showAsCSV != null)
			this.showAsCSV.setSelected(showTable);
	}

	public boolean isShowHTML() {
		return (showAsHTML != null ? showAsHTML.isSelected() : this.showHTML);
	}

	public void setShowHTML(boolean showHTML) {
		this.showHTML = showHTML;
		if(showHTML) {
			this.showText = false;
			this.showTable = false;
		} else if((!showText && !showTable) && !(this.showText ^ this.showTable)) {
			this.showText = true;
			this.showTable = false;
		}

		if(this.showAsHTML != null)
			this.showAsHTML.setSelected(showHTML);
	}
	
	public boolean isShowBuffer() {
		return (this.showBufferBox != null ? this.showBufferBox.isSelected() : showBuffer);
	}
	
	public void setShowBuffer(boolean showBuffer) {
		this.showBuffer = showBuffer;
		if(this.showBufferBox != null)
			this.showBufferBox.setSelected(showBuffer);
	}

	@Override
	public Component getComponent(GraphDocument document) {
		if(settingsPanel == null) {
			settingsPanel = new JPanel(new BorderLayout());

			final JPanel btnPanel = new JPanel(new VerticalLayout());
			showAsText = new JRadioButton("Show data as plain text");
			showAsGroup.add(showAsText);
			showAsCSV = new JRadioButton("Show data as table (CSV)");
			showAsGroup.add(showAsCSV);
			showAsHTML = new JRadioButton("Show data as HTML");
			showAsGroup.add(showAsHTML);

			showAsText.setSelected(this.showText);
			showAsCSV.setSelected(this.showTable);
			showAsHTML.setSelected(this.showHTML);
			
			showBufferBox = new JCheckBox("Show buffer");
			showBufferBox.setSelected(this.showBuffer);

			btnPanel.add(showAsText);
			btnPanel.add(showAsCSV);
			btnPanel.add(showAsHTML);
			btnPanel.add(showBufferBox);
			btnPanel.setBorder(BorderFactory.createTitledBorder("Data Format"));
			settingsPanel.add(btnPanel, BorderLayout.NORTH);

			final JPanel dataInputPanel = new JPanel(new BorderLayout());
			final HidablePanel helpMessage = new HidablePanel(PrintBufferNode.class.getName() + ".helpMessage");
			helpMessage.setBottomLabelText("<html>Enter data to add to buffer below.  If data is given in the box below and "
					+ "in the node 'data' input field during execution, the resulting text will be the data from the field below with "
					+ "the text from the input field relpacing the token $DATA.</html>");
			dataInputPanel.add(helpMessage, BorderLayout.NORTH);

			dataInputArea = new RSyntaxTextArea();
			dataInputArea.setSyntaxEditingStyle(RSyntaxTextArea.SYNTAX_STYLE_NONE);
			dataInputArea.setText(this.dataTemplate);
			final RTextScrollPane textScroller = new RTextScrollPane(dataInputArea);
			settingsPanel.add(textScroller);
			dataInputPanel.add(textScroller, BorderLayout.CENTER);

			settingsPanel.add(dataInputPanel, BorderLayout.CENTER);
		}
		return settingsPanel;
	}

	@Override
	public Properties getSettings() {
		final Properties retVal = new Properties();

		retVal.setProperty("showText", Boolean.toString(isShowText()));
		retVal.setProperty("showTable", Boolean.toString(isShowTable()));
		retVal.setProperty("showHTML", Boolean.toString(isShowHTML()));
		
		retVal.setProperty("showBuffer", Boolean.toString(isShowBuffer()));

		retVal.setProperty("dataTemplate", getDataTemplate());

		return retVal;
	}

	@Override
	public void loadSettings(Properties properties) {
		setShowText(Boolean.parseBoolean(properties.getProperty("showText", "false")));
		setShowTable(Boolean.parseBoolean(properties.getProperty("showTable", "true")));
		setShowHTML(Boolean.parseBoolean(properties.getProperty("showHTML", "false")));
		
		setShowBuffer(Boolean.parseBoolean(properties.getProperty("showBuffer", "true")));

		setDataTemplate(properties.getProperty("dataTemplate", DEFAULT_TEMPLATE));
	}

}
