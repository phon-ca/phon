package ca.phon.app.opgraph.macro;

import ca.gedge.opgraph.OpGraph;
import ca.phon.app.opgraph.editor.EditorModelInstantiator;
import ca.phon.app.opgraph.editor.EditorModelInstantiator.EditorModelInstantiatorMenuInfo;
import ca.phon.plugin.IPluginExtensionFactory;
import ca.phon.plugin.IPluginExtensionPoint;

@EditorModelInstantiatorMenuInfo(
		name="Macro",
		tooltip="Create new macro...",
		modelType=MacroOpgraphEditorModel.class
)
public class MacroEditorModelInstantiator implements EditorModelInstantiator, IPluginExtensionPoint<EditorModelInstantiator> {

	@Override
	public MacroOpgraphEditorModel createModel(OpGraph graph) {
		return new MacroOpgraphEditorModel(graph);
	}

	@Override
	public Class<?> getExtensionType() {
		return EditorModelInstantiator.class;
	}

	@Override
	public IPluginExtensionFactory<EditorModelInstantiator> getFactory() {
		return (Object... args) -> this;
	}

}
