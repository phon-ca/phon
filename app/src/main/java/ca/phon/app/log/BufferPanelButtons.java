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
package ca.phon.app.log;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JScrollPane;

import org.jdesktop.swingx.HorizontalLayout;

import ca.phon.app.session.editor.SegmentedButtonBuilder;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.ui.fonts.FontPreferences;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;

public class BufferPanelButtons extends JComponent {

	private static final long serialVersionUID = 1L;
	
	private static final String TEXT_ICON = "mimetypes/text-x-generic";
	
	private static final String TABLE_TEXT = "Show data as table";
	
	private static final String TABLE_ICON = "mimetypes/x-office-spreadsheet";
	
	private static final String TEXT_TEXT = "Show data as text";
	
	private ButtonGroup buttonGroup;
	private JButton tableButton;
	private JButton textButton;
	
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
		final List<JButton> buttons = 
				(new SegmentedButtonBuilder<JButton>(JButton::new)).createSegmentedButtons(2, buttonGroup);
		
		final ImageIcon txtIcon = IconManager.getInstance().getIcon(TEXT_ICON, IconSize.SMALL);
		final PhonUIAction txtAct = new PhonUIAction(this, "showText");
		txtAct.putValue(PhonUIAction.SMALL_ICON, txtIcon);
		txtAct.putValue(PhonUIAction.SHORT_DESCRIPTION, TEXT_TEXT);
		textButton = buttons.get(0);
		textButton.setAction(txtAct);
		textButton.setFocusable(false);
		
		final ImageIcon tblIcon = IconManager.getInstance().getIcon(TABLE_ICON, IconSize.SMALL);
		final PhonUIAction tblAct = new PhonUIAction(this, "showTable");
		tblAct.putValue(PhonUIAction.SMALL_ICON, tblIcon);
		tblAct.putValue(PhonUIAction.SHORT_DESCRIPTION, TABLE_TEXT);
		tableButton = buttons.get(1);
		tableButton.setAction(tblAct);
		tableButton.setFocusable(false);
		
		final BufferPanel bufferPanel = getBufferPanel();
		bufferPanel.addPropertyChangeListener(BufferPanel.SHOWING_BUFFER_PROP, new PropertyChangeListener() {
			
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				textButton.setSelected(bufferPanel.isShowingBuffer());
				tableButton.setSelected(!bufferPanel.isShowingBuffer());
			}
			
		});
		
		textButton.setSelected(bufferPanel.isShowingBuffer());
		tableButton.setSelected(!bufferPanel.isShowingBuffer());
		
		setLayout(new HorizontalLayout(0));
		add(textButton);
		add(tableButton);
	}
	
	public void showHTML() {
		final HTMLTableBufferExporter exporter = new HTMLTableBufferExporter(true);
		try {
			final String html = exporter.exportBuffer(getBufferPanel().getLogBuffer());
			
			final JFrame tempFrame = new JFrame("HTML Table");
			final JEditorPane editorPane = new JEditorPane("text/html", html);
			editorPane.setEditable(false);
			editorPane.setFont(FontPreferences.getUIIpaFont());
			tempFrame.add(new JScrollPane(editorPane));
			
			tempFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			tempFrame.pack();
			tempFrame.setVisible(true);
			
		} catch (BufferExportException e) {
			e.printStackTrace();
		}
	}
	
	public void showText() {
		if(!getBufferPanel().isShowingBuffer()) {
			getBufferPanel().onSwapBuffer();
		}
	}
	
	public void showTable() {
		if(getBufferPanel().isShowingBuffer()) {
			getBufferPanel().onSwapBuffer();
		}
	}

}
