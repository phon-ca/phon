/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2016, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
 * Dept of Linguistics, Memorial University <https://phon.ca>
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
package ca.phon.media.export;

import java.text.NumberFormat;
import java.text.ParseException;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextPane;
import javax.swing.UIManager;

import org.jdesktop.swingx.JXCollapsiblePane;
import org.jdesktop.swingx.JXCollapsiblePane.Direction;
import org.jdesktop.swingx.VerticalLayout;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import ca.phon.formatter.Formatter;
import ca.phon.ui.nativedialogs.FileFilter;
import ca.phon.ui.text.FileSelectionField;
import ca.phon.ui.text.FormatterTextField;

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
