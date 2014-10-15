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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.SwingUtilities;
import javax.swing.UIDefaults;

import org.pushingpixels.substance.api.SubstanceLookAndFeel;
import org.pushingpixels.substance.api.fonts.FontPolicy;
import org.pushingpixels.substance.api.fonts.FontSet;

import ca.phon.app.hooks.PhonStartupHook;
import ca.phon.plugin.IPluginExtensionFactory;
import ca.phon.plugin.IPluginExtensionPoint;
import ca.phon.plugin.PluginException;
import ca.phon.ui.nativedialogs.MessageDialogProperties;
import ca.phon.ui.nativedialogs.NativeDialogAdapter;
import ca.phon.ui.nativedialogs.NativeDialogs;
import ca.phon.util.OpenFileLauncher;

public class FontLoaderStartupHook implements PhonStartupHook, IPluginExtensionPoint<PhonStartupHook> {
	
	private static final Logger LOGGER = Logger
			.getLogger(FontLoaderStartupHook.class.getName());
	
	private final static String FONT_LIST = "data/fonts/fonts.list";
	
	private final static String FONT_INFO_PAGE = "https://www.phon.ca/phontrac/wiki/Fonts";

	@Override
	public void startup() throws PluginException {
		checkFonts();
//		loadFonts();
	}
	
	private void checkFonts() {
		final String[] fontNames = new String[] {
			"Liberation Sans", "Liberation Mono", "Charis SIL", "Charis SIL Compact"	
		};
		final Set<String> fontSet = new HashSet<String>();
		fontSet.addAll(Arrays.asList(fontNames));
		final String[] allFonts = 
				GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
		for(String systemFont:allFonts) {
			if(fontSet.contains(systemFont)) {
				fontSet.remove(systemFont);
			}
		}
		
		if(fontSet.size() > 0) {
			String message = "The following fonts were not found on your computer:";
			for(String fontName:fontSet) {
				message += " " + fontName;
			}
			message += ". For best results, please install these fonts.";
			final MessageDialogProperties props = new MessageDialogProperties();
			props.setMessage(message);
			props.setTitle("Unable to find required fonts");
			props.setHeader("Unable to find required fonts");
			props.setOptions(new String[]{"More Information", "Ok"});
			props.setRunAsync(false);
			
			int retVal = NativeDialogs.showMessageDialog(props);
			if(retVal == 0) {
				// show information page
				try {
					OpenFileLauncher.openURL(new URL(FONT_INFO_PAGE));
				} catch (MalformedURLException e) {
					LOGGER
							.log(Level.SEVERE, e.getLocalizedMessage(), e);
				}
			}
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

}
