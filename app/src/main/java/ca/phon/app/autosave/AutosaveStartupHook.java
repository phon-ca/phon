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
package ca.phon.app.autosave;

import ca.phon.app.hooks.PhonStartupHook;
import ca.phon.plugin.*;
import ca.phon.util.PrefHelper;

public class AutosaveStartupHook implements PhonStartupHook, IPluginExtensionPoint<PhonStartupHook> {

	@Override
	public void startup() throws PluginException {
		final AutosaveManager manager = AutosaveManager.getInstance();
		final int autosaveInterval = 
				PrefHelper.getInt(AutosaveManager.AUTOSAVE_INTERVAL_PROP, 0);
		manager.setInterval(autosaveInterval * 60);
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
			return AutosaveStartupHook.this;
		}
		
	};

}
