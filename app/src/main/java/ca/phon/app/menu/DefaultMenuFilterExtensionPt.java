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
package ca.phon.app.menu;

import ca.phon.plugin.*;


/**
 * Extension point implementation for application menubar filter.
 * 
 */
@PhonPlugin(
		author="Greg J. Hedlund",
		name="Application Menu",
		version="1.0",
		comments="Default menu filter for application windows."
)
@Rank(0)
public class DefaultMenuFilterExtensionPt implements IPluginExtensionPoint<IPluginMenuFilter> {

	@Override
	public Class<?> getExtensionType() {
		return IPluginMenuFilter.class;
	}

	@Override
	public IPluginExtensionFactory<IPluginMenuFilter> getFactory() {
		return new DefaultMenuFilterExtensionFactory();
	}

	/**
	 * Factory
	 */
	private class DefaultMenuFilterExtensionFactory implements IPluginExtensionFactory<IPluginMenuFilter> {

		@Override
		public IPluginMenuFilter createObject(Object ... args) {
			return new DefaultMenuFilter();
		}
		
	}
	
}
