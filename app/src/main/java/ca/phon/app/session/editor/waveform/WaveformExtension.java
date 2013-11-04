package ca.phon.app.session.editor.waveform;

import ca.phon.app.session.editor.EditorView;
import ca.phon.app.session.editor.SessionEditor;
import ca.phon.plugin.IPluginExtensionFactory;
import ca.phon.plugin.IPluginExtensionPoint;
import ca.phon.plugin.PhonPlugin;

/**
 * Waveform view extension point for the {@link SessionEditor}
 *
 */
@PhonPlugin(name="Waveform")
public class WaveformExtension implements IPluginExtensionPoint<EditorView> {

	@Override
	public Class<?> getExtensionType() {
		return EditorView.class;
	}

	@Override
	public IPluginExtensionFactory<EditorView> getFactory() {
		return factory;
	}

	private final IPluginExtensionFactory<EditorView> factory = new IPluginExtensionFactory<EditorView>() {
		
		@Override
		public EditorView createObject(Object... args) {
			final SessionEditor editor = (SessionEditor)args[0];
			return new WaveformEditorView(editor);
		}
		
	};
}
