package ca.phon.app;

import java.awt.*;
import java.io.*;

import ca.phon.app.actions.*;
import ca.phon.app.hooks.*;
import ca.phon.app.log.*;
import ca.phon.app.modules.*;
import ca.phon.plugin.*;
import ca.phon.util.*;

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
					Toolkit.getDefaultToolkit().beep();
					LogUtil.severe(ex);
				}
			});
			
			desktop.setPreferencesHandler( (e) -> {
				try {
					PluginEntryPointRunner.executePlugin("Preferences");
				} catch (PluginException ex) {
					Toolkit.getDefaultToolkit().beep();
					LogUtil.severe(ex);
				}
			});
			
			desktop.setOpenFileHandler( (e) -> {
				for(File file:e.getFiles()) {
					EntryPointArgs args = new EntryPointArgs();
					args.put(OpenFileEP.INPUT_FILE, file);
					try {
						PluginEntryPointRunner.executePlugin(OpenFileEP.EP_NAME, args);
					} catch (PluginException ex) {
						Toolkit.getDefaultToolkit().beep();
						LogUtil.severe(ex);
					}
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
