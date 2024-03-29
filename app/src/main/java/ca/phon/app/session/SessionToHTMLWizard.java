/*
 * Copyright (C) 2005-2020 Gregory Hedlund & Yvan Rose
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

import ca.phon.app.log.*;
import ca.phon.project.Project;
import ca.phon.session.Session;
import ca.phon.ui.decorations.TitledPanel;
import ca.phon.ui.wizard.*;
import org.cef.browser.CefBrowser;
import org.cef.handler.CefLoadHandlerAdapter;
import org.jdesktop.swingx.JXBusyLabel;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.MalformedURLException;
import java.util.concurrent.ExecutionException;

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
						
			SessionToHTML.SessionToHTMLSettings settings = (SessionToHTML.SessionToHTMLSettings)optionsPanel.getSettings();
			settings.setIncludeLinks(false);
			SessionToHTML converter = new SessionToHTML(settings);
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
				buffer.addBrowserLoadHandler(new CefLoadHandlerAdapter() {
					@Override
					public void onLoadingStateChange(CefBrowser browser, boolean isLoading, boolean canGoBack, boolean canGoForward) {
						if(!isLoading) {
							SwingUtilities.invokeLater(() -> {
								buffer.removeBrowserLoadHandler(this);
								buffer.getBrowser().loadURL(htmlFile.toURI().toString());
							});
						}
					}
				});
			} catch (InterruptedException | ExecutionException e) {
				LogUtil.severe(e.getLocalizedMessage(), e);
			}
			busyLabel.setBusy(false);
		}
		
	}
	
	
	
	

}
