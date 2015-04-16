/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2015, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
 * Dept of Linguistics, Memorial University <https://phon.ca>
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
package ca.phon.query.report.csv;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.JAXBElement;

import au.com.bytecode.opencsv.CSVWriter;
import ca.phon.project.Project;
import ca.phon.query.db.Query;
import ca.phon.query.db.ResultSet;
import ca.phon.query.report.ReportBuilder;
import ca.phon.query.report.ReportBuilderException;
import ca.phon.query.report.datasource.InventoryDataSource;
import ca.phon.query.report.datasource.ParamDataSource;
import ca.phon.query.report.datasource.ResultListingDataSource;
import ca.phon.query.report.datasource.SummaryDataSource;
import ca.phon.query.report.io.AggregrateInventory;
import ca.phon.query.report.io.CommentSection;
import ca.phon.query.report.io.Group;
import ca.phon.query.report.io.InventorySection;
import ca.phon.query.report.io.ParamSection;
import ca.phon.query.report.io.ReportDesign;
import ca.phon.query.report.io.ResultListing;
import ca.phon.query.report.io.Section;
import ca.phon.query.report.io.SummarySection;
import ca.phon.session.AgeFormatter;
import ca.phon.session.DateFormatter;
import ca.phon.session.Participant;
import ca.phon.session.Session;
import ca.phon.session.Sex;
import ca.phon.util.OSInfo;

/**
 * CSV report builder implementation.
 *
 */
public class CSVReportBuilder extends ReportBuilder {
	
	private final static Logger LOGGER = Logger.getLogger(CSVReportBuilder.class.getName());
	
	/**
	 * Property for indenting content at sections (default:false)
	 * 
	 */
	public final static String INDENT_CONTENT = "indent_content";
	
	/**
	 * Property for printing section names (default:true)
	 */
	public final static String PRINT_SECTION_NAMES = "print_section_names";

	@Override
	public String[] getPropertyNames() {
		return new String[] { PRINT_SECTION_NAMES, INDENT_CONTENT };
	}

	@Override
	public Class<?> getPropertyClass(String propName) {
		if(INDENT_CONTENT.equals(propName) || PRINT_SECTION_NAMES.equals(propName)) {
			return Boolean.class;
		} else {
			return super.getPropertyClass(propName);
		}
	}

	@Override
	public String getPropertyMessage(String propName) {
		String retVal = null;
		
		if(propName.equals(PRINT_SECTION_NAMES)) {
			retVal = "Print report element titles";
		} else if(propName.equals(INDENT_CONTENT)) {
			retVal = "Indent content";
		}
		
		return (retVal == null ? super.getPropertyMessage(propName) : retVal);
	}

	@Override
	public Object getPropertyDefault(String propName) {
		Object retVal = null;
		
		if(propName.equals(PRINT_SECTION_NAMES)) {
			retVal = new Boolean(true);
		} else if(propName.equals(INDENT_CONTENT)) {
			retVal = new Boolean(false);
		}
		
		return (retVal == null ? super.getPropertyDefault(propName) : retVal);
	}
	
	
	/*
	 * CSV report properties
	 */
	/**
	 * CSV separator char
	 */
	public static final String CSV_SEP_CHAR = "_sep_char_";
	
	/**
	 * CSV quote char
	 */
	public static final String CSV_QUOTE_CHAR = "_quote_char_";
	
	/**
	 * CSV line term
	 */
	public static final String CSV_LINE_TERM = "_line_term_";
	
	/**
	 * File Encoding
	 */
	public static final String FILE_ENCODING = "_file_encoding_";
	
	/**
	 * CSV File writer
	 */
	private CSVWriter writer;

	public CSVReportBuilder() {
		// put default props
		putProperty(CSV_SEP_CHAR, ',');
		putProperty(CSV_QUOTE_CHAR, '\"');
		putProperty(CSV_LINE_TERM,
				(OSInfo.isWindows() ? "\r\n" : "\n"));
		putProperty(FILE_ENCODING, "UTF-8");
	}
	
	/**
	 * TODO Datasources should be given as indirect dependencies since
	 * we can re-use them for other report builders.  Perhaps a global
	 * object can be created to keep track of created datasources so
	 * they can still be lazilly generated.
	 */
	@Override
	public void buildReport(ReportDesign design, Project project, 
			Query query, ResultSet[] resultSets, OutputStream stream) throws ReportBuilderException {
		
		char sep = (Character)getProperty(CSV_SEP_CHAR);
		char quote = (Character)getProperty(CSV_QUOTE_CHAR);
		String lineTerm = (String)getProperty(CSV_LINE_TERM);
		
		try {
			OutputStreamWriter fWriter = 
				new OutputStreamWriter(stream, getProperty(FILE_ENCODING).toString());
			
			writer = new CSVWriter(fWriter, sep, quote, lineTerm);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			throw new ReportBuilderException(e);
		}
		
		for(JAXBElement<? extends Section> sectionEle:design.getReportSection()) {
			// throw an exception if the build was cancelled
			if(isBuildCanceled()) {
				throw new ReportBuilderException("Canceled by user");
			}
			Section section = sectionEle.getValue();
			
//			if(isIndentContent())
//				prevIndentLevel();
			
			if(isPrintSectionNames()) {
				// output newline then a single line with the section name
				writer.writeNext(new String[0]);
				writer.writeNext(new String[]{ section.getName() });
				
//				if(isIndentContent())
//					nextIndentLevel();
			}
			
			CSVSectionWriter sectionWriter = null;
			
			if(section instanceof AggregrateInventory) {
				AggregrateInventory invData = (AggregrateInventory)section;
//				List<Search> searches = new ArrayList<Search>();
//				for(long sid:invData.getSid()) {
//					searches.add(new Search(sid));
//				}
				InventoryDataSource invDs = 
					new InventoryDataSource(resultSets, (AggregrateInventory)section);
				sectionWriter = new CSVTableDataSourceWriter(this, invDs);
			} else if (section instanceof ParamSection) {
				ParamSection pSec = (ParamSection)section;
				sectionWriter = new CSVTableDataSourceWriter(this, new ParamDataSource(query, pSec));
		    } else if (section instanceof SummarySection) {
		    	SummarySection summarySection = (SummarySection)section;
		    	sectionWriter = new CSVTableDataSourceWriter(this, new SummaryDataSource(resultSets, summarySection));
			} else if (section instanceof CommentSection) {
				CommentSection commentSection = (CommentSection)section;
				sectionWriter = new CSVCommentWriter(commentSection);
			} else if (section instanceof Group) {
				Group group = (Group)section;
				
//				if(isIndentContent())
//					nextIndentLevel();
				
				for(ResultSet resultSet:resultSets) {
					if(isBuildCanceled()) {
						throw new ReportBuilderException("Canceled by user");
					}
					// write session info header if requested
					if(group.isPrintSessionHeader()) {
						printSessionHeader(group, project, resultSet);
					}
					
					for(JAXBElement<? extends Section> groupSectionEle:group.getGroupReportSection()) {
						if(isBuildCanceled()) {
							throw new ReportBuilderException("Canceled by user");
						}
						
						Section groupSection = groupSectionEle.getValue();
						
						if(isPrintSectionNames()) {
							// output newline then a single line with the section name
							writer.writeNext(new String[0]);
							List<String> groupTitleLine = new ArrayList<String>();
							for(int i = 0; i < getIndentLevel(); i++) groupTitleLine.add("");
							groupTitleLine.add(groupSection.getName());
							writer.writeNext(groupTitleLine.toArray(new String[0]));
	//						writer.writeNext(new String[0]);
							
							if(isIndentContent()) nextIndentLevel();
						}
						
						if(groupSection instanceof ResultListing) {
							ResultListing tblInv = (ResultListing)groupSection;
							ResultListingDataSource tblInvDs = 
								new ResultListingDataSource(project, resultSet, tblInv);
							
							CSVSectionWriter groupSectionWriter = new CSVTableDataSourceWriter(this, tblInvDs);
							groupSectionWriter.writeSection(writer, getIndentLevel());
							writer.writeNext(new String[0]);
						} else if (groupSection instanceof CommentSection) {
							CommentSection commentSection = (CommentSection)groupSection;
							CSVCommentWriter commentWriter = new CSVCommentWriter(commentSection);
							commentWriter.writeSection(writer, getIndentLevel());
							writer.writeNext(new String[0]);
						} else if(groupSection instanceof InventorySection) {
							InventorySection invSection = (InventorySection)groupSection;
							
							InventoryDataSource invDs = new InventoryDataSource(new ResultSet[]{resultSet}, invSection);
							CSVTableDataSourceWriter dsWriter = new CSVTableDataSourceWriter(this, invDs);
							dsWriter.writeSection(writer, getIndentLevel());
							writer.writeNext(new String[0]);
						}
						
						if(isPrintSectionNames() && 
								isIndentContent()) prevIndentLevel();
					}
				}
				
			}
			
			if(sectionWriter != null) {
				sectionWriter.writeSection(writer, getIndentLevel());
				writer.writeNext(new String[0]);
				try {
					writer.flush();
				} catch (IOException e) {
					LOGGER
							.log(Level.SEVERE, e.getLocalizedMessage(), e);
				}
			}
 
//			if(isPrintSectionNames() && isIndentContent())
//				prevIndentLevel();
		}
		
		// flush and close writer
		try {   
			writer.flush();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
			throw new ReportBuilderException(e);
		}
		
	}
	
	private void printSessionHeader(Group group, Project project, ResultSet resultSet) {
		
		String sessionPath = resultSet.getSessionPath();
		
		List<String> sessionNameLine = new ArrayList<String>();
		for(int i = 0; i < getIndentLevel(); i++) sessionNameLine.add("");
		sessionNameLine.add("Session:");
		sessionNameLine.add(resultSet.getSessionPath());
//		String[] sessionNameLine = { "Session:", sessionPath };
		writer.writeNext(sessionNameLine.toArray(new String[0]));
//		writer.writeNext(new String[0]);
		
		if(group.isPrintParticipantInformation()) {
			try {
				Session t = project.openSession(resultSet.getCorpus(), resultSet.getSession());
				
//				String participantTitleLine[] = { "Participants:" };
				List<String> participantTitleLine = new ArrayList<String>();
				for(int i = 0; i < getIndentLevel(); i++) participantTitleLine.add("");
				participantTitleLine.add("Participants");
				writer.writeNext(participantTitleLine.toArray(new String[0]));
				
				List<String> currentLine = new ArrayList<String>();
				for(int i = 0; i < indentLevel; i++) currentLine.add(new String());
				String participantTableHeader[] = {
						"Name", "Age", "Sex", "Birthday", "Language", "Education", "Group", "Role" };
				for(int i = 0; i < participantTableHeader.length; i++) currentLine.add(participantTableHeader[i]);
				writer.writeNext(currentLine.toArray(new String[0]));
				
				for(int i = 0; i < t.getParticipantCount(); i++) {
					final Participant participant = t.getParticipant(i);
					currentLine.clear();
					for(int j = 0; j < indentLevel; j++) currentLine.add(new String());
					
					String name = 
						(participant.getName() != null ? participant.getName() : "");
//					PhonDurationFormat pdf = new PhonDurationFormat(PhonDurationFormat.PHON_FORMAT);
					String age = 
						AgeFormatter.ageToString(participant.getAge(t.getDate()));
					String sex = 
						(participant.getSex() == Sex.MALE ? "M" : "F");
//					PhonDateFormat pdtf =  new PhonDateFormat(PhonDateFormat.YEAR_LONG);
					String birthday = 
						DateFormatter.dateTimeToString(participant.getBirthDate());
					
					String participantLine[] = {
							name, age, sex, birthday,
							participant.getLanguage(), participant.getEducation(),
							participant.getGroup(), participant.getRole().getTitle()
					};
					for(int j = 0; j < participantLine.length; j++) currentLine.add(participantLine[j]);
					
					writer.writeNext(currentLine.toArray(new String[0]));
				}
				writer.writeNext(new String[0]);
				writer.flush();
			} catch (IOException e) {
				LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
			}
		}
	}
	
	/**
	 * Should we indent content
	 */
	public boolean isIndentContent() {
		Object v = super.getProperty(INDENT_CONTENT);
		if(v == null) v = getPropertyDefault(INDENT_CONTENT);
		boolean retVal = false;
		if(v != null && (v instanceof Boolean)) {
			retVal = (Boolean)v;
		}
		return retVal;
	}
	
	public boolean isPrintSectionNames() {
		Object v = super.getProperty(PRINT_SECTION_NAMES);
		if(v == null) v = getPropertyDefault(PRINT_SECTION_NAMES);
		boolean retVal = false;
		if(v != null && (v instanceof Boolean)) {
			retVal = (Boolean)v;
		}
		return retVal;
	}
	
	private int indentLevel = 0;
	private int getIndentLevel() {
		return indentLevel;
	}
	
	private int nextIndentLevel() {
		return (++indentLevel);
	}

	private int prevIndentLevel() {
		int retVal = (indentLevel > 0 ? --indentLevel : 0);
		return retVal;
	}

	@Override
	public String getMimetype() {
		return "text/csv";
	}

	@Override
	public String getFileExtension() {
		return "csv";
	}
	
	@Override
	public String getDisplayName() {
		return "CSV";
	}

}
