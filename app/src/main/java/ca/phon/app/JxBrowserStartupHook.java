package ca.phon.app;

import java.io.File;
import java.util.ArrayList;

import com.teamdev.jxbrowser.chromium.BrowserPreferences;
import com.teamdev.jxbrowser.chromium.ProductInfo;

import ca.phon.app.hooks.PhonStartupHook;
import ca.phon.app.log.LogUtil;
import ca.phon.plugin.IPluginExtensionFactory;
import ca.phon.plugin.IPluginExtensionPoint;
import ca.phon.plugin.PluginException;
import ca.phon.util.OSInfo;
import ca.phon.util.PrefHelper;

public class JxBrowserStartupHook implements PhonStartupHook, IPluginExtensionPoint<PhonStartupHook> {
	
	private final static int REMOTE_DEBUGGING_PORT = 9222;
	
	private final static String CHROMIUM_FOLDER = PrefHelper.getUserDataFolder() + File.separator + "chromium";

	@Override
	public Class<?> getExtensionType() {
		return PhonStartupHook.class;
	}

	@Override
	public IPluginExtensionFactory<PhonStartupHook> getFactory() {
		return (args) -> this;
	}

	@Override
	public void startup() throws PluginException {
		LogUtil.info("Using JxBrowser version: " + ProductInfo.getVersion());
		
		// setup chromium folder
		LogUtil.info(String.format("Chromium folder: %s",CHROMIUM_FOLDER));
		BrowserPreferences.setChromiumDir(CHROMIUM_FOLDER);
		
		// setup chromium switches before any browsers are started
		var switches = new ArrayList<>();
		if(!OSInfo.isMacOs()) {
			/*
			 * accelerated lightweight rendering
			 * https://jxbrowser.support.teamdev.com/support/solutions/articles/9000104965-accelerated-lightweight-rendering
			 */
			LogUtil.info("Chormium accelerated lightweight rendering");
			switches.add("--disable-gpu");
			switches.add("--disable-gpu-compositing");
			switches.add("--enable-begin-frame-scheduling");
			switches.add("--software-rendering-fps=60");
		}
		if(PrefHelper.getBoolean("phon.debug", false)) {
			// setup remote debugging port
			LogUtil.info(String.format("Chromium remote debugging port: %d",REMOTE_DEBUGGING_PORT));
			switches.add(String.format("--remote-debugging-port=%d",REMOTE_DEBUGGING_PORT));
		}
		if(switches.size() > 0) {
			BrowserPreferences.setChromiumSwitches(switches.toArray(new String[0]));
		}
	}

}
