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
import ca.phon.app.log.LogUtil;
import ca.phon.app.prefs.PhonProperties;
import ca.phon.plugin.*;
import ca.phon.ui.fonts.FontPreferences;
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
					} else if(OSInfo.isWindows()) {
						setupWindowsCustomizations();
					}
					
					try {
						 String uiClassName = PrefHelper.get(
								PhonProperties.UI_THEME,
								OSInfo.isNix() ? null : 
										UIManager.getSystemLookAndFeelClassName()
								);
						if(uiClassName != null) {
							LogUtil.info("Installing L&F " + uiClassName);
							UIManager.setLookAndFeel(uiClassName);
						}
					} catch (UnsupportedLookAndFeelException | ClassNotFoundException | InstantiationException | IllegalAccessException e) {
						LogUtil.severe( e.getLocalizedMessage(), e);
					}

					for (String key : uiMap.keySet()) {
						UIManager.put(key, uiMap.get(key));
					}
				}
			});
		} catch (InterruptedException | InvocationTargetException e) {
			LogUtil.severe(e.getLocalizedMessage(), e);
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

	private void setupWindowsCustomizations() {
		UIManager.put("Button.font", Font.decode(FontPreferences.DEFAULT_TIER_FONT));
		UIManager.put("ToggleButton.font", Font.decode(FontPreferences.DEFAULT_TIER_FONT));
		UIManager.put("RadioButton.font", Font.decode(FontPreferences.DEFAULT_TIER_FONT));
		UIManager.put("CheckBox.font", Font.decode(FontPreferences.DEFAULT_TIER_FONT));
		UIManager.put("ColorChooser.font", Font.decode(FontPreferences.DEFAULT_TIER_FONT));
		UIManager.put("ComboBox.font", Font.decode(FontPreferences.DEFAULT_TIER_FONT));
		UIManager.put("Label.font", Font.decode(FontPreferences.DEFAULT_TIER_FONT));
		UIManager.put("List.font", Font.decode(FontPreferences.DEFAULT_TIER_FONT));
		UIManager.put("MenuBar.font", Font.decode(FontPreferences.DEFAULT_TIER_FONT));
		UIManager.put("MenuItem.font", Font.decode(FontPreferences.DEFAULT_TIER_FONT));
		UIManager.put("RadioButtonMenuItem.font", Font.decode(FontPreferences.DEFAULT_TIER_FONT));
		UIManager.put("CheckBoxMenuItem.font", Font.decode(FontPreferences.DEFAULT_TIER_FONT));
		UIManager.put("Menu.font", Font.decode(FontPreferences.DEFAULT_TIER_FONT));
		UIManager.put("PopupMenu.font", Font.decode(FontPreferences.DEFAULT_TIER_FONT));
		UIManager.put("OptionPane.font", Font.decode(FontPreferences.DEFAULT_TIER_FONT));
		UIManager.put("Panel.font", Font.decode(FontPreferences.DEFAULT_TIER_FONT));
		UIManager.put("ProgressBar.font", Font.decode(FontPreferences.DEFAULT_TIER_FONT));
		UIManager.put("ScrollPane.font", Font.decode(FontPreferences.DEFAULT_TIER_FONT));
		UIManager.put("Viewport.font", Font.decode(FontPreferences.DEFAULT_TIER_FONT));
		UIManager.put("TabbedPane.font", Font.decode(FontPreferences.DEFAULT_TIER_FONT));
		UIManager.put("Table.font", Font.decode(FontPreferences.DEFAULT_TIER_FONT));
		UIManager.put("TableHeader.font", Font.decode(FontPreferences.DEFAULT_TIER_FONT));
		UIManager.put("TextField.font", Font.decode(FontPreferences.DEFAULT_TIER_FONT));
		UIManager.put("PasswordField.font", Font.decode(FontPreferences.DEFAULT_TIER_FONT));
		UIManager.put("TextArea.font", Font.decode(FontPreferences.DEFAULT_TIER_FONT));
		UIManager.put("TextPane.font", Font.decode(FontPreferences.DEFAULT_TIER_FONT));
		UIManager.put("EditorPane.font", Font.decode(FontPreferences.DEFAULT_TIER_FONT));
		UIManager.put("TitledBorder.font", Font.decode(FontPreferences.DEFAULT_TIER_FONT));
		UIManager.put("ToolBar.font", Font.decode(FontPreferences.DEFAULT_TIER_FONT));
		UIManager.put("ToolTip.font", Font.decode(FontPreferences.DEFAULT_TIER_FONT));
		UIManager.put("Tree.font", Font.decode(FontPreferences.DEFAULT_TIER_FONT));
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
