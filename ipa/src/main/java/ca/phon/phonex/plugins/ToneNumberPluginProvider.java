package ca.phon.phonex.plugins;


import ca.phon.ipa.ToneNumber;
import ca.phon.phonex.PhoneMatcher;
import ca.phon.phonex.PhonexPlugin;
import ca.phon.phonex.PluginProvider;

import java.util.ArrayList;
import java.util.List;

/**
 * Plugin provider for tone number matcher.
 *
 * Usage: tn("&lt;list of tone numbers&gt;")
 * E.g., Look for a phone that has tone number 214
 *
 * \c:tn("214")
 *
 * or look for a phone that has tone number 214 or 51
 * \c:tn("214,51")
 *
 * or look for a phone that does not have tone number 214
 * \c:tn("-214")
 */
@PhonexPlugin(name = "tn", description="Match tone number", arguments={"toneNumberList"} )
public class ToneNumberPluginProvider implements PluginProvider {

    @Override
    public PhoneMatcher createMatcher(List<String> args) throws IllegalArgumentException {
        if(args == null)
            throw new NullPointerException();
        if(args.size() != 1) {
            throw new IllegalArgumentException();
        }

        final String arg = args.get(0);
        final String[] toneStrs = arg.split(",");

        final List<ToneNumber> toneNumList = new ArrayList<>();
        final List<ToneNumber> notToneNumbers = new ArrayList<>();

        for(int i = 0; i < toneStrs.length; i++) {
            String toneStr = toneStrs[i].trim();
            boolean isNot = false;
            if(toneStr.startsWith("-")) {
                isNot = true;
                toneStr = toneStr.substring(1).trim();
            }
            try {
                final ToneNumber tn = ToneNumber.fromString(toneStr);
                if(isNot) {
                    notToneNumbers.add(tn);
                } else {
                    toneNumList.add(tn);
                }
            } catch(NumberFormatException e) {
                // ignore
            }
        }

        return new ToneNumberPluginMatcher(toneNumList.toArray(new ToneNumber[toneNumList.size()]),
                notToneNumbers.toArray(new ToneNumber[notToneNumbers.size()]));
    }

}
