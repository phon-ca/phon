package ca.phon.audio;

/**
 * Audio file formats
 *
 */
public enum AudioFileEncoding {
	LINEAR_8_SIGNED,
	LINEAR_8_UNSIGNED,
	LINEAR_16_BIG_ENDIAN,
	LINEAR_16_LITTLE_ENDIAN,
	LINEAR_24_BIG_ENDIAN,
	LINEAR_24_LITTLE_ENDIAN,
	LINEAR_32_BIG_ENDIAN,
	LINEAR_32_LITTLE_ENDIAN,
	MULAW,
	ALAW,
	SHORTEN,
	POLYPHONE,
	IEEE_FLOAT_32_BIG_ENDIAN,
	IEEE_FLOAT_32_LITTLE_ENDIAN,
	IEEE_FLOAT_64_BIG_ENDIAN,
	IEEE_FLOAT_64_LITTLE_ENDIAN;
	
	public boolean isSigned() {
		return (this == LINEAR_8_SIGNED) || ordinal() > LINEAR_8_UNSIGNED.ordinal();
	}
	
	public boolean isBigEndian() {
		return (this == LINEAR_24_BIG_ENDIAN) ||
				(this == LINEAR_24_BIG_ENDIAN) ||
				(this == LINEAR_32_BIG_ENDIAN) ||
				(this == IEEE_FLOAT_32_BIG_ENDIAN) ||
				(this == IEEE_FLOAT_64_BIG_ENDIAN);
	}
	
}
