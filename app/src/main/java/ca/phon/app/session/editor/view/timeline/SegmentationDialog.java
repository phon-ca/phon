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
package ca.phon.app.session.editor.view.timeline;

import java.awt.*;
import java.awt.event.*;
import java.text.*;
import java.util.*;
import java.util.List;

import javax.swing.*;

import org.jdesktop.swingx.*;

import ca.phon.app.session.editor.*;
import ca.phon.app.session.editor.view.timeline.SegmentationHandler.*;
import ca.phon.formatter.Formatter;
import ca.phon.session.*;
import ca.phon.ui.action.*;
import ca.phon.ui.layout.*;
import ca.phon.ui.text.*;
import ca.phon.util.*;

/**
 * Options dialog for segmentation.
 *
 */
public class SegmentationDialog extends JDialog {

	private final static String INSERT_MODE_PROP = SegmentationDialog.class.getSimpleName() + ".insertMode";
	private final static SegmentationMode DEFAULT_SEGMENTATION_MODE = SegmentationMode.INSERT_AT_END;
	
	private ButtonGroup insertModeGroup = new ButtonGroup();
	private JRadioButton insertAtEndButton;
	private JRadioButton overwriteCurrentButton;
	
	private final static String PLAYBACK_MODE_PROP = SegmentationDialog.class.getSimpleName() + ".playbackMode";
	private final static MediaStart DEFAULT_MEDIA_START = MediaStart.FROM_CURRENT_POSITION;
	
	private final static String PARTICIPANT_PROP = SegmentationDialog.class.getSimpleName() + ".participant";
	private final static Participant DEFAULT_PARTICIPANT = Participant.UNKNOWN;
	
	private ButtonGroup playbackStartGroup = new ButtonGroup();
	private JRadioButton fromBeginningButton;
	private JRadioButton fromCurrentPositionButton;
	private JRadioButton fromEndOfSegmentedButton;
	private JRadioButton fromEndofParticipantButton;
	private JComboBox<Participant> participantSelectionBox;

	private JCheckBox useWindowButton;
	private final static boolean DEFAULT_USE_WINDOW = false;
	private final static String WINDOW_LENGTH_PROP = SegmentationDialog.class.getSimpleName() + ".segmentationWindowMS";
	private final static long DEFAULT_WINDOW_LENGTH = 3000L;
	private FormatterTextField<Long> windowLengthField;
	
	private JButton startButton;
	
	private JButton cancelButton;
	
	private JEditorPane keymapInfoPane;
	
	private boolean wasCanceled = false;
	
	private final SessionEditor editor;
	
	public SegmentationDialog(SessionEditor editor) {
		super(editor);
		
		setTitle("Start Segmentation");
		
		this.editor = editor;
		
		init();
		//setResizable(false);
		loadSettings();
	}
	
	private void init() {
		setLayout(new BorderLayout());
		
		insertAtEndButton = new JRadioButton(SegmentationHandler.SegmentationMode.INSERT_AT_END.toString());
		insertAtEndButton.setSelected(true);
		insertModeGroup.add(insertAtEndButton);
				
		overwriteCurrentButton = new JRadioButton(SegmentationHandler.SegmentationMode.REPLACE_CURRENT.toString());
		insertModeGroup.add(overwriteCurrentButton);
		
		fromBeginningButton = new JRadioButton(SegmentationHandler.MediaStart.AT_BEGNINNING.toString());
		playbackStartGroup.add(fromBeginningButton);
		
		fromCurrentPositionButton = new JRadioButton(SegmentationHandler.MediaStart.FROM_CURRENT_POSITION.toString());
		fromCurrentPositionButton.setSelected(true);
		playbackStartGroup.add(fromCurrentPositionButton);
		
		fromEndOfSegmentedButton = new JRadioButton(SegmentationHandler.MediaStart.AT_END_OF_LAST_RECORD.toString());
		playbackStartGroup.add(fromEndOfSegmentedButton);
		
		fromEndofParticipantButton = new JRadioButton(SegmentationHandler.MediaStart.AT_END_OF_LAST_RECORD_FOR_PARTICIPANT.toString());
		playbackStartGroup.add(fromEndofParticipantButton);
	
		List<Participant> participantList = new ArrayList<>();
		editor.getSession().getParticipants().forEach( participantList::add );		
		participantList.add(Participant.UNKNOWN);
		participantSelectionBox = new JComboBox<Participant>(participantList.toArray(new Participant[0]));
		participantSelectionBox.setSelectedItem(Participant.UNKNOWN);
		
		participantSelectionBox.setEnabled(fromEndofParticipantButton.isSelected());
		ActionListener enabledListener = (e) -> { participantSelectionBox.setEnabled(fromEndofParticipantButton.isSelected()); };
		fromBeginningButton.addActionListener(enabledListener);
		fromCurrentPositionButton.addActionListener(enabledListener);
		fromEndOfSegmentedButton.addActionListener(enabledListener);
		fromEndofParticipantButton.addActionListener(enabledListener);
		
		useWindowButton = new JCheckBox("Use segmentation window");
		windowLengthField = new FormatterTextField<>(new Formatter<Long>() {
			
			@Override
			public String format(Long obj) {
				return Long.toString(obj.longValue());
			}
			
			@Override
			public Long parse(String text) throws ParseException {
				return Long.parseLong(text);
			}
			
		});
		useWindowButton.addActionListener((e) -> {
			windowLengthField.setEnabled(useWindowButton.isSelected());			
		});
		useWindowButton.setSelected(DEFAULT_USE_WINDOW);
		windowLengthField.setEnabled(DEFAULT_USE_WINDOW);
		windowLengthField.setValue(DEFAULT_WINDOW_LENGTH);
		
		keymapInfoPane = new JEditorPane("text/html", KEYMAP_HTML);
		keymapInfoPane.setEditable(false);
		keymapInfoPane.setCaretPosition(0);
//		keymapInfoPane.setPreferredSize(new Dimension(250, 300));
		
		GridBagConstraints gbc = new GridBagConstraints();
		JPanel mediaPlaybackPanel = new JPanel(new GridBagLayout());
		
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.NORTHEAST;
		gbc.insets = new Insets(5, 5, 0, 5);
		
		gbc.gridwidth = 2;
		mediaPlaybackPanel.add(fromBeginningButton, gbc);
		gbc.gridy++;
		mediaPlaybackPanel.add(fromCurrentPositionButton, gbc);
		gbc.gridy++;
		mediaPlaybackPanel.add(fromEndOfSegmentedButton, gbc);
		gbc.gridy++;
		gbc.gridwidth = 1;
		gbc.weightx = 0.0;
		mediaPlaybackPanel.add(fromEndofParticipantButton, gbc);
		gbc.weightx = 1.0;
		gbc.gridx++;
		mediaPlaybackPanel.add(participantSelectionBox, gbc);
		
		mediaPlaybackPanel.setBorder(BorderFactory.createTitledBorder("Media Playback"));
		mediaPlaybackPanel.setVisible(editor.getMediaModel().isSessionMediaAvailable());
		
		JPanel recordCreationPanel = new JPanel(new GridBagLayout());
		
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.EAST;
		gbc.insets = new Insets(5, 5, 0, 5);
		gbc.gridwidth = 2;
		
		recordCreationPanel.add(new JLabel("When identifiying a new segment:"), gbc);
		gbc.gridy++;
		gbc.insets  = new Insets(5, 10, 0, 5);
		recordCreationPanel.add(insertAtEndButton, gbc);
		gbc.gridy++;
		recordCreationPanel.add(overwriteCurrentButton, gbc);
		gbc.insets = new Insets(5, 5, 0, 5);
		gbc.gridy++;
		gbc.weightx = 0.0;
		gbc.gridwidth = 1;
		recordCreationPanel.add(useWindowButton, gbc);
		gbc.gridx++;
		gbc.weightx = 1.0;
		recordCreationPanel.add(windowLengthField, gbc);
		gbc.insets = new Insets(5, 10, 0, 5);
		JLabel lbl = new JLabel("<html><span style='font-size:small;'>(maximum segment length in milliseconds)</span></html>");
		gbc.gridy++;
		recordCreationPanel.add(lbl, gbc);
		
		recordCreationPanel.setBorder(BorderFactory.createTitledBorder("Segmentation"));
		
		PhonUIAction okAct = new PhonUIAction(this, "onOk");
		okAct.putValue(PhonUIAction.NAME, "Start segmentation");
		startButton = new JButton(okAct);
		getRootPane().setDefaultButton(startButton);
		
		PhonUIAction cancelAct = new PhonUIAction(this, "onCancel");
		cancelAct.putValue(PhonUIAction.NAME, "Cancel");
		cancelButton = new JButton(cancelAct);
		
		JComponent buttonBar = ButtonBarBuilder.buildOkCancelBar(startButton, cancelButton);
		
		JPanel centerPanel = new JPanel(new VerticalLayout());
		centerPanel.add(mediaPlaybackPanel);
		centerPanel.add(recordCreationPanel);

		JPanel rightPanel = new JPanel(new VerticalLayout());
		rightPanel.add(mediaPlaybackPanel);
		rightPanel.add(recordCreationPanel);
		JPanel contentPanel = new JPanel(new GridLayout(0, 2));
		contentPanel.add(new JScrollPane(keymapInfoPane));
		contentPanel.add(rightPanel);

		add(contentPanel, BorderLayout.CENTER);
		add(buttonBar, BorderLayout.SOUTH);
	}
	
	public SegmentationHandler.SegmentationMode getSegmentationMode() {
		SegmentationHandler.SegmentationMode retVal = SegmentationMode.INSERT_AT_END;
		
		if(insertAtEndButton.isSelected()) {
			retVal = SegmentationMode.INSERT_AT_END;
		} else if(overwriteCurrentButton.isSelected()) {
			retVal = SegmentationMode.REPLACE_CURRENT;
		}
		
		return retVal;
	}
	
	public Participant getSelectedParticipant() {
		return (Participant)participantSelectionBox.getSelectedItem();
	}
	
	public SegmentationHandler.MediaStart getMediaStart() {
		MediaStart retVal = MediaStart.FROM_CURRENT_POSITION;
		
		if(fromBeginningButton.isSelected()) {
			retVal = MediaStart.AT_BEGNINNING;
		} else if(fromCurrentPositionButton.isSelected()) {
			retVal = MediaStart.FROM_CURRENT_POSITION;
		} else if(fromEndOfSegmentedButton.isSelected()) {
			retVal = MediaStart.AT_END_OF_LAST_RECORD;
		} else if(fromEndofParticipantButton.isSelected()) {
			retVal = MediaStart.AT_END_OF_LAST_RECORD_FOR_PARTICIPANT;
		}
		
		return retVal;
	}
	
	public long getWindowLength() {
		return (useWindowButton.isSelected() ? windowLengthField.getValue() : 0L);
	}
	
	/**
	 * Dialog settings will be saved when the ok button is pressed
	 */
	private void saveSettings() {
		PrefHelper.getUserPreferences().put(INSERT_MODE_PROP, getSegmentationMode().toString());
		PrefHelper.getUserPreferences().put(PLAYBACK_MODE_PROP, getMediaStart().toString());
		PrefHelper.getUserPreferences().put(PARTICIPANT_PROP, getSelectedParticipant().getId());
		PrefHelper.getUserPreferences().putLong(WINDOW_LENGTH_PROP, getWindowLength());
	}
	
	/**
	 * Dialog settings are loaded in init()
	 */
	private void loadSettings() {
		SegmentationMode segmentationMode = 
				SegmentationMode.fromString(PrefHelper.get(INSERT_MODE_PROP, DEFAULT_SEGMENTATION_MODE.toString()));
		switch(segmentationMode) {
		case INSERT_AT_END:
			insertAtEndButton.setSelected(true);
			break;
			
		case REPLACE_CURRENT:
			overwriteCurrentButton.setSelected(true);
			break;
			
		default:
			insertAtEndButton.setSelected(true);	
		}
		
		String participantId = PrefHelper.get(PARTICIPANT_PROP, DEFAULT_PARTICIPANT.getId());
		for(int i = 0; i < participantSelectionBox.getItemCount(); i++) {
			if(participantSelectionBox.getItemAt(i).getId().equals(participantId)) {
				participantSelectionBox.setSelectedIndex(i);
				break;
			}
		}
		
		MediaStart mediaStart = 
				MediaStart.fromString(PrefHelper.get(PLAYBACK_MODE_PROP, DEFAULT_MEDIA_START.toString()));
		switch(mediaStart) {
		case AT_BEGNINNING:
			fromBeginningButton.setSelected(true);
			break;
			
		case AT_END_OF_LAST_RECORD:
			fromEndOfSegmentedButton.setSelected(true);
			break;
			
		case AT_END_OF_LAST_RECORD_FOR_PARTICIPANT:
			fromEndofParticipantButton.setSelected(true);
			participantSelectionBox.setEnabled(true);
			break;
			
		case FROM_CURRENT_POSITION:
			fromCurrentPositionButton.setSelected(true);
			break;
			
		default:
			fromCurrentPositionButton.setSelected(true);
		}
		
		long windowLength = PrefHelper.getLong(WINDOW_LENGTH_PROP, DEFAULT_WINDOW_LENGTH);
		if(windowLength == 0L) {
			windowLengthField.setValue(DEFAULT_WINDOW_LENGTH);
			windowLengthField.setEnabled(false);
			useWindowButton.setSelected(false);
		} else {
			windowLengthField.setValue(windowLength);
			windowLengthField.setEnabled(true);
			useWindowButton.setSelected(true);
		}
	}
	
	public void onOk() {
		saveSettings();
		wasCanceled = false;
		setVisible(false);
	}
	
	public void onCancel() {
		wasCanceled = true;
		setVisible(false);
	}
	
	public boolean wasCanceled() {
		return this.wasCanceled;
	}
	
	private final static String KEYMAP_HTML = "<html>\n" +
		"<p>During segmentation mode all keyboard input for the application will be blocked except the following keystrokes:</p>\n" + 
		"<h3>Segmentation Controls</h3>\n" + 
		"<table width='100%' style='border: 1px solid black'>\n" + 
		"<thead>\n" + 
		"<tr><th width='50%' style='border-bottom: 1px solid black'>Action</th><th width='50%' style='border-bottom: 1px solid black'>Keystroke(s)</th></tr>\n" + 
		"</thead>\n" + 
		"<tbody>\n" + 
		"<tr><td>Stop segmentation</td><td><code>Esc</code></td></tr>\n" + 
		"<tr><td>New segment (unidentified)</td><td><code>Space</code> or <code>0</code> or <code>Numpad 0</code></td></tr>\n" + 
		"<tr><td>New segment (participant 1...9)</td><td><code>1</code>...<code>9</code> or <code>Numpad 1</code>...<code>Numpad 9</code></td></tr>\n" + 
		"<tr><td>Break (e.g., silence, noise, etc.)</td><td><code>b</code> or <code>Numpad decimal</code></td></tr>\n" + 
		"<tr><td>Toggle segmentation window</td><td><code>W</code></td></tr>\n" + 
		"<tr><td>Increase segmentation window (100ms)</td><td><code>Up</code></td></tr>\n" +
		"<tr><td>Decrease segmentation window (100ms)</td><td><code>Down</code></td></tr>\n" +
		"</tbody>\n" + 
		"</table>\n" + 
		"<h3>Media Controls</h3>\n" + 
		"<table width='100%' style='border: 1px solid black'>\n" + 
		"<thead>\n" + 
		"<tr><th width='50%' style='border-bottom: 1px solid black'>Action</th><th width='50%' style='border-bottom: 1px solid black'>Keystroke(s)</th></tr>\n" + 
		"</thead>\n" + 
		"<tbody>\n" + 
		"<tr><td>Volume up</td><td><code>Shift+Up</code> or <code>Numpad multiply</code></td></tr>\n" + 
		"<tr><td>Volume down</td><td><code>Shift+Down</code> or <code>Numpad divide</code></td></tr>\n" + 
		"<tr><td>Go back 1s</td><td><code>Left</code> or <code>Numpad subtract</code></td></tr>\n" + 
		"<tr><td>Go forward 1s</td><td><code>Right</code> or <code>Numpad add</code></td></tr>\n" + 
		"<tr><td>Go back 5s</td><td><code>Shift+Left</code> or <code>Shift+Numpad subtract</code></td></tr>\n" + 
		"<tr><td>Go forward 5s</td><td><code>Shift+Right</code> or <code>Shift+Numpad add</code></td></tr>\n" +
		"<tr><td>Increase playback rate</td><td><code>Period</code></td></tr>\n" +
		"<tr><td>Decrease playback rate</td><td><code>Comma</code></td></tr>\n" +
		"</tbody>\n" + 
		"</table>"
		+ "</html>\n";
	
}
