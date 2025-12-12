package ca.phon.app.session.editor.view.speechAnalysis;

import ca.hedlund.jpraat.binding.fon.TextGrid;
import ca.hedlund.jpraat.binding.sys.MelderFile;
import ca.hedlund.jpraat.exceptions.PraatException;
import ca.phon.app.log.LogUtil;
import ca.phon.app.session.editor.undo.AddIntervalTierEdit;
import ca.phon.app.session.editor.undo.AddTimelineTierIntervalsEdit;
import ca.phon.plugin.IPluginExtensionFactory;
import ca.phon.plugin.IPluginExtensionPoint;
import ca.phon.session.IntervalTier;
import ca.phon.session.Session;
import ca.phon.ui.CommonModuleFrame;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.ui.menu.MenuBuilder;
import ca.phon.ui.nativedialogs.FileFilter;
import ca.phon.ui.nativedialogs.NativeDialogEvent;
import ca.phon.ui.nativedialogs.NativeDialogs;
import ca.phon.ui.nativedialogs.OpenDialogProperties;

import java.util.List;

public class TextGridIntervalTierMenuHandler implements IntervalTierMenuHandler, IPluginExtensionPoint<IntervalTierMenuHandler> {

    @Override
    public void setupMenu(SpeechAnalysisIntervalsTier speechAnalysisIntervalsTier, MenuBuilder menuBuilder) {
        final PhonUIAction<SpeechAnalysisIntervalsTier> importTextGridAction = PhonUIAction.consumer(this::onImportTextGrid, speechAnalysisIntervalsTier);
        importTextGridAction.putValue(PhonUIAction.NAME, "Import TextGrid...");
        importTextGridAction.putValue(PhonUIAction.SHORT_DESCRIPTION, "Import intervals tiers from a Praat TextGrid file");
        menuBuilder.addItem(".", importTextGridAction);
    }

    private void onImportTextGrid(SpeechAnalysisIntervalsTier speechAnalysisIntervalsTier) {
        final Session session = speechAnalysisIntervalsTier.getParentView().getEditor().getSession();

        final OpenDialogProperties props = new OpenDialogProperties();
        props.setCanChooseFiles(true);
        props.setCanChooseDirectories(false);
        props.setAllowMultipleSelection(false);
        props.setParentWindow(CommonModuleFrame.getCurrentFrame());
        props.setTitle("Import TextGrid File");
        props.setMessage("Please select a file to import");
        final FileFilter textGridFilter =  new FileFilter("TextGrid Files", "textgrid");
        props.setFileFilter(textGridFilter);
        props.setRunAsync(true);
        props.setListener((openEvent) -> {
            if(openEvent.getDialogResult() == NativeDialogEvent.OK_OPTION) {
                final String selectedFile = openEvent.getDialogData().toString();
                importTextGrid(session, speechAnalysisIntervalsTier, selectedFile);
            }
        });

        NativeDialogs.showOpenDialog(props);
    }

    private void importTextGrid(Session session, SpeechAnalysisIntervalsTier speechAnalysisIntervalsTier, String filePath) {
        try {
            final TextGrid tg = TextGrid.readFromTextFile(TextGrid.class, MelderFile.fromPath(filePath));
            final TextGridImporter importer = new TextGridImporter();
            final List<IntervalTier> importedTierData = importer.importTextGrid(tg);

            speechAnalysisIntervalsTier.getParentView().getEditor().getUndoSupport().beginUpdate("Import TextGrid");
            for(IntervalTier tier : importedTierData) {
                final IntervalTier existingTier = session.getTimeline().getTier(tier.getName());
                final IntervalTier importToTier = (existingTier != null ? existingTier : session.getTimeline().addTier(tier.getName()));

                if(existingTier == null) {
                    // perform add timeline tier edit
                    final AddIntervalTierEdit addTierEdit = new AddIntervalTierEdit(session,
                            speechAnalysisIntervalsTier.getParentView().getEditor().getEventManager(),
                            importToTier.getName());
                    speechAnalysisIntervalsTier.getParentView().getEditor().getUndoSupport().postEdit(addTierEdit);
                }

                // add all intervals to the tier
                final AddTimelineTierIntervalsEdit addIntervalsEdit = new AddTimelineTierIntervalsEdit(session,
                        speechAnalysisIntervalsTier.getParentView().getEditor().getEventManager(),
                        importToTier.getName(), tier.getIntervals());
                speechAnalysisIntervalsTier.getParentView().getEditor().getUndoSupport().postEdit(addIntervalsEdit);
            }
            speechAnalysisIntervalsTier.getParentView().getEditor().getUndoSupport().endUpdate();
        } catch (PraatException e) {
            LogUtil.severe(e);
        }
    }

    @Override
    public Class<?> getExtensionType() {
        return IntervalTierMenuHandler.class;
    }

    @Override
    public IPluginExtensionFactory<IntervalTierMenuHandler> getFactory() {
        return (args) -> this;
    }

}
