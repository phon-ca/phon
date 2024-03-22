package ca.phon.app.session.editor.view.tierManagement;

import ca.phon.app.session.editor.EditorEventManager;
import ca.phon.app.session.editor.SessionEditor;
import ca.phon.app.session.editor.actions.*;
import ca.phon.app.session.editor.undo.AddTierEdit;
import ca.phon.app.session.editor.undo.SessionEditUndoSupport;
import ca.phon.app.session.editor.undo.ToggleTierAlignedEdit;
import ca.phon.session.*;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.ui.fonts.FontPreferences;
import ca.phon.ui.menu.MenuBuilder;
import ca.phon.util.Tuple;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;

import javax.swing.*;
import java.awt.*;
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
        setupNewTierMenu(editor.getSession(), editor.getEventManager(), editor.getUndoSupport(), menuBuilder);
    }

    public static void setupNewTierMenu(Session session, EditorEventManager eventManager, SessionEditUndoSupport undoSupport, MenuBuilder menuBuilder) {
        List<UserTierType> availableUserTierTypes = new ArrayList<>(List.of(UserTierType.values()));
        for (UserTierType userTierType : availableUserTierTypes) {
            boolean exists = session.getUserTiers().get(userTierType.getPhonTierName()) != null;
            PhonUIAction<Void> addTierAct = PhonUIAction.runnable(() -> {
                TierDescription td = SessionFactory.newFactory().createTierDescription(userTierType);
                TierViewItem tvi = SessionFactory.newFactory().createTierViewItem(td.getName());
                AddTierEdit edit = new AddTierEdit(session, eventManager, td, tvi);
                editor.getUndoSupport().postEdit(edit);
            });
            addTierAct.putValue(PhonUIAction.NAME, userTierType.getPhonTierName() + " (" + userTierType.getChatTierName() + ")");
            menuBuilder.addItem(".", addTierAct).setEnabled(!exists);
        }
        menuBuilder.addSeparator(".", "new_tier");
        final NewTierAction customTierAct = new NewTierAction(session, eventManager, undoSupport);
        menuBuilder.addItem(".", customTierAct);
    }

    /**
     * Append tier menu items to the given menu builder.
     *
     * @param editor
     * @param td
     * @param tvi
     * @param menuBuilder
     */
    public static void setupTierMenu(SessionEditor editor, TierDescription td, TierViewItem tvi, MenuBuilder menuBuilder) {
        setupTierMenu(editor.getSession(), editor.getEventManager(), editor.getUndoSupport(), td, tvi, menuBuilder);
    }

    public static void setupTierMenu(Session session, EditorEventManager eventManager, SessionEditUndoSupport undoSupport, TierDescription td, TierViewItem tvi, MenuBuilder menuBuilder) {
        final SystemTierType systemTierType = SystemTierType.tierFromString(tvi.getTierName());
        final UserTierType userTierType = UserTierType.fromPhonTierName(tvi.getTierName());
        final List<TierViewItem> view = session.getTierView();
        final MoveTierAction moveUpAction = new MoveTierAction(session, eventManager, undoSupport, tvi, -1);
        final MoveTierAction moveDownAction = new MoveTierAction(session, eventManager, undoSupport, tvi, 1);
        final int tierIdx = view.indexOf(tvi);
        if(tierIdx > 0)
            menuBuilder.addItem(".", moveUpAction);
        if(tierIdx < view.size() - 1)
            menuBuilder.addItem(".", moveDownAction);
        menuBuilder.addSeparator(".", "visible_locked");
        menuBuilder.addItem(".", new ToggleTierLockAction(session, eventManager, undoSupport, tvi));
        menuBuilder.addItem(".", new ToggleTierVisibleAction(session, eventManager, undoSupport, tvi));
        menuBuilder.addSeparator(".", "edit_remove");
        menuBuilder.addItem(".", new ToggleTierBlind(session, eventManager, undoSupport, tvi.getTierName()));
        if(systemTierType == null && userTierType == null)
            menuBuilder.addItem(".", new ToggleTierAlignedAction(session, eventManager, undoSupport, tvi.getTierName()));
        menuBuilder.addItem(".", new DuplicateTierAction(session, eventManager, undoSupport, tvi.getTierName(), tierIdx + 1));
        menuBuilder.addItem(".", new EditTierAction(session, eventManager, undoSupport, tvi));
        if(td != null)
            menuBuilder.addItem(".", new RemoveTierAction(session, eventManager, undoSupport, td, tvi));
    }

    /**
     * Append tier menus for existing session tiers
     *
     * @param editor
     * @param menuBuilder
     */
    public static void appendExistingTiersMenu(SessionEditor editor, MenuBuilder menuBuilder) {
        appendExistingTiersMenu(editor.getSession(), editor.getEventManager(), editor.getUndoSupport(), menuBuilder);
    }

    public static void appendExistingTiersMenu(Session session, EditorEventManager eventManager, SessionEditUndoSupport undoSupport, MenuBuilder menuBuilder) {
        final List<TierViewItem> view = session.getTierView();
        for(int i = 0; i < view.size(); i++) {
            final TierViewItem tvi = view.get(i);
            final JMenu tierMenu = menuBuilder.addMenu(".", tvi.getTierName());
            TierDescription tierDesc = null;
            for(TierDescription td:session.getUserTiers()) {
                if(td.getName().equals(tvi.getTierName())) {
                    tierDesc = td;
                    break;
                }
            }
            setupTierMenu(session, eventManager, undoSupport, tierDesc, tvi, new MenuBuilder(tierMenu));
        }
    }

    /**
     * Setup tier font menu
     *
     * @param tvi
     * @param builder
     */
    public static void setupFontMenu(TierViewItem tvi, MenuBuilder builder) {
//        final ImageIcon icon =
//                IconManager.getInstance().getIcon("apps/preferences-desktop-font", IconSize.SMALL);
//        final ImageIcon reloadIcon =
//                IconManager.getInstance().getIcon("actions/reload", IconSize.SMALL);
//        final ImageIcon addIcon =
//                IconManager.getInstance().getIcon("actions/list-add", IconSize.SMALL);
//        final ImageIcon subIcon =
//                IconManager.getInstance().getIcon("actions/list-remove", IconSize.SMALL);
//        final ImageIcon boldIcon =
//                IconManager.getInstance().getIcon("actions/format-text-bold", IconSize.SMALL);
//        final ImageIcon italicIcon =
//                IconManager.getInstance().getIcon("actions/format-text-italic", IconSize.SMALL);
//
//        Font currentFont = ("default".equals(tvi.getTierFont()) ? FontPreferences.getTierFont() : Font.decode(tvi.getTierFont()));
//
//        final PhonUIAction<Tuple<TierViewItem, Integer>> toggleBoldAct =
//                PhonUIAction.eventConsumer(this::onToggleStyle, new Tuple<>(tvi, Font.BOLD));
//        toggleBoldAct.putValue(PhonUIAction.NAME, "Bold");
//        toggleBoldAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Toggle bold modifier");
//        toggleBoldAct.putValue(PhonUIAction.SELECTED_KEY, currentFont.isBold());
//        toggleBoldAct.putValue(PhonUIAction.SMALL_ICON, boldIcon);
//        builder.addItem(".", new JCheckBoxMenuItem(toggleBoldAct));
//
//        final PhonUIAction<Tuple<TierViewItem, Integer>> toggleItalicAct =
//                PhonUIAction.eventConsumer(this::onToggleStyle, new Tuple<>(tvi, Font.ITALIC));
//        toggleItalicAct.putValue(PhonUIAction.NAME, "Italic");
//        toggleItalicAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Toggle italic modifier");
//        toggleItalicAct.putValue(PhonUIAction.SELECTED_KEY, currentFont.isItalic());
//        toggleItalicAct.putValue(PhonUIAction.SMALL_ICON, italicIcon);
//        builder.addItem(".", new JCheckBoxMenuItem(toggleItalicAct));
//
//        final PhonUIAction<TierViewItem> onIncreaseFontSize = PhonUIAction.eventConsumer(this::onIncreaseFontSize, tvi);
//        onIncreaseFontSize.putValue(PhonUIAction.NAME, "Increase size");
//        onIncreaseFontSize.putValue(PhonUIAction.SHORT_DESCRIPTION, "Increase point size by 2");
//        onIncreaseFontSize.putValue(PhonUIAction.SMALL_ICON, addIcon);
//        builder.addItem(".", onIncreaseFontSize);
//
//        final PhonUIAction<TierViewItem> onDecreaseFontSize = PhonUIAction.eventConsumer(this::onDecreaseFontSize, tvi);
//        onDecreaseFontSize.putValue(PhonUIAction.NAME, "Decrease size");
//        onDecreaseFontSize.putValue(PhonUIAction.SHORT_DESCRIPTION, "Decrease point size by 2");
//        onDecreaseFontSize.putValue(PhonUIAction.SMALL_ICON, subIcon);
//        builder.addItem(".", onDecreaseFontSize);
//
//        builder.addSeparator(".", "suggested-fonts");
//
//        JMenuItem headerItem = new JMenuItem("-- Suggested Fonts --");
//        headerItem.setEnabled(false);
//        builder.addItem(".", headerItem);
//
//        for(int i = 0; i < FontPreferences.SUGGESTED_IPA_FONT_NAMES.length; i++) {
//            String suggestedFont = FontPreferences.SUGGESTED_IPA_FONT_NAMES[i];
//            String fontString = String.format("%s-PLAIN-12", suggestedFont);
//            // font not found
//            if(Font.decode(fontString).getFamily().equals("Dialog")) continue;
//
//            final PhonUIAction<Tuple<TierViewItem, Integer>> selectSuggestedFont =
//                    PhonUIAction.eventConsumer(this::onSelectSuggestedFont, new Tuple<>(tvi, i));
//            selectSuggestedFont.putValue(PhonUIAction.NAME, suggestedFont);
//            selectSuggestedFont.putValue(PhonUIAction.SHORT_DESCRIPTION, "Use font: " + suggestedFont);
//            builder.addItem(".", selectSuggestedFont);
//        }
//
//        builder.addSeparator(".", "font-dialog");
//        final PhonUIAction<TierViewItem> defaultAct = PhonUIAction.eventConsumer(this::onSelectFont, tvi);
//        defaultAct.putValue(PhonUIAction.NAME, "Select font....");
//        defaultAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Select font using font selection dialog");
//        defaultAct.putValue(PhonUIAction.LARGE_ICON_KEY, icon);
//        builder.addItem(".", defaultAct);
    }

}
