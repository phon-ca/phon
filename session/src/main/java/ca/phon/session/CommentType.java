package ca.phon.session;

/**
 * The allowable types of comment headers in a transcript.
 *
 */
public enum CommentType {
    Activities("Activities"),
    Bck("Bck"),
    Date("Date"),
    Number("Number"),
    RecordingQuality("Recording Quality"),
    Transcription("Transcription"),
    Types("Types"),
    Blank("Blank"),
    Bg("Bg"), // begin-gem
    Eg("Eg"), // end-gem
    G("G"), // lazy-gem
    T("T"),
    Generic("Generic"),
    Location("Location"),
    NewEpisode("New Episode"),
    RoomLayout("Room Layout"),
    Situation("Situation"),
    TapeLocation("Tape Location"),
    TimeDuration("Time Duration"),
    TimeStart("Time Start"),
    Transcriber("Transcriber"),
    Warning("Warning"),
    Page("Page");

    final String label;

    private CommentType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public String toString() {
        return this.label;
    }

    public static CommentType fromString(String txt) {
        for(CommentType ct:values()) {
            if(ct.name().equals(txt) || ct.getLabel().equals(txt)) {
                return ct;
            }
        }
        return null;
    }

}
