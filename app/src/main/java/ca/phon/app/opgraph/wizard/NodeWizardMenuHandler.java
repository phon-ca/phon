package ca.phon.app.opgraph.wizard;

import java.awt.Window;

import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import ca.gedge.opgraph.OpGraph;
import ca.phon.app.opgraph.editor.OpgraphEditor;
import ca.phon.app.opgraph.wizard.actions.WizardSettingsAction;
import ca.phon.plugin.IPluginExtensionFactory;
import ca.phon.plugin.IPluginExtensionPoint;
import ca.phon.plugin.IPluginMenuFilter;
import ca.phon.ui.menu.MenuBuilder;

public class NodeWizardMenuHandler implements IPluginMenuFilter, IPluginExtensionPoint<IPluginMenuFilter> {

	@Override
	public Class<?> getExtensionType() {
		return IPluginMenuFilter.class;
	}

	@Override
	public IPluginExtensionFactory<IPluginMenuFilter> getFactory() {
		final IPluginExtensionFactory<IPluginMenuFilter> retVal = (args) -> {
			return NodeWizardMenuHandler.this;
		};
		return retVal;
	}

	@Override
	public void filterWindowMenu(Window owner, JMenuBar menu) {
		if(!(owner instanceof OpgraphEditor)) return;
		final OpgraphEditor editor = (OpgraphEditor)owner;
		if(editor.getModel() == null || editor.getModel().getDocument() == null) return;
		final OpGraph graph = editor.getModel().getDocument().getGraph();
		final WizardExtension ext = graph.getExtension(WizardExtension.class);
		if(ext != null) {
			final MenuBuilder builder = new MenuBuilder(menu);
			final WizardSettingsAction act = new WizardSettingsAction(editor);
			builder.addMenu(".@Edit", "Wizard");
			builder.addItem("Wizard", new JMenuItem(act));
		}
	}

	
	
}
