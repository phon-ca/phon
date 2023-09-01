package ca.phon.app.session.editor.view.transcriptEditor;

import ca.phon.app.log.LogUtil;
import ca.phon.ui.PhonGuiConstants;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;

import javax.swing.*;
import javax.swing.text.StyleConstants;
import java.awt.*;
import java.awt.geom.Rectangle2D;

public class TranscriptRowHeader extends JComponent {
    private final TranscriptEditor editor;
    private boolean showRecordNumbers = true;
    private final int DEFAULT_WIDTH = 36;
    private final int RECORD_NUMBER_WIDTH = 24;

    public TranscriptRowHeader(TranscriptEditor editor) {
        this.editor = editor;
        setPreferredSize(new Dimension(DEFAULT_WIDTH + RECORD_NUMBER_WIDTH, editor.getPreferredSize().height));
        System.out.println(editor.getPreferredSize().height);
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
                    var elemRect = editor.modelToView2D(innerElem.getStartOffset());
                    if (elemRect == null) return;
                    boolean topVisible = elemRect.getMinY() > drawHere.getMinY() && elemRect.getMinY() < drawHere.getMaxY();
                    boolean bottomVisible = elemRect.getMaxY() > drawHere.getMinY() && elemRect.getMaxY() < drawHere.getMaxY();
                    if (topVisible || bottomVisible) {
                        JComponent component = (JComponent)StyleConstants.getComponent(innerElem.getAttributes());
                        if (component != null) {
                            if (component instanceof JLabel) {
                                var tierLabelRect = editor.modelToView2D(innerElem.getStartOffset());

                                var locked = (Boolean)innerElem.getAttributes().getAttribute("locked");
                                if (locked != null && locked) {
                                    var lockIcon = IconManager.getInstance().getIcon("actions/lock", IconSize.XSMALL);
                                    Image lockImage = lockIcon.getImage();
                                    g.drawImage(lockImage, getWidth()-16, (int)tierLabelRect.getMinY()+1, null);
                                }

                                if (false) {
                                    g.setColor(Color.RED);
                                    g.drawString("◉", getWidth()-32, (int)tierLabelRect.getMaxY()-3);
                                    g.setColor(Color.BLACK);
                                }
                            }
                            else if (component instanceof JPanel) {
                                Integer recordNumber = (Integer) innerElem.getAttributes().getAttribute("recordIndex");
                                if (recordNumber != null) {
                                    var sepRect = editor.modelToView2D(innerElem.getStartOffset());
                                    String recordNumberText = String.valueOf(recordNumber);
                                    int stringWidth = g.getFontMetrics().stringWidth(recordNumberText);

                                    g.drawString(recordNumberText, getWidth() - stringWidth, (int) sepRect.getMaxY());
                                }
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

    public boolean isShowRecordNumbers() {
        return showRecordNumbers;
    }

    public void setShowRecordNumbers(boolean showRecordNumbers) {
        this.showRecordNumbers = showRecordNumbers;
    }

    public void setHeight(int height) {
        setSize(new Dimension(getWidth(), height));
    }
}
