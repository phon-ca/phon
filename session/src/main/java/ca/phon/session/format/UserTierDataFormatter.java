package ca.phon.session.format;

import ca.phon.formatter.Formatter;
import ca.phon.formatter.FormatterType;
import ca.phon.session.UserTierData;

import java.text.ParseException;

@FormatterType(UserTierData.class)
public class UserTierDataFormatter implements Formatter<UserTierData> {

    @Override
    public String format(UserTierData obj) {
        return obj.toString();
    }

    @Override
    public UserTierData parse(String text) throws ParseException {
        return UserTierData.parseTierData(text);
    }

}
