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
package ca.phon.app.opgraph.editor.library;

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
