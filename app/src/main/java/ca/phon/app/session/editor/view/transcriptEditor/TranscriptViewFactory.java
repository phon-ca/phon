package ca.phon.app.session.editor.view.transcriptEditor;

import javax.swing.text.*;
import java.awt.*;
import java.util.HashMap;

/**
 * The {@link ViewFactory} used by the {@link TranscriptEditor}
 * */
public class TranscriptViewFactory implements ViewFactory {

    public TranscriptViewFactory() {}

    @Override
    public View create(Element elem) {
        String kind = elem.getName();
        var attrs = elem.getAttributes();

        var componentFactory = attrs.getAttribute(TranscriptStyleConstants.ATTR_KEY_COMPONENT_FACTORY);
        if (componentFactory instanceof ComponentFactory) {
            kind = "componentFactory";
        }

        if (kind != null) {
            switch (kind) {
                case AbstractDocument.ContentElementName -> {
                    return new LabelView(elem);
                }
                case AbstractDocument.ParagraphElementName -> {
                    return new ParagraphView(elem);
                }
                case AbstractDocument.SectionElementName -> {
                    return new BoxView(elem, View.Y_AXIS);
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
     * */
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
}