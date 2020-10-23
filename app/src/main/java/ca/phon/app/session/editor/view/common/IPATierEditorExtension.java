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

import ca.phon.app.session.editor.*;
import ca.phon.ipa.*;
import ca.phon.plugin.*;
import ca.phon.session.*;
import ca.phon.syllabifier.*;

/**
 * Editor for IPATranscript tiers 
 */
@TierEditorInfo(type=IPATranscript.class)
public class IPATierEditorExtension implements IPluginExtensionPoint<TierEditor> {

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
			final SessionEditor editor = SessionEditor.class.cast(args[0]);
			final Tier<?> tier = Tier.class.cast(args[1]);
			final Integer group = Integer.class.cast(args[2]);
			
			if(tier.getDeclaredType() != IPATranscript.class) {
				throw new IllegalArgumentException("Tier type must be " + IPATranscript.class.getName());
			}
			
			@SuppressWarnings("unchecked")
			final Tier<IPATranscript> ipaTier = (Tier<IPATranscript>)tier;
			
			Syllabifier syllabifier = null;
			final SyllabifierInfo info = editor.getSession().getExtension(SyllabifierInfo.class);
			if(info != null && info.getSyllabifierLanguageForTier(tier.getName()) != null) {
				syllabifier = SyllabifierLibrary.getInstance().getSyllabifierForLanguage(
						info.getSyllabifierLanguageForTier(tier.getName()));
			}
			
			return new IPAGroupField(ipaTier, group, editor.getDataModel().getTranscriber(), syllabifier);
		}
		
	};
	
}
