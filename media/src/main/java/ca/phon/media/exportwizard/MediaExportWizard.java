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

package ca.phon.media.exportwizard;

import java.awt.BorderLayout;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JPanel;

import ca.phon.application.PhonWorker;
import ca.phon.gui.DialogHeader;
import ca.phon.gui.components.PhonLoggerConsole;
import ca.phon.gui.wizard.WizardFrame;
import ca.phon.gui.wizard.WizardStep;
import ca.phon.media.FFMpegMediaExporter;
import ca.phon.util.StringUtils;

/**
 * Wizard for exporting a segment of media.
 */
public class MediaExportWizard extends WizardFrame {

	private Map<MediaExportWizardProp, Object> wizardProps;
;
	
	/* Steps */
	private ExportSetupStep setupStep;

	private PhonLoggerConsole exportConsole;
	private WizardStep exportStep;

	public MediaExportWizard() {
		super("Export Media");

		wizardProps = new HashMap<MediaExportWizardProp, Object>();

		init();
	}

	public MediaExportWizard(Map<MediaExportWizardProp, Object> props) {
		super("Export Media");

		wizardProps = props;

		init();
	}

	private void init() {
		
		super.btnCancel.setText("Close");
		super.btnFinish.setVisible(false);
		
		setupStep = new ExportSetupStep(wizardProps);
		setupStep.setNextStep(1);
		addWizardStep(setupStep);

		exportConsole = new PhonLoggerConsole();

		JPanel exportPanel = new JPanel(new BorderLayout());
		DialogHeader exportHeader = new DialogHeader("Export media", "Export media using ffmpeg");
		exportPanel.add(exportHeader, BorderLayout.NORTH);
		exportPanel.add(exportConsole, BorderLayout.CENTER);

		exportStep = super.addWizardStep(exportPanel);
		exportStep.setPrevStep(0);
	}

	@Override
	public void next() {
		if(getCurrentStep() == setupStep) {
			// create exporter(s) and start thread
			FFMpegMediaExporter exporter =
					new FFMpegMediaExporter();
			exporter.setInputFile(setupStep.getInputFileLabel().getFile().getAbsolutePath());
			exporter.setOutputFile(setupStep.getOutputFileLabel().getFile().getAbsolutePath());

			exporter.setIncludeVideo(setupStep.getEncodeVideoBox().isSelected());
			exporter.setVideoCodec(setupStep.getVideoCodecField().getText());

			exporter.setIncludeAudio(setupStep.getEncodeAudioBox().isSelected());
			exporter.setAudioCodec(setupStep.getAudioCodecField().getText());

			if(setupStep.getPartialExtractBox().isSelected()) {
				String segment = setupStep.getSegmentField().getText();

				String vals[] = segment.split("-");
				long startTime = StringUtils.msFromDisplayString(vals[0]);
				long endTime = StringUtils.msFromDisplayString(vals[1]);
				long duration = (endTime-startTime);

				if(duration > 0L) {
					exporter.setStartTime(startTime);
					exporter.setDuration(duration);
				}
			}
			
			exporter.setOtherArgs(setupStep.getOtherArgsField().getText());

			PhonWorker worker = PhonWorker.createWorker();
			worker.setFinishWhenQueueEmpty(true);

			exportConsole.addReportThread(worker);
			exportConsole.startLogging();
			
			worker.invokeLater(exporter);
			worker.start();

			super.next();
		}
	}
	
}
