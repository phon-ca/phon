
package ca.phon.app.session.editor.view.timegrid;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Insets;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;

import ca.phon.app.media.TimeUIModel;
import ca.phon.app.media.WaveformDisplay;
import ca.phon.app.session.editor.DelegateEditorAction;
import ca.phon.app.session.editor.EditorEvent;
import ca.phon.app.session.editor.EditorEventType;
import ca.phon.app.session.editor.RunOnEDT;
import ca.phon.session.Record;
import ca.phon.session.SystemTierType;

public class TimegridWaveformTier extends TimeGridTier  {

	private static final long serialVersionUID = -2864344329017995791L;

	private WaveformDisplay wavDisplay;
	
	public TimegridWaveformTier(TimeGridView parent) {
		super(parent);
		
		init();
		setupEditorEvents();
	}
	
	private void init() {
		final TimeUIModel timeModel = getParentView().getTimeModel();
		timeModel.addPropertyChangeListener(propListener);
		
		wavDisplay = new WaveformDisplay(timeModel);
		Insets channelInsets = new Insets(wavDisplay.getChannelInsets().top, timeModel.getTimeInsets().left,
				wavDisplay.getChannelInsets().bottom, timeModel.getTimeInsets().right);
		wavDisplay.setChannelInsets(channelInsets);
		wavDisplay.setPreferredChannelHeight(50);
		wavDisplay.setTrackViewportHeight(true);
		wavDisplay.setBackground(Color.WHITE);
		wavDisplay.setOpaque(true);
		
		wavDisplay.getPreferredSize();
		
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(wavDisplay, BorderLayout.CENTER);
	}
	
	public WaveformDisplay getWaveformDisplay() {
		return this.wavDisplay;
	}
	
	private final DelegateEditorAction onRecordChange = 
			new DelegateEditorAction(this, "onRecordChange");
	
	private void setupEditorEvents() {
		getParentView().getEditor().getEventManager().registerActionForEvent(EditorEventType.RECORD_CHANGED_EVT, onRecordChange);
	}
	
	@RunOnEDT
	public void onRecordChange(EditorEvent ee) {
		wavDisplay.repaint(wavDisplay.getVisibleRect());
	}
		
	private final PropertyChangeListener propListener = (e) -> {
		if("intervalCount".equals(e.getPropertyName())) {
			wavDisplay.repaint(wavDisplay.getVisibleRect());
		}
	};
	
}
