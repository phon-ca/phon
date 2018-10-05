/*
 * Copyright (C) 2012-2018 Gregory Hedlund & Yvan Rose
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at

 *    http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ca.phon.app.session.editor.undo;

import java.util.HashMap;
import java.util.Map;

import javax.swing.undo.CannotUndoException;

import ca.phon.app.session.editor.EditorEventType;
import ca.phon.app.session.editor.SessionEditor;
import ca.phon.ipa.IPATranscript;
import ca.phon.ipa.alignment.PhoneMap;
import ca.phon.orthography.Orthography;
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
			
			final Tier<?> tier = record.getTier(key);
			if(tier != null) {
				while(tier.numberOfGroups() < groupIndex) tier.addGroup();
			}
			
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
		oldGroupData.put(SystemTierType.Orthography.getName(), 
				(groupIndex < record.getOrthography().numberOfGroups() ? record.getOrthography().getGroup(groupIndex) : new Orthography()));
		oldGroupData.put(SystemTierType.IPATarget.getName(), 
				(groupIndex < record.getIPATarget().numberOfGroups() ? record.getIPATarget().getGroup(groupIndex) : new IPATranscript()));
		oldGroupData.put(SystemTierType.IPAActual.getName(), 
				(groupIndex < record.getIPAActual().numberOfGroups() ? record.getIPAActual().getGroup(groupIndex) : new IPATranscript()));
		oldGroupData.put(SystemTierType.SyllableAlignment.getName(), 
				(groupIndex < record.getPhoneAlignment().numberOfGroups() ? record.getPhoneAlignment().getGroup(groupIndex) : new PhoneMap()));
		
		for(String tierName:record.getExtraTierNames()) {
			final Tier<String> extraTier = record.getTier(tierName, String.class);
			if(extraTier.isGrouped())
				oldGroupData.put(tierName, 
						(groupIndex < extraTier.numberOfGroups() ? extraTier.getGroup(groupIndex) : new String()));
		}
		
		record.removeGroup(groupIndex);	
		
		queueEvent(EditorEventType.GROUP_LIST_CHANGE_EVT, getSource(), null);
	}

}
