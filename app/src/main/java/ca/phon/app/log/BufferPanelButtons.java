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
package ca.phon.app.log;

import java.beans.*;
import java.lang.ref.*;

import javax.swing.*;

import org.jdesktop.swingx.*;

import ca.phon.ui.action.*;
import ca.phon.ui.fonts.*;
import ca.phon.util.icons.*;

public class BufferPanelButtons extends JComponent {

	private static final long serialVersionUID = 1L;
	
	private static final String TEXT_ICON = "mimetypes/text-x-generic";
	
	private static final String TABLE_TEXT = "Show data as table";
	
	private static final String TABLE_ICON = "mimetypes/x-office-spreadsheet";
	
	private static final String TEXT_TEXT = "Show data as text";
	
	private static final String HTML_ICON = "mimetypes/text-html";
	
	private static final String HTML_TEXT = "Show data as html";
	
	private ButtonGroup buttonGroup;
	JRadioButton tableButton;
	JRadioButton textButton;
	JRadioButton htmlButton;
	
	private final WeakReference<BufferPanel> panelRef;
	
	public BufferPanelButtons(BufferPanel panel) {
		super();
		
		panelRef = new WeakReference<BufferPanel>(panel);
		init();
	}
	
	public BufferPanel getBufferPanel() {
		return panelRef.get();
	}
		
	private void init() {
		buttonGroup = new ButtonGroup();
		
		final ImageIcon txtIcon = 
				IconManager.getInstance().getSystemIconForFileType("txt", TEXT_ICON, IconSize.SMALL);
		final PhonUIAction txtAct = new PhonUIAction(getBufferPanel(), "showBuffer");
		txtAct.putValue(PhonUIAction.NAME, "Text");
		txtAct.putValue(PhonUIAction.SHORT_DESCRIPTION, TEXT_TEXT);
		txtAct.putValue(PhonUIAction.SMALL_ICON, txtIcon);
		textButton = new JRadioButton(txtAct);
		textButton.setAction(txtAct);
		textButton.setFocusable(false);
		buttonGroup.add(textButton);
		
		final ImageIcon tblIcon =
				IconManager.getInstance().getSystemIconForFileType("csv", TABLE_ICON, IconSize.SMALL);
		final PhonUIAction tblAct = new PhonUIAction(getBufferPanel(), "showTable");
		tblAct.putValue(PhonUIAction.NAME, "Table");
		tblAct.putValue(PhonUIAction.SHORT_DESCRIPTION, TABLE_TEXT);
		tblAct.putValue(PhonUIAction.SMALL_ICON, tblIcon);
		tableButton = new JRadioButton(tblAct);
		tableButton.setAction(tblAct);
		tableButton.setFocusable(false);
		buttonGroup.add(tableButton);
		
		final ImageIcon htmlIcon =
				IconManager.getInstance().getSystemIconForFileType("html", HTML_ICON, IconSize.SMALL);
		final PhonUIAction htmlAct = new PhonUIAction(getBufferPanel(), "showHtml");
		htmlAct.putValue(PhonUIAction.NAME, "HTML");
		htmlAct.putValue(PhonUIAction.SHORT_DESCRIPTION, HTML_TEXT);
		htmlAct.putValue(PhonUIAction.SMALL_ICON, htmlIcon);
		htmlButton = new JRadioButton(htmlAct);
		htmlButton.setAction(htmlAct);
		htmlButton.setFocusable(false);
		buttonGroup.add(htmlButton);
		
		final BufferPanel bufferPanel = getBufferPanel();
		bufferPanel.addPropertyChangeListener(BufferPanel.SHOWING_BUFFER_PROP, new PropertyChangeListener() {
			
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				textButton.setSelected(bufferPanel.isShowingBuffer());
				tableButton.setSelected(bufferPanel.isShowingTable());
				htmlButton.setSelected(bufferPanel.isShowingHtml());
			}
			
		});
		
		textButton.setSelected(true);
		tableButton.setSelected(bufferPanel.isShowingTable());
		htmlButton.setSelected(bufferPanel.isShowingHtml());
		
		setLayout(new HorizontalLayout(0));
		add(textButton);
		add(tableButton);
		add(htmlButton);
	}
	
	public void showHTML() {
		final HTMLTableBufferExporter exporter = new HTMLTableBufferExporter(true);
		try {
			final String html = exporter.exportBuffer(getBufferPanel().getLogBuffer());
			
			final JFrame tempFrame = new JFrame("HTML Table");
			final JEditorPane editorPane = new JEditorPane("text/html", html);
			editorPane.setEditable(false);
			editorPane.setFont(FontPreferences.getTierFont());
			tempFrame.add(new JScrollPane(editorPane));
			
			tempFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			tempFrame.pack();
			tempFrame.setVisible(true);
			
		} catch (BufferExportException e) {
			e.printStackTrace();
		}
	}

}
