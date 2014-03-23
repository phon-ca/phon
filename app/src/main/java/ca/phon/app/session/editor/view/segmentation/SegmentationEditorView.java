package ca.phon.app.session.editor.view.segmentation;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.MouseInputAdapter;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

import org.apache.commons.lang3.StringUtils;

import uk.co.caprica.vlcj.player.MediaPlayer;
import uk.co.caprica.vlcj.player.MediaPlayerEventAdapter;
import ca.phon.app.session.editor.DelegateEditorAction;
import ca.phon.app.session.editor.EditorAction;
import ca.phon.app.session.editor.EditorEvent;
import ca.phon.app.session.editor.EditorEventType;
import ca.phon.app.session.editor.EditorView;
import ca.phon.app.session.editor.RunOnEDT;
import ca.phon.app.session.editor.SessionEditor;
import ca.phon.app.session.editor.undo.AddRecordEdit;
import ca.phon.app.session.editor.undo.TierEdit;
import ca.phon.app.session.editor.view.media_player.MediaPlayerEditorView;
import ca.phon.orthography.Orthography;
import ca.phon.session.MediaSegment;
import ca.phon.session.MediaUnit;
import ca.phon.session.Participant;
import ca.phon.session.Record;
import ca.phon.session.Session;
import ca.phon.session.SessionFactory;
import ca.phon.ui.action.PhonActionEvent;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.util.OSInfo;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class SegmentationEditorView extends EditorView {

	private static final long serialVersionUID = -3058669055923770822L;

	private final static String VIEW_NAME = "Segmentation";
	
	private enum SegmentationMode {
		INSERT_AT_END("Insert record at end of session"),
		INSERT_AFTER_CURRENT("Insert record after current record"),
		REPLACE_CURRENT("Replace segment for current record");
		
		String val = "";
		
		SegmentationMode(String txt) {
			val = txt;
		}
		
		@Override
		public String toString() {
			return this.val;
		}
	}
	
	/**
	 * View title
	 */
	private final static String VIEW_TITLE = "Segmentation";
	
	private static final int DEFAULT_SEGMENT_WINDOW = 3000;

	private JTextField segmentWindowField;

	private SegmentLabel segmentLabel;
	
	private JPanel participantPanel;
	
	/**
	 * Combo box for selecting segmentation mode
	 */
	private JComboBox modeBox;
	
	/**
	 * Media player panel
	 */
	private MediaPlayerEditorView mediaPlayerView;
	
	/**
	 * Constructor
	 */
	public SegmentationEditorView(SessionEditor editor, MediaPlayerEditorView playerView) {
		super(editor);
		
		this.mediaPlayerView = playerView;
//		this.mediaPlayerView.getPlayer().addMediaPlayerListener(_locationListener);
		
		init();
	}
	
	private void init() {
		segmentWindowField = new JTextField();
		SegmentWindowDocument segDoc = new SegmentWindowDocument();
		segmentWindowField.setDocument(segDoc);
		segmentWindowField.setText(DEFAULT_SEGMENT_WINDOW+"");
		segDoc.addDocumentListener(new SegmentWindowListener());

		segmentLabel = new SegmentLabel();
		segmentLabel.setSegmentWindow(DEFAULT_SEGMENT_WINDOW);
		segmentLabel.setCurrentTime(0L);
		segmentLabel.lockSegmentStartTime(-1L);

		modeBox = new JComboBox(SegmentationMode.values());
		modeBox.setSelectedItem(SegmentationMode.INSERT_AFTER_CURRENT);

		JPanel topPanel = new JPanel();
		FormLayout topLayout = new FormLayout(
				"right:pref, 3dlu, fill:default:grow, pref", "pref, pref, pref, pref");
		topPanel.setLayout(topLayout);
		CellConstraints cc = new CellConstraints();
		topPanel.add(new JLabel("Segment Window"), cc.xy(1,1));
		topPanel.add(segmentWindowField, cc.xy(3,1));
		topPanel.add(new JLabel("ms"), cc.xy(4, 1));

		JLabel infoLabel = new JLabel("Set to 0 for unlimited segment time");
		infoLabel.setFont(infoLabel.getFont().deriveFont(10.0f));

		topPanel.add(infoLabel, cc.xy(3, 2));
		topPanel.add(new JLabel("Current Window"), cc.xy(1,3));
		topPanel.add(segmentLabel, cc.xy(3, 3));

		topPanel.add(new JLabel("Mode"), cc.xy(1,4));
		topPanel.add(modeBox, cc.xyw(3,4,2));

		setLayout(new BorderLayout());
		add(topPanel, BorderLayout.NORTH);
		
		participantPanel = new JPanel();
		
		participantPanel.setBackground(Color.white);
		participantPanel.setOpaque(true);
		
		JScrollPane participantScroller = new JScrollPane(participantPanel);
		Dimension prefSize = participantScroller.getPreferredSize();
		prefSize.height = 150;
		participantScroller.setPreferredSize(prefSize);
		Dimension maxSize = participantScroller.getMaximumSize();
		maxSize.height = 200;
		participantScroller.setMaximumSize(maxSize);
		Dimension minSize = participantScroller.getMinimumSize();
		minSize.height = 100;
		participantScroller.setMinimumSize(minSize);
		participantScroller.setBorder(BorderFactory.createTitledBorder("Participants"));

		add(participantScroller, BorderLayout.CENTER);
		
		updateParticipantPanel();
		setupEditorActions();
	}

	private void setupEditorActions() {
		EditorAction onParticipantChangeAct = 
			new DelegateEditorAction(this, "onParticipantChanged");
		getEditor().getEventManager().registerActionForEvent(EditorEventType.PARTICIPANT_CHANGED, onParticipantChangeAct);

		EditorAction onParticipantAddedAct =
			new DelegateEditorAction(this, "onParticipantAdded");
		getEditor().getEventManager().registerActionForEvent(EditorEventType.PARTICIPANT_ADDED, onParticipantAddedAct);
		
		EditorAction onParticipantDeletedAct = 
			new DelegateEditorAction(this, "onParticipantDeleted");
		getEditor().getEventManager().registerActionForEvent(EditorEventType.PARTICIPANT_REMOVED, onParticipantDeletedAct);
	}
	
	/* Editor actions */
	@RunOnEDT
	public void onParticipantChanged(EditorEvent ee) {
		updateParticipantPanel();
		participantPanel.revalidate();
		participantPanel.repaint();
	}
	
	@RunOnEDT
	public void onParticipantAdded(EditorEvent ee) {
		updateParticipantPanel();
		participantPanel.revalidate();
		participantPanel.repaint();
	}
	
	@RunOnEDT
	public void onParticipantDeleted(EditorEvent ee) {
		updateParticipantPanel();
		participantPanel.revalidate();
		participantPanel.repaint();
	}
	
	/**
	 * Called for segmentation.
	 * Data given is the speaker:IParticipant or
	 * null if the speaker is undefined.
	 *
	 */
	public void performSegmentation(PhonActionEvent pae) {
		Participant speaker = null;
		if(pae.getData() != null) {
			speaker = (Participant)pae.getData();
		}

		// get the segment time
		long segStart = 0L;
		long segEnd = 0L;

		synchronized(segmentLabel) {
			segStart = segmentLabel.getStartTime();
			segEnd = segmentLabel.getCurrentTime();

			segmentLabel.lockSegmentStartTime(segEnd+1);
		}

		long segLength = segEnd - segStart;

		if(segLength > 0) {
			
			// should we create a new record or overwrite
			// the data in the curent?
			final SessionFactory factory = SessionFactory.newFactory();
			Record utt = factory.createRecord();
			// setup speaker
			utt.setSpeaker(speaker);

			// setup orthography
			utt.getOrthography().addGroup(new Orthography());
			
			// setup segment
			final MediaSegment m = factory.createMediaSegment();
			m.setStartValue(segStart);
			m.setEndValue(segEnd);
			m.setUnitType(MediaUnit.Millisecond);
			utt.getSegment().setGroup(0, m);
			
			// should we replace segment for current record instead?
			SegmentationMode mode = (SegmentationMode)modeBox.getSelectedItem();
			if(mode == SegmentationMode.REPLACE_CURRENT) {
				utt = getEditor().currentRecord();
			}

			if(mode == SegmentationMode.REPLACE_CURRENT) {
				final TierEdit<MediaSegment> segEdit = new TierEdit<MediaSegment>(getEditor(), utt.getSegment(), 0, m);
				getEditor().getUndoSupport().postEdit(segEdit);
			} else {
				int idx = getEditor().getDataModel().getRecordCount();
				// where are we going to insert
				if(mode == SegmentationMode.INSERT_AFTER_CURRENT) {
					idx = getEditor().getCurrentRecordIndex() + 1;
				}
				final AddRecordEdit edit = new AddRecordEdit(getEditor(), utt, idx);
				getEditor().getUndoSupport().postEdit(edit);
			}
		}
	}
	
	private void updateParticipantPanel() {
		participantPanel.removeAll();
		
		// setup layout
		String colLayout = "fill:default";
		String rowLayout = "pref, 5px";
		for(int i = 0; i <= getEditor().getSession().getParticipantCount(); i++) {
			rowLayout += ", pref, 5px";
		}
		FormLayout layout = new FormLayout(colLayout, rowLayout);
		participantPanel.setLayout(layout);
		CellConstraints cc = new CellConstraints();
		int currentRow = 1;
		
		InputMap inputMap = participantPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
		ActionMap actionMap = participantPanel.getActionMap();

		String performSegmentationID = "_perform_segmentation_";

		String ksStr = (OSInfo.isMacOs() ? "\u2318" : "CTRL +")
				+ "0";
		PhonUIAction noPartSegmentAct =
				new PhonUIAction(this, "performSegmentation", null);
		noPartSegmentAct.putValue(Action.NAME,
				ksStr + " speaker undefined");
		KeyStroke noPartKs =
				KeyStroke.getKeyStroke(KeyEvent.VK_0, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());

		String segID = performSegmentationID + "_no_spekaer_";
		actionMap.put(segID, noPartSegmentAct);
		inputMap.put(noPartKs, segID);
		
		// setup labels to be used like buttons
		String segMsg = 
			"Click name to create a new record:";
		JLabel segLbl = new JLabel(segMsg);
		
		participantPanel.add(segLbl, cc.xy(1, currentRow));
		currentRow += 2;
		
		JLabel noPartLbl = new JLabel();
		String noPartMsg = 
			ksStr + " <no speaker>";
		noPartLbl.setText(noPartMsg);
		noPartLbl.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		noPartLbl.addMouseListener(new SegmentLabelMouseHandler());
		noPartLbl.setOpaque(false);
		participantPanel.add(noPartLbl, cc.xy(1, currentRow));
		currentRow += 2;
		
		final Session session = getEditor().getSession();
		int pIdx = 1;
		for(Participant p:session.getParticipants()) {

			KeyStroke segmentKs =
					KeyStroke.getKeyStroke(KeyEvent.VK_0 + pIdx,
					getToolkit().getMenuShortcutKeyMask());

			ksStr = (OSInfo.isMacOs() ? "\u2318" : "CTRL +")
					+ pIdx;

			PhonUIAction segmentAction =
					new PhonUIAction(this, "performSegmentation", p);
			segmentAction.putValue(Action.NAME,
					ksStr + " " + p.getName());
			
			JLabel participantLbl = new JLabel();
			String participantStr = 
				ksStr + " " + (p.getName() != null ? p.getName() : p.getId());
			participantLbl.setText(participantStr);
			participantLbl.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			participantLbl.addMouseListener(new SegmentLabelMouseHandler(p));
			participantLbl.setOpaque(false);
			participantPanel.add(participantLbl, cc.xy(1, currentRow));
			currentRow += 2;
			
			segID = performSegmentationID + p.getId() + "_";
			actionMap.put(segID, segmentAction);
			inputMap.put(segmentKs, segID);
			
			pIdx++;
		}
	}
	
	private class SegmentWindowListener implements DocumentListener {

		@Override
		public void insertUpdate(DocumentEvent de) {
			updateSegmentWindow();
		}

		@Override
		public void removeUpdate(DocumentEvent de) {
			updateSegmentWindow();
		}

		@Override
		public void changedUpdate(DocumentEvent de) {
		}

	}
	
	private class SegmentLabelMouseHandler extends MouseInputAdapter {
		
		Participant p = null;
		
		public SegmentLabelMouseHandler() {
			
		}
		
		public SegmentLabelMouseHandler(Participant p) {
			this.p = p;
		}

		@Override
		public void mousePressed(MouseEvent arg0) {
			// create a fake PhonActionEvent for segmentation
			ActionEvent ae = 
				new ActionEvent(arg0.getSource(), arg0.getID(), "_new_segment_");
			PhonActionEvent pae = new PhonActionEvent(ae, p);
			performSegmentation(pae);
		}
		
	}
	
	private void updateSegmentWindow() {
		String txt = 
				StringUtils.strip(segmentWindowField.getText());
		if(txt.length() == 0)
			txt = "0";
		Integer windowLen = Integer.parseInt(txt);

		synchronized(segmentLabel) {
			segmentLabel.setSegmentWindow(windowLen);
		}
	}

	private class SegmentWindowDocument extends PlainDocument {

		@Override
		public void insertString(int offs, String str, AttributeSet a)
				throws BadLocationException {
			for(char c:str.toCharArray()) {
				if(!Character.isDigit(c))
					return;
			}
			super.insertString(offs, str, a);
		}

	}
	
	private final MediaLocationListener _locationListener = 
		new MediaLocationListener();
	private class MediaLocationListener extends MediaPlayerEventAdapter {
		
		private long lastBufferTime = -1L;
		
		@Override
		public void timeChanged(MediaPlayer mediaPlayer, long newTime) {
			// TODO Auto-generated method stub
			super.timeChanged(mediaPlayer, newTime);
			synchronized(segmentLabel) {
				segmentLabel.setCurrentTime(newTime);
			}
		}

//		@Override
//		public void onBuffering(VLCMediaPlayerEvent vlcmpe) {
//			// segment label class handles thread safety for updates
//			VLCMediaPlayer player = vlcmpe.getSource();
//			try {
//				long currentTime = player.getTime();
//				
//				if(lastBufferTime != currentTime) {
//					synchronized (segmentLabel) {
//						segmentLabel.setCurrentTime(currentTime);
//					}
//					lastBufferTime = currentTime;
//				}
//			} catch (VLCException e) {
//				VLCError.logAndClear(e);
//			}
//		}
		
	}

	@Override
	public String getName() {
		return VIEW_NAME;
	}

	@Override
	public ImageIcon getIcon() {
		return IconManager.getInstance().getIcon("actions/film-link", IconSize.SMALL);
	}

	@Override
	public JMenu getMenu() {
		return null;
	}

}
