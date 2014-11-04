package ca.phon.app.query.analysis;

import java.awt.Window;

import javax.swing.JMenu;
import javax.swing.JMenuBar;

import ca.phon.app.query.analysis.actions.PCCAction;
import ca.phon.app.query.analysis.actions.WordMatchAction;
import ca.phon.plugin.IPluginExtensionFactory;
import ca.phon.plugin.IPluginExtensionPoint;
import ca.phon.plugin.IPluginMenuFilter;
import ca.phon.ui.CommonModuleFrame;

/**
 * Add canned analysis menu items to windows.
 */
public class AnalysisMenuFilter implements IPluginMenuFilter, IPluginExtensionPoint<IPluginMenuFilter> {

	@Override
	public Class<?> getExtensionType() {
		return IPluginMenuFilter.class;
	}

	@Override
	public IPluginExtensionFactory<IPluginMenuFilter> getFactory() {
		return factory;
	}

	@Override
	public void filterWindowMenu(Window owner, JMenuBar menu) {
		JMenu toolsMenu = null;
		for(int i = 0; i < menu.getMenuCount(); i++) {
			final JMenu m = menu.getMenu(i);
			if(m.getText().equals("Tools")) {
				toolsMenu = m;
				break;
			}
		}
		if(toolsMenu == null) return;
		
		if(!(owner instanceof CommonModuleFrame)) return;
		
		toolsMenu.addSeparator();
		toolsMenu.add(new WordMatchAction((CommonModuleFrame)owner));
		
		// TODO This script needs modifications so that it's performed at a word-level
		toolsMenu.add(new PCCAction((CommonModuleFrame)owner));
	}
	
	private final IPluginExtensionFactory<IPluginMenuFilter> factory =  new IPluginExtensionFactory<IPluginMenuFilter>() {
		
		@Override
		public IPluginMenuFilter createObject(Object... args) {
			return AnalysisMenuFilter.this;
		}
		
	};


}
