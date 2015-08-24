package ca.phon.opgraph.editor;

import ca.gedge.opgraph.OpGraph;
import ca.phon.plugin.IPluginExtensionFactory;
import ca.phon.plugin.IPluginExtensionPoint;
import ca.phon.plugin.PhonPlugin;

@PhonPlugin(author="Greg J. Hedlund <ghedlund@mun.ca>", minPhonVersion="2.1.0", name="Default Opgraph Editor Model")
public class DefaultOpgraphEditorModelExtPt implements IPluginExtensionPoint<OpgraphEditorModel> {

	@Override
	public Class<?> getExtensionType() {
		return OpgraphEditorModel.class;
	}

	@Override
	public IPluginExtensionFactory<OpgraphEditorModel> getFactory() {
		final IPluginExtensionFactory<OpgraphEditorModel> factory = (Object ... args) -> {
			if(args.length > 0 && args[0] instanceof OpGraph) {
				return new DefaultOpgraphEditorModel((OpGraph)args[0]);
			} else {
				return new DefaultOpgraphEditorModel();
			}
		};
		return factory;	
	}

}
