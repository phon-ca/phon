/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2017, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
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
package ca.phon.app.session.editor.view.media_player;

import java.awt.*;
import java.awt.event.*;
import java.lang.ref.WeakReference;
import java.text.ParseException;
import java.util.regex.*;

import javax.swing.*;
import javax.swing.event.*;

import org.apache.commons.lang3.StringUtils;

import com.jgoodies.forms.layout.*;

import ca.phon.app.session.editor.SessionEditor;
import ca.phon.media.player.PhonMediaPlayer;
import ca.phon.session.*;
import ca.phon.ui.*;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.ui.decorations.DialogHeader;
import ca.phon.ui.text.MediaSegmentField;
import ca.phon.ui.toast.*;
import ca.phon.util.*;
import ca.phon.util.icons.*;

/**
 * Dialog to play custom segments.
 */
public class PlayCustomSegmentDialog extends JDialog {

	private static final long serialVersionUID = -1199119546046104883L;

	/**
	 * Session editor
	 */
	private final WeakReference<SessionEditor> editorRef;
	
	/**
	 * Media player
	 */
	private final WeakReference<PhonMediaPlayer> mediaPlayerRef;
		
	/**
	 * Custom segment options
	 */
	private ButtonGroup btnGrp;
	private JRadioButton currentSegmentBtn;
	private JRadioButton contiguousSegmentBtn;
//	private JRadioButton periodBtn;
	private JRadioButton recordRangeBtn;
	private JRadioButton segmentTimeBtn;
	
	private JTextField rangeField;
	private MediaSegmentField segmentField;
	
	private JButton playBtn;
	private JButton cancelBtn;
	
	/**
	 * Constructor
	 */
	public PlayCustomSegmentDialog(SessionEditor editor, PhonMediaPlayer player) {
		super();
		this.mediaPlayerRef = new WeakReference<PhonMediaPlayer>(player);
		this.editorRef = new WeakReference<SessionEditor>(editor);
		init();
	}
	
	private void init() {
		final FormLayout layout = new FormLayout(
				"10dlu, fill:pref:grow",
				"pref, pref, pref, pref, pref, pref, pref, pref, pref, pref");
		final CellConstraints cc = new CellConstraints();
		setLayout(layout);
		
		final SessionEditor editor = editorRef.get();
		final Session session = editor.getSession();
		
		btnGrp = new ButtonGroup();
		
		currentSegmentBtn = new JRadioButton("Current segment");
		currentSegmentBtn.addActionListener(radioListener);
		currentSegmentBtn.setSelected(true);
		btnGrp.add(currentSegmentBtn);
		
		contiguousSegmentBtn = new JRadioButton("Speaker turn");
		contiguousSegmentBtn.addActionListener(radioListener);
		btnGrp.add(contiguousSegmentBtn);
		
//		periodBtn = new JRadioButton("Adjacency sequence");
//		periodBtn.addActionListener(radioListener);
//		btnGrp.add(periodBtn);
		
		recordRangeBtn = new JRadioButton("Record range");
		recordRangeBtn.addActionListener(radioListener);
		btnGrp.add(recordRangeBtn);
		rangeField = new JTextField();
		rangeField.setText( (editor.getCurrentRecordIndex()+1) + ".." + (editor.getCurrentRecordIndex()+1) );
		rangeField.setInputVerifier(new RangeVerifier());
		rangeField.setEnabled(false);
		rangeField.getDocument().addDocumentListener(rangeListener);
		
		segmentTimeBtn = new JRadioButton("Time range");
		segmentTimeBtn.addActionListener(radioListener);
		btnGrp.add(segmentTimeBtn);
		segmentField = new MediaSegmentField();
		segmentField.setEnabled(false);
		updateSegmentTimes();
		
		final DialogHeader header = new DialogHeader("Play Custom Segment", "Play a custom defined segment");
		
		add(header, cc.xyw(1, 1, 2));
		add(currentSegmentBtn, cc.xyw(1, 2, 2));
		add(contiguousSegmentBtn, cc.xyw(1, 3, 2));
//		add(periodBtn, cc.xyw(1, 4, 2));
		add(recordRangeBtn, cc.xyw(1, 5, 2));
		add(rangeField, cc.xy(2, 6));
		add(segmentTimeBtn, cc.xyw(1, 7, 2));
		add(segmentField, cc.xy(2, 8));
		
		final ImageIcon playIcon = IconManager.getInstance().getIcon("actions/media-playback-start", IconSize.SMALL);
		final ImageIcon cancelIcon = IconManager.getInstance().getIcon("actions/button_cancel", IconSize.SMALL);
		
		final PhonUIAction playAct = new PhonUIAction(this, "onPlay");
		playAct.putValue(PhonUIAction.NAME, "Play");
		playAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Play segment");
		playAct.putValue(PhonUIAction.SMALL_ICON, playIcon);
		playBtn = new JButton(playAct);
		
		final PhonUIAction cancelAct = new PhonUIAction(this, "onCancel");
		cancelAct.putValue(PhonUIAction.NAME, "Close");
		cancelAct.putValue(PhonUIAction.SMALL_ICON, cancelIcon);
		cancelBtn = new JButton(cancelAct);
		
		final JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		btnPanel.add(cancelBtn);
		btnPanel.add(playBtn);
		add(btnPanel, cc.xyw(1, 9, 2));
	}
	
	private void updateSegmentTimes() {
		final MediaSegment segment = getSegment();
		
		final String txt = 
				MsFormatter.msToDisplayString(new Float(segment.getStartValue()).longValue()) + 
				"-" +
				MsFormatter.msToDisplayString(new Float(segment.getEndValue()).longValue());
		segmentField.setText(txt);
	}
	
	private MediaSegment getSegment() {
		final SessionFactory factory = SessionFactory.newFactory();
		MediaSegment retVal = factory.createMediaSegment();
		
		final SessionEditor editor = editorRef.get();
		if(currentSegmentBtn.isSelected()) {
			final Record utt = editor.currentRecord();
			if(utt != null) {
				retVal = utt.getSegment().getGroup(0);
			}
		} else if(contiguousSegmentBtn.isSelected()) {
			retVal = SegmentCalculator.contiguousSegment(editor.getSession(), editor.getCurrentRecordIndex());
		} else if(recordRangeBtn.isSelected()) {
			try {
				final Range range = Range.fromString("(" + rangeField.getText() + ")");
				
				final Record startRecord = editor.getSession().getRecord(range.getFirst()-1);
				final MediaSegment startMedia = startRecord.getSegment().getGroup(0);
				final Record endRecord = editor.getSession().getRecord(range.getLast()-1);
				final MediaSegment endMedia = endRecord.getSegment().getGroup(0);
				
				if(startMedia != null && endMedia != null) {
					retVal.setStartValue(startMedia.getStartValue());
					retVal.setEndValue(endMedia.getEndValue());
				}
			} catch (Exception pe) {
				return retVal;
			}
		} else if(segmentTimeBtn.isSelected()) {
			final String times[] = segmentField.getText().split("-");
			long start = 0L;
			long end = 0L;
			try {
				start = MsFormatter.displayStringToMs(times[0]);
				end = MsFormatter.displayStringToMs(times[1]);
			} catch (ParseException pe) {}
			retVal.setStartValue(start);
			retVal.setEndValue(end);
		}
		
		return retVal;
	}
	
	public void onPlay() {
		final PhonMediaPlayer mp = mediaPlayerRef.get();
		if(mp != null) {
			final MediaSegment seg = getSegment();
		
			mp.playSegment((long)seg.getStartValue(), (long)(seg.getEndValue() - seg.getStartValue()));
			
			// TODO save segment
		}
		onCancel();
	}
	
	public void onCancel() {
		super.setVisible(false);
		dispose();
	}
	
	/*
	 * Button listener
	 */
	private final ActionListener radioListener = new ActionListener() {
		
		@Override
		public void actionPerformed(ActionEvent e) {
			final boolean enableRange = recordRangeBtn.isSelected();
			rangeField.setEnabled(enableRange);
			
			final boolean enableTimes = segmentTimeBtn.isSelected();
			segmentField.setEnabled(enableTimes);
			
			updateSegmentTimes();
		}
		
	};
	
	/*
	 * Record range listener
	 */
	private final DocumentListener rangeListener = new DocumentListener() {
		
		@Override
		public void removeUpdate(DocumentEvent e) {
			updateSegmentTimes();
		}
		
		@Override
		public void insertUpdate(DocumentEvent e) {
			updateSegmentTimes();
		}
		
		@Override
		public void changedUpdate(DocumentEvent e) {
			// TODO Auto-generated method stub
			
		}
	};
	
	/**
	 * Range field validator
	 */
	private class RangeVerifier extends AbstractVerifier implements VerifierListener {
		
		/** Error message */
		private String err = "";
		
		/** Range regex */
		private String rangeRegex = "([0-9]+)(?:\\.\\.([0-9]+))?";
		
		public RangeVerifier() {
			this.addVerificationListener(this);
		}

		@Override
		public boolean verification(JComponent c) {
			boolean retVal = true;
			
			Pattern p = Pattern.compile(rangeRegex);
			if(c == rangeField) {
				
				// don't validate if we are not enabled
				if(!rangeField.isEnabled()) return true;
				
				String rangeString = rangeField.getText();
				String[] ranges = rangeString.split(",");
				
				for(String range:ranges) {
					range = StringUtils.strip(range);
					Matcher m = p.matcher(range);
					
					if(m.matches()) {
						
						// make sure range is valid
						if(m.group(2) == null) {
							String idxStr = m.group(1);
							Integer idx = Integer.parseInt(idxStr);
							if(idx < 0 || idx > editorRef.get().getSession().getRecordCount()) {
								err = "Record out of bounds '" + idx + "'";
								retVal = false;
								break;
							}
						} else {
							String firstStr = m.group(1);
							String secStr = m.group(2);
							
							Integer first = Integer.parseInt(firstStr);
							Integer second = Integer.parseInt(secStr);
							if(first > second) {
								err = "Invalid range  '" + range + "'";
								retVal = false;
								break;
							} else if(
									first > editorRef.get().getSession().getRecordCount() || second > editorRef.get().getSession().getRecordCount()) {
								err = "Range out of bounds '" + range + "'";
								retVal = false;
								break;
							}
						}
						
					} else {
						err = "Invalid range string '" + range + "'";
						retVal = false;
						break;
					}
					
				}
			} else {
				retVal = false;
			}
			
			return retVal;
		}

		@Override
		public void verificationFailed(JComponent comp) {
			final Toast toast = ToastFactory.makeToast(err);
			toast.setMessageBackground(PhonGuiConstants.PHON_ORANGE);
			toast.start(comp);
		}

		@Override
		public void verificationPassed(JComponent comp) {
			comp.setBackground(Color.white);
		}

		@Override
		public void verificationReset(JComponent comp) {
			comp.setBackground(Color.white);
		}
		
	}
	
}
