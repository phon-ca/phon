package ca.phon.app.session.editor.media;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.ref.WeakReference;
import java.text.ParseException;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.velocity.runtime.parser.node.GetExecutor;

import vlc4j.VLCError;
import vlc4j.VLCException;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import ca.phon.app.session.editor.SessionEditor;
import ca.phon.media.MsFormatter;
import ca.phon.media.player.PhonMediaPlayer;
import ca.phon.session.MediaSegment;
import ca.phon.session.Record;
import ca.phon.session.SegmentCalculator;
import ca.phon.session.Session;
import ca.phon.session.SessionFactory;
import ca.phon.ui.JRangeSlider;
import ca.phon.ui.MediaSegmentField;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.ui.decorations.DialogHeader;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;

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
	private JRadioButton periodBtn;
	private JRadioButton recordRangeBtn;
	private JRadioButton segmentTimeBtn;
	
	private JRangeSlider recordRange;
	private MediaSegmentField segmentField;
	
	private JButton playBtn;
	private JButton cancelBtn;
	
	/**
	 * Constructor
	 */
	public PlayCustomSegmentDialog(SessionEditor editor, PhonMediaPlayer player) {
		super();
		this.mediaPlayerRef = new WeakReference<PhonMediaPlayer>(player);
		this.editorRef = new WeakReference<>(editor);
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
		
		periodBtn = new JRadioButton("Adjacency sequence");
		periodBtn.addActionListener(radioListener);
		btnGrp.add(periodBtn);
		
		recordRangeBtn = new JRadioButton("Record range");
		recordRangeBtn.addActionListener(radioListener);
		btnGrp.add(recordRangeBtn);
		recordRange = new JRangeSlider(1, session.getRecordCount(), editor.getCurrentRecordIndex()+1, 0);
		recordRange.setEnabled(false);
		recordRange.setPaintSlidingLabel(true);
		recordRange.addChangeListener(rangeListener);
		
		segmentTimeBtn = new JRadioButton("Specific range");
		segmentTimeBtn.addActionListener(radioListener);
		btnGrp.add(segmentTimeBtn);
		segmentField = new MediaSegmentField();
		segmentField.setEnabled(false);
		updateSegmentTimes();
		
		final DialogHeader header = new DialogHeader("Play Custom Segment", "Play a custom defined segment");
		
		add(header, cc.xyw(1, 1, 2));
		add(currentSegmentBtn, cc.xyw(1, 2, 2));
		add(contiguousSegmentBtn, cc.xyw(1, 3, 2));
		add(periodBtn, cc.xyw(1, 4, 2));
		add(recordRangeBtn, cc.xyw(1, 5, 2));
		add(recordRange, cc.xy(2, 6));
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
		} else if(periodBtn.isSelected()) {
			retVal = SegmentCalculator.conversationPeriod(editor.getSession(), editor.getCurrentRecordIndex());
		} else if(recordRangeBtn.isSelected()) {
			final int startIndex = recordRange.getStart()-1;
			final int endIndex = startIndex + recordRange.getLength();
			
			final Record startRecord = editor.getSession().getRecord(startIndex);
			final MediaSegment startMedia = startRecord.getSegment().getGroup(0);
			final Record endRecord = editor.getSession().getRecord(endIndex);
			final MediaSegment endMedia = endRecord.getSegment().getGroup(0);
			
			if(startMedia != null && endMedia != null) {
				retVal.setStartValue(startMedia.getStartValue());
				retVal.setEndValue(endMedia.getEndValue());
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
			recordRange.setEnabled(enableRange);
			
			final boolean enableTimes = segmentTimeBtn.isSelected();
			segmentField.setEnabled(enableTimes);
			
			updateSegmentTimes();
		}
		
	};
	
	/*
	 * Record range listener
	 */
	private final ChangeListener rangeListener = new ChangeListener() {
		
		@Override
		public void stateChanged(ChangeEvent arg0) {
			updateSegmentTimes();
		}
		
	};
}
