package ca.phon.app.session.editor.view.timeline;

import ca.phon.media.*;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Rectangle2D;

public class TimelineTitledSeparator extends TimeComponent {

    private final static int LABEL_MARGIN = 0;
    private final static int LABEL_PADDING = 5;

    private String title;

    private Icon icon;

    private int horizontalLabelPosition;

    private Color lineColor;

    private int lineWidth;

    private JLabel renderer;

    public TimelineTitledSeparator(TimeUIModel timeModel, String title, Icon icon, int horizontalLabelPosition, Color lineColor, int lineWidth) {
        super(timeModel);

        this.renderer = new JLabel(title);
        this.renderer.setOpaque(false);
        this.renderer.setDoubleBuffered(false);

        this.title = title;
        this.icon = icon;
        this.horizontalLabelPosition = horizontalLabelPosition;
        this.lineColor = lineColor;
        this.lineWidth = lineWidth;

        updateUI();
    }

    public void updateUI() {
        setUI(new TimelineSeparatorUI());
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        var prev = this.title;
        this.title = title;
        firePropertyChange("title", prev, title);
    }

    public Icon getIcon() {
        return icon;
    }

    public void setIcon(Icon icon) {
        var prev = this.icon;
        this.icon = icon;
        firePropertyChange("icon", prev, icon);
    }

    public int getHorizontalLabelPosition() {
        return horizontalLabelPosition;
    }

    public void setHorizontalLabelPosition(int horizontalLabelPosition) {
        var prev = this.horizontalLabelPosition;
        this.horizontalLabelPosition = horizontalLabelPosition;
        firePropertyChange("horizontalLabelPosition", prev, horizontalLabelPosition);
    }

    public Color getLineColor() {
        return lineColor;
    }

    public void setLineColor(Color lineColor) {
        var prev = this.lineColor;
        this.lineColor = lineColor;
        firePropertyChange("lineColor", prev, lineColor);
    }

    public int getLineWidth() {
        return lineWidth;
    }

    public void setLineWidth(int lineWidth) {
        var prev = this.lineWidth;
        this.lineWidth = lineWidth;
        firePropertyChange("lineWidth", prev, lineWidth);
    }

    public Rectangle getLabelRect() {
        Dimension labelSize = renderer.getPreferredSize();
        Rectangle labelBounds = new Rectangle(
                getVisibleRect().x + LABEL_MARGIN, 0, labelSize.width, labelSize.height);
        return labelBounds;
    }

    private class TimelineSeparatorUI extends TimeComponentUI {

        public TimelineSeparatorUI() {
            super();
        }

        @Override
        public void paint(Graphics g, JComponent c) {
            TimelineTitledSeparator separator = getSeparator();
            Rectangle2D bounds = g.getClipBounds();

            // background
            if(separator.isOpaque()) {
                g.setColor(separator.getBackground());
                g.fillRect(0, 0, (int) bounds.getWidth(), (int) bounds.getHeight());
            }

            renderer.setIcon(separator.getIcon());
            renderer.setText(separator.getTitle());
            renderer.setFont(separator.getFont());
            renderer.setForeground(separator.getForeground());

            Dimension labelSize = renderer.getPreferredSize();
            Rectangle labelBounds = getLabelRect();

            switch (separator.getHorizontalLabelPosition()) {
                case SwingConstants.LEFT:
                case SwingConstants.LEADING:
                    break;

                case SwingConstants.CENTER:
                    labelBounds.setRect(separator.getVisibleRect().getCenterX()-labelSize.width/2, 0, labelSize.width, labelSize.height);
                    break;

                case SwingConstants.RIGHT:
                case SwingConstants.TRAILING:
                    labelBounds.setRect(separator.getVisibleRect().getMaxX() - labelSize.width - LABEL_MARGIN, 0, labelSize.width, labelSize.height);
                    break;

                default:
                    break;
            }

            SwingUtilities.paintComponent(g, renderer, separator, labelBounds);
        }

        private TimelineTitledSeparator getSeparator() {
            return (TimelineTitledSeparator)getTimeComponent();
        }

        @Override
        public Dimension getPreferredSize(JComponent c) {
            Dimension retVal = super.getPreferredSize(c);

            TimelineTitledSeparator sep = (TimelineTitledSeparator)getTimeComponent();

            int maxHeight = sep.getLineWidth();

            if(sep.getIcon() != null) {
                maxHeight = Math.max(maxHeight, sep.getIcon().getIconHeight());
            }

            if(sep.getTitle() != null && sep.getTitle().trim().length() > 0) {
                Font f = sep.getFont();
                FontMetrics fm = sep.getFontMetrics(f);
                Rectangle2D stringBounds = f.getStringBounds(sep.getTitle(), fm.getFontRenderContext());
                maxHeight = Math.max(maxHeight, (int)Math.ceil(stringBounds.getHeight()));
            }

            retVal.height = maxHeight;

            return retVal;
        }

    }

}
