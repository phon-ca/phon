package ca.phon.formatter;

import org.jetbrains.annotations.NotNull;

import java.text.FieldPosition;
import java.text.Format;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Text format for media time values, three formats are available:
 * <ol>
 *     <li>time in minutes and seconds: <pre>1:3.5</pre></li>
 *     <li>time in minutes and seconds with padded zeros: <pre>001:03.500</pre></li>
 *     <li>time in milliseconds: <pre>63500</pre></li>
 * </ol>
 *
 * If format is provided with a float value it is interpreted as a number of seconds,
 * an integer is interpreted as a number of milliseconds.
 *
 * parseObject will always return a time in milliseconds
 *
 */
public class MediaTimeFormat extends Format {

    private final MediaTimeFormatStyle formatStyle;

    public MediaTimeFormat() {
        this(MediaTimeFormatStyle.MINUTES_AND_SECONDS);
    }

    public MediaTimeFormat(MediaTimeFormatStyle formatStyle) {
        super();
        this.formatStyle = formatStyle;
    }

    @Override
    public StringBuffer format(Object obj, @NotNull StringBuffer toAppendTo, @NotNull FieldPosition pos) {
        if(!(obj instanceof Long) && !(obj instanceof Integer) && !(obj instanceof Float))
            throw new IllegalArgumentException("Invalid type for media time format " + obj.getClass().getName());
        Long value = 0L;
        if(obj instanceof Integer) {
            value = ((Integer) obj).longValue();
        } else if(obj instanceof Long) {
            value = (Long) obj;
        } else {
            value = Float.valueOf((Float)obj * 1000.0f).longValue();
        }

        StringBuffer retVal = new StringBuffer();
        switch (formatStyle) {
            case PADDED_MINUTES_AND_SECONDS -> retVal.append(msToDisplayString(value, true));
            case MINUTES_AND_SECONDS -> retVal.append(msToDisplayString(value, false));
            case MILLISECONDS -> retVal.append(value);
        }

        return retVal;
    }

    protected String msToDisplayString(long ms, boolean padded)
            throws IllegalArgumentException {
        boolean negative = ms < 0;
        if(negative) {
            ms *= -1;
        }

        long numSeconds = ms / 1000;
        long numMSecondsLeft = ms % 1000;

        long numMinutes = numSeconds / 60;
        long numSecondsLeft = numSeconds % 60;

        NumberFormat nf = NumberFormat.getIntegerInstance();
        if(padded)
            nf.setMinimumIntegerDigits(2);

        NumberFormat msNf = NumberFormat.getIntegerInstance();
        if(padded)
            msNf.setMinimumIntegerDigits(3);

        String minuteString = msNf.format(numMinutes) + ":";

        String secondString =
                (numMinutes == 0
                        ? (nf.format(numSeconds) + ".")
                        : (nf.format(numSecondsLeft) + ".")
                );

        String msString =
                (msNf.format(numMSecondsLeft));

        String timeString =
                (negative ? "-" : "") +
                (padded || numMinutes > 0 ? minuteString : "") + secondString + msString;

        return timeString;
    }

    @Override
    public Object parseObject(String source, @NotNull ParsePosition pos) {
        boolean negative = source.startsWith("-");
        if(negative)
            source = source.substring(1);
        long value = 0L;
        if(source.matches("[0-9]+")) {
            value = Long.parseLong(source);
            pos.setIndex(source.length());
        } else {
            final Pattern pattern = Pattern.compile(MediaTimeFormatStyle.MINUTES_AND_SECONDS.getRegex());
            final Matcher matcher = pattern.matcher(source);
            if (matcher.matches()) {
                final String minString = matcher.group(1);
                int mins = 0;
                if(minString != null)
                    mins = Integer.parseInt(minString);

                final String secString = matcher.group(2);
                final int secs = Integer.parseInt(secString);

                final String msString = matcher.group(3);
                final int ms = Integer.parseInt(msString);

                value = Long.valueOf(
                        ms + (secs * 1000) + (mins * 60 * 1000));
                pos.setIndex(source.length());
            }
        }
        return (negative ? -1 : 1) * value;
    }

}
