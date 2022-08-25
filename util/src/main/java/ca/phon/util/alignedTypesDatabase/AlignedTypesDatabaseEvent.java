/*
 * Copyright (C) 2005-2022 Gregory Hedlund & Yvan Rose
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

package ca.phon.util.alignedTypesDatabase;

public class AlignedTypesDatabaseEvent {

	public static enum EventType {
		TierAdded,
		TierRemoved,
		TypeInserted,
		AlignmentAdded,
		AlignmentIncremented,
		AlignmentDecremented,
		AlignmentRemoved
	};

	private final EventType eventType;

	private final Object eventData;

	public AlignedTypesDatabaseEvent(EventType eventType, Object eventData) {
		super();

		this.eventType = eventType;
		this.eventData = eventData;
	}

	public EventType getEventType() {
		return eventType;
	}

	public Object getEventData() {
		return eventData;
	}
}
