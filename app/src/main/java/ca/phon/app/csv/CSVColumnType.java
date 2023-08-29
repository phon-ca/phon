package ca.phon.app.csv;

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
