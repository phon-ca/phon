/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2015, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
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
import java.util.logging.Logger;

import ca.phon.app.hooks.PhonStartupHook;
import ca.phon.ipadictionary.impl.IPADatabaseManager;
import ca.phon.plugin.IPluginExtensionFactory;
import ca.phon.plugin.IPluginExtensionPoint;
import ca.phon.plugin.PluginException;
import ca.phon.util.PrefHelper;

public class IPADictionaryInitHook implements PhonStartupHook, IPluginExtensionPoint<PhonStartupHook> {

	private static final Logger LOGGER = Logger
			.getLogger(IPADictionaryInitHook.class.getName());
	
	private static final String DERBY_LOG_PROP = "derby.stream.error.file";
	
	private static final String DERBY_LOG_LOCATION = 
			PrefHelper.getUserDataFolder() + File.separator + "ipadb.log";
	
	@Override
	public Class<?> getExtensionType() {
		return PhonStartupHook.class;
	}

	@Override
	public IPluginExtensionFactory<PhonStartupHook> getFactory() {
		return factory;
	}

	@Override
	public void startup() throws PluginException {
		LOGGER.info("Initializing IPA Dictionaries");
		System.setProperty(DERBY_LOG_PROP,
				PrefHelper.get(DERBY_LOG_PROP, DERBY_LOG_LOCATION));
		IPADatabaseManager.getInstance();
	}
	
	private final IPluginExtensionFactory<PhonStartupHook> factory = new IPluginExtensionFactory<PhonStartupHook>() {
		
		@Override
		public PhonStartupHook createObject(Object... args) {
			return IPADictionaryInitHook.this;
		}
		
	};

}
