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

import java.awt.Desktop;
import java.awt.Toolkit;
import java.awt.Window;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import ca.phon.app.log.LogUtil;
import ca.phon.plugin.IPluginExtensionFactory;
import ca.phon.plugin.IPluginExtensionPoint;
import ca.phon.plugin.IPluginMenuFilter;
import ca.phon.plugin.PhonPlugin;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.util.PrefHelper;

@PhonPlugin(author="Greg J. Hedlund", comments="Add manual items to Help menu", minPhonVersion="2.2.0", name="HelpMenuHandler", version="1")
public class HelpMenuHandler implements IPluginMenuFilter, IPluginExtensionPoint<IPluginMenuFilter> {

	// use github pages mirror
	

	private final static String ONLINE_MANUAL = "phon-manual/misc/Welcome.html";

	public HelpMenuHandler() {
	}

	public void filterWindowMenu(Window owner, JMenuBar menuBar) {
		JMenu menu = null;
		for(int i = 0; i < menuBar.getMenuCount(); i++) {
			if(menuBar.getMenu(i).getText().equals("Help")) {
				menu = menuBar.getMenu(i);
				break;
			}
		}
		assert menu != null;
		
		final String path = PrefHelper.get(HelpLink.WEBSITE_ROOT_PROP, HelpLink.DEFAULT_WEBSITE_ROOT) + ONLINE_MANUAL;
		final PhonUIAction showOnlineManualAct = new PhonUIAction(HelpMenuHandler.class, "showOnlineManual", path);
		showOnlineManualAct.putValue(PhonUIAction.NAME, "Show manual (online)...");
		showOnlineManualAct.putValue(PhonUIAction.SHORT_DESCRIPTION, path);

		menu.add(new JMenuItem(showOnlineManualAct), 0);
	}

	public static void showOnlineManual(String path) {
		if(Desktop.isDesktopSupported()) {
			try {
				URL url = new URL(path);
				Desktop.getDesktop().browse(url.toURI());
			} catch (IOException | URISyntaxException e1) {
				LogUtil.severe(e1);
			}
		}
	}

	public Class<?> getExtensionType() {
		return IPluginMenuFilter.class;
	}

	public IPluginExtensionFactory<IPluginMenuFilter> getFactory() {
		return (args) -> this;
	}

}