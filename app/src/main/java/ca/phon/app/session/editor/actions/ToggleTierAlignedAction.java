package ca.phon.app.session.editor.actions;

import ca.phon.app.session.editor.EditorEventManager;
import ca.phon.app.session.editor.SessionEditor;
import ca.phon.app.session.editor.undo.SessionEditUndoSupport;
import ca.phon.app.session.editor.undo.ToggleTierAlignedEdit;
import ca.phon.session.Session;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class ToggleTierAlignedAction extends SessionEditorAction {

    private final static String SHORT_DESC = "Toggle tier as an aligned tier";

    private final String tierName;

    public ToggleTierAlignedAction(SessionEditor editor, String tierName) {
        this(editor.getSession(), editor.getEventManager(), editor.getUndoSupport(), tierName);
    }

    public ToggleTierAlignedAction(Session session, EditorEventManager eventManager, SessionEditUndoSupport undoSupport, String tierName) {
        super(session, eventManager, undoSupport);

        this.tierName = tierName;
        boolean isAligned = !getSession().getTier(tierName).isExcludeFromAlignment();
        putValue(NAME, isAligned ? "Remove tier from cross tier alignment" : "Include tier in cross tier alignment");
        putValue(SHORT_DESCRIPTION, SHORT_DESC);
        putValue(SMALL_ICON, IconManager.getInstance().getFontIcon(IconManager.GoogleMaterialDesignIconsFontName,
                isAligned ? "format_align_left" : "format_align_justify", IconSize.SMALL, UIManager.getColor("Button.foreground")));
    }

    @Override
    public void hookableActionPerformed(ActionEvent ae) {
        final boolean isAligned = !getSession().getTier(tierName).isExcludeFromAlignment();

        final ToggleTierAlignedEdit edit = new ToggleTierAlignedEdit(getSession(), getEventManager(), tierName, !isAligned);
        getUndoSupport().postEdit(edit);
    }

}
