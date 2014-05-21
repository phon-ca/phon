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
package ca.phon.csv2phon.wizard;

import java.awt.BorderLayout;
import java.io.File;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.PropertyException;
import javax.xml.bind.Unmarshaller;

import ca.phon.application.PhonWorker;
import ca.phon.csv2phon.CSVImporter;
import ca.phon.csv2phon.io.ImportDescriptionType;
import ca.phon.csv2phon.io.ObjectFactory;
import ca.phon.gui.DialogHeader;
import ca.phon.gui.components.PhonLoggerConsole;
import ca.phon.gui.wizard.WizardFrame;
import ca.phon.gui.wizard.WizardStep;
import ca.phon.system.logger.PhonLogger;

/**
 * CSV import wizard.
 *
 */
public class CSVImportWizard extends WizardFrame {

	/** CSV Import settings */
	private ImportDescriptionType importDescription;
	
	/** Steps */
	private DirectoryStep dirStep;
	private SessionInfoStep sessionInfoStep;
	private ParticipantsStep partsStep;
	private ColumnMapStep columnMapStep;
	private WizardStep importStep;
	
	private PhonLoggerConsole console;
	
	private final String settingsFileName = "importsettings.xml";
	
	public CSVImportWizard() {
		super("CSV Import");
		
		setWindowName("CSV Import");
		super.btnFinish.setVisible(false);
		super.btnCancel.setText("Close");
		
		init();
	}
	
	private void init() {
		ObjectFactory factory = new ObjectFactory();
		importDescription = factory.createImportDescriptionType();
		
		dirStep = new DirectoryStep();
		dirStep.setPrevStep(-1);
		dirStep.setNextStep(1);
		addWizardStep(dirStep);
		
		sessionInfoStep = new SessionInfoStep();
		sessionInfoStep.setPrevStep(0);
		sessionInfoStep.setNextStep(2);
		addWizardStep(sessionInfoStep);
		
		partsStep = new ParticipantsStep();
		partsStep.setPrevStep(1);
		partsStep.setNextStep(3);
		addWizardStep(partsStep);
		
		columnMapStep = new ColumnMapStep();
		columnMapStep.setPrevStep(2);
		columnMapStep.setNextStep(4);
		addWizardStep(columnMapStep);
		
		importStep = createImportStep();
		importStep.setPrevStep(3);
		importStep.setNextStep(-1);
	}
	
	private WizardStep createImportStep() {
		JPanel importPanel = new JPanel(new BorderLayout());
		
		DialogHeader importHeader = new DialogHeader("CSV Import", "Importing data.");
		importPanel.add(importHeader, BorderLayout.NORTH);
		
		JPanel consolePanel = new JPanel(new BorderLayout());
//		consolePanel.setBorder(BorderFactory.createTitledBorder("Console"));
		
		console = new PhonLoggerConsole();
		consolePanel.add(console, BorderLayout.CENTER);
		
		importPanel.add(consolePanel, BorderLayout.CENTER);
		
		return super.addWizardStep(importPanel);
	}
	
	private void startImport() {
		PhonWorker worker = PhonWorker.createWorker();
		worker.setName("CSV Importer");
		worker.setFinishWhenQueueEmpty(true);

		console.addReportThread(worker);
		
		
		Runnable r = new Runnable() {
			@Override
			public void run() {
				Runnable turnOffBack = new Runnable() {
					@Override
					public void run() {
						btnBack.setEnabled(false);
						btnCancel.setEnabled(false);
						showBusyLabel(console);
					}
				};
				Runnable turnOnBack = new Runnable() {
					@Override
					public void run() {
						btnBack.setEnabled(true);
						btnCancel.setEnabled(true);
						stopBusyLabel();
					}
				};
				SwingUtilities.invokeLater(turnOffBack);
				
				console.startLogging();
				saveSettings();
				CSVImporter importer =
					new CSVImporter(dirStep.getBase(), importDescription, getProject());
				importer.setFileEncoding(dirStep.getFileEncoding());
				importer.performImport();
				console.stopLogging();
				
				console.removeReportThread(Thread.currentThread());
				
				SwingUtilities.invokeLater(turnOnBack);
			}
		};
		
		
		worker.invokeLater(r);
		worker.start();
	}
	
	private void saveSettings() {
		// write settings to file
		try {
			File settingsFile = new File(dirStep.getBase(), "importsettings.xml");
			PhonLogger.info("Saving settings to file '.../" + settingsFile.getName() + "'");
			
			JAXBContext ctx = JAXBContext.newInstance(ObjectFactory.class);
			Marshaller marshaller = ctx.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			
			JAXBElement<ImportDescriptionType> jaxbEle = 
				(new ObjectFactory()).createCsvimport(importDescription);
			marshaller.marshal(jaxbEle, settingsFile);
			
		} catch (PropertyException e) {
			e.printStackTrace();
		} catch (JAXBException e) {
			e.printStackTrace();
		}
	}
	
	
	@Override
	protected void next() {
		if(super.getCurrentStep() == dirStep) {
			sessionInfoStep.setBase(dirStep.getBase());
			
			
			ObjectFactory factory = new ObjectFactory();
			
			// check for previous settings
			File settingsFile = new File(dirStep.getBase(), settingsFileName);
			if(settingsFile.exists()) {
				// read settings from file
				try {
					JAXBContext ctx = JAXBContext.newInstance(factory.getClass());
					Unmarshaller unmarshaller = ctx.createUnmarshaller();
					
					JAXBElement<ImportDescriptionType> jaxbEle = 
						(JAXBElement<ImportDescriptionType>)unmarshaller.unmarshal(settingsFile);
					importDescription = jaxbEle.getValue();
				} catch (JAXBException e) {
					e.printStackTrace();
					importDescription = factory.createImportDescriptionType();
				}
			} else {
				// create new empty settings

				importDescription = factory.createImportDescriptionType();
			}
			
			sessionInfoStep.setSettings(importDescription);
		} else if(super.getCurrentStep() == sessionInfoStep) {
			columnMapStep.setBase(dirStep.getBase());
			columnMapStep.setSettings(importDescription);
			
			partsStep.setSettings(importDescription);
		} else if(super.getCurrentStep() == columnMapStep) {
			// start import
			startImport();
		}
		super.next();
	}
	
}
