package ca.phon.app.session;

import au.com.bytecode.opencsv.CSVWriter;
import ca.phon.ipa.IPATranscript;
import ca.phon.orthography.Orthography;
import ca.phon.session.*;
import ca.phon.session.Record;
import jxl.demo.CSV;
import org.apache.logging.log4j.LogManager;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class RecordsTransferable implements Transferable {

    private final static org.apache.logging.log4j.Logger LOGGER = LogManager.getLogger(RecordsTransferable.class.getName());

    public final static DataFlavor FLAVOR = new DataFlavor(RecordsTransferable.class, "RecordsTransferable");

    private final Session session;

    private final int[] records;

    public RecordsTransferable(Session session, int[] records) {
        super();
        this.session = session;
        this.records = records;
    }

    public Session getSession() {
        return this.session;
    }

    public int[] getRecords() {
        return this.records;
    }

    @Override
    public DataFlavor[] getTransferDataFlavors() {
        return new DataFlavor[] { FLAVOR, DataFlavor.stringFlavor };
    }

    @Override
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return (flavor == FLAVOR || flavor == DataFlavor.stringFlavor);
    }

    @Override
    public Object getTransferData(DataFlavor flavor)
            throws UnsupportedFlavorException, IOException {
        if(flavor == FLAVOR) {
            return this;
        } else if (flavor == DataFlavor.stringFlavor) {
            // record to CSV
            return recordsToCSV();
        } else {
            throw new UnsupportedFlavorException(flavor);
        }
    }

    private String recordsToCSV() {
        final ByteArrayOutputStream bout = new ByteArrayOutputStream();
        try {
            final CSVWriter writer = new CSVWriter(new OutputStreamWriter(bout, "UTF-8"), ',', '\"');

            int numColumns = writeHeader(writer, session);
            for(int record:getRecords()) {
               writeRecord(writer, session, record, numColumns);
            }

            writer.flush();
            writer.close();
        } catch (UnsupportedEncodingException e) {
            LOGGER.error( e.getLocalizedMessage(), e);
        } catch (IOException e) {
            LOGGER.error( e.getLocalizedMessage(), e);
        }
        return new String(bout.toByteArray(), Charset.forName("UTF-8"));
    }

    private int writeHeader(CSVWriter writer, Session session) throws IOException {
        List<String> columnNames = new ArrayList<>();
        columnNames.add("Speaker");
        columnNames.add("Orthography");
        columnNames.add("IPA Target");
        columnNames.add("IPA Actual");
        columnNames.add("Segment");
        columnNames.add("Notes");

        for(TierDescription td:session.getUserTiers()) {
            columnNames.add(td.getName());
        }
        writer.writeNext(columnNames.toArray(new String[columnNames.size()]));
        return columnNames.size();
    }

    private void writeRecord(CSVWriter writer, Session session, int recordIndex, int numColumns) throws
             IOException {
        Record record = session.getRecord(recordIndex);
        String[] rowData = new String[numColumns];
        int colIdx = 0;

        rowData[colIdx++] = (record.getSpeaker() != null ? record.getSpeaker().toString() : "");
        rowData[colIdx++] = record.getOrthography().toString();
        rowData[colIdx++] = record.getIPATarget().toString();
        rowData[colIdx++] = record.getIPAActual().toString();
        rowData[colIdx++] = record.getSegment().getGroup(0).toString();
        rowData[colIdx++] = record.getNotes().getGroup(0).toString();

        for(TierDescription userTier:session.getUserTiers()) {
            Tier<?> tier = record.getTier(userTier.getName());
            if(userTier.isGrouped()) {
                rowData[colIdx++] = (tier != null ? tier.toString() : "");
            } else {
                rowData[colIdx++] = (tier != null && tier.numberOfGroups() > 0 ? tier.getGroup(0).toString() : "");
            }
        }

        writer.writeNext(rowData);
        writer.flush();
    }

}
