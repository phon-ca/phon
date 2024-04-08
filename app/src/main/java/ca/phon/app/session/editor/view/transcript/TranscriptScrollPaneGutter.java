package ca.phon.app.session.editor.view.transcript;

import ca.phon.app.log.LogUtil;
import ca.phon.app.session.editor.EditorEvent;
import ca.phon.app.session.editor.EditorEventManager;
import ca.phon.app.session.editor.EditorEventType;
import ca.phon.app.session.editor.view.transcript.extensions.BlindTranscriptionExtension;
import ca.phon.session.Record;
import ca.phon.session.Tier;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.ui.fonts.FontPreferences;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;

import javax.swing.*;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Rectangle2D;
import java.lang.annotation.ElementType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The gutter that is shown on the left of the {@link TranscriptScrollPane}
 * */
public class TranscriptScrollPaneGutter extends JComponent {

    private enum IconType {
        ERROR,
        BLIND
    }

    private final TranscriptEditor editor;
    private boolean showRecordNumbers = true;
    private final int DEFAULT_WIDTH = 20;
    /**
     * The width of the record numbers (in pixels)
     * */
    private final int RECORD_NUMBER_WIDTH = 24;
    private final int PADDING = 4;
    private int currentRecord;

    /**
     * A map of rectangles in screen-space to tuples of tiers and {@link IconType}
     * */
    private final Map<Rectangle, TierAndIconType> iconRects = new HashMap<>();
    /**
     * A reference to the icon rect that the cursor is currently hovering over
     * */
    private Rectangle currentIconRect = null;
    /**
     * A reference to the current hover popup
     * */
    private Popup currentHoverPopup = null;
    /**
     * A reference to the current hover menu
     * */
    private JPopupMenu currentHoverMenu = null;

    /**
     * Constructor
     *
     * @param editor a reference to the transcript editor
     * */
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

//        addMouseMotionListener(new MouseAdapter() {
//            @Override
//            public void mouseMoved(MouseEvent e) {
//                for (Rectangle rect : iconRects.keySet()) {
//                    if (rect.contains(e.getPoint())) {
//                        if (currentIconRect != rect) {
//                            currentIconRect = rect;
//                            Point mousePos = e.getLocationOnScreen();
//                            var hoverRectData = iconRects.get(currentIconRect);
//                            IconType iconType = hoverRectData.iconType();
//                            Tier<?> hoverRectTier = hoverRectData.tier();
//                            switch (iconType) {
//                                case ERROR -> {
//                                    String errorText = hoverRectTier.getUnvalidatedValue().getParseError().toString();
//                                    currentHoverPopup = PopupFactory.getSharedInstance().getPopup(
//                                        TranscriptScrollPaneGutter.this,
//                                        new JLabel(errorText),
//                                        (int) mousePos.getX(),
//                                        (int) mousePos.getY()
//                                    );
//                                    currentHoverPopup.show();
//                                }
//                                case BLIND -> {
//                                    currentHoverMenu = new JPopupMenu();
//                                    setupBlindIconToolTip(hoverRectTier);
//                                    currentHoverMenu.show(
//                                        TranscriptScrollPaneGutter.this,
//                                        (int) (currentIconRect.getMaxX()),
//                                        (int) (currentIconRect.getY())
//                                    );
//                                    setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
//                                }
//                            }
//
//                        }
//                        return;
//                    }
//                }
//                if (currentIconRect != null) {
//                    var hoverRectData = iconRects.get(currentIconRect);
//                    switch (hoverRectData.iconType()) {
//                        case ERROR -> {
//                            currentHoverPopup.hide();
//                            currentHoverPopup = null;
//                        }
//                        case BLIND -> {
//                            currentHoverMenu.setVisible(false);
//                            currentHoverMenu = null;
//                            setCursor(Cursor.getDefaultCursor());
//                        }
//                    }
//                    currentIconRect = null;
//                    setToolTipText(null);
//                }
//
//            }
//        });
//        addMouseListener(new MouseAdapter() {
//            @Override
//            public void mouseClicked(MouseEvent e) {
//                if (currentIconRect != null && currentIconRect.contains(e.getPoint())) {
//                    var clickedRectData = iconRects.get(currentIconRect);
//                    IconType iconType = clickedRectData.iconType();
//                    switch (iconType) {
//                        case BLIND -> {
//                            setupBlindIconClickMenu();
//                            currentHoverMenu.setVisible(true);
//                        }
//                    }
//                }
//            }
//        });
    }

    // region Icons

    private ImageIcon commentIcon;
    private ImageIcon getCommentIcon() {
        if(commentIcon == null) {
            commentIcon = IconManager.getInstance().getFontIcon(IconManager.GoogleMaterialDesignIconsFontName,
                    "comment", IconSize.SMALL, Color.darkGray);
        }
        return commentIcon;
    }

    private ImageIcon gemIcon;
    private ImageIcon getGemIcon() {
        if(gemIcon == null) {
            gemIcon = IconManager.getInstance().getFontIcon(IconManager.GoogleMaterialDesignIconsFontName,
                    "diamond", IconSize.SMALL, Color.darkGray);
        }
        return gemIcon;
    }

    private ImageIcon errorIcon;
    private ImageIcon getErrorIcon() {
        if(errorIcon == null) {
            errorIcon = IconManager.getInstance().getFontIcon(IconManager.GoogleMaterialDesignIconsFontName,
                    "error", IconSize.SMALL, Color.RED);
        }
        return errorIcon;
    }

    // endregion

    @Override
    protected void paintComponent(Graphics g) {
        final Graphics2D g2 = (Graphics2D)g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);

        Rectangle drawHere = g.getClipBounds();

        // Fill clipping area with dirty brown/orange.
        g.setColor(UIManager.getColor("Button.background"));
        g.fillRect(drawHere.x, drawHere.y, drawHere.width, drawHere.height);
        g.setColor(Color.BLACK);

        final Font font = g.getFont();

        var doc = editor.getTranscriptDocument();

        var root = doc.getDefaultRootElement();

        int currentSepHeight = -1;

        iconRects.clear();

        for (int i = 0; i < root.getElementCount(); i++) {
            var elem = root.getElement(i);
            if(elem.getElementCount() == 0) continue;
            var innerElem = elem.getElement(0);

            final AttributeSet attrs = innerElem.getAttributes();

            final String elementType = TranscriptStyleConstants.getElementType(attrs);
            if(elementType == null) continue;

            Rectangle2D elemRect = null;
            try {
                elemRect = editor.modelToView2D(innerElem.getStartOffset());
            } catch (BadLocationException e) {
                continue;
            }

            if(TranscriptStyleConstants.ELEMENT_TYPE_GENERIC.equals(elementType)) {
                final Tier<?> genericTier = TranscriptStyleConstants.getGenericTier(attrs);
                if(genericTier != null && genericTier.isUnvalidated()) {
                    final ImageIcon icon = getErrorIcon();
                    final int iconWidth = icon.getIconWidth();
                    final int iconHeight = icon.getIconHeight();
                    final int x = getWidth() - iconWidth - PADDING;
                    final int y = (int)elemRect.getCenterY() - iconHeight/2;
                    icon.paintIcon(this, g, x, y);
                }
            } else if(TranscriptStyleConstants.ELEMENT_TYPE_COMMENT.equals(elementType)) {
                final ImageIcon icon = getCommentIcon();
                final int iconWidth = icon.getIconWidth();
                final int iconHeight = icon.getIconHeight();
                final int x = getWidth() - iconWidth - PADDING;
                final int y = (int)elemRect.getCenterY() - iconHeight/2;
                icon.paintIcon(this, g, x, y);
            } else if(TranscriptStyleConstants.ELEMENT_TYPE_GEM.equals(elementType)) {
                final ImageIcon icon = getGemIcon();
                final int iconWidth = icon.getIconWidth();
                final int iconHeight = icon.getIconHeight();
                final int x = getWidth() - iconWidth - PADDING;
                final int y = (int)elemRect.getCenterY() - iconHeight/2;
                icon.paintIcon(this, g, x, y);
            } else if(TranscriptStyleConstants.ELEMENT_TYPE_RECORD.equals(elementType)) {
                final String firstTier = editor.getSession().getTierView().get(0).getTierName();
                final Tier<?> tier = TranscriptStyleConstants.getTier(attrs);
                final Record record = TranscriptStyleConstants.getRecord(attrs);
                if(record != null && tier != null) {
                    if(tier.getName().equals(firstTier)) {
                        final int recordNumber = editor.getSession().getRecordPosition(record);
                        final String recordNumberText = String.valueOf(recordNumber + 1);

                        final FontMetrics fontMetrics = g.getFontMetrics();
                        final int stringWidth = fontMetrics.stringWidth(recordNumberText);
                        final int stringBaselineHeight = (int) (elemRect.getCenterY() + 0.8f * (fontMetrics.getFont().getSize() / 2.0f));

                        if (stringBaselineHeight <= currentSepHeight) continue;

                        g.setColor(Color.darkGray);
                        if (recordNumber == currentRecord) {
                            g.setFont(font.deriveFont(Font.BOLD));
                            g.setColor(Color.BLACK);
                        }

                        g.drawString(recordNumberText, PADDING, stringBaselineHeight);
                        currentSepHeight = stringBaselineHeight;

                        if (recordNumber == currentRecord) {
                            g.setFont(font);
                        }
                    }

                    if(tier.isUnvalidated()) {
                        final ImageIcon icon = getErrorIcon();
                        final int iconWidth = icon.getIconWidth();
                        final int iconHeight = icon.getIconHeight();
                        final int x = getWidth() - iconWidth - PADDING;
                        final int y = (int)elemRect.getCenterY() - iconHeight/2;
                        icon.paintIcon(this, g, x, y);
                    }
                }
            }

//            for (int j = 0; j < elem.getElementCount(); j++) {
//                try {
//                    var innerElem = elem.getElement(j);
//                    var innerElemAttrs = innerElem.getAttributes();
//                    var elemRect = editor.modelToView2D(innerElem.getStartOffset());
//                    if (elemRect == null) continue;
//
//                    if (innerElemAttrs.getAttribute(TranscriptStyleConstants.ATTR_KEY_SEPARATOR) != null) {
//                        Record record = (Record) innerElem.getAttributes().getAttribute(TranscriptStyleConstants.ATTR_KEY_RECORD);
//                        if (showRecordNumbers && record != null) {
//                            int recordNumber = editor.getSession().getRecordPosition(record);
//
//                            var sepRect = editor.modelToView2D(innerElem.getStartOffset());
//
//                            String recordNumberText = String.valueOf(recordNumber + 1);
//
//                            var fontMetrics = g.getFontMetrics();
//                            int stringWidth = fontMetrics.stringWidth(recordNumberText);
//                            int stringBaselineHeight = (int)(sepRect.getCenterY() + 0.8f * (fontMetrics.getFont().getSize() / 2.0f));
//
//                            if (stringBaselineHeight <= currentSepHeight) continue;
//
//                            if (recordNumber == currentRecord) {
//                                g.setFont(font.deriveFont(Font.BOLD));
//                            }
//
//                            g.drawString(recordNumberText, getWidth() - stringWidth - PADDING, stringBaselineHeight);
//                            currentSepHeight = stringBaselineHeight;
//
//                            if (recordNumber == currentRecord) {
//                                g.setFont(font);
//                            }
//                        }
//
//                    }
//
//                    FontMetrics fontMetrics = getFontMetrics(g.getFont());
//
//                    Tier<?> tier = (Tier<?>) innerElemAttrs.getAttribute(TranscriptStyleConstants.ATTR_KEY_TIER);
//                    if (tier != null) {
////                        boolean locked = innerElemAttrs.getAttribute("locked") != null;
////                        if (locked) {
////                            var lockIcon = IconManager.getInstance().getIcon("actions/lock", IconSize.XSMALL);
////                            Image lockImage = lockIcon.getImage();
////                            g.drawImage(lockImage, getWidth() - 16, (int) elemRect.getMinY() + 1, null);
////                        }
//
//                        if (tier.isUnvalidated()) {
//                            Rectangle hoverRect = new Rectangle(getWidth() - 32, (int) elemRect.getCenterY() - fontMetrics.getHeight() / 2, fontMetrics.stringWidth("O"), fontMetrics.getHeight() / 2);
////                            g.setColor(Color.BLUE);
////                            g.fillRect(hoverRect.x, hoverRect.y, hoverRect.width, hoverRect.height);
//                            g.setColor(Color.RED);
//                            g.drawString("O", getWidth() - 32, (int) elemRect.getCenterY());
//                            g.setColor(Color.BLACK);
//                            iconRects.put(
//                                hoverRect,
//                                new TierAndIconType(tier, IconType.ERROR)
//                            );
//                        }
//
//                        if (tier.isBlind()) {
//                            int x = getWidth() - 48;
//                            g.setColor(Color.BLUE);
//                            g.drawString("I", x, (int) elemRect.getCenterY());
//                            g.setColor(Color.BLACK);
//                            if (editor.isTranscriberValidator()) {
//                                Rectangle hoverRect = new Rectangle(
//                                    x,
//                                    (int) elemRect.getCenterY() - fontMetrics.getHeight() / 2,
//                                    fontMetrics.stringWidth("I"),
//                                    fontMetrics.getHeight() / 2
//                                );
//                                iconRects.put(
//                                    hoverRect,
//                                    new TierAndIconType(tier, IconType.BLIND)
//                                );
//                            }
//
//                        }
//                    }
//                }
//                catch (Exception e) {
//                    LogUtil.severe(e);
//                }
//            }
        }
    }

    public boolean getShowRecordNumbers() {
        return showRecordNumbers;
    }

    /**
     * Set whether the record numbers are showing, and resizes the gutter accordingly
     * */
    public void setShowRecordNumbers(boolean show) {
        this.showRecordNumbers = show;
        int newWidth = show ? DEFAULT_WIDTH + PADDING + RECORD_NUMBER_WIDTH + PADDING : DEFAULT_WIDTH + PADDING;
        setPreferredSize(new Dimension(newWidth, getPreferredSize().height));
        revalidate();
        repaint();
    }

    /**
     * Runs when the current record is changed.
     * Repaints the gutter with the new current record.
     * */
    public void onRecordChanged(EditorEvent<EditorEventType.RecordChangedData> event) {
        currentRecord = event.data().recordIndex();
        repaint();
    }

    /**
     * Runs when there are changes to tier data.
     * Repaints te gutter.
     * */
    public void onTierChanged(EditorEvent<EditorEventType.TierChangeData> event) {
        if(event.data().valueAdjusting()) return;
        revalidate();
        repaint();
    }

    /**
     * Sets up the tooltip for the blind tier icon
     *
     * @param tier the tier that the icon is referencing
     * */
    private void setupBlindIconToolTip(Tier<?> tier) {
        TranscriptDocument doc = editor.getTranscriptDocument();
        currentHoverMenu.removeAll();
        List<String> transcribers = tier.getTranscribers();
        for (String transcriber : transcribers) {
            var blindTranscription = tier.getBlindTranscription(transcriber);
            JMenuItem item = new JMenuItem(transcriber + ": " + doc.getTierText(tier, transcriber));
            item.setEnabled(false);
            currentHoverMenu.add(item);
        }
    }

    /**
     * Sets up the menu that appears when the user clicks the blind tier icon
     * */
    private void setupBlindIconClickMenu() {
        TranscriptDocument doc = editor.getTranscriptDocument();

        currentHoverMenu.removeAll();
        boolean currentValidationMode = (boolean) doc.getDocumentPropertyOrDefault(
            BlindTranscriptionExtension.VALIDATION_MODE,
            BlindTranscriptionExtension.VALIDATION_MODE_DEFAULT
        );
        JMenuItem toggleValidationMode = new JMenuItem();
        PhonUIAction<Void> toggleValidationModeAction = PhonUIAction.runnable(() -> doc.putDocumentProperty(
            BlindTranscriptionExtension.VALIDATION_MODE,
            !currentValidationMode
        ));
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

    private record TierAndIconType(Tier<?> tier, IconType iconType) {}
}
