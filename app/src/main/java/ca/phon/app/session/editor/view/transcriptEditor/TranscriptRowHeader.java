package ca.phon.app.session.editor.view.transcriptEditor;

import ca.phon.app.log.LogUtil;
import ca.phon.ui.fonts.FontPreferences;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;

import javax.swing.*;
import javax.swing.text.StyleConstants;
import java.awt.*;

public class TranscriptRowHeader extends JComponent {
    private final TranscriptEditor editor;
    private boolean showRecordNumbers = true;
    private final int DEFAULT_WIDTH = 36;
    private final int RECORD_NUMBER_WIDTH = 24;
    private final int PADDING = 4;

    public TranscriptRowHeader(TranscriptEditor editor) {
        this.editor = editor;
        setPreferredSize(new Dimension(DEFAULT_WIDTH + PADDING + RECORD_NUMBER_WIDTH + PADDING, getPreferredSize().height));
        setFont(FontPreferences.getTierFont());
    }

    @Override
    protected void paintComponent(Graphics g) {
        Rectangle drawHere = g.getClipBounds();

        // Fill clipping area with dirty brown/orange.
        g.setColor(Color.decode("#eeeeee"));
        g.fillRect(drawHere.x, drawHere.y, drawHere.width, drawHere.height);
        g.setColor(Color.BLACK);

        var doc = editor.getTranscriptDocument();

        var root = doc.getDefaultRootElement();

        for (int i = 0; i < root.getElementCount(); i++) {
            var elem = root.getElement(i);
            for (int j = 0; j < elem.getElementCount(); j++) {
                try {
                    var innerElem = elem.getElement(j);
                    var innerElemAttrs = innerElem.getAttributes();
                    var elemRect = editor.modelToView2D(innerElem.getStartOffset());
                    if (elemRect == null) return;
                    boolean topVisible = elemRect.getMinY() > drawHere.getMinY() && elemRect.getMinY() < drawHere.getMaxY();
                    boolean bottomVisible = elemRect.getMaxY() > drawHere.getMinY() && elemRect.getMaxY() < drawHere.getMaxY();
                    if (topVisible || bottomVisible) {
                        JComponent component = (JComponent)StyleConstants.getComponent(innerElemAttrs);
                        if (component != null) {
                            if (component instanceof JLabel) {
                                var tierLabelRect = editor.modelToView2D(innerElem.getStartOffset());

                                var locked = (Boolean) innerElemAttrs.getAttribute("locked");
                                if (locked != null && locked) {
                                    var lockIcon = IconManager.getInstance().getIcon("actions/lock", IconSize.XSMALL);
                                    Image lockImage = lockIcon.getImage();
                                    g.drawImage(lockImage, getWidth() - 16, (int) tierLabelRect.getMinY() + 1, null);
                                }

                                if (false) {
                                    g.setColor(Color.RED);
                                    g.drawString("◉", getWidth() - 32, (int) tierLabelRect.getMaxY() - 3);
                                    g.setColor(Color.BLACK);
                                }
                            }
                        }
                        if (innerElemAttrs.getAttribute("sep") != null) {
                            Integer recordNumber = (Integer) innerElem.getAttributes().getAttribute("recordIndex");
                            if (showRecordNumbers && recordNumber != null) {
                                var sepRect = editor.modelToView2D(innerElem.getStartOffset());

                                String recordNumberText = String.valueOf(recordNumber + 1);
                                var fontMetrics = g.getFontMetrics();
                                int stringWidth = fontMetrics.stringWidth(recordNumberText);
                                int stringBaselineHeight = (int)(sepRect.getCenterY() + 0.8f * (fontMetrics.getFont().getSize() / 2.0f));

                                g.drawString(recordNumberText, getWidth() - stringWidth - PADDING, stringBaselineHeight);
                            }

                        }
                    }
                }
                catch (Exception e) {
                    LogUtil.severe(e);
                }
            }
        }
    }

    public boolean getShowRecordNumbers() {
        return showRecordNumbers;
    }

    public void setShowRecordNumbers(boolean show) {
        this.showRecordNumbers = show;
        int newWidth = show ? DEFAULT_WIDTH + PADDING + RECORD_NUMBER_WIDTH + PADDING: DEFAULT_WIDTH + PADDING;
        setPreferredSize(new Dimension(newWidth, getPreferredSize().height));
        revalidate();
        repaint();
    }
}
