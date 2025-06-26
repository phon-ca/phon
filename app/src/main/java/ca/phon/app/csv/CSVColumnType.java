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
package ca.phon.app.csv;

/**
 * Enumeration of supported CSV column types for import/export operations.
 * Each type represents a specific kind of data that can be mapped to CSV
 * columns.
 */
public enum CSVColumnType {
    CORPUS_NAME("Corpus Name"),
    SESSION_PATH("Session Path"),
    SESSION_NAME("Session Name"),
    SESSION_DATE("Session Date"),
    SESSION_MEDIA("Session Media"),
    PARTICIPANT_ID("Participant ID"),
    PARTICIPANT_NAME("Participant Name"),
    PARTICIPANT_ROLE("Participant Role"),
    PARTICIPANT_LANGUAGE("Participant Language"),
    PARTICIPANT_BIRTHDAY("Participant Birthday"),
    PARTICIPANT_AGE("Participant Age"),
    PARTICIPANT_EDUCATION("Participant Education"),
    PARTICIPANT_SEX("Participant Sex"),
    PARTICIPANT_SES("Participant SES"),
    RECORD_ID("Record ID"),
    RECORD_NUMBER("Record Number"),
    RECORD_LANGUAGE("Record Language"),
    ORTHOGRAPHY("Orthography"),
    IPA_TARGET("IPA Target"),
    IPA_ACTUAL("IPA Actual"),
    PHONE_ALIGNMENT("Phone Alignment"),
    SEGMENT("Segment"),
    SEGMENT_START("Segment Start"),
    SEGMENT_END("Segment End"),
    SEGMENT_DURATION("Segment Duration"),
    NOTES("Notes"),
    USER_TIER("User Tier");

    private final String readableName;

    CSVColumnType(String readableName) {
        this.readableName = readableName;
    }

    public String getReadableName() {
        return readableName;
    }
}
