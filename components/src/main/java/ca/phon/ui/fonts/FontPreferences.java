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
package ca.phon.ui.fonts;

import java.awt.*;
import java.io.IOException;
import java.text.ParseException;
import java.util.*;
import java.util.prefs.Preferences;

import ca.phon.session.spi.MediaSegmentSPI;
import org.apache.logging.log4j.*;

import ca.phon.ui.*;
import ca.phon.util.*;
import org.jdesktop.swingx.plaf.LoginPaneUI;

public class FontPreferences {
	private final static String CLASSPATH_ROOT = "data/fonts/";

	private final static Map<String, Font> PROVIDED_FONTS = new LinkedHashMap<>();

	public static final String[] SUGGESTED_IPA_FONT_NAMES = {
			"Arial",				// sans
			"Arial Unicode MS",		// sans
			"Charis SIL",			// serif
			"Charis SIL Compact",	// serif
			"Doulos SIL",			// serif
			"Lucida Grande",		// sans
			"Noto Sans",			// sans
			"Noto Serif",			// serif
			"Segoe UI",				// sans
			"Times New Roman"		// serif
	};

	public static final String[] SUGGESTED_MONOSPACE_FONT_NAMES = {
			"Courier New",
			"Menlo",
			"Noto Sans Mono"
	};

	public static final float MIN_FONT_SIZE = 2.0f;
	public static final float MAX_FONT_SIZE = 40.0f;

	public static final Float DEFAULT_FONT_SIZE = 12.0f;
	public final static String DEFAULT_FONT_SIZE_PROP = FontPreferences.class.getName() + ".defaultFontSize";
	public static float getDefaultFontSize() {
		return PrefHelper.getFloat(DEFAULT_FONT_SIZE_PROP, DEFAULT_FONT_SIZE);
	}

	public static void setDefaultFontSize(float fontSize) {
		PrefHelper.getUserPreferences().putFloat(DEFAULT_FONT_SIZE_PROP, fontSize);
	}

	/**
	 * Reset all font preferences
	 */
	public static void resetAll() {
		Preferences userPrefs = PrefHelper.getUserPreferences();

		userPrefs.put(TIER_FONT, DEFAULT_TIER_FONT);
		userPrefs.put(MONOSPACE_FONT, DEFAULT_MONOSPACE_FONT);
		userPrefs.put(TITLE_FONT, DEFAULT_TITLE_FONT);

		// deprecated items
		userPrefs.put(UI_IPA_FONT, DEFAULT_UI_IPA_FONT);
		userPrefs.put(CONTROL_FONT, DEFAULT_CONTROL_FONT);
		userPrefs.put(MENU_FONT, DEFAULT_MENU_FONT);
		userPrefs.put(MESSAGE_DIALOG_FONT, DEFAULT_MESSAGE_DIALOG_FONT);
		userPrefs.put(SMALL_FONT, DEFAULT_SMALL_FONT);
		userPrefs.put(WINDOW_TITLE_FONT, DEFAULT_WINDOW_TITLE_FONT);
	}

	public static void registerFont(String filename, Font font) {
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		ge.registerFont(font);

		PROVIDED_FONTS.put(filename, font);
	}

	/**
	 * Returns font described by given preferences property or the default Dialog font.
	 * This method will provide one of our custom Noto fonts if
	 *
	 * @param fontPref
	 * @return
	 */
	private static Font _getFont(String fontPref, String defaultValue) {
		String fontDesc = PrefHelper.get(fontPref, defaultValue);
		String[] fontInfo = fontDesc.split("-");
		String fontStyle = (fontInfo.length > 2 ? fontInfo[2] : "PLAIN");
		float fontSize = (fontInfo.length > 1 ? Float.parseFloat(fontInfo[1]) : DEFAULT_FONT_SIZE);
		String fontName = (fontInfo.length > 0 ? fontInfo[0] : "Dialog");

		if(!OSInfo.isMacOs() && fontName.startsWith("Noto")) {
			// return one of our custom font files with all necessary symbols
			// as glyph fallback is not performed correctly on windows
			String fontFileName = "Noto";
			switch(fontName) {
				case "Noto Serif":
					fontFileName += "Serif";
					switch(fontStyle.toUpperCase()) {
						case "BOLD":
							fontFileName += "Bold";
							break;

						case "ITALIC":
							fontFileName += "Italic";
							break;

						case "BOLDITALIC":
							fontFileName += "BoldItalic";
							break;

						case "PLAIN":
						default:
							fontFileName += "Regular";
							break;
					}
					break;

				case "Noto Sans Mono":
					fontFileName += "SansMono";
					switch(fontStyle.toUpperCase()) {
						case "BOLD":
							fontFileName += "Bold";
							break;

						case "PLAIN":
						default:
							fontFileName += "Regular";
							break;
					}
					break;

				case "Noto Sans":
				default:
					fontFileName += "Sans-";
					switch(fontStyle.toUpperCase()) {
						case "BOLD":
							fontFileName += "Bold";
							break;

						case "ITALIC":
							fontFileName += "Italic";
							break;

						case "BOLDITALIC":
							fontFileName += "BoldItalic";
							break;

						case "PLAIN":
						default:
							fontFileName += "Regular";
							break;
					}
					break;
			}
			fontFileName += ".ttf";
			return (PROVIDED_FONTS.containsKey(fontFileName) ? PROVIDED_FONTS.get(fontFileName).deriveFont(fontSize) :
					Font.decode("Dialog-12-PLAIN"));
		} else {
			return Font.decode(fontDesc);
		}
	}
	
	/**
	 * Sans-serif Tier font
	 */
	public final static String TIER_FONT = FontPreferences.class.getName() + ".tierFont";

	public final static String DEFAULT_TIER_FONT = "Noto Sans-PLAIN-12";
	
	public static Font getTierFont() {
		return _getFont(TIER_FONT, DEFAULT_TIER_FONT);
	}
	
	public static void setTierFont(Font font) {
		PrefHelper.getUserPreferences().put(TIER_FONT, fontToString(font));
	}

	/**
	 * Title font
	 */
	public final static String TITLE_FONT = FontPreferences.class.getName() + ".titleFont";

	public final static String DEFAULT_TITLE_FONT = "Dialog-PLAIN-14";

	public static Font getTitleFont() {
		return _getFont(TITLE_FONT, DEFAULT_TITLE_FONT);
	}

	public static void setTitleFont(Font font) {
		PrefHelper.getUserPreferences().put(TITLE_FONT, fontToString(font));
	}

	/**
	 * Monospace font
	 */
	public final static String MONOSPACE_FONT = FontPreferences.class.getName() + ".monospaceFont";

	public final static String DEFAULT_MONOSPACE_FONT = "Noto Sans Mono-PLAIN-12";

	public static Font getMonospaceFont() {
		return _getFont(MONOSPACE_FONT, DEFAULT_MONOSPACE_FONT);
	}

	public static void setMonospaceFont(Font font) {
		PrefHelper.getUserPreferences().put(MONOSPACE_FONT, fontToString(font));
	}

	/**
	 * Font size increase
	 */
	public final static String FONT_SIZE_INCREASE = FontPreferences.class.getName() + ".fontSizeIncrease";

	public final static Integer DEFAULT_FONT_SIZE_INCREASE = 0;

	public static Integer getFontSizeIncrease() {
		return PrefHelper.getInt(FONT_SIZE_INCREASE, DEFAULT_FONT_SIZE_INCREASE);
	}

	public static void setFontSizeIncrease(Integer v) {
		PrefHelper.getUserPreferences().putInt(FONT_SIZE_INCREASE, v);
	}

	private static String fontToString(Font font) {
		final FontFormatter formatter = new FontFormatter();
		return formatter.format(font);
	}

	private static Font fontFromString(String txt) throws ParseException {
		final FontFormatter formatter = new FontFormatter();
		return formatter.parse(txt);
	}

	/**
	 * UI-IPA font
	 */
	@Deprecated
	public static final String UI_IPA_FONT = FontPreferences.class.getName() + ".uiIpaFont";

	@Deprecated
	public static final String DEFAULT_UI_IPA_FONT = "Noto Sans-PLAIN-12";

	@Deprecated
	public static Font getUIIpaFont() {
		return PrefHelper.getFont(UI_IPA_FONT, Font.decode(DEFAULT_UI_IPA_FONT));
	}

	@Deprecated
	public static void setUIIpaFont(Font font) {
		PrefHelper.getUserPreferences().put(UI_IPA_FONT, fontToString(font));
	}


	/**
	 * Control font - used for all dialog controls
	 */
	@Deprecated
	public final static String CONTROL_FONT = FontPreferences.class.getName() + ".controlFont";

	@Deprecated
	public final static String DEFAULT_CONTROL_FONT = "Dialog-PLAIN-12";

	@Deprecated
	public static Font getControlFont() {
		return PrefHelper.getFont(CONTROL_FONT, Font.decode(DEFAULT_CONTROL_FONT));
	}

	@Deprecated
	public static void setControlFont(Font font) {
		PrefHelper.getUserPreferences().put(CONTROL_FONT, fontToString(font));
	}
	
	/**
	 * Menu font
	 */
	@Deprecated
	public final static String MENU_FONT = FontPreferences.class.getName() + ".menuFont";

	@Deprecated
	public final static String DEFAULT_MENU_FONT = "Dialog-PLAIN-12";

	@Deprecated
	public static Font getMenuFont() {
		return PrefHelper.getFont(MENU_FONT, Font.decode(DEFAULT_MENU_FONT));
	}

	@Deprecated
	public static void setMenuFont(Font font) {
		PrefHelper.getUserPreferences().put(MENU_FONT, fontToString(font));
	}
	
	/**
	 * Message dialog font
	 */
	@Deprecated
	public final static String MESSAGE_DIALOG_FONT = FontPreferences.class.getName() + ".messageDialogFont";

	@Deprecated
	public final static String DEFAULT_MESSAGE_DIALOG_FONT = "Dialog-PLAIN-12";

	@Deprecated
	public static Font getMessageDialogFont() {
		return PrefHelper.getFont(MESSAGE_DIALOG_FONT, Font.decode(DEFAULT_MESSAGE_DIALOG_FONT));
	}

	@Deprecated
	public static void setMessageDialogFont(Font font) {
		PrefHelper.getUserPreferences().put(MESSAGE_DIALOG_FONT, fontToString(font));
	}
	
	/**
	 * Small font - used for tool tips
	 */
	@Deprecated
	public final static String SMALL_FONT = FontPreferences.class.getName() + ".smallFont";

	@Deprecated
	public final static String DEFAULT_SMALL_FONT = "Dialog-PLAIN-11";

	@Deprecated
	public static Font getSmallFont() {
		return PrefHelper.getFont(SMALL_FONT, Font.decode(DEFAULT_SMALL_FONT));
	}

	@Deprecated
	public static void setSmallFont(Font font) {
		PrefHelper.getUserPreferences().put(SMALL_FONT, fontToString(font));
	}
	
	/**
	 * Window title font
	 */
	@Deprecated
	public final static String WINDOW_TITLE_FONT = FontPreferences.class.getName() + ".windowTitleFont";

	@Deprecated
	public final static String DEFAULT_WINDOW_TITLE_FONT = "Dialog-BOLD-14";

	@Deprecated
	public static Font getWindowTitleFont() {
		return PrefHelper.getFont(WINDOW_TITLE_FONT, Font.decode(DEFAULT_WINDOW_TITLE_FONT));
	}

	@Deprecated
	public static void setWindowTitleFont(Font font) {
		PrefHelper.getUserPreferences().put(WINDOW_TITLE_FONT, fontToString(font));
	}
	
}
