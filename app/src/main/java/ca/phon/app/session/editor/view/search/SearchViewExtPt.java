package ca.phon.app.session.editor.view.search;

import ca.phon.app.session.ViewPosition;
import ca.phon.app.session.editor.EditorView;
import ca.phon.app.session.editor.EditorViewCategory;
import ca.phon.app.session.editor.EditorViewInfo;
import ca.phon.app.session.editor.SessionEditor;
import ca.phon.plugin.IPluginExtensionFactory;
import ca.phon.plugin.IPluginExtensionPoint;
import ca.phon.plugin.PhonPlugin;

@PhonPlugin(name=SearchView.VIEW_NAME)
@EditorViewInfo(name=SearchView.VIEW_NAME, category= EditorViewCategory.UTILITIES, icon=SearchView.VIEW_ICON, dockPosition = ViewPosition.RIGHT_TOP)
public class SearchViewExtPt implements IPluginExtensionPoint<EditorView>, IPluginExtensionFactory<EditorView>  {

    @Override
    public EditorView createObject(Object... args) {
        return new SearchView((SessionEditor) args[0]);
    }

    @Override
    public Class<?> getExtensionType() {
        return EditorView.class;
    }

    @Override
    public IPluginExtensionFactory<EditorView> getFactory() {
        return this;
    }

}
