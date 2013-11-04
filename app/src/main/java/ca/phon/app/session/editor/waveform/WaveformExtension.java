package ca.phon.app.session.editor.waveform;

import ca.phon.app.session.editor.EditorView;
import ca.phon.app.session.editor.SessionEditor;
import ca.phon.plugin.IPluginExtensionFactory;
import ca.phon.plugin.IPluginExtensionPoint;

/**
 * Waveform view extension point for the {@link SessionEditor}
 *
 */
public class WaveformExtension implements IPluginExtensionPoint<EditorView> {

	@Override
	public Class<?> getExtensionType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IPluginExtensionFactory<EditorView> getFactory() {
		// TODO Auto-generated method stub
		return null;
	}

}
