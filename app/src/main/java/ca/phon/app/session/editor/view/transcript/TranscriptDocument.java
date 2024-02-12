package ca.phon.app.session.editor.view.transcript;

import ca.phon.app.log.LogUtil;
import ca.phon.app.session.editor.EditorEventManager;
import ca.phon.app.session.editor.undo.SessionEditUndoSupport;
import ca.phon.extensions.ExtensionSupport;
import ca.phon.extensions.IExtendable;
import ca.phon.plugin.PluginManager;
import ca.phon.session.Record;
import ca.phon.session.*;
import ca.phon.util.Language;
import ca.phon.util.Range;

import javax.swing.*;
import javax.swing.text.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.List;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Text document for a {@link Session} that displays the transcript including all tiers, comments, and gems.
 */
public class TranscriptDocument extends DefaultStyledDocument implements IExtendable {

    /**
     * Session factory for creating new session data objects
     */
    private final SessionFactory sessionFactory;

    /**
     * Document prop support
     */
    private final PropertyChangeSupport propertyChangeSupport;

    /**
     * The set of "not editable" attributes
     */
    private final Set<String> notEditableAttributes;

    /**
     * extension support
     */
    private final ExtensionSupport extensionSupport = new ExtensionSupport(TranscriptDocument.class, this);

    /**
     * Insertion hooks, these may be added dynamically but will also be loaded using
     * from registered IPluginExtensionPoints
     */
    private final List<InsertionHook> insertionHooks = new ArrayList<>();

    /**
     * A reference to the loaded session
     */
    private Session session;

    /**
     * Whether the document is in single-record mode
     */
    private boolean singleRecordView = false;

    /**
     * The index of the record that gets displayed if the document is in single-record mode
     */
    private int singleRecordIndex = 0;

    /**
     * Whether the next removal from the document will bypass the document filter
     */
    private boolean bypassDocumentFilter = false;

    /**
     * Editor undo support
     */
    private SessionEditUndoSupport undoSupport;

    /**
     * Editor event manager
     */
    private EditorEventManager eventManager;

    /**
     * Whether the tier labels show the chat tier names or the regular tier names
     */
    private boolean chatTierNamesShown = false;
    private TranscriptStyleContext transcriptStyleContext;

    /**
     * Constructor
     */
    public TranscriptDocument() {
        super(new TranscriptStyleContext());
        this.transcriptStyleContext = (TranscriptStyleContext) getAttributeContext();

        sessionFactory = SessionFactory.newFactory();
        setDocumentFilter(new TranscriptDocumentFilter(this));

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
        SwingUtilities.invokeLater(() -> {
            try {
                if (getLength() > 0) {
                    remove(0, getLength());
                }
                populate();
            } catch (BadLocationException e) {
                LogUtil.severe(e);
            }
        });
    }

    public boolean getSingleRecordView() {
        return singleRecordView;
    }

    /**
     * Sets whether the document is in "single-record view" and reloads the document if it has changed
     *
     * @param singleRecordView whether the document will be in single record view
     */
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
     */
    public void setSingleRecordIndex(int singleRecordIndex) {
        this.singleRecordIndex = singleRecordIndex;
        if (singleRecordView) {
            reload();
        }
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

    /**
     * Returns whether the next call of {@code remove()} from the document will bypass the document filter
     *
     * @return whether the remove will be bypassed
     */
    public boolean isBypassDocumentFilter() {
        return bypassDocumentFilter;
    }

    /**
     * Sets whether the next call of {@code remove()} from the document will bypass the document filter
     *
     * @param bypassDocumentFilter whether the remove will be bypassed
     */
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

    public TranscriptStyleContext getTranscriptStyleContext() {
        return transcriptStyleContext;
    }

    // endregion Getters and Setters

    // region Batching
    /**
     * Gets the text value of a given tier for a given transcriber
     *
     * @param tier        the tier that the value will come from
     * @param transcriber the transcriber whose text will be returned
     * @return the text value of a given tier for a given transcriber (or the regular text for the tier if the transcriber is the validator or {@code null})
     */
    public String getTierText(Tier<?> tier, String transcriber) {
        if (tier.isBlind() && transcriber != null && !Transcriber.VALIDATOR.getUsername().equals(transcriber)) {
            if (tier.hasBlindTranscription(transcriber)) {
                if (tier.isBlindTranscriptionUnvalidated(transcriber)) {
                    return tier.getBlindUnvalidatedValue(transcriber).getValue();
                }
                return tier.getBlindTranscription(transcriber).toString();
            }
        } else {
            if (tier.hasValue()) {
                if (tier.isUnvalidated()) {
                    return tier.getUnvalidatedValue().getValue();
                }
                return tier.getValue().toString();
            }
        }

        return "";
    }

    /**
     * Adds the contents of the batch to the document at the specified offset,
     * and sets the global paragraph attributes
     *
     * @param offs  the offset to insert the batch at
     * @param batch the batch containing the batch to insert
     */
    public void processBatchUpdates(int offs, List<ElementSpec> batch) throws BadLocationException {

        // As with insertBatchString, this could be synchronized if
        // there was a chance multiple threads would be in here.
        DefaultStyledDocument.ElementSpec[] inserts = new DefaultStyledDocument.ElementSpec[batch.size()];
        batch.toArray(inserts);

        // Process all the inserts in bulk
        super.insert(offs, inserts);
    }
    // endregion Batching


    // region Transcript <-> Document Positioning
    /**
     * Gets the end position of the specified record
     *
     * @param record the record whose end is trying to be found
     * @return the position in the document immediately after the final character of the records content
     * (newlines included)
     */
    public int getRecordEnd(Record record) {
        return getRecordRange(record).getEnd();
    }

    /**
     * Gets the start of the record with the specified index
     *
     * @param recordIndex the index of the record
     * @return the position in the document at the beginning of the records content
     */
    public int getRecordStart(int recordIndex) {
        return getRecordRange(recordIndex).getStart();
    }

    /**
     * Get the start position of the specified comment including label
     *
     * @param comment the comment whose start position is trying to be found
     * @return the position in the document at the beginning of the comment label
     */
    public int getCommentStart(Comment comment) {
        return getCommentRange(comment).getStart();
    }

    /**
     * Gets the start position of the specified comment
     *
     * @param comment the comment whose start position is trying to be found
     * @return the position in the document at the beginning of the comments content
     */
    public int getCommentContentStart(Comment comment) {
        return getCommentContentRange(comment).getStart();
    }

    /**
     * Return the range for the given comment content
     *
     * @param comment
     * @return the range for the given comment content
     */
    public Range getCommentContentRange(Comment comment) {
        final int commentEleIdx = getSession().getTranscript().getElementIndex(comment);
        final int paraEleIdx = findParagraphElementIndexForSessionElementIndex(commentEleIdx);
        final Element paraEle = getDefaultRootElement().getElement(paraEleIdx);

        int commentStart = paraEle.getStartOffset();
        for(int i = 0; i < paraEle.getElementCount(); i++) {
            final Element childEle = paraEle.getElement(i);
            final AttributeSet attrs = childEle.getAttributes();
            final boolean isLabel = TranscriptStyleConstants.isLabel(attrs);
            if (!isLabel) {
                break;
            }
            commentStart = childEle.getEndOffset();
        }
        final int commentEnd = paraEle.getEndOffset() - 1;

        return new Range(commentStart, commentEnd);
    }

    /**
     * Gets the start position of the specified gem
     *
     * @param gem the gem whose start position is trying to be found
     * @return the position in the document at the beginning of the gem label
     */
    public int getGemStart(Gem gem) {
        return getGemRange(gem).getStart();
    }

    /**
     * Get the range for the given gem's content
     *
     * @param gem
     * @return the range for the given gem's content
     */
    public Range getGemContentRange(Gem gem) {
        final int gemEleIdx = getSession().getTranscript().getElementIndex(gem);
        final int paraEleIdx = findParagraphElementIndexForSessionElementIndex(gemEleIdx);
        final Element paraEle = getDefaultRootElement().getElement(paraEleIdx);

        int gemStart = paraEle.getStartOffset();
        for (int i = 0; i < paraEle.getElementCount(); i++) {
            final Element childEle = paraEle.getElement(i);
            final AttributeSet attrs = childEle.getAttributes();
            final boolean isLabel = TranscriptStyleConstants.isLabel(attrs);
            if (!isLabel) {
                break;
            }
            gemStart = childEle.getEndOffset();
        }
        final int gemEnd = paraEle.getEndOffset() - 1;

        return new Range(gemStart, gemEnd);
    }

    /**
     * Gets the start position of the specified gem
     *
     * @param gem the gem whose start position is trying to be found
     * @return the position in the document at the beginning of the gems content
     */
    public int getGemContentStart(Gem gem) {
        return getGemContentRange(gem).getStart();
    }

    /**
     * Gets the end position of the record at the specified index
     *
     * @param recordIndex the index of the record whose end is trying to be found
     * @return the position in the document immediately after the final character of the records content
     * (newlines included)
     */
    public int getRecordEnd(int recordIndex) {
        return getRecordRange(recordIndex).getEnd();
    }

    /**
     * Return the range of the given recordIndex
     * @param recordIndex
     * @return the range of the given recordIndex
     */
    public Range getRecordRange(int recordIndex) {
        return getRecordRange(getSession().getRecord(recordIndex));
    }

    /**
     * Gets the end position of the specified comment
     *
     * @param comment the comment whose end position is trying to be found
     * @return the position in the document immediately after the final character of the comments content
     * (newlines included)
     */
    public int getCommentEnd(Comment comment) {
        return getCommentRange(comment).getEnd();
    }

    /**
     * Gets the end position of the specified gem
     *
     * @param gem the gem whose end position is trying to be found
     * @return the position in the document immediately after the final character of the gems content
     * (newlines included)
     */
    public int getGemEnd(Gem gem) {
        return getGemRange(gem).getEnd();
    }

    /**
     * Gets the start position of the given record
     *
     * @param record the record whose start position is trying to be found
     * @return the position in the document at the beginning of the records content
     */
    public int getRecordStart(Record record) {
        return getRecordRange(record).getStart();
    }

    /**
     * Gets the start position of the specified tier
     *
     * @param tier the tier whose start position is trying to be found
     * @return the position in the document at the beginning of the tiers content
     */
    public int getTierStart(Tier<?> tier) {
        return getTierRange(tier).getStart();
    }

    /**
     * Gets the start position of a tier with the specified name in the record at the specified index
     *
     * @param recordIndex the index of the record that contains the tier
     * @param tierName    the name of the tier
     * @return the position in the document at the beginning of the tier label
     */
    public int getTierStart(int recordIndex, String tierName) {
        final int paragraphIdx = findParagraphElementIndexForTier(session.getTranscript().getRecordElementIndex(recordIndex), tierName);
        if (paragraphIdx == -1) return -1;
        Element elem = getDefaultRootElement().getElement(paragraphIdx);
        return elem.getEndOffset();
    }

    /**
     * Gets the start position of a tier with the specified name in the record at the specified index
     *
     * @param recordIndex the index of the record that contains the tier
     * @param tierName    the name of the tier
     * @return the position in the document at the beginning of the tiers content
     */
    public int getTierContentStart(int recordIndex, String tierName) {
        return getTierContentRange(recordIndex, tierName).getStart();
    }

    /**
     * Get the range of the given tier's content
     *
     * @param recordIndex
     * @param tierName
     * @return the range of the given tier's content
     */
    public Range getTierContentRange(int recordIndex, String tierName) {
        final int paragraphIdx = findParagraphElementIndexForTier(session.getTranscript().getRecordElementIndex(recordIndex), tierName);
        if (paragraphIdx == -1) return null;
        Element elem = getDefaultRootElement().getElement(paragraphIdx);
        int tierStart = elem.getStartOffset();
        for (int i = 0; i < elem.getElementCount(); i++) {
            Element innerElem = elem.getElement(i);
            AttributeSet attrs = innerElem.getAttributes();
            boolean isLabel = TranscriptStyleConstants.isLabel(attrs);
            if (isLabel) {
                tierStart = innerElem.getEndOffset();
            } else {
                break;
            }
        }
        final int tierEnd = elem.getEndOffset() - 1;

        return new Range(tierStart, tierEnd);
    }

    /**
     * Gets the end position of a tier with the specified name in the record at the specified index
     *
     * @param recordIndex the index of the record that contains the tier
     * @param tierName    the name of the tier
     * @return the position in the document immediately after the final character of the tiers content
     * (newlines included)
     */
    public int getTierEnd(int recordIndex, String tierName) {
        final int paragraphIdx = findParagraphElementIndexForTier(session.getTranscript().getRecordElementIndex(recordIndex), tierName);
        if (paragraphIdx == -1) return -1;
        Element elem = getDefaultRootElement().getElement(paragraphIdx);
        return elem.getEndOffset();
    }

    /**
     * Find paragraph element index for given tier
     *
     * @param elementIndex
     * @param tierName
     */
    public int findParagraphElementIndexForTier(int elementIndex, String tierName) {
        final int transcriptElementIndex = findParagraphElementIndexForSessionElementIndex(elementIndex);
        for(int i = transcriptElementIndex; i < getDefaultRootElement().getElementCount(); i++) {
            Element elem = getDefaultRootElement().getElement(i);
            if (elem.getElementCount() < 1) continue;
            AttributeSet attrs = elem.getAttributes();
            Record currentRecord = TranscriptStyleConstants.getRecord(attrs);
            if(elementIndex >= 0) {
                if (currentRecord == null) continue;
                Tier<?> tier = TranscriptStyleConstants.getTier(attrs);
                if(tier.getName().equals(tierName)) {
                    return i;
                }
            } else {
                if(currentRecord != null) break;
                Tier<?> tier = TranscriptStyleConstants.getGenericTier(attrs);
                if(tier != null && tier.getName().equals(tierName)) {
                    return i;
                }
            }
        }
        return -1;
    }

    /**
     * Gets the end position of the specified tier
     *
     * @param tier the tier whose end position is trying to be found
     * @return the position in the document immediately after the final character of the tiers content
     * (newlines included)
     */
    public int getTierEnd(Tier<?> tier) {
        return getTierRange(tier).getEnd();
    }

    /**
     * Gets the start position for the content of the specified tier
     *
     * @param tier the tier whose start position is trying to be found
     * @return the position in the document at the beginning of the tiers content
     */
    public int getTierContentStart(Tier<?> tier) {
        return getTierContentRange(tier).getStart();
    }

    /**
     * Get the range of the given generic tier (header tier)
     *
     * @param genericTier
     * @return the range of the given generic tier or Range(-1, -1) if not found
     */
    public Range getGenericRange(Tier<?> genericTier) {
        return getGenericRange(genericTier.getName());
    }

    /**
     * Get the range of the given generic tier (header tier)
     *
     * @param genericTierName
     * @return the range of the given generic tier or Range(-1, -1) if not found
     */
    public Range getGenericRange(String genericTierName) {
        final int paragraphIdx = findParagraphElementIndexForTier(-1, genericTierName);
        if (paragraphIdx == -1) return new Range(-1, -1);

        Element elem = getDefaultRootElement().getElement(paragraphIdx);
        return new Range(elem.getStartOffset(), elem.getEndOffset());
    }

    /**
     * Get the range of the given generic tier content (header tier)
     *
     * @param genericTier
     * @return the range of the given generic tier or Range(-1, -1) if not found
     */
    public Range getGenericContentRange(Tier<?> genericTier) {
        return getGenericContentRange(genericTier.getName());
    }

    /**
     * Get the range of the given generic tier content (header tier)
     *
     * @param genericTierName
     * @return the range of the given generic tier or Range(-1, -1) if not found
     */
    public Range getGenericContentRange(String genericTierName) {
        final int paragraphIdx = findParagraphElementIndexForTier(-1, genericTierName);
        if (paragraphIdx == -1) return new Range(-1, -1);

        Element elem = getDefaultRootElement().getElement(paragraphIdx);
        int tierStart = elem.getStartOffset();
        for (int i = 0; i < elem.getElementCount(); i++) {
            Element innerElem = elem.getElement(i);
            AttributeSet attrs = innerElem.getAttributes();
            boolean isLabel = TranscriptStyleConstants.isLabel(attrs);
            // If correct tier name
            if (isLabel) {
                tierStart = innerElem.getEndOffset();
                break;
            }
        }
        final int tierEnd = elem.getEndOffset();

        return new Range(tierStart, tierEnd);
    }

    /**
     * Gets the start position of the specified "generic tier"
     *
     * @param genericTier a reference to the generic tier whose start position is trying to be found
     * @return the position in the document at the beginning of the generic tiers content
     */
    public int getGenericContentStart(Tier<?> genericTier) {
        return getGenericContentRange(genericTier).getStart();
    }

    /**
     * Gets the start position of the "generic tier" with the specified name
     *
     * @param genericTierName the name of the generic tier whose start position is trying to be found
     * @return the position in the document at the beginning of the generic tiers label
     */
    public int getGenericStart(String genericTierName) {
        return getGenericRange(genericTierName).getStart();
    }

    /**
     * Gets the start position of the "generic tier" with the specified name
     *
     * @param genericTier the generic tier whose start position is trying to be found
     * @return the position in the document at the beginning of the generic tiers label
     */
    public int getGenericStart(Tier<?> genericTier) {
        return getGenericRange(genericTier).getStart();
    }

    /**
     * Gets the start position of the "generic tier" with the specified name
     *
     * @param genericTierName the name of the generic tier whose start position is trying to be found
     * @return the position in the document at the beginning of the generic tiers content
     */
    public int getGenericContentStart(String genericTierName) {
        return getGenericContentRange(genericTierName).getStart();
    }

    /**
     * Gets the end position of the specified "generic tier"
     *
     * @param genericTier the generic tier whose end position is trying to be found
     * @return the position in the document immediately after the final character of the generic tiers content
     * (newlines included)
     */
    public int getGenericEnd(Tier<?> genericTier) {
        return getGenericRange(genericTier).getEnd();
    }

    /**
     * Gets the end position of the "generic tier" with the specified name
     *
     * @param genericTierName the name of the generic tier whose end position is trying to be found
     * @return the position in the document immediately after the final character of the generic tiers content
     * (newlines included)
     */
    public int getGenericEnd(String genericTierName) {
        return getGenericRange(genericTierName).getEnd();
    }

    /**
     * Return string range for given tier
     *
     * @param tier
     * @return the range for the given tier or Range(-1, -1) if not found
     */
    public Range getTierRange(Tier<?> tier) {
        // find record which contains given tier
        for(Record r: getSession().getRecords()) {
            if(r.getTier(tier.getName()) == tier) {
                int firstParagraphIdx =
                        findParagraphElementIndexForSessionElementIndex(getSession().getTranscript().getElementIndex(r));
                if(firstParagraphIdx >= 0) {
                    for (int i = firstParagraphIdx; i < getDefaultRootElement().getElementCount(); i++) {
                        Element e = getDefaultRootElement().getElement(i);
                        Tier<?> eTier = TranscriptStyleConstants.getTier(e.getAttributes());
                        if (eTier != null && eTier.getName().equals(tier.getName())) {
                            return new Range(e.getStartOffset(), e.getEndOffset());
                        }
                    }
                }
            }
        }
        return new Range(-1, -1);
    }

    /**
     * Get tier content range
     *
     * @param tier
     * @return the range for the given tier or Range(-1, -1) if not found
     */
    public Range getTierContentRange(Tier<?> tier) {
        // find record which contains given tier
        for(Record r: getSession().getRecords()) {
            if(r.getTier(tier.getName()) == tier) {
                int firstParagraphIdx =
                        findParagraphElementIndexForSessionElementIndex(getSession().getTranscript().getElementIndex(r));
                if(firstParagraphIdx >= 0) {
                    for (int i = firstParagraphIdx; i < getDefaultRootElement().getElementCount(); i++) {
                        Element e = getDefaultRootElement().getElement(i);
                        if (e.getAttributes().getAttribute(TranscriptStyleConstants.ATTR_KEY_TIER) == tier) {
                            int tierStart = e.getStartOffset();
                            for (int j = 0; j < e.getElementCount(); j++) {
                                Element childEle = e.getElement(j);
                                AttributeSet attrs = childEle.getAttributes();
                                Boolean isLabel = TranscriptStyleConstants.isLabel(attrs);
                                if (!isLabel) {
                                    break;
                                }
                                tierStart = childEle.getEndOffset();
                            }
                            final int tierEnd = e.getEndOffset();
                            return new Range(tierStart, tierEnd);
                        }
                    }
                }
            }
        }
        return new Range(-1, -1);
    }

    /**
     * Return string range for given gem
     *
     * @param gem
     * @return the range for the given gem or Range(-1, -1) if not found
     */
    public Range getGemRange(Gem gem) {
        final int sessionElementIndex = getSession().getTranscript().getElementIndex(gem);
        return getRangeForSessionElementIndex(sessionElementIndex);
    }

    /**
     * Return string range for given comment
     *
     * @param comment
     * @return the range for the given comment or Range(-1, -1) if not found
     */
    public Range getCommentRange(Comment comment) {
        final int sessionElementIndex = getSession().getTranscript().getElementIndex(comment);
        return getRangeForSessionElementIndex(sessionElementIndex);
    }

    /**
     * Return string range for given record
     *
     * @param record
     * @return the range for the given record or Range(-1, -1) if not found
     */
    public Range getRecordRange(Record record) {
        final int sessionElementIndex = getSession().getTranscript().getElementIndex(record);
        return getRangeForSessionElementIndex(sessionElementIndex);
    }

    /**
     * Return the range for the given transcript element index
     *
     * @param sessionElementIndex the session element index
     * @return the range for the given transcript element index or Range(-1, -1) if not found
     */
    public Range getRangeForSessionElementIndex(int sessionElementIndex) {
        int eleIdx = findParagraphElementIndexForSessionElementIndex(sessionElementIndex);
        if(eleIdx == -1) {
            return new Range(-1, -1);
        }
        Element ele = getDefaultRootElement().getElement(eleIdx);
        Element endEle = ele;
        if(ele.getAttributes().getAttribute(TranscriptStyleConstants.ATTR_KEY_RECORD) != null) {
            for(int i = eleIdx + 1; i < getDefaultRootElement().getElementCount(); i++) {
                Element e = getDefaultRootElement().getElement(i);
                if(getTranscriptElementIndex(e) == sessionElementIndex)
                    endEle = e;
                else
                    break;
            }
        }
        if(ele != null) {
            return new Range(ele.getStartOffset(), endEle.getEndOffset());
        }
        return new Range(-1, -1);
    }

    /**
     * Binary search for the first paragraph element index for the given session element index
     *
     * @param sessionElementIndex the session element index
     * @return the first paragraph element index for the given element index or -1 if not found
     */
    private int findParagraphElementIndexForSessionElementIndex(int sessionElementIndex) {
        final Element rootEle = getDefaultRootElement();
        int low = 0;
        int high = rootEle.getElementCount() - 1;
        while (low <= high) {
            int mid = (low + high) >>> 1;
            Element midEle = rootEle.getElement(mid);
            int midEleIdx = getTranscriptElementIndex(midEle);
            if (sessionElementIndex == midEleIdx) {
                for(int i = mid; i >= 0; i--) {
                    Element e = rootEle.getElement(i);
                    if(getTranscriptElementIndex(e) == sessionElementIndex) {
                        midEleIdx = i;
                    } else {
                        break;
                    }
                }
                return midEleIdx;
            } else if (sessionElementIndex < midEleIdx) {
                high = mid - 1;
            } else {
                low = mid + 1;
            }
        }
        return -1;
    }

    /**
     * Return transcript element index for given paragraph element
     *
     * @param paraEle the paragraph element
     * @return the transcript element index or -1 if not found
     */
    private int getTranscriptElementIndex(Element paraEle) {
        final AttributeSet attrs = paraEle.getAttributes();
        int paraEleIdx = -1;
        // determine transcript element index of midEle
        if (attrs.getAttribute(TranscriptStyleConstants.ATTR_KEY_RECORD) != null) {
            paraEleIdx = getSession().getRecordElementIndex(TranscriptStyleConstants.getRecord(attrs));
        } else if (attrs.getAttribute(TranscriptStyleConstants.ATTR_KEY_COMMENT) != null) {
            paraEleIdx = getSession().getTranscript().getElementIndex(TranscriptStyleConstants.getComment(attrs));
        } else if (attrs.getAttribute(TranscriptStyleConstants.ATTR_KEY_GEM) != null) {
            paraEleIdx = getSession().getTranscript().getElementIndex(TranscriptStyleConstants.getGEM(attrs));
        } else if (attrs.getAttribute(TranscriptStyleConstants.ATTR_KEY_GENERIC_TIER) != null) {
            paraEleIdx = -1;
        }
        return paraEleIdx;
    }

    /**
     * Gets a record containing the start and end of a specified segment assuming the specified position is somewhere
     * between the two
     *
     * @param segment     the segment that the bounds are being calculated for
     * @param includedPos a position included in the bounds of the segment
     * @return a record containing the start and end of the segment
     */
    public StartEnd getSegmentBounds(MediaSegment segment, int includedPos) {
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
                        i = root.getElementCount();
                        j = elem.getElementCount();
                    }
                }
            }
        }

        if (indexInSegment == -1) return new StartEnd(-1, -1);

        int segmentStart = indexInSegment;

        AttributeSet attrs = getCharacterElement(segmentStart).getAttributes();
        while (attrs.getAttribute(TranscriptStyleConstants.ATTR_KEY_MEDIA_SEGMENT) != null) {
            segmentStart--;
            attrs = getCharacterElement(segmentStart).getAttributes();
        }

        int segmentEnd = indexInSegment;
        attrs = getCharacterElement(segmentEnd).getAttributes();
        while (attrs.getAttribute(TranscriptStyleConstants.ATTR_KEY_MEDIA_SEGMENT) != null) {
            segmentEnd++;
            attrs = getCharacterElement(segmentEnd).getAttributes();
        }

        return new StartEnd(segmentStart + 1, segmentEnd - 1);
    }


    /**
     * Gets the character at the specified position in the document
     *
     * @param pos the position to get the character from
     * @return the character at the specified position
     */
    public Character getCharAtPos(int pos) {
        try {
            return getText(pos, 1).charAt(0);
        } catch (BadLocationException e) {
            LogUtil.warning(e);
            return null;
        }
    }



    /**
     * Gets the index of the record containing the specified position
     *
     * @param position a position in the document in a record
     * @return the index of the record
     */
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
     */
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
     */
    public Tier<?> getTier(int position) {
        AttributeSet attributes = getCharacterElement(position).getAttributes();
        return (Tier<?>) attributes.getAttribute(TranscriptStyleConstants.ATTR_KEY_TIER);
    }

    /**
     * Gets the offset from the preceding label of a given position in the document
     *
     * @param pos the position that the offset will come from
     */
    public int getOffsetInContent(int pos) {
        Element elem = getCharacterElement(pos);
        String transcriptElementType = (String) elem.getAttributes().getAttribute(TranscriptStyleConstants.ATTR_KEY_ELEMENT_TYPE);
        if (transcriptElementType == null) return -1;

        switch (transcriptElementType) {
            case TranscriptStyleConstants.ATTR_KEY_RECORD -> {
                Tier<?> tier = (Tier<?>) elem.getAttributes().getAttribute(TranscriptStyleConstants.ATTR_KEY_TIER);
                if (tier == null) return -1;
                int recordStartPos = getTierContentStart(tier);
                return pos - recordStartPos;
            }
            case TranscriptStyleConstants.ATTR_KEY_COMMENT -> {
                Comment comment = (Comment) elem.getAttributes().getAttribute(TranscriptStyleConstants.ATTR_KEY_COMMENT);
                if (comment == null) return -1;
                int commentStartPos = getCommentContentStart(comment);
                return pos - commentStartPos;
            }
            case TranscriptStyleConstants.ATTR_KEY_GEM -> {
                Gem gem = (Gem) elem.getAttributes().getAttribute(TranscriptStyleConstants.ATTR_KEY_GEM);
                if (gem == null) return -1;
                return pos - getGemContentStart(gem);
            }
            case TranscriptStyleConstants.ATTR_KEY_GENERIC_TIER -> {
                Tier<?> genericTier = (Tier<?>) elem.getAttributes().getAttribute(TranscriptStyleConstants.ATTR_KEY_GENERIC_TIER);
                if (genericTier == null) return -1;
                return pos - getGenericContentStart(genericTier);
            }
            default -> {
                return -1;
            }
        }
    }
    // endregion Transcript <-> Document Positioning


    // region Transcript Changes

    /**
     * Deletes a given transcript element from the document
     */
    public void deleteTranscriptElement(Transcript.Element elem) {
        try {
            int startOffset = -1;
            int endOffset = -1;

            if (elem.isComment()) {
                startOffset = getCommentStart(elem.asComment());
                endOffset = getCommentEnd(elem.asComment());
            } else if (elem.isGem()) {
                startOffset = getGemStart(elem.asGem());
                endOffset = getGemEnd(elem.asGem());
            } else if (elem.isRecord()) {
                return;
            }
            if (startOffset < 0 || endOffset < 0) return;

            bypassDocumentFilter = true;
            remove(startOffset, endOffset - startOffset);
        } catch (BadLocationException e) {
            LogUtil.severe(e);
        }
    }

    /**
     * Updates the displayed type of the given comment in the document
     */
    public void onChangeCommentType(Comment comment) {
        final Range commentRange = getCommentRange(comment);
        if(commentRange.getStart() < 0) return;
        final int start = commentRange.getStart();
        // don't remove newline
        final int end = commentRange.getEnd() - 1;

        try {
            bypassDocumentFilter = true;
            remove(start, end - start);
            TranscriptBatchBuilder batchBuilder = new TranscriptBatchBuilder();
            batchBuilder.appendComment(comment);
            processBatchUpdates(start, batchBuilder.getBatch());
        } catch (BadLocationException e) {
            LogUtil.severe(e);
        }
    }

    /**
     * Updates the displayed type of the given gem in the document
     */
    public void onChangeGemType(Gem gem) {
        final Range gemRange = getGemRange(gem);
        if(gemRange.getStart() < 0) return;
        final int start = gemRange.getStart();
        // don't remove newline
        final int end = gemRange.getEnd() - 1;

        try {
            bypassDocumentFilter = true;
            remove(start, end - start);
            TranscriptBatchBuilder batchBuilder = new TranscriptBatchBuilder();
            batchBuilder.appendGem(gem);
            processBatchUpdates(start, batchBuilder.getBatch());
        } catch (BadLocationException e) {
            LogUtil.severe(e);
        }
    }

    /**
     * Moves a record in the document
     *
     * @param oldRecordIndex  the existing index of the record
     * @param oldElementIndex the existing element index of the record
     * @param newRecordIndex  the new index of the record
     * @param newElementIndex the new element index of the record
     */
    public void moveRecord(int oldRecordIndex, int newRecordIndex, int oldElementIndex, int newElementIndex) {
        try {
            Transcript transcript = session.getTranscript();
            var tierView = session.getTierView();
            AttributeSet newLineAttrs;

            int start = getRecordStart(Math.min(oldRecordIndex, newRecordIndex));
            int end = getRecordEnd(Math.max(oldRecordIndex, newRecordIndex));

            bypassDocumentFilter = true;
            remove(start, end - start);

            TranscriptBatchBuilder batchBuilder = new TranscriptBatchBuilder();
            batchBuilder.appendBatchEndStart();

            int startElementIndex = Math.min(oldElementIndex, newElementIndex);
            int endElementIndex = Math.max(oldElementIndex, newElementIndex);

            for (int i = startElementIndex; i < endElementIndex + 1; i++) {
                Transcript.Element elem = transcript.getElementAt(i);
                if (elem.isRecord()) {
                    batchBuilder.appendRecord(session, elem.asRecord(), isChatTierNamesShown());
                } else if (elem.isComment()) {
                    batchBuilder.appendComment(elem.asComment());
                } else {
                    batchBuilder.appendGem(elem.asGem());
                }
                batchBuilder.appendEOL();
            }

            processBatchUpdates(start, batchBuilder.getBatch());
        } catch (BadLocationException e) {
            LogUtil.severe(e);
        }
    }

    /**
     * Updates the speaker on the separator / record header
     */
    public void onChangeSpeaker(Record record) {
//        try {
//            int recordIndex = session.getRecordPosition(record);
//            int start = getRecordStart(recordIndex, true);
//
//            var tierView = session.getTierView();
//            String firstVisibleTierName = tierView
//                    .stream()
//                    .filter(item -> item.isVisible())
//                    .findFirst()
//                    .get()
//                    .getTierName();
//            int end = getTierStart(recordIndex, firstVisibleTierName);
//
//            bypassDocumentFilter = true;
//            remove(start, end - start);
//
//            AttributeSet recordAttrs = transcriptStyleContext.getRecordAttributes(record);
//
//            SimpleAttributeSet tierAttrs = transcriptStyleContext.getTierAttributes(record.getSegmentTier());
//            tierAttrs.addAttributes(transcriptStyleContext.getSeparatorAttributes());
//            tierAttrs.addAttributes(recordAttrs);
//
//            SimpleAttributeSet labelAttrs = transcriptStyleContext.getTierLabelAttributes(record.getSegmentTier());
//            labelAttrs.addAttributes(transcriptStyleContext.getSeparatorAttributes());
//            labelAttrs.addAttributes(recordAttrs);
//
//            TranscriptBatchBuilder batchBuilder = new TranscriptBatchBuilder();
//            batchBuilder.appendBatchEndStart();
//
//            Participant speaker = record.getSpeaker() == null ? Participant.UNKNOWN : record.getSpeaker();
//            batchBuilder.appendBatchString(formatLabelText(speaker.toString()) + ": ", labelAttrs);
//
//            MediaSegment segment = record.getMediaSegment();
//
//            tierAttrs.addAttributes(getStandardFontAttributes());
//            appendFormattedSegment(segment, tierAttrs, batchBuilder);
//
//            processBatchUpdates(start, batchBuilder.getBatch());
//        } catch (BadLocationException e) {
//            LogUtil.severe(e);
//        }
    }

    /**
     * Updates the data of the specified tier
     *
     * @param tier the tier whose data gets updated
     */
    public void onTierDataChanged(Tier<?> tier) {
        // ignore syllable and alignment tiers, they are updated with the parent tier
        if (tier.getName().equals(SystemTierType.TargetSyllables.getName()) || tier.getName().equals(SystemTierType.ActualSyllables.getName()))
            return;
        if (tier.getDeclaredType().equals(PhoneAlignment.class)) return;

        try {
            final Range tierRange = getTierContentRange(tier);
            int start = tierRange.getStart();
            int recordIndex = getRecordIndex(start);
            int end = tierRange.getEnd() - 1;
            final SimpleAttributeSet tierAttrs = new SimpleAttributeSet(getCharacterElement(start).getAttributes());

            bypassDocumentFilter = true;
            remove(start, end - start);

            var tierView = session.getTierView();
            TierViewItem tierViewItem = tierView.stream().filter(item -> item.getTierName().equals(tier.getName())).findFirst().orElse(null);

            TranscriptBatchBuilder batchBuilder = new TranscriptBatchBuilder();

            Record record = session.getRecord(recordIndex);
            batchBuilder.appendTierContent(record, tier, tierAttrs);

            processBatchUpdates(start, batchBuilder.getBatch());
        } catch (BadLocationException e) {
            LogUtil.severe(e);
        }
    }

    /**
     * Adds a comment to the document and the transcript at the given transcript element index
     */
    public void addComment(Comment comment, int transcriptElementIndex) {
        int offset = -1;

        if (transcriptElementIndex == 0) {
            var elementAfterComment = session.getTranscript().getElementAt(1);
            if (elementAfterComment.isRecord()) {
                offset = getRecordStart(session.getRecordPosition(elementAfterComment.asRecord()));
            } else if (elementAfterComment.isComment()) {
                offset = getCommentStart(elementAfterComment.asComment());
            } else if (elementAfterComment.isGem()) {
                offset = getGemStart(elementAfterComment.asGem());
            } else {
                throw new RuntimeException("Invalid transcript element");
            }
        } else {
            var elementBeforeComment = session.getTranscript().getElementAt(transcriptElementIndex - 1);
            if (elementBeforeComment.isRecord()) {
                offset = getRecordEnd(session.getRecordPosition(elementBeforeComment.asRecord()));
            } else if (elementBeforeComment.isComment()) {
                offset = getCommentEnd(elementBeforeComment.asComment());
            } else if (elementBeforeComment.isGem()) {
                offset = getGemEnd(elementBeforeComment.asGem());
            } else {
                throw new RuntimeException("Invalid transcript element");
            }
        }

        try {
            TranscriptBatchBuilder batchBuilder = new TranscriptBatchBuilder();
            batchBuilder.appendComment(comment);
            batchBuilder.appendEOL();
            processBatchUpdates(offset, batchBuilder.getBatch());
        } catch (BadLocationException e) {
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
            } else if (elementAfterComment.isComment()) {
                offset = getCommentStart(elementAfterComment.asComment());
            } else if (elementAfterComment.isGem()) {
                offset = getGemStart(elementAfterComment.asGem());
            } else {
                throw new RuntimeException("Invalid transcript element");
            }

        } else {
            var elementBeforeComment = session.getTranscript().getElementAt(transcriptElementIndex - 1);
            if (elementBeforeComment.isRecord()) {
                offset = getRecordEnd(session.getRecordPosition(elementBeforeComment.asRecord()));
            } else if (elementBeforeComment.isComment()) {
                offset = getCommentEnd(elementBeforeComment.asComment());
            } else if (elementBeforeComment.isGem()) {
                offset = getGemEnd(elementBeforeComment.asGem());
            } else {
                throw new RuntimeException("Invalid transcript element");
            }
        }

        try {
            TranscriptBatchBuilder batchBuilder = new TranscriptBatchBuilder();
            batchBuilder.appendGem(gem);
            batchBuilder.appendEOL();
            processBatchUpdates(offset, batchBuilder.getBatch());
        } catch (BadLocationException e) {
            LogUtil.severe(e);
        }
    }

    /**
     * Update record text in the document
     *
     * @param record the record whose media segment is being updated
     */
    public void updateRecord(Record record) {
        final Range recordRange = getRecordRange(record);
        if(recordRange.getStart() < 0) return;
        int start = recordRange.getStart();
        int end = recordRange.getEnd();

        try {
            bypassDocumentFilter = true;
            remove(start, end - start);
            TranscriptBatchBuilder batchBuilder = new TranscriptBatchBuilder();
            batchBuilder.appendRecord(session, record, isChatTierNamesShown());
            batchBuilder.appendEOL();
            processBatchUpdates(start, batchBuilder.getBatch());
        } catch (BadLocationException e) {
            LogUtil.severe(e);
        }
    }

    /**
     * Adds a record to the document
     *
     * @param addedRecord the record that gets added
     */
    public void addRecord(Record addedRecord, int elementIndex) {
        try {
            TranscriptBatchBuilder batchBuilder = new TranscriptBatchBuilder();
            batchBuilder.appendRecord(session, addedRecord, isChatTierNamesShown());
            batchBuilder.appendEOL();
            int nextElementStart = getLength();

            Transcript transcript = getSession().getTranscript();
            Transcript.Element nextElement = (elementIndex < transcript.getNumberOfElements() - 1) ? transcript.getElementAt(elementIndex + 1) : null;
            if(nextElement != null) {
                if (nextElement.isRecord()) {
                    nextElementStart = getRecordStart(nextElement.asRecord());
                } else if (nextElement.isComment()) {
                    nextElementStart = getCommentStart(nextElement.asComment());
                } else if (nextElement.isGem()) {
                    nextElementStart = getGemStart(nextElement.asGem());
                }
            }

            processBatchUpdates(nextElementStart, batchBuilder.getBatch());
        } catch (BadLocationException e) {
            LogUtil.severe(e);
        }
    }

    /**
     * Removes a record from the document
     *
     * @param removedRecord the record that gets removed
     */
    public void deleteRecord(Record removedRecord) {
        try {
            final Range recordRange = getRecordRange(removedRecord);
            if(recordRange.getStart() < 0) return;
            final int start = recordRange.getStart();
            final int end = recordRange.getEnd();

            bypassDocumentFilter = true;
            remove(start, end - start);
        } catch (BadLocationException e) {
            LogUtil.severe(e);
        }
    }

    /**
     * Sets whether a {@link TierViewItem} with the provided name is locked
     *
     * @param tierName the name of the tier that will be locked or unlocked
     * @param locked   whether the tier will be locked or unlocked
     */
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
            } else {
                newTierView.add(oldItem);
            }
        }
        session.setTierView(newTierView);
    }

    // endregion Record Changes

    // region Document Properties
    /**
     * Gets the object associated with a given key from the document properties
     *
     * @param key the key to get the object with the object
     * @return the object associated with the key ({@code null} if no object is present)
     */
    public Object getDocumentProperty(String key) {
        return getProperty(key);
    }

    /**
     * Gets the object associated with a given key from the document properties
     * and returns a given default value if none is present
     *
     * @param key          the key to get the object with the object
     * @param defaultValue the object to be returned if there is no object associated with the key
     * @return the object associated with the key or the default if no object is present
     */
    public Object getDocumentPropertyOrDefault(String key, Object defaultValue) {
        Object value = getProperty(key);
        return value == null ? defaultValue : value;
    }

    /**
     * Adds a given key-value pair to the document properties
     *
     * @param key   the kek
     * @param value the value
     */
    public void putDocumentProperty(String key, Object value) {
        Object existingValue = getProperty(key);
        putProperty(key, value);
        propertyChangeSupport.firePropertyChange(key, existingValue, value);
    }


    /**
     * Adds a property change listener that responds to changes in the document property with the given key / name
     *
     * @param propertyName           the name / key of the property that will be listened to
     * @param propertyChangeListener the listener that contains some behavior that will happen when the property changes
     */
    public void addDocumentPropertyChangeListener(String propertyName, PropertyChangeListener propertyChangeListener) {
        propertyChangeSupport.addPropertyChangeListener(propertyName, propertyChangeListener);
    }

    /**
     * Removes a document property change listener
     *
     * @param propertyChangeListener the listener that was listening for changes in a document property
     */
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
     */
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
     */
    public void addNotEditableAttribute(String attributeKey) {
        notEditableAttributes.add(attributeKey);
    }

    /**
     * Removes the given attribute from the set of "not editable" attributes
     *
     * @param attributeKey the attribute that will be removed from the set
     */
    public void removeNotEditableAttribute(String attributeKey) {
        notEditableAttributes.remove(attributeKey);
    }

    // endregion Not Editable Attributes

    // region Populate
    private final ReentrantLock populateLock = new ReentrantLock();

    private PopulateWorker populateWorker = null;

    /**
     * Determine if the document is currently being populated
     */
    public boolean isPopulating() {
        return populateWorker != null && !populateWorker.isDone();
    }

    private class PopulateWorker extends SwingWorker<Integer, List<ElementSpec>> {

        record EndStart(ElementSpec end, ElementSpec start) {}

        private EndStart nextEndStart = null;

        @Override
        protected Integer doInBackground() throws Exception {
            firePropertyChange("populate", false, true);
            int totalElements = 0;

            Transcript transcript = session.getTranscript();
            var tierView = session.getTierView();

            TranscriptBatchBuilder batchBuilder = new TranscriptBatchBuilder(getTranscriptStyleContext(), getInsertionHooks());
            for (var hook : insertionHooks) {
                batchBuilder.appendAll(hook.startSession());
            }
            publish(new ArrayList<>(batchBuilder.getBatch()));
            totalElements += batchBuilder.clear();

//            SimpleAttributeSet newLineAttrs;
            for (var hook : insertionHooks) {
                batchBuilder.appendAll(hook.startTranscript());
            }
            publish(new ArrayList<>(batchBuilder.getBatch()));
            totalElements += batchBuilder.clear();

            // Single record
            if (singleRecordView) {
                Record record = session.getRecord(singleRecordIndex);
                int recordTranscriptElementIndex = transcript.getElementIndex(record);

                // display any elements before the record
                int includedElementIndex;
                if (singleRecordIndex == 0) {
                    includedElementIndex = 0;
                } else {
                    includedElementIndex = transcript.getElementIndex(transcript.getRecord(singleRecordIndex - 1)) + 1;
                }
                while (includedElementIndex < recordTranscriptElementIndex) {
                    Transcript.Element previousElement = transcript.getElementAt(includedElementIndex);
                    if (previousElement.isRecord()) {
                        break;
                    }

                    if (previousElement.isComment()) {
                        batchBuilder.appendComment(previousElement.asComment());
                    } else {
                        batchBuilder.appendGem(previousElement.asGem());
                    }
                    batchBuilder.appendEOL();

                    includedElementIndex++;

                    publish(new ArrayList<>(batchBuilder.getBatch()));
                    totalElements += batchBuilder.clear();
                }

                // add record
                batchBuilder.appendRecord(session, record, isChatTierNamesShown());
                publish(new ArrayList<>(batchBuilder.getBatch()));
                totalElements += batchBuilder.clear();

                // display any elements after the record
                int nextElementIndex = recordTranscriptElementIndex + 1;
                int transcriptElementCount = transcript.getNumberOfElements();
                while (nextElementIndex < transcriptElementCount) {
                    Transcript.Element nextElement = transcript.getElementAt(nextElementIndex);
                    if (nextElement.isRecord()) {
                        break;
                    }

                    if (nextElement.isComment()) {
                        batchBuilder.appendComment(nextElement.asComment());
                    } else {
                        batchBuilder.appendGem(nextElement.asGem());
                    }
                    batchBuilder.appendEOL();

                    publish(new ArrayList<>(batchBuilder.getBatch()));
                    totalElements += batchBuilder.clear();

                    nextElementIndex++;
                }
            }
            // All records
            else {
                for (int i = 0; i < transcript.getNumberOfElements(); i++) {
                    if(isCancelled()) return -1;
                    Transcript.Element elem = transcript.getElementAt(i);
                    Transcript.Element nextElem = i < transcript.getNumberOfElements() - 1 ? transcript.getElementAt(i + 1) : null;

                    SimpleAttributeSet nextParagraphAttrs = null;
                    if(nextElem != null && nextElem.isRecord()) {
                        nextParagraphAttrs = transcriptStyleContext.getRecordStartAttributes();
                    }

                    if (elem.isRecord()) {
                        batchBuilder.appendRecord(session, elem.asRecord(), isChatTierNamesShown());
                    } else if (elem.isComment()) {
                        batchBuilder.appendComment(elem.asComment());
                        batchBuilder.appendEOL();
                    } else {
                        batchBuilder.appendGem(elem.asGem());
                        batchBuilder.appendEOL();
                    }

                    publish(new ArrayList<>(batchBuilder.getBatch()));
                    totalElements += batchBuilder.clear();
                }
                if (!batchBuilder.isEmpty()) {
                    publish(new ArrayList<>(batchBuilder.getBatch()));
                    totalElements += batchBuilder.clear();
                }
            }

            for (var hook : insertionHooks) {
                batchBuilder.appendAll(hook.endTranscript());
            }
            publish(new ArrayList<>(batchBuilder.getBatch()));
            totalElements += batchBuilder.clear();

            for (var hook : getInsertionHooks()) {
                batchBuilder.appendAll(hook.endSession());
            }
            if (!batchBuilder.isEmpty()) {
                publish(new ArrayList<>(batchBuilder.getBatch()));
                totalElements += batchBuilder.clear();
            }

            return totalElements;
        }

        @Override
        protected void process(List<List<ElementSpec>> chunks) {
            for (var chunk : chunks) {
                if(isCancelled()) return;
                if(chunk.isEmpty()) continue;
                try {
                    if(nextEndStart != null) {
                        chunk.add(0, nextEndStart.start);
                        chunk.add(0, nextEndStart.end);
                        nextEndStart = null;
                    }
                    if(chunk.get(chunk.size() - 1).getType() == ElementSpec.StartTagType) {
                        ElementSpec nextStart = chunk.remove(chunk.size() - 1);
                        ElementSpec nextEnd = chunk.remove(chunk.size() - 1);
                        nextEndStart = new EndStart(nextEnd, nextStart);
                    }
                    processBatchUpdates(getLength(), chunk);
                } catch (BadLocationException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        @Override
        protected void done() {
            firePropertyChange("populate", true, false);
        }

    }

    /**
     * Populates the document with the appropriate content, this thread creates a new PopulateWorker
     * if necessary and cancels the old one if it is still running.  This method must be executed on the
     * EDT.
     *
     * @throws BadLocationException if there is an error in the document
     * @throws RuntimeException if this method is not called on the EDT
     */
    private void populate() throws BadLocationException {
        if(!SwingUtilities.isEventDispatchThread())
            throw new RuntimeException("populate must be called on the EDT");
        if(populateLock.tryLock()) {
            try {
                if (populateWorker != null && !populateWorker.isDone()) {
                    populateWorker.cancel(true);
                }
                super.remove(0, getLength());
                populateWorker = new PopulateWorker();
                populateWorker.execute();
            } finally {
                populateLock.unlock();
            }
        }
    }

    /**
     * Empties the document and repopulates it with up-to-date data
     */
    public void reload() {
        try {
            bypassDocumentFilter = true;
            remove(0, getLength());
            populate();
        } catch (BadLocationException e) {
            LogUtil.severe(e);
        }
    }
    // endregion Populate

    // region TranscriptDocumentInsertionHook
    public List<InsertionHook> getInsertionHooks() {
        return Collections.unmodifiableList(this.insertionHooks);
    }

    private void loadRegisteredInsertionHooks() {
        for (var hookExtPt : PluginManager.getInstance().getExtensionPoints(InsertionHook.class)) {
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
    // endregion IExtendable

    /**
     * The default document filter for the {@link TranscriptDocument}
     */
    public static class TranscriptDocumentFilter extends DocumentFilter {

        private final TranscriptDocument doc;

        /**
         * The constructor
         *
         * @param doc a reference to the {@link TranscriptDocument}
         */
        public TranscriptDocumentFilter(TranscriptDocument doc) {
            this.doc = doc;
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

        @Override
        public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet _attrs) throws BadLocationException {



            // For some reason attrs gets the attributes from the previous character, so this fixes that
            SimpleAttributeSet attrs = new SimpleAttributeSet();
            attrs.addAttributes(doc.getCharacterElement(offset).getAttributes());

            // Labels and stuff
            if (doc.containsNotEditableAttribute(attrs)) {
                if (attrs.getAttribute(TranscriptStyleConstants.ATTR_KEY_FIRST_SEGMENT_DASH) != null) {
                    super.replace(fb, offset, length, text, _attrs);
                }
                return;
            }

            // Locked tiers
            Tier<?> tier = (Tier<?>) attrs.getAttribute(TranscriptStyleConstants.ATTR_KEY_TIER);
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
    }

    /**
     * A wrapper record for a list of {@link Language}
     */
    public record Languages(List<Language> languageList) {}

    /**
     * A wrapper record for a start and end index
     * */
    public record StartEnd(int start, int end) {}

    // endregion
}