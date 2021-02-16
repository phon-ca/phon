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
package ca.phon.app.about;

import java.awt.*;
import java.io.*;
import java.net.*;

import javax.swing.*;

import ca.phon.app.log.*;
import ca.phon.plugin.*;
import ca.phon.ui.action.*;
import ca.phon.util.*;

@PhonPlugin(author="Greg J. Hedlund", comments="Add manual items to Help menu", minPhonVersion="2.2.0", name="HelpMenuHandler", version="1")
public class HelpMenuHandler implements IPluginMenuFilter, IPluginExtensionPoint<IPluginMenuFilter> {

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
		
		final String path = PrefHelper.get(HelpLink.WEBSITE_ROOT_PROP, HelpLink.DEFAULT_WEBSITE_ROOT);
		final PhonUIAction showOnlineManualAct = new PhonUIAction(HelpMenuHandler.class, "showOnlineManual", path);
		showOnlineManualAct.putValue(PhonUIAction.NAME, "Show manual (online)...");
		showOnlineManualAct.putValue(PhonUIAction.SHORT_DESCRIPTION, path);

		final String licensePath = "https://github.com/phon-ca/phon/blob/master/LICENSE.txt";
		final PhonUIAction showLicenseAct = new PhonUIAction(HelpMenuHandler.class, "showOnlineManual", licensePath);
		showLicenseAct.putValue(PhonUIAction.NAME, "Show license (online)...");
		showLicenseAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Show application license");

		menu.add(new JMenuItem(showOnlineManualAct), 0);
		menu.add(new JMenuItem(showLicenseAct));
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
