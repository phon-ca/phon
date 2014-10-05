/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2008 The Phon Project, Memorial University <http://phon.ling.mun.ca>
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package ca.phon.app.session.editor;

import ca.phon.session.TierListener;

/**
 * The class holds the event names that are sent
 * by the editor model while the editor is displayed.
 *
 * 
 */
public class EditorEventType {

	/**
	 * Modification event.  Any event which starts with
	 * '_x_' will set the modified flag.  This is a generic
	 * event that should only be used when all other events
	 * do not apply or when a new event is needed by a module.
	 *
	 */
	public final static String MODIFICATION_EVENT = "_x_";

	/**
	 * Called when the editor is about to close
	 * data: null
	 */
	public final static String EDITOR_CLOSING = "_EDITOR_CLOSING_";

	/**
	 * Called after the editor has been initialized
	 * data: null
	 */
	public final static String EDITOR_FINISHED_LOADING = "_EDITOR_FINSIHED_LOADING_";

	/**
	 * Called when a new record is added to the open session
	 * data: 
	 */
	public final static String RECORD_ADDED_EVT = MODIFICATION_EVENT + "RECORD_ADDED_";

	/**
	 * Called when a record is removed from the open session
	 * data:
	 */
	public final static String RECORD_DELETED_EVT = MODIFICATION_EVENT + "RECORD_DELETED_";
	
	/**
	 * Called when a record is moved
	 * data: record
	 */
	public final static String RECORD_MOVED_EVT = MODIFICATION_EVENT + "RECORD_MOVED_";

	/**
	 * Called when the editor wants a refresh of the view for the
	 * current record
	 * data:
	 */
	public final static String RECORD_REFRESH_EVT = "_RECORD_REFRESH_";

	/**
	 * New session
	 * data: session handle - ITranscript
	 */
	public final static String SESSION_CHANGED_EVT = "_SESSION_CHANGED_";

	/**
	 * Record change
	 * data: record handle - IUtterance
	 */
	public final static String RECORD_CHANGED_EVT = "_RECORD_CHANGED_";

	/**
	 * Highlighted search result change
	 * data: search result handle - SerchResult
	 */
	public final static String SEARCH_RESULT_CHANGED_EVT = "_SEARCH_RESULT_CHANGED_";

	/**
	 * Selected range change
	 * data: new range to select - SessionRange
	 */
	public final static String SELECTED_RANGE_CHANGED_EVT = "_SELECTED_RANGE_CHANGED_EVT_";

	/**
	 * Position of caret changed in record tiers
	 * data: new session location - SessionLocation
	 */
	public final static String SESSION_LOCATION_CHANGED_EVT = "_SESSION_LOCATION_CHANGED_EVT_";

	/**
	 * Changes in tier data.
	 * 
	 * data: the tier name - String
	 */
	public final static String TIER_CHANGE_EVT = MODIFICATION_EVENT + "TIER_CHANGE_";
	
	/**
	 * Changes in tier data.  Unlike {@link TierListener}s, this event is only called after 
	 * focus has changed in a component.  This is useful for view which should not update 
	 * data as it is typed into editor fields. To listen for tier changes as they occur, use {@link TierListener}s
	 * instead.
	 * 
	 * data: the tier name - String
	 */
	public final static String TIER_CHANGED_EVT = MODIFICATION_EVENT + "TIER_CHANGED_";
	
	/**
	 * Fired when user clicks on the exclude record box
	 * Data: null
	 */
	public static final String RECORD_EXCLUDE_CHANGE_EVT = "_record_exclude_change_";

	/**
	 * Fired when the number of phonetic groups changes
	 * data: null
	 */
	public final static String GROUP_LIST_CHANGE_EVT = MODIFICATION_EVENT + "GROUP_LIST_CHANGE_EVT_";

	/**
	 * Change to current record's segment
	 * data: null
	 */
	public final static String SEGMENT_CHANGED_EVT = MODIFICATION_EVENT + "SEGMENT_CHANGED_EVT_";

	/**
	 * Event for playing a segment
	 * data: Tuple<Long, Long>
	 */
	public static final String SEGMENT_PLAYBACK_EVENT = "_segment_playback_event_";

	/**
	 * Notifies on changes to the modification bit
	 * data: null
	 */
	public final static String MODIFIED_FLAG_CHANGED = "_MODIFIED_FLAG_CHANGED_";


	/**
	 * Search completed event
	 * data:
	 */
	public final static String SEARCH_FINISHED_EVT = "_SEARCH_FINISHED_";


	/**
	 * Event fired when a result is hit
	 * data: search result - 
	 */
	public static final String FIND_REPLACE_RESULT_HIT = "_find_replace_result_hit_";

	/**
	 * A participant has changed
	 * data: the changed participant - IParticipant
	 */
	public final static String PARTICIPANT_CHANGED = MODIFICATION_EVENT + "PARTICIPANT_CHANGED_";

	/**
	 * A participant was added
	 * data: added participant - IParticipant
	 */
	public final static String PARTICIPANT_ADDED = MODIFICATION_EVENT + "PARTICIPANT_ADDED_";

	/**
	 * A participant was removed
	 * data: removed participant - IParticipant
	 */
	public final static String PARTICIPANT_REMOVED = MODIFICATION_EVENT + "PARTICIPANT_REMOVED_";

	/**
	 * Session date change
	 * data:
	 */
	public final static String SESSION_DATE_CHANGED = MODIFICATION_EVENT + "SESSION_DATE_CHANGED_";

	/**
	 * Session media change
	 * data: new media - String
	 */
	public final static String SESSION_MEDIA_CHANGED = MODIFICATION_EVENT + "SESSION_MEDIA_CHANGED_";

	/**
	 * Session language change
	 * data: 
	 */
	public final static String SESSION_LANG_CHANGED = MODIFICATION_EVENT + "SESSION_LANG_CHANGED_";

	/**
	 * Tier view has changed
	 * data: null
	 */
	public final static String TIER_VIEW_CHANGED_EVT = MODIFICATION_EVENT + "TIER_VIEW_CHANGE_";

	/**
	 * Tier lock has changed
	 * data: tier name - String
	 */
	public final static String TIER_LOCK_CHANGED_EVT = MODIFICATION_EVENT + "TIER_LOCK_CHANGE_";

	/**
	 * A new background task was queued
	 * data: task - PhonTask
	 */
	public final static String TASK_QUEUED = "_TASK_QUEUED_";

	/**
	 * A background task has started to run
	 * data: task - PhonTask
	 */
	public final static String TASK_RUNNING = "_TASK_RUNNING_";

	/**
	 * A background task has finished
	 * data: task - PhonTask
	 */
	public final static String TASK_FINISHED = "_TASK_FINISHED_";

	/**
	 * Task progress
	 *
	 */
	public final static String TASK_PROGRESS = "_TASK_PROGRESS_";

	/**
	 * An error occurred during a task
	 * data: task - PhonTask
	 */
	public final static String TASK_ERROR = "_TASK_ERROR_";
}
