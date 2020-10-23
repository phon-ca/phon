/*
 * Copyright (C) 2012-2018 Gregory Hedlund & Yvan Rose
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
package ca.phon.app.session;

import java.awt.datatransfer.*;
import java.io.*;
import java.nio.charset.*;
import java.util.*;

import org.apache.logging.log4j.*;

import au.com.bytecode.opencsv.*;
import ca.phon.ipa.*;
import ca.phon.orthography.*;
import ca.phon.session.*;
import ca.phon.session.Record;

/**
 * Clipboard transferable implementation for {@link Record}s.
 */
public class RecordTransferable implements Transferable {
	
	private final static org.apache.logging.log4j.Logger LOGGER = LogManager.getLogger(RecordTransferable.class.getName());
	
	public final static DataFlavor FLAVOR = new DataFlavor(RecordTransferable.class, "RecordTransferable");
	
	private final Record record;
	
	public RecordTransferable(Record r) {
		super();
		this.record = r;
	}

	public Record getRecord() {
		return this.record;
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
			return recordToCSV();
		} else {
			throw new UnsupportedFlavorException(flavor);
		}
	}
	
	private String recordToCSV() {
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
