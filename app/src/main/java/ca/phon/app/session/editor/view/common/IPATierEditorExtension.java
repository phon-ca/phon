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
