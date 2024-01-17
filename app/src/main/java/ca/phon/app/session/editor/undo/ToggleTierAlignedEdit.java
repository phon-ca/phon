package ca.phon.app.session.editor.undo;

import ca.phon.app.session.editor.EditorEvent;
import ca.phon.app.session.editor.EditorEventManager;
import ca.phon.app.session.editor.EditorEventType;
import ca.phon.session.*;
import ca.phon.session.Record;

import javax.swing.undo.CannotUndoException;
import java.util.List;

public class ToggleTierAlignedEdit extends SessionUndoableEdit {

    private final String tierName;

    private final boolean aligned;

    public ToggleTierAlignedEdit(Session session, EditorEventManager editorEventManager, String tierName, boolean aligned) {
        super(session, editorEventManager);
        this.tierName = tierName;
        this.aligned = aligned;
    }

    @Override
    public void undo() throws CannotUndoException {
        final SystemTierType systemTierType = SystemTierType.tierFromString(tierName);
        final UserTierType userTierType = UserTierType.fromPhonTierName(tierName);
        if(systemTierType == null || userTierType == null) {
            // replace tier description for tier
            final TierDescription existingTierDesc = getSession().getTier(tierName);
            if(existingTierDesc == null) return;
            if(existingTierDesc.isExcludeFromAlignment() != aligned) {
                final TierDescription newTierDesc = SessionFactory.newFactory().createTierDescription(
                        existingTierDesc.getName(), existingTierDesc.getDeclaredType(), existingTierDesc.getTierParameters(),
                        aligned, existingTierDesc.isBlind(), existingTierDesc.getSubtypeDelim(), existingTierDesc.getSubtypeExpr());

                int tierIdx = getSession().getUserTiers().indexOf(existingTierDesc);
                getSession().removeUserTier(tierIdx);
                getSession().addUserTier(tierIdx, newTierDesc);

                // update all record tiers
                for (Record r : getSession().getRecords()) {
                    Tier<?> tier = r.getTier(tierName);
                    if (tier != null) {
                        Tier<?> dupTier = SessionFactory.newFactory().createTier(newTierDesc);
                        dupTier.setText(tier.toString());
                        r.putTier(dupTier);
                    }
                }
            }
            fireToggleTierAlignedEvent();
        }
    }

    @Override
    public void doIt() {
        final SystemTierType systemTierType = SystemTierType.tierFromString(tierName);
        final UserTierType userTierType = UserTierType.fromPhonTierName(tierName);
        if(systemTierType == null || userTierType == null) {
            // replace tier description for tier
            final TierDescription existingTierDesc = getSession().getTier(tierName);
            if(existingTierDesc == null) return;
            if(existingTierDesc.isExcludeFromAlignment() == aligned) {
                final TierDescription newTierDesc = SessionFactory.newFactory().createTierDescription(
                        existingTierDesc.getName(), existingTierDesc.getDeclaredType(), existingTierDesc.getTierParameters(),
                        !aligned, existingTierDesc.isBlind(), existingTierDesc.getSubtypeDelim(), existingTierDesc.getSubtypeExpr());

                int tierIdx = getSession().getUserTiers().indexOf(existingTierDesc);
                getSession().removeUserTier(tierIdx);
                getSession().addUserTier(tierIdx, newTierDesc);

                // update all record tiers
                for (Record r : getSession().getRecords()) {
                    Tier<?> tier = r.getTier(tierName);
                    if (tier != null) {
                        Tier<?> dupTier = SessionFactory.newFactory().createTier(newTierDesc);
                        dupTier.setText(tier.toString());
                        r.putTier(dupTier);
                    }
                }
            }
            fireToggleTierAlignedEvent();
        }
    }

    // fire event
    private void fireToggleTierAlignedEvent() {
        // fire event
        final List<TierViewItem> view = getSession().getTierView();
        final TierViewItem tvi = view.stream().filter( (item) -> item.getTierName().equals(tierName) ).findFirst().orElse(null);
        if(tvi != null) {
            final int idx = view.indexOf(tvi);
            final EditorEventType.TierViewChangedData data = new EditorEventType.TierViewChangedData(view, view,
                    EditorEventType.TierViewChangeType.ALIGNED_TIER, List.of(tierName), List.of(idx));
            getEditorEventManager().queueEvent(new EditorEvent<>(EditorEventType.TierViewChanged, null, data));
        }
    }

    @Override
    public String getRedoPresentationName() {
        return "Redo toggle tier alignment";
    }

    @Override
    public String getUndoPresentationName() {
        return "Undo toggle tier alignment";
    }

    @Override
    public String toString() {
        return getUndoPresentationName();
    }

}
