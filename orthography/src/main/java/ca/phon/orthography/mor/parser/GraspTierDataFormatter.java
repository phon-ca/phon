package ca.phon.orthography.mor.parser;

import ca.phon.formatter.Formatter;
import ca.phon.formatter.FormatterType;
import ca.phon.orthography.mor.GraspTierData;
import ca.phon.plugin.IPluginExtensionFactory;
import ca.phon.plugin.IPluginExtensionPoint;

import java.text.ParseException;

@FormatterType(GraspTierData.class)
public class GraspTierDataFormatter implements Formatter<GraspTierData>, IPluginExtensionPoint<Formatter<GraspTierData>> {

    @Override
    public String format(GraspTierData obj) {
        return obj.toString();
    }

    @Override
    public GraspTierData parse(String text) throws ParseException {
        try {
            return GraspTierData.parseGraspTierData(text);
        } catch (MorParserException e) {
            throw new ParseException(e.getLocalizedMessage(), e.getPositionInLine());
        }
    }

    @Override
    public Class<?> getExtensionType() {
        return Formatter.class;
    }

    @Override
    public IPluginExtensionFactory<Formatter<GraspTierData>> getFactory() {
        return args -> this;
    }

}
