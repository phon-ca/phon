/*
 * Copyright (C) 2012-2018 Gregory Hedlund & Yvan Rose
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
package ca.phon.app.opgraph.library;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;

import javax.swing.JEditorPane;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

import org.jdesktop.swingx.JXPanel;

import ca.phon.opgraph.library.NodeData;

public class NodeDataCellRenderer implements ListCellRenderer<NodeData> {
	
	private static final long serialVersionUID = -954105268513331383L;
	
    private static final Border DEFAULT_NO_FOCUS_BORDER = new EmptyBorder(1, 1, 1, 1);
    protected static Border noFocusBorder = DEFAULT_NO_FOCUS_BORDER;

	private final JXPanel panel;
	
	private final JEditorPane label;
	
	public NodeDataCellRenderer() {
		super();
		
		panel = new JXPanel(new BorderLayout());
		
		label = new JEditorPane();
		label.setContentType("text/html");
		
		panel.add(label, BorderLayout.CENTER);
	}

	@Override
	public Component getListCellRendererComponent(
			JList<? extends NodeData> list, NodeData value, int index,
			boolean isSelected, boolean cellHasFocus) {
		final NodeData nodeData = (NodeData)value;
		
		final StringBuilder sb = new StringBuilder();
		sb.append("<html>");
		sb.append("<body style='");
		
		Color foreground = list.getForeground();
		Color background = list.getBackground();
		if(isSelected) {
			foreground = list.getSelectionForeground();
			background = list.getSelectionBackground();
		}
		sb.append("background-color: ");
		sb.append("#")
		  .append(Integer.toHexString(background.getRGB()).substring(2));
		sb.append(";");
		sb.append("color: ");
		sb.append("#")
		  .append(Integer.toHexString(foreground.getRGB()).substring(2));
		sb.append(";");
		
		sb.append("'>");
		
		sb.append("<b>").append(nodeData.name).append("</b>")
		  .append("<p>").append(nodeData.description).append("</p>")
		  .append("</body></html>");
		
		label.setText(sb.toString());
		
		if(list.getWidth() > 0)
			label.setSize(list.getWidth(), Short.MAX_VALUE);
		
		UIDefaults uidefaults = UIManager.getDefaults();
		Border border = null;
		if(cellHasFocus) {
			if(isSelected) {
				border = 
						uidefaults.getBorder("List.focusSelectedCellHighlightBorder");
			} else {
				border =
						uidefaults.getBorder("List.focusCellHighlightBorder");
			}
		}
		if(border == null)
			border = noFocusBorder;
		panel.setBorder(border);
		
		return panel;
	}
	
}
