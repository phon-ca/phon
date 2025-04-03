package ca.phon.app.session.timeline;

import ca.phon.media.TimeComponentUI;

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
        return super.getPreferredSize(c);
    }

    @Override
    public Dimension getMinimumSize(JComponent c) {
        return super.getMinimumSize(c);
    }

    @Override
    public Dimension getMaximumSize(JComponent c) {
        return super.getMaximumSize(c);
    }

    @Override
    public void paint(Graphics g, JComponent c) {
        super.paint(g, c);
    }
}
