/*
 * Copyright (C) 2005-2020 Gregory Hedlund & Yvan Rose
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
package ca.phon.app.session.editor.actions;

import ca.phon.app.log.LogUtil;
import ca.phon.app.session.RecordsTransferable;
import ca.phon.app.session.editor.SessionEditor;
import ca.phon.app.session.editor.undo.*;
import ca.phon.session.Record;
import ca.phon.session.*;
import org.apache.logging.log4j.LogManager;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.List;
import java.util.*;
import java.util.stream.StreamSupport;

/**
 * Paste record from system clipboard.
 */
public class PasteRecordAction extends SessionEditorAction {
	
	private final static org.apache.logging.log4j.Logger LOGGER = LogManager.getLogger(PasteRecordAction.class.getName());

	private static final long serialVersionUID = 4581031841588720169L;

	private final static String CMD_NAME = "Paste record";
	
	private final static String SHORT_DESC = "Paste record from clipboard after current record";
	
	private final static String ICON = "";
	
	private final static KeyStroke KS = 
			KeyStroke.getKeyStroke(KeyEvent.VK_V,
					Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx() | KeyEvent.SHIFT_MASK);
	
	public PasteRecordAction(SessionEditor editor) {
		super(editor);
		
		putValue(NAME, CMD_NAME);
		putValue(SHORT_DESCRIPTION, SHORT_DESC);
		putValue(ACCELERATOR_KEY, KS);
	}

	@Override
	public void hookableActionPerformed(ActionEvent e1) {
		Transferable clipboardContents =
				Toolkit.getDefaultToolkit().getSystemClipboard().getContents(this);
		if(clipboardContents.isDataFlavorSupported(RecordsTransferable.FLAVOR)) {
			try {
				getEditor().getUndoSupport().beginUpdate();

				RecordsTransferable recordsTransferable = (RecordsTransferable) clipboardContents.getTransferData(RecordsTransferable.FLAVOR);
				Session session = recordsTransferable.getSession();

				List<String> idList = new ArrayList<>();
				for(Participant p:session.getParticipants()) {
					Optional<Participant> existingParticipant =
							StreamSupport.stream(getEditor().getSession().getParticipants().spliterator(), false)
								.filter( pt -> p.toString().equals(pt.toString()) )
								.findAny();
					if(existingParticipant.isEmpty()) {
						Participant clonedParticipant = SessionFactory.newFactory().cloneParticipant(p);
						if (idList.contains(clonedParticipant.getId()))
							clonedParticipant.setId(getRoleId(clonedParticipant));
						idList.add(clonedParticipant.getId());
						AddParticipantEdit addParticipantEdit = new AddParticipantEdit(getEditor(), clonedParticipant);
						getEditor().getUndoSupport().postEdit(addParticipantEdit);
					} else {
						idList.add(existingParticipant.get().getId());
					}
				}

				ratifyTiers(session);

				boolean fireEvent = true;
				for(Record r:recordsTransferable.getRecords()) {
					Optional<Participant> existingSpeaker =
							StreamSupport.stream(getEditor().getSession().getParticipants().spliterator(), false)
									.filter( p -> p.toString().equals(r.getSpeaker().toString()) )
									.findAny();
					Participant speaker = Participant.UNKNOWN;
					if(r.getSpeaker() != Participant.UNKNOWN && existingSpeaker.isPresent()) {
						speaker = existingSpeaker.get();
					}

					Record clonedRecord = SessionFactory.newFactory().cloneRecord(r);
					clonedRecord.setSpeaker(speaker);

					AddRecordEdit addRecordEdit = new AddRecordEdit(getEditor(), clonedRecord);
					addRecordEdit.setFireEvent(fireEvent);
					if(fireEvent) {
						fireEvent = false;
					}
					getEditor().getUndoSupport().postEdit(addRecordEdit);
				}

				getEditor().getUndoSupport().endUpdate();
			} catch (IOException | UnsupportedFlavorException e) {
				Toolkit.getDefaultToolkit().beep();
				LogUtil.severe(e);
			}
		}
	}

	private String getRoleId(Participant speaker) {
		final ParticipantRole role = speaker.getRole();
		String id = role.getId();

		int idx = 0;
		for(Participant otherP:getEditor().getSession().getParticipants()) {
			if(otherP.getId().equals(id)) {
				id = role.getId().substring(0, 2) + (++idx);
			}
		}
		return id;
	}

	private void ratifyTiers(Session copySession) {
		Session mySession = getEditor().getSession();
		TierDescriptions currentTiers = mySession.getUserTiers();
		TierDescriptions copyTiers = copySession.getUserTiers();

		SessionFactory sessionFactory = SessionFactory.newFactory();
		List<TierDescription> toAdd = new ArrayList<>();
		for(TierDescription td:copyTiers) {
			Optional<TierDescription> existingTier =
					StreamSupport.stream(currentTiers.spliterator(), false)
							.filter( desc -> td.getName().equals(desc.getName()) && td.isGrouped() == desc.isGrouped() )
							.findAny();
			if(!existingTier.isPresent()) {
				toAdd.add(td);
			}
		}

		if(toAdd.size() > 0) {
			for (TierDescription td : toAdd) {
				TierViewItem tvi = sessionFactory.createTierViewItem(td.getName(), td.isGrouped(), true);
				AddTierEdit addTierEdit = new AddTierEdit(getEditor(), td, sessionFactory.createTierViewItem(td.getName()) );
				getEditor().getUndoSupport().postEdit(addTierEdit);
			}
		}
	}

}
