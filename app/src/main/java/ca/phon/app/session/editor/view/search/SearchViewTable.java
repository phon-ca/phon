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

/**
 * Table for displaying quick search results
 */
public class SearchViewTable extends JXTable {

    public final static String SEARCHING_PROP = SearchViewTable.class.getName() + ".searching";

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
        final FindWorker worker = new FindWorker(findManager);
        SearchViewTable.this.firePropertyChange(SEARCHING_PROP, false, true);
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

        public FindWorker(FindManager findManager) {
            super();
            this.findManager = findManager;
        }

        @Override
        protected List<FindResult> doInBackground() throws Exception {
            final List<FindResult> retVal = new ArrayList<>();
            FindResult findResult = null;
            while((findResult = findManager.findNext()) != null) {
                retVal.add(findResult);
                publish(findResult);
            }
            return retVal;
        }

        @Override
        protected void process(List<FindResult> chunks) {
            for(FindResult range:chunks) {
                getSearchViewTableModel().appendResult(range);
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
                    return getSearchResultText(range);
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

    }

}
