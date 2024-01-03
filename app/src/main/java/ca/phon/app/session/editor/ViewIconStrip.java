package ca.phon.app.session.editor;

import ca.phon.app.session.editor.view.check.SessionCheckView;
import ca.phon.app.session.editor.view.ipaDictionary.IPADictionaryView;
import ca.phon.app.session.editor.view.media_player.MediaPlayerEditorView;
import ca.phon.app.session.editor.view.participants.ParticipantsView;
import ca.phon.app.session.editor.view.speech_analysis.SpeechAnalysisEditorView;
import ca.phon.app.session.editor.view.tier_management.TierOrderingEditorView;
import ca.phon.app.session.editor.view.timeline.TimelineView;
import ca.phon.app.session.editor.view.transcriptEditor.TranscriptView;
import ca.phon.plugin.IPluginExtensionPoint;
import ca.phon.plugin.PluginManager;
import ca.phon.ui.FlatButton;
import ca.phon.ui.IconStrip;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.ui.fonts.FontPreferences;
import ca.phon.ui.theme.UIDefaults;
import ca.phon.ui.theme.UIDefaultsHandler;
import ca.phon.util.icons.IconSize;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Display icons for views in a vertical strip.  Icons are placed in two positions, top and bottom.
 *
 */
public class ViewIconStrip extends IconStrip {

    private final EditorViewModel viewModel;

    private Map<String, FlatButton> viewButtons = new HashMap<>();

    public ViewIconStrip(EditorViewModel viewModel) {
        super(SwingConstants.VERTICAL);
        this.viewModel = viewModel;
        initButtons();
    }

    protected void initButtons() {
        viewButtons.put(TranscriptView.VIEW_NAME, createViewButton(TranscriptView.VIEW_NAME));
        viewButtons.put(ParticipantsView.VIEW_NAME, createViewButton(ParticipantsView.VIEW_NAME));
        viewButtons.put(TierOrderingEditorView.VIEW_NAME, createViewButton(TierOrderingEditorView.VIEW_NAME));
        viewButtons.put(MediaPlayerEditorView.VIEW_NAME, createViewButton(MediaPlayerEditorView.VIEW_NAME));
        viewButtons.put(SpeechAnalysisEditorView.VIEW_NAME, createViewButton(SpeechAnalysisEditorView.VIEW_NAME));
        viewButtons.put(TimelineView.VIEW_NAME, createViewButton(TimelineView.VIEW_NAME));
        viewButtons.put(SessionCheckView.VIEW_NAME, createViewButton(SessionCheckView.VIEW_NAME));
        viewButtons.put(IPADictionaryView.VIEW_NAME, createViewButton(IPADictionaryView.VIEW_NAME));

        add(viewButtons.get(TranscriptView.VIEW_NAME), IconStripPosition.LEFT);
        add(viewButtons.get(ParticipantsView.VIEW_NAME), IconStripPosition.LEFT);
        add(viewButtons.get(TierOrderingEditorView.VIEW_NAME), IconStripPosition.LEFT);
        add(viewButtons.get(MediaPlayerEditorView.VIEW_NAME), IconStripPosition.LEFT);
        add(viewButtons.get(SpeechAnalysisEditorView.VIEW_NAME), IconStripPosition.LEFT);
        add(viewButtons.get(TimelineView.VIEW_NAME), IconStripPosition.RIGHT);
        add(viewButtons.get(SessionCheckView.VIEW_NAME), IconStripPosition.RIGHT);
        add(viewButtons.get(IPADictionaryView.VIEW_NAME), IconStripPosition.RIGHT);

        viewModel.addEditorViewModelListener(new EditorViewModelListener() {
            @Override
            public void viewShown(String viewName) {
                System.out.println("View shown: " + viewName);
                if(viewButtons.containsKey(viewName)) {
                    viewButtons.get(viewName).setSelected(true);
                }
                ViewIconStrip.this.repaint();
            }

            @Override
            public void viewHidden(String viewName) {
                System.out.println("View hidden: " + viewName);
                if(viewButtons.containsKey(viewName)) {
                    viewButtons.get(viewName).setSelected(false);
                }
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

    private record IconData(String fontName, String iconName) {}
    public IconData getViewIcon(String viewName) {
        for(IPluginExtensionPoint<EditorView> extPt: PluginManager.getInstance().getExtensionPoints(EditorView.class)) {
            final EditorViewInfo pluginAnnotation = extPt.getClass().getAnnotation(EditorViewInfo.class);
            if(pluginAnnotation != null && pluginAnnotation.name().equals(viewName)) {
                final String iconName = pluginAnnotation.icon();
                final String[] iconData = iconName.split(":");
                if(iconData.length == 1) {
                    return new IconData(null, iconData[0]);
                } else {
                    // setup colour as defined by system theme (dark/light)
                    return new IconData(iconData[0], iconData[1]);
                }
            }
        }
        return null;
    }

    public FlatButton createViewButton(String viewName) {
        final IconData iconData = getViewIcon(viewName);
        final Action showHideAct = PhonUIAction.runnable(() -> {
            if(viewModel.isShowing(viewName)) {
                viewModel.hideView(viewName);
            } else {
                viewModel.showView(viewName);
            }
        });
        showHideAct.putValue(FlatButton.ICON_SIZE_PROP, IconSize.LARGE);
        showHideAct.putValue(FlatButton.ICON_FONT_NAME_PROP, iconData.fontName());
        showHideAct.putValue(FlatButton.ICON_NAME_PROP, iconData.iconName());
        final FlatButton retVal = createButton(showHideAct);
        retVal.setSelected(viewModel.isShowing(viewName));
        retVal.setPopupText(viewName);
        retVal.setPopupLocation(SwingConstants.EAST);
        return retVal;
    }

}
