/*
 * Copyright (C) 2005-2020 Gregory Hedlund & Yvan Rose
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at

 *    http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ca.phon.ui;

import ca.phon.util.SearchHistory;
import ca.phon.util.SearchHistoryEntry;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * A reusable list view component for displaying search history entries with
 * grouped time periods and custom styling. This component provides a
 * popup-style
 * interface for selecting previous search queries.
 * 
 * <p>
 * Features:
 * </p>
 * <ul>
 * <li>Groups entries by time periods (Today, This week, This month, > 30
 * days)</li>
 * <li>Custom cell renderer with header styling</li>
 * <li>Prevents selection of group headers</li>
 * <li>Configurable selection callback</li>
 * <li>Automatic sizing based on content</li>
 * </ul>
 * 
 * <p>
 * Usage example:
 * </p>
 * 
 * <pre>
 * SearchHistoryListView historyView = new SearchHistoryListView("search.session");
 * historyView.setSelectionCallback(entry -> {
 *     searchField.setText(entry.queryText());
 *     // Apply other search parameters...
 * });
 * 
 * JPopupMenu popup = new JPopupMenu();
 * popup.add(historyView.createScrollPane());
 * popup.show(component, x, y);
 * </pre>
 */
public class SearchHistoryListView extends JList<String> {

    private static final int DEFAULT_MAX_HISTORY = 10;
    private static final int MIN_HEIGHT = 80;
    private static final int MAX_HEIGHT = 200;
    private static final int MIN_WIDTH = 200;
    private static final int MAX_WIDTH = 400;
    private static final int ITEM_HEIGHT = 20;
    private static final int PADDING = 10;
    private static final int WIDTH_PADDING = 20;

    private final String historyPrefix;
    private final int maxHistoryEntries;
    private final DefaultListModel<String> listModel;
    private final List<SearchHistoryEntry> historyEntries;

    private Consumer<SearchHistoryEntry> selectionCallback;

    /**
     * Creates a new SearchHistoryListView with the specified history prefix
     * and default maximum entries.
     * 
     * @param historyPrefix the prefix for the search history context
     */
    public SearchHistoryListView(String historyPrefix) {
        this(historyPrefix, DEFAULT_MAX_HISTORY);
    }

    /**
     * Creates a new SearchHistoryListView with the specified history prefix
     * and maximum entries.
     * 
     * @param historyPrefix     the prefix for the search history context
     * @param maxHistoryEntries maximum number of history entries to display
     */
    public SearchHistoryListView(String historyPrefix, int maxHistoryEntries) {
        this.historyPrefix = historyPrefix;
        this.maxHistoryEntries = maxHistoryEntries;
        this.listModel = new DefaultListModel<>();
        this.historyEntries = new ArrayList<>();

        setModel(listModel);
        setupUI();
        loadHistoryEntries();
    }

    /**
     * Sets the callback to be invoked when a history entry is selected.
     * 
     * @param selectionCallback the callback to invoke with the selected entry
     */
    public void setSelectionCallback(Consumer<SearchHistoryEntry> selectionCallback) {
        this.selectionCallback = selectionCallback;
    }

    /**
     * Refreshes the history entries from storage and updates the display.
     */
    public void refresh() {
        loadHistoryEntries();
    }

    /**
     * Creates a JScrollPane containing this list view with appropriate sizing.
     * 
     * @return a configured JScrollPane containing this list
     */
    public JScrollPane createScrollPane() {
        JScrollPane scrollPane = new JScrollPane(this);
        scrollPane.setPreferredSize(calculatePreferredSize());
        return scrollPane;
    }

    private void setupUI() {
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        setCellRenderer(new HistoryCellRenderer());
        setSelectionModel(new HistorySelectionModel());

        addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && selectionCallback != null) {
                handleSelection();
            }
        });
    }

    private void loadHistoryEntries() {
        historyEntries.clear();
        listModel.clear();

        final List<SearchHistoryEntry> entries = SearchHistory.getSearchEntries(
                historyPrefix, maxHistoryEntries);
        historyEntries.addAll(entries);

        populateListModel();
    }

    private void populateListModel() {
        if (historyEntries.isEmpty()) {
            listModel.addElement("No search history");
            return;
        }

        final LocalDateTime now = LocalDateTime.now();

        // Group entries by time period
        final List<SearchHistoryEntry> todayEntries = new ArrayList<>();
        final List<SearchHistoryEntry> thisWeekEntries = new ArrayList<>();
        final List<SearchHistoryEntry> thisMonthEntries = new ArrayList<>();
        final List<SearchHistoryEntry> olderEntries = new ArrayList<>();

        for (SearchHistoryEntry entry : historyEntries) {
            final LocalDateTime entryDate = entry.date();
            final long daysDiff = ChronoUnit.DAYS.between(entryDate.toLocalDate(), now.toLocalDate());

            if (daysDiff == 0) {
                todayEntries.add(entry);
            } else if (daysDiff <= 7) {
                thisWeekEntries.add(entry);
            } else if (daysDiff <= 30) {
                thisMonthEntries.add(entry);
            } else {
                olderEntries.add(entry);
            }
        }

        // Add entries with headings for each non-empty group
        if (!todayEntries.isEmpty()) {
            listModel.addElement("Today");
            for (SearchHistoryEntry entry : todayEntries) {
                listModel.addElement("  " + entry.queryText());
            }
        }

        if (!thisWeekEntries.isEmpty()) {
            listModel.addElement("This week");
            for (SearchHistoryEntry entry : thisWeekEntries) {
                listModel.addElement("  " + entry.queryText());
            }
        }

        if (!thisMonthEntries.isEmpty()) {
            listModel.addElement("This month");
            for (SearchHistoryEntry entry : thisMonthEntries) {
                listModel.addElement("  " + entry.queryText());
            }
        }

        if (!olderEntries.isEmpty()) {
            listModel.addElement("> 30 days");
            for (SearchHistoryEntry entry : olderEntries) {
                listModel.addElement("  " + entry.queryText());
            }
        }
    }

    private void handleSelection() {
        int selectedIndex = getSelectedIndex();
        if (selectedIndex >= 0) {
            String selectedText = listModel.getElementAt(selectedIndex);
            if (selectedText.startsWith("  ") && !selectedText.equals("No search history")) {
                // Extract query text from the display text
                // Format is now just " query"
                String queryText = selectedText.substring(2); // Remove the " " prefix

                // Find the corresponding history entry
                SearchHistoryEntry matchingEntry = null;
                for (SearchHistoryEntry entry : historyEntries) {
                    if (entry.queryText().equals(queryText)) {
                        matchingEntry = entry;
                        break;
                    }
                }

                if (matchingEntry != null) {
                    // Invoke the selection callback
                    selectionCallback.accept(matchingEntry);
                }
            }
        }
    }

    private Dimension calculatePreferredSize() {
        final int listHeight = Math.min(MAX_HEIGHT,
                Math.max(MIN_HEIGHT, listModel.getSize() * ITEM_HEIGHT + PADDING));

        // Calculate width based on content
        int maxWidth = MIN_WIDTH;
        FontMetrics fontMetrics = getFontMetrics(getFont());
        FontMetrics boldFontMetrics = getFontMetrics(getFont().deriveFont(Font.BOLD));

        for (int i = 0; i < listModel.getSize(); i++) {
            String text = listModel.getElementAt(i);
            boolean isHeader = !text.startsWith("  ") && !text.equals("No search history");

            FontMetrics metrics = isHeader ? boldFontMetrics : fontMetrics;
            int textWidth = metrics.stringWidth(text) + WIDTH_PADDING;
            maxWidth = Math.max(maxWidth, textWidth);
        }

        final int listWidth = Math.min(MAX_WIDTH, maxWidth);
        return new Dimension(listWidth, listHeight);
    }

    /**
     * Custom cell renderer that distinguishes headers from entries.
     */
    private static class HistoryCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            String text = value.toString();
            // Headers don't start with spaces, entries do
            boolean isHeader = !text.startsWith("  ") && !text.equals("No search history");

            if (isHeader) {
                setFont(getFont().deriveFont(Font.BOLD));
                setForeground(UIManager.getColor("textInactiveText"));
                setIcon(IconManager.getInstance().getFontIcon(IconManager.GoogleMaterialDesignIconsFontName, "history", IconSize.SMALL, UIManager.getColor("textInactiveText")));
                if (isSelected) {
                    setBackground(list.getBackground());
                    setOpaque(false);
                }
            } else {
                setFont(getFont().deriveFont(Font.PLAIN));
                if (!isSelected) {
                    setForeground(UIManager.getColor("textText"));
                }
            }

            return this;
        }
    }

    /**
     * Custom selection model that prevents selection of headers.
     */
    private class HistorySelectionModel extends DefaultListSelectionModel {
        @Override
        public void setSelectionInterval(int index0, int index1) {
            if (isSelectableIndex(index0)) {
                super.setSelectionInterval(index0, index1);
            }
        }

        @Override
        public void addSelectionInterval(int index0, int index1) {
            if (isSelectableIndex(index0)) {
                super.addSelectionInterval(index0, index1);
            }
        }

        private boolean isSelectableIndex(int index) {
            if (index < 0 || index >= listModel.getSize()) {
                return false;
            }
            String text = listModel.getElementAt(index);
            // Headers don't start with spaces, entries do
            return text.startsWith("  ") || text.equals("No search history");
        }
    }
}
