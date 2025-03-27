package ca.phon.app.session.editor.view.transcript;

import ca.phon.session.*;
import ca.phon.session.Record;
import ca.phon.session.tierdata.TierData;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.text.AttributeSet;
import javax.swing.text.MutableAttributeSet;
import java.awt.event.MouseEvent;
import java.util.function.BiConsumer;

/**
 * Keys and methods for setting style attributes on {@link AttributeSet}s used in
 * {@link TranscriptView}s.
 */
public class TranscriptStyleConstants {

    /**
     * Record associated with text
     */
    public static final String ATTR_KEY_RECORD = "record";

    /**
     * Get the record associated with the given attributes
     * @param attrs the attributes
     * @return the record or {@code null} if not set
     */
    public static Record getRecord(AttributeSet attrs) {
        return (Record)attrs.getAttribute(ATTR_KEY_RECORD);
    }

    /**
     * Set the record for the given attributes
     * @param attrs the attributes
     * @param record the record
     */
    public static void setRecord(MutableAttributeSet attrs, Record record) {
        attrs.removeAttribute(ATTR_KEY_RECORD);
        if(record != null)
            attrs.addAttribute(ATTR_KEY_RECORD, record);
    }

    /**
     * Tier associated with text
     */
    public static final String ATTR_KEY_TIER = "tier";

    /**
     * Get the tier associated with the given attributes
     * @param attrs the attributes
     * @return the tier or {@code null} if not set
     */
    public static Tier<?> getTier(AttributeSet attrs) {
        return (Tier<?>)attrs.getAttribute(ATTR_KEY_TIER);
    }

    /**
     * Set the tier for the given attributes
     * @param attrs the attributes
     * @param tier the tier
     */
    public static void setTier(MutableAttributeSet attrs, Tier<?> tier) {
        attrs.removeAttribute(ATTR_KEY_TIER);
        if(tier != null)
            attrs.addAttribute(ATTR_KEY_TIER, tier);
    }

    /**
     * Parent tier - some tiers are dependent on other tiers considered their 'parent'.
     * When a parent tier is moved or changed, the dependent tier should also be updated.
     */
    public static final String ATTR_KEY_PARENT_TIER = "parentTier";

    /**
     * Get the parent tier associated with the given attributes
     * @param attrs the attributes
     * @return the parent tier or {@code null} if not set
     */
    public static Tier<?> getParentTier(AttributeSet attrs) {
        return (Tier<?>)attrs.getAttribute(ATTR_KEY_PARENT_TIER);
    }

    /**
     * Set the parent tier for the given attributes
     * @param attrs the attributes
     * @param parentTier the parent tier
     */
    public static void setParentTier(MutableAttributeSet attrs, Tier<?> parentTier) {
        attrs.removeAttribute(ATTR_KEY_PARENT_TIER);
        if(parentTier != null)
            attrs.addAttribute(ATTR_KEY_PARENT_TIER, parentTier);
    }

    /**
     * Comment associated with text
     */
    public static final String ATTR_KEY_COMMENT = "comment";

    /**
     * Get the comment associated with the given attributes
     * @param attrs the attributes
     * @return the comment or {@code null} if not set
     */
    public static Comment getComment(AttributeSet attrs) {
        return (Comment)attrs.getAttribute(ATTR_KEY_COMMENT);
    }

    /**
     * Set the comment for the given attributes
     * @param attrs the attributes
     * @param comment the comment
     */
    public static void setComment(MutableAttributeSet attrs, Comment comment) {
        attrs.removeAttribute(ATTR_KEY_COMMENT);
        if(comment != null)
            attrs.addAttribute(ATTR_KEY_COMMENT, comment);
    }

    /**
     * Gem associated with text
     */
    public static final String ATTR_KEY_GEM = "gem";

    /**
     * Get the gem associated with the given attributes
     * @param attrs
     * @return
     */
    public static Gem getGem(AttributeSet attrs) {
        return (Gem)attrs.getAttribute(ATTR_KEY_GEM);
    }

    /**
     * Set the gem for the given attributes
     * @param attrs
     * @param gem
     */
    public static void setGem(MutableAttributeSet attrs, Gem gem) {
        attrs.removeAttribute(ATTR_KEY_GEM);
        if(gem != null)
            attrs.addAttribute(ATTR_KEY_GEM, gem);
    }

    /**
     * Generic/header tier associated with text
     */
    public static final String ATTR_KEY_GENERIC_TIER = "generic";

    /**
     * Get the generic tier associated with the given attributes
     * @param attrs
     * @return
     */
    public static Tier<?> getGenericTier(AttributeSet attrs) {
        return (Tier<?>)attrs.getAttribute(ATTR_KEY_GENERIC_TIER);
    }

    /**
     * Set the generic tier for the given attributes
     * @param attrs
     * @param genericTier
     */
    public static void setGenericTier(MutableAttributeSet attrs, Tier<?> genericTier) {
        attrs.removeAttribute(ATTR_KEY_GENERIC_TIER);
        if(genericTier != null)
            attrs.addAttribute(ATTR_KEY_GENERIC_TIER, genericTier);
    }

    /**
     * Element type for text
     */
    public static final String ATTR_KEY_ELEMENT_TYPE = "elementType";

    /**
     * Element types
     */
    /**
     * Element type for a record/record tier
     */
    public static final String ELEMENT_TYPE_RECORD = "record";
    /**
     * Element type for a comment
     */
    public static final String ELEMENT_TYPE_COMMENT = "comment";
    /**
     * Element type for a gem
     */
    public static final String ELEMENT_TYPE_GEM = "gem";
    /**
     * Element type for a generic/header tier
     */
    public static final String ELEMENT_TYPE_GENERIC = "generic";
    /**
     * Element type for a blind transcription tier
     */
    public static final String ELEMENT_TYPE_BLIND_TRANSCRIPTION = "blindTranscription";

    /**
     * Get the element type associated with the given attributes
     * @param attrs the attributes
     * @return the element type or {@code null} if not set
     */
    public static String getElementType(AttributeSet attrs) {
        return (String)attrs.getAttribute(ATTR_KEY_ELEMENT_TYPE);
    }

    /**
     * Set the element type for the given attributes
     * @param attrs the attributes
     * @param elementType the element type
     */
    public static void setElementType(MutableAttributeSet attrs, String elementType) {
        attrs.removeAttribute(ATTR_KEY_ELEMENT_TYPE);
        if(elementType != null)
            attrs.addAttribute(ATTR_KEY_ELEMENT_TYPE, elementType);
    }

//    public static final String ATTR_KEY_SEPARATOR = "sep";
//
//    public static boolean isSeparator(AttributeSet attrs) {
//        return attrs.isDefined(ATTR_KEY_SEPARATOR);
//    }
//
//    public static void setSeparator(MutableAttributeSet attrs, boolean separator) {
//        if(separator)
//            attrs.addAttribute(ATTR_KEY_SEPARATOR, Boolean.TRUE);
//        else
//            attrs.removeAttribute(ATTR_KEY_SEPARATOR);
//    }

    /**
     * Attribute key for setting the new paragraph attribute
     */
    public static final String ATTR_KEY_NEW_PARAGRAPH = "newParagraph";

    /**
     * Get the new paragraph attribute
     * @param attrs the attributes
     * @return {@code true} if the new paragraph attribute is set, {@code false} otherwise
     */
    public static boolean isNewParagraph(AttributeSet attrs) {
        return attrs.isDefined(ATTR_KEY_NEW_PARAGRAPH);
    }

    /**
     * Set the new paragraph attribute
     * @param attrs the attributes
     * @param newParagraph {@code true} to set the new paragraph attribute, {@code false} to remove it
     */
    public static void setNewParagraph(MutableAttributeSet attrs, boolean newParagraph) {
        if(newParagraph)
            attrs.addAttribute(ATTR_KEY_NEW_PARAGRAPH, Boolean.TRUE);
        else
            attrs.removeAttribute(ATTR_KEY_NEW_PARAGRAPH);
    }

    /**
     * Attribute key for setting the label attribute
     */
    public static final String ATTR_KEY_LABEL = "label";

    /**
     * Get the label attribute
     * @param attrs the attributes
     * @return {@code true} if the label attribute is set, {@code false} otherwise
     */
    public static boolean isLabel(AttributeSet attrs) {
        return attrs.isDefined(ATTR_KEY_LABEL);
    }

    /**
     * Set the label attribute
     * @param attrs the attributes
     * @param label {@code true} to set the label attribute, {@code false} to remove it
     */
    public static void setLabel(MutableAttributeSet attrs, boolean label) {
        if(label)
            attrs.addAttribute(ATTR_KEY_LABEL, Boolean.TRUE);
        else
            attrs.removeAttribute(ATTR_KEY_LABEL);
    }

    /**
     * Attribute key for setting ability to modify the text.  If this attribute is set
     * to {@code true} the text will not be editable unless a custom filter is set.
     */
    public static final String ATTR_KEY_NOT_EDITABLE = "notEditable";

    /**
     * Check if the text is not editable
     * @param attrs the attributes
     * @return {@code true} if the text is not editable, {@code false} otherwise
     */
    public static boolean isNotEditable(AttributeSet attrs) {
        return attrs.isDefined(ATTR_KEY_NOT_EDITABLE);
    }

    /**
     * Set the not editable attribute
     * @param attrs the attributes
     * @param notEditable {@code true} to set the not editable attribute, {@code false} to remove it
     */
    public static void setNotEditable(MutableAttributeSet attrs, boolean notEditable) {
        if(notEditable)
            attrs.addAttribute(ATTR_KEY_NOT_EDITABLE, Boolean.TRUE);
        else
            attrs.removeAttribute(ATTR_KEY_NOT_EDITABLE);
    }

    /**
     * Set the ability to traverse the text with the text caret.  If this attribute is set
     * to {@code true} the text will not be traversable.
     */
    public static final String ATTR_KEY_NOT_TRAVERSABLE = "notTraversable";

    /**
     * Check if the text is not traversable
     * @param attrs the attributes
     * @return {@code true} if the text is not traversable, {@code false} otherwise
     */
    public static boolean isNotTraversable(AttributeSet attrs) {
        return attrs.isDefined(ATTR_KEY_NOT_TRAVERSABLE);
    }

    /**
     * Set the not traversable attribute
     * @param attrs the attributes
     * @param notTraversable {@code true} to set the not traversable attribute, {@code false} to remove it
     */
    public static void setNotTraversable(MutableAttributeSet attrs, boolean notTraversable) {
        if(notTraversable)
            attrs.addAttribute(ATTR_KEY_NOT_TRAVERSABLE, Boolean.TRUE);
        else
            attrs.removeAttribute(ATTR_KEY_NOT_TRAVERSABLE);
    }

    public static final String ATTR_KEY_NOT_TRAVERSABLE_SYLLABIFICATION = "notTraversableSyllabification";

    public static boolean isNotTraversableSyllabification(MutableAttributeSet attrs) {
        return attrs.isDefined(ATTR_KEY_NOT_TRAVERSABLE_SYLLABIFICATION);
    }

    public static void setNotTraversableSyllabification(MutableAttributeSet attrs, boolean notTraversableSyllabification) {
        if(notTraversableSyllabification)
            attrs.addAttribute(ATTR_KEY_NOT_TRAVERSABLE_SYLLABIFICATION, Boolean.TRUE);
        else
            attrs.removeAttribute(ATTR_KEY_NOT_TRAVERSABLE_SYLLABIFICATION);
    }

    /**
     * Attribute key for setting the underline on hover attribute
     */
    public static final String ATTR_KEY_UNDERLINE_ON_HOVER = "clickable";

    public static boolean isUnderlineOnHover(AttributeSet attrs) {
        return attrs.isDefined(ATTR_KEY_UNDERLINE_ON_HOVER);
    }

    public static void setUnderlineOnHover(MutableAttributeSet attrs, boolean clickable) {
        if(clickable)
            attrs.addAttribute(ATTR_KEY_UNDERLINE_ON_HOVER, Boolean.TRUE);
        else
            attrs.removeAttribute(ATTR_KEY_UNDERLINE_ON_HOVER);
    }

    /**
     * Attribute key for setting the click handler
     */
    public static final String ATTR_KEY_CLICK_HANDLER = "clickHandler";

    public static BiConsumer<MouseEvent, AttributeSet> getClickHandler(AttributeSet attrs) {
        return (BiConsumer<MouseEvent, AttributeSet>)attrs.getAttribute(ATTR_KEY_CLICK_HANDLER);
    }

    public static void setClickHandler(MutableAttributeSet attrs, BiConsumer<MouseEvent, AttributeSet> clickHandler) {
        attrs.removeAttribute(ATTR_KEY_CLICK_HANDLER);
        if(clickHandler != null)
            attrs.addAttribute(ATTR_KEY_CLICK_HANDLER, clickHandler);
    }

    /**
     * Attribute key for setting the component factory for text components,
     * the component factory will create a component for the given text which
     * will be inserted in the view.
     */
    public static final String ATTR_KEY_COMPONENT_FACTORY = "componentFactory";

    /**
     * Get the component factory associated with the given attributes
     *
     * @param attrs the attributes
     *
     * @return the component factory or {@code null} if not set
     */
    public static ComponentFactory getComponentFactory(AttributeSet attrs) {
        return (ComponentFactory)attrs.getAttribute(ATTR_KEY_COMPONENT_FACTORY);
    }

    /**
     * Set the component factory for the given attributes
     *
     * @param attrs the attributes
     * @param factory the component factory
     */
    public static void setComponentFactory(MutableAttributeSet attrs, ComponentFactory factory) {
        attrs.removeAttribute(ATTR_KEY_COMPONENT_FACTORY);
        if(factory != null)
            attrs.addAttribute(ATTR_KEY_COMPONENT_FACTORY, factory);
    }

    /**
     * Attribute key for setting media segment
     */
    public static final String ATTR_KEY_MEDIA_SEGMENT = "mediaSegment";

    /**
     * Get the media segment associated with the given attributes
     *
     * @param attrs the attributes
     *
     * @return the media segment or {@code null} if not set
     */
    public static MediaSegment getMediaSegment(AttributeSet attrs) {
        return (MediaSegment)attrs.getAttribute(ATTR_KEY_MEDIA_SEGMENT);
    }

    /**
     * Set the media segment for the given attributes
     *
     * @param attrs the attributes
     * @param segment the media segment
     */
    public static void setMediaSegment(MutableAttributeSet attrs, MediaSegment segment) {
        attrs.removeAttribute(ATTR_KEY_MEDIA_SEGMENT);
        if(segment != null)
            attrs.addAttribute(ATTR_KEY_MEDIA_SEGMENT, segment);
    }

    public static final String ATTR_KEY_LOCKED = "locked";

    public static boolean isLocked(MutableAttributeSet attrs) {
        return attrs.isDefined(ATTR_KEY_LOCKED);
    }

    public static void setLocked(MutableAttributeSet attrs, boolean locked) {
        if(locked)
            attrs.addAttribute(ATTR_KEY_LOCKED, Boolean.TRUE);
        else
            attrs.removeAttribute(ATTR_KEY_LOCKED);
    }

    public static final String ATTR_KEY_SYLLABIFICATION = "syllabification";

    public static final String ATTR_KEY_PHONE = "phone";
    public static final String ATTR_KEY_COMMENT_TIER = "commentTier";

    public static Tier<TierData> getCommentTier(MutableAttributeSet attrs) {
        return (Tier<TierData>)attrs.getAttribute(ATTR_KEY_COMMENT_TIER);
    }

    public static void setCommentTier(MutableAttributeSet attrs, Tier<TierData> commentTier) {
        attrs.removeAttribute(ATTR_KEY_COMMENT_TIER);
        if(commentTier != null)
            attrs.addAttribute(ATTR_KEY_COMMENT_TIER, commentTier);
    }

    public static final String ATTR_KEY_BLIND_TRANSCRIPTION = "blindTranscription";



    public static final String ATTR_KEY_TRANSCRIBER = "transcriber";

    public static String getTranscriber(AttributeSet attrs) {
        return (String) attrs.getAttribute(ATTR_KEY_TRANSCRIBER);
    }

    public static void setTranscriber(MutableAttributeSet attrs, String transcriber) {
        attrs.removeAttribute(ATTR_KEY_TRANSCRIBER);
        if(transcriber != null)
            attrs.addAttribute(ATTR_KEY_TRANSCRIBER, transcriber);
    }

    public static final String ATTR_KEY_ENTER_ACTION = "enterAction";

    public static Action getEnterAction(AttributeSet attrs) {
        return (Action)attrs.getAttribute(ATTR_KEY_ENTER_ACTION);
    }

    public static void setEnterAction(MutableAttributeSet attrs, Action action) {
        attrs.removeAttribute(ATTR_KEY_ENTER_ACTION);
        if(action != null)
            attrs.addAttribute(ATTR_KEY_ENTER_ACTION, action);
    }

    public static final String ATTR_KEY_BORDER = "border";

    public static Border getBorder(AttributeSet attrs) {
        return (Border)attrs.getAttribute(ATTR_KEY_BORDER);
    }

    public static void setBorder(MutableAttributeSet attrs, Border border) {
        attrs.removeAttribute(ATTR_KEY_BORDER);
        if(border != null)
            attrs.addAttribute(ATTR_KEY_BORDER, border);
    }

//    public static final String ATTR_KEY_FIRST_SEGMENT_DASH = "firstSegmentDash";
//
//    public static boolean isFirstSegmentDash(MutableAttributeSet attrs) {
//        return attrs.isDefined(ATTR_KEY_FIRST_SEGMENT_DASH);
//    }
//
//    public static void setFirstSegmentDash(MutableAttributeSet attrs, boolean firstSegmentDash) {
//        if(firstSegmentDash)
//            attrs.addAttribute(ATTR_KEY_FIRST_SEGMENT_DASH, Boolean.TRUE);
//        else
//            attrs.removeAttribute(ATTR_KEY_FIRST_SEGMENT_DASH);
//    }

}
