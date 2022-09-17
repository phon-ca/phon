package ca.phon.app.session;

import au.com.bytecode.opencsv.CSVWriter;
import ca.phon.app.log.LogUtil;
import ca.phon.session.Record;
import ca.phon.session.*;

import java.awt.datatransfer.*;
import java.io.*;
import java.nio.charset.Charset;
import java.util.*;

public class RecordsTransferable implements Transferable {

    public final static DataFlavor FLAVOR = new DataFlavor(RecordsTransferable.class, "RecordsTransferable");

    /**
     * Cloned session information
     */
    private final Session session;

    /**
     * Cloned records
     */
    private final List<Record> clonedRecords;

    public RecordsTransferable(Session session, int[] records) {
        super();

        // setup cloned session
        SessionFactory factory = SessionFactory.newFactory();
        this.session = factory.cloneSession(session);
        List<Participant> speakerList = new ArrayList<>();
        for(Participant p:session.getParticipants()) {
            Participant clonedParticipant = factory.cloneParticipant(p);
            speakerList.add(p);
            this.session.addParticipant(clonedParticipant);
        }
        List<Participant> clonedSpeakerList = new ArrayList<>();
        for(Participant clonedParticipant:this.session.getParticipants())
            clonedSpeakerList.add(clonedParticipant);

        for(TierDescription td:session.getUserTiers()) {
            this.session.addUserTier(factory.createTierDescription(td.getName(), td.isGrouped(), td.getDeclaredType()));
        }

        final List<TierViewItem> tv = new ArrayList<>(session.getTierView());
        this.session.setTierView(tv);

        // setup cloned records
        this.clonedRecords = new ArrayList<>();
        for(int recordIndex:records) {
            Record record = session.getRecord(recordIndex);
            Record clonedRecord = factory.cloneRecord(record);

            if(record.getSpeaker() != Participant.UNKNOWN)
                clonedRecord.setSpeaker(this.session.getParticipant(speakerList.indexOf(record.getSpeaker())));
            clonedSpeakerList.remove(clonedRecord.getSpeaker());

            this.clonedRecords.add(clonedRecord);
        }

        for(Participant toRemove:clonedSpeakerList)
            this.session.removeParticipant(toRemove);
    }

    public Session getSession() {
        return this.session;
    }

    public List<Record> getRecords() {
        return this.clonedRecords;
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
            for(Record record:getRecords()) {
               writeRecord(writer, session, record, numColumns);
            }

            writer.flush();
            writer.close();
        } catch (IOException e) {
            LogUtil.severe( e.getLocalizedMessage(), e);
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

    private void writeRecord(CSVWriter writer, Session session, Record record, int numColumns) throws
             IOException {
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
