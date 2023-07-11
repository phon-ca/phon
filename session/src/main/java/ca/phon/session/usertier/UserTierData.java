package ca.phon.session.usertier;

import ca.phon.extensions.ExtendableObject;
import ca.phon.visitor.Visitable;
import ca.phon.visitor.Visitor;
import org.antlr.v4.runtime.*;
import org.jetbrains.annotations.NotNull;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public final class UserTierData extends ExtendableObject implements Iterable<UserTierElement>, Visitable<UserTierElement> {

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

    /**
     * Return number of user tier elements
     *
     * @return
     */
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

    @Override
    public void accept(Visitor<UserTierElement> visitor) {
        for(UserTierElement ele:this) visitor.visit(ele);
    }

    @NotNull
    @Override
    public Iterator<UserTierElement> iterator() {
        return new UserTierElementIterator();
    }

    private class UserTierElementIterator implements Iterator<UserTierElement> {

        private int currentElement = 0;

        @Override
        public boolean hasNext() {
            return currentElement < size();
        }

        @Override
        public UserTierElement next() {
            return elementAt(currentElement++);
        }

    }

    public Stream<UserTierElement> stream() {
        return stream(false);
    }

    public Stream<UserTierElement> stream(boolean parallel) {
        return StreamSupport.stream(spliterator(), parallel);
    }

}
