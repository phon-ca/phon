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

import java.awt.BorderLayout;
import java.io.IOException;
import java.net.URL;
import java.util.logging.*;

import javax.swing.*;

import ca.phon.app.VersionInfo;
import ca.phon.ui.CommonModuleFrame;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.ui.decorations.DialogHeader;
import ca.phon.ui.layout.ButtonBarBuilder;

public class AboutDialog extends CommonModuleFrame {
	
	private static final Logger LOGGER = Logger
			.getLogger(AboutDialog.class.getName());

	private static final long serialVersionUID = 1L;
	
	private final static String INFO_FILE = "about_phon.htm";
	
	private JEditorPane infoPane;
	
	private DialogHeader header;
	
	private JButton closeButton;
	
	public AboutDialog() {
		super("About Phon");
		
		init();
	}

	private void init() {
		setLayout(new BorderLayout());
		
		header = new DialogHeader("About Phon", "Version: " + VersionInfo.getInstance().getLongVersion());
		add(header, BorderLayout.NORTH);
		
		final URL aboutPhonURL = getClass().getResource(INFO_FILE);
		try {
			infoPane = new JEditorPane(aboutPhonURL);
			infoPane.setEditable(false);
			final JScrollPane sp = new JScrollPane(infoPane);
			add(sp, BorderLayout.CENTER);
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}
		
		final PhonUIAction closeAction = new PhonUIAction(this, "close");
		closeAction.putValue(PhonUIAction.NAME, "Close");
		closeButton = new JButton(closeAction);
		add(ButtonBarBuilder.buildOkBar(closeButton), BorderLayout.SOUTH);
	}
	
}
