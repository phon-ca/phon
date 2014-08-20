package ca.phon.app.fonts;

import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GraphicsEnvironment;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.SwingUtilities;
import javax.swing.UIDefaults;
import javax.swing.plaf.FontUIResource;

import org.pushingpixels.substance.api.SubstanceLookAndFeel;
import org.pushingpixels.substance.api.fonts.FontPolicy;
import org.pushingpixels.substance.api.fonts.FontSet;

import ca.phon.app.hooks.PhonStartupHook;
import ca.phon.plugin.IPluginExtensionFactory;
import ca.phon.plugin.IPluginExtensionPoint;
import ca.phon.plugin.PluginException;

public class FontLoaderStartupHook implements PhonStartupHook, IPluginExtensionPoint<PhonStartupHook> {
	
	private static final Logger LOGGER = Logger
			.getLogger(FontLoaderStartupHook.class.getName());
	
	private final static String FONT_LIST = "data/fonts/fonts.list";

	@Override
	public void startup() throws PluginException {
		loadFonts();
		setupFontPreferences();
	}
	
	private void setupFontPreferences() {
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
	
	private void loadFonts() throws PluginException {
		final InputStream fontListStream = getClass().getClassLoader().getResourceAsStream(FONT_LIST);
		if(fontListStream != null) {
			final GraphicsEnvironment ge = 
					GraphicsEnvironment.getLocalGraphicsEnvironment();
			final BufferedReader in = new BufferedReader(new InputStreamReader(fontListStream));
			String line = null;
			try {
				while((line = in.readLine()) != null) {
					LOGGER.info("Loading font " + line);
					final InputStream fontInputStream = getClass().getClassLoader().getResourceAsStream(line);
					if(fontInputStream != null) {
						try {
							if(!ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, fontInputStream))) {
								LOGGER.info(line + " provided by system");
							}
							fontInputStream.close();
						} catch (FontFormatException e) {
							LOGGER.log(Level.SEVERE,
									e.getLocalizedMessage(), e);
						}
					} else {
						LOGGER.warning("Font not found: " + line);
					}
				}
				fontListStream.close();
			} catch (IOException e) {
				throw new PluginException(e);
			}
		} else {
			throw new PluginException(new FileNotFoundException(FONT_LIST));
		}
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
			return FontLoaderStartupHook.this;
		}
	};
	
	private static class PhonUIFontSet implements FontSet {
		
		public FontUIResource getSizedFont(Font font) {
			final FontUIResource retVal = new FontUIResource(font);
			retVal.deriveFont((float)(retVal.getSize() + FontPreferences.getFontSizeIncrease()));
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
