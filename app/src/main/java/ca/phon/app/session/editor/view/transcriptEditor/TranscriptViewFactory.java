package ca.phon.app.session.editor.view.transcriptEditor;

import ca.phon.session.Tier;
import ca.phon.ui.ipa.SyllabificationDisplay;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.text.AttributedCharacterIterator;
import java.util.HashMap;
import java.util.Map;

public class TranscriptViewFactory implements ViewFactory {

    private Map<String, Integer> counterMap;

    public TranscriptViewFactory() {
        counterMap = new HashMap<>();
        counterMap.put("label", 0);
        counterMap.put("paragraph", 0);
        counterMap.put("box", 0);
        counterMap.put("component", 0);
        counterMap.put("icon", 0);
        counterMap.put("other", 0);
    }

    @Override
    public View create(Element elem) {
        String kind = elem.getName();

        if (kind != null) {
            if (kind.equals(AbstractDocument.ContentElementName)) {
                incrementCounter("label");
                return new TierView(elem);
            } else if (kind.equals(AbstractDocument.ParagraphElementName)) {
                incrementCounter("paragraph");
                return new ParagraphView(elem);
            } else if (kind.equals(AbstractDocument.SectionElementName)) {
                incrementCounter("box");
                return new BoxView(elem, View.Y_AXIS);
            } else if (kind.equals(StyleConstants.ComponentElementName)) {
                incrementCounter("component");
                var component = StyleConstants.getComponent(elem.getAttributes());
                if (component instanceof JLabel) {
                    JLabel label = ((JLabel) component);
                    return new TierLabelView(elem);
                }
                return new ComponentView(elem);
            } else if (kind.equals(StyleConstants.IconElementName)) {
                incrementCounter("icon");
                return new IconView(elem);
            }
        }

        incrementCounter("other");
        // default to text display
        return new LabelView(elem);
    }

    private void incrementCounter(String key) {
        counterMap.put(key, counterMap.get(key) + 1);
    }

    public Map<String, Integer> getCounterMap() {
        return counterMap;
    }

    private class TierView extends LabelView {
        public TierView(Element elem) {
            super(elem);
        }

//        @Override
//        public void paint(Graphics g, Shape allocation) {
//            float leftIndent = StyleConstants.getLeftIndent(getElement().getAttributes());
//
//            System.out.println("Left Indent: " + leftIndent);
//
////            if (leftIndent == null) {
////                super.paint(g, allocation);
////                return;
////            }
//
//            // Adjust the allocation for the left indent
//            Rectangle newAlloc = new Rectangle(allocation.getBounds());
//            newAlloc.x += leftIndent;
//            newAlloc.width -= leftIndent;
//
//            // Call the super's paint with the adjusted allocation
//            super.paint(g, newAlloc);
//        }
    }

    private class TierLabelView extends ComponentView {
        public TierLabelView(Element elem) {
            super(elem);
        }
    }
}
