package ca.phon.app.session.intervalTiers;

import ca.phon.media.TimeComponentUI;
import ca.phon.media.TimeUIModel;
import ca.phon.session.IntervalTier;
import ca.phon.ui.action.PhonActionEvent;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.util.Tuple;
import com.github.davidmoten.rtree.RTree;
import com.github.davidmoten.rtree.geometry.Geometries;

import javax.swing.*;
import javax.swing.event.MouseInputAdapter;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * UI for timeline tier components.
 *
 */
public class IntervalTierComponentUI extends TimeComponentUI {

    private final static int TOP_BOTTOM_MARGIN = 5;

    private final static int TEXT_MARGIN = 6;

    private final static int TIER_GAP = 5;

    protected RTree<Integer, com.github.davidmoten.rtree.geometry.Rectangle> markerTree;
    protected RTree<Integer, com.github.davidmoten.rtree.geometry.Rectangle> intervalTree;
    protected RTree<Action, com.github.davidmoten.rtree.geometry.Rectangle> actionsTree;

    private JLabel renderer;

    private IntervalTierComponent timeComponent;

    /**
     * Default constructor
     */
    public IntervalTierComponentUI() {
        super();

        markerTree = RTree.create();
        intervalTree = RTree.create();
        actionsTree = RTree.create();

        renderer = new JLabel();
        renderer.setOpaque(false);
        renderer.setDoubleBuffered(false);
    }

    @Override
    public void installUI(JComponent c) {
        if(!(c instanceof IntervalTierComponent))
            throw new IllegalArgumentException("c must be a IntervalTierComponent");
        super.installUI(c);

        timeComponent = (IntervalTierComponent)c;
        timeComponent.setDoubleBuffered(true);

        timeComponent.setBackground(UIManager.getColor("IntervalTier.background"));
        timeComponent.setForeground(UIManager.getColor("IntervalTier.foreground"));
        timeComponent.setOpaque(true);

        timeComponent.addMouseListener(intervalMouseListener);

        timeComponent.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                // repaint selection to show focus rectangle
                timeComponent.repaint();
            }

            @Override
            public void focusLost(FocusEvent e) {
                // repaint selection to hide focus rectangle
                timeComponent.repaint();
            }
        });
    }

    @Override
    public void uninstallUI(JComponent c) {
        if(!(c instanceof IntervalTierComponent))
            throw new IllegalArgumentException("c must be a IntervalTierComponent");
        super.uninstallUI(c);

        timeComponent.removeMouseListener(intervalMouseListener);
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

        final IntervalTier tier = ((IntervalTierComponent)c).getTimelineTier();

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
        int intervalIdx = 0;
        intervalTree = RTree.create();
        for(var interval:tier.getIntervals()) {
            final float startTime = interval.getStart();
            final float endTime = interval.getEnd();

            final IntervalTierComponent tc = (IntervalTierComponent)c;
            final var intervalX = tc.xForTime(startTime);
            final var intervalWidth = tc.xForTime(endTime) - intervalX;

            final Rectangle intervalRect = new Rectangle(
                    (int)intervalX,
                    tierContentY,
                    (int)intervalWidth,
                    tierRect.height);

            // add to interval tree for hit testing
            intervalTree = intervalTree.add(intervalIdx, Geometries.rectangle(
                    intervalRect.x,
                    intervalRect.y,
                    intervalRect.getMaxX(),
                    intervalRect.getMaxY()));

            final boolean selected = tc.getSelectionModel().isSelectedIndex(intervalIdx);
            if(selected) {
                g2.setColor(UIManager.getColor("IntervalTier.selectedBackground"));
            } else {
                g2.setColor(UIManager.getColor("IntervalTier.background"));
            }
            g2.fillRoundRect(intervalRect.x, intervalRect.y, intervalRect.width, intervalRect.height, 10, 10);

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
            if(selected) {
                g2.setColor(UIManager.getColor("IntervalTier.selectedIntervalBorder"));
                g2.drawRoundRect(intervalRect.x, intervalRect.y, intervalRect.width, intervalRect.height, 10, 10);

                // draw focus rectangle
                if(tc.isFocusOwner()) {
                    final Stroke oldStroke = g2.getStroke();
                    final float[] dash = {2f, 2f};
                    g2.setStroke(new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1f, dash, 0f));
                    g2.setColor(UIManager.getColor("text"));
                    g2.drawRoundRect(intervalRect.x+2, intervalRect.y+2, intervalRect.width-4, intervalRect.height-4, 10, 10);
                    g2.setStroke(oldStroke);
                }
            } else {
                g2.setColor(UIManager.getColor("IntervalTier.intervalBorder"));
                g2.drawRoundRect(intervalRect.x, intervalRect.y, intervalRect.width, intervalRect.height, 10, 10);
            }

            ++intervalIdx;
        }

        int markerIdx = 0;
        for(TimeUIModel.Marker marker:timeComponent.getTimeModel().getMarkers()) {
            final double markerX = timeComponent.xForTime(marker.getTime());
            final Rectangle markerRect = new Rectangle((int)markerX - 1, tierContentY, 2, tierRect.height);

            paintMarker(g2, marker);

            // add to marker tree for hit testing
            markerTree = markerTree.add(markerIdx, Geometries.rectangle(
                    markerRect.x,
                    markerRect.y,
                    markerRect.getMaxX(),
                    markerRect.getMaxY()));
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

    /**
     * Mouse listener for interval tier component.  This listener is responsible for handling selection
     * of intervals.
     */
    private final MouseInputAdapter intervalMouseListener = new MouseInputAdapter() {

        @Override
        public void mousePressed(java.awt.event.MouseEvent e) {
            if(!(e.getSource() instanceof IntervalTierComponent)) return;
            final IntervalTierComponent tc = (IntervalTierComponent)e.getSource();
            final Point pt = e.getPoint();

            // search for any possible intervals at given point using interval tree
            final var results = intervalTree.search(Geometries.point(pt.x, pt.y));
            List<Tuple<com.github.davidmoten.rtree.geometry.Rectangle, Integer>> tupleList = new ArrayList<>();
            results
                .map( entry -> new Tuple<>(entry.geometry(), entry.value()))
                .subscribe(tupleList::add);

            if(tupleList.size() == 1) {
                final int selectedIdx = tupleList.get(0).getObj2();
                if(e.getButton() == MouseEvent.BUTTON1) {
                    if(tc.getSelectedIndex() == selectedIdx) {
                        // already selected, do nothing
                    } else {
                        tc.setSelectedIndex(selectedIdx);
                    }
                    if(tc.getIntervalClickedCallback() != null) {
                        final var interval = tc.getTimelineTier().getIntervals().get(selectedIdx);
                        tc.getIntervalClickedCallback().accept(selectedIdx, interval);
                    }
                }
            }

            tc.repaint();
            tc.requestFocus();

            e.consume();
        }

    };

    /**
     * Returns indices of intervals which intersect the given time range.
     *
     * @param startTime start time
     * @param endTime end time
     * @return list of interval indices
     */
    public List<Integer> getIntervalIndicesForTimeRange(float startTime, float endTime) {
        if(!(timeComponent instanceof IntervalTierComponent)) return new ArrayList<>();
        final IntervalTierComponent tc = timeComponent;

        final double startX = tc.xForTime(startTime);
        final double endX = tc.xForTime(endTime);

        final var results = intervalTree.search(Geometries.rectangle(startX, 0, endX, timeComponent.getHeight()));
        List<Integer> idxList = new ArrayList<>();
        results
            .map( entry -> entry.value())
            .subscribe(idxList::add);
        return idxList;
    }

}
