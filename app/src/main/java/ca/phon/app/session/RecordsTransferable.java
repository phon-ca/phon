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
                String recordTxt = recordToCSV(session.getRecord(record));
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

    private String recordToCSV(Record record) {
        final ByteArrayOutputStream bout = new ByteArrayOutputStream();
        try {
            final CSVWriter writer = new CSVWriter(new OutputStreamWriter(bout, "UTF-8"), ',', '\"');

            final String[] uuidLine = {"UUID", record.getUuid().toString()};
            writer.writeNext(uuidLine);

            final String[] speakerLine = {"Speaker",
                    (record.getSpeaker() != null ? record.getSpeaker().getName() : "")};
            writer.writeNext(speakerLine);

            final List<String> row = new ArrayList<String>();
            final Tier<Orthography> orthography = record.getOrthography();
            row.add(orthography.getName());
            for(Orthography ortho:orthography) row.add(ortho.toString());
            writer.writeNext(row.toArray(new String[0]));

            row.clear();
            final Tier<IPATranscript> ipaTarget = record.getIPATarget();
            row.add(ipaTarget.getName());
            for(IPATranscript t:ipaTarget) row.add(t.toString());
            writer.writeNext(row.toArray(new String[0]));

            row.clear();
            final Tier<IPATranscript> ipaActual = record.getIPAActual();
            row.add(ipaActual.getName());
            for(IPATranscript t:ipaActual) row.add(t.toString());
            writer.writeNext(row.toArray(new String[0]));

            row.clear();
            final Tier<TierString> notes = record.getNotes();
            row.add(notes.getName());
            row.add(notes.getGroup(0).toString());
            writer.writeNext(row.toArray(new String[0]));

            row.clear();
            final Tier<MediaSegment> segment = record.getSegment();
            row.add(segment.getName());
            row.add(segment.getGroup(0).toString());
            writer.writeNext(row.toArray(new String[0]));

            for(String tierName:record.getExtraTierNames()) {
                row.clear();
                final Tier<String> tier = record.getTier(tierName, String.class);
                row.add(tier.getName());
                for(String v:tier) row.add(v);
                writer.writeNext(row.toArray(new String[0]));
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
