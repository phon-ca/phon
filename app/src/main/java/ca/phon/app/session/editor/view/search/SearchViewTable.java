package ca.phon.app.session.editor.view.search;

import ca.phon.app.session.editor.search.FindManager;
import ca.phon.app.session.editor.search.FindResult;
import ca.phon.orthography.InternalMedia;
import ca.phon.session.*;
import ca.phon.session.Record;
import ca.phon.session.position.TranscriptElementRange;
import ca.phon.util.Range;
import org.jdesktop.swingx.JXTable;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Table for displaying quick search results
 */
public class SearchViewTable extends JXTable {

    public final static String SEARCHING_PROP = SearchViewTable.class.getName() + ".searching";

    private Optional<FindWorker> findWorker = Optional.empty();

    public SearchViewTable(FindManager findManager) {
        super();
        search(findManager);
    }

    public SearchViewTable(SearchViewTableModel model) {
        super(model);
    }

    public SearchViewTable(Session session, List<FindResult> results) {
        super();
        setModel(new SearchViewTableModel(session, results));
    }

    public void clearSearch() {
        setModel(new SearchViewTableModel(this.getSearchViewTableModel().session, new ArrayList<>()));
    }

    /**
     * Search for results using provided find manager.  Exiting results will be cleared first.
     *
     * @param findManager
     */
    public void search(FindManager findManager) {
        final SearchViewTableModel model = new SearchViewTableModel(findManager.getSession(), new ArrayList<>());
        setModel(model);
        if(findWorker.isPresent()) {
            findWorker.get().cancelSearch();
        }
        final FindWorker worker = new FindWorker(findManager, model);
        SearchViewTable.this.firePropertyChange(SEARCHING_PROP, false, true);
        findWorker = Optional.of(worker);
        worker.execute();
    }

    public SearchViewTableModel getSearchViewTableModel() {
        return (SearchViewTableModel)getModel();
    }

    /**
     * Swing worker for finding results
     */
    private class FindWorker extends SwingWorker<List<FindResult>, FindResult> {

        private final FindManager findManager;

        private final SearchViewTableModel model;

        private boolean cancelled = false;

        public FindWorker(FindManager findManager, SearchViewTableModel model) {
            super();
            this.findManager = findManager;
            this.model = model;
        }

        @Override
        protected List<FindResult> doInBackground() throws Exception {
            final List<FindResult> retVal = new ArrayList<>();
            FindResult findResult = null;
            while((findResult = findManager.findNext()) != null) {
                if(cancelled) {
                    break;
                }
                retVal.add(findResult);
                publish(findResult);
            }
            return retVal;
        }

        public void cancelSearch() {
            this.cancelled = true;
        }

        @Override
        protected void process(List<FindResult> chunks) {
            for(FindResult range:chunks) {
                model.appendResult(range);
            }
        }

        @Override
        protected void done() {
            SearchViewTable.this.firePropertyChange(SEARCHING_PROP, true, false);
        }

    }

    /**
     * Search result table
     */
    public static class SearchViewTableModel extends AbstractTableModel {

        private Session session;

        enum Columns {
            RECORD("Record"),
            TIER("Tier"),
            TEXT("Text"),
            Range("Range");

            private final String title;

            Columns(String title) {
                this.title = title;
            }

            public String getTitle() {
                return this.title;
            }

        }

        private List<FindResult> results;

        private List<Integer> invalidatedRows = new ArrayList<>();

        public SearchViewTableModel(Session session, List<FindResult> results) {
            super();
            this.session = session;
            this.results = results;
        }

        @Override
        public int getRowCount() {
            return results.size();
        }

        @Override
        public int getColumnCount() {
            return Columns.values().length - 1;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            final FindResult findResult = results.get(rowIndex);
            final TranscriptElementRange range = findResult.range();
            final Transcript.Element element = session.getTranscript().getElementAt(range.transcriptElementIndex());
            switch(Columns.values()[columnIndex]) {
                case RECORD:
                    if(element.isComment() || element.isGem()) return "";
                    return session.getTranscript().getRecordIndex(range.transcriptElementIndex()) + 1;
                case TIER:
                    return range.tier();
                case TEXT:
                    return invalidatedRows.contains(rowIndex) ? "INVALID" : getSearchResultText(range);
                case Range:
                    return range.range();
            }
            return "";
        }

        private String getSearchResultText(TranscriptElementRange range) {
            final Transcript.Element element = session.getTranscript().getElementAt(range.transcriptElementIndex());
            if(element.isComment()) {
                final String commentText = element.asComment().getValue().toString();
                return getTokenizedText(commentText, range.range());
            } else if(element.isGem()) {
                final String gemText = element.asGem().getLabel().toString();
                return getTokenizedText(gemText, range.range());
            } else {
                final Record r = element.asRecord();
                final Tier<?> tier = r.getTier(range.tier());
                if(tier == null) return "";
                // TODO blind transcriptions
                String tierText = tier.toString();
                if(tier.getDeclaredType() == MediaSegment.class) {
                    tierText = InternalMedia.MEDIA_BULLET + tierText + InternalMedia.MEDIA_BULLET;
                }
                return getTokenizedText(tierText, range.range());
            }
        }

        private String getTokenizedText(String text, Range range) {
            if(range.getStart() < 0 || range.getEnd() > text.length() || range.getStart() >= range.getEnd()) {
                return text; // invalid range, return original text
            }
            final String start = text.substring(0, range.getStart());
            final String middle = text.substring(range.getStart(), range.getEnd());
            final String end = text.substring(range.getEnd());
            return "<html>" + start + "<b>" + middle + "</b>" + end + "</html>";
        }

        @Override
        public String getColumnName(int column) {
            return Columns.values()[column].getTitle();
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            switch(Columns.values()[columnIndex]) {
                case RECORD:
                    return Integer.class;
                case TIER:
                    return String.class;
                case TEXT:
                    return String.class;
                case Range:
                    return String.class;
            }
            return super.getColumnClass(columnIndex);
        }

        public FindResult getResultAt(int rowIndex) {
            return results.get(rowIndex);
        }

        public void appendResult(FindResult result) {
            this.results.add(result);
            fireTableRowsInserted(results.size()-1, results.size()-1);
        }

        public void insertResults(List<FindResult> results, int index) {
            if(index > this.results.size()) {
                throw new IndexOutOfBoundsException("Index out of bounds: " + index);
            }
            if(index < 0) {
                this.results.addAll(results);
                fireTableRowsInserted(this.results.size() - results.size(), this.results.size() - 1);
            } else {
                this.results.addAll(index, results);
                fireTableRowsInserted(index, index + results.size() - 1);
            }
        }

        public void invalidateResultAt(int rowIndex) {
            if(rowIndex < 0 || rowIndex >= results.size()) return;
            invalidatedRows.add(rowIndex);
            fireTableRowsUpdated(rowIndex, rowIndex);
        }

        public boolean isInvalid(int rowIndex) {
            return invalidatedRows.contains(rowIndex);
        }

        public void clearInvalidatedRows() {
            if(invalidatedRows.isEmpty()) return;
            // remove sorted invalidated rows in reverse order
            invalidatedRows.sort(Integer::compareTo);
            for(int i = invalidatedRows.size() - 1; i >= 0; i--) {
                int rowIndex = invalidatedRows.get(i);
                if(rowIndex >= 0 && rowIndex < results.size()) {
                    results.remove(rowIndex);
                    fireTableRowsDeleted(rowIndex, rowIndex);
                }
            }
            invalidatedRows.clear();
        }

        public void setResults(List<FindResult> results) {
            this.results = new ArrayList<>(results);
            this.invalidatedRows.clear();
            fireTableDataChanged();
        }
        
    }

}
