package ca.phon.app.fonts;

import java.awt.Font;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.SwingUtilities;
import javax.swing.UIDefaults;
import javax.swing.plaf.FontUIResource;

import org.pushingpixels.substance.api.SubstanceLookAndFeel;
import org.pushingpixels.substance.api.fonts.FontPolicy;
import org.pushingpixels.substance.api.fonts.FontSet;

import ca.phon.ui.FontFormatter;
import ca.phon.util.PrefHelper;

public class FontPreferences {
	
	private static final Logger LOGGER = Logger
			.getLogger(FontPreferences.class.getName());
	
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
	
	public final static String DEFAULT_TITLE_FONT = "Liberation Sans-BOLD-14";
	
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
	
	public static void setupFontPreferences() {
		final Runnable onEDT = new Runnable() {
			
			@Override
			public void run() {
				SubstanceLookAndFeel.setFontPolicy(null);
	            
	              // Create the wrapper font set
	              FontPolicy newFontPolicy = new FontPolicy() {
	                public FontSet getFontSet(String lafName,
	                    UIDefaults table) {
	                  return new PhonUIFontSet();
	                }
	              };

				SubstanceLookAndFeel.setFontPolicy(newFontPolicy);
			}
			
		};
		if(SwingUtilities.isEventDispatchThread())
			onEDT.run();
		else
			try {
				SwingUtilities.invokeAndWait(onEDT);
			} catch (InvocationTargetException e) {
				LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
			} catch (InterruptedException e) {
				LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
			}
	}
	
	private static class PhonUIFontSet implements FontSet {
		
		public FontUIResource getSizedFont(Font font) {
			final FontUIResource retVal = new FontUIResource(font.getName(),
					font.getStyle(), font.getSize() + FontPreferences.getFontSizeIncrease());
			return retVal;
		}

		public FontUIResource getControlFont() {
			return getSizedFont(FontPreferences.getControlFont());
		}

		public FontUIResource getMenuFont() {
			return getSizedFont(FontPreferences.getMenuFont());
		}

		public FontUIResource getMessageFont() {
			return getSizedFont(FontPreferences.getMenuFont());
		}

		public FontUIResource getSmallFont() {
			return getSizedFont(FontPreferences.getSmallFont());
		}

		public FontUIResource getTitleFont() {
			return getSizedFont(FontPreferences.getTierFont());
		}

		public FontUIResource getWindowTitleFont() {
			return getSizedFont(FontPreferences.getWindowTitleFont());
		}

	}
}
