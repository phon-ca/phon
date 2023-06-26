package ca.phon.session.usertier;

import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenSource;

public class UserTierTokenStream extends CommonTokenStream {

    public UserTierTokenStream(TokenSource tokenSource) {
        super(tokenSource);
    }

    public UserTierTokenStream(TokenSource tokenSource, int channel) {
        super(tokenSource, channel);
    }

}
