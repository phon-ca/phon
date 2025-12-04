package ca.phon.app.session.editor.view.speechAnalysis;

import ca.phon.app.log.LogUtil;
import ca.phon.app.session.editor.EditorEvent;
import ca.phon.app.session.editor.EditorEventType;
import ca.phon.app.session.editor.undo.AddTierEdit;
import ca.phon.app.session.editor.undo.TierEdit;
import ca.phon.app.session.intervalTiers.*;
import ca.phon.media.TimeUIModel;
import ca.phon.media.TimeUIModelAdapter;
import ca.phon.orthography.*;
import ca.phon.plugin.IPluginExtensionPoint;
import ca.phon.plugin.PluginManager;
import ca.phon.session.*;
import ca.phon.session.Record;
import ca.phon.session.tierdata.TierData;
import ca.phon.session.tierdata.TierElement;
import ca.phon.session.tierdata.TierInternalMedia;
import ca.phon.ui.FlatButton;
import ca.phon.ui.HidablePanel;
import ca.phon.ui.IconStrip;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.ui.menu.MenuBuilder;
import ca.phon.util.Range;
import ca.phon.util.SegmentOverlapUtil;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;
import ca.phon.visitor.VisitorAdapter;
import ca.phon.visitor.annotation.Visits;
import org.jdesktop.swingx.VerticalLayout;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.undo.AbstractUndoableEdit;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.*;

/**
 * Tier for displaying intervals from the session {@link IntervalTiers}
 */
public class SpeechAnalysisIntervalsTier extends SpeechAnalysisTier {

    private TimeUIModel intervalTierTimeModel;

    /**
     * Currently focused selected interval, changes to this interval will update the selected interval
     * as well as any intervals which share a relationship with this interval (e.g. phones within a word).
     * Intervals across other tiers will also be affected if they share an start/end time with this interval.
     */
    private TimeUIModel.Interval currentInterval;

    private IntervalTierComponent currentIntervalTierComponent;

    private int currentIntervalIndex = -1;

    /**
     * Map of tier name to interval tier components for record data tiers
     */
    private final Map<String, IntervalTierComponent> recordDataIntervalTiers = new HashMap<>();

    /**
     * Map of tier name to interval tier components for session intervalTiers tiers
     */
    private final Map<String, IntervalTierComponent> sessionLevelIntervalTiers = new HashMap<>();

    final WordAndPhoneSelectionListener wordAndPhoneSelectionListener = new WordAndPhoneSelectionListener();

    private JSeparator separator;

    public SpeechAnalysisIntervalsTier(SpeechAnalysisEditorView parentView) {
        super(parentView);

        init();
        setupEditorEventHandlers();
    }

    private void setupEditorEventHandlers() {
        getParentView().getEditor().getEventManager().registerActionForEvent(EditorEventType.TierChange, this::onTierChange);
        getParentView().getEditor().getEventManager().registerActionForEvent(EditorEventType.TimelineTierAdd, this::onIntervalTierAdd);
        getParentView().getEditor().getEventManager().registerActionForEvent(EditorEventType.TimelineTierRemove, this::onIntervalTierRemove);
        getParentView().getEditor().getEventManager().registerActionForEvent(EditorEventType.TierViewChanged, this::onTierViewChanged);
        getParentView().getEditor().getEventManager().registerActionForEvent(EditorEventType.RecordDataIntervalTierAdd, this::onRecordDataIntervalTierAdd);
        getParentView().getEditor().getEventManager().registerActionForEvent(EditorEventType.RecordDataIntervalTierRemove, this::onRecordDataIntervalTierRemove);
    }

    private void unregisterEditorEventHandlers() {
        getParentView().getEditor().getEventManager().removeActionForEvent(EditorEventType.TierChange, this::onTierChange);
        getParentView().getEditor().getEventManager().removeActionForEvent(EditorEventType.TimelineTierAdd, this::onIntervalTierAdd);
        getParentView().getEditor().getEventManager().removeActionForEvent(EditorEventType.TimelineTierRemove, this::onIntervalTierRemove);
        getParentView().getEditor().getEventManager().removeActionForEvent(EditorEventType.TierViewChanged, this::onTierViewChanged);
        getParentView().getEditor().getEventManager().removeActionForEvent(EditorEventType.RecordDataIntervalTierAdd, this::onRecordDataIntervalTierAdd);
        getParentView().getEditor().getEventManager().removeActionForEvent(EditorEventType.RecordDataIntervalTierRemove, this::onRecordDataIntervalTierRemove);
    }

    public void onTierViewChanged(EditorEvent<EditorEventType.TierViewChangedData> ee) {
        if(ee.data().changeType() == EditorEventType.TierViewChangeType.ADD_TIER) {
            if(ee.data().tierNames().contains(UserTierType.Wor.getPhonTierName())) {
                addWorTierToView();
            } else if(ee.data().tierNames().contains(UserTierType.PhoneIntervals.getPhonTierName())) {
                addPhoneIntervalsTierToView();
            }
        } else if(ee.data().changeType() == EditorEventType.TierViewChangeType.DELETE_TIER) {
            if(ee.data().tierNames().contains(UserTierType.Wor.getPhonTierName())) {
                removeWorTierFromView();
            } else if(ee.data().tierNames().contains(UserTierType.PhoneIntervals.getPhonTierName())) {
                removePhoneIntervalsTierFromView();
            }
        }
    }

    public void onTierChange(EditorEvent<EditorEventType.TierChangeData> ee) {
        final EditorEventType.TierChangeData data = ee.data();
        final Tier<?> tier = data.tier();

        if(recordDataIntervalTiers.containsKey(tier.getName())) {
            final IntervalTierComponent intervalTierComponent = recordDataIntervalTiers.get(tier.getName());
            final RecordIntervalTier recordIntervalTier = intervalTierComponent.getTimelineTier().getExtension(RecordIntervalTier.class);
            if(recordIntervalTier != null) {
                recordIntervalTier.updateCachedIntervals(ee.data().record());
            }
            intervalTierComponent.repaint();
        }
    }

    public void onIntervalTierAdd(EditorEvent<EditorEventType.TimelineTierAddData> ee) {
        // create and add tier to view
        final IntervalTier newTier = getParentView().getEditor().getSession().getTimeline().getTier(ee.data().tierName());
        if(newTier != null) {
            final IntervalTierComponent intervalTierComponent = new IntervalTierComponent(this.intervalTierTimeModel, newTier);
            intervalTierComponent.setIntervalClickedCallback( (index,interval) ->
                    setupSelectionInterval(intervalTierComponent, index, interval)
            );
            if(!recordDataIntervalTiers.isEmpty() && sessionLevelIntervalTiers.isEmpty()) {
                // first session level interval tier, add separator before
                add(separator);
            }
            add(intervalTierComponent);
            revalidate();
            repaint();
            sessionLevelIntervalTiers.put(newTier.getName(), intervalTierComponent);
        }
    }

    public void onIntervalTierRemove(EditorEvent<EditorEventType.TimelineTierRemoveData> ee) {
        final String tierName = ee.data().tierName();
        if(sessionLevelIntervalTiers.containsKey(tierName)) {
            final IntervalTierComponent intervalTierComponent = sessionLevelIntervalTiers.get(tierName);
            remove(intervalTierComponent);
            revalidate();
            repaint();
            sessionLevelIntervalTiers.remove(tierName);

            if(sessionLevelIntervalTiers.isEmpty() && !recordDataIntervalTiers.isEmpty()) {
                // last session level interval tier removed, remove separator
                for(int i = 0; i < getComponentCount(); i++) {
                    if(getComponent(i) instanceof JSeparator) {
                        remove(i);
                        break;
                    }
                }
            }
        }
    }

    public void onRecordDataIntervalTierAdd(EditorEvent<EditorEventType.RecordDataIntervalTierAddData> ee) {
        final Session session = getParentView().getEditor().getSession();
        final TierDescription tierDesc = session.getTier(ee.data().tierName());
        if (tierDesc == null) return;
        final RecordIntervalTier recordTimelineTier = new RecordIntervalTier(session, tierDesc.getName(), getParentView().getEditor().getDataModel().getTranscriber());
        final IntervalTier intervalTier = new IntervalTier(recordTimelineTier);
        intervalTier.putExtension(RecordIntervalTier.class, recordTimelineTier);
        final IntervalTierComponent intervalTierComponent = new IntervalTierComponent(intervalTierTimeModel, intervalTier);
        intervalTierComponent.getSelectionModel().addListSelectionListener(wordAndPhoneSelectionListener);
        intervalTierComponent.setIntervalClickedCallback( (index,interval) ->
                setupSelectionInterval(intervalTierComponent, index, interval)
        );
        final int intervalTierInsertIdx = recordDataIntervalTiers.size();
        add(intervalTierComponent, intervalTierInsertIdx);
        recordDataIntervalTiers.put(tierDesc.getName(), intervalTierComponent);
        if(recordDataIntervalTiers.size() == 1 && !sessionLevelIntervalTiers.isEmpty()) {
            // first record data interval tier, add separator after
            add(separator, intervalTierInsertIdx + 1);
        }
    }

    public void onRecordDataIntervalTierRemove(EditorEvent<EditorEventType.RecordDataIntervalTierRemoveData> ee) {
        final String tierName = ee.data().tierName();
        if(recordDataIntervalTiers.containsKey(tierName)) {
            final IntervalTierComponent intervalTierComponent = recordDataIntervalTiers.get(tierName);
            remove(intervalTierComponent);
            recordDataIntervalTiers.remove(tierName);
            revalidate();
            repaint();

            if(recordDataIntervalTiers.isEmpty()) {
                // last record data interval tier removed, remove separator
                for(int i = 0; i < getComponentCount(); i++) {
                    if(getComponent(i) instanceof JSeparator) {
                        remove(i);
                        break;
                    }
                }
            }
        }
    }

    public void addWorTierToView() {
        final Session session = getParentView().getEditor().getSession();
        final TierDescription worTierDesc = session.getTier(UserTierType.Wor.getPhonTierName());
        if (worTierDesc == null) return;
        final RecordIntervalTier recordTimelineTier = new RecordIntervalTier(session, worTierDesc.getName(), getParentView().getEditor().getDataModel().getTranscriber());
        final IntervalTier intervalTier = new IntervalTier(recordTimelineTier);
        intervalTier.putExtension(RecordIntervalTier.class, recordTimelineTier);
        final IntervalTierComponent intervalTierComponent = new IntervalTierComponent(intervalTierTimeModel, intervalTier);
        intervalTierComponent.getSelectionModel().addListSelectionListener(wordAndPhoneSelectionListener);
        intervalTierComponent.setIntervalClickedCallback( (index,interval) ->
                setupSelectionInterval(intervalTierComponent, index, interval)
        );
        add(intervalTierComponent, 0);
        recordDataIntervalTiers.put(worTierDesc.getName(), intervalTierComponent);
        if(recordDataIntervalTiers.size() == 1 && !sessionLevelIntervalTiers.isEmpty()) {
            // first record data interval tier, add separator after
            add(separator, 1);
        }
    }

    public void removeWorTierFromView() {
        final String tierName = UserTierType.Wor.getPhonTierName();
        if(recordDataIntervalTiers.containsKey(tierName)) {
            final IntervalTierComponent intervalTierComponent = recordDataIntervalTiers.get(tierName);
            remove(intervalTierComponent);
            revalidate();
            repaint();
            recordDataIntervalTiers.remove(tierName);

            if(recordDataIntervalTiers.isEmpty()) {
                // last record data interval tier removed, remove separator
                for(int i = 0; i < getComponentCount(); i++) {
                    if(getComponent(i) instanceof JSeparator) {
                        remove(i);
                        break;
                    }
                }
            }
        }
    }

    public void addPhoneIntervalsTierToView() {
        final Session session = getParentView().getEditor().getSession();
        final TierDescription phoTierDesc = session.getTier(UserTierType.PhoneIntervals.getPhonTierName());
        if (phoTierDesc == null) return;
        final RecordIntervalTier recordTimelineTier = new RecordIntervalTier(session, phoTierDesc.getName(), getParentView().getEditor().getDataModel().getTranscriber());
        final IntervalTier intervalTier = new IntervalTier(recordTimelineTier);
        intervalTier.putExtension(RecordIntervalTier.class, recordTimelineTier);
        final IntervalTierComponent intervalTierComponent = new IntervalTierComponent(intervalTierTimeModel, intervalTier);
        intervalTierComponent.getSelectionModel().addListSelectionListener(wordAndPhoneSelectionListener);
        intervalTierComponent.setIntervalClickedCallback( (index,interval) ->
                setupSelectionInterval(intervalTierComponent, index, interval)
        );
        if(recordDataIntervalTiers.containsKey(UserTierType.Wor.getPhonTierName())) {
            add(intervalTierComponent, 1);
        } else {
            add(intervalTierComponent, 0);
        }
        recordDataIntervalTiers.put(phoTierDesc.getName(), intervalTierComponent);
        if(recordDataIntervalTiers.size() == 1 && !sessionLevelIntervalTiers.isEmpty()) {
            // first record data interval tier, add separator after
            add(separator, 1);
        }
    }

    public void removePhoneIntervalsTierFromView() {
        final String tierName = UserTierType.PhoneIntervals.getPhonTierName();
        if(recordDataIntervalTiers.containsKey(tierName)) {
            final IntervalTierComponent intervalTierComponent = recordDataIntervalTiers.get(tierName);
            remove(intervalTierComponent);
            revalidate();
            repaint();
            recordDataIntervalTiers.remove(tierName);

            if(recordDataIntervalTiers.isEmpty()) {
                // last record data interval tier removed, remove separator
                for(int i = 0; i < getComponentCount(); i++) {
                    if(getComponent(i) instanceof JSeparator) {
                        remove(i);
                        break;
                    }
                }
            }
        }
    }

    private void init() {
        setLayout(new VerticalLayout());

        // setup time model
        final TimeUIModel parentModel = getParentView().getTimeModel();
        this.intervalTierTimeModel = new TimeUIModel();
        this.intervalTierTimeModel.setCurrentTime(parentModel.getCurrentTime());
        this.intervalTierTimeModel.setEndTime(parentModel.getEndTime());
        this.intervalTierTimeModel.setTimeInsets(parentModel.getTimeInsets());
        this.intervalTierTimeModel.setMediaEndTime(parentModel.getMediaEndTime());
        this.intervalTierTimeModel.setPixelsPerSecond(parentModel.getPixelsPerSecond());
        parentModel.addTimeUIModelListener(new TimeUIModelAdapter() {
            @Override
            public void propertyChange(PropertyChangeEvent e) {
            switch(e.getPropertyName()) {
               case "currentTime" -> intervalTierTimeModel.setCurrentTime(parentModel.getCurrentTime());
               case "endTime" -> intervalTierTimeModel.setEndTime(parentModel.getEndTime());
               case "timeInsets" -> intervalTierTimeModel.setTimeInsets(parentModel.getTimeInsets());
               case "mediaEndTime" -> intervalTierTimeModel.setMediaEndTime(parentModel.getMediaEndTime());
               case "pixelsPerSecond" -> intervalTierTimeModel.setPixelsPerSecond(parentModel.getPixelsPerSecond());
               default -> super.propertyChange(e);
            }
            }
        });

        // add a tier for each session intervalTiers tier
        final Session session = getParentView().getEditor().getSession();
        final IntervalTiers intervalTiers = session.getTimeline();

        separator = new JSeparator(SwingConstants.HORIZONTAL);

        // check for word intervals tier
        final TierDescription worTierDesc = session.getUserTiers()
                .stream()
                .filter(td -> UserTierType.Wor.getPhonTierName().equals(td.getName()))
                .findAny().orElse(null);
        if(worTierDesc != null) {
            addWorTierToView();
        }

        // check for phone intervals tier
        final TierDescription phoTierDesc = session.getUserTiers()
                .stream()
                .filter(td -> UserTierType.PhoneIntervals.getPhonTierName().equals(td.getName()))
                .findAny().orElse(null);
        if(phoTierDesc != null) {
            addPhoneIntervalsTierToView();
        }

        for(String timelineTierName: intervalTiers.getRecordIntervalTiers()) {
            final RecordIntervalTier recordTimelineTier = new RecordIntervalTier(session, timelineTierName, getParentView().getEditor().getDataModel().getTranscriber());
            final IntervalTier intervalTier = new IntervalTier(recordTimelineTier);
            intervalTier.putExtension(RecordIntervalTier.class, recordTimelineTier);
            final IntervalTierComponent intervalTierComponent = new IntervalTierComponent(this.intervalTierTimeModel, intervalTier);
            intervalTierComponent.setIntervalClickedCallback( (index,interval) ->
                    setupSelectionInterval(intervalTierComponent, index, interval)
            );
            add(intervalTierComponent);
            recordDataIntervalTiers.put(timelineTierName, intervalTierComponent);
        }

        if(!recordDataIntervalTiers.isEmpty()) {
            add(separator);
        }

        for(var timelineTier : intervalTiers.getTiers()) {
            final IntervalTierComponent intervalTierComponent = new IntervalTierComponent(this.intervalTierTimeModel, timelineTier);
            intervalTierComponent.setIntervalClickedCallback( (index,interval) ->
                    setupSelectionInterval(intervalTierComponent, index, interval)
            );
            add(intervalTierComponent);
            sessionLevelIntervalTiers.put(timelineTier.getName(), intervalTierComponent);
        }

        // add button in toolbar to display interval tier menu
        final JPopupMenu intervalTierMenu = new JPopupMenu();
        intervalTierMenu.addPopupMenuListener(new PopupMenuListener() {
            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                intervalTierMenu.removeAll();
                final MenuBuilder menuBuilder = new MenuBuilder(intervalTierMenu);
                setupIntervalMenu(menuBuilder);
            }

            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {

            }

            @Override
            public void popupMenuCanceled(PopupMenuEvent e) {

            }
        });
        final PhonUIAction<Void> phonUIAction = PhonUIAction.eventConsumer((phonActionEvent) -> {
            final JButton source = (JButton) phonActionEvent.getActionEvent().getSource();
            intervalTierMenu.show(source, 0, source.getHeight());
        });
        phonUIAction.putValue(PhonUIAction.NAME, "Interval Tiers");
        phonUIAction.putValue(PhonUIAction.SHORT_DESCRIPTION, "Show interval tiers menu");
        phonUIAction.putValue(FlatButton.ICON_FONT_NAME_PROP, IconManager.GoogleMaterialDesignIconsFontName);
        phonUIAction.putValue(FlatButton.ICON_NAME_PROP, "arrow_drop_down");
        phonUIAction.putValue(FlatButton.ICON_SIZE_PROP, IconSize.MEDIUM);
        final JButton intervalTierMenuButton = new FlatButton(phonUIAction);
        getParentView().getToolbar().add(intervalTierMenuButton, IconStrip.IconStripPosition.LEFT);
    }

    /**
     * Setup selection for given interval tier name and index
     * @param tierComponent the interval tier component
     * @param intervalIndex the interval index
     * @param interval the interval
     */
    private void setupSelectionInterval(IntervalTierComponent tierComponent, int intervalIndex, IntervalTier.Interval interval) {
        getParentView().setSelection(interval.getStart(), interval.getEnd());

        // remove old markers
        if(currentInterval != null) {
            intervalTierTimeModel.removeInterval(currentInterval);
        }

        float secsPerPixel = 1.0f / getTimeModel().getPixelsPerSecond();

        currentIntervalTierComponent = tierComponent;
        currentIntervalIndex = intervalIndex;
        final IntervalTier.Interval previousInterval = currentIntervalIndex > 0 ? tierComponent.getTimelineTier().getIntervals().get(currentIntervalIndex-1) : null;
        final IntervalTier.Interval nextInterval = (currentIntervalIndex < tierComponent.getTimelineTier().getIntervals().size() - 1) ?
                tierComponent.getTimelineTier().getIntervals().get(currentIntervalIndex+1) : null;
        currentInterval = new TimeUIModel.Interval(interval.getStart(), interval.getEnd());
        currentInterval.getStartMarker().setMaxTime(interval.getEnd() - secsPerPixel);
        if(previousInterval != null) {
            currentInterval.getStartMarker().setMinTime(previousInterval.getStart() + secsPerPixel);
        }
        currentInterval.getEndMarker().setMinTime(interval.getStart() + secsPerPixel);
        if(nextInterval != null) {
            currentInterval.getEndMarker().setMaxTime(nextInterval.getEnd() - secsPerPixel);
        }
        currentInterval.setAutoSwapMarkers(false);
        currentInterval.addPropertyChangeListener(currentIntervalListener);
        intervalTierTimeModel.addInterval(currentInterval);
    }

    public void clearSelectionInterval() {
        if(currentInterval != null) {
            intervalTierTimeModel.removeInterval(currentInterval);
            currentInterval.removePropertyChangeListener(currentIntervalListener);
            currentInterval = null;
            currentIntervalIndex = -1;
            currentIntervalTierComponent = null;
        }
    }

    private String recordIntervalTierInfoText() {
        return """
                <html><p>Record interval tiers are stored in record data<br/>
                and are updated with changes to the record segment.<br/>
                </p></html>""";
    }

    private void setupIntervalMenu(MenuBuilder mb) {
        final boolean hasWorTier = recordDataIntervalTiers.containsKey(UserTierType.Wor.getPhonTierName());
        final boolean hasPhoTier = recordDataIntervalTiers.containsKey(UserTierType.PhoneIntervals.getPhonTierName());
        final JMenu recordIntervalTiersMenu = mb.addMenu(".", "Record Interval Tiers");

        final HidablePanel infoPanel = new HidablePanel("SpeechAnalysisIntervalsTier.recordIntervalTierInfo");
        infoPanel.setTopLabelText("<html><em>Record Interval Tiers</em></html>");
        infoPanel.setBottomLabelText(recordIntervalTierInfoText());

        recordIntervalTiersMenu.add(infoPanel);

        final MenuBuilder recordTierMenuBuilder = new MenuBuilder(recordIntervalTiersMenu);

        if(!hasWorTier) {
            final PhonUIAction<Void> addWorTierAct = PhonUIAction.runnable(this::addWorTier);
            addWorTierAct.putValue(PhonUIAction.NAME, "Add " + UserTierType.Wor.getPhonTierName() + " (" + UserTierType.Wor.getChatTierName() + ")" + " tier");
            addWorTierAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Add " + UserTierType.Wor.getPhonTierName() + " (" + UserTierType.Wor.getChatTierName() + ")" + " tier to records");
            recordTierMenuBuilder.addItem(".", addWorTierAct);
        }

        if(!hasPhoTier) {
            final PhonUIAction<Void> addPhoTierAct = PhonUIAction.runnable(this::addPhoTier);
            addPhoTierAct.putValue(PhonUIAction.NAME, "Add " + UserTierType.PhoneIntervals.getPhonTierName() + " (" + UserTierType.PhoneIntervals.getChatTierName() + ")" + " tier");
            addPhoTierAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Add " + UserTierType.PhoneIntervals.getPhonTierName() + " (" + UserTierType.PhoneIntervals.getChatTierName() + ")" + " tier to records");
            recordTierMenuBuilder.addItem(".", addPhoTierAct);
        }

        if(!hasWorTier || !hasPhoTier && !recordDataIntervalTiers.isEmpty()) {
            recordTierMenuBuilder.addSeparator(".", "addtiers");
        }

        // add show/hide menu items for each tier
        if(!recordDataIntervalTiers.isEmpty()) {
            for(var entry: recordDataIntervalTiers.entrySet()) {
                final String tierName = entry.getKey();
                final IntervalTierComponent tierComp = entry.getValue();
                final JCheckBoxMenuItem showTierItem = new JCheckBoxMenuItem(tierName, tierComp.isVisible());
                showTierItem.addActionListener( (e) -> {
                    final boolean newVisibility = showTierItem.isSelected();
                    setTierVisible(tierName, newVisibility);
                    final TierVisibilityEdit edit = new TierVisibilityEdit(this, tierName, newVisibility);
                    getParentView().getEditor().getUndoSupport().postEdit(edit);
                });
                recordTierMenuBuilder.addItem(".", showTierItem);
            }

            // add show all/hide all
            recordTierMenuBuilder.addSeparator(".", "showhide");
            final JMenuItem showAllItem = new JMenuItem("Show All");
            showAllItem.addActionListener( (e) -> {
                getParentView().getEditor().getUndoSupport().beginUpdate("Show all record interval tiers");
                for(var entry: recordDataIntervalTiers.entrySet()) {
                    final String tierName = entry.getKey();
                    final IntervalTierComponent tierComp = entry.getValue();
                    if(!tierComp.isVisible()) {
                        tierComp.setVisible(true);
                        final TierVisibilityEdit visibilityEdit = new TierVisibilityEdit(this, tierName, true);
                        getParentView().getEditor().getUndoSupport().postEdit(visibilityEdit);
                    }
                }
                getParentView().getEditor().getUndoSupport().endUpdate();
            });
            recordTierMenuBuilder.addItem(".", showAllItem);

            final JMenuItem hideAllItem = new JMenuItem("Hide All");
            hideAllItem.addActionListener( (e) -> {
                getParentView().getEditor().getUndoSupport().beginUpdate("Hide all record interval tiers");
                for(var entry: recordDataIntervalTiers.entrySet()) {
                    final String tierName = entry.getKey();
                    final IntervalTierComponent tierComp = entry.getValue();
                    if(tierComp.isVisible()) {
                        tierComp.setVisible(false);
                        final TierVisibilityEdit visibilityEdit = new TierVisibilityEdit(this, tierName, false);
                        getParentView().getEditor().getUndoSupport().postEdit(visibilityEdit);
                    }
                }
                getParentView().getEditor().getUndoSupport().endUpdate();
            });
            recordTierMenuBuilder.addItem(".", hideAllItem);
        }

        // add show/hide menu items for each tier
        if(!sessionLevelIntervalTiers.isEmpty()) {
            final MenuBuilder sessionTierMenuBuilder = new MenuBuilder(mb.addMenu(".", "Session Interval Tiers"));
            for(var entry: sessionLevelIntervalTiers.entrySet()) {
                final String tierName = entry.getKey();
                final IntervalTierComponent tierComp = entry.getValue();
                
                // Create submenu for each tier
                final MenuBuilder tierMenuBuilder = new MenuBuilder(sessionTierMenuBuilder.addMenu(".", tierName));
                
                // Show/hide checkbox
                final JCheckBoxMenuItem showTierItem = new JCheckBoxMenuItem("Show/Hide", tierComp.isVisible());
                showTierItem.addActionListener( (e) -> {
                    final boolean newVisibility = showTierItem.isSelected();
                    setTierVisible(tierName, newVisibility);
                    final TierVisibilityEdit edit = new TierVisibilityEdit(this, tierName, newVisibility);
                    getParentView().getEditor().getUndoSupport().postEdit(edit);
                });
                tierMenuBuilder.addItem(".", showTierItem);
                
                // Add separator before import options
                tierMenuBuilder.addSeparator(".", "import_records");

                // Import to Record Segments
                final JMenuItem importToRecordSegmentsItem = new JMenuItem("Import as records...");
                importToRecordSegmentsItem.addActionListener( (e) -> {
                    showImportToRecordSegmentsDialog(tierName);
                });
                tierMenuBuilder.addItem(".", importToRecordSegmentsItem);

                tierMenuBuilder.addSeparator(".", "import_tiers");
                
                // Import to Orthography
                final JMenuItem importToOrthographyItem = new JMenuItem("Import into Orthography...");
                importToOrthographyItem.setEnabled(getParentView().getEditor().getSession().getRecordCount() > 0);
                importToOrthographyItem.addActionListener( (e) -> {
                    showImportToOrthographyDialog(tierName);
                });
                tierMenuBuilder.addItem(".", importToOrthographyItem);

                // Import to IPA tier
                final JMenuItem importToIPATierItem = new JMenuItem("Import into IPA tier...");
                importToIPATierItem.setEnabled(getParentView().getEditor().getSession().getRecordCount() > 0);
                importToIPATierItem.addActionListener( (e) -> {
                    showImportToIPATierDialog(tierName);
                });
                tierMenuBuilder.addItem(".", importToIPATierItem);

                // Import to Word Intervals
                final JMenuItem importToWordIntervalsItem = new JMenuItem("Import into Word intervals...");
                importToWordIntervalsItem.setEnabled(getParentView().getEditor().getSession().getRecordCount() > 0);
                importToWordIntervalsItem.addActionListener( (e) -> {
                    showImportToWordIntervalsDialog(tierName);
                });
                tierMenuBuilder.addItem(".", importToWordIntervalsItem);

                // Import to Phone Intervals
                final JMenuItem importToPhoneIntervalsItem = new JMenuItem("Import into Phone Intervals...");
                importToPhoneIntervalsItem.setEnabled(getParentView().getEditor().getSession().getRecordCount() > 0);
                importToPhoneIntervalsItem.addActionListener( (e) -> {
                    showImportToPhoneIntervalsDialog(tierName);
                });
                tierMenuBuilder.addItem(".", importToPhoneIntervalsItem);

                // Import to User Tier
                final JMenuItem importToUserTierItem = new JMenuItem("Import into User tier...");
                importToUserTierItem.setEnabled(getParentView().getEditor().getSession().getRecordCount() > 0);
                importToUserTierItem.addActionListener( (e) -> {
                    showImportToUserTierDialog(tierName);
                });
                tierMenuBuilder.addItem(".", importToUserTierItem);
            }

            // add show all/hide all
            sessionTierMenuBuilder.addSeparator(".", "showhide");
            final JMenuItem showAllItem = new JMenuItem("Show All");
            showAllItem.addActionListener( (e) -> {
                getParentView().getEditor().getUndoSupport().beginUpdate("Show all session interval tiers");
                for(var entry: sessionLevelIntervalTiers.entrySet()) {
                    final String tierName = entry.getKey();
                    final IntervalTierComponent tierComp = entry.getValue();
                    if(!tierComp.isVisible()) {
                        tierComp.setVisible(true);
                        final TierVisibilityEdit visibilityEdit = new TierVisibilityEdit(this, tierName, true);
                        getParentView().getEditor().getUndoSupport().postEdit(visibilityEdit);
                    }
                }
                getParentView().getEditor().getUndoSupport().endUpdate();
            });
            sessionTierMenuBuilder.addItem(".", showAllItem);

            final JMenuItem hideAllItem = new JMenuItem("Hide All");
            hideAllItem.addActionListener( (e) -> {
                getParentView().getEditor().getUndoSupport().beginUpdate("Hide all session interval tiers");
                for(var entry: sessionLevelIntervalTiers.entrySet()) {
                    final String tierName = entry.getKey();
                    final IntervalTierComponent tierComp = entry.getValue();
                    if(tierComp.isVisible()) {
                        tierComp.setVisible(false);
                        final TierVisibilityEdit visibilityEdit = new TierVisibilityEdit(this, tierName, false);
                        getParentView().getEditor().getUndoSupport().postEdit(visibilityEdit);
                    }
                }
                getParentView().getEditor().getUndoSupport().endUpdate();
            });
            sessionTierMenuBuilder.addItem(".", hideAllItem);
        }

        // plugin point for adding menu items
        final List<IPluginExtensionPoint<IntervalTierMenuHandler>> extPts =
                PluginManager.getInstance().getExtensionPoints(IntervalTierMenuHandler.class);
        for(IPluginExtensionPoint<IntervalTierMenuHandler> extPt:extPts) {
            try {
                final IntervalTierMenuHandler handler = extPt.getFactory().createObject();
                handler.setupMenu(this, mb);
            } catch (Exception ex) {
                LogUtil.severe(ex);
            }
        }
    }

    private void addWorTier() {
        final Session session = getParentView().getEditor().getSession();
        final TierDescription existingTierDesc = session.getTier(UserTierType.Wor.getPhonTierName());
        if(existingTierDesc != null) return;
        final SessionFactory factory = SessionFactory.newFactory();
        final TierDescription worTierDesc = factory.createTierDescription(UserTierType.Wor);
        final TierViewItem tvi = factory.createTierViewItem(UserTierType.Wor.getPhonTierName(), false, true);

        getParentView().getEditor().getUndoSupport().beginUpdate("Add word intervals tier");
        final AddTierEdit addTierEdit = new AddTierEdit(getParentView().getEditor(), worTierDesc, tvi, -1);
        getParentView().getEditor().getUndoSupport().postEdit(addTierEdit);

        for(int i = 0; i < session.getRecordCount(); i++) {
            final Record r = session.getRecord(i);
            final Orthography orthography = r.getOrthography();
            final Orthography wor = UpdateWorAfterOrthography.worFromOrthography(orthography, r.getMediaSegment());

            final Tier<Orthography> worTier = r.getTier(UserTierType.Wor.getPhonTierName(), Orthography.class);
            final TierEdit<Orthography> worTierEdit = new TierEdit<>(getParentView().getEditor().getSession(),
                    getParentView().getEditor().getEventManager(), getParentView().getEditor().getDataModel().getTranscriber(),
                    r, worTier, wor, false);
            getParentView().getEditor().getUndoSupport().postEdit(worTierEdit);
        }
        getParentView().getEditor().getUndoSupport().endUpdate();
    }

    private void addPhoTier() {
        final Session session = getParentView().getEditor().getSession();
        final TierDescription existingTierDesc = session.getTier(UserTierType.PhoneIntervals.getPhonTierName());
        if(existingTierDesc != null) return;
        final SessionFactory factory = SessionFactory.newFactory();
        final TierDescription phointTierDesc = factory.createTierDescription(UserTierType.PhoneIntervals);
        final TierViewItem tvi = factory.createTierViewItem(UserTierType.PhoneIntervals.getPhonTierName(), false, true);

        getParentView().getEditor().getUndoSupport().beginUpdate("Add phone intervals tier");
        final AddTierEdit addTierEdit = new AddTierEdit(getParentView().getEditor(), phointTierDesc, tvi, -1);
        getParentView().getEditor().getUndoSupport().postEdit(addTierEdit);

        for(int i = 0; i < session.getRecordCount(); i++) {
            final Record r = session.getRecord(i);

            // if we have a word intervals tier, use it to generate phone intervals
        }
        getParentView().getEditor().getUndoSupport().endUpdate();
    }

    @Override
    public void addMenuItems(JMenu menuEle, boolean includeAccelerators) {
        final MenuBuilder mb = new MenuBuilder(menuEle);
        setupIntervalMenu(mb);
    }

    @Override
    public void onRefresh() {

    }

    public boolean isTierVisible(String tierName) {
        return (recordDataIntervalTiers.containsKey(tierName) && recordDataIntervalTiers.get(tierName).isVisible())
                || (sessionLevelIntervalTiers.containsKey(tierName) && sessionLevelIntervalTiers.get(tierName).isVisible());
    }

    public void setTierVisible(String tierName, boolean visible) {
        if(recordDataIntervalTiers.containsKey(tierName)) {
            recordDataIntervalTiers.get(tierName).setVisible(visible);
        } else if(sessionLevelIntervalTiers.containsKey(tierName)) {
            sessionLevelIntervalTiers.get(tierName).setVisible(visible);
        }
    }

    /**
     * Word and phone interval tier selection listener
     *
     * 1) When a word interval is selected, if phone intervals from another word interval are selected,
     *   they should be deselected.
     * 2) When a phone interval is selected, if the parent word interval is not selected, it should be selected.
     */
    private class WordAndPhoneSelectionListener implements ListSelectionListener {

        private IntervalTierComponent wordIntervalTierComponent() {
            return recordDataIntervalTiers.get(UserTierType.Wor.getPhonTierName());
        }

        private IntervalTierComponent phoneIntervalTierComponent() {
            return recordDataIntervalTiers.get(UserTierType.PhoneIntervals.getPhonTierName());
        }

        @Override
        public void valueChanged(ListSelectionEvent e) {
            final IntervalTierComponent wordIntervalTier = wordIntervalTierComponent();
            final RecordIntervalTier worIntervalTier = (wordIntervalTier == null ? null : wordIntervalTier.getTimelineTier().getExtension(RecordIntervalTier.class));
            final IntervalTierComponent phoneIntervalTier = phoneIntervalTierComponent();
            final RecordIntervalTier phoIntervalTier = (phoneIntervalTier == null ? null : phoneIntervalTier.getTimelineTier().getExtension(RecordIntervalTier.class));
            if(worIntervalTier == null && phoIntervalTier == null) return;

            if(e.getValueIsAdjusting()) return;
            if(e.getSource() == wordIntervalTier.getSelectionModel()) {
                if(phoneIntervalTier == null) return;
                // get indices of selected phone intervals for the word
                final int selectedWord = wordIntervalTier.getSelectedIndex();
                if(selectedWord == -1) return;
                final IntervalTier.Interval wordInterval = wordIntervalTier.getTimelineTier().getIntervals().get(selectedWord);
                final var phoneIndiciesForWord =
                    ((IntervalTierComponentUI)phoneIntervalTier.getUI()).getIntervalIndicesForTimeRange(wordInterval.getStart(), wordInterval.getEnd());
                if(phoneIndiciesForWord.isEmpty()) {
                    phoneIntervalTier.getSelectionModel().clearSelection();
                    phoneIntervalTier.repaint();
                } else {
                    final int selectedPhone = phoneIntervalTier.getSelectedIndex();
                    if(selectedPhone >= 0) {
                        if (!phoneIndiciesForWord.contains(selectedPhone)) {
                            phoneIntervalTier.getSelectionModel().clearSelection();
                            phoneIntervalTier.repaint();
                        }
                    }
                }

                // select the record which contains this word interval
                for(int i = 0; i < getParentView().getEditor().getSession().getRecordCount(); i++) {
                    final Record r = getParentView().getEditor().getSession().getRecord(i);
                    final Range intervalRange = worIntervalTier.getIntervalRangeForRecord(r);
                    if(intervalRange != null && intervalRange.contains(selectedWord)) {
                        if(getParentView().getEditor().getCurrentRecordIndex() != i) {
                            getParentView().getEditor().setCurrentRecordIndex(i);
                        }
                        break;
                    }
                }
            } else if(phoneIntervalTier != null && e.getSource() == phoneIntervalTier.getSelectionModel()) {
                if(wordIntervalTier == null) return;
                // get selected phone interval
                final int selectedPhone = phoneIntervalTier.getSelectedIndex();
                if(selectedPhone == -1) return;
                final IntervalTier.Interval phoneInterval = phoneIntervalTier.getTimelineTier().getIntervals().get(selectedPhone);
                // find the record which contains this phone interval
                int recordIdx = -1;
                for(int i = 0; i < getParentView().getEditor().getSession().getRecordCount(); i++) {
                    final Record r = getParentView().getEditor().getSession().getRecord(i);
                    final Range intervalRange = phoIntervalTier.getIntervalRangeForRecord(r);
                    if(intervalRange != null && intervalRange.contains(selectedPhone)) {
                        recordIdx = i;
                        break;
                    }
                }
                if(recordIdx == -1) return;
                final Record r = getParentView().getEditor().getSession().getRecord(recordIdx);

                // find the word interval that contains the phone interval
                final var wordIndicesForRecord = worIntervalTier.getIntervalRangeForRecord(r);
                if(wordIndicesForRecord == null) return;

                for(int wordIndex:wordIndicesForRecord) {
                    final IntervalTier.Interval wordInterval = wordIntervalTier.getTimelineTier().getIntervals().get(wordIndex);
                    if(wordInterval.getStart() <= phoneInterval.getStart() &&
                            wordInterval.getEnd() >= phoneInterval.getEnd()) {
                        wordIntervalTier.getSelectionModel().setSelectionInterval(wordIndex, wordIndex);
                        wordIntervalTier.repaint();
                        break;
                    }
                }
            }
        }

    };

    private Orthography originalWor = null;
    private TierEdit<Orthography> lastWorEdit = null;
    private TierEdit<TierData> lastPhoneIntervalsEdit = null;
    private TierEdit<TierData> lastTierDataEdit = null;

    private final PropertyChangeListener currentIntervalListener = (e) -> {
        if(currentIntervalTierComponent == null || currentIntervalIndex == -1) return;

        final String tierName = findTierNameForCurrentComponent();
        if(tierName == null) return;

        switch (e.getPropertyName()) {
            case "startMarker.time", "endMarker.time" -> handleMarkerTimeChange(e, tierName);
            case "valueAdjusting" -> handleValueAdjustingChange(e);
            default -> { /* ignore */ }
        }
    };

    /**
     * Find the tier name associated with the currentIntervalTierComponent.
     */
    private String findTierNameForCurrentComponent() {
        for(var entry: recordDataIntervalTiers.entrySet()) {
            if(entry.getValue() == currentIntervalTierComponent) {
                return entry.getKey();
            }
        }
        return null;
    }

    /**
     * Handle start/end marker changes for current interval.
     */
    private void handleMarkerTimeChange(PropertyChangeEvent e, String tierName) {
        final Record currentRecord = getParentView().getEditor().currentRecord();
        final MediaSegment seg = currentRecord.getMediaSegment();
        if(seg.isPoint()) return;

        if(UserTierType.Wor.getPhonTierName().equals(tierName)) {
            handleWorMarkerChange(currentRecord);
        } else if(UserTierType.PhoneIntervals.getPhonTierName().equals(tierName)) {
            handlePhoneIntervalsMarkerChange(e, currentRecord);
        } else {
            if(recordDataIntervalTiers.containsKey(tierName)) {
                handleTierDataIntervalsMarkerChange(e, currentRecord, tierName);
            }
        }

        // update selection in parent view
        getParentView().setSelection(
                currentInterval.getStartMarker().getTime(),
                currentInterval.getEndMarker().getTime()
        );
    }

    /**
     * Handle marker changes when current tier is the Wor (word intervals) tier.
     */
    private void handleWorMarkerChange(Record currentRecord) {
        final RecordIntervalTier worIntervalTier =
                currentIntervalTierComponent.getTimelineTier().getExtension(RecordIntervalTier.class);
        if(worIntervalTier == null) return;

        final Range recordIntervalIndices = worIntervalTier.getIntervalRangeForRecord(currentRecord);
        if(recordIntervalIndices == null) return;

        final int offset = recordIntervalIndices.getStart();
        final int idx = currentIntervalIndex - offset;
        if(idx < 0) return;

        final int modifiers = currentInterval.getModifiers();

        final InternalMedia newInterval =
                new InternalMedia(currentInterval.getStartMarker().getTime(), currentInterval.getEndMarker().getTime());

        final Tier<Orthography> worTier =
                currentRecord.getTier(UserTierType.Wor.getPhonTierName(), Orthography.class);
        final Orthography wor = worTier
                .getValueForTranscriber(getParentView().getEditor().getDataModel().getTranscriber())
                .orElse(new Orthography());

        Orthography updatedWor =
                updateWorIntervalsWithInterval(idx, newInterval, wor, modifiers);

        final TierEdit<Orthography> worEdit = createWorTierEdit(
                updatedWor,
                currentInterval.isValueAdjusting()
        );
        lastWorEdit = worEdit;
        getParentView().getEditor().getUndoSupport().postEdit(worEdit);
    }

    /**
     * Handle marker changes when current tier is a generic record interval tier.
     */
    private void handleTierDataIntervalsMarkerChange(PropertyChangeEvent e, Record currentRecord, String tierName) {
        final RecordIntervalTier recordIntervalTier =
                currentIntervalTierComponent.getTimelineTier().getExtension(RecordIntervalTier.class);
        if(recordIntervalTier == null) return;

        final Range recordIntervalIndices = recordIntervalTier.getIntervalRangeForRecord(currentRecord);
        if(recordIntervalIndices == null) return;

        final int offset = recordIntervalIndices.getStart();
        final int idx = currentIntervalIndex - offset;
        if(idx < 0) return;

        int modifiers = currentInterval.getModifiers();
        boolean altPressed = (modifiers & MouseEvent.ALT_DOWN_MASK) != 0;

        final InternalMedia newInterval =
                new InternalMedia(currentInterval.getStartMarker().getTime(), currentInterval.getEndMarker().getTime());
        final Tier<TierData> tierDataTier =
                currentRecord.getTier(tierName, TierData.class);
        final TierData tierData = tierDataTier.getValueForTranscriber(
                getParentView().getEditor().getDataModel().getTranscriber()
        ).orElse(new TierData());
        final TierDataIntervalUpdater updater = new TierDataIntervalUpdater(idx, new TierInternalMedia(newInterval));
        tierData.accept(updater);
        TierData updatedTierData = updater.getUpdatedTierData();

        // update neighboring intervals if needed (as in original code)
        final TierDataIntervalVisitor tierDataIntervalVisitor = new TierDataIntervalVisitor();
        tierData.accept(tierDataIntervalVisitor);
        final List<IntervalTier.Interval> tierDataIntervals = tierDataIntervalVisitor.getIntervals();
        final TierDataIntervalVisitor updatedTierDataIntervalVisitor = new TierDataIntervalVisitor();
        updatedTierData.accept(updatedTierDataIntervalVisitor);
        final List<IntervalTier.Interval> updatedTierDataIntervals = updatedTierDataIntervalVisitor.getIntervals();

        for(int i = 0; i < tierDataIntervals.size(); i++) {
            final IntervalTier.Interval oldInterval = tierDataIntervals.get(i);
            final IntervalTier.Interval updatedInterval = updatedTierDataIntervals.get(i);
            if(oldInterval.getStart() != updatedInterval.getStart() ||
                    oldInterval.getEnd() != updatedInterval.getEnd()) {

                if(oldInterval.getStart() != updatedInterval.getStart() && i > 0) {
                    final IntervalTier.Interval prevInterval = tierDataIntervals.get(i - 1);
                    final var overlapType = SegmentOverlapUtil.computeOverlap(
                            prevInterval.getStart(), prevInterval.getEnd(),
                            updatedInterval.getStart(), updatedInterval.getEnd());
                    if(SegmentOverlapUtil.areContiguous(
                                oldInterval.getStart(), oldInterval.getEnd(),
                                prevInterval.getStart(), prevInterval.getEnd())
                            || overlapType == SegmentOverlapUtil.OverlapType.PARTIAL_OVERLAP_START) {
                        if(!altPressed || overlapType == SegmentOverlapUtil.OverlapType.PARTIAL_OVERLAP_START) {
                            final InternalMedia replacedPrevInterval =
                                    new InternalMedia(prevInterval.getStart(), updatedInterval.getStart());
                            final TierDataIntervalUpdater prevUpdater =
                                    new TierDataIntervalUpdater(i - 1, new TierInternalMedia(replacedPrevInterval));
                            updatedTierData.accept(prevUpdater);
                            updatedTierData = prevUpdater.getUpdatedTierData();

                            // refresh intervals after mutation
                            updatedTierDataIntervalVisitor.reset();
                            updatedTierData.accept(updatedTierDataIntervalVisitor);
                        }
                    }
                }

                if(oldInterval.getEnd() != updatedInterval.getEnd() && i < tierDataIntervals.size() - 1) {
                    final IntervalTier.Interval nextInterval = tierDataIntervals.get(i + 1);
                    final var overlapType = SegmentOverlapUtil.computeOverlap(
                            nextInterval.getStart(), nextInterval.getEnd(),
                            updatedInterval.getStart(), updatedInterval.getEnd());
                    if(SegmentOverlapUtil.areContiguous(
                                oldInterval.getStart(), oldInterval.getEnd(),
                                nextInterval.getStart(), nextInterval.getEnd())
                            || overlapType == SegmentOverlapUtil.OverlapType.PARTIAL_OVERLAP_END) {
                        if(!altPressed || overlapType == SegmentOverlapUtil.OverlapType.PARTIAL_OVERLAP_END) {
                            final InternalMedia replacedNextInterval =
                                    new InternalMedia(updatedInterval.getEnd(), nextInterval.getEnd());
                            final TierDataIntervalUpdater nextUpdater =
                                    new TierDataIntervalUpdater(i + 1, new TierInternalMedia(replacedNextInterval));
                            updatedTierData.accept(nextUpdater);
                            updatedTierData = nextUpdater.getUpdatedTierData();

                            // refresh intervals after mutation
                            updatedTierDataIntervalVisitor.reset();
                            updatedTierData.accept(updatedTierDataIntervalVisitor);
                        }
                    }
                }
            }
        }

        final TierEdit<TierData> tierDataEdit = createTierDataTierEdit(
                tierName,
                updatedTierData,
                currentInterval.isValueAdjusting()
        );
        lastTierDataEdit = tierDataEdit;
        getParentView().getEditor().getUndoSupport().postEdit(tierDataEdit);
    }

    /**
     * Handle marker changes when current tier is the PhoneIntervals tier.
     */
    private void handlePhoneIntervalsMarkerChange(PropertyChangeEvent e, Record currentRecord) {
        final RecordIntervalTier phointIntervalTier =
                currentIntervalTierComponent.getTimelineTier().getExtension(RecordIntervalTier.class);
        if(phointIntervalTier == null) return;

        final Range recordIntervalIndices = phointIntervalTier.getIntervalRangeForRecord(currentRecord);
        if(recordIntervalIndices == null) return;

        final int offset = recordIntervalIndices.getStart();
        final int idx = currentIntervalIndex - offset;
        if(idx < 0) return;

        final int modifiers = currentInterval.getModifiers();

        // first get the word interval that contains this phone interval,
        // then all of the other phone intervals within that word interval
        final IntervalTierComponent wordIntervalTier =
                recordDataIntervalTiers.get(UserTierType.Wor.getPhonTierName());
        if(wordIntervalTier == null) return;

        final IntervalTier.Interval phoneInterval =
                currentIntervalTierComponent.getTimelineTier().getIntervals().get(currentIntervalIndex);

        final int[] wordIntervals =
                wordIntervalTier.getTimelineTier().overlappingIntervals(
                        phoneInterval,
                        SegmentOverlapUtil.OverlapType.IS_FULLY_CONTAINED,
                        SegmentOverlapUtil.OverlapType.FULLY_CONTAINS
                );
        if(wordIntervals.length == 0) return;

        final int wordIntervalIndex = wordIntervals[0];
        final IntervalTier.Interval wordInterval =
                wordIntervalTier.getTimelineTier().getIntervals().get(wordIntervalIndex);

        // obtain all phone intervals within this word interval
        final int[] phoneIntervalIndices =
                currentIntervalTierComponent.getTimelineTier().overlappingIntervals(
                        wordInterval,
                        SegmentOverlapUtil.OverlapType.FULLY_CONTAINS
                );

        final String prop = e.getPropertyName();
        final boolean isStartChange = "startMarker.time".equals(prop);
        final boolean isEndChange   = "endMarker.time".equals(prop);

        if(currentIntervalIndex == phoneIntervalIndices[0] && isStartChange) {
            handlePhoneFirstIntervalStartChange(
                    idx, currentRecord, phoneInterval, wordInterval,
                    wordIntervalIndex, modifiers
            );
        } else if(currentIntervalIndex == phoneIntervalIndices[phoneIntervalIndices.length - 1]
                && isEndChange) {
            handlePhoneLastIntervalEndChange(
                    idx, currentRecord, phoneInterval, wordInterval,
                    wordIntervalIndex, modifiers
            );
        } else {
            handlePhoneMiddleIntervalChange(
                    idx, currentRecord, phoneIntervalIndices, isStartChange, isEndChange, modifiers
            );
        }
    }

    /**
     * Apply WorTierUpdater and neighbor-adjustment logic extracted from the
     * original listener for Wor tier updates.
     */
    private Orthography updateWorIntervalsWithInterval(int idx,
                                                       InternalMedia newInterval,
                                                       Orthography wor,
                                                       int modifiers) {
        final WorTierUpdater updater = new WorTierUpdater(idx, newInterval);
        final OrthoIntervalVisitor worIntervalVisitor = new OrthoIntervalVisitor();
        wor.accept(worIntervalVisitor);
        final List<IntervalTier.Interval> worIntervals = worIntervalVisitor.getIntervals();

        wor.accept(updater);
        Orthography updatedWor = updater.getUpdatedOrthography();

        final OrthoIntervalVisitor updatedWorIntervalVisitor = new OrthoIntervalVisitor();
        updatedWor.accept(updatedWorIntervalVisitor);
        final List<IntervalTier.Interval> updatedWorIntervals = updatedWorIntervalVisitor.getIntervals();

        boolean altPressed = (modifiers & MouseEvent.ALT_DOWN_MASK) != 0;

        // adjust previous/next contiguous intervals as in original code
        for(int i = 0; i < worIntervals.size(); i++) {
            final IntervalTier.Interval oldInterval = worIntervals.get(i);
            final IntervalTier.Interval updatedInterval = updatedWorIntervals.get(i);
            if(oldInterval.getStart() != updatedInterval.getStart() ||
                    oldInterval.getEnd() != updatedInterval.getEnd()) {

                if(oldInterval.getStart() != updatedInterval.getStart() && i > 0) {
                    final IntervalTier.Interval prevInterval = worIntervals.get(i - 1);
                    final var overlapType = SegmentOverlapUtil.computeOverlap(
                            prevInterval.getStart(), prevInterval.getEnd(),
                            updatedInterval.getStart(), updatedInterval.getEnd());
                    if(SegmentOverlapUtil.areContiguous(
                                oldInterval.getStart(), oldInterval.getEnd(),
                                prevInterval.getStart(), prevInterval.getEnd())
                            || overlapType == SegmentOverlapUtil.OverlapType.PARTIAL_OVERLAP_START) {
                        if(!altPressed || overlapType == SegmentOverlapUtil.OverlapType.PARTIAL_OVERLAP_START) {
                            final InternalMedia replacedPrevInterval =
                                    new InternalMedia(prevInterval.getStart(), updatedInterval.getStart());
                            final WorTierUpdater prevUpdater =
                                    new WorTierUpdater(i - 1, replacedPrevInterval);
                            updatedWor.accept(prevUpdater);
                            updatedWor = prevUpdater.getUpdatedOrthography();

                            // refresh intervals after mutation
                            updatedWorIntervalVisitor.reset();
                            updatedWor.accept(updatedWorIntervalVisitor);
                        }
                    }
                }

                if(oldInterval.getEnd() != updatedInterval.getEnd() && i < worIntervals.size() - 1) {
                    final IntervalTier.Interval nextInterval = worIntervals.get(i + 1);
                    final var overlapType = SegmentOverlapUtil.computeOverlap(
                            nextInterval.getStart(), nextInterval.getEnd(),
                            updatedInterval.getStart(), updatedInterval.getEnd());
                    if(SegmentOverlapUtil.areContiguous(
                                oldInterval.getStart(), oldInterval.getEnd(),
                                nextInterval.getStart(), nextInterval.getEnd())
                            || overlapType == SegmentOverlapUtil.OverlapType.PARTIAL_OVERLAP_END) {
                        if(!altPressed || overlapType == SegmentOverlapUtil.OverlapType.PARTIAL_OVERLAP_END) {
                            final InternalMedia replacedNextInterval =
                                    new InternalMedia(updatedInterval.getEnd(), nextInterval.getEnd());
                            final WorTierUpdater nextUpdater =
                                    new WorTierUpdater(i + 1, replacedNextInterval);
                            updatedWor.accept(nextUpdater);
                            updatedWor = nextUpdater.getUpdatedOrthography();

                            // refresh intervals after mutation
                            updatedWorIntervalVisitor.reset();
                            updatedWor.accept(updatedWorIntervalVisitor);
                        }
                    }
                }
            }
        }
        return updatedWor;
    }

    /**
     * Apply TierDataIntervalUpdater and neighbor-adjustment logic for
     * phone intervals, extracted from original code.
     */
    private TierData updatePhoneIntervalsWithInterval(int idx,
                                                      TierData tierData,
                                                      TierInternalMedia newIntervalMedia) {
        final TierDataIntervalUpdater updater =
                new TierDataIntervalUpdater(idx, newIntervalMedia);

        final TierDataIntervalVisitor tierDataIntervalVisitor =
                new TierDataIntervalVisitor();
        tierData.accept(tierDataIntervalVisitor);
        final List<IntervalTier.Interval> oldPhoneIntervals =
                tierDataIntervalVisitor.getIntervals();

        tierData.accept(updater);
        TierData updatedPhoneIntervals = updater.getUpdatedTierData();

        // second pass for contiguous neighbors (logic identical to the
        // "first/last phone intervals" branches; call-site chooses neighbors)
        final TierDataIntervalVisitor updatedVisitor =
                new TierDataIntervalVisitor();
        updatedPhoneIntervals.accept(updatedVisitor);
        final List<IntervalTier.Interval> updatedIntervals =
                updatedVisitor.getIntervals();

        for(int i = 0; i < oldPhoneIntervals.size(); i++) {
            final IntervalTier.Interval oldInterval = oldPhoneIntervals.get(i);
            final IntervalTier.Interval updatedInterval = updatedIntervals.get(i);
            if(oldInterval.getStart() != updatedInterval.getStart()
                    || oldInterval.getEnd() != updatedInterval.getEnd()) {
                // caller may do further neighbor adjustments; no-op here
            }
        }

        return updatedPhoneIntervals;
    }

    /**
     * Create a TierEdit for the Wor tier using current editor context.
     */
    private TierEdit<Orthography> createWorTierEdit(Orthography updatedWor,
                                                    boolean valueAdjusting) {
        return new TierEdit<>(
                getParentView().getEditor().getSession(),
                getParentView().getEditor().getEventManager(),
                getParentView().getEditor().getDataModel().getTranscriber(),
                getParentView().getEditor().currentRecord(),
                getParentView().getEditor().currentRecord()
                        .getTier(UserTierType.Wor.getPhonTierName(), Orthography.class),
                updatedWor,
                valueAdjusting
        );
    }

    /**
     * Create a TierEdit for the PhoneIntervals tier using current editor context.
     */
    private TierEdit<TierData> createPhoneIntervalsEdit(TierData updated,
                                                        boolean valueAdjusting) {
        return new TierEdit<>(
                getParentView().getEditor().getSession(),
                getParentView().getEditor().getEventManager(),
                getParentView().getEditor().getDataModel().getTranscriber(),
                getParentView().getEditor().currentRecord(),
                getParentView().getEditor().currentRecord()
                        .getTier(UserTierType.PhoneIntervals.getPhonTierName(), TierData.class),
                updated,
                valueAdjusting
        );
    }

    /**
     * Create tier edit for generic TierData tier using current editor context.
     *
     * @param tierName the tier name
     * @param updated the updated tier data
     * @param valueAdjusting whether the edit is value-adjusting
     */
    private TierEdit<TierData> createTierDataTierEdit(String tierName,
                                                      TierData updated,
                                                      boolean valueAdjusting) {
        return new TierEdit<>(
                getParentView().getEditor().getSession(),
                getParentView().getEditor().getEventManager(),
                getParentView().getEditor().getDataModel().getTranscriber(),
                getParentView().getEditor().currentRecord(),
                getParentView().getEditor().currentRecord()
                        .getTier(tierName, TierData.class),
                updated,
                valueAdjusting
        );
    }

    /**
     * Handle the case where the first phone interval in a word has its start
     * time changed; adjust phone tier then wor tier, as in original code.
     */
    private void handlePhoneFirstIntervalStartChange(int idx,
                                                     Record currentRecord,
                                                     IntervalTier.Interval phoneInterval,
                                                     IntervalTier.Interval wordInterval,
                                                     int wordIntervalIndex,
                                                     int modifiers) {
        final RecordIntervalTier worIntervalTier =
                recordDataIntervalTiers.get(UserTierType.Wor.getPhonTierName())
                        .getTimelineTier().getExtension(RecordIntervalTier.class);
        if(worIntervalTier == null) return;

        final Range worRecordIntervalIndices =
                worIntervalTier.getIntervalRangeForRecord(currentRecord);
        if(worRecordIntervalIndices == null) return;

        final int worOffset = worRecordIntervalIndices.getStart();
        final int wordIdx = wordIntervalIndex - worOffset;
        final boolean altPressed = (modifiers & MouseEvent.ALT_DOWN_MASK) != 0;

        // adjust phone interval first
        final InternalMedia newPhoneInterval =
                new InternalMedia(currentInterval.getStartMarker().getTime(), phoneInterval.getEnd());
        final TierInternalMedia tierInternalMedia =
                new TierInternalMedia(newPhoneInterval);

        final Tier<TierData> phoneIntervalsTier =
                currentRecord.getTier(UserTierType.PhoneIntervals.getPhonTierName(), TierData.class);
        final TierData tierData =
                phoneIntervalsTier.getValueForTranscriber(getParentView().getEditor().getDataModel().getTranscriber())
                        .orElse(new TierData());

        final TierDataIntervalVisitor tierDataIntervalVisitor = new TierDataIntervalVisitor();
        tierData.accept(tierDataIntervalVisitor);
        final List<IntervalTier.Interval> oldPhoneIntervals =
                tierDataIntervalVisitor.getIntervals();

        TierData updatedPhoneIntervals =
                updatePhoneIntervalsWithInterval(idx, tierData, tierInternalMedia);

        // neighbor-fix for previous interval, identical to original code
        final TierDataIntervalVisitor updatedTierDataIntervalVisitor =
                new TierDataIntervalVisitor();
        updatedPhoneIntervals.accept(updatedTierDataIntervalVisitor);
        final List<IntervalTier.Interval> updatedPhoneIntervalsList =
                updatedTierDataIntervalVisitor.getIntervals();
        for(int i = 0; i < oldPhoneIntervals.size(); i++) {
            final IntervalTier.Interval oldInterval = oldPhoneIntervals.get(i);
            final IntervalTier.Interval updatedInterval = updatedPhoneIntervalsList.get(i);
            if(oldInterval.getStart() != updatedInterval.getStart() ||
                    oldInterval.getEnd() != updatedInterval.getEnd()) {
                if(oldInterval.getStart() != updatedInterval.getStart() && i > 0) {
                    final IntervalTier.Interval prevInterval = oldPhoneIntervals.get(i - 1);
                    final var overlapType = SegmentOverlapUtil.computeOverlap(
                            prevInterval.getStart(), prevInterval.getEnd(),
                            updatedInterval.getStart(), updatedInterval.getEnd());
                    if(SegmentOverlapUtil.areContiguous(
                                oldInterval.getStart(), oldInterval.getEnd(),
                                prevInterval.getStart(), prevInterval.getEnd())
                            || overlapType == SegmentOverlapUtil.OverlapType.PARTIAL_OVERLAP_START) {
                        if(!altPressed || overlapType == SegmentOverlapUtil.OverlapType.PARTIAL_OVERLAP_START) {
                            final InternalMedia replacedPrevInterval =
                                    new InternalMedia(prevInterval.getStart(),
                                            updatedInterval.getStart());
                            final TierInternalMedia replacedTierInternalMedia =
                                    new TierInternalMedia(replacedPrevInterval);
                            final TierDataIntervalUpdater prevUpdater =
                                    new TierDataIntervalUpdater(i - 1, replacedTierInternalMedia);
                            updatedPhoneIntervals.accept(prevUpdater);
                            updatedPhoneIntervals = prevUpdater.getUpdatedTierData();
                        }
                    }
                }
            }
        }

        final TierEdit<TierData> phoneIntervalsEdit =
                createPhoneIntervalsEdit(updatedPhoneIntervals, currentInterval.isValueAdjusting());
        lastPhoneIntervalsEdit = phoneIntervalsEdit;
        getParentView().getEditor().getUndoSupport().postEdit(phoneIntervalsEdit);

        // now adjust the word interval
        final InternalMedia newWordInterval =
                new InternalMedia(currentInterval.getStartMarker().getTime(),
                        wordInterval.getEnd());
        final Tier<Orthography> worTier =
                currentRecord.getTier(UserTierType.Wor.getPhonTierName(), Orthography.class);
        final Orthography wor =
                worTier.getValueForTranscriber(getParentView().getEditor().getDataModel().getTranscriber())
                        .orElse(new Orthography());

        Orthography updatedWor =
                updateWorIntervalsWithInterval(wordIdx, newWordInterval, wor, modifiers);

        final TierEdit<Orthography> worEdit =
                createWorTierEdit(updatedWor, currentInterval.isValueAdjusting());
        worEdit.setSource(recordDataIntervalTiers.get(UserTierType.PhoneIntervals.getPhonTierName()));
        lastWorEdit = worEdit;
        getParentView().getEditor().getUndoSupport().postEdit(worEdit);
    }

    /**
     * Handle the case where the last phone interval in a word has its end
     * time changed; adjust phone tier then wor tier, as in original code.
     */
    private void handlePhoneLastIntervalEndChange(int idx,
                                                  Record currentRecord,
                                                  IntervalTier.Interval phoneInterval,
                                                  IntervalTier.Interval wordInterval,
                                                  int wordIntervalIndex,
                                                  int modifiers) {
        final IntervalTierComponent wordIntervalTier =
                recordDataIntervalTiers.get(UserTierType.Wor.getPhonTierName());
        final RecordIntervalTier worIntervalTier =
                wordIntervalTier.getTimelineTier().getExtension(RecordIntervalTier.class);
        if(worIntervalTier == null) return;

        final Range worRecordIntervalIndices =
                worIntervalTier.getIntervalRangeForRecord(currentRecord);
        if(worRecordIntervalIndices == null) return;

        final int worOffset = worRecordIntervalIndices.getStart();
        final int wordIdx = wordIntervalIndex - worOffset;

        // adjust end time of phone interval
        final InternalMedia newPhoneInterval =
                new InternalMedia(phoneInterval.getStart(), currentInterval.getEndMarker().getTime());
        final TierInternalMedia tierInternalMedia =
                new TierInternalMedia(newPhoneInterval);

        final Tier<TierData> phoneIntervalsTier =
                currentRecord.getTier(UserTierType.PhoneIntervals.getPhonTierName(), TierData.class);
        final TierData tierData =
                phoneIntervalsTier.getValueForTranscriber(getParentView().getEditor().getDataModel().getTranscriber())
                        .orElse(new TierData());

        final TierDataIntervalVisitor tierDataIntervalVisitor =
                new TierDataIntervalVisitor();
        tierData.accept(tierDataIntervalVisitor);
        final List<IntervalTier.Interval> oldPhoneIntervals =
                tierDataIntervalVisitor.getIntervals();

        TierData updatedPhoneIntervals =
                updatePhoneIntervalsWithInterval(idx, tierData, tierInternalMedia);

        final boolean altPressed = (modifiers & MouseEvent.ALT_DOWN_MASK) != 0;
        // neighbor-fix for next interval, identical to original code
        final TierDataIntervalVisitor updatedTierDataIntervalVisitor =
                new TierDataIntervalVisitor();
        updatedPhoneIntervals.accept(updatedTierDataIntervalVisitor);
        final List<IntervalTier.Interval> updatedPhoneIntervalsList =
                updatedTierDataIntervalVisitor.getIntervals();
        for(int i = 0; i < oldPhoneIntervals.size(); i++) {
            final IntervalTier.Interval oldInterval = oldPhoneIntervals.get(i);
            final IntervalTier.Interval updatedInterval = updatedPhoneIntervalsList.get(i);
            if(oldInterval.getStart() != updatedInterval.getStart() ||
                    oldInterval.getEnd() != updatedInterval.getEnd()) {
                if(oldInterval.getEnd() != updatedInterval.getEnd()
                        && i < oldPhoneIntervals.size() - 1) {
                    final IntervalTier.Interval nextInterval = oldPhoneIntervals.get(i + 1);
                    final var overlapType = SegmentOverlapUtil.computeOverlap(
                            nextInterval.getStart(), nextInterval.getEnd(),
                            updatedInterval.getStart(), updatedInterval.getEnd());
                    if(SegmentOverlapUtil.areContiguous(
                                oldInterval.getStart(), oldInterval.getEnd(),
                                nextInterval.getStart(), nextInterval.getEnd())
                            || overlapType == SegmentOverlapUtil.OverlapType.PARTIAL_OVERLAP_END) {
                        if(!altPressed || overlapType == SegmentOverlapUtil.OverlapType.PARTIAL_OVERLAP_END) {
                            final InternalMedia replacedNextInterval =
                                    new InternalMedia(updatedInterval.getEnd(),
                                            nextInterval.getEnd());
                            final TierInternalMedia replacedTierInternalMedia =
                                    new TierInternalMedia(replacedNextInterval);
                            final TierDataIntervalUpdater nextUpdater =
                                    new TierDataIntervalUpdater(i + 1, replacedTierInternalMedia);
                            updatedPhoneIntervals.accept(nextUpdater);
                            updatedPhoneIntervals = nextUpdater.getUpdatedTierData();
                        }
                    }
                }
            }
        }

        final TierEdit<TierData> phoneIntervalsEdit =
                createPhoneIntervalsEdit(updatedPhoneIntervals, currentInterval.isValueAdjusting());
        lastPhoneIntervalsEdit = phoneIntervalsEdit;
        getParentView().getEditor().getUndoSupport().postEdit(phoneIntervalsEdit);

        // now adjust the word interval
        final InternalMedia newWordInterval =
                new InternalMedia(wordInterval.getStart(),
                        currentInterval.getEndMarker().getTime());

        final Tier<Orthography> worTier =
                currentRecord.getTier(UserTierType.Wor.getPhonTierName(), Orthography.class);
        final Orthography wor =
                worTier.getValueForTranscriber(getParentView().getEditor().getDataModel().getTranscriber())
                        .orElse(new Orthography());

        Orthography updatedWor =
                updateWorIntervalsWithInterval(wordIdx, newWordInterval, wor, modifiers);

        final TierEdit<Orthography> worEdit =
                createWorTierEdit(updatedWor, currentInterval.isValueAdjusting());
        worEdit.setSource(recordDataIntervalTiers.get(UserTierType.PhoneIntervals.getPhonTierName()));
        lastWorEdit = worEdit;
        getParentView().getEditor().getUndoSupport().postEdit(worEdit);
    }

    /**
     * Handle updates where the edited phone interval is not the first or last
     * in its word; only phone tier is updated and adjacent phone intervals
     * are adjusted as needed (original behavior).
     */
    private void handlePhoneMiddleIntervalChange(int idx,
                                                 Record currentRecord,
                                                 int[] phoneIntervalIndices,
                                                 boolean isStartChange,
                                                 boolean isEndChange,
                                                 int modifiers) {
        final Tier<TierData> phoneIntervalsTier =
                currentRecord.getTier(UserTierType.PhoneIntervals.getPhonTierName(), TierData.class);

        final InternalMedia newInterval =
                new InternalMedia(currentInterval.getStartMarker().getTime(),
                        currentInterval.getEndMarker().getTime());
        final TierInternalMedia tierInternalMedia =
                new TierInternalMedia(newInterval);
        final TierDataIntervalUpdater updater =
                new TierDataIntervalUpdater(idx, tierInternalMedia);

        final TierData tierData =
                phoneIntervalsTier.getValueForTranscriber(getParentView().getEditor().getDataModel().getTranscriber())
                        .orElse(new TierData());
        final TierDataIntervalVisitor tierDataIntervalVisitor =
                new TierDataIntervalVisitor();
        tierData.accept(tierDataIntervalVisitor);
        final List<IntervalTier.Interval> oldPhoneIntervals =
                tierDataIntervalVisitor.getIntervals();

        tierData.accept(updater);
        TierData updatedPhoneIntervals = updater.getUpdatedTierData();

        boolean altPressed = (modifiers & MouseEvent.ALT_DOWN_MASK) != 0;
        if(isStartChange && currentIntervalIndex > phoneIntervalIndices[0]) {
            // adjust previous interval end time
            final var prevInterval = oldPhoneIntervals.get(idx - 1);
            final var overlapType = SegmentOverlapUtil.computeOverlap(
                    prevInterval.getStart(), prevInterval.getEnd(),
                    currentInterval.getStartMarker().getTime(), currentInterval.getEndMarker().getTime());
            if(!altPressed || overlapType == SegmentOverlapUtil.OverlapType.PARTIAL_OVERLAP_START) {
                final InternalMedia adjustedPrevInternalMedia =
                        new InternalMedia(prevInterval.getStart(),
                                currentInterval.getStartMarker().getTime());
                final TierInternalMedia adjustedPrevTierInternalMedia =
                        new TierInternalMedia(adjustedPrevInternalMedia);
                final TierDataIntervalUpdater prevUpdater =
                        new TierDataIntervalUpdater(idx - 1, adjustedPrevTierInternalMedia);
                updatedPhoneIntervals.accept(prevUpdater);
                updatedPhoneIntervals = prevUpdater.getUpdatedTierData();
            }
        } else if(isEndChange
                && currentIntervalIndex < phoneIntervalIndices[phoneIntervalIndices.length - 1]) {
            // adjust next interval start time
            final var nextInterval = oldPhoneIntervals.get(idx + 1);
            final var overlapType = SegmentOverlapUtil.computeOverlap(
                    currentInterval.getStartMarker().getTime(), currentInterval.getEndMarker().getTime(),
                    nextInterval.getStart(), nextInterval.getEnd());
            if(!altPressed || overlapType == SegmentOverlapUtil.OverlapType.PARTIAL_OVERLAP_START) {
                final InternalMedia adjustedNextInternalMedia =
                        new InternalMedia(currentInterval.getEndMarker().getTime(),
                                nextInterval.getEnd());
                final TierInternalMedia adjustedNextTierInternalMedia =
                        new TierInternalMedia(adjustedNextInternalMedia);
                final TierDataIntervalUpdater nextUpdater =
                        new TierDataIntervalUpdater(idx + 1, adjustedNextTierInternalMedia);
                updatedPhoneIntervals.accept(nextUpdater);
                updatedPhoneIntervals = nextUpdater.getUpdatedTierData();
            }
        }

        final TierEdit<TierData> phoneIntervalsEdit =
                createPhoneIntervalsEdit(updatedPhoneIntervals, currentInterval.isValueAdjusting());
        lastPhoneIntervalsEdit = phoneIntervalsEdit;
        getParentView().getEditor().getUndoSupport().postEdit(phoneIntervalsEdit);
    }

    /**
     * Handle valueAdjusting property changes: begin/end grouped undo and
     * re-post final non-adjusting edits, as in original listener.
     */
    private void handleValueAdjustingChange(PropertyChangeEvent e) {
        final boolean adjusting = (boolean)e.getNewValue();
        if(adjusting) {
            final Record currentRecord = getParentView().getEditor().currentRecord();
            final Tier<Orthography> worTier =
                    currentRecord.getTier(UserTierType.Wor.getPhonTierName(), Orthography.class);
            if(worTier == null) return;

            getParentView().getEditor().getUndoSupport().beginUpdate("Adjust interval");
            originalWor =
                    worTier.getValueForTranscriber(getParentView().getEditor().getDataModel().getTranscriber())
                            .orElse(new Orthography());
        } else {
            repostLastPhoneIntervalsEditIfNeeded();
            repostLastWorEditIfNeeded();
            repostLastTierDataEditIfNeeded();
            getParentView().getEditor().getUndoSupport().endUpdate();

            // update min/max times for interval markers
            float secsPerPixel = 1.0f / getTimeModel().getPixelsPerSecond();
            currentInterval.getStartMarker().setMaxTime(
                    currentInterval.getEndMarker().getTime() - secsPerPixel);
            currentInterval.getEndMarker().setMinTime(
                    currentInterval.getStartMarker().getTime() + secsPerPixel);
        }
    }

    private void repostLastTierDataEditIfNeeded() {
        if(lastTierDataEdit == null) return;

        final TierEdit<TierData> lastEdit = new TierEdit<>(
                lastTierDataEdit.getSession(),
                lastTierDataEdit.getEditorEventManager(),
                lastTierDataEdit.getTranscriber(),
                lastTierDataEdit.getRecord(),
                lastTierDataEdit.getTier(),
                lastTierDataEdit.getNewValue(),
                false
        );
        lastEdit.setSource(lastTierDataEdit.getSource());
        getParentView().getEditor().getUndoSupport().postEdit(lastEdit);
        lastTierDataEdit = null;
    }

    private void repostLastPhoneIntervalsEditIfNeeded() {
        if(lastPhoneIntervalsEdit == null) return;

        final TierEdit<TierData> lastEdit = new TierEdit<>(
                lastPhoneIntervalsEdit.getSession(),
                lastPhoneIntervalsEdit.getEditorEventManager(),
                lastPhoneIntervalsEdit.getTranscriber(),
                lastPhoneIntervalsEdit.getRecord(),
                lastPhoneIntervalsEdit.getTier(),
                lastPhoneIntervalsEdit.getNewValue(),
                false
        );
        lastEdit.setSource(lastPhoneIntervalsEdit.getSource());
        getParentView().getEditor().getUndoSupport().postEdit(lastEdit);
        lastPhoneIntervalsEdit = null;
    }

    private void repostLastWorEditIfNeeded() {
        if(lastWorEdit == null) return;

        final TierEdit<Orthography> lastEdit = new TierEdit<>(
                lastWorEdit.getSession(),
                lastWorEdit.getEditorEventManager(),
                lastWorEdit.getTranscriber(),
                lastWorEdit.getRecord(),
                lastWorEdit.getTier(),
                lastWorEdit.getNewValue(),
                false
        );
        lastEdit.setSource(lastWorEdit.getSource());
        getParentView().getEditor().getUndoSupport().postEdit(lastEdit);
        lastWorEdit = null;
    }

    public static class WorTierUpdater extends VisitorAdapter<OrthographyElement> {

        private final int intervalIndex;

        private InternalMedia newInterval;

        private OrthographyBuilder builder = new OrthographyBuilder();

        private int currentIndex = 0;

        public WorTierUpdater(int intervalIndex, InternalMedia newInterval) {
            super();
            this.intervalIndex = intervalIndex;
            this.newInterval = newInterval;
        }

        @Visits
        public void visitInternalMedia(InternalMedia internalMedia) {
            if(currentIndex == intervalIndex) {
                builder.append(newInterval);
            } else {
                builder.append(internalMedia);
            }
            currentIndex++;
        }

        @Override
        public void fallbackVisit(OrthographyElement element) {
            builder.append(element);
        }

        public Orthography getUpdatedOrthography() {
            return builder.toOrthography();
        }
    }

    public static class TierDataIntervalUpdater extends VisitorAdapter<TierElement> {
        private final int intervalIndex;

        private TierInternalMedia newInterval;

        private List<TierElement> elements = new ArrayList<>();

        private int currentIndex = 0;

        public TierDataIntervalUpdater(int intervalIndex, TierInternalMedia newInterval) {
            super();
            this.intervalIndex = intervalIndex;
            this.newInterval = newInterval;
        }

        @Visits
        public void visitInternalMedia(TierInternalMedia internalMedia) {
            if(currentIndex == intervalIndex) {
                elements.add(newInterval);
            } else {
                elements.add(internalMedia);
            }
            currentIndex++;
        }

        @Override
        public void fallbackVisit(TierElement element) {
            elements.add(element);
        }

        public TierData getUpdatedTierData() {
            return new TierData(elements);
        }
    }

    /**
     * Undoable edit for changing tier visibility
     */
    public static class TierVisibilityEdit extends AbstractUndoableEdit {

        private final SpeechAnalysisIntervalsTier intervalsTier;

        private final String tierName;

        private final boolean newVisibility;

        private final boolean oldVisibility;

        public TierVisibilityEdit(SpeechAnalysisIntervalsTier intervalsTier, String tierName, boolean newVisibility) {
            super();
            this.intervalsTier = intervalsTier;
            this.tierName = tierName;
            this.newVisibility = newVisibility;
            this.oldVisibility = intervalsTier.isTierVisible(tierName);
        }

        @Override
        public String getPresentationName() {
            return "Change tier visibility";
        }

        @Override
        public void redo() {
            super.redo();
            intervalsTier.setTierVisible(tierName, newVisibility);
        }

        @Override
        public void undo() {
            super.undo();
            intervalsTier.setTierVisible(tierName, oldVisibility);
        }

    }
    
    private void showImportToIPATierDialog(String tierName) {
        var dialog = ca.phon.app.session.intervalTiers.IntervalTierImportDialog.createIPATierImportDialog(
            (java.awt.Frame) SwingUtilities.getWindowAncestor(this),
            getParentView().getEditor(),
            tierName
        );
        dialog.setVisible(true);
    }
    
    private void showImportToOrthographyDialog(String tierName) {
        var dialog = ca.phon.app.session.intervalTiers.IntervalTierImportDialog.createOrthographyImportDialog(
            (java.awt.Frame) SwingUtilities.getWindowAncestor(this),
            getParentView().getEditor(),
            tierName
        );
        dialog.setVisible(true);
    }
    
    private void showImportToPhoneIntervalsDialog(String tierName) {
        var dialog = ca.phon.app.session.intervalTiers.IntervalTierImportDialog.createPhoneIntervalsImportDialog(
            (java.awt.Frame) SwingUtilities.getWindowAncestor(this),
            getParentView().getEditor(),
            tierName
        );
        dialog.setVisible(true);
    }

    private void showImportToWordIntervalsDialog(String tierName) {
        var dialog = ca.phon.app.session.intervalTiers.IntervalTierImportDialog.createWordIntervalsImportDialog(
            (java.awt.Frame) SwingUtilities.getWindowAncestor(this),
            getParentView().getEditor(),
            tierName
        );
        dialog.setVisible(true);
    }

    private void showImportToRecordSegmentsDialog(String tierName) {
        var dialog = ca.phon.app.session.intervalTiers.IntervalTierImportDialog.createRecordSegmentsImportDialog(
            (java.awt.Frame) SwingUtilities.getWindowAncestor(this),
            getParentView().getEditor(),
            tierName
        );
        dialog.setVisible(true);
    }
    
    private void showImportToUserTierDialog(String tierName) {
        var dialog = ca.phon.app.session.intervalTiers.IntervalTierImportDialog.createUserTierImportDialog(
            (java.awt.Frame) SwingUtilities.getWindowAncestor(this),
            getParentView().getEditor(),
            tierName
        );
        dialog.setVisible(true);
    }

}
