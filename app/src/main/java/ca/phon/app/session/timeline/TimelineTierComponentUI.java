package ca.phon.app.session.timeline;

import ca.phon.media.TimeComponentUI;
import ca.phon.session.IntervalTier;
import ca.phon.ui.action.PhonActionEvent;
import ca.phon.ui.action.PhonUIAction;
import com.github.davidmoten.rtree.RTree;
import com.github.davidmoten.rtree.geometry.Geometries;

import javax.swing.*;
import java.awt.*;

/**
 * UI for timeline tier components.
 *
 */
public class TimelineTierComponentUI extends TimeComponentUI {

    private final static int TOP_BOTTOM_MARGIN = 5;

    private final static int TEXT_MARGIN = 6;

    private final static int TIER_GAP = 5;

    protected RTree<Integer, com.github.davidmoten.rtree.geometry.Rectangle> markerTree;
//    protected RTree<Integer, com.github.davidmoten.rtree.geometry.Rectangle> tierLabelTree;
    protected RTree<Integer, com.github.davidmoten.rtree.geometry.Rectangle> intervalTree;
    protected RTree<Action, com.github.davidmoten.rtree.geometry.Rectangle> actionsTree;

    private JLabel renderer;

    private TimelineTierComponent timeComponent;

    /**
     * Default constructor
     */
    public TimelineTierComponentUI() {
        super();

        markerTree = RTree.create();
//        tierLabelTree = RTree.create();
        intervalTree = RTree.create();
        actionsTree = RTree.create();

        renderer = new JLabel();
        renderer.setOpaque(false);
        renderer.setDoubleBuffered(false);
    }

    @Override
    public void installUI(JComponent c) {
        if(!(c instanceof TimelineTierComponent))
            throw new IllegalArgumentException("c must be a TimelineTierComponent");
        super.installUI(c);

        timeComponent = (TimelineTierComponent)c;
        timeComponent.setDoubleBuffered(true);

        timeComponent.setBackground(UIManager.getColor("IntervalTier.background"));
        timeComponent.setForeground(UIManager.getColor("IntervalTier.foreground"));
        timeComponent.setOpaque(true);
    }

    @Override
    public void uninstallUI(JComponent c) {
        if(!(c instanceof TimelineTierComponent))
            throw new IllegalArgumentException("c must be a TimelineTierComponent");
        super.uninstallUI(c);
    }

    @Override
    public Dimension getPreferredSize(JComponent c) {
        final var prefWidth = super.getPreferredSize(c).getWidth();
        // calculate height based on number of tiers and font size
        final var font = c.getFont();
        renderer.setFont(font.deriveFont(Font.BOLD));
        renderer.setText("X");
        final var tierLblHeight = renderer.getPreferredSize().getHeight();
        final var prefHeight = TOP_BOTTOM_MARGIN
                + tierLblHeight
                + TIER_GAP
                + 2 * font.getSize()
                + TOP_BOTTOM_MARGIN;
        return new Dimension((int)prefWidth, (int)prefHeight);
    }

    private Rectangle getTierLabelRect(String label) {
        renderer.setFont(timeComponent.getFont().deriveFont(Font.BOLD));
        renderer.setText(label);

        int y = TOP_BOTTOM_MARGIN;
        int x = TEXT_MARGIN;
        int width = renderer.getPreferredSize().width;
        int height = renderer.getPreferredSize().height;

        Rectangle rect = new Rectangle(x, y, width, height);
        return rect;
    }

    private void showTierMenu(PhonActionEvent<Rectangle> pae) {
//        final JPopupMenu popup = new JPopupMenu();
//        final JMenuItem item = new JMenuItem("Show menu");
//        item.addActionListener(action);
//        popup.add(item);
//        popup.show(timeComponent, rect.x, rect.y + rect.height);
    }

    @Override
    public void paint(Graphics g, JComponent c) {
        final Graphics2D g2 = (Graphics2D)g;

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);

        // reset trees
        markerTree = RTree.create();
//        tierLabelTree = RTree.create();
        intervalTree = RTree.create();
        actionsTree = RTree.create();

        // fill background if opaque
        if(c.isOpaque()) {
            g2.setColor(c.getBackground());
            g2.fill(g2.getClipBounds());
        }

        final IntervalTier tier = ((TimelineTierComponent)c).getTimelineTier();

        // draw tier name
        final Rectangle labelRect = paintLabel(g2, tier.getName());
        final PhonUIAction<Rectangle> showTierMenuAct =
                PhonUIAction.eventConsumer(this::showTierMenu, labelRect);
        actionsTree.add(showTierMenuAct, Geometries.rectangle(
                labelRect.x,
                labelRect.y,
                labelRect.getMaxX(),
                labelRect.getMaxY()));

        final int tierContentY = (int)(labelRect.getMaxY() + TIER_GAP);
        final int tierContentMaxY = tierContentY + (2 * c.getFont().getSize());
        final Rectangle tierRect = new Rectangle(0, tierContentY, c.getWidth(), tierContentMaxY - tierContentY);

        // draw tier intervals
        for(var interval:tier.getIntervals()) {
            final float startTime = interval.getStart();
            final float endTime = interval.getEnd();

            final TimelineTierComponent tc = (TimelineTierComponent)c;
            final var intervalX = tc.xForTime(startTime);
            final var intervalWidth = tc.xForTime(endTime) - intervalX;

            final Rectangle intervalRect = new Rectangle(
                    (int)intervalX,
                    tierContentY,
                    (int)intervalWidth,
                    tierRect.height);

            // paint text centered in interval with ellipsis if necessary
            final String text = interval.getLabel();
            renderer.setFont(c.getFont());
            renderer.setText(text);
            renderer.setIcon(null);
            renderer.setForeground(c.getForeground());
            renderer.setHorizontalTextPosition(SwingConstants.CENTER);
            renderer.setHorizontalAlignment(SwingConstants.CENTER);
            renderer.setVerticalTextPosition(SwingConstants.CENTER);
            renderer.setVerticalAlignment(SwingConstants.CENTER);
            renderer.setSize(intervalRect.width, intervalRect.height);

            SwingUtilities.paintComponent(g2, renderer, c, intervalRect);

            // draw rounded rectangle for interval
            g2.setColor(UIManager.getColor("IntervalTier.intervalBorder"));
            g2.drawRoundRect(intervalRect.x, intervalRect.y, intervalRect.width, intervalRect.height, 10, 10);
//
//            final int y = (int)(intervalRect.getCenterY() + textHeight / 4);
//            final int textX = (int)(intervalX + (intervalWidth - textWidth) / 2);
//
//            g2.setColor(c.getForeground());
//            g2.drawString(text, textX, y);
        }
    }

    /**
     * Paint tier label at the upper left corner of the visible area
     *
     * @param g2 graphics context
     * @param label tier  label
     *
     */
    protected Rectangle paintLabel(Graphics2D g2, String label) {
        Rectangle labelRect = getTierLabelRect(label);
        labelRect.x += timeComponent.getVisibleRect().x;

        renderer.setHorizontalTextPosition(SwingConstants.RIGHT);
        renderer.setForeground(UIManager.getColor("IntervalTier.foreground"));
        renderer.setFont(timeComponent.getFont().deriveFont(Font.BOLD));
        renderer.setText(label);
        renderer.setSize(labelRect.width, labelRect.height);
        renderer.setHorizontalAlignment(SwingConstants.LEFT);
        renderer.setVerticalAlignment(SwingConstants.CENTER);
        SwingUtilities.paintComponent(g2, renderer, timeComponent, labelRect);

        return labelRect;
    }

}
