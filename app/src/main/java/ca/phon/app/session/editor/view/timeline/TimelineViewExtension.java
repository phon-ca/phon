package ca.phon.app.session.editor.view.timeline;

import ca.phon.app.session.editor.*;
import ca.phon.plugin.*;

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
