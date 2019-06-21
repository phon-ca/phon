package ca.phon.app.session.editor.view.timegrid;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.event.MouseInputAdapter;

import org.jdesktop.swingx.VerticalLayout;

import ca.phon.app.log.LogUtil;
import ca.phon.app.media.TimeUIModel;
import ca.phon.app.session.editor.DelegateEditorAction;
import ca.phon.app.session.editor.EditorEvent;
import ca.phon.app.session.editor.EditorEventType;
import ca.phon.app.session.editor.EditorView;
import ca.phon.app.session.editor.RunOnEDT;
import ca.phon.app.session.editor.SessionEditor;
import ca.phon.app.session.editor.view.timegrid.actions.ZoomInAction;
import ca.phon.media.LongSound;
import ca.phon.media.util.MediaLocator;
import ca.phon.session.MediaSegment;
import ca.phon.session.Participant;
import ca.phon.session.Record;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;

public final class TimeGridView extends EditorView {

	private static final long serialVersionUID = 8442995100291417078L;
	
	public static final String VIEW_TITLE = "Time Grid";
	
	private JToolBar toolbar;
	
	private JPanel tierPanel;
	
	private TimeUIModel timebarModel;
	
	private TimegridWaveformTier wavTier;
	
	private RecordGrid recordGrid;
	
	public TimeGridView(SessionEditor editor) {
		super(editor);
		
		init();
		registerEditorEvents();
		update();
	}
	
	private void init() {
		toolbar = new JToolBar();
		
		tierPanel = new JPanel(new VerticalLayout());
		JScrollPane scroller = new JScrollPane(tierPanel);
		
		timebarModel = new TimeUIModel();
		timebarModel.addPropertyChangeListener((e) -> {
			tierPanel.revalidate();
			scroller.revalidate();
		});
		timebarModel.setPixelsPerSecond(100.0f);
		timebarModel.setStartTime(0.0f);
		timebarModel.setEndTime(0.0f);
		
		wavTier = new TimegridWaveformTier(this);
		wavTier.getWaveformDisplay().setStartTime(0.0f);
		wavTier.getWaveformDisplay().setEndTime(0.0f);
		wavTier.getWaveformDisplay().setPixelsPerSecond(100.0f);
		wavTier.getWaveformDisplay().setTrackViewportHeight(true);
		
		recordGrid = new RecordGrid(this);
		
		addTier(wavTier);
		addTier(recordGrid);
		
		
		
//		List<Participant> speakers = new ArrayList<>();
//		speakers.add(Participant.UNKNOWN);
//		getEditor().getSession().getParticipants().forEach( speakers::add );
//		for(var speaker:speakers) {
//			addTier(new SegmentTier(this, speaker, "Orthography"));
//		}
		
		setLayout(new BorderLayout());
		add(toolbar, BorderLayout.NORTH);
		
		add(scroller, BorderLayout.CENTER);
	}
	
	private void addTier(TimeGridTier tier) {
		tierPanel.add(tier);
		
		var separator =  new JSeparator(SwingConstants.HORIZONTAL);
		separator.setCursor(Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR));
		separator.addMouseMotionListener(new SeparatorMouseListener(tier));
		
		tierPanel.add(separator);
	}
	
	private void update() {
		final File audioFile = getAudioFile();
		if(audioFile != null) {
			loadSessionAudio(audioFile);
		} else {
			// determine time values based on record segements
			float startTime = 0.0f;
			float endTime = 0.0f;
			
			for(Record r:getEditor().getSession().getRecords()) {
				MediaSegment segment = r.getSegment().getGroup(0);
				if(segment != null) {
					float segEnd = (float)(segment.getEndValue() / 1000.0f);
					endTime = Math.max(segEnd, endTime);
				}
			}
			
			timebarModel.setEndTime(endTime);
		}
	}
	
	private void loadSessionAudio(File audioFile) {
		try {
			LongSound ls = LongSound.fromFile(audioFile);
			timebarModel.setEndTime(ls.length());
			wavTier.getWaveformDisplay().setEndTime(ls.length());
			wavTier.getWaveformDisplay().setLongSound(ls);
		} catch (IOException e) {
			LogUtil.severe(e);
		}
	}
	
	private File getAudioFile() {
		File selectedMedia =
				MediaLocator.findMediaFile(getEditor().getProject(), getEditor().getSession());
		if(selectedMedia == null) return null;
		File audioFile = null;

		int lastDot = selectedMedia.getName().lastIndexOf('.');
		String mediaName = selectedMedia.getName();
		if(lastDot >= 0) {
			mediaName = mediaName.substring(0, lastDot);
		}
		if(!selectedMedia.isAbsolute()) selectedMedia =
			MediaLocator.findMediaFile(getEditor().getSession().getMediaLocation(), getEditor().getProject(), getEditor().getSession().getCorpus());

		if(selectedMedia != null) {
			File parentFile = selectedMedia.getParentFile();
			audioFile = new File(parentFile, mediaName + ".wav");

			if(!audioFile.exists()) {
				audioFile = null;
			}
		}
		return audioFile;
	}
	
	public TimeUIModel getTimebarModel() {
		return this.timebarModel;
	}
	
	private final DelegateEditorAction onEditorClosingAct = new DelegateEditorAction(this, "onEditorClosing");
	
	private final DelegateEditorAction onMediaChangedAct = new DelegateEditorAction(this, "onMediaChanged");
	
	private void registerEditorEvents() {
		getEditor().getEventManager().registerActionForEvent(EditorEventType.SESSION_MEDIA_CHANGED, onMediaChangedAct);

		getEditor().getEventManager().registerActionForEvent(EditorEventType.EDITOR_CLOSING, onEditorClosingAct);
	}
	
	private void deregisterEditorEvents() {
		getEditor().getEventManager().removeActionForEvent(EditorEventType.SESSION_MEDIA_CHANGED, onMediaChangedAct);
	
		getEditor().getEventManager().removeActionForEvent(EditorEventType.EDITOR_CLOSING, onEditorClosingAct);
	}
	
	@RunOnEDT
	public void onMediaChanged(EditorEvent ee) {
		
	}
	
	@RunOnEDT
	public void onEditorClosing(EditorEvent ee) {
		deregisterEditorEvents();
	}

	@Override
	public String getName() {
		return VIEW_TITLE;
	}

	@Override
	public ImageIcon getIcon() {
		return IconManager.getInstance().getIcon("misc/table", IconSize.SMALL);
	}

	@Override
	public JMenu getMenu() {
		JMenu menu = new JMenu();
		
		menu.add(new ZoomInAction(this));
		
		return menu;
	}

	private class SeparatorMouseListener extends MouseInputAdapter {
		
		private TimeGridTier tier;
		
		public SeparatorMouseListener(TimeGridTier tier) {
			super();
			
			this.tier = tier;
		}
		
		public TimeGridTier getTier() {
			return this.tier;
		}
		
		@Override
		public void mouseDragged(MouseEvent e) {
			Dimension currentSize = wavTier.getSize();
			Dimension prefSize = wavTier.getPreferredSize();

			prefSize.height = currentSize.height + e.getY();
			if(prefSize.height < 0) prefSize.height = 0;
			
			tier.setPreferredSize(prefSize);
			tierPanel.revalidate();
		}
		
	}
	
}
