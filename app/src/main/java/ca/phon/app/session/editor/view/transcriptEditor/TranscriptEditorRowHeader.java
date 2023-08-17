package ca.phon.app.session.editor.view.transcriptEditor;

import ca.phon.ui.PhonGuiConstants;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyleConstants;
import java.awt.*;

public class TranscriptEditorRowHeader extends JComponent {

    private final TranscriptEditor editor;

    public TranscriptEditorRowHeader(TranscriptEditor editor) {
        this.editor = editor;
        setPreferredSize(new Dimension(150, editor.getPreferredSize().height));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Rectangle drawHere = g.getClipBounds();

        // Fill clipping area with dirty brown/orange.
        g.setColor(PhonGuiConstants.PHON_UI_STRIP_COLOR);
        g.fillRect(drawHere.x, drawHere.y, drawHere.width, drawHere.height);

        g.setColor(Color.BLACK);

        var doc = editor.getTranscriptDocument();

        var root = doc.getDefaultRootElement();

        for (int i = 0; i < root.getElementCount(); i++) {
            var elem = root.getElement(i);
            for (int j = 0; j < elem.getElementCount(); j++) {
                var innerElem = elem.getElement(j);
                JComponent component = (JComponent)StyleConstants.getComponent(innerElem.getAttributes());
                if (component != null) {
                    /*if (component instanceof JSeparator) {
                        try {
                            var sepRect = editor.modelToView2D(innerElem.getStartOffset());
                            Integer recordIndex = (Integer)innerElem.getAttributes().getAttribute("recordIndex");
                            if (recordIndex != null) {
                                String text = "Record " + (recordIndex + 1);
                                int stringWidth = g.getFontMetrics().stringWidth(text);
                                //System.out.println(g.getFontMetrics().getHeight());
                                //System.out.println(sepRect.getHeight());
                                g.drawString(text, getWidth()-stringWidth-40, ((int) sepRect.getMaxY())-2);
                            }
                        } catch (BadLocationException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    else*/ if (component instanceof JLabel) {
                        try {
                            var tierLabelRect = editor.modelToView2D(innerElem.getStartOffset());

                            var locked = (Boolean)innerElem.getAttributes().getAttribute("locked");
                            if (locked != null && locked) {
                                var lockIcon = IconManager.getInstance().getIcon("actions/lock", IconSize.XSMALL);
                                Image lockImage = lockIcon.getImage();
                                //System.out.println(lockImage.getHeight(null));
                                //System.out.println(tierLabelRect.getHeight());
                                g.drawImage(lockImage, getWidth()-16, (int)tierLabelRect.getMinY()+1, null);
                            }

                            if (false) {
                                g.setColor(Color.RED);
                                g.drawString("◉", getWidth()-32, (int)tierLabelRect.getMaxY()-3);
                                g.setColor(Color.BLACK);
                            }
                        } catch (BadLocationException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
        }
    }
}
