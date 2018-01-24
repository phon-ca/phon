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
package ca.phon.app.about;

import java.awt.*;

import javax.swing.*;

import ca.phon.app.VersionInfo;
import ca.phon.ui.CommonModuleFrame;
import ca.phon.util.icons.*;

public class AboutDialog extends CommonModuleFrame {

	private static final long serialVersionUID = 1L;

	public AboutDialog() {
		super("About Phon");
		
		init();
	}

	private void init() {
		final ImageIcon phonIcn = IconManager.getInstance().getIcon("apps/database-phon", IconSize.XXLARGE);
		final JLabel icnLbl = new JLabel(phonIcn);
		icnLbl.setVerticalAlignment(JLabel.TOP);
		
		final JLabel versionLbl = new JLabel();
		versionLbl.setText("Phon " + VersionInfo.getInstance().getVersion());
		
		final GridBagLayout layout = new GridBagLayout();
		setLayout(layout);
		
		final GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 0.0;
		gbc.weighty = 1.0;
		gbc.fill = GridBagConstraints.VERTICAL;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.gridheight = 4;
		gbc.gridwidth = 1;
		add(icnLbl, gbc);
		
		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1.0;
		gbc.weighty = 0.0;
		gbc.gridwidth = 2;
		gbc.gridheight = 1;
		add(versionLbl, gbc);
	}
	
}
