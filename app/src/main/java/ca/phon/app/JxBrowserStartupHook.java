/*
 * Copyright (C) 2005-2020 Gregory Hedlund & Yvan Rose
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at

 *    http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ca.phon.app;

import ca.phon.app.hooks.PhonStartupHook;
import ca.phon.app.log.LogUtil;
import ca.phon.plugin.*;
import ca.phon.util.*;
import com.teamdev.jxbrowser.chromium.*;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Startup hook for JxBrowser library
 *
 */
public class JxBrowserStartupHook implements PhonStartupHook, IPluginExtensionPoint<PhonStartupHook> {
	
	public static final String ACCELERATED_LIGHTWEIGHT_RENDERING = "jxbrowser.acceleratedLightweightRendering";
	public static final boolean DEFAULT_ACCELERATED_LIGHTWEIGHT_RENDERING = false;
	
	private final static int REMOTE_DEBUGGING_PORT = 9222;
	
	public static final String CHROMIUM_FOLDER = "jxbrowser.chromiumFolder";
	public final static String DEFAULT_CHROMIUM_FOLDER = PrefHelper.getUserDataFolder() + File.separator + "chromium";
	
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
		BrowserPreferences.setChromiumDir(PrefHelper.get(CHROMIUM_FOLDER, DEFAULT_CHROMIUM_FOLDER));
		
		// setup chromium switches before any browsers are started
		List<String> switches = new ArrayList<>();
		if(!OSInfo.isMacOs() && PrefHelper.getBoolean(ACCELERATED_LIGHTWEIGHT_RENDERING, DEFAULT_ACCELERATED_LIGHTWEIGHT_RENDERING)) {
			/*
			 * accelerated lightweight rendering
			 * https://jxbrowser.support.teamdev.com/support/solutions/articles/9000104965-accelerated-lightweight-rendering
			 */
			switches.add("--disable-gpu");
			switches.add("--disable-gpu-compositing");
			switches.add("--enable-begin-frame-scheduling");
			switches.add("--software-rendering-fps=60");
		}
		if(PrefHelper.getBoolean("phon.debug", false)) {
			// setup remote debugging port
			switches.add(String.format("--remote-debugging-port=%d",REMOTE_DEBUGGING_PORT));
		}
		if(switches.size() > 0) {
			LogUtil.info(String.format("Chromium switches: %s", switches.stream().collect(Collectors.joining(" "))));
			BrowserPreferences.setChromiumSwitches(switches.toArray(new String[0]));
		}
	}

}
