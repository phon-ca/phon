package ca.phon.formatter;

import java.text.ParseException;

/**
 * Media time formatter for numbers
 */
@FormatterType(Number.class)
public class MediaTimeFormatter implements Formatter<Number> {

    /**
     * Return times in minutes and seconds (short form: e.g., 1:3.5)
     *
     * @param number if number is a float time is interpreted as a values in seconds, milliseconds otherwise
     * @return formatted time value
     */
    public static String timeToMinutesAndSeconds(Number number) {
        return timeToString(number, MediaTimeFormatStyle.MINUTES_AND_SECONDS);
    }

    public static String timeToPaddedMinutesAndSeconds(Number number) {
        return timeToString(number, MediaTimeFormatStyle.PADDED_MINUTES_AND_SECONDS);
    }

    public static String timeToMilliseconds(Number number) {
        return timeToString(number, MediaTimeFormatStyle.MILLISECONDS);
    }

    public static String timeToString(Number number, MediaTimeFormatStyle formatStyle) {
        return (new MediaTimeFormatter(formatStyle)).format(number);
    }

    public static float parseTimeToSeconds(String text) throws ParseException {
        return parseTimeToMilliseconds(text) / 1000.0f;
    }

    public static long parseTimeToMilliseconds(String text) throws ParseException {
        long timeInMs = (new MediaTimeFormatter()).parse(text).longValue();
        return timeInMs;
    }

    private final MediaTimeFormatStyle formatStyle;

    public MediaTimeFormatter() {
        this(MediaTimeFormatStyle.MINUTES_AND_SECONDS);
    }

    public MediaTimeFormatter(MediaTimeFormatStyle formatStyle) {
        this.formatStyle = formatStyle;
    }

    @Override
    public String format(Number obj) {
        MediaTimeFormat format = new MediaTimeFormat(formatStyle);
        return format.format(obj);
    }

    @Override
    public Number parse(String text) throws ParseException {
        MediaTimeFormat format = new MediaTimeFormat();
        try {
            return (Long) format.parseObject(text);
        } catch (IllegalArgumentException e) {
            throw new ParseException(e.getLocalizedMessage(), 0);
        }
    }

}
