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
package ca.phon.ui.fonts;

import java.awt.Font;

import org.apache.logging.log4j.LogManager;

import ca.phon.ui.FontFormatter;
import ca.phon.util.PrefHelper;

public class FontPreferences {
	
	private static final org.apache.logging.log4j.Logger LOGGER = LogManager.getLogger(FontPreferences.class.getName());
	
	/**
	 * Tier font
	 */
	public final static String TIER_FONT = FontPreferences.class.getName() + ".tierFont";
	
	public final static String DEFAULT_TIER_FONT = "Charis SIL-PLAIN-12";
	
	public static Font getTierFont() {
		return PrefHelper.getFont(TIER_FONT, Font.decode(DEFAULT_TIER_FONT));
	}
	
	public static void setTierFont(Font font) {
		PrefHelper.getUserPreferences().put(TIER_FONT, fontToString(font));
	}
	
	/**
	 * UI-IPA font
	 */
	public static final String UI_IPA_FONT = FontPreferences.class.getName() + ".uiIpaFont";
	
	public static final String DEFAULT_UI_IPA_FONT = "Charis SIL Compact-PLAIN-12";
	
	public static Font getUIIpaFont() {
		return PrefHelper.getFont(UI_IPA_FONT, Font.decode(DEFAULT_UI_IPA_FONT));
	}
	
	public static void setUIIpaFont(Font font) {
		PrefHelper.getUserPreferences().put(UI_IPA_FONT, fontToString(font));
	}
	
	/**
	 * Control font - used for all dialog controls
	 */
	public final static String CONTROL_FONT = FontPreferences.class.getName() + ".controlFont";
	
	public final static String DEFAULT_CONTROL_FONT = "Liberation Sans-PLAIN-12";
	
	public static Font getControlFont() {
		return PrefHelper.getFont(CONTROL_FONT, Font.decode(DEFAULT_CONTROL_FONT));
	}
	
	public static void setControlFont(Font font) {
		PrefHelper.getUserPreferences().put(CONTROL_FONT, fontToString(font));
	}
	
	/**
	 * Menu font
	 */
	public final static String MENU_FONT = FontPreferences.class.getName() + ".menuFont";
	
	public final static String DEFAULT_MENU_FONT = "Liberation Sans-PLAIN-12";
	
	public static Font getMenuFont() {
		return PrefHelper.getFont(MENU_FONT, Font.decode(DEFAULT_MENU_FONT));
	}
	
	public static void setMenuFont(Font font) {
		PrefHelper.getUserPreferences().put(MENU_FONT, fontToString(font));
	}
	
	/**
	 * Message dialog font
	 */
	public final static String MESSAGE_DIALOG_FONT = FontPreferences.class.getName() + ".messageDialogFont";
	
	public final static String DEFAULT_MESSAGE_DIALOG_FONT = "Liberation Sans-PLAIN-12";
	
	public static Font getMessageDialogFont() {
		return PrefHelper.getFont(MESSAGE_DIALOG_FONT, Font.decode(DEFAULT_MESSAGE_DIALOG_FONT));
	}
	
	public static void setMessageDialogFont(Font font) {
		PrefHelper.getUserPreferences().put(MESSAGE_DIALOG_FONT, fontToString(font));
	}
	
	/**
	 * Small font - used for tool tips
	 */
	public final static String SMALL_FONT = FontPreferences.class.getName() + ".smallFont";
	
	public final static String DEFAULT_SMALL_FONT = "Liberation Sans-PLAIN-11";

	public static Font getSmallFont() {
		return PrefHelper.getFont(SMALL_FONT, Font.decode(DEFAULT_SMALL_FONT));
	}
	
	public static void setSmallFont(Font font) {
		PrefHelper.getUserPreferences().put(SMALL_FONT, fontToString(font));
	}
	
	/**
	 * Title font
	 */
	public final static String TITLE_FONT = FontPreferences.class.getName() + ".titleFont";
	
	public final static String DEFAULT_TITLE_FONT = "Liberation Sans-PLAIN-14";
	
	public static Font getTitleFont() {
		return PrefHelper.getFont(TITLE_FONT, Font.decode(DEFAULT_TITLE_FONT));
	}
	
	public static void setTitleFont(Font font) {
		PrefHelper.getUserPreferences().put(TITLE_FONT, fontToString(font));
	}
	
	/**
	 * Window title font
	 */
	public final static String WINDOW_TITLE_FONT = FontPreferences.class.getName() + ".windowTitleFont";
	
	public final static String DEFAULT_WINDOW_TITLE_FONT = "Liberation Sans-BOLD-14";
	
	public static Font getWindowTitleFont() {
		return PrefHelper.getFont(WINDOW_TITLE_FONT, Font.decode(DEFAULT_WINDOW_TITLE_FONT));
	}
	
	public static void setWindowTitleFont(Font font) {
		PrefHelper.getUserPreferences().put(WINDOW_TITLE_FONT, fontToString(font));
	}
	
	/**
	 * Monospace font
	 */
	public final static String MONOSPACE_FONT = FontPreferences.class.getName() + ".monospaceFont";
	
	public final static String DEFAULT_MONOSPACE_FONT = "Liberation Mono-PLAIN-12";
	
	public static Font getMonospaceFont() {
		return PrefHelper.getFont(MONOSPACE_FONT, Font.decode(DEFAULT_MONOSPACE_FONT));
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
	
}