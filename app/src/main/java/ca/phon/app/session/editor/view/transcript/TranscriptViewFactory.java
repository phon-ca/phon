package ca.phon.app.session.editor.view.transcript;

import ca.phon.session.Tier;
import ca.phon.ui.fonts.FontPreferences;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.text.*;
import java.awt.*;
import java.awt.geom.Rectangle2D;

/**
 * The {@link ViewFactory} used by the {@link TranscriptEditor}
 */
public class TranscriptViewFactory implements ViewFactory {

    public int pageWidth = (int)(8.5 * 96);

    private int labelColumnWidth = 150;

    /**
     * The spacing between the lines of the document
     */

    private float lineSpacing = 0.2f;

    /**
     * Calculate preferred tier label width for given document
     *
     * @param g
     * @param transcriptDocument
     * @return width of tier label column
     */
    public static int calculatePreferredLabelColumnWidth(Graphics g, TranscriptDocument transcriptDocument) {
        var root = transcriptDocument.getDefaultRootElement();
        return calculatePreferredLabelColumnWidth(g, 0, root);
    }

    private static int calculatePreferredLabelColumnWidth(Graphics g, int currentMax, Element element) {
        if(element.isLeaf()) {
            var attrs = element.getAttributes();
            var isLabel = TranscriptStyleConstants.isLabel(attrs);
            if (isLabel) {
                var tier = TranscriptStyleConstants.getTier(attrs);
                if(tier != null) {
                    var lblText = "    " + tier.getName() + ": ";
                    var tierLabelWidth = g.getFontMetrics(FontPreferences.getTierFont()).stringWidth(lblText);
                    return Math.max(currentMax, tierLabelWidth);
                }
            }
            return currentMax;
        } else {
            int retVal = currentMax;
            for (int i = 0; i < element.getElementCount(); i++) {
                var elem = element.getElement(i);
                retVal = Math.max(retVal, calculatePreferredLabelColumnWidth(g, retVal, elem));
            }
            return retVal;
        }
    }

    public TranscriptViewFactory() {
    }

    public float getLineSpacing() {
        return lineSpacing;
    }

    public int getLabelColumnWidth(Graphics g, TranscriptDocument transcriptDocument) {
        if(labelColumnWidth < 0) {
            labelColumnWidth = calculatePreferredLabelColumnWidth(g, transcriptDocument);
        }
        return labelColumnWidth;
    }

    public int getPageWidth() {
        return pageWidth;
    }

    @Override
    public View create(Element elem) {
        String kind = elem.getName();
        var attrs = elem.getAttributes();

        var componentFactory = TranscriptStyleConstants.getComponentFactory(attrs);
        if (componentFactory != null) {
            kind = "componentFactory";
        }

        var isLabel = TranscriptStyleConstants.isLabel(attrs);
        var clickable = TranscriptStyleConstants.isClickable(attrs);
        if(isLabel && clickable) {
            kind = "label";
        }

        if (kind != null) {
            switch (kind) {
                case "label" -> {
                    return new TierLabelView(elem);
                }
                case AbstractDocument.ContentElementName -> {
                    return new CustomWrapView(elem);
                }
                case AbstractDocument.ParagraphElementName -> {
                    return new TierParagraphView(elem);
                }
                case AbstractDocument.SectionElementName -> {
                    return new SessionBoxView(elem, View.Y_AXIS);
                }
                case StyleConstants.ComponentElementName -> {
                    return new ComponentView(elem);
                }
                case StyleConstants.IconElementName -> {
                    return new IconView(elem);
                }
                case "componentFactory" -> {
                    return new ComponentFactoryView(elem);
                }
            }
        }

        // default to text display
        return new LabelView(elem);
    }

    /**
     * A {@link ComponentView} that dynamically loads components from a component factory referenced in the
     * {@code TranscriptStyleConstants.ATTR_KEY_COMPONENT_FACTORY} attribute
     */
    private static class ComponentFactoryView extends ComponentView {
        public ComponentFactoryView(Element elem) {
            super(elem);
        }

        @Override
        protected Component createComponent() {
            AttributeSet attrs = getAttributes();
            var componentFactory = TranscriptStyleConstants.getComponentFactory(attrs);
            if (componentFactory != null) {
                return componentFactory.createComponent(attrs);
            }

            return null;
        }
    }

    /**
     * A {@link BoxView} that is used to display the transcript and is used to set the width of the
     * transcript to the width of a letter page
     */
    private class SessionBoxView extends BoxView {

        public SessionBoxView(Element elem, int axis) {
            super(elem, axis);
        }

        @Override
        protected void layoutMinorAxis(int targetSpan, int axis, int[] offsets, int[] spans) {
            super.layoutMinorAxis(getPageWidth(), axis, offsets, spans);
        }

    }

    /**
     * A {@link LabelView} that is used to display tier labels
     */
    private class TierLabelView extends LabelView {

        public TierLabelView(Element elem) {
            super(elem);
        }

        @Override
        public float getPreferredSpan(int axis) {
            float span = super.getPreferredSpan(axis);
            if(axis == View.X_AXIS) {
                span = getLabelColumnWidth(getGraphics(), (TranscriptDocument) getElement().getDocument()) - labelEndWidth();
            }
            return span;
        }

        @Override
        public float getMinimumSpan(int axis) {
            return getPreferredSpan(axis);
        }

        @Override
        public float getMaximumSpan(int axis) {
            return getPreferredSpan(axis);
        }

        @Override
        public int getBreakWeight(int axis, float pos, float len) {
            return View.BadBreakWeight;
        }

        private int labelTextWidth() {
            int p0 = getStartOffset();
            int p1 = getEndOffset();
            Segment text = getText(p0, p1);
            Font font = getFont();
            Rectangle2D textBounds = getGraphics().getFontMetrics(font).getStringBounds(text.toString(), getGraphics());
            return (int)textBounds.getWidth();
        }

        private int labelEndWidth() {
            int p0 = getStartOffset();
            int p1 = getEndOffset();
            String text = ": ";
            Font font = getFont();
            Rectangle2D textBounds = getGraphics().getFontMetrics(font).getStringBounds(text.toString(), getGraphics());
            return (int)textBounds.getWidth();
        }


        @Override
        public TabExpander getTabExpander() {
            final TabExpander defaultExpander = super.getTabExpander();
            return new TabExpander() {
                @Override
                public float nextTabStop(float x, int tabOffset) {
                    return getLabelColumnWidth(getGraphics(), (TranscriptDocument) getElement().getDocument()) - labelTextWidth() - labelEndWidth();
                }
            };
        }

        @Override
        public void paint(Graphics g, Shape a) {
            g.clearRect(a.getBounds().x, a.getBounds().y, a.getBounds().width, a.getBounds().height);
            super.paint(g, a);
        }

    }

    /**
     * A {@link ParagraphView} that is used to display tier paragraphs including label and content
     */
    private class TierParagraphView extends ParagraphView {

        private Border border = BorderFactory.createMatteBorder(0, 0, 0, 0, Color.LIGHT_GRAY);

        public TierParagraphView(Element elem) {
            super(elem);
            if(elem.getAttributes().getAttribute(TranscriptStyleConstants.ATTR_KEY_BORDER) instanceof Border b) {
                border = b;
            }
            setPropertiesFromAttributes();
            setLineSpacing(getLineSpacing());
            SimpleAttributeSet attrs = new SimpleAttributeSet();
            StyleConstants.setLeftIndent(attrs, labelColumnWidth);
            setParagraphInsets(attrs);
            setFirstLineIndent(-labelColumnWidth);
        }

        // add border insets to view insets
        @Override
        public short getTopInset() {
            return (short) (super.getTopInset() + border.getBorderInsets(getContainer()).top);
        }

        @Override
        public short getBottomInset() {
            return (short) (super.getBottomInset() + border.getBorderInsets(getContainer()).bottom);
        }

        @Override
        public short getLeftInset() {
            return (short) (super.getLeftInset() + border.getBorderInsets(getContainer()).left);
        }

        @Override
        public short getRightInset() {
            return (short) (super.getRightInset() + border.getBorderInsets(getContainer()).right);
        }

        @Override
        public void paint(Graphics g, Shape a) {
            // return if view count is 0 to avoid an issue at the end of the document when populating using a SwingWorker
            if(getViewCount() == 0) return;
            super.paint(g, a);
            border.paintBorder(getContainer(), g, a.getBounds().x, a.getBounds().y, a.getBounds().width, a.getBounds().height);
        }

    }

    /**
     * A {@link LabelView} that is used to display content within a tier paragraph.  Content is wrapped
     * on spaces only.
     */
    static class CustomWrapView extends LabelView {
        public CustomWrapView(Element elem) {
            super(elem);
        }

        @Override
        public void paint(Graphics g, Shape a) {
            g.clearRect(a.getBounds().x, a.getBounds().y, a.getBounds().width, a.getBounds().height);
            super.paint(g, a);
        }

        @Override
        public int getBreakWeight(int axis, float pos, float len) {
            if (axis == View.X_AXIS) {
                checkPainter();
                int p0 = getStartOffset();
                int p1 = getGlyphPainter().getBoundedPosition(this, p0, pos, len);
                if (p1 == p0) {
                    // can't fit anything into this
                    return View.BadBreakWeight;
                }
                try {
                    if (getDocument().getText(p0, p1 - p0).contains(" ")) {
                        return View.ExcellentBreakWeight;
                    }
                } catch (BadLocationException ble) {
                    // assume we can't break
                    return View.BadBreakWeight;
                }
            }
            return View.BadBreakWeight;
        }

        @Override
        public View breakView(int axis, int p0, float pos, float len) {
            if (axis == View.X_AXIS) {
                checkPainter();
                int p1 = getGlyphPainter().getBoundedPosition(this, p0, pos, len);
                try {
                    int breakPos = getDocument().getText(p0, p1 - p0).lastIndexOf(" ");
                    if (breakPos >= 0) {
                        p1 = p0 + breakPos + 1;
                    }
                } catch (BadLocationException ble) {
                    // assume we can't break
                }
                if (p0 == getStartOffset() && p1 == getEndOffset()) {
                    return this;
                }
                return createFragment(p0, p1);
            }
            return this;
        }

    }
}