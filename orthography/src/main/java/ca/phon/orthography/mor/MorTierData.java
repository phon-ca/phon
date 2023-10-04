package ca.phon.orthography.mor;

import ca.phon.extensions.ExtendableObject;
import ca.phon.mor.MorLexer;
import ca.phon.mor.MorParser;
import ca.phon.orthography.mor.parser.MorBuilder;
import ca.phon.orthography.mor.parser.MorParserException;
import org.antlr.v4.runtime.*;
import org.jetbrains.annotations.NotNull;

import java.text.ParseException;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Container for a list of Mor items used for tier data
 */
public final class MorTierData extends ExtendableObject implements Iterable<Mor> {

    /**
     * Parser mor tier data
     */
    public static MorTierData parseMorTierData(String text) throws MorParserException {
        CharStream charStream = CharStreams.fromString(text);
        MorLexer lexer = new MorLexer(charStream);
        TokenStream tokenStream = new CommonTokenStream(lexer);
        MorBuilder listener = new MorBuilder();
        MorParser parser = new MorParser(tokenStream);
        parser.addParseListener(listener);

        try {
            parser.start();
            return new MorTierData(listener.getMors());
        } catch(RecognitionException e) {
            throw new MorParserException(e.getLocalizedMessage(), e.getOffendingToken().getCharPositionInLine());
        }
    }

    private final List<Mor> mors;

    public MorTierData() {
        this(new ArrayList<>());
    }

    public MorTierData(List<Mor> mors) {
        super();
        this.mors = mors;
    }

    public List<Mor> getMors() {
        return Collections.unmodifiableList(mors);
    }

    public int size() {
        return mors.size();
    }

    public boolean isEmpty() {
        return mors.isEmpty();
    }

    public boolean contains(Mor mor) {
        return mors.contains(mor);
    }

    public Mor get(int index) {
        return mors.get(index);
    }

    public int indexOf(Mor mor) {
        return mors.indexOf(mor);
    }

    @Override
    public String toString() {
        return getMors().stream().map(Mor::text).collect(Collectors.joining(" "));
    }

    @NotNull
    @Override
    public Iterator<Mor> iterator() {
        return mors.iterator();
    }

    @Override
    public void forEach(Consumer<? super Mor> action) {
        Iterable.super.forEach(action);
    }

    @Override
    public Spliterator<Mor> spliterator() {
        return Iterable.super.spliterator();
    }

}
