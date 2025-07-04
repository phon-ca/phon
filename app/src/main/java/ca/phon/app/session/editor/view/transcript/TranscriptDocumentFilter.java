package ca.phon.app.session.editor.view.transcript;

import ca.phon.app.log.LogUtil;
import ca.phon.formatter.MediaTimeFormatStyle;
import ca.phon.session.MediaSegment;
import ca.phon.session.Tier;
import ca.phon.session.Transcriber;
import ca.phon.session.format.MediaSegmentFormatter;
import ca.phon.session.position.TranscriptElementLocation;

import javax.swing.text.*;
import java.text.ParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The default document filter for the {@link TranscriptDocument}
 */
public class TranscriptDocumentFilter extends DocumentFilter {

    /**
     * Attribute key used for indicating custom filters for non-editable
     * elements of the document.
     *
     * The type of the attribute value should be a {@link DocumentFilter)
     */
    public static final String ATTR_KEY_CUSTOM_FILTER = "customFilter";

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
            if (TranscriptStyleConstants.isNotEditable(attrs)) {
                final DocumentFilter customFilter = getCustomFilter(attrs);
                if(customFilter != null) {
                    customFilter.remove(fb, offset, length);
                }
                return;
            }
            if (attrs.getAttribute(TranscriptStyleConstants.ATTR_KEY_SYLLABIFICATION) != null) return;
            try {
                String txt = doc.getText(offset, length);
                if(txt.contains("\n")) {
                    return;
                }
            } catch (BadLocationException e) {
                LogUtil.severe(e);
            }
        }
        super.remove(fb, offset, length);
    }

    @Override
    public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet _attrs) throws BadLocationException {
        // For some reason attrs gets the attributes from the previous character, so this fixes that
        SimpleAttributeSet attrs = new SimpleAttributeSet();
        attrs.addAttributes(doc.getCharacterElement(offset).getAttributes());

        final TranscriptElementLocation location = doc.charPosToSessionLocation(offset);

        // Locked tiers - if locked, do not allow editing
        Tier<?> tier = TranscriptStyleConstants.getTier(attrs);
        if (tier != null) {
            String tierName = tier.getName();

            if(!tier.isBlind() && doc.getTranscriber() != Transcriber.VALIDATOR) {
                return;
            }

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

        // Labels and other non-editable elements
        if (TranscriptStyleConstants.isNotEditable(attrs)) {
            final DocumentFilter customFilter = getCustomFilter(attrs);
            if(customFilter != null) {
                customFilter.replace(fb, offset, length, text, attrs);
            }
            return;
        }

        super.replace(fb, offset, length, text, attrs);
    }

    public static DocumentFilter getCustomFilter(AttributeSet attrs) {
        return (DocumentFilter)attrs.getAttribute(ATTR_KEY_CUSTOM_FILTER);
    }

    public static void setCustomFilter(MutableAttributeSet attrs, DocumentFilter filter) {
        attrs.addAttribute(ATTR_KEY_CUSTOM_FILTER, filter);
    }

}
