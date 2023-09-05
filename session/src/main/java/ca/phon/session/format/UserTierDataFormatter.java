package ca.phon.session.format;

import ca.phon.formatter.Formatter;
import ca.phon.formatter.FormatterType;
import ca.phon.session.tierdata.TierData;

import java.text.ParseException;

@FormatterType(TierData.class)
public class UserTierDataFormatter implements Formatter<TierData> {

    @Override
    public String format(TierData obj) {
        return obj.toString();
    }

    @Override
    public TierData parse(String text) throws ParseException {
        return TierData.parseTierData(text);
    }

}
