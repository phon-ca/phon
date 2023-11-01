package ca.phon.session;

import ca.phon.orthography.Orthography;
import ca.phon.orthography.mor.Grasp;
import ca.phon.orthography.mor.GraspTierData;
import ca.phon.orthography.mor.MorTierData;
import ca.phon.session.tierdata.TierData;

/**
 * An enumeration of suggested dependent tiers names and their
 * equivalents in CHAT
 */
public enum UserTierType {
    /*
     * Utterance-level dependent tiers
     */
    Addressee("Addressee", "addressee", "%add", TierData.class, false),
    Actions("Actions", "actions", "%act", TierData.class,false),
    Alternative("Alternative", "alternative", "%alt", TierData.class,false),
    Coding("Coding", "coding", "%cod", TierData.class,false),
    Cohesion("Cohesion", "cohesion", "%coh", TierData.class,false),
    Comments("Comments", "comments", "%com", TierData.class, false),
    EnglishTranslation("Eng translation", "english translation", "%eng", TierData.class,false),
    Errcoding("Errcoding", "errcoding", "%err", TierData.class,false),
    Explanation("Explanation", "explanation", "%exp", TierData.class,false),
    Facial("Facial", "facial", "%fac", TierData.class, false),
    Flow("Flow", "flow", "%flo", TierData.class,false),
    TargetGloss("Target gloss", "target gloss", "%gls", TierData.class,false),
    Gesture("Gesture", "gesture", "%gpx", TierData.class,false),
    Intonation("Intonation", "intonation", "%int", TierData.class,false),
    Orthography("Ort", "orthography", "%ort", TierData.class,false),
    Paralinguistics("Paralinguistics", "paralinguistics", "%par", TierData.class,false),
    SALT("SALT", "SALT", "%def", TierData.class,false),
    Situation("Situation", "situation", "%sit", TierData.class,false),
    SpeechAct("Speech act", "speech act", "%spa", TierData.class,false),
    TimeStamp("Time stamp", "time stamp", "%tim", TierData.class,false),
    /**
     * word segment information, each word in orthography will be reproduced along with an
     * internal-media element, this tier is not directly editable.
     */
    Wor("Word intervals", "", "%wor", Orthography.class, true),
    /**
     * Morphological tiers from CHAT
     */
    Mor("Morphology", "", "%mor", MorTierData.class, false),
    Trn("Speech Turn", "", "%trn", MorTierData.class, false),
    /**
     * GRASP tiers
     */
    Gra("GRASP", "", "%gra", GraspTierData.class, false),
    Grt("GRASP Turn", "", "%grt", GraspTierData.class, false);

    /**
     * tier name in Phon
     */
    private final String tierName;

    /**
     * talkbank xml tier type
     */
    private final String tbTierType;

    /**
     * Tier name in CHAT
     */
    private final String chatTierName;

    /**
     * Tier type
     */
    private final Class<?> type;

    /**
     * Included in x-tier alignement by default
     */
    private final boolean alignable;

    private UserTierType(String tierName, String tbTierType, String chatTierName, Class<?> type, boolean alignable) {
        this.tierName = tierName;
        this.tbTierType = tbTierType;
        this.chatTierName = chatTierName;
        this.type = type;
        this.alignable = alignable;
    }

    public String getTierName() {
        return tierName;
    }

    public String getTalkbankTierType() { return tbTierType; }

    public String getChatTierName() {
        return chatTierName;
    }

    public Class<?> getType() {
        return type;
    }

    public boolean isAlignable() {
        return alignable;
    }

    public static UserTierType fromPhonTierName(String tierName) {
        return fromPhonTierName(tierName, false);
    }

    public static UserTierType fromPhonTierName(String tierName, boolean ignoreCase) {
        for(UserTierType tierType:values()) {
            if(ignoreCase ? tierType.getTierName().equalsIgnoreCase(tierName) : tierType.getTierName().equals(tierName)) {
                return tierType;
            }
        }
        return null;
    }

    public static UserTierType fromChatTierName(String tierName) {
        for(UserTierType tierType:values()) {
            if(tierType.getChatTierName().equals(tierName)) {
                return tierType;
            }
        }
        return null;
    }

    public static UserTierType fromTalkbankTierType(String tbTierType) {
        for(UserTierType tierType:values()) {
            if(tierType.getTalkbankTierType().equals(tbTierType)) {
                return tierType;
            }
        }
        return null;
    }

}
