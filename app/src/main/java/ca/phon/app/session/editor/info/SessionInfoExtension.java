package ca.phon.app.session.editor.info;

import ca.phon.app.session.editor.EditorView;
import ca.phon.app.session.editor.EditorViewCategory;
import ca.phon.app.session.editor.EditorViewInfo;
import ca.phon.app.session.editor.SessionEditor;
import ca.phon.plugin.IPluginExtensionFactory;
import ca.phon.plugin.IPluginExtensionPoint;
import ca.phon.plugin.PhonPlugin;

@PhonPlugin(name="Session Information")
@EditorViewInfo(name="Session Information", category=EditorViewCategory.SESSION)
public class SessionInfoExtension implements IPluginExtensionPoint<EditorView> {

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
			if(args.length != 1 || !(args[0] instanceof SessionEditor)) {
				throw new IllegalArgumentException("Arguments must include SessionEditor reference.");
			}
			final SessionEditor editor = (SessionEditor)args[0];
			return new SessionInfoPanel(editor);
		}
		
	};
	
}
