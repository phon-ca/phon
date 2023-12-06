package ca.phon.app.session.editor;

import ca.phon.app.session.editor.view.check.SessionCheckView;
import ca.phon.app.session.editor.view.find_and_replace.FindAndReplaceEditorView;
import ca.phon.app.session.editor.view.ipa_lookup.IPALookupView;
import ca.phon.app.session.editor.view.media_player.MediaPlayerEditorView;
import ca.phon.app.session.editor.view.participants.ParticipantsView;
import ca.phon.app.session.editor.view.speech_analysis.SpeechAnalysisEditorView;
import ca.phon.app.session.editor.view.tier_management.TierOrderingEditorView;
import ca.phon.app.session.editor.view.timeline.TimelineView;
import ca.phon.app.session.editor.view.transcriptEditor.TranscriptView;
import ca.phon.plugin.IPluginExtensionPoint;
import ca.phon.plugin.PluginManager;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import org.jdesktop.swingx.VerticalLayout;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.MouseInputAdapter;
import javax.swing.event.MouseInputListener;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

/**
 * Display icons for views in a vertical strip.  Icons are placed in two positions, top and bottom.
 *
 */
public class ViewIconStrip extends JPanel {

    private final EditorViewModel viewModel;

    public ViewIconStrip(EditorViewModel viewModel) {
        super();
        this.viewModel = viewModel;

        initUI();
    }

    private void initUI() {
        final String cols = "center:pref";
        final String rows = "fill:pref, fill:pref, fill:pref, fill:pref, fill:pref, fill:pref:grow, fill:pref, fill:pref, fill:pref, fill:pref";
        setLayout(new FormLayout(cols, rows));

        CellConstraints cc = new CellConstraints();

        // add in specific order
        add(new ViewIcon(TranscriptView.VIEW_NAME), cc.xy(1, 1));
        add(new ViewIcon(ParticipantsView.VIEW_NAME), cc.xy(1, 2));
        add(new ViewIcon(TierOrderingEditorView.VIEW_NAME), cc.xy(1, 3));
        add(new ViewIcon(MediaPlayerEditorView.VIEW_NAME), cc.xy(1, 4));
        add(new ViewIcon(SpeechAnalysisEditorView.VIEW_NAME), cc.xy(1, 5));
        add(Box.createVerticalGlue(), cc.xy(1, 6));
        add(new ViewIcon(TimelineView.VIEW_NAME), cc.xy(1, 7));
        add(new ViewIcon(SessionCheckView.VIEW_NAME), cc.xy(1, 8));
        add(new ViewIcon(FindAndReplaceEditorView.VIEW_NAME), cc.xy(1, 9));
        add(new ViewIcon(IPALookupView.VIEW_NAME), cc.xy(1, 10));

        viewModel.addEditorViewModelListener(new EditorViewModelListener() {
            @Override
            public void viewShown(String viewName) {
                System.out.println("View shown: " + viewName);
                ViewIconStrip.this.repaint();
            }

            @Override
            public void viewHidden(String viewName) {
                System.out.println("View hidden: " + viewName);
                ViewIconStrip.this.repaint();
            }

            @Override
            public void viewMinimized(String viewName) {

            }

            @Override
            public void viewMaximized(String viewName) {

            }

            @Override
            public void viewNormalized(String viewName) {

            }

            @Override
            public void viewExternalized(String viewName) {

            }

            @Override
            public void viewFocused(String viewName) {

            }
        });
    }

    public ImageIcon getViewIcon(String viewName) {
        for(IPluginExtensionPoint<EditorView> extPt: PluginManager.getInstance().getExtensionPoints(EditorView.class)) {
            final EditorViewInfo pluginAnnotation = extPt.getClass().getAnnotation(EditorViewInfo.class);
            if(pluginAnnotation != null && pluginAnnotation.name().equals(viewName)) {
                final String iconName = pluginAnnotation.icon();
                final String[] iconData = iconName.split(":");
                if(iconData.length == 1) {
                    return IconManager.getInstance().getIcon(iconName, IconSize.SMALL);
                } else {
                    // setup colour as defined by system theme (dark/light)
                    return IconManager.getInstance().buildFontIcon(iconData[0], iconData[1], IconSize.MEDIUM_LARGE, UIManager.getColor(SessionEditorUIProps.VIEW_ICON_COLOR));
                }
            }
        }
        return null;
    }

    private class ViewIcon extends JLabel implements MouseInputListener, MouseMotionListener {

        private final String viewName;

        private boolean pressed = false;

        private boolean mouseInside = false;

        private final Border insetBorder = BorderFactory.createEmptyBorder(5, 10, 5, 0);

        public ViewIcon(String viewName) {
            super();
            setOpaque(false);
            this.viewName = viewName;
            ImageIcon icon = getViewIcon(viewName);
            setIcon(icon);
            setAlignmentX(Component.CENTER_ALIGNMENT);
            setPreferredSize(new Dimension(IconSize.LARGE.getWidth() + 5, IconSize.LARGE.getHeight() + 5));
            // center icon in label
            setHorizontalAlignment(SwingConstants.CENTER);
            setBorder(insetBorder);

            addMouseListener(this);
            addMouseMotionListener(this);
        }

        public String getViewName() {
            return this.viewName;
        }

        @Override
        public void paintComponent(Graphics g) {
//            super.paintComponent(g);
            final Graphics2D g2 = (Graphics2D)g;
            g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // draw icon centered in component, background should be SesssionEditorUIProps.VIEW_ICON_BACKGROUND
            // when view is showing
            final boolean viewShowing = viewModel.isShowing(viewName);

            Color bgColor = (viewShowing ? UIManager.getColor(SessionEditorUIProps.ICON_STRIP_ICON_SELECTED_BACKGROUND) : UIManager.getColor(SessionEditorUIProps.ICON_STRIP_ICON_BACKGROUND));
            if(pressed) {
                bgColor = UIManager.getColor(SessionEditorUIProps.ICON_STRIP_ICON_PRESSED_BACKGROUND);
            }
            final int arc = 15;

            g2.setColor(bgColor);
            g2.fillRoundRect(0, 0, getWidth()-1, getHeight()-1, arc, arc);

            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            getIcon().paintIcon(this, g, (getWidth() - getIcon().getIconWidth()) / 2, (getHeight() - getIcon().getIconHeight()) / 2);

            if(mouseInside) {
                g2.setColor(UIManager.getColor(SessionEditorUIProps.ICON_STRIP_HOVER_COLOR));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, arc, arc);
            }
        }

        @Override
        public void mouseClicked(MouseEvent e) {

        }

        @Override
        public void mousePressed(MouseEvent e) {
            pressed = true;
            repaint();
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            if(pressed) {
                pressed = false;
                if(viewModel.isShowing(viewName)) {
                    viewModel.hideView(viewName);
                } else {
                    viewModel.showView(viewName);
                }
                repaint();
            }
        }

        @Override
        public void mouseEntered(MouseEvent e) {
            mouseInside = true;
            repaint();
        }

        @Override
        public void mouseExited(MouseEvent e) {
            mouseInside = false;
            repaint();
        }

        @Override
        public void mouseDragged(MouseEvent e) {

        }

        @Override
        public void mouseMoved(MouseEvent e) {

        }
    }

}
