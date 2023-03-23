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

import ca.phon.app.session.editor.*;
import ca.phon.session.Record;
import ca.phon.session.*;
import ca.phon.session.position.SegmentCalculator;
import ca.phon.util.icons.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Action for playing current segment in media player.
 */
public class PlaySegmentAction extends SessionEditorAction {
	
	public enum SegmentType {
		CURRENT_RECORD,
		SPEAKER_TURN,
		CONVERSATION_PERIOD,
		CUSTOM
	};
	
	private SegmentType segmentType = SegmentType.CUSTOM;
	
	private long startTime = -1L;
	
	private long endTime = -1L;

	public PlaySegmentAction(SessionEditor editor) {
		this(editor, SegmentType.CURRENT_RECORD);
		
		putValue(Action.NAME, "Play current record segment");
		putValue(Action.SHORT_DESCRIPTION, "Play segment for current record");
		putValue(Action.SMALL_ICON, IconManager.getInstance().getIcon("actions/media-playback-start", IconSize.SMALL));
		putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_R, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
	}
	
	public PlaySegmentAction(SessionEditor editor, SegmentType segmentType) {
		super(editor);
		
		this.segmentType = segmentType;
		putValue(Action.NAME, "Play segment");
		putValue(Action.SHORT_DESCRIPTION, "Play segment for current record");
		putValue(Action.SMALL_ICON, IconManager.getInstance().getIcon("actions/media-playback-start", IconSize.SMALL));
	}
	
	/**
	 * 
	 * @param editor
	 * @param startTime in s
	 * @param endTime in s
	 */
	public PlaySegmentAction(SessionEditor editor, float startTime, float endTime) {
		this(editor, Float.valueOf(startTime * 1000.0f).longValue(), Float.valueOf(endTime * 1000.0f).longValue());
	}
	
	/**
	 * 
	 * @param editor
	 * @param startTime in ms
	 * @param endTime in ms
	 */
	public PlaySegmentAction(SessionEditor editor, long startTime, long endTime) {
		super(editor);
		
		this.segmentType = SegmentType.CUSTOM;
		this.startTime = startTime;
		this.endTime = endTime;
	}

	private MediaSegment getMediaSegment(ActionEvent ae) {
		if(segmentType == SegmentType.CURRENT_RECORD) {
			Record r = getEditor().currentRecord();
			return (r != null ? r.getMediaSegment() : null);
		} else if(segmentType == SegmentType.SPEAKER_TURN) {
			return SegmentCalculator.contiguousSegment(getEditor().getSession(), getEditor().getCurrentRecordIndex());
		} else if(segmentType == SegmentType.CONVERSATION_PERIOD) {
			return SegmentCalculator.conversationPeriod(getEditor().getSession(), getEditor().getCurrentRecordIndex());
		} else if(segmentType == SegmentType.CUSTOM) {
			if(startTime < 0 || endTime < 0 || endTime - startTime <= 0) {
				CustomSegmentDialog dlg = new CustomSegmentDialog(getEditor());
				dlg.pack();
				
				dlg.setLocationRelativeTo(getEditor());
				
				dlg.setModal(true);
				dlg.setVisible(true);

				return dlg.getSegment();
			} else {
				MediaSegment retVal = SessionFactory.newFactory().createMediaSegment();
				retVal.setStartValue(startTime);
				retVal.setEndValue(endTime);
				
				return retVal;
			}
		} else {
			return null;
		}
	}
	
	@Override
	public void hookableActionPerformed(ActionEvent ae) {
		MediaSegment mediaSegment = getMediaSegment(ae);
		if(mediaSegment != null) {
			SessionMediaModel mediaModel = getEditor().getMediaModel();
			mediaModel.getSegmentPlayback().playSegment(mediaSegment);
		}
	}

}
