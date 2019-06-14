package ca.phon.app.session.editor.view.timegrid;

import java.awt.BorderLayout;
import java.io.File;
import java.io.IOException;

import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JToolBar;

import ca.phon.app.log.LogUtil;
import ca.phon.app.media.WaveformDisplayScrollPane;
import ca.phon.app.session.editor.DelegateEditorAction;
import ca.phon.app.session.editor.EditorEvent;
import ca.phon.app.session.editor.EditorEventType;
import ca.phon.app.session.editor.EditorView;
import ca.phon.app.session.editor.RunOnEDT;
import ca.phon.app.session.editor.SessionEditor;
import ca.phon.media.LongSound;
import ca.phon.media.util.MediaLocator;
import ca.phon.session.Session;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;

public class TimeGridView extends EditorView {

	private static final long serialVersionUID = 8442995100291417078L;
	
	public static final String VIEW_TITLE = "Time Grid";
	
	private JToolBar toolbar;
	
	private TimegridWaveformDisplay wavDisplay;

	public TimeGridView(SessionEditor editor) {
		super(editor);
		
		init();
		registerEditorEvents();
		update();
	}
	
	private void init() {
		toolbar = new JToolBar();
		
		wavDisplay = new TimegridWaveformDisplay();
		wavDisplay.setStartTime(0.0f);
		wavDisplay.setEndTime(0.0f);
		wavDisplay.setPixelsPerSecond(100.0f);
		wavDisplay.setTrackViewportHeight(true);
		
		WaveformDisplayScrollPane scroller = new WaveformDisplayScrollPane(wavDisplay);

		setLayout(new BorderLayout());
		add(toolbar, BorderLayout.NORTH);
		add(scroller, BorderLayout.CENTER);
//		JPanel centerPanel = new JPanel(new BorderLayout());
//		centerPanel.add(scroller, BorderLayout.NORTH);
//		add(centerPanel, BorderLayout.CENTER);
	}
	
	private void update() {
		loadSessionAudio();
	}
	
	private void loadSessionAudio() {
		final File audioFile = getAudioFile();
		if(audioFile == null) return;
		
		try {
			LongSound ls = LongSound.fromFile(audioFile);
			wavDisplay.setEndTime(ls.length());
			wavDisplay.setLongSound(ls);
			revalidate();
		} catch (IOException e) {
			LogUtil.severe(e);
		}
	}
	
	public File getAudioFile() {
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
		return new JMenu();
	}

}
