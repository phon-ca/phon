package ca.phon.formatter;

import java.text.ParseException;
import java.time.Period;

@FormatterType(Period.class)
public class PeriodFormatter implements Formatter<Period> {

    public static String periodToString(Period period, PeriodFormatStyle formatStyle) {
        return (new PeriodFormatter(formatStyle)).format(period);
    }

    public static Period stringToPeriod(String text) throws ParseException {
        return (new PeriodFormatter()).parse(text);
    }

    private final PeriodFormatStyle formatStyle;

    public PeriodFormatter() {
        this(PeriodFormatStyle.PHON);
    }

    public PeriodFormatter(PeriodFormatStyle formatStyle) {
        super();
        this.formatStyle = formatStyle;
    }

    @Override
    public String format(Period obj) {
        return (new PeriodFormat(formatStyle)).format(obj);
    }

    @Override
    public Period parse(String text) throws ParseException {
        try {
            return (Period) (new PeriodFormat(formatStyle)).parseObject(text);
        } catch (IllegalArgumentException e) {
            throw new ParseException(e.getLocalizedMessage(), 0);
        }
    }

    public static void main(String [] args) throws ParseException {
        PeriodFormatter formatter = new PeriodFormatter(PeriodFormatStyle.PHON);
        System.out.println(formatter.parse("02;02.15"));
    }

}
