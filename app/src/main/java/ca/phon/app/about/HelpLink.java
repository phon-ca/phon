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
package ca.phon.app.about;

import java.awt.Cursor;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.JButton;

import ca.hedlund.desktopicons.MacOSStockIcon;
import ca.hedlund.desktopicons.WindowsStockIcon;
import ca.phon.app.log.LogUtil;
import ca.phon.util.OSInfo;
import ca.phon.util.OpenFileLauncher;
import ca.phon.util.PrefHelper;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;

/**
 *
 */
public class HelpLink extends JButton {

	public final static String DEFAULT_WEBSITE_ROOT = "http://phon-ca.github.io/phon/";
	public final static String WEBSITE_ROOT_PROP = HelpLink.class.getName() + ".websiteRoot";
	private String websiteRoot = PrefHelper.get(WEBSITE_ROOT_PROP, DEFAULT_WEBSITE_ROOT);
	
	private IconSize iconSize;
	
	private String helpLocation;
	
	public HelpLink(String path) {
		this(IconSize.SMALL, path);
	}
	
	public HelpLink(IconSize size, String path) {
		super();
		this.iconSize = size;
		this.helpLocation = websiteRoot + path;
		
		if(OSInfo.isMacOs()) {
			setIcon(IconManager.getInstance().getSystemStockIcon(MacOSStockIcon.HelpIcon, iconSize));
		} else if(OSInfo.isWindows()) {
			setIcon(IconManager.getInstance().getSystemStockIcon(WindowsStockIcon.HELP, iconSize));
		} else if(OSInfo.isNix()) {
			// XXX icon for nix
		}
		
		setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		setToolTipText(path);
		
		setBorderPainted(false);
		
		addActionListener( this::openLink );
	}
	
	public String getHelpLocation() {
		return this.helpLocation;
	}
	
	public void setHelpLocation(String location) {
		this.helpLocation = location;
	}
	
	public void openLink(ActionEvent e) {
		try {
			OpenFileLauncher.openURL(new URL(getHelpLocation()));
		} catch (MalformedURLException ex) {
			Toolkit.getDefaultToolkit().beep();
			LogUtil.severe(ex);
		}
	}
	
}
