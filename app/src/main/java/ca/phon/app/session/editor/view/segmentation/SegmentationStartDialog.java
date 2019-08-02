package ca.phon.app.session.editor.view.segmentation;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.text.ParseException;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

import ca.phon.app.session.editor.SessionEditor;
import ca.phon.app.session.editor.view.segmentation.SegmentationHandler.MediaStart;
import ca.phon.app.session.editor.view.segmentation.SegmentationHandler.SegmentationMode;
import ca.phon.formatter.Formatter;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.ui.decorations.DialogHeader;
import ca.phon.ui.ipamap.io.Grid;
import ca.phon.ui.layout.ButtonBarBuilder;
import ca.phon.ui.text.FormatterTextField;
import ca.phon.util.icons.IconManager;

/**
 * Options dialog for segmentation.
 *
 */
public class SegmentationStartDialog extends JDialog {
	
	private DialogHeader header;

	private ButtonGroup insertModeGroup = new ButtonGroup();
	private JRadioButton insertAtEndButton;
//	private JRadioButton insertAfterCurrentButton;
	private JRadioButton overwriteCurrentButton;
	
	private ButtonGroup playbackStartGroup = new ButtonGroup();
	private JRadioButton fromBeginningButton;
	private JRadioButton fromCurrentPositionButton;
	private JRadioButton fromEndOfSegmentedButton;
	
	private FormatterTextField<Long> windowLengthField;
	
	private JButton startButton;
	
	private JButton cancelButton;
	
	private JEditorPane keymapInfoPane;
	
	private boolean wasCanceled = false;
	
	public SegmentationStartDialog(SessionEditor editor) {
		super(editor);
		
		init();
	}
	
	private void init() {
		setLayout(new BorderLayout());
		
		JPanel centerPanel = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		
		header = new DialogHeader("Start Segmentation", "Select options and begin segmentation");
		
		insertAtEndButton = new JRadioButton(SegmentationHandler.SegmentationMode.INSERT_AT_END.toString());
		insertAtEndButton.setSelected(true);
		insertModeGroup.add(insertAtEndButton);
		
//		insertAfterCurrentButton = new JRadioButton(SegmentationHandler.SegmentationMode.INSERT_AFTER_CURRENT.toString());
//		insertAfterCurrentButton.setSelected(true);
//		insertModeGroup.add(insertAfterCurrentButton);
		
		overwriteCurrentButton = new JRadioButton(SegmentationHandler.SegmentationMode.REPLACE_CURRENT.toString());
		insertModeGroup.add(overwriteCurrentButton);
		
		fromBeginningButton = new JRadioButton(SegmentationHandler.MediaStart.AT_BEGNINNING.toString());
		playbackStartGroup.add(fromBeginningButton);
		
		fromCurrentPositionButton = new JRadioButton(SegmentationHandler.MediaStart.FROM_CURRENT_POSITION.toString());
		fromCurrentPositionButton.setSelected(true);
		playbackStartGroup.add(fromCurrentPositionButton);
		
		fromEndOfSegmentedButton = new JRadioButton(SegmentationHandler.MediaStart.AT_END_OF_LAST_RECORD.toString());
		playbackStartGroup.add(fromEndOfSegmentedButton);
		
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
		windowLengthField.setValue(0L);
		
		keymapInfoPane = new JEditorPane("text/html", KEYMAP_HTML);
		keymapInfoPane.setEditable(false);
		keymapInfoPane.setCaretPosition(0);
		keymapInfoPane.setPreferredSize(new Dimension(0, 300));

		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.fill = GridBagConstraints.NONE;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.WEST;
		
		centerPanel.add(new JLabel("Play media:"), gbc);
		gbc.gridx++;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1.0;
		centerPanel.add(fromBeginningButton, gbc);
		gbc.gridy++;
		centerPanel.add(fromCurrentPositionButton, gbc);
		gbc.gridy++;
		centerPanel.add(fromEndOfSegmentedButton, gbc);
		
		gbc.fill = GridBagConstraints.NONE;
		gbc.weightx = 0.0;
		gbc.gridx = 0;
		gbc.gridy++;
		centerPanel.add(new JLabel("Insert record:"), gbc);
		gbc.gridx++;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1.0;
//		centerPanel.add(insertAfterCurrentButton, gbc);
//		gbc.gridy++;
		centerPanel.add(insertAtEndButton, gbc);
		gbc.gridy++;
		centerPanel.add(overwriteCurrentButton, gbc);
		
		gbc.fill = GridBagConstraints.NONE;
		gbc.weightx = 0.0;
		gbc.gridx = 0;
		gbc.gridy++;
		centerPanel.add(new JLabel("Max segment length (ms):"), gbc);
		gbc.gridx++;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1.0;
		centerPanel.add(windowLengthField, gbc);
		
		gbc.fill = GridBagConstraints.NONE;
		gbc.weightx = 0.0;
		gbc.gridx = 0;
		gbc.gridy++;
		centerPanel.add(new JLabel("Keymap:"), gbc);
		gbc.gridy++;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridwidth = 2;
		centerPanel.add(new JScrollPane(keymapInfoPane), gbc);
			
		centerPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		
		PhonUIAction okAct = new PhonUIAction(this, "onOk");
		okAct.putValue(PhonUIAction.NAME, "Start segmentation");
		startButton = new JButton(okAct);
		getRootPane().setDefaultButton(startButton);
		
		PhonUIAction cancelAct = new PhonUIAction(this, "onCancel");
		cancelAct.putValue(PhonUIAction.NAME, "Cancel");
		cancelButton = new JButton(cancelAct);
		
		JComponent buttonBar = ButtonBarBuilder.buildOkCancelBar(startButton, cancelButton);
		
		add(header, BorderLayout.NORTH);
		add(centerPanel, BorderLayout.CENTER);
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
	
	public SegmentationHandler.MediaStart getMediaStart() {
		MediaStart retVal = MediaStart.FROM_CURRENT_POSITION;
		
		if(fromBeginningButton.isSelected()) {
			retVal = MediaStart.AT_BEGNINNING;
		} else if(fromCurrentPositionButton.isSelected()) {
			retVal = MediaStart.FROM_CURRENT_POSITION;
		} else if(fromEndOfSegmentedButton.isSelected()) {
			retVal = MediaStart.AT_END_OF_LAST_RECORD;
		}
		
		return retVal;
	}
	
	public long getWindowLength() {
		return windowLengthField.getValue();
	}
	
	public void onOk() {
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
			"<h3>Segmentation Controls</h3>\n" + 
			"<table>\n" + 
			"  <thead>\n" + 
			"    <tr>\n" + 
			"      <th>Action </th>\n" + 
			"      <th>Keystroke(s) </th>\n" + 
			"    </tr>\n" + 
			"  </thead>\n" + 
			"  <tbody>\n" + 
			"    <tr>\n" + 
			"      <td>Stop segmentation </td>\n" + 
			"      <td><code>Esc</code> </td>\n" + 
			"    </tr>\n" + 
			"    <tr>\n" + 
			"      <td>Break (e.g., silence, noise, etc.) </td>\n" + 
			"      <td><code>b</code> or <code>Numpad decimal</code> </td>\n" + 
			"    </tr>\n" + 
			"    <tr>\n" + 
			"      <td>New segment (unidentified) </td>\n" + 
			"      <td><code>Space</code> or <code>Numpad 0</code> </td>\n" + 
			"    </tr>\n" + 
			"    <tr>\n" + 
			"      <td>New segment (speaker 1) </td>\n" + 
			"      <td><code>1</code> or <code>Numpad 1</code> </td>\n" + 
			"    </tr>\n" + 
			"    <tr>\n" + 
			"      <td>New segment (speaker 2) </td>\n" + 
			"      <td><code>2</code> or <code>Numpad 2</code> </td>\n" + 
			"    </tr>\n" + 
			"    <tr>\n" + 
			"      <td>New segment (speaker 3) </td>\n" + 
			"      <td><code>3</code> or <code>Numpad 3</code> </td>\n" + 
			"    </tr>\n" + 
			"    <tr>\n" + 
			"      <td>… </td>\n" + 
			"      <td>… </td>\n" + 
			"    </tr>\n" + 
			"    <tr>\n" + 
			"      <td>New segment (speaker 9) </td>\n" + 
			"      <td><code>9</code> or <code>Numpad 9</code> </td>\n" + 
			"    </tr>\n" + 
			"  </tbody>\n" + 
			"</table>\n" + 
			"<h3>Media Controls</h3>\n" + 
			"<table>\n" + 
			"  <thead>\n" + 
			"    <tr>\n" + 
			"      <th>Action </th>\n" + 
			"      <th>Keystroke(s) </th>\n" + 
			"    </tr>\n" + 
			"  </thead>\n" + 
			"  <tbody>\n" + 
			"    <tr>\n" + 
			"      <td>Volume up </td>\n" + 
			"      <td><code>Up</code> or <code>Numpad multiply</code> </td>\n" + 
			"    </tr>\n" + 
			"    <tr>\n" + 
			"      <td>Volume down </td>\n" + 
			"      <td><code>Down</code> or <code>Numpad divide</code> </td>\n" + 
			"    </tr>\n" + 
			"    <tr>\n" + 
			"      <td>Go back 1s </td>\n" + 
			"      <td><code>Left</code> or <code>Numpad subtract</code> </td>\n" + 
			"    </tr>\n" + 
			"    <tr>\n" + 
			"      <td>Go forward 1s </td>\n" + 
			"      <td><code>Right</code> or <code>Numpad add</code> </td>\n" + 
			"    </tr>\n" + 
			"    <tr>\n" + 
			"      <td>Go back 5s </td>\n" + 
			"      <td><code>Shift+Left</code> or <code>Shift+Numpad subtract</code> </td>\n" + 
			"    </tr>\n" + 
			"    <tr>\n" + 
			"      <td>Go forward 5s </td>\n" + 
			"      <td><code>Shift+Right</code> or <code>Shift+Numpad add</code> </td>\n" + 
			"    </tr>\n" + 
			"  </tbody>\n" + 
			"</table>"
			+ "</html>\n";
	
}
