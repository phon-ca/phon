package ca.phon.app.session.editor.view.search;

import ca.phon.app.session.editor.*;
import ca.phon.app.session.editor.search.FindExpr;
import ca.phon.app.session.editor.search.FindManager;
import ca.phon.app.session.editor.search.FindResult;
import ca.phon.app.session.editor.search.SearchType;
import ca.phon.app.session.editor.view.transcript.BoxSelectHighlightPainter;
import ca.phon.app.session.editor.view.transcript.TranscriptView;
import ca.phon.session.Participant;
import ca.phon.session.TierViewItem;
import ca.phon.session.position.TranscriptElementLocation;
import ca.phon.session.position.TranscriptElementRange;
import ca.phon.ui.FlatButton;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.ui.menu.MenuBuilder;
import ca.phon.ui.text.SearchField;
import ca.phon.util.SearchHistory;
import ca.phon.util.SearchHistoryEntry;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import javax.swing.*;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.text.Highlighter;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Search view for session editor. Allows users to search for text in the
 * session transcript.
 * Includes options for case-sensitive, regex, and phonex searches as well as
 * options for filtering
 * search results based on tier and speaker.
 */
@EditorViewInfo(name = SearchView.VIEW_NAME, category = EditorViewCategory.UTILITIES, icon = SearchView.VIEW_ICON)
public class SearchView extends EditorView {

    public static final String VIEW_NAME = "Search";

    public static final String VIEW_ICON = IconManager.GoogleMaterialDesignIconsFontName + ":SEARCH";

    private final static String SEARCH_HISTORY_PROP_PREFIX = "SessionEditor.searchHistory";

    private final static int MAX_SEARCH_HISTORY = 10;

    /**
     * Custom text field with search icon
     */
    private SearchField searchField;

    private JLabel resultsLabel;

    private FlatButton filterButton;

    private FlatButton caseSensitiveButton;

    private FlatButton regexButton;

    private FlatButton phonexButton;

    private SearchViewTable table;

    private List<String> filterTiers = new ArrayList<>();

    private List<Participant> filterSpeakers = new ArrayList<>();

    private boolean includeComments = true;

    private boolean includeGems = true;

    private boolean liveUpdate = true;

    public SearchView(SessionEditor editor) {
        super(editor);

        init();
        setupEditorActions();

        editor.getViewModel().addEditorViewModelListener(new EditorViewModelListener() {
            @Override
            public void viewShown(String viewName) {
                if (viewName.equals(VIEW_NAME)) {
                    if (shouldUpdateHighlights()) {
                        getEditor().getSelectionModel().clear();
                        addHighlights();
                    }
                }
            }

            @Override
            public void viewHidden(String viewName) {
                if (viewName.equals(VIEW_NAME)) {
                    if (shouldUpdateHighlights()) {
                        // clear highlights
                        getEditor().getSelectionModel().clear();
                        currentSelection = null;
                    }
                }
            }

            @Override
            public void viewMinimized(String viewName) {

            }

            @Override
            public void viewMaximized(String viewName) {

            }

            @Override
            public void viewNormalized(String viewName) {

            }

            @Override
            public void viewExternalized(String viewName) {

            }

            @Override
            public void viewFocused(String viewName) {

            }
        });
    }

    private SessionEditorSelection currentSelection = null;

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
        this.searchField.addPropertyChangeListener("text_cleared", (e) -> {
            clearResults();
        });
        this.searchField.setMenuHandler(this::setupSearchContextMenu);

        resultsLabel = new JLabel("0 results");
        resultsLabel.setForeground(UIManager.getColor("textInactiveText"));
        resultsLabel.setHorizontalAlignment(SwingConstants.CENTER);
        resultsLabel.setPreferredSize(new Dimension(100, resultsLabel.getPreferredSize().height));

        final JPanel searchOptionsPanel = new JPanel(
                new FormLayout("fill:pref:grow, pref, pref, pref, pref, pref", "pref"));
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
        this.table.getSelectionModel().addListSelectionListener(tableSelectionListener);
        add(new JScrollPane(table), BorderLayout.CENTER);
    }

    private void setupEditorActions() {
        getEditor().getEventManager().registerActionForEvent(EditorEventType.TierChange, this::onTierChange,
                EditorEventManager.RunOn.AWTEventDispatchThread);
        getEditor().getEventManager().registerActionForEvent(EditorEventType.CommentChanged, this::onCommentChanged,
                EditorEventManager.RunOn.AWTEventDispatchThread);
        getEditor().getEventManager().registerActionForEvent(EditorEventType.CommentAdded, this::onCommentAdded,
                EditorEventManager.RunOn.AWTEventDispatchThread);
        getEditor().getEventManager().registerActionForEvent(EditorEventType.CommentDeleted, this::onCommentDeleted,
                EditorEventManager.RunOn.AWTEventDispatchThread);
        getEditor().getEventManager().registerActionForEvent(EditorEventType.CommentMoved, this::onCommentMoved,
                EditorEventManager.RunOn.AWTEventDispatchThread);
        getEditor().getEventManager().registerActionForEvent(EditorEventType.GemChanged, this::onGemChanged,
                EditorEventManager.RunOn.AWTEventDispatchThread);
        getEditor().getEventManager().registerActionForEvent(EditorEventType.GemAdded, this::onGemAdded,
                EditorEventManager.RunOn.AWTEventDispatchThread);
        getEditor().getEventManager().registerActionForEvent(EditorEventType.GemDeleted, this::onGemDeleted,
                EditorEventManager.RunOn.AWTEventDispatchThread);
        getEditor().getEventManager().registerActionForEvent(EditorEventType.GemMoved, this::onGemMoved,
                EditorEventManager.RunOn.AWTEventDispatchThread);
        getEditor().getEventManager().registerActionForEvent(EditorEventType.RecordAdded, this::onRecordAdded,
                EditorEventManager.RunOn.AWTEventDispatchThread);
        getEditor().getEventManager().registerActionForEvent(EditorEventType.RecordDeleted, this::onRecordDeleted,
                EditorEventManager.RunOn.AWTEventDispatchThread);
        getEditor().getEventManager().registerActionForEvent(EditorEventType.RecordMoved, this::onRecordMoved,
                EditorEventManager.RunOn.AWTEventDispatchThread);
    }

    private void deregisterEditorActions() {
        getEditor().getEventManager().removeActionForEvent(EditorEventType.TierChange, this::onTierChange);
        getEditor().getEventManager().removeActionForEvent(EditorEventType.CommentChanged, this::onCommentChanged);
        getEditor().getEventManager().removeActionForEvent(EditorEventType.CommentAdded, this::onCommentAdded);
        getEditor().getEventManager().removeActionForEvent(EditorEventType.CommentDeleted, this::onCommentDeleted);
        getEditor().getEventManager().removeActionForEvent(EditorEventType.CommentMoved, this::onCommentMoved);
        getEditor().getEventManager().removeActionForEvent(EditorEventType.GemChanged, this::onGemChanged);
        getEditor().getEventManager().removeActionForEvent(EditorEventType.GemAdded, this::onGemAdded);
        getEditor().getEventManager().removeActionForEvent(EditorEventType.GemDeleted, this::onGemDeleted);
        getEditor().getEventManager().removeActionForEvent(EditorEventType.GemMoved, this::onGemMoved);
        getEditor().getEventManager().removeActionForEvent(EditorEventType.RecordAdded, this::onRecordAdded);
        getEditor().getEventManager().removeActionForEvent(EditorEventType.RecordDeleted, this::onRecordDeleted);
        getEditor().getEventManager().removeActionForEvent(EditorEventType.RecordMoved, this::onRecordMoved);
    }

    private void clearResults() {
        getEditor().getSelectionModel().clear();
        this.table.clearSearch();
        this.resultsLabel.setText("0 results");
        this.resultsLabel.setForeground(UIManager.getColor("textInactiveText"));
    }

    private void toggleCaseSensitive() {
        final boolean caseSensitive = caseSensitiveButton.isSelected();
        caseSensitiveButton.setSelected(!caseSensitive);
        onQuery();
    }

    private void toggleRegex() {
        final boolean regex = regexButton.isSelected();
        regexButton.setSelected(!regex);
        if (regexButton.isSelected() && phonexButton.isSelected()) {
            phonexButton.setSelected(false);
        }
        onQuery();
    }

    private void togglePhonex() {
        final boolean phonex = phonexButton.isSelected();
        phonexButton.setSelected(!phonex);
        if (phonexButton.isSelected() && regexButton.isSelected()) {
            regexButton.setSelected(false);
        }
        onQuery();
    }

    private void setupSearchContextMenu(MenuBuilder menuBuilder) {

        final PhonUIAction<Boolean> toggleLiveUpdateAct = PhonUIAction.consumer(this::setLiveUpdate, !liveUpdate);
        toggleLiveUpdateAct.putValue(Action.NAME, "Toggle live update");
        toggleLiveUpdateAct.putValue(Action.SHORT_DESCRIPTION, "Toggle live update of search results");
        toggleLiveUpdateAct.putValue(Action.SELECTED_KEY, liveUpdate);
        final JCheckBoxMenuItem toggleLiveUpdateItem = new JCheckBoxMenuItem(toggleLiveUpdateAct);
        menuBuilder.addItem(".", toggleLiveUpdateItem);

        // Separator after live update option
        menuBuilder.addSeparator(".", "search_history_separator");

        // Search history button
        final PhonUIAction<Void> searchHistoryAct = PhonUIAction.runnable(this::showSearchHistoryPopup);
        searchHistoryAct.putValue(Action.NAME, "Search history");
        searchHistoryAct.putValue(Action.SHORT_DESCRIPTION, "Show search history");
        final JMenuItem searchHistoryItem = new JMenuItem(searchHistoryAct);
        menuBuilder.addItem(".", searchHistoryItem);

    }

    private void showSearchHistoryPopup() {
        // Get search history entries
        final List<SearchHistoryEntry> historyEntries = SearchHistory.getSearchEntries(
                SEARCH_HISTORY_PROP_PREFIX, MAX_SEARCH_HISTORY);

        final DefaultListModel<String> listModel = new DefaultListModel<>();

        if (historyEntries.isEmpty()) {
            listModel.addElement("No search history");
        } else {
            final LocalDateTime now = LocalDateTime.now();
            final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
            final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMM d");

            // Group entries by time period and display query text
            for (SearchHistoryEntry entry : historyEntries) {
                final LocalDateTime entryDate = entry.date();
                final String queryText = entry.queryText();
                final String displayText;

                // Determine time category
                final long daysDiff = ChronoUnit.DAYS.between(entryDate.toLocalDate(), now.toLocalDate());

                if (daysDiff == 0) {
                    // Today - show time and query
                    displayText = String.format("[%s] %s", entryDate.format(timeFormatter), queryText);
                } else if (daysDiff <= 7) {
                    // This week - show date and query
                    displayText = String.format("[%s] %s", entryDate.format(dateFormatter), queryText);
                } else if (daysDiff <= 30) {
                    // This month - show date and query
                    displayText = String.format("[%s] %s", entryDate.format(dateFormatter), queryText);
                } else {
                    // > 30 days - show date and query
                    displayText = String.format("[%s] %s", entryDate.format(dateFormatter), queryText);
                }

                listModel.addElement(displayText);
            }
        }

        final JList<String> historyList = new JList<>(listModel);
        historyList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        historyList.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // Make items non-clickable for now (could be enhanced later to populate search
        // field)
        historyList.setEnabled(false);
        historyList.setFocusable(false);
        historyList.setSelectionModel(new DefaultListSelectionModel() {
            @Override
            public void setSelectionInterval(int index0, int index1) {
                // Do nothing - prevent selection
            }

            @Override
            public void addSelectionInterval(int index0, int index1) {
                // Do nothing - prevent selection
            }
        });

        // Set preferred size for the list - adjust height based on content
        final int listHeight = Math.min(200, Math.max(80, listModel.getSize() * 20 + 10));
        historyList.setPreferredSize(new Dimension(250, listHeight));

        // Create popup and show it
        final JPopupMenu popup = new JPopupMenu();
        popup.add(new JScrollPane(historyList));

        // Position the popup relative to the search field
        popup.show(searchField, 0, searchField.getHeight());
    }

    public void setLiveUpdate(Boolean liveUpdate) {
        var oldVal = this.liveUpdate;
        this.liveUpdate = liveUpdate;
        super.firePropertyChange("liveUpdate", oldVal, this.liveUpdate);
    }

    private void showFilterMenu() {
        final JPopupMenu filterMenu = new JPopupMenu();

        filterMenu.add(new JLabel("Filter by tier:"));
        for (TierViewItem tvi : getEditor().getSession().getTierView()) {
            if (!tvi.isVisible())
                continue;
            final JCheckBoxMenuItem tierItem = new JCheckBoxMenuItem(tvi.getTierName());
            tierItem.setSelected(filterTiers.contains(tvi.getTierName()));
            tierItem.addActionListener((e) -> {
                if (tierItem.isSelected()) {
                    filterTiers.add(tvi.getTierName());
                } else {
                    filterTiers.remove(tvi.getTierName());
                }
                onQuery();
            });
            filterMenu.add(tierItem);
        }

        filterMenu.addSeparator();

        filterMenu.add(new JLabel("Filter by speaker:"));
        final List<Participant> participants = new ArrayList<>();
        for (Participant speaker : getEditor().getSession().getParticipants()) {
            participants.add(speaker);
        }
        participants.add(Participant.UNKNOWN);
        for (Participant speaker : participants) {
            final JCheckBoxMenuItem speakerItem = new JCheckBoxMenuItem(speaker.toString());
            speakerItem.setSelected(filterSpeakers.contains(speaker));
            speakerItem.addActionListener((e) -> {
                if (speakerItem.isSelected()) {
                    filterSpeakers.add(speaker);
                } else {
                    filterSpeakers.remove(speaker);
                }
                onQuery();
            });
            filterMenu.add(speakerItem);
        }

        filterMenu.addSeparator();
        final JCheckBoxMenuItem includeCommentsItem = new JCheckBoxMenuItem("Include comments");
        includeCommentsItem.setSelected(includeComments);
        includeCommentsItem.addActionListener((e) -> {
            includeComments = includeCommentsItem.isSelected();
            onQuery();
        });
        filterMenu.add(includeCommentsItem);

        final JCheckBoxMenuItem includeGemsItem = new JCheckBoxMenuItem("Include gems");
        includeGemsItem.setSelected(includeGems);
        includeGemsItem.addActionListener((e) -> {
            includeGems = includeGemsItem.isSelected();
            onQuery();
        });
        filterMenu.add(includeGemsItem);

        filterMenu.addSeparator();
        final JMenuItem resetItem = new JCheckBoxMenuItem("Reset filters");
        resetItem.addActionListener((e) -> {
            filterTiers.clear();
            filterSpeakers.clear();
            includeComments = true;
            includeGems = true;
            onQuery();
        });
        filterMenu.add(resetItem);

        filterMenu.show(filterButton, 0, filterButton.getHeight());
    }

    private void setupSearchTiers(FindManager findManager) {
        final List<String> searchTiers = new ArrayList<>();
        for (TierViewItem tvi : getEditor().getSession().getTierView()) {
            if (!tvi.isVisible())
                continue;
            if (filterTiers.size() > 0 && !filterTiers.contains(tvi.getTierName()))
                continue;
            searchTiers.add(tvi.getTierName());
        }
        findManager.setSearchTiers(searchTiers.toArray(new String[0]));
    }

    private void setupRecordFilter(FindManager findManager) {
        final List<Participant> speakers = new ArrayList<>();
        if (filterSpeakers.size() > 0) {
            if (filterSpeakers.contains(Participant.UNKNOWN)) {
                speakers.add(Participant.UNKNOWN);
            }
            for (Participant speaker : getEditor().getSession().getParticipants()) {
                if (!filterSpeakers.contains(speaker))
                    continue;
                speakers.add(speaker);
            }
            findManager.setSpeakers(speakers);
        }
        findManager.setIncludeComments(includeComments);
        findManager.setIncludeGems(includeGems);
    }

    private void updateFilterButton() {
        if (filterTiers.size() > 0 || filterSpeakers.size() > 0 || !includeComments || !includeGems) {
            filterButton.setSelected(true);
        } else {
            filterButton.setSelected(false);
        }
    }

    /**
     * Return a new FindManager with the current search settings.
     *
     * @return a new FindManager with the current search settings
     */
    private FindManager createFindManager() {
        final FindManager findManager = new FindManager(getEditor().getSession());
        final String query = searchField.getText();
        final SearchType searchType = regexButton.isSelected() ? SearchType.REGEX
                : phonexButton.isSelected() ? SearchType.PHONEX : SearchType.PLAIN;
        final FindExpr findExpr = new FindExpr(searchType, query, caseSensitiveButton.isSelected());
        findManager.setAnyExpr(findExpr);
        setupSearchTiers(findManager);
        setupRecordFilter(findManager);
        return findManager;
    }

    /**
     * Executes query will all current filters
     *
     */
    public void onQuery() {
        getEditor().getSelectionModel().clear();
        updateFilterButton();
        final String queryText = searchField.getText();
        if (queryText.trim().length() == 0) {
            clearResults();
            return;
        }

        // Add search entry to history
        final String queryType = regexButton.isSelected() ? "regex"
                : phonexButton.isSelected() ? "phonex" : "plain";
        final SearchHistoryEntry historyEntry = SearchHistoryEntry.builder()
                .queryText(queryText.trim())
                .queryType(queryType)
                .caseSensitive(caseSensitiveButton.isSelected())
                .build();
        SearchHistory.addSearchEntry(SEARCH_HISTORY_PROP_PREFIX, historyEntry, MAX_SEARCH_HISTORY);

        final FindManager findManager = createFindManager();
        findManager.setCurrentLocation(new TranscriptElementLocation(0, findManager.getSearchTiers()[0], 0));

        this.resultsLabel.setText("0 results");
        this.resultsLabel.setForeground(UIManager.getColor("textInactiveText"));
        this.table.search(findManager);
        final TableModelListener listener = new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent e) {
                if (e.getType() != TableModelEvent.INSERT)
                    return;
                for (int i = e.getFirstRow(); i <= e.getLastRow(); i++) {
                    final FindResult findResult = table.getSearchViewTableModel().getResultAt(i);
                    if (findResult == null)
                        continue;
                    if (!getEditor().getViewModel().isShowing(VIEW_NAME))
                        return;
                    if (shouldUpdateHighlights()) {
                        final SessionEditorSelection selection = new SessionEditorSelection(findResult.range());
                        selection.putExtension(Highlighter.HighlightPainter.class, new BoxSelectHighlightPainter());
                        getEditor().getSelectionModel().addSelection(selection);
                    }
                }
                final int range = e.getLastRow() - e.getFirstRow() + 1;
                final int total = table.getRowCount() + range;
                resultsLabel.setText(total + (total == 1 ? " result" : " results"));
            }
        };
        this.table.getModel().addTableModelListener(listener);
        this.table.addPropertyChangeListener(SearchViewTable.SEARCHING_PROP, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (!(Boolean) evt.getNewValue()) {
                    resultsLabel.setForeground(UIManager.getColor("textText"));
                    table.getModel().removeTableModelListener(listener);
                    table.removePropertyChangeListener(this);
                    table.packAll();
                }
            }
        });
    }

    private void addHighlights() {
        if (!shouldUpdateHighlights())
            return;
        for (int i = 0; i < table.getSearchViewTableModel().getRowCount(); i++) {
            final FindResult findResult = table.getSearchViewTableModel().getResultAt(i);
            if (findResult == null)
                continue;
            final SessionEditorSelection selection = new SessionEditorSelection(findResult.range());
            selection.putExtension(Highlighter.HighlightPainter.class, new BoxSelectHighlightPainter());
            getEditor().getSelectionModel().addSelection(selection);
        }
    }

    private void onRecordAdded(EditorEvent<EditorEventType.RecordAddedData> ee) {
        final int elementIndex = getEditor().getSession().getRecordElementIndex(ee.data().record());
        onElementAdded(elementIndex);
    }

    private void onRecordMoved(EditorEvent<EditorEventType.RecordMovedData> ee) {
        onQuery();
    }

    private void onRecordDeleted(EditorEvent<EditorEventType.RecordDeletedData> ee) {
        final int elementIndex = ee.data().elementIndex();
        onElementRemoved(elementIndex);
    }

    private void onCommentAdded(EditorEvent<EditorEventType.CommentAddedData> ee) {
        final int elementIndex = ee.data().elementIndex();
        onElementAdded(elementIndex);
    }

    private void onCommentDeleted(EditorEvent<EditorEventType.CommentDeletedData> ee) {
        final int elementIndex = ee.data().elementIndex();
        onElementRemoved(elementIndex);
    }

    private void onCommentMoved(EditorEvent<EditorEventType.CommentMovedData> ee) {
        onQuery();
    }

    private void onGemAdded(EditorEvent<EditorEventType.GemAddedData> ee) {
        final int elementIndex = ee.data().elementIndex();
        onElementAdded(elementIndex);
    }

    private void onGemDeleted(EditorEvent<EditorEventType.GemDeletedData> ee) {
        final int elementIndex = ee.data().elementIndex();
        onElementRemoved(elementIndex);
    }

    private void onGemMoved(EditorEvent<EditorEventType.GemMovedData> ee) {
        onQuery();
    }

    private boolean shouldUpdateHighlights() {
        if (!getEditor().getViewModel().isShowing(VIEW_NAME))
            return false;
        if (table.getSearchViewTableModel().getRowCount() == 0)
            return false;
        if (isFindAndReplaceActive())
            return false; // don't update highlights while find and replace is active
        return true;
    }

    private boolean isFindAndReplaceActive() {
        final TranscriptView transcriptView = (TranscriptView) getEditor().getViewModel()
                .getView(TranscriptView.VIEW_NAME);
        if (transcriptView == null)
            return false;

        return transcriptView.isFindAndReplaceActive();
    }

    private void onElementAdded(int elementIndex) {
        final SearchViewTable.SearchViewTableModel model = table.getSearchViewTableModel();
        if (model.getRowCount() == 0)
            return;
        getEditor().getSelectionModel().clear();
        // increment record index for all results as necessary
        final List<FindResult> results = new ArrayList<>();
        for (int i = 0; i < model.getRowCount(); i++) {
            final FindResult findResult = model.getResultAt(i);
            if (findResult == null)
                continue;
            final TranscriptElementRange range = findResult.range();
            if (range.transcriptElementIndex() >= elementIndex) {
                final TranscriptElementRange newRange = new TranscriptElementRange(range.transcriptElementIndex() + 1,
                        range.tier(), range.range());
                final FindResult newFindResult = new FindResult(findResult.expr(), newRange, findResult.matcher(),
                        findResult.phonexMatcher());
                results.add(newFindResult);
            } else {
                results.add(findResult);
            }
        }
        model.setResults(results);
        addHighlights();
    }

    private void onElementRemoved(int elementIndex) {
        final SearchViewTable.SearchViewTableModel model = table.getSearchViewTableModel();
        if (model.getRowCount() == 0)
            return;
        getEditor().getSelectionModel().clear();
        // decrement record index for all results as necessary
        final List<FindResult> results = new ArrayList<>();
        for (int i = 0; i < model.getRowCount(); i++) {
            final FindResult findResult = model.getResultAt(i);
            if (findResult == null)
                continue;
            final TranscriptElementRange range = findResult.range();
            if (range.transcriptElementIndex() > elementIndex) {
                final TranscriptElementRange newRange = new TranscriptElementRange(range.transcriptElementIndex() - 1,
                        range.tier(), range.range());
                final FindResult newFindResult = new FindResult(findResult.expr(), newRange, findResult.matcher(),
                        findResult.phonexMatcher());
                results.add(newFindResult);
            } else if (range.transcriptElementIndex() == elementIndex) {
                // remove result
                continue;
            } else {
                results.add(findResult);
            }
        }
        model.setResults(results);
        addHighlights();
    }

    private void onTierChange(EditorEvent<EditorEventType.TierChangeData> ee) {
        if (table.getSearchViewTableModel().getRowCount() == 0)
            return;
        if (ee.data().valueAdjusting())
            return;
        final SearchViewTable.SearchViewTableModel model = table.getSearchViewTableModel();
        int insertIndex = -1;
        boolean hasInvalidated = false;
        for (int i = 0; i < model.getRowCount(); i++) {
            final FindResult findResult = model.getResultAt(i);
            if (findResult == null)
                continue;
            final int currentTranscriptElementIndex = getEditor().getSession()
                    .getRecordElementIndex(ee.data().record());

            final List<SessionEditorSelection> selectionsForTier = getEditor().getSelectionModel()
                    .getSelectionsForTier(currentTranscriptElementIndex, ee.data().tier().getName());
            if (shouldUpdateHighlights()) {
                for (SessionEditorSelection selection : selectionsForTier) {
                    getEditor().getSelectionModel().removeSelection(selection);
                }
            }
            if (findResult.range().transcriptElementIndex() == currentTranscriptElementIndex) {
                if (ee.data().tier().getName().equals(findResult.range().tier())) {
                    if (insertIndex == -1) {
                        insertIndex = i;
                    }
                    // invalidate result
                    hasInvalidated = true;
                    model.invalidateResultAt(i);
                }
            }
        }
        if (!liveUpdate)
            return;
        model.clearInvalidatedRows();
        if (hasInvalidated) {
            updateResultsLabel();
        }
        if (searchField.getText().trim().isEmpty())
            return;
        final int elementIndex = getEditor().getSession().getRecordElementIndex(ee.data().record());
        // create new find results for tier
        final TranscriptElementLocation startLoc = new TranscriptElementLocation(elementIndex,
                ee.data().tier().getName(), 0);
        final FindManager findManager = createFindManager();
        findManager.setCurrentLocation(startLoc);
        FindResult findResult = null;
        final List<FindResult> results = new ArrayList<>();
        while ((findResult = findManager.findNext()) != null) {
            if (findResult.range().transcriptElementIndex() != elementIndex)
                break;
            if (!findResult.range().tier().equals(ee.data().tier().getName()))
                break;
            results.add(findResult);
        }
        if (insertIndex == -1) {
            insertIndex = 0;
            // find the correct insert index based on current data
            for (int i = 0; i < model.getRowCount(); i++) {
                final FindResult fr = model.getResultAt(i);
                insertIndex = i;
                if (fr.range().transcriptElementIndex() > elementIndex) {
                    break;
                } else if (fr.range().transcriptElementIndex() == elementIndex) {
                    final List<String> searchTiers = List.of(findManager.getSearchTiers());
                    final int frTierIndex = searchTiers.indexOf(fr.range().tier());
                    final int newTierIndex = searchTiers.indexOf(ee.data().tier().getName());
                    if (newTierIndex < frTierIndex) {
                        break;
                    }
                }
            }
        }
        if (!results.isEmpty()) {
            insertResults(results, insertIndex);
            updateResultsLabel();
        }
    }

    private void onCommentChanged(EditorEvent<EditorEventType.CommentChangedData> ee) {
        if (table.getSearchViewTableModel().getRowCount() == 0)
            return;
        final SearchViewTable.SearchViewTableModel model = table.getSearchViewTableModel();
        int insertIndex = -1;
        boolean hasInvalidated = false;
        for (int i = 0; i < model.getRowCount(); i++) {
            final FindResult findResult = model.getResultAt(i);
            if (findResult == null)
                continue;

            final List<SessionEditorSelection> selectionsForTier = getEditor().getSelectionModel()
                    .getSelectionsForTier(ee.data().elementIndex(), ee.data().comment().getType().name());
            if (shouldUpdateHighlights()) {
                for (SessionEditorSelection selection : selectionsForTier) {
                    getEditor().getSelectionModel().removeSelection(selection);
                }
            }
            if (findResult.range().transcriptElementIndex() == ee.data().elementIndex()) {
                if (insertIndex == -1) {
                    insertIndex = i;
                }
                // invalidate result
                hasInvalidated = true;
                model.invalidateResultAt(i);
            }
        }
        if (!liveUpdate)
            return;
        model.clearInvalidatedRows();
        if (hasInvalidated) {
            updateResultsLabel();
        }
        if (searchField.getText().trim().isEmpty())
            return;
        // create new find results for comment
        final TranscriptElementLocation startLoc = new TranscriptElementLocation(ee.data().elementIndex(),
                ee.data().comment().getType().name(), 0);
        final FindManager findManager = createFindManager();
        findManager.setCurrentLocation(startLoc);
        FindResult findResult = null;
        final List<FindResult> results = new ArrayList<>();
        while ((findResult = findManager.findNext()) != null) {
            if (findResult.range().transcriptElementIndex() != ee.data().elementIndex())
                break;
            results.add(findResult);
        }
        if (insertIndex == -1) {
            insertIndex = 0;
            // find the correct insert index based on current data
            for (int i = 0; i < model.getRowCount(); i++) {
                final FindResult fr = model.getResultAt(i);
                insertIndex = i;
                if (fr.range().transcriptElementIndex() > ee.data().elementIndex()) {
                    break;
                }
            }
        }
        if (!results.isEmpty()) {
            insertResults(results, insertIndex);
            updateResultsLabel();
        }
    }

    private void onGemChanged(EditorEvent<EditorEventType.GemChangedData> ee) {
        if (table.getSearchViewTableModel().getRowCount() == 0)
            return;
        final SearchViewTable.SearchViewTableModel model = table.getSearchViewTableModel();
        int insertIndex = -1;
        boolean hasInvalidated = false;
        for (int i = 0; i < model.getRowCount(); i++) {
            final FindResult findResult = model.getResultAt(i);
            if (findResult == null)
                continue;

            final List<SessionEditorSelection> selectionsForTier = getEditor().getSelectionModel()
                    .getSelectionsForTier(ee.data().elementIndex(), ee.data().gem().getType().name());
            if (shouldUpdateHighlights()) {
                for (SessionEditorSelection selection : selectionsForTier) {
                    getEditor().getSelectionModel().removeSelection(selection);
                }
            }
            if (findResult.range().transcriptElementIndex() == ee.data().elementIndex()) {
                if (insertIndex == -1) {
                    insertIndex = i;
                }
                // invalidate result
                hasInvalidated = true;
                model.invalidateResultAt(i);
            }
        }
        if (!liveUpdate)
            return;
        model.clearInvalidatedRows();
        if (hasInvalidated) {
            updateResultsLabel();
        }
        if (searchField.getText().trim().isEmpty())
            return;
        // create new find results for gem
        final TranscriptElementLocation startLoc = new TranscriptElementLocation(ee.data().elementIndex(),
                ee.data().gem().getType().name(), 0);
        final FindManager findManager = createFindManager();
        findManager.setCurrentLocation(startLoc);
        FindResult findResult = null;
        final List<FindResult> results = new ArrayList<>();
        while ((findResult = findManager.findNext()) != null) {
            if (findResult.range().transcriptElementIndex() != ee.data().elementIndex())
                break;
            results.add(findResult);
        }
        if (insertIndex == -1) {
            insertIndex = 0;
            // find the correct insert index based on current data
            for (int i = 0; i < model.getRowCount(); i++) {
                final FindResult fr = model.getResultAt(i);
                insertIndex = i;
                if (fr.range().transcriptElementIndex() > ee.data().elementIndex()) {
                    break;
                }
            }
        }
        if (!results.isEmpty()) {
            insertResults(results, insertIndex);
            updateResultsLabel();
        }
    }

    private void insertResults(List<FindResult> results, int insertIndex) {
        if (results.isEmpty())
            return;
        table.getSearchViewTableModel().insertResults(results, insertIndex);
        if (shouldUpdateHighlights()) {
            for (FindResult result : results) {
                final SessionEditorSelection selection = new SessionEditorSelection(result.range());
                selection.putExtension(Highlighter.HighlightPainter.class, new BoxSelectHighlightPainter());
                getEditor().getSelectionModel().addSelection(selection);
            }
        }
    }

    private void updateResultsLabel() {
        final int total = table.getSearchViewTableModel().getRowCount();
        resultsLabel.setText(total + (total == 1 ? " result" : " results"));
        if (total > 0) {
            resultsLabel.setForeground(UIManager.getColor("textText"));
        } else {
            resultsLabel.setForeground(UIManager.getColor("textInactiveText"));
        }
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

    @Override
    public Properties getStateProperties() {
        final Properties retVal = super.getStateProperties();
        retVal.put("caseSensitive", Boolean.toString(caseSensitiveButton.isSelected()));
        retVal.put("regex", Boolean.toString(regexButton.isSelected()));
        retVal.put("phonex", Boolean.toString(phonexButton.isSelected()));
        retVal.put("liveUpdate", Boolean.toString(liveUpdate));
        return retVal;
    }

    @Override
    public void loadStateProperties(Properties props) {
        super.loadStateProperties(props);
        if (props.containsKey("caseSensitive") && caseSensitiveButton != null) {
            caseSensitiveButton.setSelected(Boolean.parseBoolean(props.getProperty("caseSensitive")));
        }
        if (props.containsKey("regex") && regexButton != null) {
            regexButton.setSelected(Boolean.parseBoolean(props.getProperty("regex")));
        }
        if (props.containsKey("phonex") && phonexButton != null) {
            phonexButton.setSelected(Boolean.parseBoolean(props.getProperty("phonex")));
        }
        if (props.containsKey("liveUpdate")) {
            liveUpdate = Boolean.parseBoolean(props.getProperty("liveUpdate"));
        }
    }

    /**
     * Listener for table selection and update transcript view
     */
    private final ListSelectionListener tableSelectionListener = (e) -> {
        final int row = table.getSelectedRow();
        if (table.getSearchViewTableModel().isInvalid(row)) {
            // row is invalid, do nothing
            return;
        }
        if (row >= 0) {
            final FindResult findResult = table.getSearchViewTableModel().getResultAt(row);
            final TranscriptElementRange range = findResult.range();
            final TranscriptElementLocation start = range.start();
            final TranscriptElementLocation end = range.end();
            // add selection to model
            if (currentSelection != null) {
                getEditor().getSelectionModel().removeSelection(currentSelection);
            }

            final SessionEditorSelection selection = new SessionEditorSelection(range);
            getEditor().getSelectionModel().addSelection(selection);
            currentSelection = selection;

            // move transcript view caret
            if (getEditor().getViewModel().isShowing(TranscriptView.VIEW_NAME)) {
                final TranscriptView transcriptView = (TranscriptView) getEditor().getViewModel()
                        .getView(TranscriptView.VIEW_NAME);
                if (transcriptView.getTranscriptEditor().isSingleRecordView()) {
                    final int recordIndex = transcriptView.getEditor().getSession().getTranscript()
                            .getRecordIndex(start.transcriptElementIndex());
                    if (recordIndex >= 0) {
                        getEditor().setCurrentRecordIndex(recordIndex);
                    }
                } else {
                    // ??? is this necessary since we are already setting the selection?
                    final int caretLocation = transcriptView.getTranscriptEditor().sessionLocationToCharPos(start);
                    final int endLocation = transcriptView.getTranscriptEditor().sessionLocationToCharPos(end);
                    if (caretLocation >= 0 && endLocation >= caretLocation) {
                        // also select text
                        transcriptView.getTranscriptEditor().getCaret().setDot(caretLocation);
                        transcriptView.getTranscriptEditor().getCaret().moveDot(endLocation);
                    }
                }
            }
        }
    };

}
