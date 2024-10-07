package ca.phon.app.session.editor.view.transcript;

import ca.phon.app.log.LogUtil;
import ca.phon.util.PrefHelper;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;

/**
 * The {@link Highlighter.HighlightPainter} that paints "box selection"
 */
public class BoxSelectHighlightPainter implements Highlighter.HighlightPainter {

    @Override
    public void paint(Graphics g, int p0, int p1, Shape bounds, JTextComponent c) {
        if (c instanceof TranscriptEditor editor) {

            Graphics2D g2d = (Graphics2D) g;
            g2d.setColor(UIManager.getColor(TranscriptEditorUIProps.SEGMENT_SELECTION));

            try {
                var p0Rect = editor.modelToView2D(p0);
                var p1Rect = editor.modelToView2D(p1);

                Element ele = editor.getTranscriptDocument().getCharacterElement(p0);
                int actualLineHeight = g.getFontMetrics().getHeight();
                if (ele != null) {
                    final AttributeSet attrs = ele.getAttributes();
                    if (StyleConstants.getFontFamily(attrs) != null && StyleConstants.getFontSize(attrs) > 0) {
                        int style = (StyleConstants.isBold(attrs) ? Font.BOLD : 0) | (StyleConstants.isItalic(attrs) ? Font.ITALIC : 0);
                        int fontSizeDelta = PrefHelper.getFloat(TranscriptView.FONT_SIZE_DELTA_PROP, 0.0f).intValue();
                        final Font f = new Font(StyleConstants.getFontFamily(attrs), style, StyleConstants.getFontSize(attrs) + fontSizeDelta);
                        actualLineHeight = g.getFontMetrics(f).getHeight();
                    }
                }

                g2d.drawRect((int) p0Rect.getMinX(), (int) p0Rect.getMinY(), (int) (p1Rect.getMaxX() - p0Rect.getMinX()) - 1, actualLineHeight - 1);
            } catch (BadLocationException e) {
                LogUtil.warning(e);
            }
        }
    }
}
