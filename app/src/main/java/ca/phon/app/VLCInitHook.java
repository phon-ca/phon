/*
 * Copyright (C) 2012-2018 Gregory Hedlund & Yvan Rose
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

import org.apache.logging.log4j.LogManager;

import ca.phon.app.hooks.PhonStartupHook;
import ca.phon.media.VLCHelper;
import ca.phon.plugin.IPluginExtensionFactory;
import ca.phon.plugin.IPluginExtensionPoint;
import ca.phon.plugin.PhonPlugin;
import ca.phon.plugin.PluginException;

@PhonPlugin
public class VLCInitHook implements PhonStartupHook, IPluginExtensionPoint<PhonStartupHook> {
	
	private static final org.apache.logging.log4j.Logger LOGGER = LogManager.getLogger(VLCInitHook.class.getName());

	@Override
	public void startup() throws PluginException {
		LOGGER.info("Initializing VLC library");
		VLCHelper.checkNativeLibrary(false);
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
			return VLCInitHook.this;
		}
		
	};

}
