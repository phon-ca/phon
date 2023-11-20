package ca.phon.session;

import ca.phon.orthography.Orthography;
import ca.phon.orthography.mor.GraspTierData;
import ca.phon.orthography.mor.MorTierData;
import ca.phon.session.tierdata.TierData;

import java.util.HashMap;
import java.util.Map;

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

    public String getPhonTierName() {
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
            if(ignoreCase ? tierType.getPhonTierName().equalsIgnoreCase(tierName) : tierType.getPhonTierName().equals(tierName)) {
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

    /**
     * Abbreviate given tier name to a CHAT tier name.
     *
     * @apram session
     * @param tierName
     *
     * @return tier name for CLAN in the form of %xAAAAAAA
     */
    public static String determineCHATTierName(Session session, String tierName) {
        final Map<String, String> tierNameMap = new HashMap<>();
        for(TierDescription userTierDesc:session.getUserTiers()) {
            final UserTierType userTierType = UserTierType.fromPhonTierName(userTierDesc.getName());
            if(userTierType == null) {
                String abbreviatedTierName = abbreviateTierName(userTierDesc.getName());
                int i = 0;
                String chatTierName = abbreviatedTierName;
                while (tierNameMap.containsValue(chatTierName)) {
                    ++i;
                    if(abbreviatedTierName.length() < 7) {
                        chatTierName = abbreviatedTierName + i;
                    } else {
                        chatTierName = abbreviatedTierName.substring(0, 6) + i;
                    }
                }
                tierNameMap.put(userTierDesc.getName(), chatTierName);
            } else {
                if(userTierDesc.getName().equals(tierName)) return userTierType.getChatTierName();
            }
        }
        return "%x" + tierNameMap.get(tierName);
    }

    /**
     * CLAN requires user-defined tier names be no longer than 7 characters
     *
     * @return mapped tier name
     */
    private static String abbreviateTierName(String tierName) {
        final StringBuilder builder = new StringBuilder();
        for(int i = 0; i < tierName.length() && builder.length() < 7; i++) {
            final char c = tierName.charAt(i);
            if(!Character.isWhitespace(c)) {
                builder.append(c);
            }
        }
        return builder.toString();
    }
}
