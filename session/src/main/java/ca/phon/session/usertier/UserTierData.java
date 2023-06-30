package ca.phon.session.usertier;

import ca.phon.extensions.ExtendableObject;
import org.antlr.v4.runtime.*;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public final class UserTierData extends ExtendableObject {

    private final List<UserTierElement> elements;

    public static UserTierData parseTierData(String text) throws ParseException {
        CharStream charStream = CharStreams.fromString(text);
        UserTierLexer lexer = new UserTierLexer(charStream);
        TokenStream tokenStream = new CommonTokenStream(lexer);

        UserTierParserListener userTierBuilder = new UserTierParserListener();
        UserTierParser parser = new UserTierParser(tokenStream);
        parser.addParseListener(userTierBuilder);

        try {
            parser.usertier();
        } catch (RecognitionException e) {
            throw new ParseException(text, e.getOffendingToken().getCharPositionInLine());
        }
        return userTierBuilder.toTierData();
    }

    public UserTierData(UserTierElement ... elements) {
        this(Arrays.asList(elements));
    }

    public UserTierData(List<UserTierElement> elements) {
        super();
        this.elements = Collections.unmodifiableList(elements);
    }

    public List<UserTierElement> getElements() {
        return elements;
    }

    public int size() {
        return elements.size();
    }

    public int length() { return elements.size(); }

    public UserTierElement elementAt(int index) {
        return elements.get(index);
    }

    @Override
    public String toString() {
        return getElements().stream().map(ele -> ele.toString()).collect(Collectors.joining(" "));
    }

}
