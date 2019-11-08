package ca.phon.app.session.editor.view.timeline;

import ca.phon.app.session.editor.EditorView;
import ca.phon.app.session.editor.EditorViewCategory;
import ca.phon.app.session.editor.EditorViewInfo;
import ca.phon.app.session.editor.SessionEditor;
import ca.phon.plugin.IPluginExtensionFactory;
import ca.phon.plugin.IPluginExtensionPoint;
import ca.phon.plugin.PhonPlugin;

@PhonPlugin(name=TimelineView.VIEW_TITLE)
@EditorViewInfo(name=TimelineView.VIEW_TITLE, category=EditorViewCategory.SESSION, icon=TimelineView.VIEW_ICON)
public class TimelineViewExtension implements IPluginExtensionPoint<EditorView> {

	@Override
	public Class<?> getExtensionType() {
		return EditorView.class;
	}

	@Override
	public IPluginExtensionFactory<EditorView> getFactory() {
		return (args) -> new TimelineView((SessionEditor)args[0]);
	}

}
