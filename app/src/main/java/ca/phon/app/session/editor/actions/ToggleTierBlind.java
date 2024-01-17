package ca.phon.app.session.editor.actions;

import ca.phon.app.session.editor.EditorEventType;
import ca.phon.app.session.editor.SessionEditor;
import ca.phon.app.session.editor.undo.TierBlindEdit;
import ca.phon.session.SessionFactory;
import ca.phon.session.SystemTierType;
import ca.phon.session.TierDescription;
import ca.phon.session.TierViewItem;
import ca.phon.ui.CommonModuleFrame;
import ca.phon.ui.nativedialogs.MessageDialogProperties;
import ca.phon.ui.nativedialogs.NativeDialogs;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.List;

public class ToggleTierBlind extends SessionEditorAction {

    private final static String SHORT_DESC = "Toggle tier as a blind tier";

    private final String tierName;

    public ToggleTierBlind(SessionEditor editor, String tierName) {
        super(editor);

        this.tierName = tierName;
        boolean isBlind = getEditor().getSession().getBlindTiers().contains(tierName);
        putValue(NAME, isBlind ? "Remove tier from blind transcription" : "Include tier in blind transcription");
        putValue(SHORT_DESCRIPTION, SHORT_DESC);
        putValue(SMALL_ICON, IconManager.getInstance().getFontIcon(IconManager.GoogleMaterialDesignIconsFontName,
               isBlind ? "layers_clear" : "layers", IconSize.SMALL, UIManager.getColor("Button.foreground")));
    }

    @Override
    public void hookableActionPerformed(ActionEvent ae) {
        final boolean isBlind = getEditor().getSession().getBlindTiers().contains(tierName);

        if(isBlind) {
            final MessageDialogProperties props = new MessageDialogProperties();
            props.setParentWindow(CommonModuleFrame.getCurrentFrame());
            props.setHeader("Remove tier from blind transcription");
            props.setTitle("Remove tier from blind transcription");
            props.setMessage("Are you sure you want to remove tier '" + tierName + "' from blind transcription?");
            props.setOptions(MessageDialogProperties.okCancelOptions);
            props.setRunAsync(false);

            final int selection = NativeDialogs.showMessageDialog(props);
            if(selection != 0) return;
        }

        final TierBlindEdit edit = new TierBlindEdit(getEditor().getSession(), getEditor().getEventManager(), tierName, !isBlind);
        getEditor().getUndoSupport().postEdit(edit);
    }

}
