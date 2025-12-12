/*
 * Copyright (C) 2012-2018 Gregory Hedlund
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

import ca.hedlund.jpraat.binding.Praat;
import ca.hedlund.jpraat.binding.sys.PraatVersion;
import ca.phon.app.hooks.PhonStartupHook;
import ca.phon.plugin.IPluginExtensionFactory;
import ca.phon.plugin.IPluginExtensionPoint;
import ca.phon.plugin.PluginException;
import ca.phon.util.PrefHelper;
import com.sun.jna.NativeLibrary;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Init Praat library on startup.
 *
 */
public class PraatStartupHook implements PhonStartupHook, IPluginExtensionPoint<PhonStartupHook> {

	private static final Logger LOGGER = Logger
			.getLogger(PraatStartupHook.class.getName());
	
	public static final String PRAAT_SEARCH_FOLDER = PraatStartupHook.class.getName() + 
			".praatSearchFolder";
	
	@Override
	public void startup() throws PluginException {
		
		try {
			LOGGER.info("Initializing Praat library");
			
			final String praatSearchFolder = 
					PrefHelper.get(PRAAT_SEARCH_FOLDER, null);
			if(praatSearchFolder != null) {
				NativeLibrary.addSearchPath("praat", praatSearchFolder);
			}
			
			Praat.initLibrary();
			
			final PraatVersion praatVersion = PraatVersion.getVersion();
			// print version information to log
			final StringBuilder sb = new StringBuilder();
			sb.append("Praat version: ").append(praatVersion.versionStr);
			sb.append(" ").append(praatVersion.day).append('-').append(praatVersion.month).append('-').append(praatVersion.year);
			LOGGER.info(sb.toString());
		} catch (UnsatisfiedLinkError e) {
			LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
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
