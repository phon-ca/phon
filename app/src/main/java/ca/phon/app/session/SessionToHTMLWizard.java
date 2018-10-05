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
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.BorderFactory;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingWorker;
import javax.swing.table.AbstractTableModel;

import org.jdesktop.swingx.JXBusyLabel;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.VerticalLayout;

import ca.phon.app.log.BufferPanel;
import ca.phon.app.log.LogUtil;
import ca.phon.app.log.MultiBufferPanel;
import ca.phon.ipa.IPATranscript;
import ca.phon.ipa.IPATranscriptBuilder;
import ca.phon.ipa.alignment.PhoneAligner;
import ca.phon.ipa.alignment.PhoneMap;
import ca.phon.session.Session;
import ca.phon.session.SessionFactory;
import ca.phon.session.SessionPath;
import ca.phon.session.TierViewItem;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.ui.decorations.TitledPanel;
import ca.phon.ui.fonts.FontPreferences;
import ca.phon.ui.ipa.PhoneMapDisplay;
import ca.phon.ui.ipa.SyllabificationDisplay;
import ca.phon.ui.wizard.BreadcrumbWizardFrame;
import ca.phon.ui.wizard.WizardStep;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;
import javafx.application.Platform;
import javafx.scene.web.WebView;

public class SessionToHTMLWizard extends BreadcrumbWizardFrame {

	private Session session;
	
	private WizardStep optionsStep;
	private JCheckBox includeParticipantInfoBox;
	private TierViewTableModel tableModel;
	private JXTable tierViewTable;
	private JButton moveTierUpBtn;
	private JButton moveTierDownBtn;
	private JCheckBox includeSyllabificationBox;
	private SyllabificationDisplay syllabificationDisplay;
	private JCheckBox includeAlignmentBox;
	private PhoneMapDisplay alignmentDisplay;
	
	private WizardStep reportStep;
	private MultiBufferPanel bufferPanel;
	private JXBusyLabel busyLabel;
	
	public SessionToHTMLWizard(String title, Session session) {
		super(title);
		setWindowName("Session to HTML : " + session.getCorpus() + "." + session.getName());
		
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
		
		includeParticipantInfoBox = new JCheckBox("Include participant information");
		includeParticipantInfoBox.setSelected(true);
				
		tableModel = new TierViewTableModel(tierView);
		tierViewTable = new JXTable(tableModel);
		tierViewTable.setVisibleRowCount(8);
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
		
		JPanel tierViewPanel = new JPanel(new BorderLayout());
		tierViewPanel.add(tableScroller, BorderLayout.CENTER);
		tierViewPanel.add(btnPanel, BorderLayout.EAST);
		tierViewPanel.setBorder(BorderFactory.createTitledBorder("Tiers"));
		
		includeSyllabificationBox = new JCheckBox("Include syllabification");
		includeSyllabificationBox.setSelected(true);
		
		final IPATranscript ipaT = (new IPATranscriptBuilder()).append("ˈb:Oʌ:Nɹ:Cθ:Cˌd:Oe͜ɪ:N").toIPATranscript();
		final IPATranscript ipaA = (new IPATranscriptBuilder()).append("ˈb:Oʌː:Nˌt:Oe͜ɪ:N").toIPATranscript();
		final PhoneMap alignment = (new PhoneAligner()).calculatePhoneAlignment(ipaT, ipaA);
		
		syllabificationDisplay = new SyllabificationDisplay();
		syllabificationDisplay.setTranscript(ipaT);
		syllabificationDisplay.setFont(FontPreferences.getUIIpaFont());
		syllabificationDisplay.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));
		
		includeAlignmentBox = new JCheckBox("Include alignment");
		includeAlignmentBox.setSelected(true);
		
		alignmentDisplay = new PhoneMapDisplay();
		alignmentDisplay.setPhoneMapForGroup(0, alignment);
		alignmentDisplay.setBackground(Color.WHITE);
		alignmentDisplay.setFont(FontPreferences.getUIIpaFont());
		alignmentDisplay.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));
		
		JPanel optionsPanel = new JPanel(new VerticalLayout());
		optionsPanel.add(includeParticipantInfoBox);
		optionsPanel.add(tierViewPanel);
		optionsPanel.add(includeSyllabificationBox);
		optionsPanel.add(syllabificationDisplay);
		optionsPanel.add(includeAlignmentBox);
		optionsPanel.add(alignmentDisplay);
		
		final TitledPanel optionsTitledPanel = new TitledPanel("Options", optionsPanel);
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
			
			SessionToHTML converter = new SessionToHTML();
			converter.setIncludeAlignment(includeAlignmentBox.isSelected());
			converter.setIncludeSyllabification(includeSyllabificationBox.isSelected());
			converter.setIncludeParticipantInfo(includeParticipantInfoBox.isSelected());
			converter.setTierView(((TierViewTableModel)tierViewTable.getModel()).tierView);
			
			ExportWorker worker = new ExportWorker(converter);
			worker.execute();
		}
		super.next();
	}
	
	private class ExportWorker extends SwingWorker<File, Object> {
		
		private SessionToHTML converter;
		
		public ExportWorker(SessionToHTML converter) {
			this.converter = converter;
		}

		@Override
		protected File doInBackground() throws Exception {
			final String html = converter.toHTML(session);

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
				final WebView webView = buffer.getWebView();
				Platform.runLater( () -> {
					webView.getEngine().load(htmlFile.toURI().toString());
				});
				buffer.showHtml(false);
			} catch (InterruptedException | ExecutionException e) {
				LogUtil.severe(e.getLocalizedMessage(), e);
			}
			busyLabel.setBusy(false);
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
