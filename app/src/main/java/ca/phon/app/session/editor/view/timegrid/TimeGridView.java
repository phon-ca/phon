package ca.phon.app.session.editor.view.timegrid;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSlider;
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
import ca.phon.app.session.editor.view.timegrid.actions.ZoomAction;
import ca.phon.media.LongSound;
import ca.phon.media.util.MediaLocator;
import ca.phon.session.MediaSegment;
import ca.phon.session.Participant;
import ca.phon.session.Record;
import ca.phon.session.SystemTierType;
import ca.phon.ui.fonts.FontPreferences;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;
import groovy.swing.factory.GlueFactory;

public final class TimeGridView extends EditorView {

	private static final long serialVersionUID = 8442995100291417078L;
	
	public static final String VIEW_TITLE = "Time Grid";
	
	/**
	 * Values for the zoom bar
	 */
	public static final float zoomValues[] = { 25.0f, 50.0f, 100.0f, 200.0f, 400.0f, 800.0f, 1600.0f };
	
	private static final int defaultZoomIdx = 3;
	
	private JToolBar toolbar;
	
	private JSlider zoomSlider;
	
	private JPanel tierPanel;
	
	/**
	 * Default {@link TimeUIModel} which should be
	 * used by most tier components
	 */
	private TimeUIModel timeModel;
	
	private TimegridWaveformTier wavTier;
	
	private RecordTier recordGrid;
	
	public TimeGridView(SessionEditor editor) {
		super(editor);
		
		init();
		registerEditorEvents();
		update();
	}
	
	private void init() {
		toolbar = setupToolbar();
		
		tierPanel = new JPanel(new GridBagLayout());
		JScrollPane scroller = new JScrollPane(tierPanel);
		
		timeModel = new TimeUIModel();
		timeModel.addPropertyChangeListener((e) -> {
			if(e.getPropertyName().equals("pixelsPerSecond")) {
				int zoomIdx = Arrays.binarySearch(zoomValues, (float)e.getNewValue());
				if(zoomIdx >= 0)
					zoomSlider.setValue(zoomIdx);
			}
			tierPanel.revalidate();
		});
		timeModel.setPixelsPerSecond(100.0f);
		timeModel.setStartTime(0.0f);
		timeModel.setEndTime(0.0f);
		
		wavTier = new TimegridWaveformTier(this);
		wavTier.getPreferredSize();
		
		recordGrid = new RecordTier(this);
		
		addTier(wavTier);
		addTier(recordGrid);
		
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.gridx = 0;
		gbc.gridy = tierIdx++;
		tierPanel.add(Box.createVerticalGlue(), gbc);

		setLayout(new BorderLayout());
		add(toolbar, BorderLayout.NORTH);
		
		add(scroller, BorderLayout.CENTER);
	}
	
	private JToolBar setupToolbar() {
		JToolBar toolbar = new JToolBar();
		
		zoomSlider = new JSlider(SwingConstants.HORIZONTAL, 0, zoomValues.length-1, defaultZoomIdx);
		zoomSlider.setPaintLabels(false);
		zoomSlider.setPaintTicks(true);
		zoomSlider.setPaintTrack(true);
		zoomSlider.setSnapToTicks(true);
		zoomSlider.putClientProperty("JComponent.sizeVariant", "small");
		
		zoomSlider.addChangeListener( (e) -> {
			getTimeModel().setPixelsPerSecond(zoomValues[zoomSlider.getValue()]);
		});
		toolbar.add(zoomSlider);
		
		return toolbar;
	}
	
	private int tierIdx = 0;
	private void addTier(TimeGridTier tier) {
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 1.0;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 0;
		gbc.gridy = tierIdx++;
		
		tierPanel.add(tier, gbc);
		
		if(tier.isResizeable()) {
			var separator =  new JSeparator(SwingConstants.HORIZONTAL);
			separator.setCursor(Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR));
			separator.addMouseMotionListener(new SeparatorMouseListener(tier));
			
			gbc.gridy = tierIdx++;
			
			tierPanel.add(separator, gbc);
		}
	}
	
	private void update() {
		final File audioFile = getAudioFile();
		if(audioFile != null && audioFile.exists() && audioFile.canRead()) {
			loadSessionAudio(audioFile);
		} else {
			// determine time values based on record segements
			float endTime = 0.0f;
			
			for(Record r:getEditor().getSession().getRecords()) {
				MediaSegment segment = r.getSegment().getGroup(0);
				if(segment != null) {
					float segEnd = (float)(segment.getEndValue() / 1000.0f);
					endTime = Math.max(segEnd, endTime);
				}
			}
			
			timeModel.setEndTime(endTime);
		}
	}
	
	private void loadSessionAudio(File audioFile) {
		try {
			LongSound ls = LongSound.fromFile(audioFile);
			timeModel.setEndTime(ls.length());
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
	
	public TimeUIModel getTimeModel() {
		return this.timeModel;
	}
	
	public void scrollToTime(float time) {
		var x = getTimeModel().xForTime(time);
		var rect = tierPanel.getVisibleRect();
		rect.x = (int)x;
		tierPanel.scrollRectToVisible(rect);
	}
	
	/* Editor actions */
	
	private final DelegateEditorAction onMediaChangedAct = new DelegateEditorAction(this, "onMediaChanged");
	
	private final DelegateEditorAction onRecordChangeAct = new DelegateEditorAction(this, "onRecordChanged");
	
	private final DelegateEditorAction onTierChangedAct = new DelegateEditorAction(this, "onTierChanged");
	
	private void registerEditorEvents() {
		getEditor().getEventManager().registerActionForEvent(EditorEventType.SESSION_MEDIA_CHANGED, onMediaChangedAct);
		getEditor().getEventManager().registerActionForEvent(EditorEventType.RECORD_SPEAKER_CHANGED_EVT, onRecordChangeAct);
		getEditor().getEventManager().registerActionForEvent(EditorEventType.TIER_CHANGED_EVT, onTierChangedAct);
	}
	
	private void deregisterEditorEvents() {
		getEditor().getEventManager().removeActionForEvent(EditorEventType.SESSION_MEDIA_CHANGED, onMediaChangedAct);
		getEditor().getEventManager().removeActionForEvent(EditorEventType.RECORD_SPEAKER_CHANGED_EVT, onRecordChangeAct);
		getEditor().getEventManager().removeActionForEvent(EditorEventType.TIER_CHANGED_EVT, onTierChangedAct);
	}
	
	@RunOnEDT
	public void onMediaChanged(EditorEvent ee) {
		update();
	}
	
	@RunOnEDT
	public void onRecordChanged(EditorEvent ee) {
		recordGrid.repaint();
	}
	
	@RunOnEDT
	public void onTierChanged(EditorEvent ee) {
		if(SystemTierType.Orthography.getName().equals(ee.getEventData().toString())) {
			recordGrid.repaint();
		}
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
		
		menu.add(new ZoomAction(this, 1));
		menu.add(new ZoomAction(this, -1));
		
		return menu;
	}
	
	@Override
	public void onClose() {
		super.onClose();
		
		deregisterEditorEvents();
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
