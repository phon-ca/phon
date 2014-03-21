package ca.phon.app.session.editor.view.common;

import java.util.List;

import ca.phon.app.session.editor.SessionEditor;
import ca.phon.plugin.IPluginExtensionPoint;
import ca.phon.plugin.PluginManager;
import ca.phon.session.Tier;

/**
 * Create tier editors
 */
public class TierEditorFactory {

	public TierEditorFactory() {
		
	}
	
	/**
	 * Create a new tier editor for the given tier.
	 * 
	 * @param tier
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public TierEditor createTierEditor(SessionEditor editor, Tier<?> tier, int group) {
		TierEditor retVal = null;
		
		final Class<?> tierType = tier.getDeclaredType();
		
		final List<IPluginExtensionPoint<TierEditor>> extPts = 
				PluginManager.getInstance().getExtensionPoints(TierEditor.class);
		for(IPluginExtensionPoint<TierEditor> extPt:extPts) {
			final TierEditorInfo info = extPt.getClass().getAnnotation(TierEditorInfo.class);
			if(info != null) {
				if(info.type() == tierType) {
					retVal = extPt.getFactory().createObject(editor, tier, group);
					// don't continue to look use this editor
					if(info.tierName().equalsIgnoreCase(tier.getName())) {
						break;
					}
				}
			}
		}
		
		// create a generic tier editor
		if(retVal == null)
			retVal = new GroupField(tier, group);
		
		return retVal;
	}
	
}
