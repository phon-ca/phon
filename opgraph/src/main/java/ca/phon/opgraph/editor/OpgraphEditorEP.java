package ca.phon.opgraph.editor;

import java.util.Map;

import javax.swing.SwingUtilities;

import ca.phon.plugin.IPluginEntryPoint;

public class OpgraphEditorEP implements IPluginEntryPoint {

	public final static String EP_NAME = "OpgraphEditor";
	
	@Override
	public String getName() {
		return EP_NAME;
	}

	@Override
	public void pluginStart(Map<String, Object> args) {
		final Runnable onEDT = () -> {
			final OpgraphEditor editor = new OpgraphEditor();
			editor.pack();
			editor.setVisible(true);
		};
		if(SwingUtilities.isEventDispatchThread())
			onEDT.run();
		else
			SwingUtilities.invokeLater(onEDT);
	}

}
