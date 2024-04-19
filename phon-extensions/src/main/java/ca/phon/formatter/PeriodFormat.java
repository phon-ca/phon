package ca.phon.formatter;

import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.time.Period;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PeriodFormat extends Format {

    private final static String AGE_FORMATTER = "%02d;%02d.%02d";

    private final PeriodFormatStyle formatStyle;

    public PeriodFormat() {
        this(PeriodFormatStyle.PHON);
    }

    public PeriodFormat(PeriodFormatStyle formatStyle) {
        super();
        this.formatStyle = formatStyle;
    }

    @Override
    public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
        if(!(obj instanceof Period))
            throw new IllegalArgumentException("Must be a java.time.Period object");
        StringBuffer retVal = new StringBuffer();
        Period p = (Period)obj;
        switch (formatStyle) {
            case ISO -> retVal.append(p.toString());
            case PHON -> retVal.append(String.format(AGE_FORMATTER, p.getYears(), p.getMonths(), p.getDays()));
        }
        return retVal;
    }

    @Override
    public Object parseObject(String source, ParsePosition pos) {
        if(source.matches(PeriodFormatStyle.ISO.getRegex())) {
            pos.setIndex(source.length());
            return parsePeriod(source, PeriodFormatStyle.ISO.getRegex());
        } else if(source.matches(PeriodFormatStyle.PHON.getRegex())) {
            pos.setIndex(source.length());
            return parsePeriod(source, PeriodFormatStyle.PHON.getRegex());
        } else {
            throw new IllegalArgumentException("Invalid period text " + source);
        }
    }

    protected Period parsePeriod(String text, String regex) {
        final Pattern pattern = Pattern.compile(regex);
        final Matcher matcher = pattern.matcher(text);
        if(!matcher.matches()) throw new IllegalArgumentException("Invalid period text " + text);
        final int years = Integer.parseInt(matcher.group(1));
        final int months = Integer.parseInt(matcher.group(2));
        final int days = Integer.parseInt(matcher.group(3));
        return Period.of(years, months, days);
    }

}
