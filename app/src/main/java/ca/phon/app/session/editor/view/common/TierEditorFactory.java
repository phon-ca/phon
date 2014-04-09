package ca.phon.app.session.editor.view.common;

import java.awt.event.KeyEvent;
import java.util.List;

import javax.swing.ActionMap;
import javax.swing.FocusManager;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.KeyStroke;

import org.jdesktop.swingx.action.ActionManager;

import ca.phon.app.session.editor.SessionEditor;
import ca.phon.plugin.IPluginExtensionPoint;
import ca.phon.plugin.PluginManager;
import ca.phon.session.Tier;
import ca.phon.session.TierDescription;
import ca.phon.ui.action.PhonUIAction;

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
	public TierEditor createTierEditor(SessionEditor editor, TierDescription tierDescription,
			Tier<?> tier, int group) {
		TierEditor retVal = null;
		
		final Class<?> tierType = tier.getDeclaredType();
		
		final List<IPluginExtensionPoint<TierEditor>> extPts = 
				PluginManager.getInstance().getExtensionPoints(TierEditor.class);
		for(IPluginExtensionPoint<TierEditor> extPt:extPts) {
			final TierEditorInfo info = extPt.getClass().getAnnotation(TierEditorInfo.class);
			if(info != null) {
				if(info.type() == tierType) {
					if(info.tierName().length() > 0) {
						if(!info.tierName().equals(tier.getName())) continue;
					}
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
		
		installTierEditorActions(retVal.getEditorComponent());
		
		return retVal;
	}
	
	/**
	 * Install tier editor action map onto given component.
	 * 
	 * @param comp
	 */
	private void installTierEditorActions(final JComponent comp) {
		final InputMap inputMap = comp.getInputMap(JComponent.WHEN_FOCUSED);
		final ActionMap actionMap = comp.getActionMap();
		
		// navigation
		final KeyStroke nextEditorKS = KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0);
		final String nextEditorActId = "next-editor";
		final PhonUIAction nextEditorAct = new PhonUIAction(FocusManager.getCurrentManager(), "focusNextComponent");
		actionMap.put(nextEditorActId, nextEditorAct);
		inputMap.put(nextEditorKS, nextEditorActId);
		
		final KeyStroke prevEditorKS = KeyStroke.getKeyStroke(KeyEvent.VK_TAB, KeyEvent.SHIFT_MASK);
		final String prevEditorActId = "prev-editor";
		final PhonUIAction prevEditorAct = new PhonUIAction(FocusManager.getCurrentManager(), "focusPreviousComponent");
		actionMap.put(prevEditorActId, prevEditorAct);
		inputMap.put(prevEditorKS, prevEditorActId);
		
		comp.setActionMap(actionMap);
		comp.setInputMap(JComponent.WHEN_FOCUSED, inputMap);
	}
}
