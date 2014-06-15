package ca.phon.app;

import java.awt.GraphicsEnvironment;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.pushingpixels.lafwidget.LafWidget;
import org.pushingpixels.substance.api.SubstanceLookAndFeel;
import org.pushingpixels.substance.api.fonts.FontPolicy;
import org.pushingpixels.substance.api.fonts.SubstanceFontUtilities;
import org.pushingpixels.substance.api.skin.SubstanceCeruleanLookAndFeel;

import ca.phon.app.hooks.PhonStartupHook;
import ca.phon.app.prefs.PhonProperties;
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
					final Map<String, Object> uiMap = new HashMap<String, Object>();
					// keep mac OS X menu bars
					if(OSInfo.isMacOs()) {
						final String[] uiKeys = new String[]{
								"MenuBarUI" };
						for(String key:uiKeys) {
							uiMap.put(key, UIManager.get(key));
						}
					}
					try {
						final String uiClassName = PrefHelper.get(PhonProperties.UI_THEME, SubstanceCeruleanLookAndFeel.class.getName());
						if(uiClassName != null) {
							UIManager.setLookAndFeel(uiClassName);
							UIManager.put(SubstanceLookAndFeel.COLORIZATION_FACTOR, 1.0);
							UIManager.put(SubstanceLookAndFeel.SHOW_EXTRA_WIDGETS, Boolean.TRUE);
							UIManager.put(LafWidget.TEXT_EDIT_CONTEXT_MENU, Boolean.TRUE);
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
					
					if(OSInfo.isMacOs()) {
						for(String key:uiMap.keySet()) {
							UIManager.put(key, uiMap.get(key));
						}
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
