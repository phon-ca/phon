package ca.phon.session;

/**
 * Location within a {@link Session}.
 * Properties include record index,  tier name, group number
 * and character location within tier.
 */
public class SessionLocation {
	
	private Session session = null;
	
	private int recordIndex = 0;
	
	private int groupIndex = 0;
	
	private int characterPositionInGroup = 0;

	public SessionLocation() {
		
	}

	public SessionLocation(Session session, int recordIndex, int groupIndex,
			int characterPositionInGroup) {
		super();
		this.session = session;
		this.recordIndex = recordIndex;
		this.groupIndex = groupIndex;
		this.characterPositionInGroup = characterPositionInGroup;
	}


	public Session getSession() {
		return session;
	}


	public void setSession(Session session) {
		this.session = session;
	}


	public int getRecordIndex() {
		return recordIndex;
	}


	public void setRecordIndex(int recordIndex) {
		this.recordIndex = recordIndex;
	}


	public int getGroupIndex() {
		return groupIndex;
	}


	public void setGroupIndex(int groupIndex) {
		this.groupIndex = groupIndex;
	}


	public int getCharacterPositionInGroup() {
		return characterPositionInGroup;
	}


	public void setCharacterPositionInGroup(int characterPositionInGroup) {
		this.characterPositionInGroup = characterPositionInGroup;
	}
	
}
