package ca.phon.app.session.editor.tier;

import ca.phon.app.session.editor.SessionEditor;
import ca.phon.ipa.IPATranscript;
import ca.phon.plugin.IPluginExtensionFactory;
import ca.phon.plugin.IPluginExtensionPoint;
import ca.phon.session.Tier;

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
			final Tier<IPATranscript> orthoTier = (Tier<IPATranscript>)tier;
			return new IPAGroupField(orthoTier, group, editor.getDataModel().getTranscriber());
		}
		
	};
	
}
