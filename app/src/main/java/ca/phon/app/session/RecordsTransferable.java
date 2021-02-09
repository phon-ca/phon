package ca.phon.app.session;

import au.com.bytecode.opencsv.CSVWriter;
import ca.phon.ipa.IPATranscript;
import ca.phon.orthography.Orthography;
import ca.phon.session.*;
import ca.phon.session.Record;
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

            for(int record:getRecords()) {
                String recordTxt = RecordTransferable.recordToCSV(session.getRecord(record));
                bout.write(recordTxt.getBytes());
                bout.write('\n');
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

}
