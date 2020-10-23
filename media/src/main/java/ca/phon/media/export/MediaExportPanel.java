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
package ca.phon.media.export;

import java.text.*;

import javax.swing.*;

import org.jdesktop.swingx.*;
import org.jdesktop.swingx.JXCollapsiblePane.*;

import com.jgoodies.forms.layout.*;

import ca.phon.formatter.*;
import ca.phon.ui.nativedialogs.*;
import ca.phon.ui.text.*;

/**
 * Panel for configuration options for media export.  Includes fields for
 * selecting start/stop times, export presets and customizing VLC media 
 * options for export.
 */
public class MediaExportPanel extends JPanel {

	private static final long serialVersionUID = -844969986668620404L;
	
	private FileSelectionField inputFileField;
	
	private FileSelectionField outputFileField;

	private FormatterTextField<Float> startTimeField;
	
	private FormatterTextField<Float> stopTimeField;
	
	private JRadioButton duplicateButton;
	
	private JRadioButton transcodeButton;
	
	private JXCollapsiblePane advancedPanel;
	
	private JComboBox<VLCMediaExporter.Preset> presetBox;
	
	private JTextPane optionsPane;
	
	private VLCMediaExporter exporter;

	public MediaExportPanel() {
		this(new VLCMediaExporter());
	}
	
	public MediaExportPanel(VLCMediaExporter exporter) {
		super();
		this.exporter = exporter;
		
		init();
	}
	
	private void init() {
		final FileFilter fileFilter = FileFilter.mediaFilter;
		inputFileField = new FileSelectionField();
		inputFileField.setFileFilter(fileFilter);
		inputFileField.getTextField().setPrompt("Input file");
		if(exporter.getInputFile() != null)
			inputFileField.setFile(exporter.getInputFile());
		
		outputFileField = new FileSelectionField();
		outputFileField.setFileFilter(fileFilter);
		outputFileField.getTextField().setPrompt("Output file");
		if(exporter.getOutputFile() != null)
			outputFileField.setFile(exporter.getOutputFile());
		
		final Formatter<Float> timeFormatter = new Formatter<Float>() {
			
			@Override
			public Float parse(String text) throws ParseException {
				Float retVal = new Float(0.0f);
				try {
					retVal = Float.parseFloat(text);
					
				} catch (NullPointerException | NumberFormatException e) {
					throw new ParseException(text, 0);
				}
				return retVal;
			}
			
			@Override
			public String format(Float obj) {
				final NumberFormat format = NumberFormat.getNumberInstance();
				format.setMaximumFractionDigits(3);
				return format.format(obj);
			}
			
		};
		startTimeField = new FormatterTextField<>(timeFormatter);
		startTimeField.setPrompt("Enter start time in seconds, leave empty to start at beginning of media");
		if(exporter.getMediaStartTime() > 0)
			startTimeField.setValue(exporter.getMediaStartTime());
		
		stopTimeField = new FormatterTextField<>(timeFormatter);
		stopTimeField.setPrompt("Enter stop time in seconds, leave empty to stop at end of media");
		if(exporter.getMediaStopTime() > 0)
			stopTimeField.setValue(exporter.getMediaStopTime());
		
		duplicateButton = new JRadioButton("Keep original media encoding");
		transcodeButton = new JRadioButton("Transcode media");
		
		final ButtonGroup btnGroup = new ButtonGroup();
		btnGroup.add(duplicateButton);
		btnGroup.add(transcodeButton);
		duplicateButton.setSelected(true);
		
		presetBox = new JComboBox<>(VLCMediaExporter.Preset.values());
		
		advancedPanel = new JXCollapsiblePane(Direction.DOWN);
		advancedPanel.setLayout(new VerticalLayout());
		optionsPane = new JTextPane();
		advancedPanel.add(optionsPane);
		
		final JPanel btmPanel = new JPanel(new VerticalLayout());
		final Action toggleAction = advancedPanel.getActionMap().get(JXCollapsiblePane.TOGGLE_ACTION);
		toggleAction.putValue(Action.NAME, "Advanced");
		toggleAction.putValue(JXCollapsiblePane.COLLAPSE_ICON, UIManager.getIcon("Tree.expandedIcon"));
		toggleAction.putValue(JXCollapsiblePane.EXPAND_ICON, UIManager.getIcon("Tree.collapsedIcon"));
		
		final JButton toggleBtn = new JButton(toggleAction);
		btmPanel.add(toggleBtn);
		btmPanel.add(advancedPanel);
		
		// file selection panel
		final FormLayout fileSelectionLayout = new FormLayout("pref, 3dlu, fill:pref:grow", "pref, pref");
		final CellConstraints cc = new CellConstraints();
		
		final JPanel fileSelectionPanel = new JPanel(fileSelectionLayout);
		fileSelectionPanel.setBorder(BorderFactory.createTitledBorder("File Selection"));
		fileSelectionPanel.add(new JLabel("Input file"), cc.xy(1,1));
		fileSelectionPanel.add(inputFileField, cc.xy(3,1));
		fileSelectionPanel.add(new JLabel("Output file"), cc.xy(1,2));
		fileSelectionPanel.add(outputFileField, cc.xy(3,2));
		
		// time selection panel
	}
	
}
