package ca.phon.app.theme;

import javax.swing.plaf.FontUIResource;

import org.pushingpixels.substance.api.fonts.FontSet;

import ca.phon.ui.fonts.FontPreferences;

class PhonUIFontSet implements FontSet {

	public FontUIResource getControlFont() {
		return new FontUIResource(FontPreferences.getControlFont());
	}

	public FontUIResource getMenuFont() {
		return new FontUIResource(FontPreferences.getMenuFont());
	}

	public FontUIResource getMessageFont() {
		return new FontUIResource(FontPreferences.getMenuFont());
	}

	public FontUIResource getSmallFont() {
		return new FontUIResource(FontPreferences.getSmallFont());
	}

	public FontUIResource getTitleFont() {
		return new FontUIResource(FontPreferences.getTierFont());
	}

	public FontUIResource getWindowTitleFont() {
		return new FontUIResource(FontPreferences.getWindowTitleFont());
	}

}