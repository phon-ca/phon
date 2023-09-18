package ca.phon.orthography;

public enum TerminatorType {
    @CHATReference("https://talkbank.org/manuals/CHAT.html#Period_Terminator")
    PERIOD(".", "period"),
    @CHATReference("https://talkbank.org/manuals/CHAT.html#QuestionMark_Terminator")
    QUESTION("?", "question"),
    @CHATReference("https://talkbank.org/manuals/CHAT.html#ExclamationMark_Terminator")
    EXCLAMATION("!", "exclamation"),
    @CHATReference("https://talkbank.org/manuals/CHAT.html#TranscriptionBreak_Terminator")
    BROKEN_FOR_CODING("+.", "broken for coding"),
    @CHATReference("https://talkbank.org/manuals/CHAT.html#TrailingOff_Terminator")
    TRAIL_OFF("+...", "trail off"),
    @CHATReference("https://talkbank.org/manuals/CHAT.html#TrailingOffQuestion_Terminator")
    TRAIL_OFF_QUESTION("+..?", "trail off question"),
    @CHATReference("https://talkbank.org/manuals/CHAT.html#QuestionExclamation_Terminator")
    QUESTION_EXCLAMATION("+!?", "question exclamation"),
    @CHATReference("https://talkbank.org/manuals/CHAT.html#Interruption_Terminator")
    INTERRUPTION("+/.", "interruption"),
    @CHATReference("https://talkbank.org/manuals/CHAT.html#QuestionInterruption_Terminator")
    INTERRUPTION_QUESTION("+/?", "interruption question"),
    @CHATReference("https://talkbank.org/manuals/CHAT.html#SelfInterruption_Terminator")
    SELF_INTERRUPTION("+//.", "self interruption"),
    @CHATReference("https://talkbank.org/manuals/CHAT.html#SelfInterruptedQuestion_Terminator")
    SELF_INTERRUPTION_QUESTION("+//?", "self interruption question"),
    @CHATReference("https://talkbank.org/manuals/CHAT.html#QuotationFollows_Terminator")
    QUOTATION_NEXT_LINE("+\"/.", "quotation next line"),
    @CHATReference("https://talkbank.org/manuals/CHAT.html#QuotationPrecedes_Terminator")
    QUOTATION_PRECEDES("+\".", "quotation precedes"),
    @CHATReference("https://talkbank.org/manuals/CHAT.html#TechnicalBreakTCUContinuation_Terminator")
    TECHNICAL_BREAK_TCU_CONTINUATION("≋", "technical break TCU continuation"),
    @CHATReference("https://talkbank.org/manuals/CHAT.html#NoBreakTCUContinuation_Terminator")
    NO_BREAK_TCU_CONTINUATION("≈", "no break TCU continuation")
    ;

    private String text;

    private String displayName;

    private TerminatorType(String text, String displayName) {
        this.text = text;
        this.displayName = displayName;
    }

    public String getText() {
        return text;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return this.text;
    }

    public static TerminatorType fromString(String text) {
        for(TerminatorType tt:TerminatorType.values()) {
            if(tt.getText().equals(text) || tt.getDisplayName().equals(text)) {
                return tt;
            }
        }
        return null;
    }

}
