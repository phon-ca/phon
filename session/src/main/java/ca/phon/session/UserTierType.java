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
    Addressee("addressee", "%add", TierData.class, false),
    Actions("actions", "%act", TierData.class,false),
    Alternative("alternative", "%alt", TierData.class,false),
    Coding("coding", "%cod", TierData.class,false),
    Cohesion("cohesion", "%coh", TierData.class,false),
    EnglishTranslation("english translation", "%eng", TierData.class,false),
    Errcoding("errcoding", "%err", TierData.class,false),
    Explanation("explanation", "%exp", TierData.class,false),
    Flow("flow", "%flo", TierData.class,false),
    TargetGloss("target gloss", "%gls", TierData.class,false),
    Gesture("gesture", "%gpx", TierData.class,false),
    Intonation("intonation", "%int", TierData.class,false),
    Orthography("orthography", "%ort", TierData.class,false),
    Paralinguistics("paralinguistics", "%par", TierData.class,false),
    SALT("SALT", "%def", TierData.class,false),
    Situation("situation", "%sit", TierData.class,false),
    SpeechAct("speech act", "%spa", TierData.class,false),
    TimeStamp("time stamp", "%tim", TierData.class,false),
    /**
     * word segment information, each word in orthography will be reproduced along with an
     * internal-media element, this tier is not directly editable.  Tier name comes
     * from CLAN
     */
    Wor("Word segments", "%wor", Orthography.class, true),
    /**
     * Morphological tiers from CHAT
     */
    Mor("Morphology", "%mor", MorTierData.class, false),
    Trn("Speech Turn", "%trn", MorTierData.class, false),
    /**;
     * GRASP tiers
     */
    Gra("GRASP", "%gra", GraspTierData.class, false),
    Grt("GRASP Turn", "%grt", GraspTierData.class, false);

    /**
     * tier name in Phon
     */
    private final String tierName;

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

    private UserTierType(String tierName, String chatTierName, Class<?> type, boolean alignable) {
        this.tierName = tierName;
        this.chatTierName = chatTierName;
        this.type = type;
        this.alignable = alignable;
    }

    public String getTierName() {
        return tierName;
    }

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
        for(UserTierType tierType:values()) {
            if(tierType.getTierName().equals(tierName)) {
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

}
