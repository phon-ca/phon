package ca.phon.app.opgraph.wizard;

import ca.phon.app.PhonURI;
import ca.phon.app.actions.PhonURISchemeHandler;
import ca.phon.app.log.BufferPanel;
import ca.phon.app.log.LogUtil;
import ca.phon.app.modules.EntryPointArgs;
import ca.phon.app.session.editor.EditorSelectionModel;
import ca.phon.app.session.editor.SessionEditor;
import ca.phon.app.session.editor.SessionEditorEP;
import ca.phon.app.session.editor.SessionEditorSelection;
import ca.phon.plugin.PluginEntryPointRunner;
import ca.phon.plugin.PluginException;
import ca.phon.project.Project;
import ca.phon.query.db.Result;
import ca.phon.query.db.ResultValue;
import ca.phon.query.report.datasource.DefaultTableDataSource;
import ca.phon.query.report.datasource.TableDataSource;
import ca.phon.session.SessionPath;
import ca.phon.ui.CommonModuleFrame;
import ca.phon.util.Range;

import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class ResultTableUtil {

    public static void openEditorAtResult(TableDataSource table, int row) {
        // get project reference from parent window
        final CommonModuleFrame cmf = CommonModuleFrame.getCurrentFrame();
        if(cmf == null) return;

        final Project project = cmf.getExtension(Project.class);
        if(project == null) return;

        openEditorAtResult(project, table, row);
    }

    /**
     * This method will attempt to display a session editor with highlighted results
     * given a table which contains the appropriate column names and data.
     *
     * Required columns:
     * <ul>
     *     <li>Session</li>
     *     <li>Record/Record #</li>
     * </ul>
     *
     * Optional columns:
     * <ul>
     *     <li>Result</li>
     * </ul>
     *
     * @param table
     * @param row
     */
    public static void openEditorAtResult(Project project, TableDataSource table, int row) {
        int sessionColumn = -1;
        int recordColumn = -1;

        for(int i = 0; i < table.getColumnCount(); i++) {
            final String colName = table.getColumnTitle(i);

            if(colName.equalsIgnoreCase("session")) {
                sessionColumn = i;
            } else if(colName.equalsIgnoreCase("record")
                    || colName.equalsIgnoreCase("record #")) {
                recordColumn = i;
            }
        }

        if(sessionColumn == -1) return;
        if(recordColumn == -1) return;

        SessionPath sp = new SessionPath();
        if(sessionColumn >= 0) {
            String sessionTxt = table.getValueAt(row, sessionColumn).toString();
            if(sessionTxt == null || sessionTxt.length() == 0 || sessionTxt.indexOf('.') < 0) return;
            String[] sessionPath = sessionTxt.split("\\.");
            if(sessionPath.length != 2) return;
            sp.setCorpus(sessionPath[0]);
            sp.setSession(sessionPath[1]);
        }

        // get record index
        String recordTxt = table.getValueAt(row, recordColumn).toString();
        int recordNum = Integer.parseInt(recordTxt) - 1;
        if(recordNum < 0) return;

        List<Integer> groups = new ArrayList<>();
        List<String> tiers = new ArrayList<>();
        List<Range> ranges = new ArrayList<>();

        final int resultColumn = table.getColumnIndex("Result");
        if(resultColumn >= 0) {
            final Object resultVal = table.getValueAt(row, resultColumn);
            if(resultVal != null && resultVal instanceof Result) {
                final Result result = (Result)resultVal;

                // setup highlighting
                for(ResultValue rv:result) {
                    final Range range = new Range(rv.getRange().getFirst(), rv.getRange().getLast(), false);
                    groups.add(rv.getGroupIndex());
                    tiers.add(rv.getTierName());
                    ranges.add(range);
                }
            }
        }

        final PhonURI phonURI = new PhonURI(project.getLocation(), sp.getCorpus(), sp.getSession(), recordNum,
                groups, tiers, ranges);
        final PhonURISchemeHandler schemeHandler = new PhonURISchemeHandler();
        try {
            schemeHandler.openURI(phonURI.toURI());
        } catch (MalformedURLException | FileNotFoundException | PluginException | URISyntaxException e) {
            LogUtil.severe(e);
        }
    }

}
