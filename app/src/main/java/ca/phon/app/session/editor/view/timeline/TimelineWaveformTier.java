
package ca.phon.app.session.editor.view.timeline;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;

import javax.swing.event.MouseInputAdapter;

import ca.phon.app.media.TimeUIModel;
import ca.phon.app.media.TimeUIModel.Interval;
import ca.phon.app.media.WaveformDisplay;
import ca.phon.app.session.editor.DelegateEditorAction;
import ca.phon.app.session.editor.EditorEvent;
import ca.phon.app.session.editor.EditorEventType;
import ca.phon.app.session.editor.RunOnEDT;
import ca.phon.session.Record;
import ca.phon.session.SystemTierType;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.ui.menu.MenuBuilder;

public class TimelineWaveformTier extends TimelineTier  {

	private static final long serialVersionUID = -2864344329017995791L;

	private WaveformDisplay wavDisplay;
	
	public TimelineWaveformTier(TimelineView parent) {
		super(parent);
		
		init();
		setupEditorEvents();
	}
	
	private void init() {
		final TimeUIModel timeModel = getParentView().getTimeModel();
		
		wavDisplay = new WaveformDisplay(timeModel);
		Insets channelInsets = new Insets(wavDisplay.getChannelInsets().top, timeModel.getTimeInsets().left,
				wavDisplay.getChannelInsets().bottom, timeModel.getTimeInsets().right);
		wavDisplay.setChannelInsets(channelInsets);
		wavDisplay.setPreferredChannelHeight(50);
		wavDisplay.setTrackViewportHeight(true);
		wavDisplay.setBackground(Color.WHITE);
		wavDisplay.setOpaque(true);
		
		wavDisplay.getPreferredSize();
		
		wavDisplay.addMouseListener(selectionListener);
		wavDisplay.addMouseMotionListener(selectionListener);
		
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
	
	public void toggleVisible() {
		setVisible(!isVisible());
	}
	
	@Override
	public void setupContextMenu(MouseEvent me, MenuBuilder builder) {
		final PhonUIAction toggleVisiblityAct = new PhonUIAction(this, "toggleVisible");
		toggleVisiblityAct.putValue(PhonUIAction.NAME, 
				(isVisible() ? "Hide waveform" : "Show waveform"));
		toggleVisiblityAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Toggle waveform visibility");
		builder.addItem(".", toggleVisiblityAct);
	}
	
	@RunOnEDT
	public void onRecordChange(EditorEvent ee) {
		wavDisplay.repaint(wavDisplay.getVisibleRect());
	}
	
	/* Selection using mouse */
	private Interval selectionInterval = null;
	
	private float initialSelectionTime = -1.0f;
	
	private MouseInputAdapter selectionListener = new MouseInputAdapter() {

		@Override
		public void mousePressed(MouseEvent e) {
			wavDisplay.requestFocus();
			if(wavDisplay.getUI().getCurrentlyDraggedMarker() != null) {
				initialSelectionTime = -1.0f;
				return;
			}
			
			if(e.getButton() == MouseEvent.BUTTON1) {
				
				if(selectionInterval != null) {
					getTimeModel().removeInterval(selectionInterval);
					selectionInterval = null;
				}
				
				initialSelectionTime = getTimeModel().timeAtX(e.getX());
				
				// TODO change media playback position
			}
		}
		
		@Override
		public void mouseDragged(MouseEvent e) {
			if(initialSelectionTime > 0) {
				float currentTime = getTimeModel().timeAtX(e.getX());
				float diff = currentTime - initialSelectionTime;
				if(selectionInterval == null) {
					float intervalStartTime, intervalEndTime = 0.0f;
					if(diff > 0) {
						intervalStartTime = initialSelectionTime;
						intervalEndTime = currentTime; 
						
					} else {
						intervalStartTime = currentTime;
						intervalEndTime = initialSelectionTime;
					}
					
					selectionInterval = getTimeModel().addInterval(intervalStartTime, intervalEndTime);
					selectionInterval.setColor(new Color(50, 125, 200, 50));
					
					if(diff > 0) {
						wavDisplay.getUI().beginDrag(selectionInterval, selectionInterval.getEndMarker());
					} else {
						wavDisplay.getUI().beginDrag(selectionInterval, selectionInterval.getStartMarker());
					}
				}
			}
		}
		
	};
		
}
