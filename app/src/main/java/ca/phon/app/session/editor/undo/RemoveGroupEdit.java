package ca.phon.app.session.editor.undo;

import java.util.HashMap;
import java.util.Map;

import javax.swing.undo.CannotUndoException;

import ca.phon.app.session.editor.EditorEvent;
import ca.phon.app.session.editor.EditorEventType;
import ca.phon.app.session.editor.SessionEditor;
import ca.phon.ipa.IPATranscript;
import ca.phon.ipa.alignment.PhoneMap;
import ca.phon.orthography.Orthography;
import ca.phon.session.Group;
import ca.phon.session.Record;
import ca.phon.session.SystemTierType;
import ca.phon.session.Tier;

public class RemoveGroupEdit extends SessionEditorUndoableEdit {

	private static final long serialVersionUID = 546623740766808063L;

	/**
	 * tier
	 */
	private final Record record;
	
	/** 
	 * group index
	 */
	private final int groupIndex;

	private final Map<String, Object> oldGroupData = new HashMap<String, Object>();
	
	public RemoveGroupEdit(SessionEditor editor, Record record, int groupIndex) {
		super(editor);
		this.record = record;
		this.groupIndex = groupIndex;
	}
	
	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		
		for(String key:oldGroupData.keySet()) {
			if(SystemTierType.tierFromString(key) == SystemTierType.Orthography)
				record.getOrthography().addGroup(groupIndex, (Orthography)oldGroupData.get(key));
			else if(SystemTierType.tierFromString(key) == SystemTierType.IPATarget)
				record.getIPATarget().addGroup(groupIndex, (IPATranscript)oldGroupData.get(key));
			else if(SystemTierType.tierFromString(key) == SystemTierType.IPAActual) 
				record.getIPAActual().addGroup(groupIndex, (IPATranscript)oldGroupData.get(key));
			else if(SystemTierType.tierFromString(key) == SystemTierType.SyllableAlignment)
				record.getPhoneAlignment().addGroup(groupIndex, (PhoneMap)oldGroupData.get(key));
			else {
				record.getTier(key, String.class).addGroup(groupIndex, (String)oldGroupData.get(key));
			}
		}
		
		queueEvent(EditorEventType.GROUP_LIST_CHANGE_EVT, getSource(), null);
	}

	@Override
	public void doIt() {
		oldGroupData.clear();
		oldGroupData.put(SystemTierType.Orthography.getName(), record.getOrthography().getGroup(groupIndex));
		oldGroupData.put(SystemTierType.IPATarget.getName(), record.getIPATarget().getGroup(groupIndex));
		oldGroupData.put(SystemTierType.IPAActual.getName(), record.getIPAActual().getGroup(groupIndex));
		oldGroupData.put(SystemTierType.SyllableAlignment.getName(), record.getPhoneAlignment().getGroup(groupIndex));
		
		for(String tierName:record.getExtraTierNames()) {
			final Tier<String> extraTier = record.getTier(tierName, String.class);
			if(extraTier.isGrouped())
				oldGroupData.put(tierName, extraTier.getGroup(groupIndex));
		}
		
		record.removeGroup(groupIndex);	
		
		queueEvent(EditorEventType.GROUP_LIST_CHANGE_EVT, getSource(), null);
	}

}
