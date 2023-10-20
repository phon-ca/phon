package ca.phon.app.session.editor.view.transcriptEditor;

import ca.phon.ui.fonts.FontPreferences;
import ca.phon.util.PrefHelper;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.util.HashMap;

public class TranscriptViewFactory implements ViewFactory {
    private HashMap<String, Font> fontCache = new HashMap<>();

    public TranscriptViewFactory() {}

    @Override
    public View create(Element elem) {
        String kind = elem.getName();
        var attrs = elem.getAttributes();

        var componentFactory = attrs.getAttribute(TranscriptDocument.ATTR_KEY_COMPONENT_FACTORY);
        if (componentFactory instanceof ComponentFactory) {
            kind = "componentFactory";
        }

        if (kind != null) {
            if (kind.equals(AbstractDocument.ContentElementName)) {
                var view = new TierView(elem);
                return view;
            } else if (kind.equals(AbstractDocument.ParagraphElementName)) {
                return new ParagraphView(elem);
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

    private class TierView extends LabelView {

        public TierView(Element elem) {
            super(elem);
        }

        @Override
        public Font getFont() {
            Font superFont = super.getFont();
            return superFont;
//            String fontString = superFont.toString();
//
//            Font derivedFont;
//
//            if (fontCache.containsKey(fontString)) {
//                derivedFont = fontCache.get(fontString);
//            }
//            else {
//                derivedFont = superFont.deriveFont(superFont.getSize() + PrefHelper.getUserPreferences().getFloat(TranscriptView.FONT_SIZE_DELTA_PROP, 0));
//                fontCache.put(fontString, derivedFont);
//            }
//
//            return derivedFont;
        }
    }

    private class ComponentFactoryView extends ComponentView {
        public ComponentFactoryView(Element elem) {
            super(elem);
        }

        @Override
        protected Component createComponent() {
            AttributeSet attrs = getAttributes();
            var componentFactory = attrs.getAttribute(TranscriptDocument.ATTR_KEY_COMPONENT_FACTORY);
            if (componentFactory instanceof ComponentFactory factory) {
                return factory.createComponent(attrs);
            }

            return null;
        }
    }
}