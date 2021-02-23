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
package ca.phon.app.session.editor.view.common;

import ca.phon.app.session.editor.*;
import ca.phon.orthography.*;
import ca.phon.plugin.*;
import ca.phon.session.*;

@TierEditorInfo(type=Orthography.class)
public class OrthographyTierEditorExtension implements IPluginExtensionPoint<TierEditor> {

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
			final SessionEditor editor = SessionEditor.class.cast(args[TierEditorFactory.EDITOR]);
			final Tier<?> tier = Tier.class.cast(args[TierEditorFactory.TIER]);
			final Integer group = Integer.class.cast(args[TierEditorFactory.GROUP]);
			
			if(tier.getDeclaredType() != Orthography.class) {
				throw new IllegalArgumentException("Tier type must be " + Orthography.class.getName());
			}
			
			@SuppressWarnings("unchecked")
			final Tier<Orthography> orthoTier = (Tier<Orthography>)tier;
			return new OrthoGroupField(orthoTier, group);
		}
		
	};

}
