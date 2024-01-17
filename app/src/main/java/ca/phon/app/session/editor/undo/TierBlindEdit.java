package ca.phon.app.session.editor.undo;

import ca.phon.app.session.editor.EditorEvent;
import ca.phon.app.session.editor.EditorEventManager;
import ca.phon.app.session.editor.EditorEventType;
import ca.phon.session.Session;
import ca.phon.session.TierDescription;
import ca.phon.session.TierViewItem;
import com.kitfox.svg.A;

import javax.swing.undo.CannotUndoException;
import java.util.ArrayList;
import java.util.List;

public class TierBlindEdit extends SessionUndoableEdit {

    private final String tierName;

    private final boolean blind;

    public TierBlindEdit(Session session, EditorEventManager editorEventManager, String tierName, boolean blind) {
        super(session, editorEventManager);
        this.tierName = tierName;
        this.blind = blind;
    }

    private void fireEvent() {
        // fire event
        final List<TierViewItem> view = getSession().getTierView();
        final TierViewItem tvi = view.stream().filter( (item) -> item.getTierName().equals(tierName) ).findFirst().orElse(null);
        if(tvi != null) {
            final int idx = view.indexOf(tvi);
            final EditorEventType.TierViewChangedData data = new EditorEventType.TierViewChangedData(view, view,
                    EditorEventType.TierViewChangeType.BLIND_TIER, List.of(tierName), List.of(idx));
            getEditorEventManager().queueEvent(new EditorEvent<>(EditorEventType.TierViewChanged, null, data));
        }
    }

    @Override
    public void undo() throws CannotUndoException {
        List<String> blindTiers = new ArrayList<>(getSession().getBlindTiers());
        if(blind) {
            blindTiers.remove(tierName);
        } else {
            if(!blindTiers.contains(tierName))
                blindTiers.add(tierName);
        }
        getSession().setBlindTiers(blindTiers);
        final TierDescription existingTierDesc = getSession().getTier(tierName);
        if(existingTierDesc != null)
            existingTierDesc.setBlind(!blind);

        fireEvent();
    }

    @Override
    public void doIt() {
        List<String> blindTiers = new ArrayList<>(getSession().getBlindTiers());
        if(blind) {
            if(!blindTiers.contains(tierName))
                blindTiers.add(tierName);
        } else {
            blindTiers.remove(tierName);
        }
        getSession().setBlindTiers(blindTiers);
        final TierDescription existingTierDesc = getSession().getTier(tierName);
        if(existingTierDesc != null)
            existingTierDesc.setBlind(blind);

        fireEvent();
    }

}
