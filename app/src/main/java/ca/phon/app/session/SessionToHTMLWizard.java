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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingWorker;
import javax.swing.table.AbstractTableModel;

import org.jdesktop.swingx.HorizontalLayout;
import org.jdesktop.swingx.JXBusyLabel;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.VerticalLayout;

import ca.phon.app.log.BufferPanel;
import ca.phon.app.log.LogUtil;
import ca.phon.app.log.MultiBufferPanel;
import ca.phon.app.query.OpenResultSetSelector;
import ca.phon.ipa.IPATranscript;
import ca.phon.ipa.IPATranscriptBuilder;
import ca.phon.ipa.alignment.PhoneAligner;
import ca.phon.ipa.alignment.PhoneMap;
import ca.phon.project.Project;
import ca.phon.query.db.ResultSet;
import ca.phon.session.Session;
import ca.phon.session.SessionFactory;
import ca.phon.session.TierViewItem;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.ui.decorations.TitledPanel;
import ca.phon.ui.fonts.FontPreferences;
import ca.phon.ui.ipa.PhoneMapDisplay;
import ca.phon.ui.ipa.SyllabificationDisplay;
import ca.phon.ui.wizard.BreadcrumbWizardFrame;
import ca.phon.ui.wizard.WizardStep;
import ca.phon.util.PrefHelper;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;

public class SessionToHTMLWizard extends BreadcrumbWizardFrame {

	private Project project;
	
	private Session session;
	
	private WizardStep optionsStep;
	private RecordFilterPanel recordFilterPanel;
	
	private JCheckBox includeParticipantInfoBox;
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
	private JCheckBox includeRole;
	private JCheckBox includeAge;
	private JCheckBox includeBirthday;
	private JCheckBox includeSex;
	private JCheckBox includeLanguage;
	private JCheckBox includeGroup;
	private JCheckBox includeEducation;
	private JCheckBox includeSES;
	
	private TierViewTableModel tableModel;
	private JXTable tierViewTable;
	private JButton moveTierUpBtn;
	private JButton moveTierDownBtn;
	
	private JCheckBox includeSyllabificationBox;
	private SyllabificationDisplay syllabificationDisplay;
	
	private JCheckBox includeAlignmentBox;
	private PhoneMapDisplay alignmentDisplay;
	
	private JCheckBox includeQueryResults;
	private JCheckBox filterUsingQueryResults;
	private OpenResultSetSelector resultSetSelector;
	private JXTable resultSetValuesTable;
	
	private WizardStep reportStep;
	private MultiBufferPanel bufferPanel;
	private JXBusyLabel busyLabel;
	
	public SessionToHTMLWizard(String title, Project project, Session session) {
		super(title);
		setWindowName("Session to HTML : " + session.getCorpus() + "." + session.getName());
		
		this.project = project;
		this.session = session;
		
		init();
	}
	
	private void init() {
		this.optionsStep = createOptionsStep();
		this.optionsStep.setNextStep(1);
		addWizardStep(optionsStep);
		
		this.reportStep = createPreviewStep();
		this.reportStep.setPrevStep(0);
		addWizardStep(reportStep);
	}
	
	public void moveTierUp() {
		final int selectedRow = tierViewTable.getSelectedRow();
		if(selectedRow > 0 && selectedRow < tableModel.tierView.size()) {
			final TierViewItem tvi = tableModel.tierView.remove(selectedRow);
			tableModel.fireTableRowsDeleted(selectedRow, selectedRow);
			int newRow = selectedRow - 1;
			tableModel.tierView.add(newRow, tvi);
			tableModel.fireTableRowsInserted(newRow, newRow);
			
			tierViewTable.getSelectionModel().setSelectionInterval(newRow, newRow);
		}
	}
	
	public void moveTierDown() {
		final int selectedRow = tierViewTable.getSelectedRow();
		if(selectedRow >= 0 && selectedRow < tableModel.tierView.size()-1) {
			final TierViewItem tvi = tableModel.tierView.remove(selectedRow);
			tableModel.fireTableRowsDeleted(selectedRow, selectedRow);
			int newRow = selectedRow + 1;
			tableModel.tierView.add(newRow, tvi);
			tableModel.fireTableRowsInserted(newRow, newRow);
			
			tierViewTable.getSelectionModel().setSelectionInterval(newRow, newRow);
		}
	}
	
	private WizardStep createOptionsStep() {
		final List<TierViewItem> tierView = new ArrayList<>();
		final SessionFactory factory = SessionFactory.newFactory();
		session.getTierView().forEach( (tvi) -> {
			final TierViewItem item = factory.createTierViewItem(tvi.getTierName(), tvi.isVisible(), tvi.getTierFont());
			tierView.add(item);
		});
		
		recordFilterPanel = new RecordFilterPanel(project, session);
		recordFilterPanel.getQueryOptionsPanel().setVisible(false);
		recordFilterPanel.setBorder(BorderFactory.createTitledBorder("Record Filter"));
		
		includeParticipantInfoBox = new JCheckBox("Include participant information");
		includeParticipantInfoBox.addActionListener( (e) -> {
			var enabled = includeParticipantInfoBox.isSelected();
			includeRole.setEnabled(enabled);
			includeAge.setEnabled(enabled);
			includeBirthday.setEnabled(enabled);
			includeSex.setEnabled(enabled);
			includeLanguage.setEnabled(enabled);
			includeGroup.setEnabled(enabled);
			includeEducation.setEnabled(enabled);
			includeSES.setEnabled(enabled);
		});
		includeParticipantInfoBox.setSelected(
				PrefHelper.getBoolean(SessionToHTMLWizard.class.getName() + ".includeParticipantInfo", true));
		
		int pInfoIdx = 0;
		includeRole = new JCheckBox("Role");
		includeRole.setSelected(
				PrefHelper.getBoolean(SessionToHTMLWizard.class.getName() + "." + participantInfoParamIds[pInfoIdx], 
						participantInfoDefaults[pInfoIdx]));
		++pInfoIdx;
		
		includeAge = new JCheckBox("Age");
		includeAge.setSelected(
				PrefHelper.getBoolean(SessionToHTMLWizard.class.getName() + "." + participantInfoParamIds[pInfoIdx], 
						participantInfoDefaults[pInfoIdx]));
		++pInfoIdx;
		
		includeBirthday = new JCheckBox("Birthday");
		includeBirthday.setSelected(
				PrefHelper.getBoolean(SessionToHTMLWizard.class.getName() + "." + participantInfoParamIds[pInfoIdx], 
						participantInfoDefaults[pInfoIdx]));
		++pInfoIdx;
		
		
		includeSex = new JCheckBox("Sex");
		includeSex.setSelected(
				PrefHelper.getBoolean(SessionToHTMLWizard.class.getName() + "." + participantInfoParamIds[pInfoIdx], 
						participantInfoDefaults[pInfoIdx]));
		++pInfoIdx;
		
		includeLanguage = new JCheckBox("Language");
		includeLanguage.setSelected(
				PrefHelper.getBoolean(SessionToHTMLWizard.class.getName() + "." + participantInfoParamIds[pInfoIdx], 
						participantInfoDefaults[pInfoIdx]));
		++pInfoIdx;
		
		includeGroup = new JCheckBox("Group");
		includeGroup.setSelected(
				PrefHelper.getBoolean(SessionToHTMLWizard.class.getName() + "." + participantInfoParamIds[pInfoIdx], 
						participantInfoDefaults[pInfoIdx]));
		++pInfoIdx;
		
		includeEducation = new JCheckBox("Education");
		includeEducation.setSelected(
				PrefHelper.getBoolean(SessionToHTMLWizard.class.getName() + "." + participantInfoParamIds[pInfoIdx], 
						participantInfoDefaults[pInfoIdx]));
		++pInfoIdx;
		
		includeSES = new JCheckBox("SES");
		includeSES.setSelected(
				PrefHelper.getBoolean(SessionToHTMLWizard.class.getName() + "." + participantInfoParamIds[pInfoIdx], 
						participantInfoDefaults[pInfoIdx]));

		JPanel checkBoxPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		checkBoxPanel.add(includeRole);
		checkBoxPanel.add(includeAge);
		checkBoxPanel.add(includeBirthday);
		checkBoxPanel.add(includeSex);
		checkBoxPanel.add(includeLanguage);
		checkBoxPanel.add(includeGroup);
		checkBoxPanel.add(includeEducation);
		checkBoxPanel.add(includeSES);
		
		JPanel participantOptionsPanel = new JPanel(new VerticalLayout());
		participantOptionsPanel.setBorder(BorderFactory.createTitledBorder("Participant Options"));
		participantOptionsPanel.add(includeParticipantInfoBox);
		participantOptionsPanel.add(checkBoxPanel);
		
		tableModel = new TierViewTableModel(tierView);
		tierViewTable = new JXTable(tableModel);
		tierViewTable.setVisibleRowCount(7);
		tierViewTable.setSelectionMode(DefaultListSelectionModel.SINGLE_SELECTION);
		final JScrollPane tableScroller = new JScrollPane(tierViewTable);
		
		final PhonUIAction moveUpAct = new PhonUIAction(this, "moveTierUp");
		moveUpAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Move tier up");
		moveUpAct.putValue(PhonUIAction.SMALL_ICON, IconManager.getInstance().getIcon("actions/go-up", IconSize.SMALL));
		moveTierUpBtn = new JButton(moveUpAct);
		
		final PhonUIAction moveDownAct = new PhonUIAction(this, "moveTierDown");
		moveDownAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Move tier down");
		moveDownAct.putValue(PhonUIAction.SMALL_ICON, IconManager.getInstance().getIcon("actions/go-down", IconSize.SMALL));
		moveTierDownBtn = new JButton(moveDownAct);
		
		JPanel btnPanel = new JPanel(new VerticalLayout());
		btnPanel.add(moveTierUpBtn);
		btnPanel.add(moveTierDownBtn);
		
		JPanel tierTablePanel = new JPanel(new BorderLayout());
		tierTablePanel.add(tableScroller, BorderLayout.CENTER);
		tierTablePanel.add(btnPanel, BorderLayout.EAST);
		
		includeSyllabificationBox = new JCheckBox("Include syllabification");
		includeSyllabificationBox.setSelected(
				PrefHelper.getBoolean(SessionToHTMLWizard.class.getName() + ".includeSyllabification", true));
		
		final IPATranscript ipaT = (new IPATranscriptBuilder()).append("ˈb:Oʌ:Nɹ:Cθ:Cˌd:Oe͜ɪ:N").toIPATranscript();
		final IPATranscript ipaA = (new IPATranscriptBuilder()).append("ˈb:Oʌː:Nˌt:Oe͜ɪ:N").toIPATranscript();
		final PhoneMap alignment = (new PhoneAligner()).calculatePhoneAlignment(ipaT, ipaA);
		
		syllabificationDisplay = new SyllabificationDisplay();
		syllabificationDisplay.setTranscript(ipaT);
		syllabificationDisplay.setFont(FontPreferences.getUIIpaFont());
		
		includeAlignmentBox = new JCheckBox("Include alignment");
		includeAlignmentBox.setSelected(
				PrefHelper.getBoolean(SessionToHTMLWizard.class.getName() + ".includeAlignment", true));
		
		alignmentDisplay = new PhoneMapDisplay();
		alignmentDisplay.setPhoneMapForGroup(0, alignment);
		alignmentDisplay.setBackground(Color.WHITE);
		alignmentDisplay.setFont(FontPreferences.getUIIpaFont());
		
		JPanel tierOptionsPanel = new JPanel(new GridBagLayout());
		tierOptionsPanel.setBorder(BorderFactory.createTitledBorder("Tier Options"));
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridheight = 4;
		gbc.gridwidth = 1;
		
		tierOptionsPanel.add(tierTablePanel, gbc);
		
		gbc.fill = GridBagConstraints.NONE;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.gridheight = 1;
		gbc.insets = new Insets(0, 5, 0, 5);
		tierOptionsPanel.add(includeSyllabificationBox, gbc);
		
		++gbc.gridy;
		gbc.ipadx = 5;
		gbc.insets = new Insets(0, 25, 0, 5);
		tierOptionsPanel.add(syllabificationDisplay, gbc);
		
		++gbc.gridy;
		gbc.insets = new Insets(5, 5, 0, 5);
		tierOptionsPanel.add(includeAlignmentBox, gbc);
		
		++gbc.gridy;
		gbc.insets = new Insets(0, 25, 0, 5);
		tierOptionsPanel.add(alignmentDisplay, gbc);
		
		var resultSetEnabledListener = new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				var enabled = includeQueryResults.isSelected() | filterUsingQueryResults.isSelected();
				resultSetSelector.getResultSetTable().setEnabled(enabled);
				resultSetValuesTable.setEnabled(enabled);
			}
			
		};
		includeQueryResults = new JCheckBox("Include query results");
		includeQueryResults.addActionListener(resultSetEnabledListener);
		includeQueryResults.setSelected(false);
		
		filterUsingQueryResults = new JCheckBox("Filter records using query results");
		filterUsingQueryResults.addActionListener(resultSetEnabledListener);
		filterUsingQueryResults.setSelected(false);
		
		JPanel queryOptionsPanel = new JPanel(new HorizontalLayout(5));
		queryOptionsPanel.add(includeQueryResults);
		queryOptionsPanel.add(filterUsingQueryResults);
		
		resultSetSelector = new OpenResultSetSelector(session);
		resultSetSelector.getResultSetTable().setEnabled(false);
		resultSetSelector.getResultSetTable().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		resultSetSelector.getResultSetTable().getSelectionModel().addListSelectionListener( (e) -> {
			var selectedResultSets = resultSetSelector.getSelectedResultSets();
			if(selectedResultSets.size() > 0) {
				var rs = selectedResultSets.get(0);
				if(rs.numberOfResults(true) > 0) {
					var result = rs.getResult(0);
					
					var rvList = new ArrayList<String>();
					for(var rv:result) {
						rvList.add(rv.getName());
					}
					rvList.addAll(result.getMetadata().keySet());
					
					var tableModel = new ResultSetValuesTableModel(rvList);
					resultSetValuesTable.setModel(tableModel);
				}
			}
		});
		
		var resultSetTableModel = new ResultSetValuesTableModel(new ArrayList<>());
		resultSetValuesTable = new JXTable(resultSetTableModel);
		resultSetValuesTable.setEnabled(false);
		resultSetValuesTable.setVisibleRowCount(5);
		var resultSetTableScroller = new JScrollPane(resultSetValuesTable);
		
		JPanel resultSetPanel = new JPanel(new VerticalLayout());
		resultSetPanel.setBorder(BorderFactory.createTitledBorder("Query Results"));
		resultSetPanel.add(queryOptionsPanel);
		resultSetPanel.add(resultSetSelector);
		resultSetPanel.add(resultSetTableScroller);
		
		JPanel optionsPanel = new JPanel(new VerticalLayout(5));
		optionsPanel.add(recordFilterPanel);
		optionsPanel.add(participantOptionsPanel);
		optionsPanel.add(tierOptionsPanel);
		optionsPanel.add(resultSetPanel);
		
		final TitledPanel optionsTitledPanel = new TitledPanel("Options", new JScrollPane(optionsPanel));
		WizardStep step = new WizardStep();
		step.setTitle("Options");
		step.setLayout(new BorderLayout());
		step.add(optionsTitledPanel, BorderLayout.CENTER);
		
		return step;
	}
	
	private WizardStep createPreviewStep() {
		bufferPanel = new MultiBufferPanel();
		
		final TitledPanel tp = new TitledPanel("Preview", bufferPanel);
		busyLabel = new JXBusyLabel(new Dimension(16, 16));
		tp.setLeftDecoration(busyLabel);
		
		WizardStep retVal = new WizardStep();
		retVal.setTitle("Preview");
		retVal.setLayout(new BorderLayout());
		retVal.add(tp, BorderLayout.CENTER);
		return retVal;
	}
	
	@Override
	public void next() {
		if(getCurrentStep() == optionsStep) {
			busyLabel.setBusy(true);
			
			// save selections
			PrefHelper.getUserPreferences().putBoolean(SessionToHTMLWizard.class.getName() + ".includeParticipantInfo", 
					includeParticipantInfoBox.isSelected());
			int pInfoIdx = 0;
			PrefHelper.getUserPreferences().putBoolean(SessionToHTMLWizard.class.getName() + "." + participantInfoParamIds[pInfoIdx++], 
					includeRole.isSelected());
			PrefHelper.getUserPreferences().putBoolean(SessionToHTMLWizard.class.getName() + "." + participantInfoParamIds[pInfoIdx++], 
					includeAge.isSelected());
			PrefHelper.getUserPreferences().putBoolean(SessionToHTMLWizard.class.getName() + "." + participantInfoParamIds[pInfoIdx++], 
					includeBirthday.isSelected());
			PrefHelper.getUserPreferences().putBoolean(SessionToHTMLWizard.class.getName() + "." + participantInfoParamIds[pInfoIdx++], 
					includeSex.isSelected());
			PrefHelper.getUserPreferences().putBoolean(SessionToHTMLWizard.class.getName() + "." + participantInfoParamIds[pInfoIdx++], 
					includeLanguage.isSelected());
			PrefHelper.getUserPreferences().putBoolean(SessionToHTMLWizard.class.getName() + "." + participantInfoParamIds[pInfoIdx++], 
					includeGroup.isSelected());
			PrefHelper.getUserPreferences().putBoolean(SessionToHTMLWizard.class.getName() + "." + participantInfoParamIds[pInfoIdx++], 
					includeEducation.isSelected());
			PrefHelper.getUserPreferences().putBoolean(SessionToHTMLWizard.class.getName() + "." + participantInfoParamIds[pInfoIdx++], 
					includeSES.isSelected());
			
			PrefHelper.getUserPreferences().putBoolean(SessionToHTMLWizard.class.getName() + ".includeSyllabification", 
					includeSyllabificationBox.isSelected());
			PrefHelper.getUserPreferences().putBoolean(SessionToHTMLWizard.class.getName() + ".includeAlignment", 
					includeAlignmentBox.isSelected());
			
			SessionToHTML converter = new SessionToHTML();
			converter.setRecordFilter(recordFilterPanel.getRecordFilter());
			
			converter.setIncludeParticipantInfo(includeParticipantInfoBox.isSelected());
			converter.setIncludeRole(includeRole.isSelected());
			converter.setIncludeAge(includeAge.isSelected());
			converter.setIncludeBirthday(includeBirthday.isSelected());
			converter.setIncludeSex(includeSex.isSelected());
			converter.setIncludeLanguage(includeLanguage.isSelected());
			converter.setIncludeGroup(includeGroup.isSelected());
			converter.setIncludeEducation(includeEducation.isSelected());
			converter.setIncludeSES(includeSES.isSelected());
			
			converter.setTierView(((TierViewTableModel)tierViewTable.getModel()).tierView);
			converter.setIncludeSyllabification(includeSyllabificationBox.isSelected());
			converter.setIncludeAlignment(includeAlignmentBox.isSelected());

			List<ResultSet> selectedResults = resultSetSelector.getSelectedResultSets();
			ResultSet rs = null;
			if(selectedResults.size() > 0) {
				converter.setIncludeQueryResults(includeQueryResults.isSelected());
				converter.setFilterRecordsUsingQueryResults(filterUsingQueryResults.isSelected());
				
				rs = selectedResults.get(0);
				
				ResultSetValuesTableModel tableModel = (ResultSetValuesTableModel)resultSetValuesTable.getModel();
				var rvList = 
						tableModel.resultValueList.stream()
							.filter( (rvName) -> tableModel.resultValueVisible.get(rvName) )
							.collect( Collectors.toList() );
				converter.setResultValues(rvList);
				converter.setExcludeResultValues(false);
			}
			
			ExportWorker worker = new ExportWorker(converter, rs);
			worker.execute();
		}
		super.next();
	}
	
	private class ExportWorker extends SwingWorker<File, Object> {
		
		private SessionToHTML converter;
		
		private ResultSet resultSet;
		
		public ExportWorker(SessionToHTML converter, ResultSet rs) {
			this.converter = converter;
			this.resultSet = rs;
		}

		@Override
		protected File doInBackground() throws Exception {
			final String html = converter.toHTML(session, resultSet);

			final File tempFile = File.createTempFile("phon", ".html");
			try (final PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(tempFile)))) {
				writer.write(html);
				writer.flush();
			} catch (IOException e) {
				LogUtil.severe(e.getLocalizedMessage(), e);
			}
			
			return tempFile;
		}

		@Override
		public void done() {
			try {
				File htmlFile = get();
				
				final String bufferName = session.getCorpus() + "." + session.getName();
				final BufferPanel buffer = bufferPanel.createBuffer(bufferName);
				buffer.showHtml(false);
				buffer.getBrowser().loadURL(htmlFile.toURI().toURL().toString());
			} catch (InterruptedException | ExecutionException | MalformedURLException e) {
				LogUtil.severe(e.getLocalizedMessage(), e);
			}
			busyLabel.setBusy(false);
		}
		
	}
	
	private class ResultSetValuesTableModel extends AbstractTableModel {
		
		private List<String> resultValueList;
		
		private Map<String, Boolean> resultValueVisible;
		
		public ResultSetValuesTableModel(List<String> rvList) {
			super();
			
			resultValueList = rvList;
			resultValueVisible = new HashMap<>();
			for(String rvName:rvList) resultValueVisible.put(rvName, true);
		}

		@Override
		public int getRowCount() {
			return resultValueList.size();
		}

		@Override
		public int getColumnCount() {
			return 2;
		}
		
		@Override
		public String getColumnName(int column) {
			switch(column) {
			case 0:
				return "Visible";
			
			case 1:
				return "Tier";
				
			default:
				return "";
			}
		}
		
		@Override
		public Class<?> getColumnClass(int columnIndex) {
			switch(columnIndex) {
			case 0:
				return Boolean.class;
				
			case 1:
				return String.class;
				
			default:
				return String.class;
			}
		}

		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return columnIndex == 0;
		}

		@Override
		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
			var rvName = resultValueList.get(rowIndex);
			resultValueVisible.put(rvName, (Boolean)aValue);
			fireTableCellUpdated(rowIndex, columnIndex);
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			String rvName = resultValueList.get(rowIndex);
			switch(columnIndex) {
			case 0:
				return resultValueVisible.get(rvName);
				
			case 1:
				return rvName;
				
			default:
				return "";
			}
		}
		
	}
	
	private class TierViewTableModel extends AbstractTableModel {
		
		private List<TierViewItem> tierView;
		
		public TierViewTableModel() {
			this(new ArrayList<TierViewItem>());
		}
		
		public TierViewTableModel(List<TierViewItem> tierView) {
			this.tierView = tierView;
		}
		
		public void setTierView(List<TierViewItem> tierView) {
			this.tierView = tierView;
			super.fireTableDataChanged();
		}

		public List<TierViewItem> getTierView() {
			return this.tierView;
		}
		
		@Override
		public int getRowCount() {
			return tierView.size();
		}

		@Override
		public int getColumnCount() {
			return 3;
		}
		
		@Override
		public String getColumnName(int column) {
			switch(column) {
			case 0:
				return "Visible";
				
			case 1:
				return "Tier Name";
				
			case 2:
				return "Font";
				
			default:
				return super.getColumnName(column);
			}
		}

		@Override
		public Class<?> getColumnClass(int columnIndex) {
			switch(columnIndex) {
			case 0:
				return Boolean.class;
				
			default:
				return super.getColumnClass(columnIndex);
			}
		}

		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return columnIndex == 0;
		}

		@Override
		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
			final TierViewItem tvi = tierView.remove(rowIndex);
			final TierViewItem newItem = SessionFactory.newFactory().createTierViewItem(tvi.getTierName(), Boolean.valueOf(aValue.toString()), tvi.getTierFont());
			tierView.add(rowIndex, newItem);
			super.fireTableCellUpdated(rowIndex, columnIndex);
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			final TierViewItem tierViewItem = tierView.get(rowIndex);
			
			switch(columnIndex) {
			case 0:
				return tierViewItem.isVisible();
				
			case 1:
				return tierViewItem.getTierName();
				
			case 2:
				return tierViewItem.getTierFont();
				
			default:
				return "";
			}
		}
		
	}

}
