package ca.phon.app.opgraph.analysis;

import ca.gedge.opgraph.OpGraph;
import ca.phon.app.opgraph.editor.OpgraphEditorModel;
import ca.phon.plugin.IPluginExtensionFactory;
import ca.phon.plugin.IPluginExtensionPoint;
import ca.phon.plugin.PhonPlugin;

@PhonPlugin(author="Greg J. Hedlund <ghedlund@mun.ca>", minPhonVersion="2.1.0", name="Analysis Opgraph Editor Model")
public class AnalysisOpGraphEditorModelExtPt implements IPluginExtensionPoint<OpgraphEditorModel> {

	@Override
	public Class<?> getExtensionType() {
		return OpgraphEditorModel.class;
	}

	@Override
	public IPluginExtensionFactory<OpgraphEditorModel> getFactory() {
		final IPluginExtensionFactory<OpgraphEditorModel> factory = (Object ... args) -> {
			if(args.length == 1 && args[0] instanceof OpGraph) {
				final OpGraph graph = (OpGraph)args[0];
				return new AnalysisOpGraphEditorModel(graph);
			} else {
				return new AnalysisOpGraphEditorModel();
			}
		};
		return factory;
	}

}
