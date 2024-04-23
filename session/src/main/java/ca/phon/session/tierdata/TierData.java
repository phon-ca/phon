package ca.phon.session.tierdata;

import ca.phon.extensions.ExtendableObject;
import ca.phon.visitor.Visitable;
import ca.phon.visitor.Visitor;
import org.antlr.v4.runtime.*;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public final class TierData extends ExtendableObject implements Iterable<TierElement>, Visitable<TierElement> {

    private final List<TierElement> elements;

    public static TierData parseTierData(String text) throws ParseException {
        CharStream charStream = CharStreams.fromString(text.trim());
        TierDataLexer lexer = new TierDataLexer(charStream);
        TierDataErrorListener errorListener = new TierDataErrorListener();
        lexer.addErrorListener(errorListener);
        TokenStream tokenStream = new CommonTokenStream(lexer);

        TierDataParserListener userTierBuilder = new TierDataParserListener();
        TierDataParser parser = new TierDataParser(tokenStream);
        parser.setErrorHandler(new TierDataParserErrorStrategy());
        parser.addParseListener(userTierBuilder);

        try {
            parser.usertier();

            if(!errorListener.getParseExceptions().isEmpty()) {
                throw errorListener.getParseExceptions().get(0);
            }
        } catch (RecognitionException e) {
            throw new ParseException(text, e.getOffendingToken().getCharPositionInLine());
        } catch (TierDataParserException pe) {
            final ParseException parseException = new ParseException(pe.getLocalizedMessage(), pe.getPositionInLine());
            parseException.addSuppressed(pe);
            throw parseException;
        }
        return userTierBuilder.toTierData();
    }

    public TierData(TierElement... elements) {
        this(Arrays.asList(elements));
    }

    public TierData(List<TierElement> elements) {
        super();
        this.elements = Collections.unmodifiableList(elements);
    }

    public List<TierElement> getElements() {
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

    public TierElement elementAt(int index) {
        return elements.get(index);
    }

    /**
     * Return string index of given element
     *
     * @param element
     *
     * @return string index or -1 if not found
     */
    public int stringIndexOf(TierElement element) {
        int idx = 0;
        for(var ele:getElements()) {
            if(idx > 0) ++idx; // space between elements
            if(ele == element)
                return idx;
            idx += ele.toString().length();
        }
        return -1;
    }

    @Override
    public String toString() {
        return getElements().stream().map(ele -> ele.toString()).collect(Collectors.joining(" "));
    }

    @Override
    public void accept(Visitor<TierElement> visitor) {
        for(TierElement ele:this) visitor.visit(ele);
    }

    @Override
    public Iterator<TierElement> iterator() {
        return new UserTierElementIterator();
    }

    private class UserTierElementIterator implements Iterator<TierElement> {

        private int currentElement = 0;

        @Override
        public boolean hasNext() {
            return currentElement < size();
        }

        @Override
        public TierElement next() {
            return elementAt(currentElement++);
        }

    }

    public Stream<TierElement> stream() {
        return stream(false);
    }

    public Stream<TierElement> stream(boolean parallel) {
        return StreamSupport.stream(spliterator(), parallel);
    }

}
