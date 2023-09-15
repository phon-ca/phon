package ca.phon.app.session.editor.view.transcriptEditor;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;

public class TranscriptViewFactory implements ViewFactory {

    public TranscriptViewFactory() {

    }

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
                return new TierView(elem);
            } else if (kind.equals(AbstractDocument.ParagraphElementName)) {
                return new ParagraphView(elem);
            } else if (kind.equals(AbstractDocument.SectionElementName)) {
                return new BoxView(elem, View.Y_AXIS);
            } else if (kind.equals(StyleConstants.ComponentElementName)) {
                var component = StyleConstants.getComponent(elem.getAttributes());
                if (component instanceof JLabel) {
                    JLabel label = ((JLabel) component);
                    return new TierLabelView(elem);
                }
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
    }

    private class TierLabelView extends ComponentView {
        public TierLabelView(Element elem) {
            super(elem);
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