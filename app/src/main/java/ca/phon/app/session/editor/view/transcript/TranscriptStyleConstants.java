package ca.phon.app.session.editor.view.transcript;

import ca.phon.session.*;
import ca.phon.session.tierdata.TierData;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.text.AttributeSet;
import javax.swing.text.MutableAttributeSet;
import java.lang.Record;

public class TranscriptStyleConstants {

    public static final String ATTR_KEY_RECORD = "record";

    public static Record getRecord(MutableAttributeSet attrs) {
        return (Record)attrs.getAttribute(ATTR_KEY_RECORD);
    }

    public static void setRecord(MutableAttributeSet attrs, Record record) {
        attrs.removeAttribute(ATTR_KEY_RECORD);
        if(record != null)
            attrs.addAttribute(ATTR_KEY_RECORD, record);
    }

    public static final String ATTR_KEY_TIER = "tier";

    public static Tier<?> getTier(AttributeSet attrs) {
        return (Tier<?>)attrs.getAttribute(ATTR_KEY_TIER);
    }

    public static void setTier(MutableAttributeSet attrs, Tier<?> tier) {
        attrs.removeAttribute(ATTR_KEY_TIER);
        if(tier != null)
            attrs.addAttribute(ATTR_KEY_TIER, tier);
    }

    public static final String ATTR_KEY_COMMENT = "comment";

    public static Comment getComment(AttributeSet attrs) {
        return (Comment)attrs.getAttribute(ATTR_KEY_COMMENT);
    }

    public static void setComment(MutableAttributeSet attrs, Comment comment) {
        attrs.removeAttribute(ATTR_KEY_COMMENT);
        if(comment != null)
            attrs.addAttribute(ATTR_KEY_COMMENT, comment);
    }

    public static final String ATTR_KEY_GEM = "gem";

    public static Gem getGEM(AttributeSet attrs) {
        return (Gem)attrs.getAttribute(ATTR_KEY_GEM);
    }

    public static void setGEM(MutableAttributeSet attrs, Gem gem) {
        attrs.removeAttribute(ATTR_KEY_GEM);
        if(gem != null)
            attrs.addAttribute(ATTR_KEY_GEM, gem);
    }

    public static final String ATTR_KEY_GENERIC = "generic";

    public static boolean isGeneric(AttributeSet attrs) {
        return attrs.isDefined(ATTR_KEY_GENERIC);
    }

    public static void setGeneric(MutableAttributeSet attrs, boolean generic) {
        if(generic)
            attrs.addAttribute(ATTR_KEY_GENERIC, Boolean.TRUE);
        else
            attrs.removeAttribute(ATTR_KEY_GENERIC);
    }

    public static final String ATTR_KEY_ELEMENT_TYPE = "elementType";

    public static final String ELEMENT_TYPE_TIER = "tier";
    public static final String ELEMENT_TYPE_COMMENT = "comment";
    public static final String ELEMENT_TYPE_GEM = "gem";
    public static final String ELEMENT_TYPE_GENERIC = "generic";

    public static String getElementType(AttributeSet attrs) {
        return (String)attrs.getAttribute(ATTR_KEY_ELEMENT_TYPE);
    }

    public static void setElementType(MutableAttributeSet attrs, String elementType) {
        attrs.removeAttribute(ATTR_KEY_ELEMENT_TYPE);
        if(elementType != null)
            attrs.addAttribute(ATTR_KEY_ELEMENT_TYPE, elementType);
    }

    public static final String ATTR_KEY_SEPARATOR = "sep";

    public static boolean isSeparator(AttributeSet attrs) {
        return attrs.isDefined(ATTR_KEY_SEPARATOR);
    }

    public static void setSeparator(MutableAttributeSet attrs, boolean separator) {
        if(separator)
            attrs.addAttribute(ATTR_KEY_SEPARATOR, Boolean.TRUE);
        else
            attrs.removeAttribute(ATTR_KEY_SEPARATOR);
    }

    public static final String ATTR_KEY_LABEL = "label";

    public static boolean isLabel(AttributeSet attrs) {
        return attrs.isDefined(ATTR_KEY_LABEL);
    }

    public static void setLabel(MutableAttributeSet attrs, boolean label) {
        if(label)
            attrs.addAttribute(ATTR_KEY_LABEL, Boolean.TRUE);
        else
            attrs.removeAttribute(ATTR_KEY_LABEL);
    }

    public static final String ATTR_KEY_NOT_EDITABLE = "notEditable";

    public static boolean isNotEditable(AttributeSet attrs) {
        return attrs.isDefined(ATTR_KEY_NOT_EDITABLE);
    }

    public static void setNotEditable(MutableAttributeSet attrs, boolean notEditable) {
        if(notEditable)
            attrs.addAttribute(ATTR_KEY_NOT_EDITABLE, Boolean.TRUE);
        else
            attrs.removeAttribute(ATTR_KEY_NOT_EDITABLE);
    }

    public static final String ATTR_KEY_NOT_TRAVERSABLE = "notTraversable";

    public static boolean isNotTraversable(AttributeSet attrs) {
        return attrs.isDefined(ATTR_KEY_NOT_TRAVERSABLE);
    }

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

    public static final String ATTR_KEY_CLICKABLE = "clickable";

    public static boolean isClickable(AttributeSet attrs) {
        return attrs.isDefined(ATTR_KEY_CLICKABLE);
    }

    public static void setClickable(MutableAttributeSet attrs, boolean clickable) {
        if(clickable)
            attrs.addAttribute(ATTR_KEY_CLICKABLE, Boolean.TRUE);
        else
            attrs.removeAttribute(ATTR_KEY_CLICKABLE);
    }

    public static final String ATTR_KEY_COMPONENT_FACTORY = "componentFactory";

    public static ComponentFactory getComponentFactory(AttributeSet attrs) {
        return (ComponentFactory)attrs.getAttribute(ATTR_KEY_COMPONENT_FACTORY);
    }

    public static void setComponentFactory(MutableAttributeSet attrs, ComponentFactory factory) {
        attrs.removeAttribute(ATTR_KEY_COMPONENT_FACTORY);
        if(factory != null)
            attrs.addAttribute(ATTR_KEY_COMPONENT_FACTORY, factory);
    }

    public static final String ATTR_KEY_MEDIA_SEGMENT = "mediaSegment";

    public static MediaSegment getMediaSegment(AttributeSet attrs) {
        return (MediaSegment)attrs.getAttribute(ATTR_KEY_MEDIA_SEGMENT);
    }

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

    public static String getTranscriber(MutableAttributeSet attrs) {
        return (String) attrs.getAttribute(ATTR_KEY_TRANSCRIBER);
    }

    public static void setTranscriber(MutableAttributeSet attrs, String transcriber) {
        attrs.removeAttribute(ATTR_KEY_TRANSCRIBER);
        if(transcriber != null)
            attrs.addAttribute(ATTR_KEY_TRANSCRIBER, transcriber);
    }

    public static final String ATTR_KEY_ENTER_ACTION = "enterAction";

    public static Action getEnterAction(MutableAttributeSet attrs) {
        return (Action)attrs.getAttribute(ATTR_KEY_ENTER_ACTION);
    }

    public static void setEnterAction(MutableAttributeSet attrs, Action action) {
        attrs.removeAttribute(ATTR_KEY_ENTER_ACTION);
        if(action != null)
            attrs.addAttribute(ATTR_KEY_ENTER_ACTION, action);
    }

    public static final String ATTR_KEY_BORDER = "border";

    public static Border getBorder(MutableAttributeSet attrs) {
        return (Border)attrs.getAttribute(ATTR_KEY_BORDER);
    }

    public static void setBorder(MutableAttributeSet attrs, Border border) {
        attrs.removeAttribute(ATTR_KEY_BORDER);
        if(border != null)
            attrs.addAttribute(ATTR_KEY_BORDER, border);
    }

    public static final String ATTR_KEY_FIRST_SEGMENT_DASH = "firstSegmentDash";

    public static boolean isFirstSegmentDash(MutableAttributeSet attrs) {
        return attrs.isDefined(ATTR_KEY_FIRST_SEGMENT_DASH);
    }

    public static void setFirstSegmentDash(MutableAttributeSet attrs, boolean firstSegmentDash) {
        if(firstSegmentDash)
            attrs.addAttribute(ATTR_KEY_FIRST_SEGMENT_DASH, Boolean.TRUE);
        else
            attrs.removeAttribute(ATTR_KEY_FIRST_SEGMENT_DASH);
    }

}
