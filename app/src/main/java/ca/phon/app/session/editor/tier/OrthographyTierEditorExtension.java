package ca.phon.app.session.editor.tier;

import ca.phon.app.session.editor.SessionEditor;
import ca.phon.orthography.Orthography;
import ca.phon.plugin.IPluginExtensionFactory;
import ca.phon.plugin.IPluginExtensionPoint;
import ca.phon.session.Tier;

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
			final SessionEditor editor = SessionEditor.class.cast(args[0]);
			final Tier<?> tier = Tier.class.cast(args[1]);
			final Integer group = Integer.class.cast(args[2]);
			
			if(tier.getDeclaredType() != Orthography.class) {
				throw new IllegalArgumentException("Tier type must be " + Orthography.class.getName());
			}
			
			@SuppressWarnings("unchecked")
			final Tier<Orthography> orthoTier = (Tier<Orthography>)tier;
			return new OrthoGroupField(editor, orthoTier, group);
		}
		
	};

}
