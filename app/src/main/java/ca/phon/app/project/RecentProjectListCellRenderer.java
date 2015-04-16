/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2015 The Phon Project, Memorial University <https://phon.ca>
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
package ca.phon.app.project;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;

import org.apache.commons.lang3.StringUtils;

import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;

public class RecentProjectListCellRenderer extends DefaultListCellRenderer {
	
	private ImageIcon localProjectIcon;
	private ImageIcon remoteProjectIcon;
	
	/** Constructor */
	public RecentProjectListCellRenderer() {
		super();
		
		this.localProjectIcon = 
			IconManager.getInstance().getIcon("misc/phon-corpus", IconSize.LARGE);
		this.remoteProjectIcon = 
			IconManager.getInstance().getIcon("misc/phon-corpus", IconSize.LARGE);
	}

	@Override
	public Component getListCellRendererComponent(
			JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
		JLabel comp = (JLabel)super.getListCellRendererComponent(list, value, index, isSelected,
				cellHasFocus);
		
//		comp.setVerticalTextPosition(JLabel.BOTTOM);
//		comp.setHorizontalTextPosition(JLabel.CENTER);
		
		// if the file string stats with RMI, show the remote project icon,
		// otherwise, show the default project icon
		if(value.toString().startsWith("rmi://"))
			comp.setIcon(remoteProjectIcon);
		else
			comp.setIcon(localProjectIcon);
		
		int numChars = 20;
		int numBreaks = value.toString().length() / numChars;
		
//		String compString = new String();
//		for(int i = 0; i < numBreaks; i++) {
//			compString += value.toString().substring(i*numChars, i*numChars+numChars) + "<br>";
//		}
//		compString += value.toString().substring(numBreaks*numChars, 
//				value.toString().length());
		
		comp.setText("<html><center>" + StringUtils.abbreviate(value.toString(), 60) + "</center></html>");
		
		return comp;
	}

}
