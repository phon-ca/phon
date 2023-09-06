package ca.phon.app.session.editor;

import ca.phon.session.*;
import ca.phon.session.Record;
import ca.phon.session.position.SessionLocation;
import ca.phon.session.tierdata.TierData;
import ca.phon.util.Language;

import java.time.*;
import java.util.List;

public record  EditorEventType<T>(String eventName, Class<T> type) {

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

	public record SessionLangChangedData(List<Language> oldLang, List<Language> newLang) { }
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

	/**
	 * View change event type.
	 * In most cases multiple tiers may be specified in TierViewChangedData.tierNames along with a matching set of viewIndices
	 */
	public static enum TierViewChangeType {
		/**
		 * Reload tier view
		 * TierViewChangedData.tierNames and TierViewChangeData.viewIndices will be empty
		 */
		RELOAD,
		TIER_NAME_CHANGE,
		TIER_FONT_CHANGE,
		HIDE_TIER,
		SHOW_TIER,
		LOCK_TIER,
		UNLOCK_TIER,
		ADD_TIER,
		DELETE_TIER,
		/**
		 * Will be called for only one tier at a time.
		 * TierViewChangedData.viewIndices will contain two values, the first will be the original view index the second
		 * wll be the new view index.
		 */
		MOVE_TIER
	}
	public record TierViewChangedData(List<TierViewItem> oldTierView, List<TierViewItem> newTierView,
									  TierViewChangeType changeType, List<String> tierNames, List<Integer> viewIndices) { }
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

	public record ElementAddedData(Transcript.Element element, int elementIndex) { }
	public final static EditorEventType<ElementAddedData> ElementAdded =
			new EditorEventType<>(EditorEventName.ELEMENT_ADDED_EVT.getEventName(), ElementAddedData.class);

	public record ElementDeletedData(Transcript.Element element, int elementIndex) { }
	public final static EditorEventType<ElementDeletedData> ElementDeleted =
			new EditorEventType<>(EditorEventName.ELEMENT_DELETED_EVT.getEventName(), ElementDeletedData.class);

	public record ElementMovedData(Transcript.Element element, int fromElementIndex, int toElementIndex) { }
	public final static EditorEventType<ElementMovedData> ElementMoved =
			new EditorEventType<>(EditorEventName.ELEMENT_MOVED_EVT.getEventName(), ElementMovedData.class);

	public record CommentAddedData(Comment record, int elementIndex) { }
	/**
	 * A comment was added
	 */
	public final static EditorEventType<CommentAddedData> CommentAdded =
			new EditorEventType<>(EditorEventName.COMMENT_ADDED_EVT.getEventName(), CommentAddedData.class);

	public record CommentDeletedData(Comment comment, int elementIndex) { }
	/**
	 * A comment was removed
	 */
	public final static EditorEventType<CommentDeletedData> CommentDeleted =
			new EditorEventType<>(EditorEventName.COMMENT_DELETED_EVT.getEventName(), CommentDeletedData.class);

	public record CommentMovedData(Comment comment, int oldElementIndex, int newElementIndex) { }
	public final static EditorEventType<CommentMovedData> CommentMoved =
			new EditorEventType<>(EditorEventName.COMMENT_MOVED_EVT.getEventName(), CommentMovedData.class);

	public record CommentTypeChangedData(Comment comment, int elementIndex, CommentType oldType, CommentType newType) { }
	public final static EditorEventType<CommentTypeChangedData> CommenTypeChanged =
			new EditorEventType<>(EditorEventName.COMMENT_TYPE_CHANGED_EVT.getEventName(), CommentTypeChangedData.class);

	public record CommentChangedData(Comment comment, int elementIndex, TierData oldComment, TierData newComment) { }
	public final static EditorEventType<CommentChangedData> CommentChanged =
			new EditorEventType<>(EditorEventName.COMMENT_CHANGED_EVT.getEventName(), CommentChangedData.class);

	public record GemAddedData(Gem gem, int elementIndex) { }
	public final static EditorEventType<GemAddedData> GemAdded =
			new EditorEventType<>(EditorEventName.GEM_ADDED_EVT.getEventName(), GemAddedData.class);

	public record GemDeletedData(Gem gem, int elementIndex) { }
	public final static EditorEventType<GemDeletedData> GemDeleted =
			new EditorEventType<>(EditorEventName.GEM_DELETED_EVT.getEventName(), GemDeletedData.class);

	public record GemMovedData(Gem gem, int oldElementIndex, int newElementIndex) { }
	public final static EditorEventType<GemMovedData> GemMoved =
			new EditorEventType<>(EditorEventName.GEM_MOVED_EVT.getEventName(), GemMovedData.class);

	public record GemChangedData(Gem gem, int elementIndex, String oldLabel, String newLabel) { }
	public final static EditorEventType<GemChangedData> GemChanged =
			new EditorEventType<>(EditorEventName.GEM_CHANGED_EVT.getEventName(), GemChangedData.class);

	public record GemTypeChangedData(Gem gem, int elementIndex, GemType oldType, GemType newType) { }
	public final static EditorEventType<GemTypeChangedData> GemTypeChanged =
			new EditorEventType<>(EditorEventName.GEM_TYPE_CHANGED_EVT.getEventName(), GemTypeChangedData.class);

	public record RecordAddedData(Record recrod, int elementIndex, int recordIndex) { }
	/**
	 * Called when a new record is added to the open session
	 */
	public final static EditorEventType<RecordAddedData> RecordAdded =
			new EditorEventType<>(EditorEventName.RECORD_ADDED_EVT.getEventName(), RecordAddedData.class);
	public record RecordDeletedData(Record record, int elementIndex, int recordIndex) { }
	/**
	 * Called when a record is removed from the open session
	 */
	public final static EditorEventType<RecordDeletedData> RecordDeleted =
			new EditorEventType<>(EditorEventName.RECORD_DELETED_EVT.getEventName(), RecordDeletedData.class);
	public record RecordMovedData(Record record, int fromElementIndex, int fromRecordIndex, int toElementIndex, int toRecordIndex) { }
	/**
	 * Called when a record is moved
	 */
	public final static EditorEventType<RecordMovedData> RecordMoved =
			new EditorEventType<>(EditorEventName.RECORD_MOVED_EVT.getEventName(), RecordMovedData.class);
	public record RecordChangedData(Record record, int elementIndex, int recordIndex) { }
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

	public record TierChangeData(Tier<?> tier, Object oldValue, Object newValue) { }
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
