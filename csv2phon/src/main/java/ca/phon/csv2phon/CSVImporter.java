/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2008 The Phon Project, Memorial University <http://phon.ling.mun.ca>
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ca.phon.csv2phon;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import au.com.bytecode.opencsv.CSVReader;
import ca.phon.csv2phon.io.ColumnMapType;
import ca.phon.csv2phon.io.FileType;
import ca.phon.csv2phon.io.ImportDescriptionType;
import ca.phon.csv2phon.io.ParticipantType;
import ca.phon.fontconverter.TranscriptConverter;
import ca.phon.ipa.IPATranscript;
import ca.phon.ipa.IPATranscriptBuilder;
import ca.phon.ipa.alignment.PhoneAligner;
import ca.phon.ipa.alignment.PhoneMap;
import ca.phon.orthography.Orthography;
import ca.phon.project.Project;
import ca.phon.session.Group;
import ca.phon.session.MediaSegment;
import ca.phon.session.MediaSegmentFormatter;
import ca.phon.session.MediaUnit;
import ca.phon.session.Participant;
import ca.phon.session.Record;
import ca.phon.session.Session;
import ca.phon.session.SessionFactory;
import ca.phon.session.SystemTierType;
import ca.phon.session.Tier;
import ca.phon.session.TierDescription;
import ca.phon.syllabifier.Syllabifier;
import ca.phon.syllabifier.SyllabifierLibrary;
import ca.phon.util.Language;
import ca.phon.util.OSInfo;

/**
 * Reads in the XML description of a CSV import and performs the import.
 * 
 *
 */
public class CSVImporter {
	
	private final static Logger LOGGER = Logger
			.getLogger(CSVImporter.class.getName());
	
	/** The import description */
	private ImportDescriptionType importDescription;
	
	/** The project we are importing into */
	private Project project;
	
	/** Directory where files are located */
	private String base;
	
	private String fileEncoding = "UTF-8";
	
	/**
	 * Constructor.
	 */
	public CSVImporter(String baseDir, ImportDescriptionType importDesc, Project project) {
		super();
		
		this.importDescription = importDesc;
		this.project = project;
		this.base = baseDir;
	}
	
	public void setFileEncoding(String charset) {
		this.fileEncoding = charset;
	}
	
	public String getFileEncoding() {
		return this.fileEncoding;
	}
	
	/**
	 * Begin import of specified files.
	 */
	public void performImport() {
		// print some info messages
		LOGGER.info("Importing files from directory '" + base + '"');
		
		for(FileType ft:importDescription.getFile()) {
			if(ft.isImport()) {
				try {
					LOGGER.info("Importing file '.../" + ft.getLocation() + "'");
					importFile(ft);
				} catch (IOException e) {
					LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
				}
			}
		}
		
		LOGGER.info("Import finished.");
	}
	
	private void importFile(FileType fileInfo) 
		throws IOException {
		// first try relative path from base
//		String base = importDescription.getBase();
		String location = fileInfo.getLocation();
		
		// check if location is an absolute path
		boolean absolute = false;
		if(OSInfo.isWindows()) {
			if(location.matches("[A-Z]:\\\\.*"))
				absolute = true;
		} else {
			if(location.startsWith("/"))
				absolute = true;
		}
		
		File csvFile = null;
		if(absolute)
			csvFile = new File(location);
		else
			csvFile = new File(base, location);
		
		if(!csvFile.exists()) {
			// throw an exception
			throw new FileNotFoundException("'" + 
					csvFile.getAbsolutePath() + "' not found, check the 'base' attribute of the csvimport element.");
		}
		
		final InputStreamReader csvInputReader = new InputStreamReader(new FileInputStream(csvFile), fileEncoding);
		// read in csv file
		final CSVReader reader = new CSVReader(csvInputReader, ',', '\"');
		
		// create a new transcript in the project 
		// with the specified corpus and session name
		final String corpus = importDescription.getCorpus();
		final String session = fileInfo.getSession();
		if(!project.getCorpora().contains(corpus)) {
			LOGGER.info("Creating corpus '" + corpus + "'");
			project.addCorpus(corpus, "");
		}
		
		final SessionFactory factory = SessionFactory.newFactory();
		
		final Session t = project.createSessionFromTemplate(corpus, session);
		if(t.getRecordCount() > 0) t.removeRecord(0);
		
		if(fileInfo.getDate() != null) {
			final DateTimeFormatter dateFormatter = 
					DateTimeFormat.forPattern("yyyy-MM-dd");
			DateTime sessionDate = dateFormatter.parseDateTime(fileInfo.getDate());
			t.setDate(sessionDate);
		}
		
		// add participants
		for(ParticipantType pt:importDescription.getParticipant()) {
			Participant newPart = CSVParticipantUtil.copyXmlParticipant(factory, pt, t.getDate());
			t.addParticipant(newPart);
		}
		
		if(fileInfo.getMedia() != null) {
			t.setMediaLocation(fileInfo.getMedia());
		}
		// set media file and date
		String[] colLine = reader.readNext();
		
		// create deptier descriptions as necessary
		for(String columnName:colLine) {
			ColumnMapType colmap = getColumnMap(columnName);
			if(colmap != null) {
				String tierName = colmap.getPhontier();
				if(tierName.equalsIgnoreCase("Don't import")) continue;
				
				if(!SystemTierType.isSystemTier(tierName) && !tierName.equalsIgnoreCase("Speaker:Name")) {
					final TierDescription tierDesc =
							factory.createTierDescription(tierName, colmap.isGrouped(), String.class);
					t.addUserTier(tierDesc);
				}
			}
		}
		
		String[] currentRow = null;
		while((currentRow = reader.readNext()) != null) {
			
			// add a new record to the transcript
			Record utt = factory.createRecord();
			
			for(int colIdx = 0; colIdx < colLine.length; colIdx++) {
				String csvcol = colLine[colIdx];
				String rowval = currentRow[colIdx];
				
				ColumnMapType colmap = getColumnMap(csvcol);
				if(colmap == null) {
					// print warning and continue
					LOGGER.warning("No column map for csv column '" + csvcol + "'");
					continue;
				}

				// convert if necessary
				TranscriptConverter tc = null;
				if(colmap.getFilter() != null && colmap.getFilter().length() > 0) {
					tc = TranscriptConverter.getInstanceOf(colmap.getFilter());
					if(tc == null) {
						LOGGER.warning("Could not find transcript converter '" + colmap.getFilter() + "'");
					}
				}
				
				String phontier = colmap.getPhontier();
				if(phontier.equalsIgnoreCase("Don't Import")) {
					continue;
				}

				// do data pre-formatting if required
				if(colmap.getScript() != null) {
					// TODO: create a new javascript context and run the given script
				}

				// handle participant tier
				if(phontier.equals("Speaker:Name")) {

					// look for the participant in the transcript
					Participant speaker = null;
					for(Participant p:t.getParticipants()) {
						if(p.getName().equals(rowval)) {
							speaker = p;
							break;
						}
					}

					// if not found in the transcript, find the
					// participant info in the import description
					// add add the participant
					if(speaker == null) {
						speaker = factory.createParticipant();
						speaker.setName(rowval);
					}

					utt.setSpeaker(speaker);
				} else {
					// convert rowval into a list of group values
					List<String> rowVals = new ArrayList<String>();
					if(colmap.isGrouped() && rowval.startsWith("[") && rowval.endsWith("]")) {
						String[] splitRow = rowval.split("\\[");
						for(int i = 1; i < splitRow.length; i++) {
							String splitVal = splitRow[i];
							splitVal = splitVal.replaceAll("\\]", "");
							rowVals.add(splitVal);
						}
					} else {
						rowVals.add(rowval);
					}
					
					final SystemTierType systemTier = SystemTierType.tierFromString(phontier);
					if(systemTier != null) {
						if(systemTier == SystemTierType.Orthography) {
							final Tier<Orthography> orthoTier = utt.getOrthography();
							for(String grpVal:rowVals) {
								final Orthography ortho = Orthography.parseOrthography(grpVal);
								orthoTier.addGroup(ortho);
							}
						} else if(systemTier == SystemTierType.IPATarget 
								|| systemTier == SystemTierType.IPAActual) {
							final Tier<IPATranscript> ipaTier = 
									(systemTier == SystemTierType.IPATarget ? utt.getIPATarget() : utt.getIPAActual());
							for(String grpVal:rowVals) {
								if(tc != null) {
									grpVal = tc.convert(grpVal);
								}
								final IPATranscript ipa = (new IPATranscriptBuilder()).append(grpVal).toIPATranscript();
								ipaTier.addGroup(ipa);
							}
						} else if(systemTier == SystemTierType.Notes) {
							utt.getNotes().addGroup(rowval);
						} else if(systemTier == SystemTierType.Segment) {
							final MediaSegmentFormatter segmentFormatter = new MediaSegmentFormatter();
							MediaSegment segment = factory.createMediaSegment();
							segment.setStartValue(0.0f);
							segment.setEndValue(0.0f);
							segment.setUnitType(MediaUnit.Millisecond);
							try {
								segment = segmentFormatter.parse(rowval);
							} catch (ParseException e) {
								LOGGER.log(Level.SEVERE,
										e.getLocalizedMessage(), e);
							}
							utt.getSegment().addGroup(segment);
						}
					} else {
						Tier<String> tier = utt.getTier(phontier, String.class);
						if(tier == null) {
							tier = factory.createTier(phontier, String.class, colmap.isGrouped());
							utt.putTier(tier);
						}
						
						for(String grpVal:rowVals) {
							tier.addGroup(grpVal);
						}
					}
				}
			} // end for(colIdx)
			
			// do syllabification + alignment if necessary
			ColumnMapType targetMapping = getPhonColumnMap(SystemTierType.IPATarget.getName());
			ColumnMapType actualMapping = getPhonColumnMap(SystemTierType.IPAActual.getName());
			if(targetMapping != null && actualMapping != null) {
				
				final SyllabifierLibrary library = SyllabifierLibrary.getInstance();
				
				final Language targetLang = Language.parseLanguage(targetMapping.getSyllabifier());
				final Language actualLang = Language.parseLanguage(actualMapping.getSyllabifier());
				
				final PhoneAligner aligner = new PhoneAligner();
				
				Syllabifier targetSyllabifier = library.getSyllabifierForLanguage(targetLang);
				Syllabifier actualSyllabifier = library.getSyllabifierForLanguage(actualLang);
				
				for(int i = 0; i < utt.numberOfGroups(); i++) {
					final Group grp = utt.getGroup(i);
					final IPATranscript targetRep = grp.getIPATarget();
					if(targetSyllabifier != null) {
						targetSyllabifier.syllabify(targetRep.toList());
					}
					
					final IPATranscript actualRep = grp.getIPAActual();
					if(actualSyllabifier != null) {
						actualSyllabifier.syllabify(actualRep.toList());
					}
					
					PhoneMap pm = aligner.calculatePhoneMap(targetRep, actualRep);
					grp.setPhoneAlignment(pm);
				}
				
			}
		} // end while(currentRow)
		
		// save transcript
		final UUID writeLock = project.getSessionWriteLock(t);
		if(writeLock != null) {
			project.saveSession(t, writeLock);
			project.releaseSessionWriteLock(t, writeLock);
		}
	}
	
	/**
	 * Returns the column mapping for the given csvcolumn.
	 */
	private ColumnMapType getColumnMap(String csvcol) {
		ColumnMapType retVal = null;
		
		for(ColumnMapType cmt:importDescription.getColumnmap()) {
			if(cmt.getCsvcolumn().equals(csvcol)) {
				retVal = cmt;
				break;
			}
		}
		
		return retVal;
	}
	
	/**
	 * Returns the column mapping for the given phon column.
	 */
	private ColumnMapType getPhonColumnMap(String phoncol) {
		ColumnMapType retVal = null;
		
		for(ColumnMapType cmt:importDescription.getColumnmap()) {
			if(cmt.getPhontier().equals(phoncol)) {
				retVal = cmt;
				break;
			}
		}
		
		return retVal;
	}

	/**
	 * Returns the participant with the given name
	*/
	private ParticipantType getParticipant(String partName) {
		ParticipantType retVal = null;

		for(ParticipantType part:importDescription.getParticipant()) {
			if(part.getName().equals(partName)) {
				retVal = part;
				break;
			}
		}

		return retVal;
	}

}
