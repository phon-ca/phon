package ca.phon.app.session.editor.view.tier_management;

import ca.phon.app.session.editor.SessionEditor;
import ca.phon.app.session.editor.undo.AddTierEdit;
import ca.phon.app.session.editor.actions.NewTierAction;
import ca.phon.session.*;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.ui.menu.MenuBuilder;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Methods for building menus for tier management and tier ordering.
 */
public class TierMenuBuilder {

    /**
     * Build a menu for adding new tiers.  A list of tiers from UserTierType are provided as
     * items along with a custom item which displays the new tier dialog.
     *
     * @param menuBuilder
     */
    public static void setupNewTierMenu(SessionEditor editor, MenuBuilder menuBuilder) {
        List<UserTierType> availableUserTierTypes = new ArrayList<>(List.of(UserTierType.values()));
        for (UserTierType userTierType : availableUserTierTypes) {
            boolean exists = editor.getSession().getUserTiers().get(userTierType.getPhonTierName()) != null;
            PhonUIAction<Void> addTierAct = PhonUIAction.runnable(() -> {
                TierDescription td = SessionFactory.newFactory().createTierDescription(userTierType);
                TierViewItem tvi = SessionFactory.newFactory().createTierViewItem(td.getName());

                AddTierEdit edit = new AddTierEdit(editor, td, tvi);
                editor.getUndoSupport().postEdit(edit);
            });
            addTierAct.putValue(PhonUIAction.NAME, userTierType.getPhonTierName() + " (" + userTierType.getChatTierName() + ")");
            menuBuilder.addItem(".", addTierAct).setEnabled(!exists);
        }
        menuBuilder.addSeparator(".", "new_tier");
        final NewTierAction customTierAct = new NewTierAction(editor);
        menuBuilder.addItem(".", customTierAct);
    }

    /**
     * Append tier menu items to the given menu builder.
     *
     * @param td
     * @param tvi
     * @param tierIdx
     * @param menuBuilder
     */
    public static void appendTierMenu(TierDescription td, TierViewItem tvi, int tierIdx, MenuBuilder menuBuilder) {

    }

}
