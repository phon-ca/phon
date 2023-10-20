package ca.phon.app.session.editor.view.transcriptEditor;

import ca.phon.app.log.LogUtil;
import ca.phon.app.session.editor.EditorEvent;
import ca.phon.app.session.editor.EditorEventManager;
import ca.phon.app.session.editor.EditorEventType;
import ca.phon.session.Record;
import ca.phon.session.Tier;
import ca.phon.ui.fonts.FontPreferences;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;

public class TranscriptScrollPaneGutter extends JComponent {
    private final TranscriptEditor editor;
    private boolean showRecordNumbers = true;
    private final int DEFAULT_WIDTH = 36;
    private final int RECORD_NUMBER_WIDTH = 24;
    private final int PADDING = 4;
    private int currentRecord;
    private Map<Rectangle, String> hoverRects = new HashMap();
    private Rectangle currentHoverRect = null;
    private Popup currentHoverPopup = null;

    public TranscriptScrollPaneGutter(TranscriptEditor editor) {
        this.editor = editor;
        setPreferredSize(new Dimension(DEFAULT_WIDTH + PADDING + RECORD_NUMBER_WIDTH + PADDING, getPreferredSize().height));
        setFont(FontPreferences.getTierFont());

        currentRecord = editor.getTranscriptDocument().getSingleRecordIndex();
        editor.getEventManager().registerActionForEvent(
            EditorEventType.RecordChanged,
            this::onRecordChanged,
            EditorEventManager.RunOn.AWTEventDispatchThread
        );
        editor.getEventManager().registerActionForEvent(
            EditorEventType.TierChanged,
            this::onTierChanged,
            EditorEventManager.RunOn.AWTEventDispatchThread
        );

        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                for (Rectangle rect : hoverRects.keySet()) {
                    if (rect.contains(e.getPoint())) {
                        if (currentHoverRect != rect) {
                            currentHoverRect = rect;
                            Point mousePos = e.getLocationOnScreen();
                            currentHoverPopup = PopupFactory.getSharedInstance().getPopup(
                                TranscriptScrollPaneGutter.this,
                                new JLabel(hoverRects.get(rect)),
                                (int) mousePos.getX(),
                                (int) mousePos.getY()
                            );
                            currentHoverPopup.show();
                        }
                        return;
                    }
                }
                if (currentHoverRect != null) {
                    currentHoverRect = null;
                    currentHoverPopup.hide();
                    currentHoverPopup = null;
                }

            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        final Graphics2D g2 = (Graphics2D)g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);

        Rectangle drawHere = g.getClipBounds();

        // Fill clipping area with dirty brown/orange.
        g.setColor(Color.decode("#eeeeee"));
        g.fillRect(drawHere.x, drawHere.y, drawHere.width, drawHere.height);
        g.setColor(Color.BLACK);

        final Font font = g.getFont();

        var doc = editor.getTranscriptDocument();

        var root = doc.getDefaultRootElement();

        int currentSepHeight = -1;

        hoverRects.clear();

        for (int i = 0; i < root.getElementCount(); i++) {
            var elem = root.getElement(i);
            for (int j = 0; j < elem.getElementCount(); j++) {
                try {
                    var innerElem = elem.getElement(j);
                    var innerElemAttrs = innerElem.getAttributes();
                    var elemRect = editor.modelToView2D(innerElem.getStartOffset());
                    if (elemRect == null) continue;

                    if (innerElemAttrs.getAttribute(TranscriptDocument.ATTR_KEY_SEPARATOR) != null) {
                        Record record = (Record) innerElem.getAttributes().getAttribute(TranscriptDocument.ATTR_KEY_RECORD);
                        if (showRecordNumbers && record != null) {
                            int recordNumber = editor.getSession().getRecordPosition(record);

                            var sepRect = editor.modelToView2D(innerElem.getStartOffset());

                            String recordNumberText = String.valueOf(recordNumber + 1);

                            var fontMetrics = g.getFontMetrics();
                            int stringWidth = fontMetrics.stringWidth(recordNumberText);
                            int stringBaselineHeight = (int)(sepRect.getCenterY() + 0.8f * (fontMetrics.getFont().getSize() / 2.0f));

                            if (stringBaselineHeight <= currentSepHeight) continue;

                            if (recordNumber == currentRecord) {
                                g.setFont(font.deriveFont(Font.BOLD));
                            }

                            g.drawString(recordNumberText, getWidth() - stringWidth - PADDING, stringBaselineHeight);
                            currentSepHeight = stringBaselineHeight;

                            if (recordNumber == currentRecord) {
                                g.setFont(font);
                            }
                        }

                    }

                    FontMetrics fontMetrics = getFontMetrics(g.getFont());

                    Tier<?> tier = (Tier<?>) innerElemAttrs.getAttribute(TranscriptDocument.ATTR_KEY_TIER);
                    if (tier != null) {
//                        boolean locked = innerElemAttrs.getAttribute("locked") != null;
//                        if (locked) {
//                            var lockIcon = IconManager.getInstance().getIcon("actions/lock", IconSize.XSMALL);
//                            Image lockImage = lockIcon.getImage();
//                            g.drawImage(lockImage, getWidth() - 16, (int) elemRect.getMinY() + 1, null);
//                        }

                        if (tier.isUnvalidated()) {
                            Rectangle hoverRect = new Rectangle(getWidth() - 32, (int) elemRect.getCenterY() - fontMetrics.getHeight() / 2, fontMetrics.stringWidth("O"), fontMetrics.getHeight() / 2);
//                            g.setColor(Color.BLUE);
//                            g.fillRect(hoverRect.x, hoverRect.y, hoverRect.width, hoverRect.height);
                            g.setColor(Color.RED);
                            g.drawString("O", getWidth() - 32, (int) elemRect.getCenterY());
                            g.setColor(Color.BLACK);
                            hoverRects.put(
                                hoverRect,
                                tier.getUnvalidatedValue().getParseError().toString()
                            );
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

    public void onRecordChanged(EditorEvent<EditorEventType.RecordChangedData> event) {
        currentRecord = event.data().recordIndex();
        repaint();
    }

    public void onTierChanged(EditorEvent<EditorEventType.TierChangeData> event) {
        revalidate();
        repaint();
    }
}
