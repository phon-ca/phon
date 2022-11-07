package ca.phon.app.session.editor.undo;

import ca.phon.app.session.editor.*;
import ca.phon.session.*;
import ca.phon.session.Record;

public class ConvertFlatTierEdit extends SessionEditorUndoableEdit {

	private final String tierName;

	/**
	 * Constructor
	 *
	 * @param editor
	 */
	public ConvertFlatTierEdit(SessionEditor editor, String tierName) {
		super(editor);

		this.tierName = tierName;
	}

	@Override
	public String getPresentationName() {
		return "convert flat tier";
	}

	@Override
	public void doIt() {
		final Session session = getEditor().getSession();
		TierDescription tierDescription = null;
		int tierIdx = -1;
		for(int i = 0; i < session.getUserTierCount(); i++) {
			final TierDescription td = session.getUserTier(i);
			if(td.getName().equals(tierName)) {
				tierDescription = td;
				tierIdx = i;
				break;
			}
		}
		if(tierDescription == null) throw new IllegalArgumentException(tierName);

		final SessionFactory factory = SessionFactory.newFactory();
		final TierDescription newTierDescription = factory.createTierDescription(tierName, true, TierString.class);
		session.removeUserTier(tierIdx);
		session.addUserTier(tierIdx, newTierDescription);

		for(Record record:session.getRecords()) {
			final Tier<TierString> tier = record.getTier(tierName, TierString.class);
			final Tier<TierString> newTier = factory.createTier(tierName, TierString.class, true);
			newTier.setGroup(0, tier.getGroup(0));
			for(int i = 1; i < record.numberOfGroups(); i++) {
				newTier.setGroup(i, new TierString());
			}
			record.removeTier(tierName);
			record.putTier(newTier);
		}

		final EditorEvent<EditorEventType.TierViewChangedData> tierViewChangedEvt = new EditorEvent<>(EditorEventType.TierViewChanged, getSource(),
				new EditorEventType.TierViewChangedData(session.getTierView(), session.getTierView()));
		getEditor().getEventManager().queueEvent(tierViewChangedEvt);
	}

	@Override
	public void undo() {
		final Session session = getEditor().getSession();
		TierDescription tierDescription = null;
		int tierIdx = -1;
		for(int i = 0; i < session.getUserTierCount(); i++) {
			final TierDescription td = session.getUserTier(i);
			if(td.getName().equals(tierName)) {
				tierDescription = td;
				tierIdx = i;
				break;
			}
		}
		if(tierDescription == null) throw new IllegalArgumentException(tierName);

		final SessionFactory factory = SessionFactory.newFactory();
		final TierDescription newTierDescription = factory.createTierDescription(tierName, false, TierString.class);
		session.removeUserTier(tierIdx);
		session.addUserTier(tierIdx, newTierDescription);

		for(Record record:session.getRecords()) {
			final Tier<TierString> tier = record.getTier(tierName, TierString.class);
			final Tier<TierString> newTier = factory.createTier(tierName, TierString.class, false);
			newTier.setGroup(0, tier.getGroup(0));
			record.removeTier(tierName);
			record.putTier(newTier);
		}

		final EditorEvent<EditorEventType.TierViewChangedData> tierViewChangedEvt = new EditorEvent<>(EditorEventType.TierViewChanged, getSource(),
				new EditorEventType.TierViewChangedData(session.getTierView(), session.getTierView()));
		getEditor().getEventManager().queueEvent(tierViewChangedEvt);
	}

}
