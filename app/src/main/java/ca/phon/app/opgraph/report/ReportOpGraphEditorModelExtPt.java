package ca.phon.app.opgraph.report;

import ca.gedge.opgraph.OpGraph;
import ca.phon.opgraph.editor.OpgraphEditorModel;
import ca.phon.plugin.IPluginExtensionFactory;
import ca.phon.plugin.IPluginExtensionPoint;
import ca.phon.plugin.PhonPlugin;

@PhonPlugin(author="Greg J. Hedlund <ghedlund@mun.ca>", minPhonVersion="2.1.0", name="Report Opgraph Editor Model")
public class ReportOpGraphEditorModelExtPt implements IPluginExtensionPoint<OpgraphEditorModel> {

	@Override
	public Class<?> getExtensionType() {
		return OpgraphEditorModel.class;
	}

	@Override
	public IPluginExtensionFactory<OpgraphEditorModel> getFactory() {
		final IPluginExtensionFactory<OpgraphEditorModel> factory = (Object ... args) -> {
			if(args.length == 1 && args[0] instanceof OpGraph) {
				return new ReportOpGraphEditorModel((OpGraph)args[0]);
			} else {
				return new ReportOpGraphEditorModel();
			}
		};
		return factory;
	}

}
