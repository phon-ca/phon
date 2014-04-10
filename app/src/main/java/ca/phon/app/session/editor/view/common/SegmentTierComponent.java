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
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JComponent;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import ca.phon.formatter.Formatter;
import ca.phon.formatter.FormatterFactory;
import ca.phon.session.MediaSegment;
import ca.phon.session.SessionFactory;
import ca.phon.session.Tier;
import ca.phon.session.TierListener;

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

	public SegmentTierComponent(Tier<MediaSegment> tier, int groupIndex) {
		super();
		
		this.segmentTier = tier;
		segmentTier.addTierListener(tierListener);
		this.groupIndex = groupIndex;
		
		super.setBorder(new GroupFieldBorder());
		
		updateText();
		getDocument().addDocumentListener(docListener);
	}

	/**
	 * Validate text contents
	 * 
	 * @return <code>true</code> if the contents of the field
	 *  are valid, <code>false</code> otherwise.
	 */
	private final AtomicReference<MediaSegment> validatedObjRef = new AtomicReference<MediaSegment>();
	protected boolean validateText() {
		boolean retVal = true;

		final String text = getText();
		
		// look for a formatter
		final Formatter<MediaSegment> formatter = FormatterFactory.createFormatter(MediaSegment.class);

		try {
			final MediaSegment validatedObj = formatter.parse(text);
			if(validatedObj.getEndValue() >= validatedObj.getStartValue()) {
				setValidatedObject(validatedObj);
			} else {
				retVal = false;
			}
		} catch (ParseException e) {
			retVal = false;
		}
		
		return retVal;
	}
	
	public void validateAndUpdate() {
		if(validateText())
			updateTier();
	}
	
	protected MediaSegment getValidatedObject() {
		return this.validatedObjRef.get();
	}
	
	protected void setValidatedObject(MediaSegment object) {
		this.validatedObjRef.getAndSet(object);
	}

	public void updateText() {
		final MediaSegment segment = getGroupValue();
		final Formatter<MediaSegment> segmentFormatter = FormatterFactory.createFormatter(MediaSegment.class);
		
		String tierTxt = 
				(segmentFormatter != null ? segmentFormatter.format(segment) : DEFAULT_SEGMENT_TEXT);
//		int caretLocation = -1;
//		if(super.hasFocus()) {
//			caretLocation = super.getCaretPosition();
//		}
//		setFormatter(formatter);
		setText(tierTxt);
//		if(super.hasFocus() && caretLocation >= 0) {
//			super.setCaretPosition(caretLocation);
//		}
	}
	
	private void updateTier() {
		final MediaSegment oldVal = getGroupValue();
		final MediaSegment newVal = getValidatedObject();
		if(newVal != null) {
			for(TierEditorListener listener:listeners) {
				listener.tierValueChanged(segmentTier, groupIndex, newVal, oldVal);
			}
		}
	}

	private MediaSegment getGroupValue() {
		MediaSegment retVal = null;
		
		if(groupIndex < segmentTier.numberOfGroups()) {
			retVal = segmentTier.getGroup(groupIndex);
		} else {
			final SessionFactory factory = SessionFactory.newFactory();
			retVal = factory.createMediaSegment();
		}
		
		return retVal;
	}
	
	private final DocumentListener docListener = new DocumentListener() {

		@Override
		public void insertUpdate(DocumentEvent de) {
			if(hasFocus() && validateText())
				updateTier();
		}

		@Override
		public void removeUpdate(DocumentEvent de) {
		}

		@Override
		public void changedUpdate(DocumentEvent de) {
		}

	};

	private final TierListener<MediaSegment> tierListener = new TierListener<MediaSegment>() {
		
		@Override
		public void groupsCleared(Tier<MediaSegment> tier) {
		}
		
		@Override
		public void groupRemoved(Tier<MediaSegment> tier, int index,
				MediaSegment value) {
		}
		
		@Override
		public void groupChanged(Tier<MediaSegment> tier, int index,
				MediaSegment oldValue, MediaSegment value) {
			if(!hasFocus() && index == groupIndex) {
				updateText();
			}
		}
		
		@Override
		public void groupAdded(Tier<MediaSegment> tier, int index,
				MediaSegment value) {
		}
	};
	
	@Override
	public JComponent getEditorComponent() {
		return this;
	}

	private final List<TierEditorListener> listeners = 
			Collections.synchronizedList(new ArrayList<TierEditorListener>());
	
	@Override
	public void addTierEditorListener(TierEditorListener listener) {
		if(!listeners.contains(listener))
			listeners.add(listener);
	}

	@Override
	public void removeTierEditorListener(TierEditorListener listener) {
		listeners.remove(listener);
	}

	@Override
	public List<TierEditorListener> getTierEditorListeners() {
		return listeners;
	}

}
