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

import java.awt.BorderLayout;
import java.awt.Component;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.SwingUtilities;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rtextarea.RTextScrollPane;
import org.jdesktop.swingx.VerticalLayout;

import ca.gedge.opgraph.InputField;
import ca.gedge.opgraph.OpContext;
import ca.gedge.opgraph.OpNode;
import ca.gedge.opgraph.OpNodeInfo;
import ca.gedge.opgraph.OutputField;
import ca.gedge.opgraph.app.GraphDocument;
import ca.gedge.opgraph.app.extensions.NodeSettings;
import ca.gedge.opgraph.exceptions.ProcessingException;
import ca.phon.app.log.BufferPanel;
import ca.phon.app.log.BufferPanelContainer;
import ca.phon.app.log.BufferWindow;
import ca.phon.app.log.LogBuffer;
import ca.phon.formatter.FormatterUtil;
import ca.phon.query.report.datasource.DefaultTableDataSource;
import ca.phon.ui.HidablePanel;

@OpNodeInfo(
		name="Print to Buffer",
		category=BufferNodeConstants.BUFFER_NODE_LIBRARY_CATEGORY,
		description="Print given data to the buffer specified in settings.",
		showInLibrary=true
)
public class PrintBufferNode extends OpNode implements NodeSettings {

	private final static Logger LOGGER = Logger.getLogger(PrintBufferNode.class.getName());

	private InputField dataField =
			new InputField("data", "Data to print", true, false, Object.class);

	private InputField bufferNameField =
			new InputField("buffer", "Buffer name", true, true, String.class);

	private InputField appendField =
			new InputField("append", "Append to buffer", true, true, Boolean.class);

	private OutputField bufferNameOutputField =
			new OutputField("buffer", "Buffer name, this may differ from the input buffer name if not appending and another buffer with the same name exists.",
					true, String.class);

	private final static String DEFAULT_TEMPLATE = "$DATA";
	private String dataTemplate = DEFAULT_TEMPLATE;

	private boolean showTable = true;
	private boolean showText = false;
	private boolean showHTML = false;

	private JPanel settingsPanel;

	private ButtonGroup showAsGroup = new ButtonGroup();
	private JRadioButton showAsText;
	private JRadioButton showAsCSV;
	private JRadioButton showAsHTML;

	private RSyntaxTextArea dataInputArea;

	public PrintBufferNode() {
		super();

		putField(dataField);
		putField(bufferNameField);
		putField(appendField);
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
				bufferPanel.putExtension(DefaultTableDataSource.class,
						(DefaultTableDataSource)data);
				bufferPanel.setUserObject(data);
			}

			if(!append) {
				bufferPanel.getLogBuffer().setText("");
			}

			try (final PrintWriter out = new PrintWriter(
					new OutputStreamWriter(bufferPanel.getLogBuffer().getStdOutStream(), "UTF-8"))) {
				out.print(dataBuffer.toString());
				out.flush();

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
		return (ctx.get(bufferNameField) != null ? ctx.get(bufferNameField).toString() : "default");
	}

	public boolean isAppendToBuffer(OpContext ctx) {
		return (ctx.get(appendField) != null ? (Boolean)ctx.get(appendField) : false);
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

			btnPanel.add(showAsText);
			btnPanel.add(showAsCSV);
			btnPanel.add(showAsHTML);
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

		retVal.setProperty("dataTemplate", getDataTemplate());

		return retVal;
	}

	@Override
	public void loadSettings(Properties properties) {
		setShowText(Boolean.parseBoolean(properties.getProperty("showText", "false")));
		setShowTable(Boolean.parseBoolean(properties.getProperty("showTable", "true")));
		setShowHTML(Boolean.parseBoolean(properties.getProperty("showHTML", "false")));

		setDataTemplate(properties.getProperty("dataTemplate", DEFAULT_TEMPLATE));
	}

}
