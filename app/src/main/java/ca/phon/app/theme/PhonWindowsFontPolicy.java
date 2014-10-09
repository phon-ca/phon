package ca.phon.app.theme;

import javax.swing.UIDefaults;
import javax.swing.plaf.FontUIResource;

import ca.phon.ui.fonts.FontPreferences;

import com.jgoodies.looks.FontPolicy;
import com.jgoodies.looks.FontSet;

public class PhonWindowsFontPolicy implements FontPolicy {

	@Override
	public FontSet getFontSet(String lafName, UIDefaults table) {
		return new FontSet() {
			
			@Override
			public FontUIResource getWindowTitleFont() {
				return new FontUIResource(FontPreferences.getWindowTitleFont());
			}
			
			@Override
			public FontUIResource getTitleFont() {
				return new FontUIResource(FontPreferences.getTitleFont());
			}
			
			@Override
			public FontUIResource getSmallFont() {
				return new FontUIResource(FontPreferences.getSmallFont());
			}
			
			@Override
			public FontUIResource getMessageFont() {
				return new FontUIResource(FontPreferences.getMessageDialogFont());
			}
			
			@Override
			public FontUIResource getMenuFont() {
				return new FontUIResource(FontPreferences.getMenuFont());
			}
			
			@Override
			public FontUIResource getControlFont() {
				return new FontUIResource(FontPreferences.getControlFont());
			}
		};
	}

}
