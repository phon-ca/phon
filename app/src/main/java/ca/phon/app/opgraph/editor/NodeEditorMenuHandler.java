package ca.phon.app.opgraph.editor;

import java.awt.Window;

import javax.swing.JMenuBar;

import ca.phon.app.opgraph.editor.actions.OpenComposerAction;
import ca.phon.plugin.IPluginExtensionFactory;
import ca.phon.plugin.IPluginExtensionPoint;
import ca.phon.plugin.IPluginMenuFilter;
import ca.phon.ui.menu.MenuBuilder;

public class NodeEditorMenuHandler implements IPluginMenuFilter, IPluginExtensionPoint<IPluginMenuFilter> {

	public NodeEditorMenuHandler() {
	}

	@Override
	public Class<?> getExtensionType() {
		return IPluginMenuFilter.class;
	}

	@Override
	public IPluginExtensionFactory<IPluginMenuFilter> getFactory() {
		return (args) -> this;
	}

	@Override
	public void filterWindowMenu(Window owner, JMenuBar menuBar) {
		final MenuBuilder builder = new MenuBuilder(menuBar);
		
		builder.addSeparator("./Tools",	"composer");
		builder.addItem("./Tools@composer", new OpenComposerAction());
	}

}
