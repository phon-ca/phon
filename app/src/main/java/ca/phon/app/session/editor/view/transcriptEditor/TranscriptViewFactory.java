package ca.phon.app.session.editor.view.transcriptEditor;

import ca.phon.app.session.editor.EditorEvent;
import ca.phon.app.session.editor.EditorEventManager;

import javax.swing.*;
import javax.swing.text.*;

public class TranscriptViewFactory implements ViewFactory {

    public TranscriptViewFactory() {

    }

    @Override
    public View create(Element elem) {
        String kind = elem.getName();
        var attrs = elem.getAttributes();

        var componentFactory = attrs.getAttribute("componentFactory");
        if (componentFactory instanceof ComponentFactory factory) {
            if (StyleConstants.getComponent(attrs) == null) {
                var component = factory.createComponent(attrs);
                if (attrs instanceof MutableAttributeSet mutableAttrs) {
                    System.out.println(Thread.currentThread().getName());
                    StyleConstants.setComponent(mutableAttrs, component);
                }
                else {
                    System.out.println(attrs);
                }
            }
            kind = StyleConstants.ComponentElementName;
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
}