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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.apache.commons.lang3.ArrayUtils;
import org.jdesktop.swingx.VerticalLayout;

import ca.phon.app.log.ExcelExporter;
import ca.phon.app.log.LogUtil;
import ca.phon.app.session.SessionExportSettings;
import ca.phon.app.session.SessionToExcel;
import ca.phon.app.session.SessionToHTML;
import ca.phon.app.session.SessionToHTML.SessionToHTMLSettings;
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
import ca.phon.session.SessionFactory;
import ca.phon.session.SessionPath;
import ca.phon.session.TierViewItem;
import ca.phon.ui.fonts.FontPreferences;
import ca.phon.ui.ipa.PhoneMapDisplay;
import ca.phon.ui.ipa.SyllabificationDisplay;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;

@OpNodeInfo(name="Results to HTML", category="Query", description="Print results in HTML format optionally including tier data.", showInLibrary=true)
public class ResultsToHTMLNode extends OpNode implements NodeSettings {
	
	private final InputField projectInput = new InputField("project", "Project", false, true, Project.class);

	private final InputField resultSetsInput = new InputField("results", "Query results", false, true, ResultSet[].class);

	private final OutputField htmlMapOutput = new OutputField("htmlMap", "Map of session -> HTML values", true, Map.class);
	
	private final OutputField keySetOutput = new OutputField("keySet", "Session path keys for html map", true, Collection.class);
	
	private final OutputField exporterMapOutput = new OutputField("excelExporterMap", "Map of session -> exporter values", true, Map.class);
	
	private JPanel settingsPanel;
	
	private JCheckBox includeTierDataBox;
	private boolean includeTierData = false;
	
	private boolean includeSyllabifiation = false;
	private JCheckBox includeSyllabificationBox;
	private SyllabificationDisplay syllabificationDisplay;
	
	private boolean includeAlignment = false;
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
	
	private JRadioButton includeResultValuesBtn;
	private JRadioButton excludeResultValuesBtn;
	private boolean excludeResultValues = true;
	private JTextArea resultValuesArea;
	private List<String> resultValues = new ArrayList<>();
	
	private JRadioButton includeTiersBtn;
	private JRadioButton excludeTiersBtn;
	private boolean excludeTiers = false;
	private JTextArea tierArea;
	private List<String> tierNames = new ArrayList<>();
	
	public ResultsToHTMLNode() {
		super();
		
		putField(projectInput);
		putField(resultSetsInput);
		putField(htmlMapOutput);
		putField(exporterMapOutput);
		putField(keySetOutput);
		
		putExtension(NodeSettings.class, this);
	}

	@Override
	public void operate(OpContext context) throws ProcessingException {
		final Project project = (Project)context.get(projectInput);
		final ResultSet[] resultSets = (ResultSet[])context.get(resultSetsInput);
		
		final Map<SessionPath, String> htmlMap = new LinkedHashMap<>();
		final Map<SessionPath, ExcelExporter> exporterMap = new LinkedHashMap<>();
		
		SessionToHTML sessionToHTML = new SessionToHTML();
		
		SessionToHTMLSettings settings = (SessionToHTMLSettings)sessionToHTML.getSettings();
		settings.setShowQueryResultsFirst(true);
		
		settings.setIncludeParticipantInfo(isIncludeParticipantInfo());
		settings.setIncludeRole(isIncludeParticipantInfo("Role"));
		settings.setIncludeAge(isIncludeParticipantInfo("Age"));
		settings.setIncludeBirthday(isIncludeParticipantInfo("Birthday"));
		settings.setIncludeEducation(isIncludeParticipantInfo("Education"));
		settings.setIncludeGroup(isIncludeParticipantInfo("Group"));
		settings.setIncludeLanguage(isIncludeParticipantInfo("Language"));
		settings.setIncludeSex(isIncludeParticipantInfo("Sex"));
		settings.setIncludeSES(isIncludeParticipantInfo("SES"));
		
		settings.setIncludeHeader(false);
		settings.setIncludeTierData(isIncludeTierData());
		settings.setIncludeSyllabification(isIncludeSyllabification());
		settings.setIncludeAlignment(isIncludeAlignment());
		settings.setFilterRecordsUsingQueryResults(true);
		settings.setIncludeQueryResults(true);
		settings.setExcludeResultValues(isExcludeResultValues());
		settings.setResultValues(getResultValues());
		
		for(ResultSet rs:resultSets) {
			SessionPath sp = new SessionPath(rs.getCorpus(), rs.getSession());
			
			try {
				Session session = project.openSession(sp.getCorpus(), sp.getSession());
				SessionFactory factory = SessionFactory.newFactory();
				
				List<String> tierNames = getTierNames();
				List<TierViewItem> tierView = new ArrayList<>();
				if(tierNames.size() > 0) {
					for(TierViewItem tvi:session.getTierView()) {
						boolean tierNameInList = tierNames.contains(tvi.getTierName());
						if(isExcludeTiers()) {
							tierView.add(factory.createTierViewItem(tvi.getTierName(), !tierNameInList, tvi.getTierFont()));
						} else {
							tierView.add(factory.createTierViewItem(tvi.getTierName(), tierNameInList, tvi.getTierFont()));
						}
					}
					
					if(!isExcludeTiers()) {
						// use order defined by user
						tierView.sort( (tvi1, tvi2) -> {
							return Integer.valueOf(tierNames.indexOf(tvi1.getTierName())).compareTo(
									tierNames.indexOf(tvi2.getTierName()));
						});
					}
				} else {
					tierView.addAll(session.getTierView());
				}
				settings.setTierView(tierView);
				settings.setResultSet(rs);
				
				String html = sessionToHTML.toHTML(session);
				htmlMap.put(sp, html);
				
				SessionExportSettings cpy = new SessionExportSettings();
				cpy.copySettings(settings);
				
				exporterMap.put(sp, new SessionToExcelExporter(project, sp, cpy));
			} catch (IOException e) {
				LogUtil.severe(e);
			}
		}
		
		context.put(htmlMapOutput, htmlMap);
		context.put(exporterMapOutput, exporterMap);
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
	
	public void setIncludeTierData(boolean includeTierData) {
		this.includeTierData = includeTierData;
		if(includeTierDataBox != null)
			includeTierDataBox.setSelected(includeTierData);
	}

	public boolean isIncludeTierData() {
		return (includeTierDataBox != null ? includeTierDataBox.isSelected() : this.includeTierData);
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
	
	public boolean isExcludeResultValues() {
		return (excludeResultValuesBtn != null ? excludeResultValuesBtn.isSelected() : this.excludeResultValues);
	}
	
	public void setExcludeResultValues(boolean excludeResultValues) {
		this.excludeResultValues = excludeResultValues;
		if(includeResultValuesBtn != null)
			includeResultValuesBtn.setSelected(!excludeResultValues);
		if(excludeResultValuesBtn != null)
			excludeResultValuesBtn.setSelected(excludeResultValues);
	}
	
	public List<String> getResultValues() {
		if(this.resultValuesArea != null) {
			List<String> resultValues = new ArrayList<>();
			try(BufferedReader reader = new BufferedReader(new StringReader(resultValuesArea.getText()))) {
				String line = null;
				while((line = reader.readLine()) != null) {
					resultValues.add(line.trim());
				}
			} catch (IOException e) {
				LogUtil.warning(e);
			}
			return resultValues;
		} else {
			return this.resultValues;
		}
	}
	
	public void setResultValues(List<String> resultValues) {
		this.resultValues = resultValues;
		if(this.resultValuesArea != null) {
			String rvTxt = resultValues.stream().collect(Collectors.joining("\n"));
			resultValuesArea.setText(rvTxt);
		}
	}
	
	public boolean isExcludeTiers() {
		return (this.excludeTiersBtn != null ? this.excludeTiersBtn.isSelected() : this.excludeTiers);
	}
	
	public void setExcludeTiers(boolean excludeTiers) {
		this.excludeTiers = excludeTiers;
		if(this.excludeTiersBtn != null)
			this.excludeTiersBtn.setSelected(excludeTiers);
		if(this.includeTiersBtn != null)
			this.includeTiersBtn.setSelected(!excludeTiers);
	}
	
	public void setTierNames(List<String> tierNames) {
		this.tierNames = tierNames;
		if(this.tierArea != null) {
			String tierTxt = tierNames.stream().collect(Collectors.joining("\n"));
			tierArea.setText(tierTxt);
		}
	}
	
	public List<String> getTierNames() {
		if(this.tierArea != null && this.tierArea.getText().trim().length() > 0) {
			List<String> tierNames = new ArrayList<>();
			try(BufferedReader reader = new BufferedReader(new StringReader(tierArea.getText()))) {
				String line = null;
				while((line = reader.readLine()) != null) {
					tierNames.add(line.trim());
				}
			} catch (IOException e) {
				LogUtil.warning(e);
			}
			return tierNames;
		} else {
			return this.tierNames;
		}
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
			
			includeTierDataBox = new JCheckBox("Include tier data");
			includeTierDataBox.setSelected(includeTierData);
			includeTierDataBox.addActionListener( (e) -> {
				includeTiersBtn.setEnabled(includeTierDataBox.isSelected());
				excludeTiersBtn.setEnabled(includeTierDataBox.isSelected());
				tierArea.setEnabled(includeTierDataBox.isSelected());
				includeSyllabificationBox.setEnabled(includeTierDataBox.isSelected());
				includeAlignmentBox.setEnabled(includeTierDataBox.isSelected());
			});
	
			ButtonGroup bg = new ButtonGroup();
			includeTiersBtn = new JRadioButton("Include tiers");
			includeTiersBtn.setSelected(!this.excludeTiers);
			includeTiersBtn.setEnabled(includeTierData);
			bg.add(includeTiersBtn);
			
			excludeTiersBtn = new JRadioButton("Exclude tiers");
			excludeTiersBtn.setSelected(this.excludeTiers);
			excludeTiersBtn.setEnabled(includeTierData);
			bg.add(excludeTiersBtn);
			
			tierArea = new JTextArea();
			tierArea.setEnabled(includeTierData);
			tierArea.setRows(5);
			String tierTxt = tierNames.stream().collect(Collectors.joining("\n"));
			tierArea.setText(tierTxt);
						
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
			
			JPanel tierDataLeftPanel = new JPanel(new GridBagLayout());
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.gridx = 0;
			gbc.gridy = 0;
			gbc.weightx = 1.0;
			gbc.weighty = 0.0;
			gbc.anchor = GridBagConstraints.WEST;
			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbc.gridwidth = 1;
			gbc.gridheight = 1;
			tierDataLeftPanel.add(includeTiersBtn, gbc);
						
			gbc.gridy++;
			tierDataLeftPanel.add(excludeTiersBtn, gbc);
			
			gbc.gridheight = 1;
			gbc.gridwidth = 1;
			gbc.gridy++;
			gbc.fill = GridBagConstraints.BOTH;
			gbc.weightx = 1.0;
			gbc.weighty = 1.0;
			tierDataLeftPanel.add(new JScrollPane(tierArea), gbc);
			
			String tierNameLblTxt = 
					"<html><body style='font-size: small;'>One tier name per line. Leave empty to use session tier view.</body></html>";
			JLabel tierNameLbl = new JLabel(tierNameLblTxt);
			gbc.weightx = 1.0;
			gbc.weighty = 0.0;
			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbc.gridy++;
			tierDataLeftPanel.add(tierNameLbl, gbc);
			
			JPanel tierDataRightPanel = new JPanel(new VerticalLayout());
			tierDataRightPanel.add(includeSyllabificationBox);
			tierDataRightPanel.add(syllabificationDisplay);
			tierDataRightPanel.add(includeAlignmentBox);
			tierDataRightPanel.add(alignmentDisplay);
			
			JPanel tierDataPanel = new JPanel(new GridBagLayout());
			
			gbc.gridx = 0;
			gbc.gridy = 0;
			gbc.weightx = 1.0;
			gbc.weighty = 0.0;
			gbc.gridheight = 1;
			gbc.gridwidth = 2;
			gbc.fill = GridBagConstraints.HORIZONTAL;
			tierDataPanel.add(includeTierDataBox, gbc);
			
			gbc.weighty = 1.0;
			gbc.fill = GridBagConstraints.BOTH;
			gbc.gridy++;
			gbc.gridwidth = 1;
			tierDataPanel.add(tierDataLeftPanel, gbc);
			
			gbc.gridx++;
			tierDataPanel.add(tierDataRightPanel, gbc);
			
			tierDataPanel.setBorder(BorderFactory.createTitledBorder("Tier Data Options"));
			
			JPanel resultValuesPanel = new JPanel(new VerticalLayout());
			ButtonGroup btnGrp = new ButtonGroup();
			includeResultValuesBtn = new JRadioButton("Include result values");
			includeResultValuesBtn.setSelected(!excludeResultValues);
			excludeResultValuesBtn = new JRadioButton("Exclude result values");
			excludeResultValuesBtn.setSelected(excludeResultValues);
			
			btnGrp.add(includeResultValuesBtn);
			btnGrp.add(excludeResultValuesBtn);
			resultValuesArea = new JTextArea();
			resultValuesArea.setRows(5);
			resultValuesPanel.setBorder(BorderFactory.createTitledBorder("Result values"));
			String rvTxt = resultValues.stream().collect(Collectors.joining("\n"));
			resultValuesArea.setText(rvTxt);
			
			resultValuesPanel.add(includeResultValuesBtn);
			resultValuesPanel.add(excludeResultValuesBtn);
			resultValuesPanel.add(new JScrollPane(resultValuesArea));
			
			settingsPanel.add(participantPanel);
			settingsPanel.add(tierDataPanel);
			settingsPanel.add(resultValuesPanel);
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
		
		retVal.put("excludeTiers", Boolean.toString(isExcludeTiers()));
		String tierTxt = getTierNames().stream().collect(Collectors.joining(","));
		retVal.put("tierNames", tierTxt);
		retVal.put("includeAlignment", Boolean.toString(isIncludeAlignment()));
		retVal.put("includeSyllabification", Boolean.toString(isIncludeSyllabification()));
		
		retVal.put("excludeResultValues", Boolean.toString(isExcludeResultValues()));
		String rvTxt = getResultValues().stream().collect(Collectors.joining(","));
		retVal.put("resultValues", rvTxt);

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
		setExcludeTiers(Boolean.parseBoolean(properties.getProperty("excludeTiers", "false")));
		String tierTxt = properties.getProperty("tierNames", "");
		String tierNames[] = tierTxt.split(",");
		setTierNames(Arrays.asList(tierNames));
		setIncludeSyllabification(Boolean.parseBoolean(properties.getProperty("includeSyllabification", "false")));
		setIncludeAlignment(Boolean.parseBoolean(properties.getProperty("includeAlignment", "false")));
		
		setExcludeResultValues(Boolean.parseBoolean(properties.getProperty("excludeResultValues", "true")));
		String rvTxt = properties.getProperty("resultValues", "");
		String rvs[] = rvTxt.split(",");
		setResultValues(Arrays.asList(rvs));
	}
	
	private class SessionToExcelExporter implements ExcelExporter {

		private Project project;
		
		private SessionPath sessionPath;
		
		private SessionExportSettings settings;
		
		public SessionToExcelExporter(Project project, SessionPath sessionPath, SessionExportSettings settings) {
			super();
			
			this.project = project;
			this.sessionPath = sessionPath;
			this.settings = settings;
		}
		
		@Override
		public void addToWorkbook(WritableWorkbook wb) throws WriteException {
			try {
				Session session = project.openSession(sessionPath.getCorpus(), sessionPath.getSession());
				
				SessionToExcel converter = new SessionToExcel(settings);
				converter.createSheetInWorkbook(wb, session);
			} catch (IOException e) {
				LogUtil.severe(e);
			}
		}
		
	}

}
