package ca.phon.app.session.editor.view.record_data;

import ca.phon.app.session.editor.EditorView;
import ca.phon.app.session.editor.EditorViewCategory;
import ca.phon.app.session.editor.EditorViewInfo;
import ca.phon.app.session.editor.SessionEditor;
import ca.phon.plugin.IPluginExtensionFactory;
import ca.phon.plugin.IPluginExtensionPoint;
import ca.phon.plugin.PhonPlugin;

/**
 * Provides plug-in extension point for {@link RecordDataEditorView}
 *
 */
@PhonPlugin(name=RecordDataEditorView.VIEW_NAME)
@EditorViewInfo(name=RecordDataEditorView.VIEW_NAME, category=EditorViewCategory.RECORD, icon=RecordDataEditorView.VIEW_ICON)
public class RecordDataViewExtension implements IPluginExtensionPoint<EditorView> {

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
			return new RecordDataEditorView(editor);
		}
		
	};
	
}
