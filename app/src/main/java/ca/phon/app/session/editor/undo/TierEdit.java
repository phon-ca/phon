package ca.phon.app.session.editor.undo;

import ca.phon.app.session.editor.EditorEventType;
import ca.phon.app.session.editor.SessionEditor;
import ca.phon.session.Tier;

/**
 * A change to the value of a group in a tier.
 * 
 */
public class TierEdit<T> extends SessionEditorUndoableEdit {
	
	private static final long serialVersionUID = -3236844601334798650L;

	/**
	 * tier
	 */
	private final Tier<T> tier;
	
	/**
	 * Group
	 */
	private final int groupIndex;
	
	/**
	 * Old value
	 */
	private T oldValue;
	
	/**
	 * New value
	 */
	private final T newValue;
	
	/**
	 * Constructor 
	 * 
	 * @param group
	 * @param tierName
	 * @param oldValue
	 * @param newValue
	 */
	public TierEdit(SessionEditor editor, Tier<T> tier, int groupIndex, T newValue) {
		super(editor);
		this.tier = tier;
		this.groupIndex = groupIndex;
		this.newValue = newValue;
	}

	public T getOldValue() {
		return oldValue;
	}

	public void setOldValue(T oldValue) {
		this.oldValue = oldValue;
	}

	public Tier<T> getTier() {
		return tier;
	}

	public int getGroupIndex() {
		return groupIndex;
	}
	
	public T getNewValue() {
		return newValue;
	}

	@Override
	public void undo() {
		tier.setGroup(groupIndex, getOldValue());
		
		if(getEditor() != null)
			queueEvent(EditorEventType.TIER_CHANGE_EVT, getEditor().getUndoSupport(), tier.getName());
	}
	
	@Override
	public void doIt() {
		setOldValue(tier.getGroup(groupIndex));
		tier.setGroup(groupIndex, newValue);
		
		if(getEditor() != null)
			queueEvent(EditorEventType.TIER_CHANGE_EVT, getSource(), tier.getName());
	}

}
