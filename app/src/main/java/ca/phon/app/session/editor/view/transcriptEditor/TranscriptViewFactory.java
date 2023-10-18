package ca.phon.app.session.editor.view.transcriptEditor;

import ca.phon.session.TierViewItem;
import ca.phon.ui.fonts.FontPreferences;
import ca.phon.util.PrefHelper;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.util.HashMap;

public class TranscriptViewFactory implements ViewFactory {
    private HashMap<String, Font> fontCache = new HashMap<>();
    private int indent = -1;
    private int rightInset = -1;

    public TranscriptViewFactory() {}

    @Override
    public View create(Element elem) {
        String kind = elem.getName();
        var attrs = elem.getAttributes();

        var componentFactory = attrs.getAttribute("componentFactory");
        if (componentFactory instanceof ComponentFactory) {
            kind = "componentFactory";
        }

        if (kind != null) {
            if (kind.equals(AbstractDocument.ContentElementName)) {
                var view = new TierView(elem);
                return view;
            } else if (kind.equals(AbstractDocument.ParagraphElementName)) {
                if (indent == -1) setupIndent((Integer) elem.getAttributes().getAttribute("labelColumnWidth"));
                return new CustomParagraphView(elem);
            } else if (kind.equals(AbstractDocument.SectionElementName)) {
                return new BoxView(elem, View.Y_AXIS);
            } else if (kind.equals(StyleConstants.ComponentElementName)) {
                return new ComponentView(elem);
            } else if (kind.equals(StyleConstants.IconElementName)) {
                return new IconView(elem);
            } else if (kind.equals("componentFactory")) {
                return new ComponentFactoryView(elem);
            }
        }

        // default to text display
        return new LabelView(elem);
    }

    private void setupIndent(Integer labelColumnWidth) {
        if (labelColumnWidth == null) labelColumnWidth = 20;

        Font font = FontPreferences.getMonospaceFont().deriveFont(14.0f);

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < labelColumnWidth + 2; i++) {
            builder.append(" ");
        }

        FontMetrics fm = new JLabel().getFontMetrics(font);

        indent = (short) fm.stringWidth(builder.toString());
        rightInset = (short) fm.stringWidth(" ");
    }

    private class TierView extends LabelView {

        public TierView(Element elem) {
            super(elem);
        }

        @Override
        public Font getFont() {
            Font superFont = super.getFont();
            String fontString = superFont.toString();

            Font derivedFont;

            if (fontCache.containsKey(fontString)) {
                derivedFont = fontCache.get(fontString);
            }
            else {
                derivedFont = superFont.deriveFont(superFont.getSize() + PrefHelper.getUserPreferences().getFloat(TranscriptView.FONT_SIZE_DELTA_PROP, 0));
                fontCache.put(fontString, derivedFont);
            }

            return derivedFont;
        }
    }

    class CustomParagraphView extends ParagraphView {
        public CustomParagraphView(Element elem) {
            super(elem);
            setInsets((short) 0, (short) indent, (short) 0, (short) rightInset);
            setFirstLineIndent(-indent);
        }
    }

    private class ComponentFactoryView extends ComponentView {
        public ComponentFactoryView(Element elem) {
            super(elem);
        }

        @Override
        protected Component createComponent() {
            AttributeSet attrs = getAttributes();
            var componentFactory = attrs.getAttribute("componentFactory");
            if (componentFactory instanceof ComponentFactory factory) {
                return factory.createComponent(attrs);
            }

            return null;
        }
    }
}