/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2008 The Phon Project, Memorial University <http://phon.ling.mun.ca>
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
package ca.phon.app.prefs;

import java.awt.BorderLayout;

import javax.swing.JButton;
import javax.swing.JLabel;

import org.jdesktop.swingx.JXList;

public class HiddenMessagesPrefsPanel extends PrefsPanel {

	private static final long serialVersionUID = -8956102208686690713L;
	
	/*
	 * UI
	 */
	private JXList hiddenPropsList;
	
	private JButton resetAllButton;
	
	private JButton resetSelectedButton;

	public HiddenMessagesPrefsPanel() {
		super("User-hidden Messages");
		
		
		init();
	}
	
	private void init() {
		setLayout(new BorderLayout());
		
		String msgText = "<html>" +
				"<p>Use the list below to reset (i.e., show) user-hidable messages in the application.</p>" +
				"</html>";
		JLabel msgLabel = new JLabel(msgText);
		
		
	}
	
	
}
