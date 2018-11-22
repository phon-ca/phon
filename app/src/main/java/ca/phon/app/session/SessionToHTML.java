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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.imageio.ImageIO;
import javax.swing.CellRendererPane;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.apache.derby.iapi.store.access.conglomerate.LogicalUndo;

import ca.phon.app.VersionInfo;
import ca.phon.app.log.LogUtil;
import ca.phon.formatter.FormatterUtil;
import ca.phon.ipa.IPATranscript;
import ca.phon.ipa.alignment.PhoneMap;
import ca.phon.query.db.Result;
import ca.phon.query.db.ResultSet;
import ca.phon.query.db.ResultSetRecordFilter;
import ca.phon.query.db.ResultValue;
import ca.phon.session.Record;
import ca.phon.session.RecordFilter;
import ca.phon.session.Session;
import ca.phon.session.TierViewItem;
import ca.phon.ui.fonts.FontPreferences;
import ca.phon.ui.ipa.PhoneMapDisplay;
import ca.phon.ui.ipa.SyllabificationDisplay;

/**
 * Class for constructing an HTML page from a Phon session.
 *
 */
public class SessionToHTML {

	// primary record filter
	private RecordFilter recordFilter;
	
	private boolean includeHeader = true;

	// participant information
	private boolean includeParticipantInfo;
	private boolean includeAge = true;
	private boolean includeBirthday = false;
	private boolean includeRole = true;
	private boolean includeSex = false;
	private boolean includeLanguage = false;
	private boolean includeGroup = false;
	private boolean includeEducation = false;
	private boolean includeSES = false;
	
	// tier data
	private boolean includeTierData = true;
		
	// options to include/exclude tiers by name
	private boolean excludeTiers = false;
	private List<String> tierList = new ArrayList<>();
	
	// add syllabification display data to IPA Target/Actual tiers
	private boolean includeSyllabification;
	private SyllabificationDisplay syllabificationDisplay;
	private JPanel syllabificationRenderPane;
	
	// add alignment display to end of tier information
	private boolean includeAlignment;
	private PhoneMapDisplay alignmentDisplay;
	private JPanel alignmentRenderPane;
	
	// tier view - order, visibility and fonts
	private List<TierViewItem> tierView;
	
	// query result options
	private boolean includeQueryResults = false;
	private boolean filterRecordsUsingQueryResults = false;
	
	// options to include/exclude result values/metadata by name
	private boolean excludeResultValues;
	private List<String> resultValueList = new ArrayList<>();
	
	public SessionToHTML() {
		this(true, true, true, null);
	}
	
	/**
	 * 
	 * @param includeParticipantInfo
	 * @param includeSyllabification
	 * @param includeAlignment
	 * @param tierView if <code>null</code>, tierView from session is used
	 */
	public SessionToHTML(boolean includeParticipantInfo, boolean includeSyllabification,
			boolean includeAlignment, List<TierViewItem> tierView) {
		super();
		
		this.includeParticipantInfo = includeParticipantInfo;
		this.includeSyllabification = includeSyllabification;
		this.includeAlignment = includeAlignment;
		this.tierView = tierView;
	}
	
	public boolean isIncludeHeader() {
		return this.includeHeader;
	}
	
	public void setIncludeHeader(boolean includeHeader) {
		this.includeHeader = includeHeader;
	}

	public boolean isIncludeParticipantInfo() {
		return includeParticipantInfo;
	}

	public void setIncludeParticipantInfo(boolean includeParticipantInfo) {
		this.includeParticipantInfo = includeParticipantInfo;
	}
	
	public boolean isIncludeAge() {
		return includeAge;
	}

	public void setIncludeAge(boolean includeAge) {
		this.includeAge = includeAge;
	}

	public boolean isIncludeBirthday() {
		return includeBirthday;
	}

	public void setIncludeBirthday(boolean includeBirthday) {
		this.includeBirthday = includeBirthday;
	}

	public boolean isIncludeRole() {
		return includeRole;
	}

	public void setIncludeRole(boolean includeRole) {
		this.includeRole = includeRole;
	}

	public boolean isIncludeSex() {
		return includeSex;
	}

	public void setIncludeSex(boolean includeSex) {
		this.includeSex = includeSex;
	}

	public boolean isIncludeLanguage() {
		return includeLanguage;
	}

	public void setIncludeLanguage(boolean includeLanguage) {
		this.includeLanguage = includeLanguage;
	}

	public boolean isIncludeGroup() {
		return includeGroup;
	}

	public void setIncludeGroup(boolean includeGroup) {
		this.includeGroup = includeGroup;
	}

	public boolean isIncludeEducation() {
		return includeEducation;
	}

	public void setIncludeEducation(boolean includeEducation) {
		this.includeEducation = includeEducation;
	}

	public boolean isIncludeSES() {
		return includeSES;
	}

	public void setIncludeSES(boolean includeSES) {
		this.includeSES = includeSES;
	}

	public boolean isIncludeSyllabification() {
		return includeSyllabification;
	}

	public void setIncludeSyllabification(boolean includeSyllabification) {
		this.includeSyllabification = includeSyllabification;
	}

	public boolean isIncludeAlignment() {
		return includeAlignment;
	}

	public void setIncludeAlignment(boolean includeAlignment) {
		this.includeAlignment = includeAlignment;
	}

	public boolean isIncludeTierData() {
		return this.includeTierData;
	}
	
	public void setIncludeTierData(boolean includeTierData) {
		this.includeTierData = includeTierData;
	}
	
	public boolean isExcludeTiers() {
		return this.excludeTiers;
	}
	
	public void setExcludeTiers(boolean excludeTiers) {
		this.excludeTiers = excludeTiers;
	}
	
	public List<String> getTierList() {
		return this.tierList;
	}
	
	public void setTierList(List<String> tierList) {
		this.tierList = tierList;
	}
	
	public List<TierViewItem> getTierView() {
		return tierView;
	}

	public void setTierView(List<TierViewItem> tierView) {
		this.tierView = tierView;
	}
	
	public boolean isIncludeQueryResults() {
		return this.includeQueryResults;
	}
	
	public void setIncludeQueryResults(boolean includeQueryResults) {
		this.includeQueryResults = includeQueryResults;
	}
	
	public boolean isFilterRecordsUsingQueryResults() {
		return this.filterRecordsUsingQueryResults;
	}
	
	public void setFilterRecordsUsingQueryResults(boolean filterRecords) {
		this.filterRecordsUsingQueryResults = filterRecords;
	}
	
	public boolean isExcludeResultValues() {
		return this.excludeResultValues;
	}
	
	public void setExcludeResultValues(boolean excludeResultValues) {
		this.excludeResultValues = excludeResultValues;
	}
	
	public List<String> getResultValues() {
		return this.resultValueList;
	}
	
	public void setResultValues(List<String> resultValues) {
		this.resultValueList = resultValues;
	}
	
	public RecordFilter getRecordFilter() {
		return this.recordFilter;
	}
	
	public void setRecordFilter(RecordFilter recordFilter) {
		this.recordFilter = recordFilter;
	}
	
	public SyllabificationDisplay getSyllabificationDisplay() {
		if(syllabificationDisplay == null) {
			try {
				SwingUtilities.invokeAndWait( () -> {
					syllabificationDisplay = new SyllabificationDisplay();
					syllabificationRenderPane = new JPanel();
					syllabificationDisplay.setFont(FontPreferences.getUIIpaFont());
				});
			} catch (InvocationTargetException | InterruptedException e) {
				LogUtil.severe(e.getLocalizedMessage(), e);
			}
		}
		return this.syllabificationDisplay;
	}
	
	public PhoneMapDisplay getAlignmentDisplay() {
		if(alignmentDisplay == null) {
			try {
				SwingUtilities.invokeAndWait( () -> {
					alignmentDisplay = new PhoneMapDisplay();
					alignmentDisplay.setBackground(Color.white);
					alignmentDisplay.setFont(FontPreferences.getUIIpaFont());
					alignmentRenderPane = new JPanel();
				});
			} catch (InvocationTargetException | InterruptedException e) {
				LogUtil.severe(e.getLocalizedMessage(), e);
			}
		}
		return alignmentDisplay;
	}
	
	private String createSyllabificationImageData(IPATranscript ipa) {
		final StringBuffer buffer = new StringBuffer();
		final SyllabificationDisplay display = getSyllabificationDisplay();
		
		try {
			SwingUtilities.invokeAndWait( () -> {
				display.setTranscript(ipa);
				display.revalidate();
				
				Dimension prefSize = display.getPreferredSize();
				
				BufferedImage img = new BufferedImage((int)prefSize.width+5, (int)prefSize.height, BufferedImage.TYPE_4BYTE_ABGR);
				
				Graphics2D g = img.createGraphics();
				SwingUtilities.paintComponent(g, display, syllabificationRenderPane, 0, 0, prefSize.width+5, prefSize.height);
				
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				try {
					ImageIO.write(img, "png", bos);
				} catch (IOException e) {
					LogUtil.warning(e.getLocalizedMessage(), e);
				}
				
				var base64data = Base64.getEncoder().encodeToString(bos.toByteArray());
				buffer.append(base64data);				
			});
		} catch (InvocationTargetException | InterruptedException e) {
			LogUtil.severe(e.getLocalizedMessage(), e);
		}
		
		return buffer.toString();
	}
	
	private String createAlignmentImageData(PhoneMap alignment) {
		final StringBuffer buffer = new StringBuffer();
		final PhoneMapDisplay display = getAlignmentDisplay();
		
		try {
			SwingUtilities.invokeAndWait( () -> {
				display.setPhoneMapForGroup(0, alignment);
				display.revalidate();
				
				Dimension prefSize = display.getPreferredSize();
				
				BufferedImage img = new BufferedImage((int)prefSize.width, (int)prefSize.height, BufferedImage.TYPE_4BYTE_ABGR);
				
				Graphics2D g = img.createGraphics();
				SwingUtilities.paintComponent(g, display, alignmentRenderPane, 0, 0, prefSize.width, prefSize.height);
				
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				try {
					ImageIO.write(img, "png", bos);
				} catch (IOException e) {
					LogUtil.warning(e.getLocalizedMessage(), e);
				}
				
				var base64data = Base64.getEncoder().encodeToString(bos.toByteArray());
				buffer.append(base64data);
			});
		} catch (InvocationTargetException | InterruptedException e) {
			LogUtil.severe(e.getLocalizedMessage(), e);
		}
		
		return buffer.toString();
	}
	
	private void appendParticipantInformation(Session session, StringBuffer buffer) {
		var nl = "\n";
		buffer.append("<div class='participants'>").append(nl);
		
		buffer.append("<table id='participant_table'>").append(nl);
		buffer.append("<tr>").append(nl);
		buffer.append("<th>Participant</th>").append(nl);
		if(includeAge) {
			buffer.append("<th>").append("Age").append("</th>").append(nl);
		}
		if(includeBirthday) {
			buffer.append("<th>").append("Birthday").append("</th>").append(nl);
		}
		if(includeRole) {
			buffer.append("<th>").append("Role").append("</th>").append(nl);
		}
		if(includeSex) {
			buffer.append("<th>").append("Sex").append("</th>").append(nl);
		}
		if(includeLanguage) {
			buffer.append("<th>").append("Language").append("</th>").append(nl);
		}
		if(includeGroup) {
			buffer.append("<th>").append("Group").append("</th>").append(nl);
		}
		if(includeEducation) {
			buffer.append("<th>").append("Education").append("</th>").append(nl);
		}
		if(includeSES) {
			buffer.append("<th>").append("SES").append("</th>").append(nl);
		}
		buffer.append("</tr>").append(nl);
		
		for(var i = 0; i < session.getParticipantCount(); i++) {
			var participant = session.getParticipant(i);
			buffer.append("<tr>").append(nl);
			buffer.append("<td>").append(participant).append("</td>").append(nl);
			
			if(includeAge) {
				if(participant.getBirthDate() != null) {
					buffer.append("<td>").append(
							FormatterUtil.format(participant.getAge(session.getDate())) ).append("</td>").append(nl);
				} else {
					buffer.append("<td>&nbsp;</td>").append(nl);
				}
			}
			if(includeBirthday) {
				if(participant.getBirthDate() != null) {
					buffer.append("<td>").append(
							FormatterUtil.format(participant.getBirthDate()) ).append("</td>").append(nl);
				} else {
					buffer.append("<td>&nbsp;</td>").append(nl);
				}
			}
			if(includeRole) {
				if(participant.getRole() != null) {
					buffer.append("<td>").append(participant.getRole()).append("</td>").append(nl);
				} else {
					buffer.append("<td>&nbsp;</td>").append(nl);
				}
			}
			if(includeSex) {
				if(participant.getSex() != null) {
					buffer.append("<td>").append(participant.getSex()).append("</td>").append(nl);
				} else {
					buffer.append("<td>&nbsp;</td>").append(nl);
				}
			}
			if(includeLanguage) {
				if(participant.getLanguage() != null) {
					buffer.append("<td>").append(participant.getLanguage()).append("</td>").append(nl);
				} else {
					buffer.append("<td>&nbsp;</td>").append(nl);
				}
			}
			if(includeGroup) {
				if(participant.getGroup() != null) {
					buffer.append("<td>").append(participant.getGroup()).append("</td>").append(nl);
				} else {
					buffer.append("<td>&nbsp;</td>").append(nl);
				}
			}
			if(includeEducation) {
				if(participant.getEducation() != null) {
					buffer.append("<td>").append(participant.getEducation()).append("</td>").append(nl);
				} else {
					buffer.append("<td>&nbsp;</td>").append(nl);
				}
			}
			if(includeSES) {
				if(participant.getSES() != null) {
					buffer.append("<td>").append(participant.getSES()).append("</td>").append(nl);
				} else {
					buffer.append("<td>&nbsp;</td>").append(nl);
				}
			}
			buffer.append("</tr>").append(nl);
		}
		
		buffer.append("</table>").append(nl);
		
		buffer.append("</div>").append(nl);
	}

	private void appendRecord(Session session, int recordIndex, ResultSet resultSet, StringBuffer buffer) {
		var record = session.getRecord(recordIndex);
		var nl = "\n";
		
		List<Result> resultsForRecord = new ArrayList<>();
		if(isIncludeQueryResults() && resultSet != null) {
			resultsForRecord = StreamSupport.stream(resultSet.spliterator(), false)
				.filter( (result) -> result.getRecordIndex() == recordIndex )
				.collect( Collectors.toList() );
		}
		
		buffer.append("<div class='record' id='record").append(recordIndex).append("'>").append(nl);
		
		var recordNum = recordIndex + 1;
		buffer.append("<b>").append(recordNum).append(". ").append(record.getSpeaker()).append("</b>");
		if(isIncludeQueryResults() && resultsForRecord.size() > 0) {
			buffer.append(" (").append(resultsForRecord.size()).append(" result").append(
					(resultsForRecord.size() > 1 ? "s)" : ")"));
		}
		buffer.append(nl);
		
		if(isIncludeTierData()) {
			// setup table
			buffer.append("<table class='record_table' id='table").append(recordIndex).append("'>").append(nl);
			
			
			var tierView = (getTierView() != null ? getTierView() : session.getTierView());
			
			var groupCount = record.numberOfGroups();
			for(var tierIdx = 0; tierIdx < tierView.size(); tierIdx++) {
				var tvi = tierView.get(tierIdx);
				
				if(getTierList().size() > 0) {
					boolean inList = getTierList().contains(tvi.getTierName());
					if(!inList && !isExcludeResultValues()) continue;
					else if(inList && isExcludeResultValues()) continue;
				}
				if(!tvi.isVisible()) continue;
				
				var tierClass = (tierIdx % 2 == 0 ? "tier_row" : "tier_alt_row" );
				
				buffer.append("<tr class='").append(tierClass).append("'>").append(nl);
				
				var tier = record.getTier(tvi.getTierName());
				if(tier == null) {
					buffer.append("<td class='tier_name'><em>").append(tvi.getTierName()).append("</em></td>").append(nl);
					buffer.append("<td class='tier_value' colspan='").append(groupCount).append("'>&nbsp;</td>").append(nl);
				} else {
					buffer.append("<td class='tier_name'><em>").append(tier.getName()).append("</em></td>").append(nl);
					if(tier.isGrouped()) {
						for(var i = 0; i < record.numberOfGroups(); i++) {
							buffer.append("<td class='tier_value'>").append(tier.getGroup(i)).append("</td>").append(nl);
						}
					} else {
						buffer.append("<td class='tier_value' colspan='").append(groupCount).append("'>").append(tier.getGroup(0)).append("</td>").append(nl);
					}
				}
				
				buffer.append("</tr>").append(nl);
				
				if(includeSyllabification && (tvi.getTierName().equals("IPA Target") || tvi.getTierName().equals("IPA Actual"))) {
					buffer.append("<tr class='").append(tierClass).append("'>").append(nl);
					
					buffer.append("<td class='tier_name'>&nbsp;</td>").append(nl);
					for(var i = 0; i < record.numberOfGroups(); i++) {
						IPATranscript ipa = (IPATranscript)tier.getGroup(i);
						String imgData = createSyllabificationImageData(ipa);
						if(imgData.length() > 0) {
							buffer.append("<td><img src='data:image/png;base64,").append(imgData).append("'/></td>").append(nl);
						} else {
							buffer.append("<td>&nbsp</td>").append(nl);
						}
					}
					
					buffer.append("</tr>").append(nl);
				}
				
			}
			
			if(includeAlignment) {
				// alignment
				var alignmentTier = record.getTier("Alignment");
				buffer.append("<tr class='tier_row'>").append(nl);
				buffer.append("<td class='tier_name'><em>Alignment</em></td>").append(nl);
				for(var i = 0; i < record.numberOfGroups(); i++) {
					final PhoneMap alignment = (PhoneMap)alignmentTier.getGroup(i);
					String imgData = createAlignmentImageData(alignment);
					if(imgData.length() > 0) {
						buffer.append("<td><img src='data:image/png;base64,").append(imgData).append("'/></td>").append(nl);
					} else {
						buffer.append("<td>&nbsp</td>").append(nl);
					}
				}
				buffer.append("</tr>").append(nl);
			}
					
			buffer.append("</table>").append(nl);
		}
		
		if(isIncludeQueryResults() && resultSet != null) {
			buffer.append("<div class='results'>").append(nl);
			buffer.append("<ol>").append(nl);
			int rIdx = 0;
			for(var result:resultsForRecord) {
				buffer.append("<li>").append(nl);
				appendQueryResult(rIdx++, result, buffer);
				buffer.append("</li>").append(nl);
			}
			buffer.append("</ol>").append(nl);
			buffer.append("</div>").append(nl);
		}
		buffer.append("</div>").append(nl);
	}
	
	private void appendQueryResult(int resultIdx, Result result, StringBuffer buffer) {
		var nl = "\n";
				
		buffer.append("<div class='result' id='result_").append(resultIdx).append("'>").append(nl);
		
		// setup table
		buffer.append("<table class='result_table' id='result_table_").append(resultIdx).append("'>").append(nl);
		int rowIdx = 0;
		for(int i = 0; i < result.getNumberOfResultValues(); i++) {
			final ResultValue rv = result.getResultValue(i);
			
			if(getResultValues().size() > 0) {
				boolean inList = getResultValues().contains(rv.getName());
				if(inList && isExcludeResultValues()) continue;
				else if(!inList && !isExcludeResultValues()) continue;
			}
			
			var trClass = (rowIdx++ % 2 == 0 ? "tier_row" : "tier_alt_row" );
			buffer.append("<tr class='").append(trClass).append("'>").append(nl);
			
			buffer.append("<td class='tier_name'><em>").append(rv.getName()).append("</em></td>").append(nl);
			buffer.append("<td class='tier_value'>").append(rv.getData()).append("</td>").append(nl);
			
			buffer.append("</tr>").append(nl);
		}
		
		for(String metadataKey:result.getMetadata().keySet()) {
			if(getResultValues().size() > 0) {
				boolean inList = getResultValues().contains(metadataKey);
				if(inList && isExcludeResultValues()) continue;
				else if(!inList && !isExcludeResultValues()) continue;
			}
			
			final String metadataValue = result.getMetadata().get(metadataKey);
			var trClass = (rowIdx++ % 2 == 0 ? "tier_row" : "tier_alt_row" );
			buffer.append("<tr class='").append(trClass).append("'>").append(nl);
			
			buffer.append("<td class='tier_name'><em>").append(metadataKey).append("</em></td>").append(nl);
			buffer.append("<td class='tier_value'>").append(metadataValue).append("</td>").append(nl);
			
			buffer.append("</tr>").append(nl);
		}
		
		buffer.append("</table>").append(nl);
		
		buffer.append("</div>").append(nl);
	}
	
	public String toHTML(Session session) {
		return toHTML(session, null);
	}
	
	public String toHTML(Session session, ResultSet resultSet) {
		final StringBuffer buffer = new StringBuffer();
		var nl = "\n";
		
		if(isIncludeHeader()) {
			buffer.append("<html lang='en'>").append(nl);
	
			// header
			buffer.append("<head>").append(nl);
			buffer.append("<title>Phon - ").append(session.getCorpus())
				.append(".").append(session.getName()).append("</title>").append(nl);
			
			var version = VersionInfo.getInstance().getVersion();
			buffer.append("<meta name='author' content='").append(version).append("'/>").append(nl);
			
			buffer.append("<meta charset='UTF-8'/>").append(nl);
			
			buffer.append("<style>").append(CSS).append("</style>").append(nl);
			// buffer.append("<script>").append(JS).append("</script>").append(nl);
			
			buffer.append("</head>").append(nl);
			
			// body
			buffer.append("<body>").append(nl);
			
			buffer.append("<div class='header'>").append(nl);
			buffer.append("<img width='36' height='36' src='").append(icon).append("'>&nbsp;</img>").append(nl);
			buffer.append("<div class='header_text'>").append(session.getCorpus()).append(".").append(session.getName()).append("</div>");
			buffer.append("</div>").append(nl);
		}
		
		if(isIncludeParticipantInfo()) {
			appendParticipantInformation(session, buffer);
		}
		
		RecordFilter queryFilter = null;
		if(isFilterRecordsUsingQueryResults() && resultSet != null) {
			queryFilter = new ResultSetRecordFilter(session, resultSet);
		}
		
		for(int rIdx = 0; rIdx < session.getRecordCount(); rIdx++) {
			var utt = session.getRecord(rIdx);
			// check filters
			if(recordFilter != null && !recordFilter.checkRecord(utt)) continue;
			if(queryFilter != null && !queryFilter.checkRecord(utt)) continue;
			appendRecord(session, rIdx, resultSet, buffer);
		}
		
		if(isIncludeHeader()) {
			buffer.append("</body>").append(nl);
			buffer.append("</html>").append(nl);
		}
		
		return buffer.toString();
	}
	
	private final static String icon = "data:image/png;base64,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";
	
	private static final String CSS = 
		"body {\n" + 
		"    background: white;\n" + 
		"    font-family: \"Charis SIL Compact\", arial,\"noto sans\", sans-serif;\n" + 
		"    margin-top: 0px;\n" + 
		"}" +
		"\n" +
		".header {\n" + 
		"	display: flex;\n" + 
		"	align-items: center;\n" + 
		"	margin-bottom: 1em;\n" + 
		"	background-color: #e6e6e6;\n" + 
		"}\n" + 
		"\n" + 
		".header_text {\n" + 
		"	font-size: large;\n" + 
		"	font-weight: bold;\n" + 
		"	margin-left: 5;\n" + 
		"}\n" + 
		"			\n" + 
		"table {\n" + 
		"    border: thin solid darkgrey;\n" + 
		"    empty-cells: show;\n" + 
		"    table-layout:fixed;\n" + 
		"    text-align: left;\n" + 
		"}" + "\n" + 
		".participants {\n" +
		"	margin-bottom: 1em;\n" +
		"}\n" +
		"\n" +
		".participants table {\n" + 
		"}\n" +
		".participants table tr th {\n" + 
		"    overflow:hidden;\n" + 
		"    font-weight: bold;\n" + 
		"    border-bottom: thin solid darkgrey;\n" + 
		"    padding-right: 20px;\n" + 
		"}\n" + 
		"\n" +
		"\n" +
		".record {\n" +
		"	margin-bottom: 1em;\n" + 
		"}\n" + 
		"\n" + 
		".record_table {\n" + 
		"	\n" + 
		"}\n" + 
		".record table tr td:first-child {\n" + 
		"    border-right: 1px solid lightgrey;\n" + 
		"}\n" +
		"\n" + 
		".tier_row {\n" + 
		"}\n" + 
		"\n" + 
		".tier_alt_row {\n" + 
		"}\n" + 
		"\n" + 
		".tier_name {\n" + 
		"	padding-right: 20;\n" + 
		"}\n" + 
		"\n" + 
		".tier_value {\n" + 
		"	padding-right: 5;\n" + 
		"}\n" + 
		"\n" + 
		".record_list {\n" + 
		"}\n" + 
		"\n" + 
		"@media print {\n" + 
		"	.record {\n" + 
		"		page-break-inside: avoid !important;\n" + 
		"	}\n" + 
		"}\n";
	
}