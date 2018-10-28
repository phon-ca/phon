package ca.phon.app;

import java.awt.Desktop;
import java.awt.desktop.QuitEvent;
import java.awt.desktop.QuitHandler;
import java.awt.desktop.QuitResponse;

import ca.phon.app.hooks.PhonStartupHook;
import ca.phon.app.log.LogUtil;
import ca.phon.plugin.IPluginExtensionFactory;
import ca.phon.plugin.IPluginExtensionPoint;
import ca.phon.plugin.PluginEntryPointRunner;
import ca.phon.plugin.PluginException;
import ca.phon.util.OSInfo;

/**
 * 
 */
public class PhonDesktopStartupHook implements PhonStartupHook, IPluginExtensionPoint<PhonStartupHook> {

	@Override
	public void startup() throws PluginException {
		Desktop desktop = Desktop.getDesktop();
		
		if(Desktop.isDesktopSupported() && OSInfo.isMacOs()) {
			desktop.setQuitHandler( 			
				(e, r) -> {
					try {
						PluginEntryPointRunner.executePlugin("Exit");
						r.performQuit();
					} catch (PluginException e1) {
						LogUtil.severe(e1);
						r.cancelQuit();
					}
				}
			);
			
			desktop.setAboutHandler( (e) -> {
				try {
					PluginEntryPointRunner.executePlugin("Help");
				} catch (PluginException ex) {
					LogUtil.severe(ex);
				}
			});
			
			desktop.setPreferencesHandler( (e) -> {
				try {
					PluginEntryPointRunner.executePlugin("Preferences");
				} catch (PluginException ex) {
					LogUtil.severe(ex);
				}
			});
		}
	}

	@Override
	public Class<?> getExtensionType() {
		return PhonStartupHook.class;
	}

	@Override
	public IPluginExtensionFactory<PhonStartupHook> getFactory() {
		return (args) -> this;
	} 

}
