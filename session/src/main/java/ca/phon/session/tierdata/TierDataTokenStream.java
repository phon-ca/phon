package ca.phon.session.tierdata;

import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.TokenSource;

public class TierDataTokenStream extends CommonTokenStream {

    public TierDataTokenStream(TokenSource tokenSource) {
        super(tokenSource);
    }

    public TierDataTokenStream(TokenSource tokenSource, int channel) {
        super(tokenSource, channel);
    }

}
