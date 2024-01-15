package ca.phon.app.session.editor.view.transcriptEditor;

import javax.swing.text.*;
import java.util.ArrayList;
import java.util.List;

public class TranscriptBatchBuilder {

    /**
     * A character array containing just the newline character
     */
    private static final char[] EOL_ARRAY = {'\n'};
    private final List<DefaultStyledDocument.ElementSpec> batch = new ArrayList<>();
    private final List<InsertionHook> insertionHooks;

    public TranscriptBatchBuilder() {
        this(new ArrayList<>());
    }

    public TranscriptBatchBuilder(List<InsertionHook> insertionHooks) {
        this.insertionHooks = insertionHooks;
    }

    public List<DefaultStyledDocument.ElementSpec> getBatch() {
        return batch;
    }

    public void append(DefaultStyledDocument.ElementSpec elementSpec) {
        batch.add(elementSpec);
    }

    public void appendAll(List<DefaultStyledDocument.ElementSpec> elementSpecList) {
        batch.addAll(elementSpecList);
    }

    public void clear() {
        batch.clear();
    }

    /**
     * Gets a list of {@link javax.swing.text.DefaultStyledDocument.ElementSpec} containing
     * the {@code ElementSpec.EndTagType} and {@code ElementSpec.StartTagType} tags
     *
     * @return a list containing the end and start tags
     */
    static public List<DefaultStyledDocument.ElementSpec> getBatchEndStart() {
        return getBatchEndStart(null, null);
    }

    /**
     * Gets a list of {@link javax.swing.text.DefaultStyledDocument.ElementSpec} containing
     * the {@code ElementSpec.EndTagType} and {@code ElementSpec.StartTagType} tags
     *
     * @param endAttrs attributes for end tag (may be null)
     * @param startAttrs attributes for start tag (may be null)
     * @return a list containing the end and start tags
     */
    static public List<DefaultStyledDocument.ElementSpec> getBatchEndStart(AttributeSet endAttrs, AttributeSet startAttrs) {
        List<DefaultStyledDocument.ElementSpec> retVal = new ArrayList<>();
        retVal.add(new DefaultStyledDocument.ElementSpec(endAttrs != null ? endAttrs.copyAttributes() : null, DefaultStyledDocument.ElementSpec.EndTagType));
        retVal.add(new DefaultStyledDocument.ElementSpec(startAttrs != null ? startAttrs.copyAttributes() : null, DefaultStyledDocument.ElementSpec.StartTagType));
        return retVal;
    }

    /**
     * Appends a {@link javax.swing.text.DefaultStyledDocument.ElementSpec} containing the specified string and
     * attributes to the end of the batch
     */
    public void appendBatchString(String str, MutableAttributeSet a) {
        final StringBuilder builder = new StringBuilder();
        builder.append(str);
        final List<DefaultStyledDocument.ElementSpec> additionalInsertions = new ArrayList<>();
        for (InsertionHook hook : insertionHooks) {
            additionalInsertions.addAll(hook.batchInsertString(builder, a));
        }
        batch.add(getBatchString(builder.toString(), a));
        batch.addAll(additionalInsertions);
    }

    /**
     * Appends the end and start tags to the end of the batch
     */
    public void appendBatchEndStart() {
        appendBatchEndStart(null, null);
    }

    /**
     * Appends the end and start tags to the end of the batch
     *
     * @param endAttrs attributes for end tag (may be null)
     * @param startAttrs attributes for starting paragraph (may be null)
     * @poaram batch the batch to append the tags to
     */
    public void appendBatchEndStart(AttributeSet endAttrs, AttributeSet startAttrs) {
        batch.addAll(getBatchEndStart(endAttrs, startAttrs));
    }

    /**
     * Appends a newline character with the given attributes and the start and end tags to the end of the batch
     */
    public void appendBatchLineFeed(AttributeSet endAttrs, AttributeSet startAttrs) {
        batch.addAll(getBatchEndLineFeed(endAttrs, startAttrs));
    }

    /**
     * Gets a list of {@link javax.swing.text.DefaultStyledDocument.ElementSpec} containing a newline character
     * with the specified attributes and the {@code ElementSpec.EndTagType} and {@code ElementSpec.StartTagType} tags
     *
     * @param endAttrs attributes for end tag (may be null)
     * @param startAttrs attributes for start tag (may be null)
     * @return a list with the newline character and the end and start tags
     */
    static public List<DefaultStyledDocument.ElementSpec> getBatchEndLineFeed(AttributeSet endAttrs, AttributeSet startAttrs) {
        List<DefaultStyledDocument.ElementSpec> retVal = new ArrayList<>();
        retVal.add(new DefaultStyledDocument.ElementSpec(endAttrs != null ? endAttrs.copyAttributes() : null, DefaultStyledDocument.ElementSpec.ContentType, EOL_ARRAY, 0, 1));
        retVal.addAll(getBatchEndStart(endAttrs, startAttrs));
        return retVal;
    }

    /**
     * Gets a {@link javax.swing.text.DefaultStyledDocument.ElementSpec} containing the specified string and attributes
     *
     * @return a {@link javax.swing.text.DefaultStyledDocument.ElementSpec} containing the specified
     * string and attributes
     */
    static public DefaultStyledDocument.ElementSpec getBatchString(String str, MutableAttributeSet a) {
        char[] chars = str.toCharArray();
        return new DefaultStyledDocument.ElementSpec(new SimpleAttributeSet(a), DefaultStyledDocument.ElementSpec.ContentType, chars, 0, str.length());
    }

    public boolean isEmpty() {
        return batch.isEmpty();
    }

}
