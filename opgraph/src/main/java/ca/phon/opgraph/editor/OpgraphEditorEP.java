package ca.phon.opgraph.editor;

import java.util.Map;

import javax.swing.SwingUtilities;

import ca.phon.plugin.IPluginEntryPoint;

public class OpgraphEditorEP implements IPluginEntryPoint {

	public final static String OPGRAPH_MODEL_KEY = "model";
	
	public final static String EP_NAME = "OpgraphEditor";
	
	@Override
	public String getName() {
		return EP_NAME;
	}

	@Override
	public void pluginStart(Map<String, Object> args) {
		final OpgraphEditorModel model =
				(args.containsKey(OPGRAPH_MODEL_KEY) ? (OpgraphEditorModel)args.get(OPGRAPH_MODEL_KEY) : 
					new DefaultOpgraphEditorModel());
		final Runnable onEDT = () -> {
			final OpgraphEditor editor = new OpgraphEditor(model);
			editor.pack();
			editor.setSize(1064, 768);
			editor.setLocationByPlatform(true);
			editor.setVisible(true);
		};
		if(SwingUtilities.isEventDispatchThread())
			onEDT.run();
		else
			SwingUtilities.invokeLater(onEDT);
	}

}
