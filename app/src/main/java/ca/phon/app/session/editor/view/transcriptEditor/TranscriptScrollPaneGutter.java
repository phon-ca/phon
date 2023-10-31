package ca.phon.app.session.editor.view.transcriptEditor;

import ca.phon.app.log.LogUtil;
import ca.phon.app.session.editor.EditorEvent;
import ca.phon.app.session.editor.EditorEventManager;
import ca.phon.app.session.editor.EditorEventType;
import ca.phon.session.Record;
import ca.phon.session.Tier;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.ui.fonts.FontPreferences;
import ca.phon.util.Tuple;
import org.jdesktop.swingx.VerticalLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TranscriptScrollPaneGutter extends JComponent {

    private enum IconType {
        ERROR,
        BLIND
    }

    private final TranscriptEditor editor;
    private boolean showRecordNumbers = true;
    private final int DEFAULT_WIDTH = 36;
    private final int RECORD_NUMBER_WIDTH = 24;
    private final int PADDING = 4;
    private int currentRecord;
    private Map<Rectangle, Tuple<Tier<?>, IconType>> iconRects = new HashMap();
    private Rectangle currentIconRect = null;
    private Popup currentHoverPopup = null;
    private JPopupMenu currentHoverMenu = null;

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
            EditorEventType.TierChange,
            this::onTierChanged,
            EditorEventManager.RunOn.AWTEventDispatchThread
        );

        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                for (Rectangle rect : iconRects.keySet()) {
                    if (rect.contains(e.getPoint())) {
                        if (currentIconRect != rect) {
                            currentIconRect = rect;
                            Point mousePos = e.getLocationOnScreen();
                            var hoverRectData = iconRects.get(currentIconRect);
                            IconType iconType = hoverRectData.getObj2();
                            Tier<?> hoverRectTier = hoverRectData.getObj1();
                            switch (iconType) {
                                case ERROR -> {
                                    String errorText = hoverRectTier.getUnvalidatedValue().getParseError().toString();
                                    currentHoverPopup = PopupFactory.getSharedInstance().getPopup(
                                        TranscriptScrollPaneGutter.this,
                                        new JLabel(errorText),
                                        (int) mousePos.getX(),
                                        (int) mousePos.getY()
                                    );
                                    currentHoverPopup.show();
                                }
                                case BLIND -> {
                                    currentHoverMenu = new JPopupMenu();
                                    setupBlindIconToolTip(hoverRectTier);
                                    currentHoverMenu.show(
                                        TranscriptScrollPaneGutter.this,
                                        (int) (currentIconRect.getMaxX()),
                                        (int) (currentIconRect.getY())
                                    );
//                                    setToolTipText("Testing\n something");
                                }
                            }

                        }
                        return;
                    }
                }
                if (currentIconRect != null) {
                    var hoverRectData = iconRects.get(currentIconRect);
                    switch (hoverRectData.getObj2()) {
                        case ERROR -> {
                            currentHoverPopup.hide();
                            currentHoverPopup = null;
                        }
                        case BLIND -> {
                            currentHoverMenu.setVisible(false);
                            currentHoverMenu = null;
                        }
                    }
                    currentIconRect = null;
                    setToolTipText(null);
                }

            }
        });
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (currentIconRect != null && currentIconRect.contains(e.getPoint())) {
                    System.out.println("Clicked on icon");
                    var clickedRectData = iconRects.get(currentIconRect);
                    IconType iconType = clickedRectData.getObj2();
                    switch (iconType) {
                        case BLIND -> {
                            setupBlindIconClickMenu();
                            currentHoverMenu.setVisible(true);
                        }
                    }
                }
            }
        });
        Toolkit.getDefaultToolkit().addAWTEventListener(new AWTEventListener() {
                @Override
                public void eventDispatched(AWTEvent event) {
                    KeyEvent keyEvent = (KeyEvent) event;
                    System.out.println("Does this work?");
                    if (keyEvent.getKeyCode() == KeyEvent.VK_F2) {
                        System.out.println("Woo!");
                    }
                }
            },
            AWTEvent.KEY_EVENT_MASK
        );
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

        iconRects.clear();

        for (int i = 0; i < root.getElementCount(); i++) {
            var elem = root.getElement(i);
            for (int j = 0; j < elem.getElementCount(); j++) {
                try {
                    var innerElem = elem.getElement(j);
                    var innerElemAttrs = innerElem.getAttributes();
                    var elemRect = editor.modelToView2D(innerElem.getStartOffset());
                    if (elemRect == null) continue;

                    if (innerElemAttrs.getAttribute(TranscriptStyleConstants.ATTR_KEY_SEPARATOR) != null) {
                        Record record = (Record) innerElem.getAttributes().getAttribute(TranscriptStyleConstants.ATTR_KEY_RECORD);
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

                    Tier<?> tier = (Tier<?>) innerElemAttrs.getAttribute(TranscriptStyleConstants.ATTR_KEY_TIER);
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
                            iconRects.put(
                                hoverRect,
                                new Tuple<>(tier, IconType.ERROR)
                            );
                        }

                        if (tier.isBlind()) {
                            int x = getWidth() - 48;
                            g.setColor(Color.BLUE);
                            g.drawString("I", x, (int) elemRect.getCenterY());
                            g.setColor(Color.BLACK);
                            if (editor.isTranscriberValidator()) {
                                Rectangle hoverRect = new Rectangle(
                                    x,
                                    (int) elemRect.getCenterY() - fontMetrics.getHeight() / 2,
                                    fontMetrics.stringWidth("I"),
                                    fontMetrics.getHeight() / 2
                                );
                                iconRects.put(
                                    hoverRect,
                                    new Tuple<>(tier, IconType.BLIND)
                                );
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

    public void onRecordChanged(EditorEvent<EditorEventType.RecordChangedData> event) {
        currentRecord = event.data().recordIndex();
        repaint();
    }

    public void onTierChanged(EditorEvent<EditorEventType.TierChangeData> event) {
        if(event.data().valueAdjusting()) return;
        revalidate();
        repaint();
    }

    private void setupBlindIconToolTip(Tier<?> tier) {
        TranscriptDocument doc = editor.getTranscriptDocument();
        currentHoverMenu.removeAll();
        List<String> transcribers = tier.getTranscribers();
        for (String transcriber : transcribers) {
            var blindTranscription = tier.getBlindTranscription(transcriber);
            System.out.println(transcriber + ": " + blindTranscription.toString());
            JMenuItem item = new JMenuItem(transcriber + ": " + doc.getTierText(tier, transcriber));
            item.setEnabled(false);
            currentHoverMenu.add(item);
        }
    }

    private void setupBlindIconClickMenu() {
        currentHoverMenu.removeAll();
        boolean currentValidationMode = editor.isValidationMode();
        JMenuItem toggleValidationMode = new JMenuItem();
        PhonUIAction toggleValidationModeAction = PhonUIAction.runnable(() -> editor.setValidationMode(!currentValidationMode));
        toggleValidationModeAction.putValue(
            PhonUIAction.NAME,
            (currentValidationMode ? "Disable" : "Enable") + " validation mode"
        );
        toggleValidationMode.setAction(toggleValidationModeAction);
        currentHoverMenu.add(toggleValidationMode);
    }

//    @Override
//    public JToolTip createToolTip() {
//        JToolTip toolTip = super.createToolTip();
//        toolTip.setLayout(new VerticalLayout());
//
//        JMenu menu = new JMenu();
//        menu.add(new JMenuItem("testing 1"));
//        menu.add(new JMenuItem("testing 2"));
//
//
//        toolTip.add(menu);
//        toolTip.setPreferredSize(menu.getPreferredSize());
//        return toolTip;
//    }
}
