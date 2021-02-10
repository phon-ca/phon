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

import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;

import javax.swing.*;

import ca.phon.app.log.LogUtil;
import ca.phon.session.*;
import ca.phon.session.Record;
import org.apache.logging.log4j.*;

import ca.phon.app.session.*;
import ca.phon.app.session.editor.*;
import ca.phon.app.session.editor.undo.*;

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
				RecordsTransferable recordsTransferable = (RecordsTransferable) clipboardContents.getTransferData(RecordsTransferable.FLAVOR);
				getEditor().getUndoSupport().beginUpdate();
				Session session = recordsTransferable.getSession();
				ratifyTiers(session);
				for(int recordIndex:recordsTransferable.getRecords()) {
					Record r = session.getRecord(recordIndex);
					Optional<Participant> existingSpeaker =
							StreamSupport.stream(getEditor().getSession().getParticipants().spliterator(), false)
									.filter( p -> {
										if(p.getId().equals(r.getSpeaker().getId())) {
											if(p.getName() == null) {
												return r.getSpeaker().getName() == null;
											} else {
												return p.getName().equals(r.getSpeaker().getName());
											}
										} else {
											return false;
										}
									} )
									.findAny();
					Participant speaker = Participant.UNKNOWN;
					if(existingSpeaker.isEmpty()) {
						speaker = SessionFactory.newFactory().cloneParticipant(r.getSpeaker());
						AddParticipantEdit addParticipantEdit = new AddParticipantEdit(getEditor(), speaker);
						getEditor().getUndoSupport().postEdit(addParticipantEdit);
					} else {
						speaker = existingSpeaker.get();
					}

					Record clonedRecord = SessionFactory.newFactory().cloneRecord(r);
					clonedRecord.setSpeaker(speaker);
					AddRecordEdit addRecordEdit = new AddRecordEdit(getEditor(), clonedRecord);
					getEditor().getUndoSupport().postEdit(addRecordEdit);
				}
				getEditor().getUndoSupport().endUpdate();
			} catch (IOException | UnsupportedFlavorException e) {
				Toolkit.getDefaultToolkit().beep();
				LogUtil.severe(e);
			}
		}
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
				AddTierEdit addTierEdit = new AddTierEdit(getEditor(), td, sessionFactory.createTierViewItem(td.getName()) );
				getEditor().getUndoSupport().postEdit(addTierEdit);
			}
		}
	}

}
