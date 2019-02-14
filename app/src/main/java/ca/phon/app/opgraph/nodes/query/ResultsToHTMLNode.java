/*
 * Copyright (C) 2012-2019 Gregory Hedlund & Yvan Rose
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
package ca.phon.app.opgraph.nodes.query;

import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextArea;

import org.apache.commons.lang3.ArrayUtils;
import org.jdesktop.swingx.VerticalLayout;

import ca.phon.app.log.LogUtil;
import ca.phon.app.session.SessionToHTML;
import ca.phon.ipa.IPATranscript;
import ca.phon.ipa.IPATranscriptBuilder;
import ca.phon.ipa.alignment.PhoneAligner;
import ca.phon.ipa.alignment.PhoneMap;
import ca.phon.opgraph.InputField;
import ca.phon.opgraph.OpContext;
import ca.phon.opgraph.OpNode;
import ca.phon.opgraph.OpNodeInfo;
import ca.phon.opgraph.OutputField;
import ca.phon.opgraph.app.GraphDocument;
import ca.phon.opgraph.app.extensions.NodeSettings;
import ca.phon.opgraph.exceptions.ProcessingException;
import ca.phon.project.Project;
import ca.phon.query.db.ResultSet;
import ca.phon.session.Session;
import ca.phon.session.SessionPath;
import ca.phon.ui.fonts.FontPreferences;
import ca.phon.ui.ipa.PhoneMapDisplay;
import ca.phon.ui.ipa.SyllabificationDisplay;

@OpNodeInfo(name="Results to HTML", category="Query", description="Print results in HTML format optionally including tier data.", showInLibrary=true)
public class ResultsToHTMLNode extends OpNode implements NodeSettings {
	
	private final InputField projectInput = new InputField("project", "Project", false, true, Project.class);

	private final InputField resultSetsInput = new InputField("results", "Query results", false, true, ResultSet[].class);

	private final OutputField htmlMapOutput = new OutputField("htmlMap", "Map of session -> HTML values", true, Map.class);
	
	private final OutputField keySetOutput = new OutputField("keySet", "Session path keys for html map", true, Collection.class);
	
	private JPanel settingsPanel;
	
	private JCheckBox includeTierDataBox;
	private boolean includeTierData = false;
	
	private boolean includeSyllabifiation = false;
	private JCheckBox includeSyllabificationBox;
	private SyllabificationDisplay syllabificationDisplay;
	
	private boolean includeAlignment = true;
	private JCheckBox includeAlignmentBox;
	private PhoneMapDisplay alignmentDisplay;
	
	private JCheckBox includeParticipantInfoBox;
	private boolean includeParticipantInfo;
	private boolean participantInfoDefaults[] = {
			true,
			true,
			false,
			false,
			false,
			false,
			false,
			false
	};
	private String participantInfoParamIds[] = {
			"includeRole",
			"includeAge",
			"includeBirthday",
			"includeSex",
			"includeLanguage",
			"includeGroup",
			"includeEducation",
			"includeSES"
	};
	private String participantInfoLabels[] = {
			"Role",
			"Age",
			"Birthday",
			"Sex",
			"Language",
			"Group",
			"Education",
			"SES"
	};
	private JCheckBox[] participantInfoBoxes;
	private boolean[] participantInfoChoices = Arrays.copyOf(participantInfoDefaults, participantInfoDefaults.length);
	
	public ResultsToHTMLNode() {
		super();
		
		putField(projectInput);
		putField(resultSetsInput);
		putField(htmlMapOutput);
		putField(keySetOutput);
		
		putExtension(NodeSettings.class, this);
	}

	@Override
	public void operate(OpContext context) throws ProcessingException {
		final Project project = (Project)context.get(projectInput);
		final ResultSet[] resultSets = (ResultSet[])context.get(resultSetsInput);
		
		final Map<SessionPath, String> htmlMap = new LinkedHashMap<>();
		
		SessionToHTML sessionToHTML = new SessionToHTML();
		
		sessionToHTML.setShowQueryResultsFirst(true);
		
		sessionToHTML.setIncludeParticipantInfo(isIncludeParticipantInfo());
		sessionToHTML.setIncludeRole(isIncludeParticipantInfo("Role"));
		sessionToHTML.setIncludeAge(isIncludeParticipantInfo("Age"));
		sessionToHTML.setIncludeBirthday(isIncludeParticipantInfo("Birthday"));
		sessionToHTML.setIncludeEducation(isIncludeParticipantInfo("Education"));
		sessionToHTML.setIncludeGroup(isIncludeParticipantInfo("Group"));
		sessionToHTML.setIncludeLanguage(isIncludeParticipantInfo("Language"));
		sessionToHTML.setIncludeSex(isIncludeParticipantInfo("Sex"));
		sessionToHTML.setIncludeSES(isIncludeParticipantInfo("SES"));
		
		sessionToHTML.setIncludeHeader(false);
		sessionToHTML.setIncludeTierData(isIncludeTierData());
		sessionToHTML.setIncludeSyllabification(isIncludeSyllabification());
		sessionToHTML.setIncludeAlignment(isIncludeAlignment());
		sessionToHTML.setFilterRecordsUsingQueryResults(true);
		sessionToHTML.setIncludeQueryResults(true);
		
		for(ResultSet rs:resultSets) {
			SessionPath sp = new SessionPath(rs.getCorpus(), rs.getSession());
			
			try {
				Session session = project.openSession(sp.getCorpus(), sp.getSession());
				sessionToHTML.setTierView(session.getTierView());
				
				String html = sessionToHTML.toHTML(session, rs);
				htmlMap.put(sp, html);
			} catch (IOException e) {
				LogUtil.severe(e);
			}
		}
		
		context.put(htmlMapOutput, htmlMap);
		context.put(keySetOutput, htmlMap.keySet());
	}
	
	public boolean isIncludeParticipantInfo() {
		return (includeParticipantInfoBox != null ? includeParticipantInfoBox.isSelected() : includeParticipantInfo);
	}
	
	public void setIncludeParticipantInfo(boolean includeParticipantInfo) {
		this.includeParticipantInfo = includeParticipantInfo;
		if(includeParticipantInfoBox != null) {
			this.includeParticipantInfoBox.setSelected(includeParticipantInfo);
		}
	}
	
	private int participantInfoIndexOf(String name) {
		int idx = ArrayUtils.indexOf(participantInfoLabels, name);
		if(idx < 0) {
			idx = ArrayUtils.indexOf(participantInfoParamIds, name);
		}
		return idx;
	}
	
	public boolean isIncludeParticipantInfo(String name) {
		int idx = participantInfoIndexOf(name);
		if(idx < 0) return false;
		
		if(participantInfoBoxes != null) {
			return participantInfoBoxes[idx].isSelected();
		} else {
			return participantInfoChoices[idx];
		}
	}
	
	public void setIncludeParticipantInfo(String name, boolean inc) {
		int idx = participantInfoIndexOf(name);
		if(idx < 0) return;
		
		participantInfoChoices[idx] = inc;
		if(participantInfoBoxes != null) {
			participantInfoBoxes[idx].setSelected(inc);
		}
	}
	
	public boolean isIncludeTierData() {
		return (includeTierDataBox != null ? includeTierDataBox.isSelected() : this.includeTierData);
	}

	public void setIncludeTierData(boolean includeTierData) {
		this.includeTierData = includeTierData;
		if(includeTierDataBox != null)
			includeTierDataBox.setSelected(includeTierData);
	}
	
	public boolean isIncludeAlignment() {
		return (includeAlignmentBox != null ? includeAlignmentBox.isSelected() : this.includeAlignment);
	}
	
	public void setIncludeAlignment(boolean includeAlignment) {
		this.includeAlignment = includeAlignment;
		if(includeAlignmentBox != null)
			includeAlignmentBox.setSelected(includeAlignment);
	}
	
	public boolean isIncludeSyllabification() {
		return (includeSyllabificationBox != null ? includeSyllabificationBox.isSelected() : this.includeSyllabifiation);
	}
	
	public void setIncludeSyllabification(boolean includeSyllabification) {
		this.includeSyllabifiation = includeSyllabification;
		if(includeSyllabificationBox != null) 
			includeSyllabificationBox.setSelected(includeSyllabification);
	}

	@Override
	public Component getComponent(GraphDocument document) {
		if(settingsPanel == null) {
			settingsPanel = new JPanel(new VerticalLayout(5));
			
			JPanel participantPanel = new JPanel(new VerticalLayout());
			participantPanel.setBorder(BorderFactory.createTitledBorder("Participant Options"));
			
			includeParticipantInfoBox = new JCheckBox("Include participant information");
			includeParticipantInfoBox.setSelected(includeParticipantInfo);
			includeParticipantInfoBox.addActionListener( (e) -> {
				for(JCheckBox cb:participantInfoBoxes) {
					cb.setEnabled(includeParticipantInfoBox.isSelected());
				}
			});
			participantPanel.add(includeParticipantInfoBox);
			
			JPanel participantInfoChoicesPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
			participantInfoBoxes = new JCheckBox[participantInfoLabels.length];
			for(int i = 0; i < participantInfoLabels.length; i++) {
				participantInfoBoxes[i] = new JCheckBox(participantInfoLabels[i]);
				participantInfoBoxes[i].setSelected(participantInfoChoices[i]);
				participantInfoBoxes[i].setEnabled(includeParticipantInfo);
				participantInfoChoicesPanel.add(participantInfoBoxes[i]);
			}
			participantPanel.add(participantInfoChoicesPanel);
			
			JPanel tierDataPanel = new JPanel(new VerticalLayout());
			includeTierDataBox = new JCheckBox("Include tier data");
			includeTierDataBox.setSelected(includeTierData);
			includeTierDataBox.addActionListener( (e) -> {
				includeSyllabificationBox.setEnabled(includeTierDataBox.isSelected());
				includeAlignmentBox.setEnabled(includeTierDataBox.isSelected());
			});
			
			includeSyllabificationBox = new JCheckBox("Include syllabification");
			includeSyllabificationBox.setSelected(includeSyllabifiation);
			includeSyllabificationBox.setEnabled(includeTierData);
			
			final IPATranscript ipaT = (new IPATranscriptBuilder()).append("ˈb:Oʌ:Nɹ:Cθ:Cˌd:Oe͜ɪ:N").toIPATranscript();
			final IPATranscript ipaA = (new IPATranscriptBuilder()).append("ˈb:Oʌː:Nˌt:Oe͜ɪ:N").toIPATranscript();
			final PhoneMap alignment = (new PhoneAligner()).calculatePhoneAlignment(ipaT, ipaA);
			
			syllabificationDisplay = new SyllabificationDisplay();
			syllabificationDisplay.setTranscript(ipaT);
			syllabificationDisplay.setFont(FontPreferences.getUIIpaFont());
			syllabificationDisplay.setBorder(BorderFactory.createEmptyBorder(0, 25, 0, 5));
			
			includeAlignmentBox = new JCheckBox("Include alignment");
			includeAlignmentBox.setSelected(includeAlignment);
			includeAlignmentBox.setEnabled(includeTierData);
			
			alignmentDisplay = new PhoneMapDisplay();
			alignmentDisplay.setPhoneMapForGroup(0, alignment);
			alignmentDisplay.setBackground(Color.WHITE);
			alignmentDisplay.setFont(FontPreferences.getUIIpaFont());
			alignmentDisplay.setBorder(BorderFactory.createEmptyBorder(0, 25, 0, 5));
			
			tierDataPanel.add(includeTierDataBox);
			tierDataPanel.add(includeSyllabificationBox);
			tierDataPanel.add(syllabificationDisplay);
			tierDataPanel.add(includeAlignmentBox);
			tierDataPanel.add(alignmentDisplay);
			tierDataPanel.setBorder(BorderFactory.createTitledBorder("Tier Data Options"));
			
			settingsPanel.add(participantPanel);
			settingsPanel.add(tierDataPanel);
		}
		return settingsPanel;
	}

	@Override
	public Properties getSettings() {
		Properties retVal = new Properties();
		
		retVal.put("includeParticipantInfo", Boolean.toString(isIncludeParticipantInfo()));
		for(int i = 0; i < participantInfoLabels.length; i++) {
			String id = participantInfoParamIds[i];
			retVal.put(id, Boolean.toString(isIncludeParticipantInfo(id)));
		}

		retVal.put("includeTierData", Boolean.toString(isIncludeTierData()));
		retVal.put("includeAlignment", Boolean.toString(isIncludeAlignment()));
		retVal.put("includeSyllabification", Boolean.toString(isIncludeSyllabification()));
		
		return retVal;
	}

	@Override
	public void loadSettings(Properties properties) {
		setIncludeParticipantInfo(Boolean.parseBoolean(properties.getProperty("includeParticipantInfo", "false")));
		for(int i = 0; i < participantInfoParamIds.length; i++) {
			setIncludeParticipantInfo(participantInfoParamIds[i], 
					Boolean.parseBoolean(
							properties.getProperty(participantInfoParamIds[i], Boolean.toString(participantInfoDefaults[i]))));
		}

		setIncludeTierData(Boolean.parseBoolean(properties.getProperty("includeTierData", "false")));
		setIncludeSyllabification(Boolean.parseBoolean(properties.getProperty("includeSyllabification", "false")));
		setIncludeAlignment(Boolean.parseBoolean(properties.getProperty("includeAlignment", "true")));
	}

}
