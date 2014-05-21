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
import java.util.Calendar;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import ca.phon.csv2phon.io.FileType;
import ca.phon.csv2phon.io.ImportDescriptionType;
import ca.phon.csv2phon.io.ParticipantType;
import ca.phon.csv2phon.wizard.CSVImportParticipant;
import ca.phon.project.Project;
import ca.phon.session.Participant;
import ca.phon.session.Session;
import ca.phon.util.OSInfo;
import au.com.bytecode.opencsv.CSVReader;

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
		
		final Session t = project.createSessionFromTemplate(corpus, session);
		if(t.getRecordCount() > 0) t.removeRecord(0);
		
		// add participants
//		int participantIdx = 0;
		for(ParticipantType pt:importDescription.getParticipant()) {
			Participant part = new CSVImportParticipant(pt);
			Participant newPart = t.newParticipant();
			TranscriptUtils.copyParticipant(part, newPart);
//			newPart.setId("p" + (participantIdx++));
		}
		
		if(fileInfo.getMedia() != null) {
			t.setMediaLocation(fileInfo.getMedia());
		}
		if(fileInfo.getDate() != null) {
			// convert date to calendar
			PhonDateFormat pdf = new PhonDateFormat(PhonDateFormat.YEAR_LONG);
			try {
				Calendar sessionDate = (Calendar)pdf.parseObject(fileInfo.getDate());
				t.setDate(sessionDate);
			} catch (ParseException e) {
				PhonLogger.warning(e.toString());
			}
			
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
					// create a new tier description
					IDepTierDesc tierDesc = t.newDependentTier();
//					tierDesc.setTierFont("default");
					tierDesc.setTierName(tierName);
					tierDesc.setIsGrouped(colmap.isGrouped());
				}
			}
		}
		
		String[] currentRow = null;
		while((currentRow = reader.readNext()) != null) {
			
			// add a new record to the transcript
			IUtterance utt = t.newUtterance();
			
			for(int colIdx = 0; colIdx < colLine.length; colIdx++) {
				String csvcol = colLine[colIdx];
				String rowval = currentRow[colIdx];
				
				ColumnMapType colmap = getColumnMap(csvcol);
				if(colmap == null) {
					// print warning and continue
					PhonLogger.warning("No column map for csv column '" + csvcol + "'");
					continue;
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
					IParticipant speaker = null;
					for(IParticipant p:t.getParticipants()) {
						if(p.getName().equals(rowval)) {
							speaker = p;
							break;
						}
					}

					// if not found in the transcript, find the
					// participant info in the import description
					// add add the participant
					if(speaker == null) {
//						ParticipantType pt = getParticipant(rowval);
//						if(pt == null) {
//							pt = (new ObjectFactory()).createParticipantType();
//							pt.setName(rowval);
//						}

						speaker = t.newParticipant();

						// copy participant information
						speaker.setName(rowval);
						
					}

					utt.setSpeaker(speaker);
				} else {
					try {
						
						// use transcript converter if necessary
						if(colmap.getFilter() != null && colmap.getFilter().length() > 0) {
							
							if(SystemTierType.IPATarget.getTierName().equalsIgnoreCase(phontier)
									|| SystemTierType.IPAActual.getTierName().equalsIgnoreCase(phontier)) {
								ArrayList<String> vals = StringUtils.extractedBracketedStrings(rowval);
								TranscriptConverter tc = TranscriptConverter.getInstanceOf(colmap.getFilter());
								if(tc != null) {
									String newval = "";
									for(String v:vals) {
										newval += (newval.length() > 0 ? " " : "") + "[";
										newval += tc.convert(v);
										newval += "]";
									}
									
//									System.out.println(rowval);
									rowval = newval;
								}
							}
						}
						
						// make sure grouped data is enclosed in '[]'
						if(!SystemTierType.isSystemTier(phontier) &&
								colmap.isGrouped()) {
							if(rowval.indexOf('[') < 0 && rowval.indexOf(']') < 0) {
								rowval = '[' + rowval + ']';
							}
						}
						
						utt.setTierString(phontier, rowval);
					} catch (ParserException e) {
						PhonLogger.warning(e.toString());
					}
				}
			} // end for(colIdx)
			
			// do syllabification + alignment if necessary
			ColumnMapType targetMapping = getPhonColumnMap(SystemTierType.IPATarget.getTierName());
			ColumnMapType actualMapping = getPhonColumnMap(SystemTierType.IPAActual.getTierName());
			if(targetMapping != null && actualMapping != null) {
				
				Syllabifier targetSyllabifier = null;
				Syllabifier actualSyllabifier = null;
				// check for syllabifiers
				if(targetMapping.getSyllabifier() != null 
						&& Syllabifier.getAvailableSyllabifiers().contains(targetMapping.getSyllabifier())) {
					targetSyllabifier = Syllabifier.getInstance(targetMapping.getSyllabifier());
				}
				
				if(actualMapping.getSyllabifier() != null
						&& Syllabifier.getAvailableSyllabifiers().contains(actualMapping.getSyllabifier())) {
					actualSyllabifier = Syllabifier.getInstance(actualMapping.getSyllabifier());
				}
				
				for(IWord w:utt.getWords()) {
					IPhoneticRep targetRep = w.getPhoneticRepresentation(Form.Target);
					if(targetSyllabifier != null) {
						List<Phone> targetPhones = targetRep.getPhones();
						targetSyllabifier.syllabify(targetPhones);
						targetRep.setPhones(targetPhones);
					}
					
					IPhoneticRep actualRep = w.getPhoneticRepresentation(Form.Actual);
					if(actualSyllabifier != null) {
						List<Phone> actualPhones = actualRep.getPhones();
						actualSyllabifier.syllabify(actualPhones);
						actualRep.setPhones(actualPhones);
					}
					
					PhoneMap pm = Aligner.getPhoneAlignment(w);
					if(pm != null) {
						w.setPhoneAlignment(pm);
					}
				}
				
			}
		} // end while(currentRow)
		
		// save transcript
		int writeLock = project.getTranscriptWriteLock(corpus, session);
		project.saveTranscript(t, writeLock);
		project.releaseTranscriptWriteLock(corpus, session, writeLock);
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
