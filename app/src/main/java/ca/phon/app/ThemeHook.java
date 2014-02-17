package ca.phon.app;

import java.awt.GraphicsEnvironment;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.pushingpixels.substance.api.SubstanceLookAndFeel;
import org.pushingpixels.substance.api.skin.SubstanceAutumnLookAndFeel;
import org.pushingpixels.substance.api.skin.SubstanceBusinessBlueSteelLookAndFeel;
import org.pushingpixels.substance.api.skin.SubstanceBusinessLookAndFeel;
import org.pushingpixels.substance.api.skin.SubstanceCeruleanLookAndFeel;
import org.pushingpixels.substance.api.skin.SubstanceCremeCoffeeLookAndFeel;
import org.pushingpixels.substance.api.skin.SubstanceCremeLookAndFeel;
import org.pushingpixels.substance.api.skin.SubstanceGeminiLookAndFeel;

import ca.phon.app.hooks.PhonStartupHook;
import ca.phon.app.prefs.PhonProperties;
import ca.phon.media.player.PhonPlayerCanvas;
import ca.phon.plugin.IPluginExtensionFactory;
import ca.phon.plugin.IPluginExtensionPoint;
import ca.phon.plugin.PluginException;
import ca.phon.util.OSInfo;
import ca.phon.util.PrefHelper;

/**
 * Sets UI theme
 *
 */
public class ThemeHook implements PhonStartupHook, IPluginExtensionPoint<PhonStartupHook> {
	
	private final static Logger LOGGER = Logger.getLogger(ThemeHook.class.getName());
	
	@Override
	public void startup() throws PluginException {
		if(GraphicsEnvironment.isHeadless()) return;
		
		try {
			SwingUtilities.invokeAndWait(new Runnable() {
				public void run() {
					try {
						final String uiClassName = PrefHelper.get(PhonProperties.UI_THEME, 
								OSInfo.isMacOs() ? null : SubstanceCeruleanLookAndFeel.class.getName());
						if(uiClassName != null) {
							UIManager.setLookAndFeel(uiClassName);
							UIManager.put(SubstanceLookAndFeel.COLORIZATION_FACTOR, 1.0);
						}
					} catch (UnsupportedLookAndFeelException e) {
						LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
					} catch (ClassNotFoundException e) {
						LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
					} catch (InstantiationException e) {
						LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
					} catch (IllegalAccessException e) {
						LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
					}
				}
			});
		} catch (InterruptedException e) {
			LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
		} catch (InvocationTargetException e) {
			LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
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
			return ThemeHook.this;
		}
	};
	
}
