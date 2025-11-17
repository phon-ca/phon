package ca.phon.app.session.editor;

import ca.phon.app.session.editor.undo.ShowHideViewEdit;
import ca.phon.app.session.editor.view.check.SessionCheckView;
import ca.phon.app.session.editor.view.ipaDictionary.IPADictionaryView;
import ca.phon.app.session.editor.view.mediaPlayer.MediaPlayerEditorView;
import ca.phon.app.session.editor.view.participants.ParticipantsView;
import ca.phon.app.session.editor.view.search.SearchView;
import ca.phon.app.session.editor.view.speechAnalysis.SpeechAnalysisEditorView;
import ca.phon.app.session.editor.view.syllabificationAlignment.SyllabificationAlignmentEditorView;
import ca.phon.app.session.editor.view.tierManagement.TierManagementView;
import ca.phon.app.session.editor.view.timeline.TimelineView;
import ca.phon.app.session.editor.view.transcript.TranscriptView;
import ca.phon.plugin.IPluginExtensionPoint;
import ca.phon.plugin.PluginManager;
import ca.phon.session.Session;
import ca.phon.ui.FlatButton;
import ca.phon.ui.IconStrip;
import ca.phon.ui.action.PhonActionEvent;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;

import javax.swing.*;
import javax.swing.undo.UndoableEditSupport;
import java.util.HashMap;
import java.util.Map;

/**
 * Display icons for views in a vertical strip.  Icons are placed in two positions, top and bottom.
 *
 */
public class ViewIconStrip extends IconStrip {

    private int side = SwingConstants.LEFT;

    private final Session session;

    private final UndoableEditSupport undoSupport;

    private final EditorEventManager editorEventManager;

    private final EditorViewModel viewModel;

    private Map<String, FlatButton> viewButtons = new HashMap<>();

    public ViewIconStrip(Session session, EditorEventManager editorEventManager, UndoableEditSupport undoableEditSupport, EditorViewModel viewModel) {
        this(SwingConstants.LEFT, session, editorEventManager, undoableEditSupport, viewModel);
    }

    public ViewIconStrip(int side, Session session, EditorEventManager editorEventManager, UndoableEditSupport undoableEditSupport, EditorViewModel viewModel) {
        super(SwingConstants.VERTICAL);
        this.viewModel = viewModel;
        this.side = side;
        this.editorEventManager = editorEventManager;
        this.undoSupport= undoableEditSupport;
        this.session = session;
        initButtons();
    }

    protected void initButtons() {
        if(side == SwingConstants.LEFT) {
            viewButtons.put(TranscriptView.VIEW_NAME, createViewButton(TranscriptView.VIEW_NAME));
            viewButtons.put(ParticipantsView.VIEW_NAME, createViewButton(ParticipantsView.VIEW_NAME));
            viewButtons.put(TierManagementView.VIEW_NAME, createViewButton(TierManagementView.VIEW_NAME));
            viewButtons.put(MediaPlayerEditorView.VIEW_NAME, createViewButton(MediaPlayerEditorView.VIEW_NAME));
            viewButtons.put(SpeechAnalysisEditorView.VIEW_NAME, createViewButton(SpeechAnalysisEditorView.VIEW_NAME));
            viewButtons.put(TimelineView.VIEW_NAME, createViewButton(TimelineView.VIEW_NAME));
            viewButtons.put(SessionCheckView.VIEW_NAME, createViewButton(SessionCheckView.VIEW_NAME));
            viewButtons.put(IPADictionaryView.VIEW_NAME, createViewButton(IPADictionaryView.VIEW_NAME));
            final FlatButton moreButton = createLayoutButton();

            add(viewButtons.get(TranscriptView.VIEW_NAME), IconStripPosition.LEFT);
            add(viewButtons.get(ParticipantsView.VIEW_NAME), IconStripPosition.LEFT);
            add(viewButtons.get(TierManagementView.VIEW_NAME), IconStripPosition.LEFT);
            add(viewButtons.get(MediaPlayerEditorView.VIEW_NAME), IconStripPosition.LEFT);
            add(viewButtons.get(SpeechAnalysisEditorView.VIEW_NAME), IconStripPosition.LEFT);
            add(moreButton, IconStripPosition.LEFT);
            add(viewButtons.get(TimelineView.VIEW_NAME), IconStripPosition.RIGHT);
            add(viewButtons.get(SessionCheckView.VIEW_NAME), IconStripPosition.RIGHT);
        } else {
            viewButtons.put(SearchView.VIEW_NAME, createViewButton(SearchView.VIEW_NAME));
            viewButtons.put(IPADictionaryView.VIEW_NAME, createViewButton(IPADictionaryView.VIEW_NAME));
            viewButtons.put(SyllabificationAlignmentEditorView.VIEW_NAME, createViewButton(SyllabificationAlignmentEditorView.VIEW_NAME));

            add(viewButtons.get(SearchView.VIEW_NAME), IconStripPosition.LEFT);
            add(viewButtons.get(IPADictionaryView.VIEW_NAME), IconStripPosition.LEFT);
            add(viewButtons.get(SyllabificationAlignmentEditorView.VIEW_NAME), IconStripPosition.LEFT);

            var pluginViews = viewModel.getViewsByCategory().get(EditorViewCategory.PLUGINS);
            if(pluginViews != null) {
                for (String viewName : pluginViews) {
                    viewButtons.put(viewName, createViewButton(viewName));
                    add(viewButtons.get(viewName), IconStripPosition.RIGHT);
                }
            }
        }

        viewModel.addEditorViewModelListener(new EditorViewModelListener() {
            @Override
            public void viewShown(String viewName) {
                if(viewButtons.containsKey(viewName)) {
                    viewButtons.get(viewName).setSelected(true);
                }
                ViewIconStrip.this.repaint();
            }

            @Override
            public void viewHidden(String viewName) {
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

    public FlatButton createLayoutButton() {
        final PhonUIAction showLayoutMenu = PhonUIAction.eventConsumer(this::onShowLayoutMenu);
        showLayoutMenu.putValue(FlatButton.ICON_SIZE_PROP, IconSize.MEDIUM_LARGE);
        showLayoutMenu.putValue(FlatButton.ICON_NAME_PROP, "auto_awesome_mosaic");
        showLayoutMenu.putValue(FlatButton.ICON_FONT_NAME_PROP, IconManager.GoogleMaterialDesignIconsFontName);
        final FlatButton moreButton = createButton(showLayoutMenu);
        moreButton.setPadding(2);
        moreButton.setPopupLocation(SwingConstants.EAST);
        moreButton.setPopupText("Layout...");
        return moreButton;
    }

    private void onShowLayoutMenu(PhonActionEvent pae) {
        final JMenu menu = new JMenu("Layout...");

        viewModel.setupPerspectiveMenu(menu);
        final JComponent source = (JComponent)pae.getActionEvent().getSource();
        menu.getPopupMenu().show(source, 0, source.getHeight());
    }

    public FlatButton createViewButton(String viewName) {
        final IconData iconData = getViewIcon(viewName);
        final Action showHideAct = PhonUIAction.runnable(() -> {
            final ShowHideViewEdit showHideEdit =
                    new ShowHideViewEdit(session, editorEventManager, viewModel, viewName,
                            !viewModel.isShowing(viewName));
            undoSupport.postEdit(showHideEdit);
        });
        showHideAct.putValue(FlatButton.ICON_SIZE_PROP, IconSize.MEDIUM_LARGE);
        showHideAct.putValue(FlatButton.ICON_FONT_NAME_PROP, iconData.fontName());
        showHideAct.putValue(FlatButton.ICON_NAME_PROP, iconData.iconName());
        final FlatButton retVal = createButton(showHideAct);
        retVal.setSelected(viewModel.isShowing(viewName));
        retVal.setPadding(2);
        retVal.setPopupText(viewName);
        if(side == SwingConstants.LEFT)
            retVal.setPopupLocation(SwingConstants.EAST);
        else
            retVal.setPopupLocation(SwingConstants.WEST);
        return retVal;
    }

}
