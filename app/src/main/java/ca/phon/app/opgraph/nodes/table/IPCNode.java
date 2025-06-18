package ca.phon.app.opgraph.nodes.table;

import ca.phon.app.log.LogUtil;
import ca.phon.ipa.IPATranscript;
import ca.phon.ipa.ipc.IndexOfPhoneticComplexity;
import ca.phon.opgraph.InputField;
import ca.phon.opgraph.OpContext;
import ca.phon.opgraph.OpNodeInfo;
import ca.phon.opgraph.exceptions.ProcessingException;
import ca.phon.query.report.datasource.DefaultTableDataSource;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Creates a table of results for the Index of Phonetic Complexity (IPC)
 * of phonetic transcriptions.
 *
 * This node will produce a table that contains the following columns:
 *
 * 1. Session (if available)
 * 2. Speaker (if available)
 * 3. Record # (if available)
 * 4. Orthography (if available)
 * 5. IPA Target (or other provided tier name for target)
 * 6. IPA Actual (or other provided tier name for actual)
 * 7. Dt - Place (D) - dorsals for target
 * 8. Da - Place (D) - dorsals for actual
 * 9. Mt - Manner (M) - fricatives, affricates, and liquids for target
 * 10. Ma - Manner (M) - fricatives, affricates, and liquids for actual
 * 11. Vt - Vowels (V) - Rhotic vowels for target
 * 12. Va - Vowels (V) - Rhotic vowels for actual
 * 13. St - Word shape (S) - ends with a consonant for target
 * 14. Sa - Word shape (S) - ends with a consonant for actual
 * 15. Lt - Word length (L) - 3 or more syllables for target
 * 16. La - Word length (L) - 3 or more syllables for actual
 * 17. Pt - Place variegation (P) - singleton consonants that are place variegated for target
 * 18. Pa - Place variegation (P) - singleton consonants that are place variegated for actual
 * 19. Ct - Contiguous clusters (C) for target
 * 20. Ca - Contiguous clusters (C) for actual
 * 21. Tt - Cluster type (T) - Each cluster that is comprised of segments that vary in place (CCV) gets 1 point for being heterorganic for target
 * 22. Ta - Cluster type (T) - Each cluster that is comprised of segments that vary in place (CCV) gets 1 point for being heterorganic for actual
 * 23. IPCt - total score for target
 * 24. IPCa - total score for actual
 * 25. Δ - difference between target and actual IPC
 */
@OpNodeInfo(name="Index of Phonetic Complexity",
        category="IPA Table Analysis",
        description="Calculate Index of Phonetic Complexity (IPC) for phonetic transcriptions")
public class IPCNode extends TableOpNode {

    /**
     * Tier names from the input table, each tier name should have type IPATranscript
     * (default: IPA Target, IPA Actual)
     */
    private InputField tierNamesField = new InputField("tierNames", "Comma-separated list of tier names",
            true, true, String.class);

    public IPCNode() {
        super();

        putField(tierNamesField);
    }

    /**
     * Look for provided tier name or "<tier name> (Word)"
     *
     * @param tableDataSource
     * @param tierName
     * @return
     */
    private int findTierColumn(DefaultTableDataSource tableDataSource, String tierName) {
        int idx = tableDataSource.getColumnIndex(tierName);
        if(idx < 0) {
            // try with " (Word)" suffix
            String tierNameWithSuffix = tierName + " (Word)";
            idx = tableDataSource.getColumnIndex(tierNameWithSuffix);
        }
        return idx;
    }

    private void setupOutputTable(DefaultTableDataSource inputTable, DefaultTableDataSource outputTable, String[] tierNames) {
        final boolean hasSession = inputTable.getColumnIndex("Session") >= 0;
        final boolean hasSpeaker = inputTable.getColumnIndex("Speaker") >= 0;
        final boolean hasRecordNumber = inputTable.getColumnIndex("Record #") >= 0;
        final boolean hasOrthography = findTierColumn(inputTable, "Orthography") >= 0;

        final List<String> outputColumns = new ArrayList<>();
        if(hasSession) {
            outputColumns.add("Session");
        }
        if(hasSpeaker) {
            outputColumns.add("Speaker");
        }
        if(hasRecordNumber) {
            outputColumns.add("Record #");
        }
        if(hasOrthography) {
            outputColumns.add("Orthography");
        }

        outputColumns.add(tierNames[0].trim());
        if(tierNames.length > 1) {
            outputColumns.add(tierNames[1].trim());
        }

        outputColumns.add("Dt");
        if(tierNames.length > 0) {
            outputColumns.add("Da");
        }

        outputColumns.add("Mt");
        if(tierNames.length > 1) {
            outputColumns.add("Ma");
        }

        outputColumns.add("Vt");
        if(tierNames.length > 1) {
            outputColumns.add("Va");
        }

        outputColumns.add("St");
        if(tierNames.length > 1) {
            outputColumns.add("Sa");
        }

        outputColumns.add("Lt");
        if(tierNames.length > 1) {
            outputColumns.add("La");
        }

        outputColumns.add("Pt");
        if(tierNames.length > 1) {
            outputColumns.add("Pa");
        }

        outputColumns.add("Ct");
        if(tierNames.length > 1) {
            outputColumns.add("Ca");
        }

        outputColumns.add("Tt");
        if(tierNames.length > 1) {
            outputColumns.add("Ta");
        }

        outputColumns.add("IPCt");
        if(tierNames.length > 1) {
            outputColumns.add("IPCa");
        }

        if(tierNames.length > 1) {
            outputColumns.add("Δ");
        }

        for(int i = 0; i < outputColumns.size(); i++) {
            outputTable.setColumnTitle(i, outputColumns.get(i));
        }
    }

    private IPATranscript getTranscriptForTier(DefaultTableDataSource inputTable, int rowIndex, int tierColumn) {
        final Object tierObj = inputTable.getRow(rowIndex)[tierColumn];
        if(tierObj instanceof IPATranscript ipa) {
            return ipa;
        } else if(tierObj instanceof String transcript) {
            try {
                return IPATranscript.parseIPATranscript(transcript);
            } catch (ParseException e) {
                LogUtil.warning("IPCNode: Unable to parse transcript '" + transcript + "'", e);
            }
        } else {
            LogUtil.warning("IPCNode: Unsupported type for tier, expected IPATranscript or String, found " + tierObj.getClass().getName());
        }
        return null;
    }

    private void appendIPCRow(DefaultTableDataSource inputTable, DefaultTableDataSource outputTable,
            int rowIndex, String[] tierNames,
            IPATranscript targetTranscript, Optional<IPATranscript> actualTranscript,
            IndexOfPhoneticComplexity ipct, Optional<IndexOfPhoneticComplexity> ipca) {
        final boolean hasSession = inputTable.getColumnIndex("Session") >= 0;
        final boolean hasSpeaker = inputTable.getColumnIndex("Speaker") >= 0;
        final boolean hasRecordNumber = inputTable.getColumnIndex("Record #") >= 0;
        final boolean hasOrthography = findTierColumn(inputTable, "Orthography") >= 0;

        final List<Object> row = new ArrayList<>();
        if(hasSession) {
            row.add(inputTable.getRow(rowIndex)[inputTable.getColumnIndex("Session")]);
        }
        if(hasSpeaker) {
            row.add(inputTable.getRow(rowIndex)[inputTable.getColumnIndex("Speaker")]);
        }
        if(hasRecordNumber) {
            row.add(inputTable.getRow(rowIndex)[inputTable.getColumnIndex("Record #")]);
        }
        if(hasOrthography) {
            row.add(inputTable.getRow(rowIndex)[findTierColumn(inputTable, "Orthography")]);
        }

        row.add(targetTranscript);
        if(actualTranscript.isPresent()) {
            row.add(actualTranscript.get());
        }

        row.add(ipct.D());
        if(actualTranscript.isPresent()) {
            row.add(ipct.D());
        }

        row.add(ipct.M());
        if(actualTranscript.isPresent()) {
            row.add(ipct.M());
        }

        row.add(ipct.V());
        if(actualTranscript.isPresent()) {
            row.add(ipct.V());
        }

        row.add(ipct.S());
        if(actualTranscript.isPresent()) {
            row.add(ipct.S());
        }

        row.add(ipct.L());
        if(actualTranscript.isPresent()) {
            row.add(ipct.L());
        }

        row.add(ipct.P());
        if(actualTranscript.isPresent()) {
            row.add(ipct.P());
        }

        row.add(ipct.C());
        if(actualTranscript.isPresent()) {
            row.add(ipct.C());
        }

        row.add(ipct.T());
        if(actualTranscript.isPresent()) {
            row.add(ipct.T());
        }

        row.add(ipct.totalScore());
        if(actualTranscript.isPresent()) {
            row.add(ipca.get().totalScore());
        }

        if(actualTranscript.isPresent()) {
            row.add(ipct.totalScore() - ipca.get().totalScore());
        }

        outputTable.addRow(row.toArray());
    }

    @Override
    public void operate(OpContext opContext) throws ProcessingException {
        final DefaultTableDataSource inputTable =  (DefaultTableDataSource)opContext.get(super.tableInput);
        final String tierNamesStr = opContext.get(tierNamesField) != null ?
            opContext.get(tierNamesField).toString() : "IPA Target, IPA Actual";
        final String[] tierNames = tierNamesStr.split(",");
        if(tierNames.length < 1) {
            LogUtil.warning("IPCNode: At least one tier name is required for IPC calculation");
            return;
        }
        if(tierNames.length > 2) {
            LogUtil.warning("IPCNode: Only two tier names are supported for IPC calculation, found " + tierNames.length);
            return;
        }
        final int[] tierColumns = new int[tierNames.length];

        final DefaultTableDataSource outputTable = new DefaultTableDataSource();
        setupOutputTable(inputTable, outputTable, tierNames);
        setTableOutput(opContext, outputTable);

        for(int i = 0; i < tierNames.length; i++) {
            String tierName = tierNames[i].trim();
            int colIdx = findTierColumn(inputTable, tierName);
            if(colIdx < 0) {
                LogUtil.warning("IPCNode: Unable to find tier column for '" + tierName + "'");
                return;
            }
            tierColumns[i] = colIdx;
        }

        // for each row in the input table, grab the values for the tiers and calculate IPC for the output row
        for(int i = 0; i < inputTable.getRowCount(); i++) {
            IPATranscript targetTranscript = getTranscriptForTier(inputTable, i, tierColumns[0]);
            if(targetTranscript == null) {
                targetTranscript = new IPATranscript();
            }
            IPATranscript actualTranscript = null;
            if(tierNames.length > 1) {
                actualTranscript = getTranscriptForTier(inputTable, i, tierColumns[1]);
                if(actualTranscript == null) {
                    actualTranscript = new IPATranscript();
                }
            }

            if(tierNames.length == 1) {

            }
            final IndexOfPhoneticComplexity ipct = IndexOfPhoneticComplexity.FromTranscript(targetTranscript);
            Optional<IndexOfPhoneticComplexity> ipca = Optional.empty();
            if(tierNames.length > 1) {
                ipca = Optional.of(IndexOfPhoneticComplexity.FromTranscript(actualTranscript));
            }
            appendIPCRow(inputTable, outputTable, i, tierNames, targetTranscript, Optional.of(actualTranscript), ipct, ipca);
        }
    }
}
