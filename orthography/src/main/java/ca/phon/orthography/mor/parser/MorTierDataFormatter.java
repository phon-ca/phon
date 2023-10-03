package ca.phon.orthography.mor.parser;

import ca.phon.formatter.Formatter;
import ca.phon.formatter.FormatterType;
import ca.phon.orthography.mor.MorTierData;
import ca.phon.plugin.IPluginExtensionFactory;
import ca.phon.plugin.IPluginExtensionPoint;

import java.text.ParseException;

@FormatterType(MorTierData.class)
public class MorTierDataFormatter implements Formatter<MorTierData>, IPluginExtensionPoint<Formatter<MorTierData>> {

    @Override
    public String format(MorTierData obj) {
        return obj.toString();
    }

    @Override
    public MorTierData parse(String text) throws ParseException {
        try {
            return MorTierData.parseMorTierData(text);
        } catch (MorParserException e) {
            throw new ParseException(e.getLocalizedMessage(), e.getPositionInLine());
        }
    }

    @Override
    public Class<?> getExtensionType() {
        return Formatter.class;
    }

    @Override
    public IPluginExtensionFactory<Formatter<MorTierData>> getFactory() {
        return args -> this;
    }

}
