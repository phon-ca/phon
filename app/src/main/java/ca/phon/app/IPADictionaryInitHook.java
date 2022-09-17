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
import ca.phon.ipadictionary.impl.IPADatabaseManager;
import ca.phon.plugin.*;
import ca.phon.util.PrefHelper;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;

import java.io.*;

public class IPADictionaryInitHook implements PhonStartupHook, IPluginExtensionPoint<PhonStartupHook> {

	private static final org.apache.logging.log4j.Logger LOGGER = LogManager.getLogger(IPADictionaryInitHook.class.getName());
	
	private static final String DERBY_LOG_PROP = "derby.stream.error.file";
	
	private static final String DERBY_LOG_LOCATION = 
			PrefHelper.getUserDataFolder() + File.separator + "ipadb.log";
	
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
		LOGGER.info("Initializing IPA Dictionaries");
		System.setProperty(DERBY_LOG_PROP,
				PrefHelper.get(DERBY_LOG_PROP, DERBY_LOG_LOCATION));
		
		// check for old version and copy it to the
		// new application data
		// folder
		final File oldDbFolder = new File(PrefHelper.getUserDocumentsPhonFolder(), "ipadb");
		final File dbFolder = new File(PrefHelper.getUserDataFolder(), "ipadb");
		
		if(!dbFolder.exists() && oldDbFolder.exists()) {
			// copy folder
			try {
				LogUtil.info("Copying ipa dictionary from:" + oldDbFolder.getAbsolutePath() + " to: " + dbFolder.getAbsolutePath());
				FileUtils.copyDirectory(oldDbFolder, dbFolder);
			} catch (IOException e) {
				LogUtil.severe(e);
			}
		}
		
		IPADatabaseManager.getInstance();
	}
	
}
