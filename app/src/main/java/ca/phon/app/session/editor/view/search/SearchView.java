package ca.phon.app.session.editor.view.search;

import ca.phon.app.session.editor.EditorView;
import ca.phon.app.session.editor.EditorViewCategory;
import ca.phon.app.session.editor.EditorViewInfo;
import ca.phon.app.session.editor.SessionEditor;
import ca.phon.app.session.editor.search.FindExpr;
import ca.phon.app.session.editor.search.FindManager;
import ca.phon.session.position.TranscriptElementLocation;
import ca.phon.ui.FlatButton;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.ui.text.SearchField;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Stack;

@EditorViewInfo(name=SearchView.VIEW_NAME, category= EditorViewCategory.UTILITIES, icon=SearchView.VIEW_ICON)
public class SearchView extends EditorView {

    public static final String VIEW_NAME = "Search";

    public static final String VIEW_ICON = IconManager.GoogleMaterialDesignIconsFontName + ":SEARCH";

    private final static String SEARCH_HISTORY_PROP_PREFIX = "SessionEditor.searchHistory";

    private final static int MAX_SEARCH_HISTORY = 10;

    private Stack<String> searchHistory;

    private SearchField searchField;

    private JLabel resultsLabel;

    private FlatButton filterButton;

    private FlatButton caseSensitiveButton;

    private FlatButton regexButton;

    private FlatButton phonexButton;

    private SearchViewTable table;

    public SearchView(SessionEditor editor) {
        super(editor);

        init();
    }

    private void init() {
        setLayout(new BorderLayout());


        final PhonUIAction<Void> caseSensitiveAct = PhonUIAction.runnable(this::toggleCaseSensitive);
        caseSensitiveAct.putValue(FlatButton.ICON_FONT_NAME_PROP, IconManager.GoogleMaterialDesignIconsFontName);
        caseSensitiveAct.putValue(FlatButton.ICON_NAME_PROP, "match_case");
        caseSensitiveAct.putValue(FlatButton.ICON_SIZE_PROP, IconSize.MEDIUM);
        caseSensitiveAct.putValue(Action.SHORT_DESCRIPTION, "Match case");
        caseSensitiveAct.putValue(Action.SELECTED_KEY, false);
        caseSensitiveButton = new FlatButton(caseSensitiveAct);
        caseSensitiveButton.setIconColor(UIManager.getColor("textInactiveText"));
        caseSensitiveButton.setIconSelectedColor(UIManager.getColor("Phon.darkBlue"));

        final PhonUIAction<Void> regexAct = PhonUIAction.runnable(this::toggleRegex);
        regexAct.putValue(FlatButton.ICON_FONT_NAME_PROP, IconManager.GoogleMaterialDesignIconsFontName);
        regexAct.putValue(FlatButton.ICON_NAME_PROP, "regular_expression");
        regexAct.putValue(FlatButton.ICON_SIZE_PROP, IconSize.MEDIUM);
        regexAct.putValue(Action.SHORT_DESCRIPTION, "Regular expression search");
        regexAct.putValue(Action.SELECTED_KEY, false);
        regexButton = new FlatButton(regexAct);
        regexButton.setIconColor(UIManager.getColor("textInactiveText"));
        regexButton.setIconSelectedColor(UIManager.getColor("Phon.darkBlue"));

        final PhonUIAction<Void> phonexAct = PhonUIAction.runnable(this::togglePhonex);
        phonexAct.putValue(FlatButton.ICON_FONT_NAME_PROP, IconManager.GoogleMaterialDesignIconsFontName);
        phonexAct.putValue(FlatButton.ICON_NAME_PROP, "data_object");
        phonexAct.putValue(FlatButton.ICON_SIZE_PROP, IconSize.MEDIUM);
        phonexAct.putValue(Action.SHORT_DESCRIPTION, "Phonex search");
        phonexAct.putValue(Action.SELECTED_KEY, false);
        phonexButton = new FlatButton(phonexAct);
        phonexButton.setIconColor(UIManager.getColor("textInactiveText"));
        phonexButton.setIconSelectedColor(UIManager.getColor("Phon.darkBlue"));

        final PhonUIAction<Void> filterAct = PhonUIAction.runnable(this::showFilterMenu);
        filterAct.putValue(FlatButton.ICON_FONT_NAME_PROP, IconManager.GoogleMaterialDesignIconsFontName);
        filterAct.putValue(FlatButton.ICON_NAME_PROP, "filter_list");
        filterAct.putValue(FlatButton.ICON_SIZE_PROP, IconSize.MEDIUM);
        filterAct.putValue(Action.SHORT_DESCRIPTION, "Filter search results");
        filterButton = new FlatButton(filterAct);
        filterButton.setIconColor(UIManager.getColor("textInactiveText"));
        filterButton.setIconSelectedColor(UIManager.getColor("Phon.darkBlue"));

        this.searchField = new SearchField("Search tiers...");
        final PhonUIAction<Void> searchAct = PhonUIAction.runnable(this::onQuery);
        this.searchField.setAction(searchAct);

        resultsLabel = new JLabel("0 results");
        resultsLabel.setForeground(UIManager.getColor("textInactiveText"));
        resultsLabel.setHorizontalAlignment(SwingConstants.CENTER);
        resultsLabel.setPreferredSize(new Dimension(100, resultsLabel.getPreferredSize().height));

        final JPanel searchOptionsPanel = new JPanel(new FormLayout("fill:pref:grow, pref, pref, pref, pref, pref", "pref"));
        final CellConstraints cc = new CellConstraints();
        int col = 1;
        searchOptionsPanel.add(searchField, cc.xy(col++, 1));
        searchOptionsPanel.add(caseSensitiveButton, cc.xy(col++, 1));
        searchOptionsPanel.add(regexButton, cc.xy(col++, 1));
        searchOptionsPanel.add(phonexButton, cc.xy(col++, 1));
        searchOptionsPanel.add(resultsLabel, cc.xy(col++, 1));
        searchOptionsPanel.add(filterButton, cc.xy(col++, 1));
        add(searchOptionsPanel, BorderLayout.NORTH);

        this.table = new SearchViewTable(getEditor().getSession(), new ArrayList<>());
        add(new JScrollPane(table), BorderLayout.CENTER);
    }

    private void toggleCaseSensitive() {
        final boolean caseSensitive = caseSensitiveButton.isSelected();
        caseSensitiveButton.setSelected(!caseSensitive);
    }

    private void toggleRegex() {
        final boolean regex = regexButton.isSelected();
        regexButton.setSelected(!regex);
        if(regexButton.isSelected() && phonexButton.isSelected()) {
            phonexButton.setSelected(false);
        }
    }

    private void togglePhonex() {
        final boolean phonex = phonexButton.isSelected();
        phonexButton.setSelected(!phonex);
        if(phonexButton.isSelected() && regexButton.isSelected()) {
            regexButton.setSelected(false);
        }
    }

    private void showFilterMenu() {
        final JPopupMenu filterMenu = new JPopupMenu();
        filterMenu.add(new JMenuItem("Filter by tier"));
        filterMenu.add(new JMenuItem("Filter by speaker"));
        filterMenu.show(filterButton, 0, filterButton.getHeight());
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
