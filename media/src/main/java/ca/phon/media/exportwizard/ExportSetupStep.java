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
import java.io.File;
import java.text.ParseException;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.text.MaskFormatter;

import ca.phon.ui.JFileLabel;
import ca.phon.ui.action.PhonActionEvent;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.ui.decorations.DialogHeader;
import ca.phon.ui.nativedialogs.FileFilter;
import ca.phon.ui.nativedialogs.NativeDialogs;
import ca.phon.ui.nativedialogs.OpenDialogProperties;
import ca.phon.ui.nativedialogs.SaveDialogProperties;
import ca.phon.ui.toast.Toast;
import ca.phon.ui.toast.ToastFactory;
import ca.phon.ui.wizard.WizardStep;
import ca.phon.util.MsFormatter;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * Wizard step for selecting input/output files
 * and extraction times.
 */
public class ExportSetupStep extends WizardStep {
	
	private static final long serialVersionUID = -4429583978372058763L;

	private static final Logger LOGGER = Logger
			.getLogger(ExportSetupStep.class.getName());

	// wizard props
	private Map< MediaExportWizardProp, Object> props;

	/* UI */
	private DialogHeader header;

	private JFileLabel inputFileLabel;
	private JButton inputBrowseBtn;

	private JFileLabel outputFileLabel;
	private JButton outputBrowseBtn;

	private JCheckBox encodeAudioBox;
	private JTextField audioCodecField;
	
	private JCheckBox encodeVideoBox;
	private JTextField videoCodecField;
	
	private JTextArea otherArgsField;

	private JCheckBox partialExtractBox;

//	private JRadioButton partialExtractRecordBtn;
//	private JTextField recordsField;

//	private JRadioButton partialExtractTimeBtn;
	private JFormattedTextField segmentField;
	
	private static final String OTHER_ARGS_DEFAULT_TEXT = 
			"# For more information please see http://www.ffmpeg.org\n"
			+ "\n"
			+ "# Enter one argument pair per line\n"
			+ "# Lines starting with '#' are ignored\n";

	public ExportSetupStep(Map< MediaExportWizardProp, Object> props) {
		super();

		this.props = props;
		init();
	}

	private void init() {
		setLayout(new BorderLayout());

		header = new DialogHeader("Export media",
				"Export media segment using ffmpeg");
		add(header, BorderLayout.NORTH);

		// setup top panel
		FormLayout topLayout = new FormLayout(
				"left:100px, 3dlu, fill:pref:grow, pref",
				"pref, pref, pref, pref");
		CellConstraints cc = new CellConstraints();

		JPanel topPanel = new JPanel(topLayout);

		topPanel.add(new JLabel("Input file:"), cc.xy(1, 1));
		topPanel.add(getInputFileLabel(), cc.xy(3, 1));
		topPanel.add(getInputBrowseButton(), cc.xy(4, 1));

		topPanel.add(new JLabel("Output file:"), cc.xy(1, 2));
		topPanel.add(getOutputFileLabel(), cc.xy(3, 2));
		topPanel.add(getOutputBrowseButton(), cc.xy(4, 2));

		topPanel.add(getPartialExtractBox(), cc.xyw(1, 3, 3));

//		topPanel.add(getExtractRecordsButton(), cc.xy(1, 4));
//		topPanel.add(getRecordsField(), cc.xy(3, 4));

		topPanel.add(new JLabel("Segment"), cc.xy(1, 4));
		topPanel.add(getSegmentField(), cc.xy(3, 4));

		// setup bottom panel
		FormLayout btmLayout = new FormLayout(
				"left:100px, 3dlu, fill:pref:grow",
				"pref, pref, pref, pref, pref, fill:pref:grow");

		JPanel btmPanel = new JPanel(btmLayout);
		btmPanel.setBorder(BorderFactory.createTitledBorder("Advanced options"));

		btmPanel.add(getEncodeVideoBox(), cc.xyw(1, 1, 3));
		btmPanel.add(new JLabel("Video codec:"), cc.xy(1, 2));
		btmPanel.add(getVideoCodecField(), cc.xy(3, 2));

		btmPanel.add(getEncodeAudioBox(), cc.xyw(1, 3, 3));
		btmPanel.add(new JLabel("Audio codec:"), cc.xy(1, 4));
		btmPanel.add(getAudioCodecField(), cc.xy(3, 4));

		btmPanel.add(new JLabel("Other arguments:"), cc.xy(1, 5));
		btmPanel.add(getOtherArgsField(), cc.xyw(1, 6, 3));

		JPanel centerPanel = new JPanel();
		centerPanel.setLayout(new BorderLayout());
		centerPanel.add(topPanel, BorderLayout.NORTH);
		centerPanel.add(btmPanel, BorderLayout.CENTER);

		add(centerPanel, BorderLayout.CENTER);

		// setup actions
		ImageIcon brwseIcn =
				IconManager.getInstance().getIcon("actions/document-open", IconSize.SMALL);

		PhonUIAction browseForInputAct =
				new PhonUIAction(this, "onBrowseForInput");
		browseForInputAct.putValue(Action.SMALL_ICON, brwseIcn);
		browseForInputAct.putValue(Action.SHORT_DESCRIPTION, "Browse for media...");

		PhonUIAction showSaveDialogAct =
				new PhonUIAction(this, "onShowSaveDialog");
		showSaveDialogAct.putValue(Action.SMALL_ICON, brwseIcn);
		showSaveDialogAct.putValue(Action.SHORT_DESCRIPTION, "Save media as...");

		PhonUIAction togglePartialExtractAct =
				new PhonUIAction(this, "onTogglePartialExtract");
		togglePartialExtractAct.putValue(Action.NAME, "Extract segment");

		PhonUIAction toggleIncludeVideoAct =
				new PhonUIAction(this, "onToggleIncludeVideo");
		toggleIncludeVideoAct.putValue(Action.NAME, "Include video");

		PhonUIAction toggleIncludeAudioAct =
				new PhonUIAction(this, "onToggleIncludeAudio");
		toggleIncludeAudioAct.putValue(Action.NAME, "Include audio");

		PhonUIAction onRecordSelectionSwitch =
				new PhonUIAction(this, "onPartialExtractRecord");
		onRecordSelectionSwitch.putValue(Action.NAME, "Record(s)");

		PhonUIAction onTimeSelectionSwitch =
				new PhonUIAction(this, "onPartialExtractTime");
		onTimeSelectionSwitch.putValue(Action.NAME, "Segment");

		getInputBrowseButton().setAction(browseForInputAct);

		getOutputBrowseButton().setAction(showSaveDialogAct);

		getPartialExtractBox().setAction(togglePartialExtractAct);

//		getExtractRecordsButton().setAction(onRecordSelectionSwitch);

//		getExtractTimeButton().setAction(onTimeSelectionSwitch);

		getEncodeVideoBox().setAction(toggleIncludeVideoAct);

		getEncodeAudioBox().setAction(toggleIncludeAudioAct);

//		ButtonGroup btnGrp = new ButtonGroup();
//		btnGrp.add(getExtractRecordsButton());
//		btnGrp.add(getExtractTimeButton());

		// check to see if we have a session
//		if(props.get(MediaExportWizardProp.SESSION) == null) {
//			// disable the record extraction selection
//			getExtractRecordsButton().setEnabled(false);
//			getRecordsField().setEnabled(false);
//
//			getExtractTimeButton().setSelected(true);
//		}

		// set values based on wizard props
		if(props.get(MediaExportWizardProp.INPUT_FILE) != null) {
			String inputFile =
					(String)props.get(MediaExportWizardProp.INPUT_FILE);
			getInputFileLabel().setFile(new File(inputFile));
		}

		if(props.get(MediaExportWizardProp.OUTPUT_FILE) != null) {
			String outputFile =
					(String)props.get(MediaExportWizardProp.OUTPUT_FILE);
			getOutputFileLabel().setFile(new File(outputFile));
		}

		boolean isAllowPartialExtract = true;
		if(props.get(MediaExportWizardProp.ALLOW_PARTIAL_EXTRACT) != null) {
			isAllowPartialExtract =
					(Boolean)props.get(MediaExportWizardProp.ALLOW_PARTIAL_EXTRACT);
		}
		if(!isAllowPartialExtract) {
			// disable segment extraction components
			getPartialExtractBox().setSelected(false);
			getPartialExtractBox().setEnabled(false);
		} else {
			boolean isPartialExtract = false;
			if(props.get(MediaExportWizardProp.IS_PARTICAL_EXTRACT) != null) {
				isPartialExtract =
						 (Boolean)props.get(MediaExportWizardProp.IS_PARTICAL_EXTRACT);
			}
			if(isPartialExtract) {
				getPartialExtractBox().setSelected(true);
				getSegmentField().setEnabled(true);
			} else {
				getPartialExtractBox().setSelected(false);
				getSegmentField().setEnabled(false);
			}

			if(props.get(MediaExportWizardProp.PARTIAL_EXTRACT_SEGMENT_START) != null
					&& props.get(MediaExportWizardProp.PARTIAL_EXTRACT_SEGMENT_DURATION) != null) {
				long startTime =
						(Long)props.get(MediaExportWizardProp.PARTIAL_EXTRACT_SEGMENT_START);
				long duration =
						(Long)props.get(MediaExportWizardProp.PARTIAL_EXTRACT_SEGMENT_DURATION);
				String timeStr =
						MsFormatter.msToDisplayString(startTime) +
						"-" +
						MsFormatter.msToDisplayString(startTime+duration);
				getSegmentField().setText(timeStr);
			}
		}
		

		boolean encodeVideo = true;
		if(props.get(MediaExportWizardProp.ENCODE_VIDEO) != null) {
			encodeVideo =
					(Boolean)props.get(MediaExportWizardProp.ENCODE_VIDEO);
		}
		getEncodeVideoBox().setSelected(encodeVideo);
		getVideoCodecField().setEnabled(encodeVideo);

		String videoCodec = "copy";
		if(props.get(MediaExportWizardProp.VIDEO_CODEC) != null) {
			videoCodec =
					(String)props.get(MediaExportWizardProp.VIDEO_CODEC);
		}
		getVideoCodecField().setText(videoCodec);

		boolean encodeAudio = true;
		if(props.get(MediaExportWizardProp.ENCODE_AUDIO) != null) {
			encodeAudio =
					(Boolean)props.get(MediaExportWizardProp.ENCODE_AUDIO);
		}
		getEncodeAudioBox().setSelected(encodeAudio);
		getAudioCodecField().setEnabled(encodeAudio);

		String audioCodec = "copy";
		if(props.get(MediaExportWizardProp.AUDIO_CODEC) != null) {
			audioCodec =
					(String)props.get(MediaExportWizardProp.AUDIO_CODEC);
		}
		getAudioCodecField().setText(audioCodec);

		if(props.get(MediaExportWizardProp.OTHER_ARGS) != null) {
			String otherArgs =
					(String)props.get(MediaExportWizardProp.OTHER_ARGS);
			getOtherArgsField().setText(otherArgs);
		}
	}

	public JFileLabel getInputFileLabel() {
		JFileLabel retVal = inputFileLabel;
		if(retVal == null) {
			retVal = new JFileLabel();
			retVal.setShowNameOnly(false);
			retVal.setMaxChars(45);
			inputFileLabel = retVal;
		}
		return retVal;
	}

	public JButton getInputBrowseButton() {
		JButton retVal = inputBrowseBtn;
		if(retVal == null) {
			retVal = new JButton();
			inputBrowseBtn = retVal;
		}
		return retVal;
	}

	public JFileLabel getOutputFileLabel() {
		JFileLabel retVal = outputFileLabel;
		if(retVal == null) {
			retVal = new JFileLabel();
			retVal.setShowNameOnly(false);
			retVal.setMaxChars(45);
			outputFileLabel = retVal;
		}
		return retVal;
	}

	public JButton getOutputBrowseButton() {
		JButton retVal = this.outputBrowseBtn;
		if(retVal == null) {
			retVal = new JButton();
			outputBrowseBtn = retVal;
		}
		return retVal;
	}

	public JCheckBox getEncodeAudioBox() {
		JCheckBox retVal = encodeAudioBox;
		if(retVal == null) {
			retVal = new JCheckBox("Include audio");
			encodeAudioBox = retVal;
		}
		return retVal;
	}

	public JTextField getAudioCodecField() {
		JTextField retVal = audioCodecField;
		if(retVal == null) {
			retVal = new JTextField();
			audioCodecField = retVal;
		}
		return retVal;
	}

	public JCheckBox getEncodeVideoBox() {
		JCheckBox retVal = encodeVideoBox;
		if(retVal == null) {
			retVal = new JCheckBox("Include video");
			encodeVideoBox = retVal;
		}
		return retVal;
	}

	public JTextField getVideoCodecField() {
		JTextField retVal = videoCodecField;
		if(retVal == null) {
			retVal = new JTextField();
			videoCodecField = retVal;
		}
		return retVal;
	}

	public JCheckBox getPartialExtractBox() {
		JCheckBox retVal = partialExtractBox;
		if(retVal == null) {
			retVal = new JCheckBox("Extract segment");
			partialExtractBox = retVal;
		}
		return retVal;
	}

//	public JRadioButton getExtractRecordsButton() {
//		JRadioButton retVal = partialExtractRecordBtn;
//		if(retVal == null) {
//			retVal = new JRadioButton("Record(s)");
//			partialExtractRecordBtn = retVal;
//		}
//		return retVal;
//	}
//
//	public JTextField getRecordsField() {
//		JTextField retVal = recordsField;
//		if(retVal == null) {
//			retVal = new JTextField();
//			recordsField = retVal;
//		}
//		return retVal;
//	}

//	public JRadioButton getExtractTimeButton() {
//		JRadioButton retVal = partialExtractTimeBtn;
//		if(retVal == null) {
//			retVal = new JRadioButton("Segment");
//			partialExtractTimeBtn = retVal;
//		}
//		return retVal;
//	}

	public JFormattedTextField getSegmentField() {
		JFormattedTextField retVal = segmentField;
		if(retVal == null) {
			retVal = new JFormattedTextField();
			retVal.setFormatterFactory(new SegmentFormatterFactory());
			segmentField = retVal;
		}
		return retVal;
	}

	public JTextArea getOtherArgsField() {
		JTextArea retVal = otherArgsField;
		if(retVal == null) {
			retVal = new JTextArea();
			retVal.setRows(6);
			otherArgsField = retVal;
			otherArgsField.setText(OTHER_ARGS_DEFAULT_TEXT);
		}
		return retVal;
	}
	/**
	 * UI Actions
	 */
	public void onBrowseForInput(PhonActionEvent pae) {
		final OpenDialogProperties props = new OpenDialogProperties();
		props.setFileFilter(FileFilter.allFilesFilter);
		props.setMessage("Open media");
		props.setAllowMultipleSelection(false);
		props.setCanChooseDirectories(false);
		props.setCanChooseFiles(true);
		props.setRunAsync(false);
		
		final List<String> selectedFiles = 
				NativeDialogs.showOpenDialog(props);
		if(selectedFiles.size() > 0) {
			final String selectedFile = selectedFiles.get(0);
			inputFileLabel.setFile(new File(selectedFile));
		}
	}

	public void onShowSaveDialog(PhonActionEvent pae) {
		final SaveDialogProperties props = new SaveDialogProperties();
		props.setMessage("Save as");
		props.setCanCreateDirectories(true);
		props.setRunAsync(false);
		
		final String selectedFile = 
				NativeDialogs.showSaveDialog(props);
		if(selectedFile != null) {
//			final String selectedFile = selectedFiles.get(0);
			outputFileLabel.setFile(new File(selectedFile));
		}
	}
	
	public void onTogglePartialExtract(PhonActionEvent pae) {
		boolean partialExtract = getPartialExtractBox().isSelected();

//		getExtractRecordsButton().setEnabled(partialExtract);
//		getRecordsField().setEnabled(partialExtract);
//
//		getExtractTimeButton().setEnabled(partialExtract);
		getSegmentField().setEnabled(partialExtract);
	}

	public void onToggleIncludeVideo(PhonActionEvent pae) {
		boolean includeVideo = getEncodeVideoBox().isSelected();

		getVideoCodecField().setEnabled(includeVideo);
	}

	public void onToggleIncludeAudio(PhonActionEvent pae) {
		boolean includeAudio = getEncodeAudioBox().isSelected();

		getAudioCodecField().setEditable(includeAudio);
	}

//	public void onPartialExtractRecord(PhonActionEvent pae) {
//		boolean extractRecord = getExtractRecordsButton().isSelected();
//
//		getRecordsField().setEnabled(extractRecord);
//	}
//
//	public void onPartialExtractTime(PhonActionEvent pae) {
//		boolean extractTime = getExtractTimeButton().isSelected();
//
//		getSegmentField().setEnabled(extractTime);
//	}

	@Override
	public boolean validateStep() {
		boolean retVal = true;
		
		// make sure we have file selected
		File inputFile = inputFileLabel.getFile();
		if(inputFile == null) {
			retVal = false;
			final Toast toast = ToastFactory.makeToast("No input file selected");
			toast.start(inputFileLabel);
		} else {
			if(!inputFile.exists()) {
				retVal = false;
				final Toast toast = ToastFactory.makeToast("Input file not found");
				toast.start(inputFileLabel);
			}
		}
		
		File outputFile = outputFileLabel.getFile();
		if(outputFile == null) {
			retVal = false;
			final Toast toast = ToastFactory.makeToast("No output path specified");
			toast.start(outputFileLabel);
		}
		
		// we need to encode something
		if(!getEncodeVideoBox().isSelected() && !getEncodeAudioBox().isSelected()) {
			retVal = false;
			final Toast toast = ToastFactory.makeToast("Must select to encode video and/or audio");
			toast.start(getEncodeVideoBox());
		}
		
		return retVal;
	}

	/** Formatter factory */
	private class SegmentFormatterFactory extends JFormattedTextField.AbstractFormatterFactory {

		@Override
		public JFormattedTextField.AbstractFormatter getFormatter(JFormattedTextField arg0) {
			JFormattedTextField.AbstractFormatter retVal = null;
			try {
				retVal = new MaskFormatter("###:##.###-###:##.###");
				((MaskFormatter)retVal).setPlaceholderCharacter('0');
			} catch (ParseException e) {
				LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
			}
			return retVal;
		}

	}

	
}
