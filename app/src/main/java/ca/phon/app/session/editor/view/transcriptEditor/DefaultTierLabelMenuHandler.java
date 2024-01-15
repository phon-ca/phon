package ca.phon.app.session.editor.view.transcriptEditor;

import ca.phon.app.log.LogUtil;
import ca.phon.app.session.TierTransferrable;
import ca.phon.app.session.editor.EditorEventManager;
import ca.phon.app.session.editor.undo.*;
import ca.phon.app.session.editor.view.tierManagement.TierEditorDialog;
import ca.phon.app.session.editor.view.tierManagement.TierInfoEditor;
import ca.phon.app.session.editor.view.tierManagement.TierMenuBuilder;
import ca.phon.plugin.IPluginExtensionFactory;
import ca.phon.plugin.IPluginExtensionPoint;
import ca.phon.session.*;
import ca.phon.session.Record;
import ca.phon.session.tierdata.TierData;
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
                final PhonUIAction<RecordAndTier> pasteTierAct =
                        PhonUIAction.eventConsumer(this::onPasteTier, new RecordAndTier(record, tier));
                pasteTierAct.putValue(PhonUIAction.NAME, "Paste tier");
                pasteTierAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Paste tier data");
                builder.addItem(".", pasteTierAct);
            }

            builder.addSeparator(".", "");
        }
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

    public void onPasteTier(PhonActionEvent<RecordAndTier> pae) {
        RecordAndTier data = pae.getData();
        Record destRecord = data.record();
        Tier<?> destTier = data.tier();

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

    @Override
    public void lostOwnership(Clipboard clipboard, Transferable contents) {

    }

    public record RecordAndTier(Record record, Tier<?> tier) {}
}
