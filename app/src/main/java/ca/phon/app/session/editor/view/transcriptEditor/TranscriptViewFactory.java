package ca.phon.app.session.editor.view.transcriptEditor;

import org.w3c.dom.css.Rect;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.text.*;
import java.awt.*;
import java.awt.geom.Rectangle2D;

/**
 * The {@link ViewFactory} used by the {@link TranscriptEditor}
 */
public class TranscriptViewFactory implements ViewFactory {

    public static final int LABEL_WIDTH = 150;

    // TODO adjust for graphics dpi - make configurable
    public static final int PAGE_WIDTH = (int) Math.floor(8.5 * 96);

    public TranscriptViewFactory() {
    }

    @Override
    public View create(Element elem) {
        String kind = elem.getName();
        var attrs = elem.getAttributes();

        var componentFactory = attrs.getAttribute(TranscriptStyleConstants.ATTR_KEY_COMPONENT_FACTORY);
        if (componentFactory instanceof ComponentFactory) {
            kind = "componentFactory";
        }

        var isLabel = attrs.getAttribute(TranscriptStyleConstants.ATTR_KEY_LABEL);
        var clickable = attrs.getAttribute(TranscriptStyleConstants.ATTR_KEY_CLICKABLE);
        if(isLabel != null && clickable != null) {
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
            var componentFactory = attrs.getAttribute(TranscriptStyleConstants.ATTR_KEY_COMPONENT_FACTORY);
            if (componentFactory instanceof ComponentFactory factory) {
                return factory.createComponent(attrs);
            }

            return null;
        }
    }

    private class SessionBoxView extends BoxView {

        public SessionBoxView(Element elem, int axis) {
            super(elem, axis);
        }

        @Override
        protected void layoutMinorAxis(int targetSpan, int axis, int[] offsets, int[] spans) {
            super.layoutMinorAxis(PAGE_WIDTH, axis, offsets, spans);
        }

    }

    private class TierLabelView extends LabelView {

        public TierLabelView(Element elem) {
            super(elem);
        }

        @Override
        public float getPreferredSpan(int axis) {
            float span = super.getPreferredSpan(axis);
            if(axis == View.X_AXIS) {
                span = LABEL_WIDTH;
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

        @Override
        public Shape modelToView(int pos, Shape a, Position.Bias b) throws BadLocationException {
            Shape retVal = super.modelToView(pos, a, b);
            int p0 = getStartOffset();
            int p1 = getEndOffset();
            Segment text = getText(p0, p1);
            Font font = getFont();
            Graphics g = getGraphics();
            Rectangle2D textBounds = g.getFontMetrics(font).getStringBounds(text.toString(), g);
            Shape newBounds = new Rectangle2D.Double(a.getBounds().x + a.getBounds().width - textBounds.getWidth(),
                    a.getBounds().y, textBounds.getWidth(), a.getBounds().height);
            Rectangle2D retValBounds = new Rectangle2D.Double(newBounds.getBounds2D().getX() + retVal.getBounds().getX(),
                    newBounds.getBounds2D().getY() + retVal.getBounds().getY(),
                    retVal.getBounds().getWidth(), retVal.getBounds().getHeight());
            return retValBounds;
        }

        @Override
        public int viewToModel(float x, float y, Shape a, Position.Bias[] biasReturn) {
            return super.viewToModel(x, y, a, biasReturn);
        }

        @Override
        public void paint(Graphics g, Shape a) {
            g.clearRect(a.getBounds().x, a.getBounds().y, a.getBounds().width, a.getBounds().height);
            int p0 = getStartOffset();
            int p1 = getEndOffset();
            Segment text = getText(p0, p1);
            Font font = getFont();
            Rectangle2D textBounds = g.getFontMetrics(font).getStringBounds(text.toString(), g);
            // right align text
            Shape newBounds = new Rectangle2D.Double(a.getBounds().x + a.getBounds().width - textBounds.getWidth(),
                    a.getBounds().y, textBounds.getWidth(), a.getBounds().height);
            super.paint(g, newBounds);
        }

    }

    private class TierParagraphView extends ParagraphView {

        private Border border = BorderFactory.createMatteBorder(0, 0, 0, 0, Color.LIGHT_GRAY);

        public TierParagraphView(Element elem) {
            super(elem);
            if(elem.getAttributes().getAttribute(TranscriptStyleConstants.ATTR_KEY_BORDER) instanceof Border b) {
                border = b;
            }
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
            super.paint(g, a);
            border.paintBorder(getContainer(), g, a.getBounds().x, a.getBounds().y, a.getBounds().width, a.getBounds().height);
        }

    }

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