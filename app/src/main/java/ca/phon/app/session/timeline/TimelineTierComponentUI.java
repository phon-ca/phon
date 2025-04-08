package ca.phon.app.session.timeline;

import ca.phon.media.TimeComponentUI;
import ca.phon.media.TimeUIModel;
import ca.phon.session.TimelineTier;

import javax.swing.*;
import java.awt.*;

/**
 * UI for timeline tier components.
 *
 */
public class TimelineTierComponentUI extends TimeComponentUI {

    /**
     * Default constructor
     */
    public TimelineTierComponentUI() {
        super();
    }

    @Override
    public void installUI(JComponent c) {
        if(!(c instanceof TimelineTierComponent))
            throw new IllegalArgumentException("c must be a TimelineTierComponent");
        super.installUI(c);
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
        final var prefHeight = font.getSize() * 2;
        return new Dimension((int)prefWidth, prefHeight);
    }

    @Override
    public void paint(Graphics g, JComponent c) {
        final Graphics2D g2 = (Graphics2D)g;

        // fill background if opaque
        if(c.isOpaque()) {
            g.setColor(c.getBackground());
            g.fillRect(0, 0, c.getWidth(), c.getHeight());
        }

        final TimelineTier tier = ((TimelineTierComponent)c).getTimelineTier();

        // draw tier name
        g.setColor(c.getForeground());
        g.drawString(tier.getName(), 2, c.getHeight() - 2);

        // draw tier intervals
        for(var interval:tier.getIntervals()) {
            float startTime = interval.getStart();
            float endTime = interval.getEnd();

            final TimeUIModel.Interval intervalUI = new TimeUIModel.Interval(startTime, endTime);
            paintInterval(g2, intervalUI, false);

            // paint text centered in interval with ellipsis if necessary
            final String text = interval.getLabel();
            final FontMetrics fm = g2.getFontMetrics();
            final int textWidth = fm.stringWidth(text);
            final int textHeight = fm.getHeight();

            final TimelineTierComponent tc = (TimelineTierComponent)c;
            final var intervalX = tc.xForTime(startTime);
            final var intervalWidth = tc.xForTime(endTime) - intervalX;
            final float y = c.getHeight() - textHeight / 2;

            final int textX = (int)(intervalX + (intervalWidth - textWidth) / 2);
            g2.drawString(text, textX, y);
        }
    }
}
