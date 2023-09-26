package ca.phon.app.session.editor.view.transcriptEditor;

import ca.phon.app.log.LogUtil;
import ca.phon.plugin.IPluginExtensionFactory;
import ca.phon.plugin.IPluginExtensionPoint;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.ui.theme.UIDefaults;
import ca.phon.ui.theme.UIDefaultsHandler;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;
import org.jdesktop.swingx.HorizontalLayout;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;

public class CalloutWindow extends JDialog implements UIDefaultsHandler, IPluginExtensionPoint<UIDefaultsHandler> {
    private final static int TRIANGLE_HEIGHT = 20;
    private final static int PADDING = 8;
    private Component content;
    private Shape shape;
    private Point relativeArrowPoint = null;

    public CalloutWindow(JFrame frame, Component content, int sideOfWindow, int topMiddleBottom, Point pointAtPos) {
        super(frame, false);
        this.content = content;
        setBackground(UIManager.getColor("CalloutWindow.background"));
        init(frame, sideOfWindow, topMiddleBottom, pointAtPos);
        // Set the location of the dialog relative to the frame

    }

    private void init(JFrame frame, int sideOfWindow, int topMiddleBottom, Point pointAtPos) {
        int topOfRect = sideOfWindow == SwingConstants.NORTH ? TRIANGLE_HEIGHT : 0;
        int bottomOfRect = sideOfWindow == SwingConstants.SOUTH ? TRIANGLE_HEIGHT : 0;
        int leftOfRect = sideOfWindow == SwingConstants.WEST ? TRIANGLE_HEIGHT : 0;
        int rightOfRect = sideOfWindow == SwingConstants.EAST ? TRIANGLE_HEIGHT : 0;

        Dimension d = content.getPreferredSize();

        // region Close button and panel

        JPanel closePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        closePanel.setBorder(new EmptyBorder(topOfRect, 0, 0, rightOfRect));
        JButton closeButton = new JButton();
        PhonUIAction<Void> closeAct = PhonUIAction.eventConsumer((e) -> dispose(), null);
        final ImageIcon closeIcon =
                IconManager.getInstance().getIcon("actions/button_cancel", IconSize.XSMALL);
        closeAct.putValue(PhonUIAction.SMALL_ICON, closeIcon);
        closeButton.setAction(closeAct);
        closeButton.setPreferredSize(new Dimension(12, 12));
        closeButton.setBorder(null);
        closeButton.setOpaque(false);
        closePanel.add(closeButton);

        // endregion Close button and panel

        shape = createShape(
            (int) (d.getWidth() + (PADDING * 2)),
            (int) (d.getHeight() + closePanel.getPreferredSize().getHeight() + PADDING),
            20,
            TRIANGLE_HEIGHT,
            4,
            sideOfWindow,
            topMiddleBottom
        );
        setUndecorated(true);
        setResizable(true);
        setLocationRelativeTo(null);
        setShape(shape);
        setSize(
            (int) (d.getWidth() + (PADDING * 2) + leftOfRect + rightOfRect),
            (int) (d.getHeight() + PADDING + topOfRect + bottomOfRect + closePanel.getPreferredSize().getHeight())
        );
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel innerPanel = new JPanel(new BorderLayout());
        innerPanel.setSize(d);
        innerPanel.setBorder(new EmptyBorder(
            0,
            PADDING + leftOfRect,
            PADDING + bottomOfRect,
            PADDING + rightOfRect
        ));
        innerPanel.add(content, BorderLayout.CENTER);
        add(innerPanel, BorderLayout.CENTER);


        add(closePanel, BorderLayout.NORTH);


        innerPanel.setOpaque(false);
        closePanel.setOpaque(false);

        addWindowFocusListener(new WindowAdapter() {
            @Override
            public void windowLostFocus(WindowEvent e) {
                dispose();
            }
        });
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (!contains(e.getLocationOnScreen())) {
                    dispose();
                }
            }
        });

        System.out.println(relativeArrowPoint);
        System.out.println(pointAtPos);

        if (pointAtPos != null) {
            setLocation(pointAtPos.x - relativeArrowPoint.x, pointAtPos.y - relativeArrowPoint.y);
        }
    }


    @Override
    public void paint(Graphics g) {
        super.paint(g);
//        Graphics2D g2d = (Graphics2D) g;
//        g2d.setColor(Color.BLACK);
//        Stroke oldStroke = g2d.getStroke();
//        g2d.setStroke(new BasicStroke(2)); // Set border thickness
//        g2d.draw(shape); // Draw the border
//        g2d.setStroke(oldStroke);
    }

    public static void showCallout(JFrame owner, Component content, int sideOfWindow, int topMiddleBottom, Point pointAtPos) {
        // Create a custom JDialog
        CalloutWindow dialog = new CalloutWindow(owner, content, sideOfWindow, topMiddleBottom, pointAtPos);
        dialog.setVisible(true);
    }

    private Shape createShape(int width, int height, int triangleBase, int triangleHeight, int cornerRadius, int sideOfWindow, int topMiddleBottom) {
        // Create a Path2D object
        Path2D.Double shape = new Path2D.Double();

        int topOfRect = sideOfWindow == SwingConstants.NORTH ? triangleHeight : 0;
        int leftOfRect = sideOfWindow == SwingConstants.WEST ? triangleHeight : 0;

        int horiOffset = 0;
        if (topMiddleBottom == SwingConstants.LEADING) {
            horiOffset = -(((width - triangleBase) / 2) - cornerRadius);
        }
        else if (topMiddleBottom == SwingConstants.TRAILING) {
            horiOffset = (((width - triangleBase) / 2) - cornerRadius);
        }

        int vertOffset = 0;
        if (topMiddleBottom == SwingConstants.LEADING) {
            vertOffset = -(((height - triangleBase) / 2) - cornerRadius);
        }
        else if (topMiddleBottom == SwingConstants.TRAILING) {
            vertOffset = (((height - triangleBase) / 2) - cornerRadius);
        }

        // Start the path in the top-left corner of the rectangle, considering the corner radius
        shape.moveTo(leftOfRect + cornerRadius, topOfRect);

        // Top
        if (sideOfWindow == SwingConstants.NORTH) {
            System.out.println("Top");
            shape.lineTo(horiOffset + (width + triangleBase) / 2, topOfRect);
            shape.lineTo(horiOffset + (width / 2), 0);
            relativeArrowPoint = new Point(horiOffset + (width / 2), 0);
            shape.lineTo(horiOffset + (width - triangleBase) / 2, topOfRect);
        }
        shape.lineTo(leftOfRect+ width - cornerRadius, topOfRect);

        // Top right corner
        shape.quadTo(leftOfRect+width - cornerRadius, topOfRect, width + leftOfRect, topOfRect + cornerRadius);

        // Right
        if (sideOfWindow == SwingConstants.EAST) {
            System.out.println("Right");
            shape.lineTo(width, vertOffset + (height - triangleBase) / 2);
            shape.lineTo(width + triangleHeight, vertOffset + height / 2);
            relativeArrowPoint = new Point(width + triangleHeight, vertOffset + height / 2);
            shape.lineTo(width, vertOffset + (height + triangleBase) / 2);
        }
        shape.lineTo(leftOfRect + width, height - cornerRadius + topOfRect);

        // Bottom right corner
        shape.quadTo(leftOfRect + width, topOfRect + height, width - cornerRadius + leftOfRect, height + topOfRect);

        // Bottom
        if (sideOfWindow == SwingConstants.SOUTH) {
            System.out.println("Bottom");
            shape.lineTo(leftOfRect + horiOffset + (width + triangleBase) / 2, height);
            shape.lineTo(leftOfRect + horiOffset + (width / 2), height + triangleHeight);
            relativeArrowPoint = new Point(leftOfRect + horiOffset + (width / 2), height + triangleHeight);
            shape.lineTo(leftOfRect + horiOffset + (width - triangleBase) / 2, height);
        }
        shape.lineTo(leftOfRect + cornerRadius, topOfRect + height);

        // Bottom left corner
        shape.quadTo(leftOfRect, topOfRect + height, leftOfRect, topOfRect + height - cornerRadius);

        // Left
        if (sideOfWindow == SwingConstants.WEST) {
            shape.lineTo(triangleHeight, vertOffset + (height + triangleBase) / 2);
            shape.lineTo(0, vertOffset + height / 2);
            relativeArrowPoint = new Point(0, vertOffset + height / 2);
            shape.lineTo(triangleHeight, vertOffset + (height - triangleBase) / 2);
        }
        shape.lineTo(leftOfRect, topOfRect + cornerRadius);

        // Top left corner
        shape.quadTo(leftOfRect, topOfRect + cornerRadius, cornerRadius + leftOfRect, topOfRect);

        // Close path and return
        shape.closePath();
        return shape;
    }

    @Override
    public Class<?> getExtensionType() {
        return UIDefaultsHandler.class;
    }

    @Override
    public IPluginExtensionFactory<UIDefaultsHandler> getFactory() {
        return args -> this;
    }

    @Override
    public void setupDefaults(UIDefaults defaults) {
        defaults.put("CalloutWindow.background", UIManager.getColor("List.background"));
    }
}
