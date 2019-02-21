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
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
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
import ca.phon.app.log.ExcelExporter;
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
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

public class SessionToHTMLWizard extends BreadcrumbWizardFrame {

	private Project project;
	
	private Session session;
	
	private WizardStep optionsStep;
	private SessionExportSettingsPanel optionsPanel;
	
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
	
	private WizardStep createOptionsStep() {
		optionsPanel = new SessionExportSettingsPanel(project, session, new SessionToHTML.SessionToHTMLSettings());
		
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
						
			SessionToHTML converter = new SessionToHTML((SessionToHTML.SessionToHTMLSettings)optionsPanel.getSettings());
			converter.getSettings().saveAsDefaults();
			
			ExportWorker worker = new ExportWorker(converter);
			worker.execute();
		}
		super.next();
	}
	
	private class ExportWorker extends SwingWorker<File, Object> {
		
		private final SessionToHTML converter;
		
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
				buffer.putExtension(ExcelExporter.class, (wb) -> {
					SessionToExcel toExcel = new SessionToExcel(converter.getSettings());
					toExcel.createSheetInWorkbook(wb, session);
				});
				buffer.showHtml(false);
				buffer.getBrowser().loadURL(htmlFile.toURI().toURL().toString());
			} catch (InterruptedException | ExecutionException | MalformedURLException e) {
				LogUtil.severe(e.getLocalizedMessage(), e);
			}
			busyLabel.setBusy(false);
		}
		
	}
	
	
	
	

}
