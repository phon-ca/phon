/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2017, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
 * Dept of Linguistics, Memorial University <https://phon.ca>
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ca.phon.app;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;

import ca.phon.app.hooks.PhonStartupHook;
import ca.phon.ipadictionary.impl.IPADatabaseManager;
import ca.phon.logging.LogUtil;
import ca.phon.plugin.IPluginExtensionFactory;
import ca.phon.plugin.IPluginExtensionPoint;
import ca.phon.plugin.PluginException;
import ca.phon.util.PrefHelper;

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
