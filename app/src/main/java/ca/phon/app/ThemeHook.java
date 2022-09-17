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
package ca.phon.app;

import ca.phon.app.hooks.PhonStartupHook;
import ca.phon.app.prefs.PhonProperties;
import ca.phon.plugin.*;
import ca.phon.util.*;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * Sets UI theme
 *
 */
public class ThemeHook implements PhonStartupHook,
		IPluginExtensionPoint<PhonStartupHook> {

	private final static org.apache.logging.log4j.Logger LOGGER = org.apache.logging.log4j.LogManager.getLogger(ThemeHook.class
			.getName());

	@Override
	public void startup() throws PluginException {
		if (GraphicsEnvironment.isHeadless())
			return;
		
		try {
			SwingUtilities.invokeAndWait(new Runnable() {
				public void run() {
					final Map<String, Object> uiMap = new HashMap<String, Object>();
					// don't change theme on mac
					if (OSInfo.isMacOs()) {
						setupMacosCustomizations();
						return;
					}
					
					try {
						 String uiClassName = PrefHelper.get(
								PhonProperties.UI_THEME,
								OSInfo.isNix() ? null : 
										UIManager.getSystemLookAndFeelClassName()
								);
						if(uiClassName != null) {
							LOGGER.info("Installing L&F " + uiClassName);
							UIManager.setLookAndFeel(uiClassName);
						}
					} catch (UnsupportedLookAndFeelException e) {
						LOGGER.error( e.getLocalizedMessage(), e);
					} catch (ClassNotFoundException e) {
						LOGGER.error( e.getLocalizedMessage(), e);
					} catch (InstantiationException e) {
						LOGGER.error( e.getLocalizedMessage(), e);
					} catch (IllegalAccessException e) {
						LOGGER.error( e.getLocalizedMessage(), e);
					}

					for (String key : uiMap.keySet()) {
						UIManager.put(key, uiMap.get(key));
					}
				}
			});
		} catch (InterruptedException e) {
			LOGGER.error( e.getLocalizedMessage(), e);
		} catch (InvocationTargetException e) {
			LOGGER.error( e.getLocalizedMessage(), e);
		}

	}

	// macos system colors
	final static private Color MACOS_GRAPHITE = new Color(109, 109, 109);
	final static private Color MACOS_BLUE = new Color(7, 73, 217);
	
	private void setupMacosCustomizations() {
		// check list selection colours, change breadcrumb selection color to MACOS_BLUE if 
		// user selection is not 'blue' or 'graphite' in system preferences
		Color listSelectionBackground = UIManager.getColor("List.selectionBackground");
		if( !MACOS_BLUE.equals(listSelectionBackground) && !MACOS_GRAPHITE.equals(listSelectionBackground) ) {
			UIManager.getDefaults().put("List.selectionBackground", MACOS_BLUE);
			UIManager.getDefaults().put("Tree.selectionBackground", MACOS_BLUE);
		}

		// fix selected tab foreground color
		Color listSelectionForeground = UIManager.getColor("List.selectionForeground");
		UIManager.put("TabbedPane.foreground", Color.black);
	}
	
	@Override
	public Class<?> getExtensionType() {
		return PhonStartupHook.class;
	}

	@Override
	public IPluginExtensionFactory<PhonStartupHook> getFactory() {
		return factory;
	}

	private final IPluginExtensionFactory<PhonStartupHook> factory = new IPluginExtensionFactory<PhonStartupHook>() {

		@Override
		public PhonStartupHook createObject(Object... args) {
			return ThemeHook.this;
		}
	};

}
