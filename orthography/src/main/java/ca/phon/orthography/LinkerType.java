package ca.phon.orthography;

import ca.phon.util.Documentation;

public enum LinkerType {
    @Documentation("https://talkbank.org/manuals/CHAT.html#QuotedUtterance_Linker")
    QUOTED_UTTERANCE_NEXT("+\"", "quoted utterance next"),
    @Documentation("https://talkbank.org/manuals/CHAT.html#QuickUptake_Linker")
    QUICK_UPTAKE("+^", "quick uptake"),
    @Documentation("https://talkbank.org/manuals/CHAT.html#LazyOverlap_Linker")
    LAZY_OVERLAP_MARK("+<", "lazy overlap mark"),
    @Documentation("https://talkbank.org/manuals/CHAT.html#SelfCompletion_Linker")
    SELF_COMPLETION("+,", "self completion"),
    @Documentation("https://talkbank.org/manuals/CHAT.html#OtherCompletion_Linker")
    OTHER_COMPLETION("++", "other completion"),
    @Documentation("https://talkbank.org/manuals/CHAT.html#TechnicalBreakTCUCompletion_Linker")
    TECHNICAL_BREAK_TCU_COMPLETION("+\u224b", "technical completion"),
    @Documentation("https://talkbank.org/manuals/CHAT.html#NoBreakTCUCompletion_Linker")
    NO_BREAK_TCU_COMPLETION("+\u2248", "no break completion");

    private String text;

    private String displayName;

    private LinkerType(String text, String displayName) {
        this.text = text;
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public String getText() {
        return this.text;
    }

    @Override
    public String toString() {
        return getText();
    }

    public static LinkerType fromString(String text) {
        for(LinkerType lt:values()) {
            if(lt.getText().equals(text) || lt.getDisplayName().equals(text)) {
                return lt;
            }
        }
        return null;
    }

}
