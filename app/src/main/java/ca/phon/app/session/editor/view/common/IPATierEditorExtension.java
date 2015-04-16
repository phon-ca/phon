/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2015 The Phon Project, Memorial University <https://phon.ca>
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
package ca.phon.app.session.editor.view.common;

import ca.phon.app.session.editor.SessionEditor;
import ca.phon.ipa.IPATranscript;
import ca.phon.plugin.IPluginExtensionFactory;
import ca.phon.plugin.IPluginExtensionPoint;
import ca.phon.session.SyllabifierInfo;
import ca.phon.session.Tier;
import ca.phon.syllabifier.Syllabifier;
import ca.phon.syllabifier.SyllabifierLibrary;

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
