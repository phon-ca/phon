/*
 * Copyright (C) 2005-2020 Gregory Hedlund & Yvan Rose
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at

 *    http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ca.phon.app.session.editor;

import ca.phon.session.TierListener;

/**
 * Common event names for the editor event model
 * 
 */
public enum EditorEventName {

	/**
	 * Modification event.  Any event which starts with
	 * '_x_' will set the modified flag.  This is a generic
	 * event that should only be used when all other events
	 * do not apply or when a new event is needed by a module.
	 *
	 */
	MODIFICATION_EVENT("_x_"),
	EDITOR_SAVED_SESSION("_EDITOR_SAVED_SESSION_"),
	EDITOR_RELOAD_FROM_DISK("_EDITOR_RELOAD_FROM_DISK_"),
	EDITOR_CLOSING("_EDITOR_CLOSING_"),
	EDITOR_FINISHED_LOADING("_EDITOR_FINSIHED_LOADING_"),
	RECORD_ADDED_EVT(MODIFICATION_EVENT + "RECORD_ADDED_"),
	RECORD_DELETED_EVT(MODIFICATION_EVENT + "RECORD_DELETED_"),
	RECORD_MOVED_EVT(MODIFICATION_EVENT + "RECORD_MOVED_"),
	RECORD_REFRESH_EVT("_RECORD_REFRESH_"),
	SESSION_CHANGED_EVT("_SESSION_CHANGED_"),
	RECORD_CHANGED_EVT("_RECORD_CHANGED_"),
//	RECORD_SEGMENT_CHANGED_EVT("_RECORD_SEGMENT_CHANGED_"),
	SESSION_LOCATION_CHANGED_EVT("_SESSION_LOCATION_CHANGED_EVT_"),
	SPEAKER_CHANGE_EVT(MODIFICATION_EVENT + "SPEAKER_CHANGE_"),
	TIER_CHANGE_EVT(MODIFICATION_EVENT + "TIER_CHANGE_"),
	TIER_CHANGED_EVT(MODIFICATION_EVENT + "TIER_CHANGED_"),
	RECORD_EXCLUDE_CHANGE_EVT("_record_exclude_change_"),
	GROUP_LIST_CHANGE_EVT(MODIFICATION_EVENT + "GROUP_LIST_CHANGE_EVT_"),
	/**
	 * Change to current record's segment
	 * data: null
	 */
	SEGMENT_CHANGED_EVT(MODIFICATION_EVENT + "SEGMENT_CHANGED_EVT_"),
	SEGMENT_PLAYBACK_EVENT("_segment_playback_event_"),
	MODIFIED_FLAG_CHANGED("_MODIFIED_FLAG_CHANGED_"),
	/**
	 * Search completed event
	 * data:
	 */
	SEARCH_FINISHED_EVT("_SEARCH_FINISHED_"),
	/**
	 * Event fired when a result is hit
	 * data: search result - 
	 */
	FIND_REPLACE_RESULT_HIT("_find_replace_result_hit_"),
	PARTICIPANT_CHANGED(MODIFICATION_EVENT + "PARTICIPANT_CHANGED_"),
	PARTICIPANT_ADDED(MODIFICATION_EVENT + "PARTICIPANT_ADDED_"),
	PARTICIPANT_REMOVED(MODIFICATION_EVENT + "PARTICIPANT_REMOVED_"),
	SESSION_DATE_CHANGED(MODIFICATION_EVENT + "SESSION_DATE_CHANGED_"),
	SESSION_MEDIA_CHANGED(MODIFICATION_EVENT + "SESSION_MEDIA_CHANGED_"),
	SESSION_LANG_CHANGED(MODIFICATION_EVENT + "SESSION_LANG_CHANGED_"),
	TIER_VIEW_CHANGED_EVT(MODIFICATION_EVENT + "TIER_VIEW_CHANGE_"),
	TIER_LOCK_CHANGED_EVT(MODIFICATION_EVENT + "TIER_LOCK_CHANGE_"),
	/**
	 * A new background task was queued
	 * data: task - PhonTask
	 */
	TASK_QUEUED("_TASK_QUEUED_"),
	/**
	 * A background task has started to run
	 * data: task - PhonTask
	 */
	TASK_RUNNING("_TASK_RUNNING_"),
	/**
	 * A background task has finished
	 * data: task - PhonTask
	 */
	TASK_FINISHED("_TASK_FINISHED_"),
	/**
	 * Task progress
	 *
	 */
	TASK_PROGRESS("_TASK_PROGRESS_"),
	/**
	 * An error occurred during a task
	 * data: task - PhonTask
	 */
	TASK_ERROR("_TASK_ERROR_");

	private String txt;

	private EditorEventName(String txt) {
		this.txt = txt;
	}

	public String getEventName() {
		return this.txt;
	}

	@Override
	public String toString() {
		return txt;
	}

}
