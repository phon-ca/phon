package ca.phon.app.session.editor.view.transcriptEditor;

import ca.phon.app.log.LogUtil;
import ca.phon.app.session.TierTransferrable;
import ca.phon.app.session.editor.EditorEvent;
import ca.phon.app.session.editor.EditorEventManager;
import ca.phon.app.session.editor.EditorEventType;
import ca.phon.app.session.editor.SessionEditor;
import ca.phon.app.session.editor.undo.*;
import ca.phon.app.session.editor.view.tier_management.TierEditorDialog;
import ca.phon.app.session.editor.view.tier_management.TierInfoEditor;
import ca.phon.app.session.editor.view.tier_management.TierOrderingEditorView;
import ca.phon.app.session.editor.view.tier_management.actions.NewTierAction;
import ca.phon.ipa.IPATranscript;
import ca.phon.plugin.IPluginExtensionFactory;
import ca.phon.plugin.IPluginExtensionPoint;
import ca.phon.session.*;
import ca.phon.session.Record;
import ca.phon.session.tierdata.TierData;
import ca.phon.syllabifier.Syllabifier;
import ca.phon.syllabifier.SyllabifierLibrary;
import ca.phon.ui.CommonModuleFrame;
import ca.phon.ui.FontFormatter;
import ca.phon.ui.action.PhonActionEvent;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.ui.fonts.FontPreferences;
import ca.phon.ui.menu.MenuBuilder;
import ca.phon.ui.nativedialogs.FontDialogProperties;
import ca.phon.ui.nativedialogs.NativeDialogEvent;
import ca.phon.ui.nativedialogs.NativeDialogListener;
import ca.phon.ui.nativedialogs.NativeDialogs;
import ca.phon.ui.toast.Toast;
import ca.phon.ui.toast.ToastFactory;
import ca.phon.util.Tuple;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.*;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;

public class DefaultTierLabelMenuHandler implements TierLabelMenuHandler, IPluginExtensionPoint<TierLabelMenuHandler>, ClipboardOwner {
    private Session session;
    private EditorEventManager eventManager;
    private SessionEditUndoSupport undoSupport;
    private final ImageIcon ADD_ICON =
            IconManager.getInstance().getIcon("actions/list-add", IconSize.SMALL);
    private final ImageIcon REMOVE_ICON =
            IconManager.getInstance().getIcon("actions/list-remove", IconSize.SMALL);
    private final ImageIcon COPY_ICON =
            IconManager.getInstance().getIcon("actions/edit-copy", IconSize.SMALL);



    @Override
    public void addMenuItems(MenuBuilder builder, Session session, EditorEventManager eventManager, SessionEditUndoSupport undoSupport, Tier<?> tier, Record record) {
        appendMenuItems(builder, session, eventManager, undoSupport, tier.getName(), tier, record);
    }

    @Override
    public void addMenuItems(MenuBuilder builder, Session session, EditorEventManager eventManager, SessionEditUndoSupport undoSupport, TierViewItem item) {
        appendMenuItems(builder, session, eventManager, undoSupport, item.getTierName(), null, null);
    }

    private void appendMenuItems(MenuBuilder builder, Session session, EditorEventManager eventManager, SessionEditUndoSupport undoSupport, String tierName, Tier<?> tier, Record record) {
        this.session = session;
        this.eventManager = eventManager;
        this.undoSupport = undoSupport;

        var tierView = session.getTierView();
        TierViewItem currentTVI = tierView
            .stream()
            .filter(item -> item.getTierName().equals(tierName))
            .findFirst()
            .orElse(null);
        if (currentTVI == null) return;

        if (tier != null && record != null) {
            final PhonUIAction<Tier<?>> copyTierAct = PhonUIAction.eventConsumer(this::onCopyTier, tier);
            copyTierAct.putValue(PhonUIAction.NAME, "Copy tier");
            copyTierAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Copy tier contents to clipboard");
            builder.addItem(".", copyTierAct);

            if(Toolkit.getDefaultToolkit().getSystemClipboard().isDataFlavorAvailable(TierTransferrable.FLAVOR)
                    || Toolkit.getDefaultToolkit().getSystemClipboard().isDataFlavorAvailable(DataFlavor.stringFlavor)) {
                final PhonUIAction<Tuple<Record, Tier<?>>> pasteTierAct =
                        PhonUIAction.eventConsumer(this::onPasteTier, new Tuple<>(record, tier));
                pasteTierAct.putValue(PhonUIAction.NAME, "Paste tier");
                pasteTierAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Paste tier data");
                builder.addItem(".", pasteTierAct);
            }

            builder.addSeparator(".", "");
        }

        JMenuItem hideTier = new JMenuItem();
        PhonUIAction<TierViewItem> hideTierAct = PhonUIAction.eventConsumer(this::hideTier, currentTVI);
        hideTierAct.putValue(PhonUIAction.NAME, "Hide tier");
        hideTierAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Hide " + currentTVI.getTierName());
        hideTier.setAction(hideTierAct);
        builder.addItem(".", hideTier);

        JMenu showHiddenTierMenu = new JMenu("Show hidden tier");
        builder.addMenu(".", showHiddenTierMenu);
        var hiddenTierView = tierView.stream().filter(item -> !item.isVisible()).toList();
        if (!hiddenTierView.isEmpty()) {
            for (TierViewItem item : hiddenTierView) {
                JMenuItem showTier = new JMenuItem();
                PhonUIAction<TierViewItem> showTierAct = PhonUIAction.eventConsumer(this::showTier, item);
                showTierAct.putValue(PhonUIAction.NAME, item.getTierName());
                showTierAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "show " + item.getTierName());
                showTier.setAction(showTierAct);
                builder.addItem("./Show hidden tier", showTier);
            }
        }

        JMenuItem toggleLockTier = new JMenuItem();
        PhonUIAction<TierViewItem> toggleLockTierAct = PhonUIAction.eventConsumer(this::toggleLockTier, currentTVI);
        toggleLockTierAct.putValue(PhonUIAction.NAME, currentTVI.isTierLocked() ? "Unlock tier" : "Lock tier");
        toggleLockTierAct.putValue(
                PhonUIAction.SHORT_DESCRIPTION,
                currentTVI.isTierLocked() ? "Unlock " : " Lock " + currentTVI.getTierName()
        );
        toggleLockTier.setAction(toggleLockTierAct);
        builder.addItem(".", toggleLockTier);

        builder.addSeparator(".", "");

        if (!currentTVI.getTierFont().equals("default")) {
            JMenuItem resetFont = new JMenuItem();
            PhonUIAction<TierViewItem> resetFontAct = PhonUIAction.eventConsumer(this::resetTierFont, currentTVI);
            resetFontAct.putValue(PhonUIAction.NAME, "Reset font");
            resetFontAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Reset tier font to default");
            resetFont.setAction(resetFontAct);
            builder.addItem(".", resetFont);
        }

        JMenu fontMenu = new JMenu("Font");
        setupFontMenu(new MenuBuilder(fontMenu), currentTVI);
        builder.addItem(".", fontMenu);

        builder.addSeparator(".", "");

        if (tierView.indexOf(currentTVI) > 0) {
            JMenuItem moveTierUp = new JMenuItem();
            PhonUIAction<TierViewItem> moveTierUpAct = PhonUIAction.eventConsumer(this::moveTierUp, currentTVI);
            moveTierUpAct.putValue(PhonUIAction.NAME, "Move tier up");
            moveTierUpAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Move " + currentTVI.getTierName() + " up");
            moveTierUp.setAction(moveTierUpAct);
            builder.addItem(".", moveTierUp);
        }

        if (tierView.indexOf(currentTVI) < tierView.size() - 1) {
            JMenuItem moveTierDown = new JMenuItem();
            PhonUIAction<TierViewItem> moveTierDownAct = PhonUIAction.eventConsumer(this::moveTierDown, currentTVI);
            moveTierDownAct.putValue(PhonUIAction.NAME, "Move tier down");
            moveTierDownAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Move " + currentTVI.getTierName() + " down");
            moveTierDown.setAction(moveTierDownAct);
            builder.addItem(".", moveTierDown);
        }

        builder.addSeparator(".", "");

        JMenuItem newTier = new JMenuItem();
        PhonUIAction<Void> newTierAct = PhonUIAction.runnable(this::newTier);
        newTierAct.putValue(PhonUIAction.NAME, "New tier...");
        newTierAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Add a new tier to the session");
        newTierAct.putValue(PhonUIAction.SMALL_ICON, ADD_ICON);
        newTier.setAction(newTierAct);
        builder.addItem(".", newTier);

        JMenuItem duplicateTier = new JMenuItem();
        PhonUIAction<TierViewItem> duplicateTierAct = PhonUIAction.eventConsumer(this::duplicateTier, currentTVI);
        duplicateTierAct.putValue(PhonUIAction.NAME, "Duplicate tier");
        duplicateTierAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Duplicate " + currentTVI.getTierName());
        duplicateTierAct.putValue(PhonUIAction.SMALL_ICON, COPY_ICON);
        duplicateTier.setAction(duplicateTierAct);
        builder.addItem(".", duplicateTier);

        JMenuItem deleteTier = new JMenuItem();
        PhonUIAction<TierViewItem> deleteTierAct = PhonUIAction.eventConsumer(this::deleteTier, currentTVI);
        deleteTierAct.putValue(PhonUIAction.NAME, "Delete tier");
        deleteTierAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Delete " + currentTVI.getTierName());
        deleteTierAct.putValue(PhonUIAction.SMALL_ICON, REMOVE_ICON);
        deleteTier.setAction(deleteTierAct);
        builder.addItem(".", deleteTier);
    }

    @Override
    public Class<?> getExtensionType() {
        return TierLabelMenuHandler.class;
    }

    @Override
    public IPluginExtensionFactory<TierLabelMenuHandler> getFactory() {
        return args -> this;
    }



    // region Copy / Paste

    public void onCopyTier(PhonActionEvent<Tier<?>> pae) {
        Tier<?> tier = pae.getData();

        TierTransferrable tierTrans = new TierTransferrable(tier);
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(tierTrans, this);
    }

    public void onPasteTier(PhonActionEvent<Tuple<Record, Tier<?>>> pae) {
        Tuple<Record, Tier<?>> tuple = pae.getData();
        Record destRecord = tuple.getObj1();
        Tier<?> destTier = tuple.getObj2();

        Transferable transferable = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(this);
        if(transferable.isDataFlavorSupported(TierTransferrable.FLAVOR)) {
            try {
                TierTransferrable tierTransferrable = (TierTransferrable) transferable.getTransferData(TierTransferrable.FLAVOR);
                pasteTier(destRecord, destTier, tierTransferrable.getTier().toString());
            } catch (IOException | UnsupportedFlavorException e) {
                Toolkit.getDefaultToolkit().beep();
                LogUtil.warning(e);
            }
        } else if(transferable.isDataFlavorSupported(DataFlavor.stringFlavor)) {
            try {
                String clipboardText = Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor).toString();
                pasteTier(destRecord, destTier, clipboardText);
            } catch (IOException | UnsupportedFlavorException e) {
                Toolkit.getDefaultToolkit().beep();
                LogUtil.severe(e);
            }
        } else {
            Toolkit.getDefaultToolkit().beep();
            LogUtil.warning("Pasting from other data flavors not supported");
        }
    }

    private void pasteTier(Record destRecord, Tier<?> destTier, String text) {
        final TierEdit<?> tierEdit = new TierEdit<>(session, eventManager, destRecord, destTier, text);
        undoSupport.postEdit(tierEdit);
    }
    // endregion Copy / Paste

    // region Tier View Item

    private void hideTier(PhonActionEvent<TierViewItem> pae) {
        TierViewItem tvi = pae.getData();
        TierViewItem newTVI = SessionFactory.newFactory().createTierViewItem(
            tvi.getTierName(),
            false,
            tvi.getTierFont(),
            tvi.isTierLocked()
        );
        TierViewItemEdit edit = new TierViewItemEdit(session, eventManager, tvi, newTVI);
        undoSupport.postEdit(edit);
    }

    private void showTier(PhonActionEvent<TierViewItem> pae) {
        TierViewItem tvi = pae.getData();
        TierViewItem newTVI = SessionFactory.newFactory().createTierViewItem(
            tvi.getTierName(),
            true,
            tvi.getTierFont(),
            tvi.isTierLocked()
        );
        TierViewItemEdit edit = new TierViewItemEdit(session, eventManager, tvi, newTVI);
        undoSupport.postEdit(edit);
    }

    private void toggleLockTier(PhonActionEvent<TierViewItem> pae) {
        TierViewItem tvi = pae.getData();
        TierViewItem newTVI = SessionFactory.newFactory().createTierViewItem(
            tvi.getTierName(),
            tvi.isVisible(),
            tvi.getTierFont(),
            !tvi.isTierLocked()
        );
        TierViewItemEdit edit = new TierViewItemEdit(session, eventManager, tvi, newTVI);
        undoSupport.postEdit(edit);
    }

    // endregion Tier View Item

    // region Font

    private void resetTierFont(PhonActionEvent<TierViewItem> pae) {
        TierViewItem tvi = pae.getData();
        TierViewItem newTVI = SessionFactory.newFactory().createTierViewItem(
            tvi.getTierName(),
            tvi.isVisible(),
            "default",
            tvi.isTierLocked()
        );
        TierViewItemEdit edit = new TierViewItemEdit(session, eventManager, tvi, newTVI);
        undoSupport.postEdit(edit);
    }

    private void setupFontMenu(MenuBuilder builder, TierViewItem tvi) {
        final ImageIcon icon =
                IconManager.getInstance().getIcon("apps/preferences-desktop-font", IconSize.SMALL);
        final ImageIcon addIcon =
                IconManager.getInstance().getIcon("actions/list-add", IconSize.SMALL);
        final ImageIcon subIcon =
                IconManager.getInstance().getIcon("actions/list-remove", IconSize.SMALL);
        final ImageIcon boldIcon =
                IconManager.getInstance().getIcon("actions/format-text-bold", IconSize.SMALL);
        final ImageIcon italicIcon =
                IconManager.getInstance().getIcon("actions/format-text-italic", IconSize.SMALL);

        Font currentFont = ("default".equals(tvi.getTierFont()) ? FontPreferences.getTierFont() : Font.decode(tvi.getTierFont()));

        final PhonUIAction<Tuple<TierViewItem, Integer>> toggleBoldAct =
                PhonUIAction.eventConsumer(this::onToggleStyle, new Tuple<>(tvi, Font.BOLD));
        toggleBoldAct.putValue(PhonUIAction.NAME, "Bold");
        toggleBoldAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Toggle bold modifier");
        toggleBoldAct.putValue(PhonUIAction.SELECTED_KEY, currentFont.isBold());
        toggleBoldAct.putValue(PhonUIAction.SMALL_ICON, boldIcon);
        builder.addItem(".", new JCheckBoxMenuItem(toggleBoldAct));

        final PhonUIAction<Tuple<TierViewItem, Integer>> toggleItalicAct =
                PhonUIAction.eventConsumer(this::onToggleStyle, new Tuple<>(tvi, Font.ITALIC));
        toggleItalicAct.putValue(PhonUIAction.NAME, "Italic");
        toggleItalicAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Toggle italic modifier");
        toggleItalicAct.putValue(PhonUIAction.SELECTED_KEY, currentFont.isItalic());
        toggleItalicAct.putValue(PhonUIAction.SMALL_ICON, italicIcon);
        builder.addItem(".", new JCheckBoxMenuItem(toggleItalicAct));

        final PhonUIAction<TierViewItem> onIncreaseFontSize = PhonUIAction.eventConsumer(this::onIncreaseFontSize, tvi);
        onIncreaseFontSize.putValue(PhonUIAction.NAME, "Increase size");
        onIncreaseFontSize.putValue(PhonUIAction.SHORT_DESCRIPTION, "Increase point size by 2");
        onIncreaseFontSize.putValue(PhonUIAction.SMALL_ICON, addIcon);
        builder.addItem(".", onIncreaseFontSize);

        final PhonUIAction<TierViewItem> onDecreaseFontSize = PhonUIAction.eventConsumer(this::onDecreaseFontSize, tvi);
        onDecreaseFontSize.putValue(PhonUIAction.NAME, "Decrease size");
        onDecreaseFontSize.putValue(PhonUIAction.SHORT_DESCRIPTION, "Decrease point size by 2");
        onDecreaseFontSize.putValue(PhonUIAction.SMALL_ICON, subIcon);
        builder.addItem(".", onDecreaseFontSize);

        builder.addSeparator(".", "suggested-fonts");

        JMenuItem headerItem = new JMenuItem("-- Suggested Fonts --");
        headerItem.setEnabled(false);
        builder.addItem(".", headerItem);

        for(int i = 0; i < FontPreferences.SUGGESTED_IPA_FONT_NAMES.length; i++) {
            String suggestedFont = FontPreferences.SUGGESTED_IPA_FONT_NAMES[i];
            String fontString = String.format("%s-PLAIN-12", suggestedFont);
            // font not found
            if(Font.decode(fontString).getFamily().equals("Dialog")) continue;

            final PhonUIAction<Tuple<TierViewItem, Integer>> selectSuggestedFont =
                    PhonUIAction.eventConsumer(this::onSelectSuggestedFont, new Tuple<>(tvi, i));
            selectSuggestedFont.putValue(PhonUIAction.NAME, suggestedFont);
            selectSuggestedFont.putValue(PhonUIAction.SHORT_DESCRIPTION, "Use font: " + suggestedFont);
            builder.addItem(".", selectSuggestedFont);
        }

        builder.addSeparator(".", "font-dialog");
        final PhonUIAction<TierViewItem> defaultAct = PhonUIAction.eventConsumer(this::onSelectFont, tvi);
        defaultAct.putValue(PhonUIAction.NAME, "Select font....");
        defaultAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Select font using font selection dialog");
        defaultAct.putValue(PhonUIAction.LARGE_ICON_KEY, icon);
        builder.addItem(".", defaultAct);
    }

    public void onToggleStyle(PhonActionEvent<Tuple<TierViewItem, Integer>> pae) {
        if(pae.getData() == null) return;
        Tuple<TierViewItem, Integer> tuple = pae.getData();
        TierViewItem tvi = tuple.getObj1();
        int style = tuple.getObj2();

        Font currentFont = ("default".equals(tvi.getTierFont()) ? FontPreferences.getTierFont() : Font.decode(tvi.getTierFont()));
        int fontStyle = currentFont.getStyle();
        fontStyle ^= style;
        Font font = currentFont.deriveFont(fontStyle);

        TierViewItem newTVI = SessionFactory.newFactory().createTierViewItem(
            tvi.getTierName(),
            tvi.isVisible(),
            (new FontFormatter()).format(font),
            tvi.isTierLocked()
        );

        final TierViewItemEdit edit = new TierViewItemEdit(session, eventManager, tvi, newTVI);
        undoSupport.postEdit(edit);
    }

    public void onIncreaseFontSize(PhonActionEvent<TierViewItem> pae) {
        TierViewItem tvi = pae.getData();
        if(tvi == null) return;

        Font currentFont = ("default".equals(tvi.getTierFont()) ? FontPreferences.getTierFont() : Font.decode(tvi.getTierFont()));
        Font biggerFont = currentFont.deriveFont(Math.min(72.0f, currentFont.getSize() + 2.0f));

        TierViewItem newTVI = SessionFactory.newFactory().createTierViewItem(
            tvi.getTierName(),
            tvi.isVisible(),
            (new FontFormatter()).format(biggerFont),
            tvi.isTierLocked()
        );

        final TierViewItemEdit edit = new TierViewItemEdit(session, eventManager, tvi, newTVI);
        undoSupport.postEdit(edit);
    }

    public void onDecreaseFontSize(PhonActionEvent<TierViewItem> pae) {
        TierViewItem tvi = pae.getData();
        if(tvi == null) return;

        Font currentFont = ("default".equals(tvi.getTierFont()) ? FontPreferences.getTierFont() : Font.decode(tvi.getTierFont()));
        Font smallerFont = currentFont.deriveFont(Math.max(1.0f, currentFont.getSize() - 2.0f));

        TierViewItem newTVI = SessionFactory.newFactory().createTierViewItem(
            tvi.getTierName(),
            tvi.isVisible(),
            (new FontFormatter()).format(smallerFont),
            tvi.isTierLocked()
        );

        final TierViewItemEdit edit = new TierViewItemEdit(session, eventManager, tvi, newTVI);
        undoSupport.postEdit(edit);
    }

    public void onSelectSuggestedFont(PhonActionEvent<Tuple<TierViewItem, Integer>> pae) {
        if(pae.getData() == null) return;
        Tuple<TierViewItem, Integer> tuple = pae.getData();
        TierViewItem tvi = tuple.getObj1();
        int idx = tuple.getObj2();

        Font currentFont = ("default".equals(tvi.getTierFont()) ? FontPreferences.getTierFont() : Font.decode(tvi.getTierFont()));
        String suggestedFont = FontPreferences.SUGGESTED_IPA_FONT_NAMES[idx];
        float currentFontSize = currentFont.getSize();

        Font font = Font.decode(String.format("%s-PLAIN-%d", suggestedFont, (int)currentFontSize));

        TierViewItem newTVI = SessionFactory.newFactory().createTierViewItem(
            tvi.getTierName(),
            tvi.isVisible(),
            (new FontFormatter()).format(font),
            tvi.isTierLocked()
        );

        final TierViewItemEdit edit = new TierViewItemEdit(session, eventManager, tvi, newTVI);
        undoSupport.postEdit(edit);
    }

    public void onSelectFont(PhonActionEvent<TierViewItem> pae) {
        if(pae.getData() ==  null) return;
        TierViewItem tvi = pae.getData();
        Font currentFont = ("default".equals(tvi.getTierFont()) ? FontPreferences.getTierFont() : Font.decode(tvi.getTierFont()));

        final FontDialogProperties props = new FontDialogProperties();
        props.setRunAsync(true);
        props.setListener(new FontDlgListener(tvi));
        props.setFontName(currentFont.getName());
        props.setFontSize(currentFont.getSize());
        props.setBold(currentFont.isBold());
        props.setItalic(currentFont.isItalic());
        props.setParentWindow(CommonModuleFrame.getCurrentFrame());

        NativeDialogs.showFontDialog(props);
    }

    //  endregion Font

    // region Tier View

    private void moveTierUp(PhonActionEvent<TierViewItem> pae) {
        TierViewItem tvi = pae.getData();

        var oldTV = session.getTierView();

        ArrayList<TierViewItem> newTV = new ArrayList<>(oldTV);
        int tviIndex = oldTV.indexOf(tvi);
        newTV.remove(tviIndex);
        newTV.add(tviIndex-1, tvi);

        TierViewEdit edit = new TierViewEdit(session, eventManager, oldTV, newTV);
        undoSupport.postEdit(edit);
    }

    private void moveTierDown(PhonActionEvent<TierViewItem> pae) {
        TierViewItem tvi = pae.getData();

        var oldTV = session.getTierView();

        ArrayList<TierViewItem> newTV = new ArrayList<>(oldTV);
        int tviIndex = oldTV.indexOf(tvi);
        newTV.remove(tviIndex);
        newTV.add(tviIndex+1, tvi);

        TierViewEdit edit = new TierViewEdit(session, eventManager, oldTV, newTV);
        undoSupport.postEdit(edit);
    }

    private void newTier() {
        TierEditorDialog newTierDialog = new TierEditorDialog(false);
        TierInfoEditor tierEditor = newTierDialog.getTierEditor();
        newTierDialog.add(tierEditor);
        newTierDialog.setTitle("New Tier");
        newTierDialog.setModal(true);
        newTierDialog.pack();

        if(newTierDialog.showDialog()) {
            // get tier info
            String tierName = tierEditor.getTierName();
            tierName = StringUtils.strip(tierName);
            if(tierName.length() == 0) {
                return;
            }

            boolean tierExists = false;
            if(SystemTierType.isSystemTier(tierName)) {
                tierExists = true;
            } else {
                for(TierDescription td:session.getUserTiers()) {
                    if(td.getName().equals(tierName)) {
                        tierExists = true;
                        break;
                    }
                }
            }

            if(tierExists){
                final Toast toast = ToastFactory.makeToast("A tier with name " + tierEditor.getTierName() + " already exists.");
                toast.start(tierEditor);
                return;
            }

            // create tier
            final TierDescription tierDescription = tierEditor.createTierDescription();
            final TierViewItem tierViewItem = tierEditor.createTierViewItem();

            final AddTierEdit edit = new AddTierEdit(session, eventManager, tierDescription, tierViewItem);
            undoSupport.postEdit(edit);
        }
    }

    private void duplicateTier(PhonActionEvent<TierViewItem> pae) {
        TierViewItem tvi = pae.getData();
        int duplicatedTierIndex = session.getTierView().indexOf(tvi);

        TierEditorDialog newTierDialog = new TierEditorDialog(true);
        TierInfoEditor tierEditor = newTierDialog.getTierEditor();
        newTierDialog.add(tierEditor);
        newTierDialog.setTitle("Duplicate Tier");
        newTierDialog.getHeader().setHeaderText("Duplicate Tier");
        newTierDialog.getHeader().setDescText("Create a new tier with the contents of " + tvi.getTierName());
        newTierDialog.setModal(true);
        newTierDialog.pack();

        if(newTierDialog.showDialog()) {
            // get tier info
            String tierName = tierEditor.getTierName();
            tierName = StringUtils.strip(tierName);
            if(tierName.length() == 0) {
                return;
            }

            boolean tierExists = false;
            if(SystemTierType.isSystemTier(tierName)) {
                tierExists = true;
            } else {
                for(TierDescription td:session.getUserTiers()) {
                    if(td.getName().equals(tierName)) {
                        tierExists = true;
                        break;
                    }
                }
            }

            if(tierExists){
                final Toast toast = ToastFactory.makeToast("A tier with name " + tierEditor.getTierName() + " already exists.");
                toast.start(tierEditor);
                return;
            }

            // create tier
            final TierDescription tierDescription = tierEditor.createTierDescription();
            final TierViewItem tierViewItem = tierEditor.createTierViewItem();

            undoSupport.beginUpdate();

            final AddTierEdit edit = new AddTierEdit(session, eventManager, tierDescription, tierViewItem, duplicatedTierIndex+1);
            undoSupport.postEdit(edit);

            for (Record r : session.getRecords()) {
                Tier<TierData> existingTier = r.getTier(tvi.getTierName(), TierData.class);
                if (existingTier == null) continue;
                Tier<TierData> dupTier = r.getTier(tierDescription.getName(), TierData.class);

                TierData existingVal = existingTier.getValue();
                try {
                    TierEdit<TierData> tierEdit = new TierEdit<>(session, eventManager, r, dupTier, TierData.parseTierData(existingVal.toString()));
                    undoSupport.postEdit(tierEdit);
                } catch (ParseException pe) {}
            }

            undoSupport.endUpdate();
        }
    }

    private void deleteTier(PhonActionEvent<TierViewItem> pae) {
        TierViewItem tvi = pae.getData();
        TierDescription td = session
            .getTiers()
            .stream()
            .filter(tier -> tier.getName().equals(tvi.getTierName()))
            .findFirst()
            .orElse(null);
        if (td == null) return;
        final RemoveTierEdit edit = new RemoveTierEdit(session, eventManager, td, tvi);
        undoSupport.postEdit(edit);
    }

    // endregion Tier View

    @Override
    public void lostOwnership(Clipboard clipboard, Transferable contents) {

    }

    private class FontDlgListener implements NativeDialogListener {

        private TierViewItem tvi;

        public FontDlgListener(TierViewItem tvi) {
            this.tvi = tvi;
        }

        @Override
        public void nativeDialogEvent(NativeDialogEvent nativeDialogEvent) {
            if(nativeDialogEvent.getDialogResult() == NativeDialogEvent.OK_OPTION) {
                Font selectedFont = (Font)nativeDialogEvent.getDialogData();

                TierViewItem newTVI = SessionFactory.newFactory().createTierViewItem(
                    tvi.getTierName(),
                    tvi.isVisible(),
                    (new FontFormatter()).format(selectedFont),
                    tvi.isTierLocked()
                );

                final TierViewItemEdit edit = new TierViewItemEdit(session, eventManager, tvi, newTVI);
                undoSupport.postEdit(edit);
            }
        }
    };
}
