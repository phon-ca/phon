package ca.phon.formatter;

import java.text.ParseException;

/**
 * Media time formatter with three formats as defined by {@link MediaTimeFormatStyle}
 */
@FormatterType(Number.class)
public class MediaTimeFormatter implements Formatter<Number> {

    /**
     * Return time in minutes and seconds (short form: e.g., 1:3.5)
     *
     * @param number if number is a float time is interpreted as a values in seconds, milliseconds otherwise
     * @return formatted time value
     */
    public static String timeToMinutesAndSeconds(Number number) {
        return timeToString(number, MediaTimeFormatStyle.MINUTES_AND_SECONDS);
    }

    /**
     * Convert seconds to string in minutes and seconds (short form: e.g., 1:3.5)
     *
     * @param seconds time value in seconds
     * @return formatted time value
     */
    public static String secondsToMinutesAndSeconds(Number seconds) {
        return timeToString(seconds.floatValue(), MediaTimeFormatStyle.MINUTES_AND_SECONDS);
    }

    /**
     * Convert milliseconds to string in minutes and seconds (short form: e.g., 1:3.5)
     *
     * @param seconds time value in seconds
     * @return formatted time value
     */
    public static String msToMinutesAndSeconds(Number ms) {
        return timeToString(ms.longValue(), MediaTimeFormatStyle.MINUTES_AND_SECONDS);
    }

    /**
     * Return time in minutes and seconds (long form: e.g., 001:03.05)
     *
     * @param number if number is a float time is interpreted as a values in seconds, milliseconds otherwise
     * @return formatted time value
     */
    public static String timeToPaddedMinutesAndSeconds(Number number) {
        return timeToString(number, MediaTimeFormatStyle.PADDED_MINUTES_AND_SECONDS);
    }

    /**
     * Return seconds in minutes and seconds (long form: e.g., 001:03.05)
     *
     * @param seconds time in seconds
     * @return formatted time value
     */
    public static String secondsToPaddedMinutesAndSeconds(Number seconds) {
        return timeToString(seconds.floatValue(), MediaTimeFormatStyle.PADDED_MINUTES_AND_SECONDS);
    }

    /**
     * Return milliseconds in minutes and seconds (long form: e.g., 001:03.05)
     *
     * @param ms time in milliseconds
     * @return formatted time value
     */
    public static String msToPaddedMinutesAndSeconds(Number ms) {
        return timeToString(ms.longValue(), MediaTimeFormatStyle.PADDED_MINUTES_AND_SECONDS);
    }

    /**
     * Return time in milliseconds (integer)
     *
     * @param number if number is a float time is interpreted as a values in seconds, milliseconds otherwise
     * @return formatted time value
     */
    public static String timeToMilliseconds(Number number) {
        return timeToString(number, MediaTimeFormatStyle.MILLISECONDS);
    }

    /**
     * Return time in milliseconds (integer)
     *
     * @param seconds time in seconds
     * @return formatted time value
     */
    public static String secondsToMilliseconds(Number seconds) {
        return timeToString(seconds.floatValue(), MediaTimeFormatStyle.MILLISECONDS);
    }

    /**
     * Return time in milliseconds (integer)
     *
     * @param ms time in milliseconds
     * @return formatted time value
     */
    public static String msToMilliseconds(Number ms) {
        return timeToString(ms.longValue(), MediaTimeFormatStyle.MILLISECONDS);
    }

    /**
     * Return formmated string of given number
     *
     * @param number if number is a float time is interpreted as a values in seconds, milliseconds otherwise
     * @param formatStyle
     * @return formatted time string
     */
    public static String timeToString(Number number, MediaTimeFormatStyle formatStyle) {
        return (new MediaTimeFormatter(formatStyle)).format(number);
    }

    /**
     * Parse time to a number of seconds
     *
     * @param text
     * @return number of seconds
     * @throws ParseException
     */
    public static float parseTimeToSeconds(String text) throws ParseException {
        return parseTimeToMilliseconds(text) / 1000.0f;
    }

    /**
     * Parse time to a number of milliseconds
     *
     * @param text
     * @return number of milliseconds
     * @throws ParseException
     */
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
