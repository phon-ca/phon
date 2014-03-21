package ca.phon.app.session.editor.view.common;

import ca.phon.plugin.IPluginExtensionFactory;
import ca.phon.plugin.IPluginExtensionPoint;
import ca.phon.session.MediaSegment;
import ca.phon.session.Tier;

@TierEditorInfo(type=MediaSegment.class)
public class SegmentTierEditorExtension implements IPluginExtensionPoint<TierEditor> {

	@Override
	public Class<?> getExtensionType() {
		return TierEditor.class;
	}

	@Override
	public IPluginExtensionFactory<TierEditor> getFactory() {
		return factory;
	}

	private final IPluginExtensionFactory<TierEditor> factory = new IPluginExtensionFactory<TierEditor>() {
		
		@SuppressWarnings("unchecked")
		@Override
		public TierEditor createObject(Object... args) {
			final Tier<?> tier = Tier.class.cast(args[1]);
			final Integer group = Integer.class.cast(args[2]);
			
			if(tier.getDeclaredType() != MediaSegment.class) {
				throw new IllegalArgumentException("Tier type must be " + MediaSegment.class.getName());
			}
			
			return new SegmentTierComponent((Tier<MediaSegment>)tier, group);
		}
		
	};
}
