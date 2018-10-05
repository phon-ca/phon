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
package ca.phon.app.session.editor.view.common;

import ca.phon.plugin.IPluginExtensionFactory;
import ca.phon.plugin.IPluginExtensionPoint;
import ca.phon.session.Tier;

/**
 * String editors
 */
@TierEditorInfo(type=String.class)
public class DefaultTierEditorExtension implements IPluginExtensionPoint<TierEditor> {

	@Override
	public Class<?> getExtensionType() {
		return TierEditor.class;
	}

	@Override
	public IPluginExtensionFactory<TierEditor> getFactory() {
		return factory;
	}

	private final IPluginExtensionFactory<TierEditor> factory = new IPluginExtensionFactory<TierEditor>() {
		
		@Override
		public TierEditor createObject(Object... args) {
			final Tier<?> tier = Tier.class.cast(args[1]);
			final Integer group = Integer.class.cast(args[2]);
			
			if(tier.getDeclaredType() != String.class) {
				throw new IllegalArgumentException("Tier type must be " + String.class.getName());
			}
			
			@SuppressWarnings("unchecked")
			final Tier<String> stringTier = (Tier<String>)tier;
			return new GroupField<String>(stringTier, group, !tier.isGrouped());
		}
		
	};
	
}
