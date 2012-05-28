package ca.phon.ipa.featureset;

/**
 * Feature family
 */
public enum FeatureFamily {
	PLACE("place"),
	MANNER("manner"),
	HEIGHT("height"),
	TONGUE_ROOT("tongue root"),
	BACKNESS("backness"),
	DIACRITIC("diacritic"),
	LABIAL("labial"),
	DORSAL("dorsal"),
	CORONAL("coronal"),
	VOICING("voicing"),
	CONTINUANCY("continuancy"),
	NASALITY("nasality"),
	STRIDENCY("stridency"),
	GUTTURAL("guttural"),
	UNDEFINED("undefined");
	
	private final String value;
	
	FeatureFamily(String v) {
	    value = v;
	}
	
	public String value() {
	    return value;
	}
	
	public static FeatureFamily fromValue(String v) {
	    for (FeatureFamily c: FeatureFamily.values()) {
	        if (c.value.equals(v)) {
	            return c;
	        }
	    }
	    throw new IllegalArgumentException(v);
	}
}
