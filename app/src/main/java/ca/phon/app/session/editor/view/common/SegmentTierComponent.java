/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2008 The Phon Project, Memorial University <http://phon.ling.mun.ca>
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package ca.phon.app.session.editor.view.common;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.JComponent;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import ca.phon.formatter.Formatter;
import ca.phon.formatter.FormatterFactory;
import ca.phon.session.MediaSegment;
import ca.phon.session.Tier;

/**
 * Editor for media segments.
 */
public class SegmentTierComponent extends SegmentField implements TierEditor {
	
	private static final long serialVersionUID = 5303962410367183323L;

	private static final Logger LOGGER = Logger
			.getLogger(SegmentTierComponent.class.getName());
	
	private final static String DEFAULT_SEGMENT_TEXT = "000:00.000-000:00.000";

	private Tier<MediaSegment> segmentTier;
	
	private int groupIndex = 0;

	private volatile boolean dontUpdate = false;

	public SegmentTierComponent(Tier<MediaSegment> tier, int groupIndex) {
		super();
		
		this.segmentTier = tier;
		this.groupIndex = groupIndex;
		
		super.setBorder(new GroupFieldBorder());
		
		updateText();
		getDocument().addDocumentListener(new SegmentDocumentListener());
	}


	public void updateText() {
		final MediaSegment segment = segmentTier.getGroup(groupIndex);
		final Formatter<MediaSegment> segmentFormatter = FormatterFactory.createFormatter(MediaSegment.class);
		
		String tierTxt = 
				(segmentFormatter != null ? segmentFormatter.format(segment) : DEFAULT_SEGMENT_TEXT);
		dontUpdate = true;
		int caretLocation = -1;
		if(super.hasFocus()) {
			caretLocation = super.getCaretPosition();
		}
//		setFormatter(formatter);
		setText(tierTxt);
		if(super.hasFocus() && caretLocation >= 0) {
			super.setCaretPosition(caretLocation);
		}
		dontUpdate = false;
	}

//	public void saveData() {
//		if(dontUpdate) return;
//
//		IUtterance utt = model.getRecord();
//		try {
//			String oldVal = utt.getTierString(SystemTierType.Segment.getTierName());
//			utt.setTierString(SystemTierType.Segment.getTierName(), getText());
//			String newVal = utt.getTierString(SystemTierType.Segment.getTierName());
//
//			model.fireRecordEditorEvent(TIER_CHANGE_EVT, this,
//					SystemTierType.Segment.getTierName());
//
//			TierDataEdit edit =
//					new TierDataEdit(utt, SystemTierType.Segment.getTierName(), oldVal, newVal) {
//
//				@Override
//				public void redo() throws CannotRedoException {
//					super.redo();
//					updateText();
//				}
//
//				@Override
//				public void undo() throws CannotUndoException {
//					super.undo();
//					updateText();
//				}
//
//
//
//			};
//			model.getUndoSupport().postEdit(edit);
//		} catch (ParserException ex) {
//			PhonLogger.severe(ex.toString());
//			ex.printStackTrace();
//		}
//	}
	
	private class SegmentDocumentListener implements DocumentListener {

		@Override
		public void insertUpdate(DocumentEvent de) {
//			saveData();
		}

		@Override
		public void removeUpdate(DocumentEvent de) {
		}

		@Override
		public void changedUpdate(DocumentEvent de) {
		}

	}

	@Override
	public JComponent getEditorComponent() {
		return this;
	}

	@Override
	public void addTierEditorListener(TierEditorListener listener) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void removeTierEditorListener(TierEditorListener listener) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public List<TierEditorListener> getTierEditorListeners() {
		// TODO Auto-generated method stub
		return null;
	}

}
