package ca.phon.app.session.editor;

import ca.phon.app.query.EditQueryDialog;
import ca.phon.query.db.ResultSet;
import ca.phon.session.*;
import ca.phon.session.Record;
import ca.phon.session.position.SessionLocation;

import java.time.*;
import java.util.List;

public record EditorEventType<T>(String eventName, Class<T> type) {

	/**
	 * Fired after the editor has been initialized
	 */
	public final static EditorEventType<Void> EditorFinishedLoading =
			new EditorEventType<>(EditorEventName.EDITOR_FINISHED_LOADING.getEventName(), Void.class);

	/**
	 * Editor should reload contents from disk
	 */
	public final static EditorEventType<Void> EditorReloadFromDisk =
			new EditorEventType<>(EditorEventName.EDITOR_RELOAD_FROM_DISK.getEventName(), Void.class);

	/**
	 * Called when the editor is about to close
	 */
	public final static EditorEventType<Void> EditorClosing =
			new EditorEventType<>(EditorEventName.EDITOR_CLOSING.getEventName(), Void.class);


	/**
	 * Editor has saved the session
	 */
	public final static EditorEventType<Session> SessionSaved =
			new EditorEventType<>(EditorEventName.EDITOR_SAVED_SESSION.getEventName(), Session.class);

	/**
	 * Session has changed in editor
	 */
	public final static EditorEventType<Session> SessionChanged =
			new EditorEventType<>(EditorEventName.SESSION_CHANGED_EVT.getEventName(), Session.class);

	public record SessionDateChangedData(LocalDate oldDate, LocalDate newDate) { }
	/**
	 * Session date change
	 */
	public final static EditorEventType<SessionDateChangedData> SessionDateChanged =
			new EditorEventType<>(EditorEventName.SESSION_DATE_CHANGED.getEventName(), SessionDateChangedData.class);

	public record SessionLangChangedData(String oldLang, String newLang) { }
	/**
	 * Session language change
	 */
	public final static EditorEventType<SessionLangChangedData> SessionLangChanged =
			new EditorEventType<>(EditorEventName.SESSION_LANG_CHANGED.getEventName(), SessionLangChangedData.class);

	public record SessionMediaChangedData(String oldMedia, String newMedia) { }
	/**
	 * Session media change
	 */
	public final static EditorEventType<SessionMediaChangedData> SessionMediaChanged =
			new EditorEventType<>(EditorEventName.SESSION_MEDIA_CHANGED.getEventName(), SessionMediaChangedData.class);

	public record TierViewChangedData(List<TierViewItem> oldTierView, List<TierViewItem> newTierView) { }
	/**
	 * Tier view has changed
	 */
	public final static EditorEventType<TierViewChangedData> TierViewChanged =
			new EditorEventType<>(EditorEventName.TIER_VIEW_CHANGED_EVT.getEventName(), TierViewChangedData.class);

	public record TierLockChangedData(TierViewItem tierViewItem, boolean locked) { }
	/**
	 * Tier lock has changed
	 */
	public final static EditorEventType<TierLockChangedData> TierLockChanged =
			new EditorEventType<>(EditorEventName.TIER_LOCK_CHANGED_EVT.getEventName(), TierLockChangedData.class);

	/**
	 * A participant was added
	 */
	public final static EditorEventType<Participant> ParticipantAdded =
			new EditorEventType<>(EditorEventName.PARTICIPANT_ADDED.getEventName(), Participant.class);

	/**
	 * A participant was removed
	 */
	public final static EditorEventType<Participant> ParticipantRemoved =
			new EditorEventType<>(EditorEventName.PARTICIPANT_REMOVED.getEventName(), Participant.class);

	/**
	 * Participant data has changed
	 */
	public final static EditorEventType<Participant> ParticipantChanged =
			new EditorEventType<>(EditorEventName.PARTICIPANT_CHANGED.getEventName(), Participant.class);

	public record RecordAddedData(int index, Record record) { }
	/**
	 * Called when a new record is added to the open session
	 */
	public final static EditorEventType<RecordAddedData> RecordAdded =
			new EditorEventType<>(EditorEventName.RECORD_ADDED_EVT.getEventName(), RecordAddedData.class);
	public record RecordDeletedData(int index, Record record) { }
	/**
	 * Called when a record is removed from the open session
	 */
	public final static EditorEventType<RecordDeletedData> RecordDeleted =
			new EditorEventType<>(EditorEventName.RECORD_DELETED_EVT.getEventName(), RecordDeletedData.class);
	public record RecordMovedData(int fromIndex, int toIndex, Record record) { }
	/**
	 * Called when a record is moved
	 */
	public final static EditorEventType<RecordMovedData> RecordMoved =
			new EditorEventType<>(EditorEventName.RECORD_MOVED_EVT.getEventName(), RecordMovedData.class);
	public record RecordChangedData(int index, Record record) { }
	/**
	 * Current record has changed in editor
	 */
	public final static EditorEventType<RecordChangedData> RecordChanged =
			new EditorEventType<>(EditorEventName.RECORD_CHANGED_EVT.getEventName(), RecordChangedData.class);

	/**
	 * Called when the editor wants a refresh of the view for the
	 * current record
	 */
	public final static EditorEventType<RecordChangedData> RecordRefresh =
			new EditorEventType<>(EditorEventName.RECORD_REFRESH_EVT.getEventName(), RecordChangedData.class);

	/**
	 * Position of caret changed in record tiers
	 */
	public final static EditorEventType<SessionLocation> SessionLocationChanged =
			new EditorEventType<>(EditorEventName.SESSION_LOCATION_CHANGED_EVT.getEventName(), SessionLocation.class);

	public record SpeakerChangedData(Record record, Participant oldSpeaker, Participant newSpeaker) { }
	/**
	 * Change to speaker for record
	 */
	public final static EditorEventType<SpeakerChangedData> SpeakerChanged =
			new EditorEventType<>(EditorEventName.SPEAKER_CHANGE_EVT.getEventName(), SpeakerChangedData.class);

	public record TierChangeData(Tier<?> tier, int group, Object oldValue, Object newValue) { }
	/**
	 * Changes in tier data, usually fired during value adjustments.
	 *
	 */
	public final static EditorEventType<TierChangeData> TierChange =
			new EditorEventType<>(EditorEventName.TIER_CHANGE_EVT.getEventName(), TierChangeData.class);

	/**
	 * Changes in tier data. This event is fired when tier data has been
	 * committed (such as when the current record field is changed)
	 */
	public final static EditorEventType<TierChangeData> TierChanged =
			new EditorEventType<>(EditorEventName.TIER_CHANGED_EVT.getEventName(), TierChangeData.class);

	public record RecordExcludedChangedData(Record record, boolean excluded) { }
	/**
	 * Fired when user clicks on the exclude record from searches box
	 */
	public final static EditorEventType<RecordExcludedChangedData> RecordExcludedChanged =
			new EditorEventType<>(EditorEventName.RECORD_EXCLUDE_CHANGE_EVT.getEventName(), RecordExcludedChangedData.class);

	/**
	 * Fired when the number of phonetic groups changes
	 */
	public final static EditorEventType<Void> GroupListChange =
			new EditorEventType<>(EditorEventName.GROUP_LIST_CHANGE_EVT.getEventName(), Void.class);

	/**
	 * Notifies on changes to the modification flag
	 */
	public final static EditorEventType<Boolean> ModifiedFlagChanged =
			new EditorEventType<>(EditorEventName.MODIFIED_FLAG_CHANGED.getEventName(), Boolean.class);

	/**
	 * Request playback of provided media segment
	 */
	public final static EditorEventType<MediaSegment> SegmentPlayback =
			new EditorEventType<>(EditorEventName.SEGMENT_PLAYBACK_EVENT.getEventName(), MediaSegment.class);

}
