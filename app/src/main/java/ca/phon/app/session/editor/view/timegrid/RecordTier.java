package ca.phon.app.session.editor.view.timegrid;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import com.teamdev.jxbrowser.chromium.internal.ipc.message.SetupProtocolHandlerMessage;

import ca.phon.app.media.Timebar;
import ca.phon.app.session.editor.DelegateEditorAction;
import ca.phon.app.session.editor.EditorAction;
import ca.phon.app.session.editor.EditorEvent;
import ca.phon.app.session.editor.EditorEventType;
import ca.phon.app.session.editor.RunOnEDT;
import ca.phon.app.session.editor.SessionEditor;
import ca.phon.session.MediaSegment;
import ca.phon.session.Participant;
import ca.phon.session.Record;
import ca.phon.session.Session;
import ca.phon.session.SystemTierType;
import ca.phon.ui.action.PhonActionEvent;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.ui.fonts.FontPreferences;
import ca.phon.ui.menu.MenuBuilder;

public class RecordTier extends TimeGridTier {
	
	private RecordGrid recordGrid;
	
	private Map<Participant, Boolean> speakerVisibility = new HashMap<>();
	
	public RecordTier(TimeGridView parent) {
		super(parent);
	
		init();
		setupEditorEvents();
	}

	private void init() {
		Session session = getParentView().getEditor().getSession();
		recordGrid = new RecordGrid(getTimeModel(), session);
		
		recordGrid.setFont(FontPreferences.getTierFont());
		setupSpeakers();
		recordGrid.addTier(SystemTierType.Orthography.getName());
		
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(recordGrid, BorderLayout.CENTER);
	}
	
	private final DelegateEditorAction onRecordChange = 
			new DelegateEditorAction(this, "onRecordChange");
		
	private final DelegateEditorAction onTierChangedAct = 
			new DelegateEditorAction(this, "onTierChanged");
	
	private final DelegateEditorAction onParticipantRemoveAct =
			new DelegateEditorAction(this, "onParticipantRemoved");
	
	private final DelegateEditorAction onParticipantAddedAct =
			new DelegateEditorAction(this, "onParticipantAdded");
	
	private void setupEditorEvents() {
		getParentView().getEditor().getEventManager()
			.registerActionForEvent(EditorEventType.RECORD_CHANGED_EVT, onRecordChange);
		getParentView().getEditor().getEventManager()
			.registerActionForEvent(EditorEventType.TIER_CHANGED_EVT, onTierChangedAct);
		getParentView().getEditor().getEventManager()
			.registerActionForEvent(EditorEventType.PARTICIPANT_REMOVED, onParticipantRemoveAct);
		getParentView().getEditor().getEventManager()
			.registerActionForEvent(EditorEventType.PARTICIPANT_ADDED, onParticipantAddedAct);
	}
	
	private void deregisterEditorEvents() {
		getParentView().getEditor().getEventManager()
			.removeActionForEvent(EditorEventType.RECORD_CHANGED_EVT, onRecordChange);
		getParentView().getEditor().getEventManager()
			.removeActionForEvent(EditorEventType.TIER_CHANGED_EVT, onTierChangedAct);
		getParentView().getEditor().getEventManager()
			.removeActionForEvent(EditorEventType.PARTICIPANT_REMOVED, onParticipantRemoveAct);
		getParentView().getEditor().getEventManager()
			.removeActionForEvent(EditorEventType.PARTICIPANT_ADDED, onParticipantAddedAct);
	}
	
	/* Editor events */
	@RunOnEDT
	public void onRecordChange(EditorEvent evt) {
		Record r = (Record)evt.getEventData();
		
		getTimeModel().clearIntervals();
		MediaSegment segment = r.getSegment().getGroup(0);
		var segStartTime = segment.getStartValue() / 1000.0f;
		var segEndTime = segment.getEndValue() / 1000.0f;
		getTimeModel().addInterval(segStartTime, segEndTime);

		recordGrid.setCurrentRecord(r);
	}
	
	@RunOnEDT
	public void onParticipantRemoved(EditorEvent ee) {
		speakerVisibility.remove((Participant)ee.getEventData());
		setupSpeakers();
	}
	
	@RunOnEDT
	public void onParticipantAdded(EditorEvent ee) {
		setupSpeakers();
	}
	
	@RunOnEDT
	public void onTierChanged(EditorEvent ee) {
		if(SystemTierType.Orthography.getName().equals(ee.getEventData().toString())
				|| SystemTierType.Segment.getName().equals(ee.getEventData().toString())) {
			recordGrid.repaint();
		}
	}
	
	/**
	 * Is the speaker visible? 
	 * 
	 * @param speaker
	 * @return
	 */
	public boolean isSpeakerVisible(Participant speaker) {
		boolean retVal = true;
		
		if(speakerVisibility.containsKey(speaker))
			retVal = speakerVisibility.get(speaker);
		
		return retVal;
	}
	
	public void setSpeakerVisible(Participant speaker, boolean visible) {
		speakerVisibility.put(speaker, visible);
		setupSpeakers();
	}
	
	public void toggleSpeaker(PhonActionEvent pae) {
		Participant speaker = (Participant)pae.getData();
		setSpeakerVisible(speaker, !isSpeakerVisible(speaker));
	}
	
	private void setupSpeakers() {
		Session session = getParentView().getEditor().getSession();
		
		var speakerList = new ArrayList<Participant>();
		for(var speaker:session.getParticipants()) {
			if(isSpeakerVisible(speaker))
				speakerList.add(speaker);
		}
		if(isSpeakerVisible(Participant.UNKNOWN))
			speakerList.add(Participant.UNKNOWN);
		recordGrid.setSpeakers(speakerList);
	}
	
	public void setupContextMenu(MenuBuilder builder) {
		setupSpeakerMenu(builder);
	}
	
	public void setupSpeakerMenu(MenuBuilder builder) {
		Session session = getParentView().getEditor().getSession();
		for(var speaker:session.getParticipants()) {
			final PhonUIAction toggleSpeakerAct = new PhonUIAction(this, "toggleSpeaker", speaker);
			toggleSpeakerAct.putValue(PhonUIAction.NAME, speaker.toString());
			toggleSpeakerAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Toggle speaker " + speaker);
			toggleSpeakerAct.putValue(PhonUIAction.SELECTED_KEY, isSpeakerVisible(speaker));
			final JCheckBoxMenuItem toggleSpeakerItem = new JCheckBoxMenuItem(toggleSpeakerAct);
			builder.addItem(".", toggleSpeakerItem);
		}
		
		final PhonUIAction toggleUnknownAct = new PhonUIAction(this, "toggleSpeaker", Participant.UNKNOWN);
		toggleUnknownAct.putValue(PhonUIAction.NAME, Participant.UNKNOWN.toString());
		toggleUnknownAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Toggle speaker " + Participant.UNKNOWN);
		toggleUnknownAct.putValue(PhonUIAction.SELECTED_KEY, isSpeakerVisible(Participant.UNKNOWN));
		final JCheckBoxMenuItem toggleUnknownItem = new JCheckBoxMenuItem(toggleUnknownAct);
		builder.addItem(".", toggleUnknownItem);
	}
	
	public boolean isResizeable() {
		return false;
	}

	@Override
	public void onClose() {
		deregisterEditorEvents();
	}
	
}
