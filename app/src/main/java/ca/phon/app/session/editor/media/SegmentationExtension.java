package ca.phon.app.session.editor.media;

import ca.phon.app.session.editor.EditorView;
import ca.phon.app.session.editor.EditorViewCategory;
import ca.phon.app.session.editor.EditorViewInfo;
import ca.phon.app.session.editor.EditorViewModel;
import ca.phon.app.session.editor.SessionEditor;
import ca.phon.plugin.IPluginExtensionFactory;
import ca.phon.plugin.IPluginExtensionPoint;
import ca.phon.plugin.PhonPlugin;

@PhonPlugin(name="Segmentation")
@EditorViewInfo(name="Segmentation", category=EditorViewCategory.MEDIA, icon="actions/film-link")
public class SegmentationExtension implements IPluginExtensionPoint<EditorView> {

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
			final SessionEditor editor = SessionEditor.class.cast(args[0]);
			final EditorViewModel viewModel = editor.getViewModel();
			final EditorView editorView = viewModel.getView(MediaPlayerEditorView.VIEW_TITLE);
			if(editorView != null && editorView instanceof MediaPlayerEditorView) 
				return new SegmentationEditorView(editor, MediaPlayerEditorView.class.cast(editorView));
			else
				return null;
		}
	};
	
}
