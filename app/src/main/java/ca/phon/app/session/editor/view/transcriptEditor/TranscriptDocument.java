package ca.phon.app.session.editor.view.transcriptEditor;

import ca.phon.app.log.LogUtil;
import ca.phon.app.session.editor.EditorEventManager;
import ca.phon.app.session.editor.undo.SessionEditUndoSupport;
import ca.phon.extensions.ExtensionSupport;
import ca.phon.extensions.IExtendable;
import ca.phon.formatter.MediaTimeFormatStyle;
import ca.phon.ipa.IPATranscript;
import ca.phon.orthography.InternalMedia;
import ca.phon.orthography.Orthography;
import ca.phon.orthography.mor.Grasp;
import ca.phon.orthography.mor.GraspTierData;
import ca.phon.orthography.mor.Mor;
import ca.phon.orthography.mor.MorTierData;
import ca.phon.plugin.PluginManager;
import ca.phon.session.*;
import ca.phon.session.Record;
import ca.phon.session.format.MediaSegmentFormatter;
import ca.phon.session.tierdata.*;
import ca.phon.ui.FontFormatter;
import ca.phon.ui.fonts.FontPreferences;
import ca.phon.util.*;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.*;
import java.util.List;

public class TranscriptDocument extends DefaultStyledDocument implements IExtendable  {
    /**
     * A reference to the loaded session
     * */
    private Session session;
    private final SessionFactory sessionFactory;
    /**
     * Whether the document is in single-record mode
     * */
    private boolean singleRecordView = false;
    /**
     * The index of the record that gets displayed if the document is in single-record mode
     * */
    private int singleRecordIndex = 0;
    /**
     * A character array containing just the newline character
     * */
    private static final char[] EOL_ARRAY = { '\n' };
    /**
     * The list of {@link javax.swing.text.DefaultStyledDocument.ElementSpec} that everything gets appended to
     * */
    private final ArrayList<ElementSpec> batch;
    /**
     * The number of monospace characters that make up the width of the label column
     * */
    public int labelColumnWidth = 20;
    /**
     * The spacing between the lines of the document
     * */
    private float lineSpacing = 0.2f;
    /**
     * Whether the next removal from the document will bypass the document filter
     * */
    private boolean bypassDocumentFilter = false;
    private SessionEditUndoSupport undoSupport;
    private EditorEventManager eventManager;
    private final PropertyChangeSupport propertyChangeSupport;
    /**
     * The set of "not editable" attributes
     * */
    private final Set<String> notEditableAttributes;
    /**
     * Whether the tier labels show the chat tier names or the regular tier names
     * */
    private boolean chatTierNamesShown = false;

    /**
     * extension support
     */
    private final ExtensionSupport extensionSupport = new ExtensionSupport(TranscriptDocument.class, this);

    /**
     * Constructor
     */
    public TranscriptDocument() {
        super(new TranscriptStyleContext());
        sessionFactory = SessionFactory.newFactory();
        setDocumentFilter(new TranscriptDocumentFilter(this));
        batch = new ArrayList<>();

        propertyChangeSupport = new PropertyChangeSupport(this);

        notEditableAttributes = new HashSet<>();
        notEditableAttributes.add(TranscriptStyleConstants.ATTR_KEY_NOT_EDITABLE);

        extensionSupport.initExtensions();
        loadRegisteredInsertionHooks();
    }

    // region Getters and Setters


    public Session getSession() {
        return session;
    }

    /**
     * Sets the session and reloads the content of the document accordingly
     *
     * @param session The session to be displayed
     */
    public void setSession(Session session) {
        this.session = session;
        try {
            if (getLength() > 0) {
                remove(0, getLength());
            }
            populate();
        }
        catch (BadLocationException e) {
            LogUtil.severe(e);
        }
    }

    public boolean getSingleRecordView() {
        return singleRecordView;
    }

    /**
     * Sets whether the document is in "single-record view" and reloads the document if it has changed
     *
     * @param singleRecordView whether the document will be in single record view
     * */
    public void setSingleRecordView(boolean singleRecordView) {
        if (this.singleRecordView == singleRecordView) return;
        this.singleRecordView = singleRecordView;
        reload();
    }

    public int getSingleRecordIndex() {
        return singleRecordIndex;
    }

    /**
     * Sets the index of the record that will be used in single record mode,
     * and reloads the document if it is currently in single record mode
     *
     * @param singleRecordIndex the index of the new record for single record mode
     * */
    public void setSingleRecordIndex(int singleRecordIndex) {
        this.singleRecordIndex = singleRecordIndex;
        if (singleRecordView) {
            reload();
        }
    }

    public int getLabelColumnWidth() {
        return labelColumnWidth;
    }

    public void setLabelColumnWidth(int labelColumnWidth) {
        this.labelColumnWidth = labelColumnWidth;
    }

    public float getLineSpacing() {
        return lineSpacing;
    }

    public void setLineSpacing(float lineSpacing) {
        this.lineSpacing = lineSpacing;
    }

    /**
     * Gets the text value of a given tier for a given transcriber
     *
     * @param tier the tier that the value will come from
     * @param transcriber the transcriber whose text will be returned
     * @return the text value of a given tier for a given transcriber (or the regular text for the tier if the transcriber is the validator or {@code null})
     * */
    public String getTierText(Tier<?> tier, String transcriber) {
        if (tier.isBlind() && transcriber != null && !Transcriber.VALIDATOR.getUsername().equals(transcriber)) {

            if (tier.hasBlindTranscription(transcriber)) {
                if (tier.isBlindTranscriptionUnvalidated(transcriber)) {
                    return tier.getBlindUnvalidatedValue(transcriber).getValue();
                }
                return tier.getBlindTranscription(transcriber).toString();
            }
        }
        else {
            if (tier.hasValue()) {
                if (tier.isUnvalidated()) {
                    return tier.getUnvalidatedValue().getValue();
                }
                return tier.getValue().toString();
            }
        }

        return "";
    }

    public SessionEditUndoSupport getUndoSupport() {
        return undoSupport;
    }

    public void setUndoSupport(SessionEditUndoSupport undoSupport) {
        this.undoSupport = undoSupport;
    }

    public EditorEventManager getEventManager() {
        return eventManager;
    }

    public void setEventManager(EditorEventManager eventManager) {
        this.eventManager = eventManager;
    }

    public SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public ArrayList<ElementSpec> getBatch() {
        return batch;
    }

    /**
     * Returns whether the next call of {@code remove()} from the document will bypass the document filter
     *
     * @return whether the remove will be bypassed
     * */
    public boolean isBypassDocumentFilter() {
        return bypassDocumentFilter;
    }

    /**
     * Sets whether the next call of {@code remove()} from the document will bypass the document filter
     *
     * @param bypassDocumentFilter whether the remove will be bypassed
     * */
    public void setBypassDocumentFilter(boolean bypassDocumentFilter) {
        this.bypassDocumentFilter = bypassDocumentFilter;
    }

    public boolean isChatTierNamesShown() {
        return chatTierNamesShown;
    }

    public void setChatTierNamesShown(boolean chatTierNamesShown) {
        this.chatTierNamesShown = chatTierNamesShown;
        reload();
    }

    // endregion Getters and Setters

    // region Attribute Getters

    /**
     * Gets the attributes for a given record
     *
     * @param recordIndex the index of the record to get the attributes of
     * @return a mutable attribute set containing all the necessary attributes for the given record
     * */
    public SimpleAttributeSet getRecordAttributes(int recordIndex) {
        SimpleAttributeSet retVal = new SimpleAttributeSet();

        Record record = session.getRecord(recordIndex);
        retVal.addAttribute(TranscriptStyleConstants.ATTR_KEY_RECORD, record);

        retVal.addAttribute(TranscriptStyleConstants.ATTR_KEY_ELEMENT_TYPE, TranscriptStyleConstants.ATTR_KEY_RECORD);

        return retVal;
    }

    /**
     * Gets the attributes for a given tier
     *
     * @param tier a reference to the tier to get the attributes of
     * @return a mutable attribute set containing all the necessary attributes for the given tier
     * */
    public SimpleAttributeSet getTierAttributes(Tier<?> tier) {
        return getTierAttributes(tier, null);
    }
    /**
     * Gets the attributes for a given tier
     *
     * @param tier a reference to the tier to get the attributes of
     * @param item a {@link TierViewItem} whose font data will be used if provided
     * @return a mutable attribute set containing all the necessary attributes for the given tier
     * */
    private SimpleAttributeSet getTierAttributes(Tier<?> tier, TierViewItem item) {
        final SimpleAttributeSet retVal = new SimpleAttributeSet();

        retVal.addAttribute(TranscriptStyleConstants.ATTR_KEY_TIER, tier);

        String fontString = "default";
        if (item != null) {
            fontString = item.getTierFont();
        }
        if ("default".equalsIgnoreCase(fontString)) {
            fontString = new FontFormatter().format(FontPreferences.getTierFont());
        }

        var font = Font.decode(fontString);

        StyleConstants.setFontFamily(retVal, font.getFamily());
        StyleConstants.setFontSize(retVal, font.getSize() + (int) PrefHelper.getUserPreferences().getFloat(TranscriptView.FONT_SIZE_DELTA_PROP, 0));
        StyleConstants.setBold(retVal, font.isBold());
        StyleConstants.setItalic(retVal, font.isItalic());

        return retVal;
    }

    /**
     * Gets an attribute set containing a reference to the syllabification component factory.
     * Adding the contents of this attribute set to the attributes of a syllabification tier will
     * cause it to appear as the {@link ca.phon.ui.ipa.SyllabificationDisplay} component instead of text
     *
     * @return an attribute set containing a reference to the syllabification component factory
     * */
    public SimpleAttributeSet getSyllabificationAttributes() {
        final SimpleAttributeSet retVal = new SimpleAttributeSet();
        retVal.addAttribute(TranscriptStyleConstants.ATTR_KEY_COMPONENT_FACTORY, new SyllabificationComponentFactory());
        return retVal;
    }

    /**
     * Gets an attribute set containing a reference to the alignment component factory.
     * Adding the contents of this attribute set to the attributes of an alignment tier will
     * cause it to appear as the {@link ca.phon.ui.ipa.PhoneMapDisplay} component instead of text
     *
     * @return an attribute set containing a reference to the alignment component factory
     * */
    public SimpleAttributeSet getAlignmentAttributes() {
        final SimpleAttributeSet retVal = new SimpleAttributeSet();
        retVal.addAttribute(TranscriptStyleConstants.ATTR_KEY_COMPONENT_FACTORY, new AlignmentComponentFactory());
        return retVal;
    }

    /**
     * Gets an attribute set containing a reference to the transcript selector component factory.
     * Adding the contents of this attribute set to the attributes some text will replace the text
     * with a button for selecting the specified blind transcription
     *
     * @param record the record containing the blind tier that the transcription will be selected from
     * @param tier the tier that the blind transcription will be selected from
     * @param transcriptionText the text of the transcription that will be selected if the button is clicked
     * @return an attribute set containing a reference to the transcript selector component factory
     * */
    public SimpleAttributeSet getTranscriptionSelectorAttributes(Record record, Tier<?> tier, String transcriptionText) {
        final SimpleAttributeSet retVal = new SimpleAttributeSet();

        retVal.addAttribute(TranscriptStyleConstants.ATTR_KEY_COMPONENT_FACTORY, new TranscriptionSelectorComponentFactory(
            session,
            eventManager,
            undoSupport,
            record,
            tier,
            transcriptionText
        ));
        retVal.addAttribute(TranscriptStyleConstants.ATTR_KEY_NOT_EDITABLE, true);

        return retVal;
    }

    /**
     * Gets the attributes for an IPA word
     *
     * @param tier the tier that will be referenced in the attributes with
     *             the {@code TranscriptStyleConstants.ATTR_KEY_TIER} key
     * @return a mutable attribute set containing the attributes for an IPA word
     * */
    private SimpleAttributeSet getIPAWordAttributes(Tier<IPATranscript> tier) {
        SimpleAttributeSet retVal = new SimpleAttributeSet();
        retVal.addAttribute(TranscriptStyleConstants.ATTR_KEY_TIER, tier);

        StyleConstants.setForeground(retVal, UIManager.getColor(TranscriptEditorUIProps.IPA_WORD));

        return retVal;
    }

    /**
     * Gets the attributes for an IPA pause
     *
     * @param tier the tier that will be referenced in the attributes with
     *             the {@code TranscriptStyleConstants.ATTR_KEY_TIER} key
     * @return a mutable attribute set containing the attributes for an IPA pause
     * */
    private SimpleAttributeSet getIPAPauseAttributes(Tier<IPATranscript> tier) {
        SimpleAttributeSet retVal = new SimpleAttributeSet();
        retVal.addAttribute(TranscriptStyleConstants.ATTR_KEY_TIER, tier);

        StyleConstants.setForeground(retVal, UIManager.getColor(TranscriptEditorUIProps.IPA_PAUSE));

        return retVal;
    }

    /**
     * Gets the attributes for the times in a given segment
     *
     * @param segment the segment that will be referenced in the attributes with
     *             the {@code TranscriptStyleConstants.ATTR_KEY_MEDIA_SEGMENT} key
     * @return a mutable attribute set containing the attributes for the times in a given segment
     * */
    private SimpleAttributeSet getSegmentTimeAttributes(MediaSegment segment) {
        SimpleAttributeSet retVal = new SimpleAttributeSet();
        retVal.addAttribute(TranscriptStyleConstants.ATTR_KEY_MEDIA_SEGMENT, segment);

        return retVal;
    }

    /**
     * Gets the attributes for the dash in a given segment
     *
     * @param segment the segment that will be referenced in the attributes with
     *             the {@code TranscriptStyleConstants.ATTR_KEY_MEDIA_SEGMENT} key
     * @return a mutable attribute set containing the attributes for the dash in a given segment
     * */
    private SimpleAttributeSet getSegmentDashAttributes(MediaSegment segment) {
        SimpleAttributeSet retVal = new SimpleAttributeSet();
        retVal.addAttribute(TranscriptStyleConstants.ATTR_KEY_MEDIA_SEGMENT, segment);

        StyleConstants.setForeground(retVal, UIManager.getColor(TranscriptEditorUIProps.SEGMENT_DASH));

        return retVal;
    }

    /**
     * Gets the attributes for a {@link TierString} in some {@link TierData}
     *
     * @return the attributes for a {@link TierString} in some {@link TierData}
     * */
    private SimpleAttributeSet getTierStringAttributes() {
        SimpleAttributeSet retVal = new SimpleAttributeSet();

        // TODO: add a ui prop color  for this
        StyleConstants.setForeground(retVal, Color.black);

        return retVal;
    }

    /**
     * Gets the attributes for a {@link TierComment} in some {@link TierData}
     *
     * @return the attributes for a {@link TierComment} in some {@link TierData}
     * */
    private SimpleAttributeSet getTierCommentAttributes() {
        SimpleAttributeSet retVal = new SimpleAttributeSet();

        StyleConstants.setForeground(retVal, UIManager.getColor(TranscriptEditorUIProps.TIER_COMMENT));

        return retVal;
    }

    /**
     * Gets the attributes for {@link TierInternalMedia} in some {@link TierData}
     *
     * @return the attributes for {@link TierInternalMedia} in some {@link TierData}
     * */
    private SimpleAttributeSet getTierInternalMediaAttributes() {
        SimpleAttributeSet retVal = new SimpleAttributeSet();

        StyleConstants.setForeground(retVal, UIManager.getColor(TranscriptEditorUIProps.INTERNAL_MEDIA));

        return retVal;
    }

    /**
     * Gets the attributes for a {@link TierLink} in some {@link TierData}
     *
     * @return the attributes for a {@link TierLink} in some {@link TierData}
     * */
    private SimpleAttributeSet getTierLinkAttributes() {
        SimpleAttributeSet retVal = new SimpleAttributeSet();

        return retVal;
    }

    /**
     * Gets the attributes for the label of a given tier line.
     *
     * @param tier the tier whose label these attributes will be for
     * @return a mutable attribute set containing all the necessary attributes for the label of the tier
     * */
    public SimpleAttributeSet getTierLabelAttributes(Tier<?> tier) {
        final SimpleAttributeSet retVal = new SimpleAttributeSet();

        retVal.addAttribute(TranscriptStyleConstants.ATTR_KEY_LABEL, true);
        retVal.addAttribute(TranscriptStyleConstants.ATTR_KEY_NOT_TRAVERSABLE, true);
        retVal.addAttribute(TranscriptStyleConstants.ATTR_KEY_NOT_EDITABLE, true);
        retVal.addAttribute(TranscriptStyleConstants.ATTR_KEY_TIER, tier);

        retVal.addAttributes(getMonospaceFontAttributes());

        return retVal;
    }

    /**
     * Gets the attributes for a separator / record header
     *
     * @return the attributes for a separator / record header
     * */
    private SimpleAttributeSet getSeparatorAttributes() {
        final SimpleAttributeSet retVal = new SimpleAttributeSet();

        retVal.addAttribute(TranscriptStyleConstants.ATTR_KEY_SEPARATOR, true);

        retVal.addAttributes(getMonospaceFontAttributes());

        return retVal;
    }

    /**
     * Gets the attributes for a given comment
     *
     * @param comment the comment that the attributes will be for
     * @return the attributes for the given comment
     * */
    private SimpleAttributeSet getCommentAttributes(Comment comment) {
        SimpleAttributeSet retVal = new SimpleAttributeSet();

        retVal.addAttribute(TranscriptStyleConstants.ATTR_KEY_ELEMENT_TYPE, TranscriptStyleConstants.ATTR_KEY_COMMENT);
        retVal.addAttribute(TranscriptStyleConstants.ATTR_KEY_COMMENT, comment);

        return retVal;
    }

    /**
     * Gets the attributes for the label of a given comment line.
     *
     * @param comment the comment whose label these attributes will be for
     * @return a mutable attribute set containing all the necessary attributes for the label of the comment
     * */
    private SimpleAttributeSet getCommentLabelAttributes(Comment comment) {
        SimpleAttributeSet retVal = new SimpleAttributeSet();

        retVal.addAttributes(getCommentAttributes(comment));
        retVal.addAttributes(getMonospaceFontAttributes());

        retVal.addAttribute(TranscriptStyleConstants.ATTR_KEY_LABEL, true);
        retVal.addAttribute(TranscriptStyleConstants.ATTR_KEY_NOT_TRAVERSABLE, true);
        retVal.addAttribute(TranscriptStyleConstants.ATTR_KEY_NOT_EDITABLE, true);

        return retVal;
    }

    /**
     * Gets the attributes for a given gem
     *
     * @param gem the gem that the attributes will be for
     * @return the attributes for the given gem
     * */
    private SimpleAttributeSet getGemAttributes(Gem gem) {
        SimpleAttributeSet retVal = new SimpleAttributeSet();

        retVal.addAttribute(TranscriptStyleConstants.ATTR_KEY_ELEMENT_TYPE, TranscriptStyleConstants.ATTR_KEY_GEM);
        retVal.addAttribute(TranscriptStyleConstants.ATTR_KEY_GEM, gem);

        return retVal;
    }

    /**
     * Gets the attributes for the label of a given gem line.
     *
     * @param gem the gem whose label these attributes will be for
     * @return a mutable attribute set containing all the necessary attributes for the label of the gem
     * */
    private SimpleAttributeSet getGemLabelAttributes(Gem gem) {
        SimpleAttributeSet retVal = new SimpleAttributeSet();

        retVal.addAttributes(getGemAttributes(gem));
        retVal.addAttributes(getMonospaceFontAttributes());

        retVal.addAttribute(TranscriptStyleConstants.ATTR_KEY_LABEL, true);
        retVal.addAttribute(TranscriptStyleConstants.ATTR_KEY_NOT_TRAVERSABLE, true);
        retVal.addAttribute(TranscriptStyleConstants.ATTR_KEY_NOT_EDITABLE, true);

        return retVal;
    }

    /**
     * Gets the attributes for the blind transcription of a given transcriber in a given tier
     *
     * @param tier the tier that contains the transcriptions
     * @param transcriber the name/ID of the transcriber who transcribed the transcription that
     *                    these attributes will be added to
     * @return a mutable attribute set containing the necessary attributes for the specified blind transcription
     * */
    public SimpleAttributeSet getBlindTranscriptionAttributes(Tier<?> tier, String transcriber) {
        SimpleAttributeSet retVal = new SimpleAttributeSet();

        retVal.addAttribute(TranscriptStyleConstants.ATTR_KEY_ELEMENT_TYPE, TranscriptStyleConstants.ATTR_KEY_BLIND_TRANSCRIPTION);
        retVal.addAttribute(TranscriptStyleConstants.ATTR_KEY_TIER, tier);
        retVal.addAttribute(TranscriptStyleConstants.ATTR_KEY_TRANSCRIBER, transcriber);
        retVal.addAttribute(TranscriptStyleConstants.ATTR_KEY_NOT_EDITABLE, true);

        return retVal;
    }

    /**
     * Gets the attributes for the label of a line that isn't a tier, record header / separator, comment or gem
     *
     * @return a mutable attribute set containing the necessary attributes for a label
     * */
    public SimpleAttributeSet getLabelAttributes() {
        SimpleAttributeSet retVal = new SimpleAttributeSet();

        retVal.addAttributes(getMonospaceFontAttributes());

        retVal.addAttribute(TranscriptStyleConstants.ATTR_KEY_LABEL, true);
        retVal.addAttribute(TranscriptStyleConstants.ATTR_KEY_NOT_TRAVERSABLE, true);
        retVal.addAttribute(TranscriptStyleConstants.ATTR_KEY_NOT_EDITABLE, true);

        return retVal;
    }

    /**
     * Gets the attributes corresponding to the standard tier font
     *
     * @return a mutable attribute set containing the attributes corresponding to the standard tier font
     * */
    public SimpleAttributeSet getStandardFontAttributes() {
        SimpleAttributeSet retVal = new SimpleAttributeSet();

        Font font = FontPreferences.getTierFont();
        StyleConstants.setFontFamily(retVal, font.getFamily());
        StyleConstants.setFontSize(retVal, 14);
        StyleConstants.setBold(retVal, font.isBold());
        StyleConstants.setItalic(retVal, font.isItalic());

        return retVal;
    }

    /**
     * Gets the attributes corresponding to the monospace font
     *
     * @return a mutable attribute set containing the attributes corresponding to the monospace font
     * */
    private SimpleAttributeSet getMonospaceFontAttributes() {
        SimpleAttributeSet retVal = new SimpleAttributeSet();

        Font font = FontPreferences.getMonospaceFont();
        StyleConstants.setFontFamily(retVal, font.getFamily());
        StyleConstants.setFontSize(retVal, 14);
        StyleConstants.setBold(retVal, font.isBold());
        StyleConstants.setItalic(retVal, font.isItalic());

        return retVal;
    }

    /**
     * Gets the attributes for the tiers header
     *
     * @return the attributes for the tiers header
     * */
    public SimpleAttributeSet getTiersHeaderAttributes() {
        SimpleAttributeSet retVal = new SimpleAttributeSet();

        retVal.addAttribute(TranscriptStyleConstants.ATTR_KEY_NOT_EDITABLE, true);

        return retVal;
    }

    /**
     * Gets the attributes for the participants header
     *
     * @return the attributes for the participants header
     * */
    public SimpleAttributeSet getParticipantsHeaderAttributes() {
        SimpleAttributeSet retVal = new SimpleAttributeSet();

        retVal.addAttribute(TranscriptStyleConstants.ATTR_KEY_NOT_EDITABLE, true);

        return retVal;
    }

    // endregion Attribute Getters

    // region Batching

    /**
     * Gets a {@link javax.swing.text.DefaultStyledDocument.ElementSpec} containing the specified string and attributes
     *
     * @return a {@link javax.swing.text.DefaultStyledDocument.ElementSpec} containing the specified
     * string and attributes
     * */
    public ElementSpec getBatchString(String str, MutableAttributeSet a) {
        char[] chars = str.toCharArray();
        return new ElementSpec(new SimpleAttributeSet(a), ElementSpec.ContentType, chars, 0, str.length());
    }
    /**
     * Appends a {@link javax.swing.text.DefaultStyledDocument.ElementSpec} containing the specified string and
     * attributes to the end of the batch
     * */
    public void appendBatchString(String str, MutableAttributeSet a) {
        final StringBuilder builder = new StringBuilder();
        builder.append(str);
        final List<ElementSpec> additionalInsertions = new ArrayList<>();
        for (InsertionHook hook : getInsertionHooks()) {
            additionalInsertions.addAll(hook.batchInsertString(builder, a));
        }
        batch.add(getBatchString(builder.toString(), a));
        batch.addAll(additionalInsertions);
    }

    /**
     * Gets a list of {@link javax.swing.text.DefaultStyledDocument.ElementSpec} containing a newline character
     * with the specified attributes and the {@code ElementSpec.EndTagType} and {@code ElementSpec.StartTagType} tags
     *
     * @return a list with the newline character and the end and start tags
     * */
    public List<ElementSpec> getBatchEndLineFeed(AttributeSet a) {
        List<ElementSpec> retVal = new ArrayList<>();
        retVal.add(new ElementSpec(a, ElementSpec.ContentType, EOL_ARRAY, 0, 1));
        retVal.addAll(getBatchEndStart());
        return retVal;
    }
    /**
     * Appends a newline character with the given attributes and the start and end tags to the end of the batch
     * */
    public void appendBatchLineFeed(AttributeSet a) {
        batch.addAll(getBatchEndLineFeed(a));
    }

    /**
     * Gets a list of {@link javax.swing.text.DefaultStyledDocument.ElementSpec} containing
     * the {@code ElementSpec.EndTagType} and {@code ElementSpec.StartTagType} tags
     *
     * @return a list containing the end and start tags
     * */
    public List<ElementSpec> getBatchEndStart() {
        List<ElementSpec> retVal = new ArrayList<>();
        retVal.add(new ElementSpec(null, ElementSpec.EndTagType));
        retVal.add(new ElementSpec(null, ElementSpec.StartTagType));
        return retVal;
    }
    /**
     * Appends the end and start tags to the end of the batch
     * */
    public void appendBatchEndStart() {
        batch.addAll(getBatchEndStart());
    }

    /**
     * Adds the contents of the batch to the document at the specified offset,
     * clears the batch,
     * and sets the global paragraph attributes
     * */
    public void processBatchUpdates(int offs) throws BadLocationException {
        // As with insertBatchString, this could be synchronized if
        // there was a chance multiple threads would be in here.
        ElementSpec[] inserts = new ElementSpec[batch.size()];
        batch.toArray(inserts);

        // Process all the inserts in bulk
        super.insert(offs, inserts);

        // Empty batch the list
        batch.clear();

        setGlobalParagraphAttributes();
    }

    // endregion Batching

    // region Add Comment / Gem

    /**
     * Adds a comment to the document and the transcript at the given transcript element index
     * */
    public void addComment(Comment comment, int transcriptElementIndex) {
        int offset = -1;

        if (transcriptElementIndex == 0) {
            var elementAfterComment = session.getTranscript().getElementAt(1);
            if (elementAfterComment.isRecord()) {
                offset = getRecordStart(session.getRecordPosition(elementAfterComment.asRecord()));
            }
            else if (elementAfterComment.isComment()) {
                offset = getCommentStart(elementAfterComment.asComment());
            }
            else if (elementAfterComment.isGem()) {
                offset = getGemStart(elementAfterComment.asGem());
            }
            else {
                throw new RuntimeException("Invalid transcript element");
            }

            offset -= labelColumnWidth + 2;
        }
        else {
            var elementBeforeComment = session.getTranscript().getElementAt(transcriptElementIndex-1);
            if (elementBeforeComment.isRecord()) {
                offset = getRecordEnd(session.getRecordPosition(elementBeforeComment.asRecord()));
            }
            else if (elementBeforeComment.isComment()) {
                offset = getCommentEnd(elementBeforeComment.asComment());
            }
            else if (elementBeforeComment.isGem()) {
                offset = getGemEnd(elementBeforeComment.asGem());
            }
            else {
                throw new RuntimeException("Invalid transcript element");
            }
        }

        try {
            appendBatchEndStart();
            var attrs = writeComment(comment);
            appendBatchLineFeed(attrs);
            processBatchUpdates(offset);
        }
        catch (BadLocationException e) {
            LogUtil.severe(e);
        }
    }

    // Adds a gem to the document and the transcript at the given transcript element index
    public void addGem(Gem gem, int transcriptElementIndex) {
        int offset = -1;

        if (transcriptElementIndex == 0) {
            var elementAfterComment = session.getTranscript().getElementAt(1);
            if (elementAfterComment.isRecord()) {
                offset = getRecordStart(session.getRecordPosition(elementAfterComment.asRecord()));
            }
            else if (elementAfterComment.isComment()) {
                offset = getCommentStart(elementAfterComment.asComment());
            }
            else if (elementAfterComment.isGem()) {
                offset = getGemStart(elementAfterComment.asGem());
            }
            else {
                throw new RuntimeException("Invalid transcript element");
            }

            offset -= labelColumnWidth + 2;
        }
        else {
            var elementBeforeComment = session.getTranscript().getElementAt(transcriptElementIndex-1);
            if (elementBeforeComment.isRecord()) {
                offset = getRecordEnd(session.getRecordPosition(elementBeforeComment.asRecord()));
            }
            else if (elementBeforeComment.isComment()) {
                offset = getCommentEnd(elementBeforeComment.asComment());
            }
            else if (elementBeforeComment.isGem()) {
                offset = getGemEnd(elementBeforeComment.asGem());
            }
            else {
                throw new RuntimeException("Invalid transcript element");
            }
        }

        try {
            appendBatchEndStart();
            var attrs = writeGem(gem);
            appendBatchLineFeed(attrs);
            processBatchUpdates(offset);
        }
        catch (BadLocationException e) {
            LogUtil.severe(e);
        }
    }

    // endregion Add Comment / Gem

    /**
     * Deletes a given transcript element from the document
     * */
    public void deleteTranscriptElement(Transcript.Element elem) {
        int labelLength = labelColumnWidth + 2;
        try {
            int startOffset = -1;
            int endOffset = -1;

            if (elem.isComment()) {
                startOffset = getCommentStart(elem.asComment());
                endOffset = getCommentEnd(elem.asComment());
            }
            else if (elem.isGem()) {
                startOffset = getGemStart(elem.asGem());
                endOffset = getGemEnd(elem.asGem());
            }
            else if (elem.isRecord()) {
                return;
            }

            startOffset -= labelLength;

            if (startOffset < 0 || endOffset < 0) return;

            bypassDocumentFilter = true;
            remove(startOffset, endOffset - startOffset);
        }
        catch (BadLocationException e) {
            LogUtil.severe(e);
        }
    }

    /**
     * Updates the displayed type of the given comment in the document
     * */
    public void onChangeCommentType(Comment comment) {
        int start = getCommentStart(comment);
        int end = getCommentEnd(comment);

        start -= labelColumnWidth + 2;

        try {
            bypassDocumentFilter = true;
            remove(start, end - start);
            appendBatchEndStart();
            var attrs = writeComment(comment);
            appendBatchLineFeed(attrs);
            processBatchUpdates(start);
            setGlobalParagraphAttributes();
        }
        catch (BadLocationException e) {
            LogUtil.severe(e);
        }
    }

    /**
     * Updates the displayed type of the given gem in the document
     * */
    public void onChangeGemType(Gem gem) {
        int start = getGemStart(gem);
        int end = getGemEnd(gem);

        start -= labelColumnWidth + 2;

        try {
            bypassDocumentFilter = true;
            remove(start, end - start);
            appendBatchEndStart();
            var attrs = writeGem(gem);
            appendBatchLineFeed(attrs);
            processBatchUpdates(start);
            setGlobalParagraphAttributes();
        }
        catch (BadLocationException e) {
            LogUtil.severe(e);
        }
    }

    // region Write Transcript Element

    /**
     * Writes the contents of the given record to the batch
     *
     * @param record the record that will be written to the batch
     * @param transcript a reference to the transcript containing the record
     * @param tierView a list of {@link TierViewItem} specifying how the tiers within the record should be inserted
     * @return a mutable attribute set containing the attributes of the last character of the record to add a
     * newline after if need be
     * */
    private SimpleAttributeSet writeRecord(
            Record record,
            Transcript transcript,
            List<TierViewItem> tierView
    ) {

        List<ElementSpec> additionalInsertions = new ArrayList<>();
        for (var hook : getInsertionHooks()) {
            additionalInsertions.addAll(hook.startRecord());
        }
        if (!additionalInsertions.isEmpty()) {
            batch.addAll(additionalInsertions);
            additionalInsertions.clear();
        }

        for (var hook : getInsertionHooks()) {
            additionalInsertions.addAll(hook.startRecordHeader());
        }
        if (!additionalInsertions.isEmpty()) {
            batch.addAll(additionalInsertions);
            additionalInsertions.clear();
        }

        int recordIndex = transcript.getRecordPosition(record);
        SimpleAttributeSet recordAttrs = getRecordAttributes(recordIndex);

        SimpleAttributeSet tierAttrs = getTierAttributes(record.getSegmentTier());
        tierAttrs.addAttributes(getSeparatorAttributes());
        tierAttrs.addAttributes(recordAttrs);

        SimpleAttributeSet labelAttrs = getTierLabelAttributes(record.getSegmentTier());
        labelAttrs.addAttributes(getSeparatorAttributes());
        labelAttrs.addAttributes(recordAttrs);

        String labelText = record.getSpeaker().toString();
        if (labelText.length() < labelColumnWidth) {
            appendBatchString(" ".repeat((labelColumnWidth - labelText.length())), labelAttrs);
        }
        else {
            labelText = formatLabelText(labelText);
        }
        labelAttrs.addAttribute(TranscriptStyleConstants.ATTR_KEY_CLICKABLE, true);
        appendBatchString(labelText, labelAttrs);
        labelAttrs.removeAttribute(TranscriptStyleConstants.ATTR_KEY_CLICKABLE);
        appendBatchString("  ", labelAttrs);

        MediaSegment segment = record.getMediaSegment();

        tierAttrs.addAttributes(getStandardFontAttributes());
        appendFormattedSegment(segment, tierAttrs);
        appendBatchLineFeed(tierAttrs);

        for (var hook : getInsertionHooks()) {
            additionalInsertions.addAll(hook.endRecordHeader());
        }
        if (!additionalInsertions.isEmpty()) {
            batch.addAll(additionalInsertions);
            additionalInsertions.clear();
        }

        tierAttrs.removeAttribute(TranscriptStyleConstants.ATTR_KEY_SEPARATOR);

        List<TierViewItem> visibleTierView = tierView.stream().filter(item -> item.isVisible()).toList();

        for (int i = 0; i < visibleTierView.size(); i++) {
            TierViewItem item = visibleTierView.get(i);
            Tier<?> tier = record.getTier(item.getTierName());
            // Create a tier if it doesn't exist in the record
            // This tier will be added to the record when a change is made in the TierEdit
            if (tier == null) {
                TierDescription td = session.getUserTiers().get(item.getTierName());
                if (td == null) continue;
                tier = sessionFactory.createTier(td);
            }

            for (var hook : getInsertionHooks()) {
                additionalInsertions.addAll(hook.startTier());
            }
            if (!additionalInsertions.isEmpty()) {
                batch.addAll(additionalInsertions);
                additionalInsertions.clear();
            }

            tierAttrs = insertTier(recordIndex, tier, item, recordAttrs);

            for (var hook : getInsertionHooks()) {
                additionalInsertions.addAll(hook.endTier(tierAttrs));
            }
            if (!additionalInsertions.isEmpty()) {
                batch.addAll(additionalInsertions);
                tierAttrs = new SimpleAttributeSet(additionalInsertions.get(additionalInsertions.size()-1).getAttributes());
                additionalInsertions.clear();
            }

            tierAttrs.removeAttribute(TranscriptStyleConstants.ATTR_KEY_COMPONENT_FACTORY);

            if (i < visibleTierView.size() - 1) {
                appendBatchLineFeed(tierAttrs);
            }
        }

        for (var hook : getInsertionHooks()) {
            additionalInsertions.addAll(hook.endRecord());
        }
        if (!additionalInsertions.isEmpty()) {
            batch.addAll(additionalInsertions);
        }

        return tierAttrs;
    }

    /**
     * Writes a given comment to the batch
     *
     * @param comment the comment that will be written
     * @return a mutable attribute set containing the attributes of the last character of the comment to add a
     * newline after if need be
     * */
    private SimpleAttributeSet writeComment(Comment comment) {

        List<ElementSpec> additionalInsertions = new ArrayList<>();
        for (var hook : getInsertionHooks()) {
            additionalInsertions.addAll(hook.startComment());
        }
        if (!additionalInsertions.isEmpty()) {
            batch.addAll(additionalInsertions);
            additionalInsertions.clear();
        }

        Tier<TierData> commentTier = sessionFactory.createTier("commentTier", TierData.class);
        commentTier.setValue(comment.getValue() == null ? new TierData() : comment.getValue());

        SimpleAttributeSet commentAttrs = getCommentAttributes(comment);
        commentAttrs.addAttribute(TranscriptStyleConstants.ATTR_KEY_COMMENT_TIER, commentTier);
        commentAttrs.addAttributes(getStandardFontAttributes());

        SimpleAttributeSet labelAttrs = getCommentLabelAttributes(comment);
        String labelText = comment.getType().getLabel();
        if (labelText.length() < labelColumnWidth) {
            appendBatchString(" ".repeat((labelColumnWidth - labelText.length())), labelAttrs);
        }
        else {
            labelText = formatLabelText(labelText);
        }

        TierData tierData = commentTier.getValue();

        labelAttrs.addAttribute(TranscriptStyleConstants.ATTR_KEY_CLICKABLE, true);
        appendBatchString(labelText, labelAttrs);

        labelAttrs.removeAttribute(TranscriptStyleConstants.ATTR_KEY_CLICKABLE);
        appendBatchString(": ", labelAttrs);

        for (int i = 0; i < tierData.length(); i++) {
            TierElement userTierElement = tierData.elementAt(i);
            String text = null;
            SimpleAttributeSet attrs;
            if (userTierElement instanceof TierString tierString) {
                // Text
                text = tierString.text();
                attrs = getTierStringAttributes();
            } else if (userTierElement instanceof TierComment userTierComment) {
                // Comment
                text = userTierComment.toString();
                attrs = getTierCommentAttributes();
            } else if (userTierElement instanceof TierInternalMedia internalMedia) {
                // Internal media
                attrs = getTierInternalMediaAttributes();
                appendFormattedInternalMedia(internalMedia.getInternalMedia(), attrs);
            }
            else if (userTierElement instanceof TierLink link) {
                // Link
                text = link.toString();
                attrs = getTierLinkAttributes();
            }
            else {
                throw new RuntimeException("Invalid type");
            }

            attrs.addAttributes(commentAttrs);

            if (text != null) appendBatchString(text, attrs);

            if (i < tierData.length() - 1) {
                appendBatchString(" ", attrs);
            }
        }

        for (var hook : getInsertionHooks()) {
            additionalInsertions.addAll(hook.endComment());
        }
        if (!additionalInsertions.isEmpty()) {
            batch.addAll(additionalInsertions);
        }

        return commentAttrs;
    }

    /**
     * Writes a given gem to the batch
     *
     * @param gem the comment that will be written
     * @return a mutable attribute set containing the attributes of the last character of the gem to add a
     * newline after if need be
     * */
    private SimpleAttributeSet writeGem(Gem gem) {
        List<ElementSpec> additionalInsertions = new ArrayList<>();
        for (var hook : getInsertionHooks()) {
            additionalInsertions.addAll(hook.startGem());
        }
        if (!additionalInsertions.isEmpty()) {
            batch.addAll(additionalInsertions);
            additionalInsertions.clear();
        }

        String text = gem.getLabel();

        SimpleAttributeSet gemAttrs = getGemAttributes(gem);
        gemAttrs.addAttributes(getStandardFontAttributes());

        SimpleAttributeSet labelAttrs = getGemLabelAttributes(gem);
        String labelText = gem.getType().toString() + " Gem";
        if (labelText.length() < labelColumnWidth) {
            appendBatchString(" ".repeat((labelColumnWidth - labelText.length())), labelAttrs);
        }
        else {
            labelText = formatLabelText(labelText);
        }

        labelAttrs.addAttribute(TranscriptStyleConstants.ATTR_KEY_CLICKABLE, true);
        appendBatchString(labelText, labelAttrs);

        labelAttrs.removeAttribute(TranscriptStyleConstants.ATTR_KEY_CLICKABLE);
        appendBatchString(": ", labelAttrs);

        appendBatchString(text, gemAttrs);

        for (var hook : getInsertionHooks()) {
            additionalInsertions.addAll(hook.endGem());
        }
        if (!additionalInsertions.isEmpty()) {
            batch.addAll(additionalInsertions);
        }

        return gemAttrs;
    }

    /**
     * Gets a list of {@link javax.swing.text.DefaultStyledDocument.ElementSpec} for a specified "generic tier"
     *
     * @param label the text for the label
     * @param tier the generic tier that the list will contain the data for
     * @param additionalAttributes any additional attributes to be added to the contents of the returned list
     *                             (none added if {@code null})
     * @return a list of {@link javax.swing.text.DefaultStyledDocument.ElementSpec} representing the specified
     * generic tier
     * */
    public List<ElementSpec> getGeneric(String label, Tier<?> tier, AttributeSet additionalAttributes) {
        List<ElementSpec> retVal = new ArrayList<>();

        SimpleAttributeSet attrs = new SimpleAttributeSet();
        attrs.addAttribute(TranscriptStyleConstants.ATTR_KEY_ELEMENT_TYPE, TranscriptStyleConstants.ATTR_KEY_GENERIC);
        attrs.addAttribute(TranscriptStyleConstants.ATTR_KEY_GENERIC, tier);
        attrs.addAttributes(getStandardFontAttributes());
        if (additionalAttributes != null) attrs.addAttributes(additionalAttributes);

        SimpleAttributeSet labelAttrs = new SimpleAttributeSet(attrs);
        labelAttrs.addAttributes(getLabelAttributes());
        String labelText = label;
        if (labelText.length() < labelColumnWidth) {
            retVal.add(getBatchString(" ".repeat((labelColumnWidth - labelText.length())), labelAttrs));
        }
        else {
            labelText = formatLabelText(labelText);
        }

        labelAttrs.addAttribute(TranscriptStyleConstants.ATTR_KEY_CLICKABLE, true);
        retVal.add(getBatchString(labelText, labelAttrs));

        labelAttrs.removeAttribute(TranscriptStyleConstants.ATTR_KEY_CLICKABLE);
        retVal.add(getBatchString(": ", labelAttrs));

        if (tier.isUnvalidated()) {
            retVal.add(getBatchString(tier.getUnvalidatedValue().toString(), attrs));
        }
        else {
            retVal.add(getBatchString(tier.toString(), attrs));
        }

        return retVal;
    }
    /**
     * Writes a given "generic tier" to the batch
     *
     * @param label the text for the label
     * @param tier the generic tier that will be written to the batch
     * @return a mutable attribute set containing the attributes of the last character of the text to add a
     * newline after if need be
     * */
    private SimpleAttributeSet writeGeneric(String label, Tier<?> tier) {
        List<ElementSpec> elementSpecs = getGeneric(label, tier, null);
        batch.addAll(elementSpecs);
        return new SimpleAttributeSet(elementSpecs.get(elementSpecs.size() - 1).getAttributes());
    }
    /**
     * Writes a given "generic tier" to the batch
     *
     * @param label the text for the label
     * @param tier the generic tier that will be written to the batch
     * @param additionalAttributes any additional attributes to be added to the contents of the returned list
     *                             none added if {@code null})
     * @return a mutable attribute set containing the attributes of the last character of the text to add a
     * newline after if need be
     * */
    public SimpleAttributeSet writeGeneric(String label, Tier<?> tier, AttributeSet additionalAttributes) {
        List<ElementSpec> elementSpecs = getGeneric(label, tier, additionalAttributes);
        batch.addAll(elementSpecs);
        return new SimpleAttributeSet(elementSpecs.get(elementSpecs.size() - 1).getAttributes());
    }

    // endregion Write Transcript Element


    // region Get Record/Tier Start/End

    /**
     * Gets the start of the record with the specified index
     *
     * @param recordIndex the index of the record
     * @return the position in the document at the beginning of the records content
     * */
    public int getRecordStart(int recordIndex) {
        return getRecordStart(recordIndex, false);
    }

    /**
     * Gets the start of the record with the specified index
     *
     * @param recordIndex the index of the record
     * @param includeSeparator whether the separator / record header is included in the
     *                         calculation of the start position
     * @return the position in the document at the beginning of the records content
     * */
    public int getRecordStart(int recordIndex, boolean includeSeparator) {
        Element root = getDefaultRootElement();

        for (int i = 0; i < root.getElementCount(); i++) {
            Element elem = root.getElement(i);
            if (elem.getElementCount() < 1) continue;
            AttributeSet attrs = elem.getElement(0).getAttributes();
            Record currentRecord = (Record) attrs.getAttribute(TranscriptStyleConstants.ATTR_KEY_RECORD);
            if (currentRecord == null) continue;
            int currentRecordIndex = session.getRecordPosition(currentRecord);
            var tier = attrs.getAttribute(TranscriptStyleConstants.ATTR_KEY_TIER);
            if ((tier != null || includeSeparator) && recordIndex == currentRecordIndex) {
                return elem.getStartOffset();
            }
        }

        return -1;
    }

    /**
     * Gets the start position of the given record
     *
     * @param record the record whose start position is trying to be found
     * @return the position in the document at the beginning of the records content
     * */
    public int getRecordStart(Record record) {
        return getRecordStart(record, false);
    }

    /**
     * Gets the start position of the given record
     *
     * @param record the record whose start position is trying to be found
     * @param includeSeparator whether the separator / record header is included in the
     *                         calculation of the start position
     * @return the position in the document at the beginning of the records content
     * */
    public int getRecordStart(Record record, boolean includeSeparator) {
        Element root = getDefaultRootElement();

        for (int i = 0; i < root.getElementCount(); i++) {
            Element elem = root.getElement(i);
            if (elem.getElementCount() < 1) continue;
            AttributeSet attrs = elem.getElement(0).getAttributes();
            Record currentRecord = (Record) attrs.getAttribute(TranscriptStyleConstants.ATTR_KEY_RECORD);
            if (currentRecord == null) continue;
            var tier = attrs.getAttribute(TranscriptStyleConstants.ATTR_KEY_TIER);
            if ((tier != null || includeSeparator) && currentRecord == record) {
                return elem.getStartOffset();
            }
        }

        return -1;
    }

    /**
     * Gets the start position of the record containing the given tier
     *
     * @param tier a tier from the record whose start position is trying to be found
     * @return the position in the document at the beginning of the records content
     * */
    public int getRecordStart(Tier<?> tier) {
        Element root = getDefaultRootElement();

        for (int i = 0; i < root.getElementCount(); i++) {
            Element elem = root.getElement(i);
            if (elem.getElementCount() < 1) continue;
            Record record = (Record) elem.getElement(0).getAttributes().getAttribute(TranscriptStyleConstants.ATTR_KEY_RECORD);
            // If correct record
            if (record != null) {
                for (int j = 0; j < elem.getElementCount(); j++) {
                    Element innerElem = elem.getElement(j);
                    AttributeSet attrs = innerElem.getAttributes();
                    Tier<?> currentTier = (Tier<?>)attrs.getAttribute(TranscriptStyleConstants.ATTR_KEY_TIER);
                    // If correct tier
                    if (currentTier != null && currentTier == tier) {
                        return elem.getStartOffset();
                    }
                }
            }
        }

        return -1;
    }

    /**
     * Gets the end position of the record at the specified index
     *
     * @param recordIndex the index of the record whose end is trying to be found
     * @return the position in the document immediately after the final character of the records content
     * (newlines included)
     * */
    public int getRecordEnd(int recordIndex) {
        Element root = getDefaultRootElement();

        int retVal = -1;

        for (int i = 0; i < root.getElementCount(); i++) {
            Element elem = root.getElement(i);
            if (elem.getElementCount() < 1) continue;
            AttributeSet attrs = elem.getElement(0).getAttributes();
            Record currentRecord = (Record) attrs.getAttribute(TranscriptStyleConstants.ATTR_KEY_RECORD);
            if (currentRecord == null) continue;
            int currentRecordIndex = session.getRecordPosition(currentRecord);
            if (recordIndex == currentRecordIndex) {
                retVal = Math.max(retVal, elem.getEndOffset());
            }
        }

        return retVal;
    }
    /**
     * Gets the end position of the specified record
     *
     * @param record the record whose end is trying to be found
     * @return the position in the document immediately after the final character of the records content
     * (newlines included)
     * */
    public int getRecordEnd(Record record) {
        Element root = getDefaultRootElement();

        int retVal = -1;

        for (int i = 0; i < root.getElementCount(); i++) {
            Element elem = root.getElement(i);
            if (elem.getElementCount() < 1) continue;
            AttributeSet attrs = elem.getElement(0).getAttributes();
            Record currentRecord = (Record) attrs.getAttribute(TranscriptStyleConstants.ATTR_KEY_RECORD);
            if (currentRecord == null) continue;
            if (record == currentRecord) {
                retVal = Math.max(retVal, elem.getEndOffset());
            }
        }

        return retVal;
    }
    /**
     * Gets the end position of the record containing the specified tier
     *
     * @param tier a tier from the record whose end position is trying to be found
     * @return the position in the document immediately after the final character of the records content
     * (newlines included)
     * */
    public int getRecordEnd(Tier<?> tier) {
        Element root = getDefaultRootElement();

        for (int i = 0; i < root.getElementCount(); i++) {
            Element elem = root.getElement(i);
            if (elem.getElementCount() < 1) continue;
            Record currentRecord = (Record) elem.getElement(0).getAttributes().getAttribute(TranscriptStyleConstants.ATTR_KEY_RECORD);
            // If correct record index
            if (currentRecord != null) {
                for (int j = 0; j < elem.getElementCount(); j++) {
                    Element innerElem = elem.getElement(j);
                    var currentTier = innerElem.getAttributes().getAttribute(TranscriptStyleConstants.ATTR_KEY_TIER);
                    // If correct tier
                    if (currentTier != null && currentTier == tier) {
                        return getRecordEnd(tier);
                    }
                }
            }
        }

        return -1;
    }

    /**
     * Gets the record containing the specified tier
     *
     * @param tier a tier from the record that is trying to be found
     * @return the record which contains the specified tier
     * */
    public Record getRecord(Tier<?> tier) {
        Element root = getDefaultRootElement();

        for (int i = 0; i < root.getElementCount(); i++) {
            Element elem = root.getElement(i);
            if (elem.getElementCount() < 1) continue;
            Record currentRecord = (Record) elem.getElement(0).getAttributes().getAttribute(TranscriptStyleConstants.ATTR_KEY_RECORD);
            // If correct record index
            if (currentRecord != null) {
                for (int j = 0; j < elem.getElementCount(); j++) {
                    Element innerElem = elem.getElement(j);
                    var currentTier = innerElem.getAttributes().getAttribute(TranscriptStyleConstants.ATTR_KEY_TIER);
                    // If correct tier
                    if (currentTier != null && currentTier == tier) {
                        return currentRecord;
                    }
                }
            }
        }

        return null;
    }

    /**
     * Gets the start position of a tier with the specified name in the record at the specified index
     *
     * @param recordIndex the index of the record that contains the tier
     * @param tierName the name of the tier
     * @return the position in the document at the beginning of the tiers content
     * */
    public int getTierStart(int recordIndex, String tierName) {
        Element root = getDefaultRootElement();

        for (int i = 0; i < root.getElementCount(); i++) {
            Element elem = root.getElement(i);
            if (elem.getElementCount() < 1) continue;
            Record currentRecord = (Record) elem.getElement(0).getAttributes().getAttribute(TranscriptStyleConstants.ATTR_KEY_RECORD);
            if (currentRecord == null) continue;
            int currentRecordIndex = session.getRecordPosition(currentRecord);
            // If correct record index
            if (currentRecordIndex == recordIndex) {
                for (int j = 0; j < elem.getElementCount(); j++) {
                    Element innerElem = elem.getElement(j);
                    AttributeSet attrs = innerElem.getAttributes();
                    Tier<?> tier = (Tier<?>) attrs.getAttribute(TranscriptStyleConstants.ATTR_KEY_TIER);
                    Boolean isLabel = (Boolean) attrs.getAttribute(TranscriptStyleConstants.ATTR_KEY_LABEL);
                    // If correct tier name
                    if (isLabel == null && tier != null && tier.getName().equals(tierName)) {
                        return innerElem.getStartOffset();
                    }
                }
            }
        }

        return -1;
    }

    /**
     * Gets the start position of the specified tier
     *
     * @param tier the tier whose start position is trying to be found
     * @return the position in the document at the beginning of the tiers content
     * */
    public int getTierStart(Tier<?> tier) {
        Element root = getDefaultRootElement();

        for (int i = 0; i < root.getElementCount(); i++) {
            Element elem = root.getElement(i);
            if (elem.getElementCount() == 0) continue;
            String transcriptElementType = (String) elem.getElement(0).getAttributes().getAttribute(TranscriptStyleConstants.ATTR_KEY_ELEMENT_TYPE);
            // If correct record index
            if (transcriptElementType != null && transcriptElementType.equals(TranscriptStyleConstants.ATTR_KEY_RECORD)) {
                for (int j = 0; j < elem.getElementCount(); j++) {
                    Element innerElem = elem.getElement(j);
                    AttributeSet attrs = innerElem.getAttributes();
                    Tier<?> currentTier = (Tier<?>)attrs.getAttribute(TranscriptStyleConstants.ATTR_KEY_TIER);
                    Boolean isLabel = (Boolean)attrs.getAttribute(TranscriptStyleConstants.ATTR_KEY_LABEL);
                    // If correct tier
                    if (isLabel == null && currentTier != null && currentTier == tier) {
                        return innerElem.getStartOffset();
                    }
                }
            }
        }

        return -1;
    }

    /**
     * Gets the end position of a tier with the specified name in the record at the specified index
     *
     * @param recordIndex the index of the record that contains the tier
     * @param tierName the name of the tier
     * @return the position in the document immediately after the final character of the tiers content
     * (newlines included)
     * */
    public int getTierEnd(int recordIndex, String tierName) {
        Element root = getDefaultRootElement();

        int retVal = -1;

        for (int i = 0; i < root.getElementCount(); i++) {
            Element elem = root.getElement(i);
            if (elem.getElementCount() < 1) continue;
            Record currentRecord = (Record) elem.getElement(0).getAttributes().getAttribute(TranscriptStyleConstants.ATTR_KEY_RECORD);
            if (currentRecord == null) continue;
            int currentRecordIndex = session.getRecordPosition(currentRecord);
            // If correct record index
            if ((currentRecordIndex) == recordIndex) {
                for (int j = 0; j < elem.getElementCount(); j++) {
                    Element innerElem = elem.getElement(j);
                    AttributeSet attrs = innerElem.getAttributes();
                    Tier<?> tier = (Tier<?>)attrs.getAttribute(TranscriptStyleConstants.ATTR_KEY_TIER);
                    Boolean isLabel = (Boolean)attrs.getAttribute(TranscriptStyleConstants.ATTR_KEY_LABEL);
                    if (isLabel == null && tier != null && tier.getName().equals(tierName)) {
                        retVal = Math.max(retVal, innerElem.getEndOffset());
                    }
                }
            }
        }

        return retVal;
    }

    /**
     * Gets the end position of the specified tier
     *
     * @param tier the tier whose end position is trying to be found
     * @return the position in the document immediately after the final character of the tiers content
     * (newlines included)
     * */
    public int getTierEnd(Tier<?> tier) {
        Element root = getDefaultRootElement();

        for (int i = 0; i < root.getElementCount(); i++) {
            Element elem = root.getElement(i);
            if (elem.getElementCount() < 1) continue;
            Record currentRecord = (Record) elem.getElement(0).getAttributes().getAttribute(TranscriptStyleConstants.ATTR_KEY_RECORD);
            // If correct record index
            if (currentRecord != null) {
                int currentRecordIndex = session.getRecordPosition(currentRecord);
                for (int j = 0; j < elem.getElementCount(); j++) {
                    Element innerElem = elem.getElement(j);
                    Tier<?> currentTier = (Tier<?>)innerElem.getAttributes().getAttribute(TranscriptStyleConstants.ATTR_KEY_TIER);
                    // If correct tier
                    if (currentTier != null && currentTier == tier) {
                        return getTierEnd(currentRecordIndex, tier.getName());
                    }
                }
            }
        }

        return -1;
    }

    /**
     * Gets the start position of the specified comment
     *
     * @param comment the comment whose start position is trying to be found
     * @return the position in the document at the beginning of the comments content
     * */
    public int getCommentStart(Comment comment) {
        Element root = getDefaultRootElement();

        for (int i = 0; i < root.getElementCount(); i++) {
            Element elem = root.getElement(i);
            if (elem.getElementCount() == 0) continue;
            String transcriptElementType = (String) elem.getElement(0).getAttributes().getAttribute(TranscriptStyleConstants.ATTR_KEY_ELEMENT_TYPE);
            // If transcript element type is comment
            if (transcriptElementType != null && transcriptElementType.equals(TranscriptStyleConstants.ATTR_KEY_COMMENT)) {
                for (int j = 0; j < elem.getElementCount(); j++) {
                    Element innerElem = elem.getElement(j);
                    AttributeSet attrs = innerElem.getAttributes();
                    Comment currentComment = (Comment)attrs.getAttribute(TranscriptStyleConstants.ATTR_KEY_COMMENT);
                    Boolean isLabel = (Boolean)attrs.getAttribute(TranscriptStyleConstants.ATTR_KEY_LABEL);
                    // If correct comment
                    if (isLabel == null && currentComment != null && currentComment == comment) {
                        return innerElem.getStartOffset();
                    }
                }
            }
        }

        return -1;
    }

    /**
     * Gets the end position of the specified comment
     *
     * @param comment the comment whose end position is trying to be found
     * @return the position in the document immediately after the final character of the comments content
     * (newlines included)
     * */
    public int getCommentEnd(Comment comment) {
        Element root = getDefaultRootElement();

        int retVal = -1;

        for (int i = 0; i < root.getElementCount(); i++) {
            Element elem = root.getElement(i);
            if (elem.getElementCount() < 1) continue;
            String transcriptElementType = (String) elem.getElement(0).getAttributes().getAttribute(TranscriptStyleConstants.ATTR_KEY_ELEMENT_TYPE);
            // If transcript element type is comment
            if (transcriptElementType != null && transcriptElementType.equals(TranscriptStyleConstants.ATTR_KEY_COMMENT)) {
                for (int j = 0; j < elem.getElementCount(); j++) {
                    Element innerElem = elem.getElement(j);
                    AttributeSet attrs = innerElem.getAttributes();
                    Comment currentComment = (Comment)attrs.getAttribute(TranscriptStyleConstants.ATTR_KEY_COMMENT);
                    Boolean isLabel = (Boolean)attrs.getAttribute(TranscriptStyleConstants.ATTR_KEY_LABEL);
                    // If correct comment
                    if (isLabel == null && currentComment != null && currentComment == comment) {
                        retVal = Math.max(retVal, innerElem.getEndOffset());
                    }
                }
            }
        }

        return retVal;
    }

    /**
     * Gets the start position of the specified gem
     *
     * @param gem the gem whose start position is trying to be found
     * @return the position in the document at the beginning of the gems content
     * */
    public int getGemStart(Gem gem) {
        Element root = getDefaultRootElement();

        for (int i = 0; i < root.getElementCount(); i++) {
            Element elem = root.getElement(i);
            if (elem.getElementCount() == 0) continue;
            String transcriptElementType = (String) elem.getElement(0).getAttributes().getAttribute(TranscriptStyleConstants.ATTR_KEY_ELEMENT_TYPE);
            // If transcript element type is gem
            if (transcriptElementType != null && transcriptElementType.equals(TranscriptStyleConstants.ATTR_KEY_GEM)) {
                for (int j = 0; j < elem.getElementCount(); j++) {
                    Element innerElem = elem.getElement(j);
                    AttributeSet attrs = innerElem.getAttributes();
                    Gem currentGem = (Gem)attrs.getAttribute(TranscriptStyleConstants.ATTR_KEY_GEM);
                    Boolean isLabel = (Boolean)attrs.getAttribute(TranscriptStyleConstants.ATTR_KEY_LABEL);
                    // If correct tier
                    if (isLabel == null && currentGem != null && currentGem == gem) {
                        return innerElem.getStartOffset();
                    }
                }
            }
        }

        return -1;
    }

    /**
     * Gets the end position of the specified gem
     *
     * @param gem the gem whose end position is trying to be found
     * @return the position in the document immediately after the final character of the gems content
     * (newlines included)
     * */
    public int getGemEnd(Gem gem) {
        Element root = getDefaultRootElement();

        int retVal = -1;

        for (int i = 0; i < root.getElementCount(); i++) {
            Element elem = root.getElement(i);
            if (elem.getElementCount() < 1) continue;
            String transcriptElementType = (String) elem.getElement(0).getAttributes().getAttribute(TranscriptStyleConstants.ATTR_KEY_ELEMENT_TYPE);
            // If transcript element type is gem
            if (transcriptElementType != null && transcriptElementType.equals(TranscriptStyleConstants.ATTR_KEY_GEM)) {
                for (int j = 0; j < elem.getElementCount(); j++) {
                    Element innerElem = elem.getElement(j);
                    AttributeSet attrs = innerElem.getAttributes();
                    Gem currentGem = (Gem) attrs.getAttribute(TranscriptStyleConstants.ATTR_KEY_GEM);
                    Boolean isLabel = (Boolean)attrs.getAttribute(TranscriptStyleConstants.ATTR_KEY_LABEL);
                    // If correct tier name
                    if (isLabel == null && currentGem != null && currentGem == gem) {
                        retVal = Math.max(retVal, innerElem.getEndOffset());
                    }
                }
            }
        }

        return retVal;
    }

    /**
     * Gets the start position of the specified "generic tier"
     *
     * @param genericTier a reference to the generic tier whose start position is trying to be found
     * @return the position in the document at the beginning of the generic tiers content
     * */
    public int getGenericStart(Tier<?> genericTier) {
        Element root = getDefaultRootElement();

        for (int i = 0; i < root.getElementCount(); i++) {
            Element elem = root.getElement(i);
            if (elem.getElementCount() == 0) continue;
            String transcriptElementType = (String) elem.getElement(0).getAttributes().getAttribute(TranscriptStyleConstants.ATTR_KEY_ELEMENT_TYPE);
            // If transcript element type is tierData
            if (transcriptElementType != null && transcriptElementType.equals(TranscriptStyleConstants.ATTR_KEY_GENERIC)) {
                for (int j = 0; j < elem.getElementCount(); j++) {
                    Element innerElem = elem.getElement(j);
                    AttributeSet attrs = innerElem.getAttributes();
                    Tier<?> currentGenericTier = (Tier<?>) attrs.getAttribute(TranscriptStyleConstants.ATTR_KEY_GENERIC);
                    Boolean isLabel = (Boolean)attrs.getAttribute(TranscriptStyleConstants.ATTR_KEY_LABEL);
                    // If correct tierData
                    if (isLabel == null && currentGenericTier != null && currentGenericTier == genericTier) {
                        return innerElem.getStartOffset();
                    }
                }
            }
        }

        return -1;
    }

    /**
     * Gets the start position of the "generic tier" with the specified name
     *
     * @param genericTierName the name of the generic tier whose start position is trying to be found
     * @return the position in the document at the beginning of the generic tiers content
     * */
    public int getGenericStart(String genericTierName) {
        Element root = getDefaultRootElement();

        for (int i = 0; i < root.getElementCount(); i++) {
            Element elem = root.getElement(i);
            if (elem.getElementCount() == 0) continue;
            String transcriptElementType = (String) elem.getElement(0).getAttributes().getAttribute(TranscriptStyleConstants.ATTR_KEY_ELEMENT_TYPE);
            // If transcript element type is generic
            if (transcriptElementType != null && transcriptElementType.equals(TranscriptStyleConstants.ATTR_KEY_GENERIC)) {
                for (int j = 0; j < elem.getElementCount(); j++) {
                    Element innerElem = elem.getElement(j);
                    AttributeSet attrs = innerElem.getAttributes();
                    Tier<?> currentGenericTier = (Tier<?>) attrs.getAttribute(TranscriptStyleConstants.ATTR_KEY_GENERIC);
                    Boolean isLabel = (Boolean)attrs.getAttribute(TranscriptStyleConstants.ATTR_KEY_LABEL);
                    // If correct tier name
                    if (isLabel == null && currentGenericTier != null && currentGenericTier.getName().equals(genericTierName)) {
                        return innerElem.getStartOffset();
                    }
                }
            }
        }

        return -1;
    }

    /**
     * Gets the end position of the specified "generic tier"
     *
     * @param genericTier the generic tier whose end position is trying to be found
     * @return the position in the document immediately after the final character of the generic tiers content
     * (newlines included)
     * */
    public int getGenericEnd(Tier<?> genericTier) {
        int retVal = -1;

        Element root = getDefaultRootElement();

        for (int i = 0; i < root.getElementCount(); i++) {
            Element elem = root.getElement(i);
            if (elem.getElementCount() < 1) continue;
            String transcriptElementType = (String) elem.getElement(0).getAttributes().getAttribute(TranscriptStyleConstants.ATTR_KEY_ELEMENT_TYPE);
            // If transcript element type is tierData
            if (transcriptElementType != null && transcriptElementType.equals(TranscriptStyleConstants.ATTR_KEY_GENERIC)) {
                for (int j = 0; j < elem.getElementCount(); j++) {
                    Element innerElem = elem.getElement(j);
                    AttributeSet attrs = innerElem.getAttributes();
                    Tier<?> currentGenericTier = (Tier<?>) attrs.getAttribute(TranscriptStyleConstants.ATTR_KEY_GENERIC);
                    Boolean isLabel = (Boolean)attrs.getAttribute(TranscriptStyleConstants.ATTR_KEY_LABEL);
                    // If correct tierData
                    if (isLabel == null && currentGenericTier != null && currentGenericTier.toString().equals(genericTier.toString())) {
                        retVal = Math.max(retVal, innerElem.getEndOffset());
                    }
                }
            }
        }

        return retVal;
    }

    /**
     * Gets the end position of the "generic tier" with the specified name
     *
     * @param genericTierName the name of the generic tier whose end position is trying to be found
     * @return the position in the document immediately after the final character of the generic tiers content
     * (newlines included)
     * */
    public int getGenericEnd(String genericTierName) {
        int retVal = -1;

        Element root = getDefaultRootElement();

        for (int i = 0; i < root.getElementCount(); i++) {
            Element elem = root.getElement(i);
            if (elem.getElementCount() < 1) continue;
            String transcriptElementType = (String) elem.getElement(0).getAttributes().getAttribute(TranscriptStyleConstants.ATTR_KEY_ELEMENT_TYPE);
            // If transcript element type is tierData
            if (transcriptElementType != null && transcriptElementType.equals(TranscriptStyleConstants.ATTR_KEY_GENERIC)) {
                for (int j = 0; j < elem.getElementCount(); j++) {
                    Element innerElem = elem.getElement(j);
                    AttributeSet attrs = innerElem.getAttributes();
                    Tier<?> currentGenericTier = (Tier<?>) attrs.getAttribute(TranscriptStyleConstants.ATTR_KEY_GENERIC);
                    Boolean isLabel = (Boolean)attrs.getAttribute(TranscriptStyleConstants.ATTR_KEY_LABEL);
                    // If correct tierData
                    if (isLabel == null && currentGenericTier != null && currentGenericTier.getName().equals(genericTierName)) {
                        retVal = Math.max(retVal, innerElem.getEndOffset());
                    }
                }
            }
        }

        return retVal;
    }

    /**
     * Gets a tuple containing the start and end of a specified segment assuming the specified position is somewhere
     * between the two
     *
     * @param segment the segment that the bounds are being calculated for
     * @param includedPos a position included in the bounds of the segment
     * @return a tuple containing the start and end of the segment
     * */
    public Tuple<Integer, Integer> getSegmentBounds(MediaSegment segment, int includedPos) {
        Element root = getDefaultRootElement();

        int indexInSegment = -1;

        for (int i = 0; i < root.getElementCount(); i++) {
            Element elem = root.getElement(i);
            if (elem.getElementCount() < 1) continue;
            String transcriptElementType = (String) elem.getElement(0).getAttributes().getAttribute(TranscriptStyleConstants.ATTR_KEY_ELEMENT_TYPE);
            // If correct record index
            if (transcriptElementType != null && transcriptElementType.equals(TranscriptStyleConstants.ATTR_KEY_RECORD)) {
                for (int j = 0; j < elem.getElementCount(); j++) {
                    Element innerElem = elem.getElement(j);
                    MediaSegment elemSegment = (MediaSegment) innerElem.getAttributes().getAttribute(TranscriptStyleConstants.ATTR_KEY_MEDIA_SEGMENT);
                    boolean includedPosInElem = includedPos < innerElem.getEndOffset() && includedPos >= innerElem.getStartOffset();
                    if (elemSegment != null && elemSegment == segment && includedPosInElem) {
                        indexInSegment = innerElem.getStartOffset();
                        i = root.getElementCount();;
                        j = elem.getElementCount();
                    }
                }
            }
        }

        Tuple<Integer, Integer> retVal = new Tuple<>(-1, -1);

        if (indexInSegment == -1) return retVal;

        int segmentStart = indexInSegment;

        AttributeSet attrs = getCharacterElement(segmentStart).getAttributes();
        while (attrs.getAttribute(TranscriptStyleConstants.ATTR_KEY_MEDIA_SEGMENT) != null) {
            segmentStart--;
            attrs = getCharacterElement(segmentStart).getAttributes();
        }
        retVal.setObj1(segmentStart + 1);

        int segmentEnd = indexInSegment;
        attrs = getCharacterElement(segmentEnd).getAttributes();
        while (attrs.getAttribute(TranscriptStyleConstants.ATTR_KEY_MEDIA_SEGMENT) != null) {
            segmentEnd++;
            attrs = getCharacterElement(segmentEnd).getAttributes();
        }
        retVal.setObj2(segmentEnd - 1);

        return retVal;
    }

    // endregion Get Record/Tier Start/End


    // region Record Changes

    /**
     * Adds a record to the document
     *
     * @param addedRecord the record that gets added
     * */
    public void addRecord(Record addedRecord, int elementIndex) {
        try {
            appendBatchEndStart();
            AttributeSet attrs = writeRecord(addedRecord, session.getTranscript(), session.getTierView());
            appendBatchLineFeed(attrs);
            int nextElementStart = getLength();

            Transcript transcript = getSession().getTranscript();
            Transcript.Element nextElement = transcript.getElementAt(elementIndex + 1);
            if (nextElement.isRecord()) {
                nextElementStart = getRecordStart(nextElement.asRecord());
            }
            else if (nextElement.isComment()) {
                nextElementStart = getCommentStart(nextElement.asComment());
            }
            else if (nextElement.isGem()) {
                nextElementStart = getGemStart(nextElement.asGem());
            }

            processBatchUpdates(nextElementStart);
            setGlobalParagraphAttributes();
        }
        catch (BadLocationException e) {
            LogUtil.severe(e);
        }
    }

    /**
     * Removes a record from the document
     *
     * @param removedRecord the record that gets removed
     * */
    public void deleteRecord(Record removedRecord) {
        try {
            int start = getRecordStart(removedRecord, true);
            int end = getRecordEnd(removedRecord);

            bypassDocumentFilter = true;
            remove(start, end - start);
        }
        catch (BadLocationException e) {
            LogUtil.severe(e);
        }
    }

    /**
     * Moves a record in the document
     *
     * @param oldRecordIndex the existing index of the record
     * @param oldElementIndex the existing element index of the record
     * @param newRecordIndex the new index of the record
     * @param newElementIndex the new element index of the record
     * */
    public void moveRecord(int oldRecordIndex, int newRecordIndex, int oldElementIndex, int newElementIndex) {
        try {
            Transcript transcript = session.getTranscript();
            var tierView = session.getTierView();
            AttributeSet newLineAttrs;

            int start = getRecordStart(Math.min(oldRecordIndex, newRecordIndex));
            int end = getRecordEnd(Math.max(oldRecordIndex, newRecordIndex));

            bypassDocumentFilter = true;
            remove(start, end - start);

            appendBatchEndStart();

            int startElementIndex = Math.min(oldElementIndex, newElementIndex);
            int endElementIndex = Math.max(oldElementIndex, newElementIndex);

            for (int i = startElementIndex; i < endElementIndex + 1; i++) {
                Transcript.Element elem = transcript.getElementAt(i);
                if (elem.isRecord()) {
                    newLineAttrs = writeRecord(elem.asRecord(), transcript, tierView);
                }
                else if (elem.isComment()) {
                    newLineAttrs = writeComment(elem.asComment());
                }
                else {
                    newLineAttrs = writeGem(elem.asGem());
                }

                appendBatchLineFeed(newLineAttrs);
            }

            processBatchUpdates(start);
            setGlobalParagraphAttributes();
        }
        catch (BadLocationException e) {
            LogUtil.severe(e);
        }
    }

    /**
     * Updates the speaker on the separator / record header
     * */
    public void onChangeSpeaker(Record record) {
        try {
            int recordIndex = session.getRecordPosition(record);
            int start = getRecordStart(recordIndex, true);

            var tierView = session.getTierView();
            String firstVisibleTierName = tierView
                .stream()
                .filter(item -> item.isVisible())
                .findFirst()
                .get()
                .getTierName();
            int end = getTierStart(recordIndex, firstVisibleTierName) - (labelColumnWidth + 2);

            bypassDocumentFilter = true;
            remove(start, end - start);

            AttributeSet recordAttrs = getRecordAttributes(recordIndex);

            SimpleAttributeSet tierAttrs = getTierAttributes(record.getSegmentTier());
            tierAttrs.addAttributes(getSeparatorAttributes());
            tierAttrs.addAttributes(recordAttrs);

            SimpleAttributeSet labelAttrs = getTierLabelAttributes(record.getSegmentTier());
            labelAttrs.addAttributes(getSeparatorAttributes());
            labelAttrs.addAttributes(recordAttrs);

            appendBatchEndStart();

            Participant speaker = record.getSpeaker() == null ? Participant.UNKNOWN : record.getSpeaker();
            appendBatchString(formatLabelText(speaker.toString()) + "  ", labelAttrs);

            MediaSegment segment = record.getMediaSegment();

            tierAttrs.addAttributes(getStandardFontAttributes());
            appendFormattedSegment(segment, tierAttrs);

            processBatchUpdates(start);
            setGlobalParagraphAttributes();
        }
        catch (BadLocationException e) {
            LogUtil.severe(e);
        }
    }

    /**
     * Updates the data of the specified tier
     *
     * @param tier the tier whose data gets updated
     * */
    public void onTierDataChanged(Tier<?> tier) {
        if (tier.getName().equals(SystemTierType.TargetSyllables.getName()) || tier.getName().equals(SystemTierType.ActualSyllables.getName())) return;

        if (tier.getDeclaredType().equals(PhoneAlignment.class)) return;

        try {
            int start = getTierStart(tier);
            int recordIndex = getRecordIndex(start);
            start -= labelColumnWidth + 2;
            int end = getTierEnd(tier);

            bypassDocumentFilter = true;
            remove(start, end - start);

            var tierView = session.getTierView();
            TierViewItem tierViewItem = tierView.stream().filter(item -> item.getTierName().equals(tier.getName())).findFirst().orElse(null);

            appendBatchEndStart();

            SimpleAttributeSet attrs = insertTier(recordIndex, tier, tierViewItem, getRecordAttributes(recordIndex));

            appendBatchLineFeed(attrs);

            processBatchUpdates(start);
            setGlobalParagraphAttributes();
        }
        catch (BadLocationException e) {
            LogUtil.severe(e);
        }
    }

    // endregion Record Changes

    /**
     * Gets the character at the specified position in the document
     *
     * @param pos the position to get the character from
     * @return the character at the specified position
     * */
    public Character getCharAtPos(int pos) {
        try {
            return getText(pos, 1).charAt(0);
        }
        catch (BadLocationException e) {
            LogUtil.warning(e);
            return null;
        }
    }

    /**
     * Formats the provided text to be used as a label by appending the appropriate number of
     * spaces at the beginning or ellipses at the end
     *
     * @param labelText the text to be formatted
     * @return the formatted text
     * */
    public String formatLabelText(String labelText) {
        int labelTextLen = labelText.length();
        if (labelTextLen < labelColumnWidth) {
            return " ".repeat((labelColumnWidth - labelTextLen)) + labelText;
        }
        else if (labelTextLen > labelColumnWidth) {
            String remaining = labelText.substring(labelTextLen - labelColumnWidth + 3, labelTextLen);
            return "..." + remaining;
        }
        return labelText;
    }

    /**
     * Empties the document and repopulates it with up-to-date data
     * */
    public void reload() {
        try {
            bypassDocumentFilter = true;
            remove(0, getLength());
            populate();
        }
        catch (BadLocationException e) {
            LogUtil.severe(e);
        }
    }

    /**
     * Gets the index of the record containing the specified position
     *
     * @param position a position in the document in a record
     * @return the index of the record
     * */
    public int getRecordIndex(int position) {
        AttributeSet attributes = getCharacterElement(position).getAttributes();
        Record record = (Record) attributes.getAttribute(TranscriptStyleConstants.ATTR_KEY_RECORD);
        if (record == null) return -1;
        return session.getRecordPosition(record);
    }

    /**
     * Gets the element index of the record containing the specified position
     *
     * @param position a position in the document in a record
     * @return the element index of the record
     * */
    public int getRecordElementIndex(int position) {
        AttributeSet attributes = getCharacterElement(position).getAttributes();
        Record record = (Record) attributes.getAttribute(TranscriptStyleConstants.ATTR_KEY_RECORD);
        if (record == null) return -1;
        return session.getRecordElementIndex(record);
    }

    /**
     * Gets the tier containing the specified position
     *
     * @param position a position in the document in a record
     * @return a reference to the tier
     * */
    public Tier<?> getTier(int position) {
        AttributeSet attributes = getCharacterElement(position).getAttributes();
        return (Tier<?>) attributes.getAttribute(TranscriptStyleConstants.ATTR_KEY_TIER);
    }

    /**
     * Appends a formatted representation of the provided segment to the batch
     *
     * @param segment the segment that will be appended
     * @param additionalAttrs any additional attributes to be added to the segment (none added if {@code null})
     * */
    private void appendFormattedSegment(MediaSegment segment, AttributeSet additionalAttrs) {
        appendFormattedSegment(segment, additionalAttrs, MediaTimeFormatStyle.PADDED_MINUTES_AND_SECONDS);
    }
    /**
     * Appends a formatted representation of the provided segment in the provided style to the batch
     *
     * @param segment the segment that will be appended
     * @param additionalAttrs any additional attributes to be added to the segment (none added if {@code null})
     * @param style the style to format the times of the segment
     * */
    private void appendFormattedSegment(MediaSegment segment, AttributeSet additionalAttrs, MediaTimeFormatStyle style) {

        var formatter = new MediaSegmentFormatter(style);
        String value = formatter.format(segment);

        var segmentTimeAttrs = getSegmentTimeAttributes(segment);
        var segmentDashAttrs = getSegmentDashAttributes(segment);
        segmentTimeAttrs.addAttribute(TranscriptStyleConstants.ATTR_KEY_NOT_EDITABLE, true);
        segmentDashAttrs.addAttribute(TranscriptStyleConstants.ATTR_KEY_NOT_EDITABLE, true);
        if (additionalAttrs != null) {
            segmentTimeAttrs.addAttributes(additionalAttrs);
            segmentDashAttrs.addAttributes(additionalAttrs);
        }

        appendBatchString("•", segmentDashAttrs);

        appendBatchString(value, segmentTimeAttrs);

        appendBatchString("•", segmentDashAttrs);
    }

    /**
     * Appends the specified internal media to the batch
     *
     * @param internalMedia the internal media that gets appended
     * @param additionalAttrs any additional attributes to be added to the segment (none added if {@code null})
     * */
    public void appendFormattedInternalMedia(InternalMedia internalMedia, AttributeSet additionalAttrs) {
        MediaSegment segment = sessionFactory.createMediaSegment();
        segment.setSegment(internalMedia.getStartTime(), internalMedia.getEndTime(), MediaUnit.Second);
        appendFormattedSegment(segment, additionalAttrs, MediaTimeFormatStyle.MINUTES_AND_SECONDS);
    }

    /**
     * Sets the global paragraph attributes to the entirety of the document
     * */
    public void setGlobalParagraphAttributes() {
        SimpleAttributeSet paragraphAttrs = new SimpleAttributeSet();
        StyleConstants.setLineSpacing(paragraphAttrs, getLineSpacing());
        StyleConstants.setForeground(paragraphAttrs, UIManager.getColor(TranscriptEditorUIProps.FOREGROUND));

        // Soft wrap
        Font font = FontPreferences.getMonospaceFont().deriveFont(14.0f);
        String builder = " ".repeat(Math.max(0, labelColumnWidth + 2));
        FontMetrics fm = new JLabel().getFontMetrics(font);
        int indent = fm.stringWidth(builder);
        int rightInset =  fm.stringWidth(" ");
        StyleConstants.setLeftIndent(paragraphAttrs, indent);
        StyleConstants.setRightIndent(paragraphAttrs, rightInset);
        StyleConstants.setFirstLineIndent(paragraphAttrs, -indent);

        setParagraphAttributes(0, getLength(), paragraphAttrs, false);
    }

    /**
     * Inserts a given tier at the end of the batch
     *
     * @param recordIndex the index of the record the tier is in
     * @param tier the tier that will be inserted
     * @param tierViewItem a reference to a {@link TierViewItem} used to get font info if any is present
     * @param recordAttrs an attribute set containing attributes for the containing record to be added to the tier
     *                    attributes (none will be added if {@code null})
     * @return a mutable attribute set containing the attributes of the last character of the tier to add a
     * newline after if need be
     * */
    private SimpleAttributeSet insertTier(int recordIndex, Tier<?> tier, TierViewItem tierViewItem, AttributeSet recordAttrs) {
        String tierName = tier.getName();
        Record record = session.getRecord(recordIndex);

        SimpleAttributeSet tierAttrs = getTierAttributes(tier, tierViewItem);
        if (recordAttrs != null) {
            tierAttrs.addAttributes(recordAttrs);
        }

        SimpleAttributeSet labelAttrs = getTierLabelAttributes(tier);
        if (recordAttrs != null) {
            labelAttrs.addAttributes(recordAttrs);
        }

        String labelText = tierName;
        if (chatTierNamesShown) {
            SystemTierType systemTierType = SystemTierType.tierFromString(tierName);
            if (systemTierType != null) {
                if (systemTierType == SystemTierType.Orthography) {
                    labelText = "*" + record.getSpeaker().getId();
                }
                else {
                    labelText = systemTierType.getChatTierName();
                }
            }
            else {
                UserTierType userTierType = UserTierType.fromPhonTierName(tierName);
                if (userTierType != null) {
                    labelText = userTierType.getChatTierName();
                }
                else {
                    labelText = UserTierType.determineCHATTierName(session, tierName);
                }
            }
        }

        if (labelText.length() < labelColumnWidth) {
            appendBatchString(" ".repeat((labelColumnWidth - labelText.length())), labelAttrs);
        }
        else {
            labelText = formatLabelText(labelText);
        }

        labelAttrs.addAttribute(TranscriptStyleConstants.ATTR_KEY_CLICKABLE, true);
        appendBatchString(labelText, labelAttrs);

        labelAttrs.removeAttribute(TranscriptStyleConstants.ATTR_KEY_CLICKABLE);
        appendBatchString(": ", labelAttrs);

        Class<?> tierType = tier.getDeclaredType();

        if(!tier.hasValue()) {
            appendBatchString("", tierAttrs);
        }
        else if (tier.isUnvalidated()) {
            appendBatchString(tier.getUnvalidatedValue().getValue(), tierAttrs);
        }
        else {
            if (tierType.equals(IPATranscript.class)) {
                Tier<IPATranscript> ipaTier = (Tier<IPATranscript>)tier;
                List<IPATranscript> words = (ipaTier).getValue().words();
                for (int i = 0; i < words.size(); i++) {
                    var word = words.get(i);
                    SimpleAttributeSet attrs;
                    if (word.matches("\\P")) {
                        // Pause
                        attrs = getIPAPauseAttributes(ipaTier);
                    } else {
                        // Word
                        attrs = getIPAWordAttributes(ipaTier);
                    }
                    attrs.addAttributes(tierAttrs);
                    String content = word.toString();
                    appendBatchString(content, attrs);

                    if (i < words.size() - 1) {
                        appendBatchString(" ", tierAttrs);
                    }
                }
            }
            else if (tierType.equals(MediaSegment.class)) {
                MediaSegment segment = record.getMediaSegment();
                appendFormattedSegment(segment, tierAttrs);
            }
            else if (tierType.equals(Orthography.class)) {
                Tier<Orthography> orthographyTier = (Tier<Orthography>) tier;
                orthographyTier.getValue().accept(new TranscriptOrthographyVisitors.KeywordVisitor(this, tierAttrs));
            }
            else if (tierType.equals(MorTierData.class)) {
                Tier<MorTierData> morTier = (Tier<MorTierData>) tier;
                MorTierData mors = morTier.getValue();

                for (int i = 0; i < mors.size(); i++) {
                    Mor mor = mors.get(i);
                    appendBatchString(mor.toString(), tierAttrs);
                    if (i < mors.size() - 1) {
                        appendBatchString(" ", tierAttrs);
                    }
                }
            }
            else if (tierType.equals(GraspTierData.class)) {
                Tier<GraspTierData> graspTier = (Tier<GraspTierData>) tier;
                GraspTierData grasps = graspTier.getValue();

                for (int i = 0; i < grasps.size(); i++) {
                    Grasp grasp = grasps.get(i);
                    appendBatchString(grasp.toString(), tierAttrs);
                    if (i < grasps.size() - 1) {
                        appendBatchString(" ", tierAttrs);
                    }
                }
            }
            else if (tierType.equals(TierData.class))  {
                Tier<TierData> userTier = (Tier<TierData>) tier;
                TierData tierData = userTier.getValue();
                if (tierData != null) {
                    for (int i = 0; i < tierData.length(); i++) {
                        TierElement elem = tierData.elementAt(i);
                        String text = null;
                        SimpleAttributeSet attrs;
                        if (elem instanceof TierString tierString) {
                            text = tierString.text();
                            attrs = getTierStringAttributes();
                        }
                        else if (elem instanceof TierComment comment) {
                            text = comment.toString();
                            attrs = getTierCommentAttributes();
                        }
                        else if (elem instanceof TierInternalMedia internalMedia) {
                            attrs = getTierInternalMediaAttributes();
                            appendFormattedInternalMedia(internalMedia.getInternalMedia(), attrs);
                        }
                        else if (elem instanceof TierLink link) {
                            text = link.toString();
                            attrs = getTierLinkAttributes();
                        }
                        else {
                            throw new RuntimeException("Invalid type");
                        }

                        attrs.addAttributes(tierAttrs);

                        if (text != null) appendBatchString(text, attrs);

                        if (i < tierData.length() - 1) {
                            appendBatchString(" ", tierAttrs);
                        }
                    }
                }
            }
        }

        tierAttrs.removeAttribute(TranscriptStyleConstants.ATTR_KEY_COMPONENT_FACTORY);
        return tierAttrs;
    }

    /**
     * Sets whether a {@link TierViewItem} with the provided name is locked
     *
     * @param tierName the name of the tier that will be locked or unlocked
     * @param locked whether the tier will be locked or unlocked
     * */
    public void setTierItemViewLocked(String tierName, boolean locked) {
        var currentTierVIew = session.getTierView();
        List<TierViewItem> newTierView = new ArrayList<>();
        for (TierViewItem oldItem : currentTierVIew) {
            if (oldItem.getTierName().equals(tierName)) {
                final TierViewItem newItem = sessionFactory.createTierViewItem(
                    oldItem.getTierName(),
                    oldItem.isVisible(),
                    oldItem.getTierFont(),
                    locked
                );
                newTierView.add(newItem);
            }
            else {
                newTierView.add(oldItem);
            }
        }
        session.setTierView(newTierView);
    }

    /**
     * Gets the offset from the preceding label of a given position in the document
     *
     * @param pos the position that the offset will come from
     * */
    public int getOffsetInContent(int pos) {
        Element elem = getCharacterElement(pos);
        String transcriptElementType = (String) elem.getAttributes().getAttribute(TranscriptStyleConstants.ATTR_KEY_ELEMENT_TYPE);
        if (transcriptElementType == null) return -1;

        switch (transcriptElementType) {
            case TranscriptStyleConstants.ATTR_KEY_RECORD -> {
                Tier<?> tier = (Tier<?>) elem.getAttributes().getAttribute(TranscriptStyleConstants.ATTR_KEY_TIER);
                if (tier == null) return -1;
                int recordStartPos = getTierStart(tier);
                return pos - recordStartPos;
            }
            case TranscriptStyleConstants.ATTR_KEY_COMMENT -> {
                Comment comment = (Comment) elem.getAttributes().getAttribute(TranscriptStyleConstants.ATTR_KEY_COMMENT);
                if (comment == null) return -1;
                int commentStartPos = getCommentStart(comment);
                return pos - commentStartPos;
            }
            case TranscriptStyleConstants.ATTR_KEY_GEM -> {
                Gem gem = (Gem) elem.getAttributes().getAttribute(TranscriptStyleConstants.ATTR_KEY_GEM);
                if (gem == null) return -1;
                return pos - getGemStart(gem);
            }
            case TranscriptStyleConstants.ATTR_KEY_GENERIC -> {
                Tier<?> genericTier = (Tier<?>) elem.getAttributes().getAttribute(TranscriptStyleConstants.ATTR_KEY_GENERIC);
                if (genericTier == null) return -1;
                return pos - getGenericStart(genericTier);
            }
            default -> {
                return -1;
            }
        }
    }

    /**
     * Gets the attributes of the last {@link javax.swing.text.DefaultStyledDocument.ElementSpec} in a provided list
     *
     * @param elementSpecs the list of elementspecs
     * @return the attributes of the last elementspec
     * */
    public AttributeSet getTrailingAttributes(List<DefaultStyledDocument.ElementSpec> elementSpecs) {
        if (elementSpecs.isEmpty()) return new SimpleAttributeSet();
        AttributeSet attrs = elementSpecs.get(elementSpecs.size() - 1).getAttributes();
        return attrs == null ? new SimpleAttributeSet() : attrs;
    }

    // region Document Properties

    /**
     * Gets the object associated with a given key from the document properties
     *
     * @param key the key to get the object with the object
     * @return the object associated with the key ({@code null} if no object is present)
     * */
    public Object getDocumentProperty(String key) {
        return getProperty(key);
    }

    /**
     * Gets the object associated with a given key from the document properties
     * and returns a given default value if none is present
     *
     * @param key the key to get the object with the object
     * @param defaultValue the object to be returned if there is no object associated with the key
     * @return the object associated with the key or the default if no object is present
     * */
    public Object getDocumentPropertyOrDefault(String key, Object defaultValue) {
        Object value = getProperty(key);
        return value == null ? defaultValue : value;
    }

    /**
     * Adds a given key-value pair to the document properties
     *
     * @param key the kek
     * @param value the value
     * */
    public void putDocumentProperty(String key, Object value) {
        Object existingValue = getProperty(key);
        putProperty(key, value);
        propertyChangeSupport.firePropertyChange(key, existingValue, value);
    }

    /**
     * Adds a property change listener that responds to changes in the document property with the given key / name
     *
     * @param propertyName the name / key of the property that will be listened to
     * @param propertyChangeListener the listener that contains some behavior that will happen when the property changes
     * */
    public void addDocumentPropertyChangeListener(String propertyName, PropertyChangeListener propertyChangeListener) {
        propertyChangeSupport.addPropertyChangeListener(propertyName, propertyChangeListener);
    }

    /**
     * Removes a document property change listener
     *
     * @param propertyChangeListener the listener that was listening for changes in a document property
     * */
    public void removeDocumentPropertyChangeListener(PropertyChangeListener propertyChangeListener) {
        propertyChangeSupport.removePropertyChangeListener(propertyChangeListener);
    }

    // endregion Document Properties

    // region Not Editable Attributes

    /**
     * Checks if the provided attribute set contains any attributes that are currently considered "not editable"
     *
     * @param attrs the attribute set that will be checked
     * @return whether there were any "not editable" attributes
     * */
    public boolean containsNotEditableAttribute(AttributeSet attrs) {
        for (String key : notEditableAttributes) {
            if (attrs.getAttribute(key) != null) return true;
        }
        return false;
    }

    /**
     * Adds the given attribute to the set of "not editable" attributes
     *
     * @param attributeKey the attribute that will be added to the set
     * */
    public void addNotEditableAttribute(String attributeKey) {
        notEditableAttributes.add(attributeKey);
    }
    /**
     * Removes the given attribute from the set of "not editable" attributes
     *
     * @param attributeKey the attribute that will be removed from the set
     * */
    public void removeNotEditableAttribute(String attributeKey) {
        notEditableAttributes.remove(attributeKey);
    }

    // endregion Not Editable Attributes

    /**
     * Populates the document with the appropriate content
     * */
    private void populate() throws BadLocationException {
        Transcript transcript = session.getTranscript();
        var tierView = session.getTierView();

        List<ElementSpec> additionalInsertions = new ArrayList<>();
        for (var hook : insertionHooks) {
            additionalInsertions.addAll(hook.startSession());
        }
        batch.addAll(additionalInsertions);
        additionalInsertions.clear();

        SimpleAttributeSet newLineAttrs;

        for (var hook : insertionHooks) {
            additionalInsertions.addAll(hook.startTranscript());
        }
        batch.addAll(additionalInsertions);
        additionalInsertions.clear();

        // Single record
        if (singleRecordView) {
            Record record = session.getRecord(singleRecordIndex);
            int recordTranscriptElementIndex = transcript.getElementIndex(record);

            int includedElementIndex;
            if (singleRecordIndex == 0) {
                includedElementIndex = 0;
            }
            else {
                includedElementIndex = transcript.getElementIndex(transcript.getRecord(singleRecordIndex-1)) + 1;
            }
            while (includedElementIndex < recordTranscriptElementIndex) {
                Transcript.Element previousElement = transcript.getElementAt(includedElementIndex);
                if (previousElement.isRecord()) {
                    break;
                }

                if (previousElement.isComment()) {
                    newLineAttrs = writeComment(previousElement.asComment());
                }
                else {
                    newLineAttrs = writeGem(previousElement.asGem());
                }

                includedElementIndex++;

                appendBatchLineFeed(newLineAttrs);
            }

            newLineAttrs = writeRecord(record, transcript, tierView);

            int nextElementIndex = recordTranscriptElementIndex + 1;
            int transcriptElementCount = transcript.getNumberOfElements();
            while (nextElementIndex < transcriptElementCount) {
                Transcript.Element nextElement = transcript.getElementAt(nextElementIndex);
                appendBatchLineFeed(newLineAttrs);
                if (nextElement.isRecord()) {
                    break;
                }

                if (nextElement.isComment()) {
                    newLineAttrs = writeComment(nextElement.asComment());
                }
                else {
                    newLineAttrs = writeGem(nextElement.asGem());
                }

                nextElementIndex++;
            }
        }
        // All records
        else {
            for (int i = 0; i < transcript.getNumberOfElements(); i++) {
                Transcript.Element elem = transcript.getElementAt(i);
                if (elem.isRecord()) {
                    newLineAttrs = writeRecord(elem.asRecord(), transcript, tierView);
                }
                else if (elem.isComment()) {
                    newLineAttrs = writeComment(elem.asComment());
                }
                else {
                    newLineAttrs = writeGem(elem.asGem());
                }

                appendBatchLineFeed(newLineAttrs);
            }
        }

        for (var hook : insertionHooks) {
            additionalInsertions.addAll(hook.endTranscript());
        }
        batch.addAll(additionalInsertions);
        additionalInsertions.clear();

        processBatchUpdates(0);
        setGlobalParagraphAttributes();

        for (var hook : getInsertionHooks()) {
            additionalInsertions.addAll(hook.endSession());
        }
        if (!additionalInsertions.isEmpty()) {
            batch.addAll(additionalInsertions);
        }
    }

    /**
     * The default document filter for the {@link TranscriptDocument}
     * */
    public static class TranscriptDocumentFilter extends DocumentFilter {
        private final TranscriptDocument doc;

        /**
         * The constructor
         *
         * @param doc a reference to the {@link TranscriptDocument}
         * */
        public TranscriptDocumentFilter(TranscriptDocument doc) {
            this.doc = doc;
        }

        @Override
        public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet _attrs) throws BadLocationException {

            // For some reason attrs gets the attributes from the previous character, so this fixes that
            SimpleAttributeSet attrs = new SimpleAttributeSet();
            attrs.addAttributes(doc.getCharacterElement(offset).getAttributes());

            // Labels and stuff
            if (doc.containsNotEditableAttribute(attrs)) return;

            // Locked tiers
            Tier<?> tier = (Tier<?>)attrs.getAttribute(TranscriptStyleConstants.ATTR_KEY_TIER);
            if (tier != null) {
                String tierName = tier.getName();
                var tierViewItem = doc
                    .getSession()
                    .getTierView()
                    .stream()
                    .filter(item -> item.getTierName().equals(tierName))
                    .findFirst();
                if (tierViewItem.isPresent() && tierViewItem.get().isTierLocked()) {
                    return;
                }
            }
            super.replace(fb, offset, length, text, attrs);
        }

        @Override
        public void remove(FilterBypass fb, int offset, int length) throws BadLocationException {

            if (!doc.isBypassDocumentFilter()) {
                var attrs = doc.getCharacterElement(offset).getAttributes();
                if (doc.containsNotEditableAttribute(attrs)) return;
                if (attrs.getAttribute(TranscriptStyleConstants.ATTR_KEY_SYLLABIFICATION) != null) return;
            }

            doc.setBypassDocumentFilter(false);
            super.remove(fb, offset, length);
        }
    }

    /**
     * A wrapper record for a list of {@link Language}
     * */
    public record Languages(List<Language> languageList) {}

    // region TranscriptDocumentInsertionHook

    /**
     * Insertion hooks, these may be added dynamically but will also be loaded using
     * from registered IPluginExtensionPoints
     */
    private final List<InsertionHook> insertionHooks = new ArrayList<>();

    private void loadRegisteredInsertionHooks() {
        for(var hookExtPt: PluginManager.getInstance().getExtensionPoints(InsertionHook.class)) {
            final InsertionHook hook = hookExtPt.getFactory().createObject();
            addInsertionHook(hook);
        }
    }

    public void addInsertionHook(InsertionHook hook) {
        this.insertionHooks.add(hook);
    }

    public boolean removeInsertionHook(InsertionHook hook) {
        return this.insertionHooks.remove(hook);
    }

    public List<InsertionHook> getInsertionHooks() {
        return Collections.unmodifiableList(this.insertionHooks);
    }

    // endregion TranscriptDocumentInsertionHook

    // region IExtendable
    @Override
    public Set<Class<?>> getExtensions() {
        return extensionSupport.getExtensions();
    }

    @Override
    public <T> T getExtension(Class<T> cap) {
        return extensionSupport.getExtension(cap);
    }

    @Override
    public <T> T putExtension(Class<T> cap, T impl) {
        return extensionSupport.putExtension(cap, impl);
    }

    @Override
    public <T> T removeExtension(Class<T> cap) {
        return extensionSupport.removeExtension(cap);
    }

    // endregion
}