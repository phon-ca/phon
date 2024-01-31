package ca.phon.app.session.editor.view.search;

import ca.phon.app.session.editor.EditorView;
import ca.phon.app.session.editor.EditorViewCategory;
import ca.phon.app.session.editor.EditorViewInfo;
import ca.phon.app.session.editor.SessionEditor;
import ca.phon.app.session.editor.search.FindExpr;
import ca.phon.app.session.editor.search.FindManager;
import ca.phon.app.session.editor.search.SessionEditorQuickSearch;
import ca.phon.app.session.editor.search.SessionEditorQuickSearchField;
import ca.phon.session.position.TranscriptElementLocation;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.ui.text.SearchField;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;
import org.jdesktop.swingx.JXTable;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Stack;

@EditorViewInfo(name=SearchView.VIEW_NAME, category= EditorViewCategory.UTILITIES, icon=SearchView.VIEW_ICON)
public class SearchView extends EditorView {

    public static final String VIEW_NAME = "Search";

    public static final String VIEW_ICON = IconManager.GoogleMaterialDesignIconsFontName + ":SEARCH";

    private final static String SEARCH_HISTORY_PROP_PREFIX = "SessionEditor.searchHistory";

    private final static int MAX_SEARCH_HISTORY = 100;

    private Stack<String> searchHistory;

    private SearchField searchField;

    private SearchViewTable table;

    public SearchView(SessionEditor editor) {
        super(editor);

        init();
    }

    private void init() {
        setLayout(new BorderLayout());
        this.searchField = new SearchField("Search tiers...");
        final PhonUIAction<Void> searchAct = PhonUIAction.runnable(this::onQuery);
        this.searchField.setAction(searchAct);
        add(searchField, BorderLayout.NORTH);

        this.table = new SearchViewTable(getEditor().getSession(), new ArrayList<>());
        add(new JScrollPane(table), BorderLayout.CENTER);
    }

    public void onQuery() {
        final String queryText = searchField.getText();
        if(queryText.trim().length() == 0) return;

        final FindManager findManager = new FindManager(getEditor().getSession());
        findManager.setCurrentLocation(new TranscriptElementLocation(0, findManager.getSearchTiers()[0], 0));
        final FindExpr findExpr = new FindExpr(searchField.getText());
        findManager.setAnyExpr(findExpr);
        this.table.search(findManager);
    }

    @Override
    public String getName() {
        return VIEW_NAME;
    }

    @Override
    public ImageIcon getIcon() {
        final String[] iconData = VIEW_ICON.split(":");
        return IconManager.getInstance().getFontIcon(iconData[0], iconData[1], IconSize.MEDIUM, Color.darkGray);
    }

    @Override
    public JMenu getMenu() {
        return null;
    }

}
