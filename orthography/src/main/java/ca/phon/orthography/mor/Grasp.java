package ca.phon.orthography.mor;

import ca.phon.util.Documentation;

import java.text.ParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * GRASP (Grammatical relations) data for a single word in %gra or %grt
 */
@Documentation({"https://talkbank.org/manuals/CHAT.html#GrammaticalRelations_Tier",
        "https://talkbank.org/manuals/CHAT.html#GrammaticalRelationsTraining_Tier"})
public final class Grasp {

    private final static String GRASP_REGEX = "(\\d+)\\|(\\d+)\\|(\\w+)";
    private final static int INDEX = 1;
    private final static int HEAD = 2;
    private final static int RELATION = 3;

    public static Grasp fromString(String text) throws ParseException {
        final Pattern pattern = Pattern.compile(GRASP_REGEX);
        final Matcher matcher = pattern.matcher(text);
        if(matcher.matches()) {
            final int index = Integer.parseInt(matcher.group(INDEX));
            final int head = Integer.parseInt(matcher.group(HEAD));
            final String relation = matcher.group(RELATION);
            return new Grasp(index, head, relation);
        } else {
            // TODO better error handling
            throw new ParseException("Invalid GRASP string " + text, 0);
        }
    }

    private final int index;

    private final int head;

    private final String relation;

    public Grasp(int index, int head, String relation) {
        super();
        this.index = index;
        this.head = head;
        this.relation = relation;
    }

    public int getIndex() {
        return index;
    }

    public int getHead() {
        return head;
    }

    public String getRelation() {
        return relation;
    }

    @Override
    public String toString() {
        return String.format("%d|%d|%s", getIndex(), getHead(), getRelation());
    }

}
