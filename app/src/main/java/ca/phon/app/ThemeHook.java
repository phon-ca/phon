package ca.phon.app;

import java.awt.GraphicsEnvironment;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import ca.phon.app.hooks.PhonStartupHook;
import ca.phon.plugin.IPluginExtensionFactory;
import ca.phon.plugin.IPluginExtensionPoint;
import ca.phon.plugin.PluginException;
import ca.phon.util.OSInfo;

/**
 * Sets UI theme
 * @author Greg
 *
 */
public class ThemeHook implements PhonStartupHook, IPluginExtensionPoint<PhonStartupHook> {

	private final static Logger LOGGER = Logger.getLogger(ThemeHook.class.getName());
	
	@Override
	public void startup() throws PluginException {
		if(GraphicsEnvironment.isHeadless()) return;
		
		try {
			if(OSInfo.isWindows()) {
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			} else if(OSInfo.isNix()) {
				UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
			}
		} catch (ClassNotFoundException e) {
			LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
		} catch (InstantiationException e) {
			LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
		} catch (IllegalAccessException e) {
			LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
		} catch (UnsupportedLookAndFeelException e) {
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
