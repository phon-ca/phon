package ca.phon.app.session.editor;

import ca.phon.util.Range;

/**
 * Selection information used for {@link EditorSelectionModel}
 */
public class SessionEditorSelection {
	
	private final int recordIndex;
	
	private final String tierName;
	
	private final int groupIndex;
	
	private final Range groupRange;

	public SessionEditorSelection(int recordIndex, String tierName,
			int groupIndex, Range groupRange) {
		super();
		this.recordIndex = recordIndex;
		this.tierName = tierName;
		this.groupIndex = groupIndex;
		this.groupRange = groupRange;
	}

	public int getRecordIndex() {
		return recordIndex;
	}

	public String getTierName() {
		return tierName;
	}

	public int getGroupIndex() {
		return groupIndex;
	}

	public Range getGroupRange() {
		return groupRange;
	}
	
}