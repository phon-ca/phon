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

package ca.phon.app.session.editor.view.common;

import ca.phon.app.session.editor.SessionEditor;
import ca.phon.formatter.Formatter;
import ca.phon.formatter.*;
import ca.phon.session.Record;
import ca.phon.session.*;
import ca.phon.session.check.SegmentOverlapCheck;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.util.PrefHelper;
import org.apache.logging.log4j.LogManager;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.lang.ref.WeakReference;
import java.text.ParseException;
import java.util.List;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Editor for media segments.
 */
public class SegmentTierComponent extends JComponent implements TierEditor {

	private static final long serialVersionUID = 5303962410367183323L;

	private static final org.apache.logging.log4j.Logger LOGGER = LogManager.getLogger(SegmentTierComponent.class.getName());

	private final static String DEFAULT_SEGMENT_TEXT = "000:00.000-000:00.000";

	private final WeakReference<SessionEditor> editorRef;

	private Tier<MediaSegment> segmentTier;

	private int groupIndex = 0;

	private final SegmentField segmentField;

	public SegmentTierComponent(SessionEditor editor, Tier<MediaSegment> tier, int groupIndex) {
		super();
		setOpaque(false);
		setFocusable(false);

		this.editorRef = new WeakReference<SessionEditor>(editor);
		
		this.segmentTier = tier;
		segmentTier.addTierListener(tierListener);
		this.groupIndex = groupIndex;

		segmentField = new SegmentField() {

			public void validateText() {
				super.validateText();
				SegmentTierComponent.this.validateText();
			}

		};

		updateText();
		//validateText();
		segmentField.getDocument().addDocumentListener(docListener);

		// validate text when 'enter' is pressed
		final ActionMap actionMap = segmentField.getActionMap();
		final InputMap inputMap = segmentField.getInputMap(JComponent.WHEN_FOCUSED);

		final KeyStroke validateKs = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
		final String validateId = "validate";
		final PhonUIAction<Void> validateAct = PhonUIAction.runnable(this::onEnter);
		actionMap.put(validateId, validateAct);
		inputMap.put(validateKs, validateId);

		segmentField.setActionMap(actionMap);
		segmentField.setInputMap(JComponent.WHEN_FOCUSED, inputMap);

		segmentField.addFocusListener(focusListener);

		setLayout(new FlowLayout(FlowLayout.LEADING, 0, 0));
		add(segmentField);
	}

	public GroupFieldBorder getGroupFieldBorder() {
		return (GroupFieldBorder)segmentField.getBorder();
	}

	@Override
	public void setFont(Font font) {
		super.setFont(font);
		segmentField.setFont(font);
	}

	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		segmentField.setEnabled(enabled);
	}

	private SessionEditor getEditor() {
		return editorRef.get();
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

		final String text = segmentField.getText();

		// look for a formatter
		final Formatter<MediaSegment> formatter = FormatterFactory.createFormatter(MediaSegment.class);

		try {
			final MediaSegment validatedObj = formatter.parse(text);
			if(validatedObj.getEndValue() >= validatedObj.getStartValue()) {
				setValidatedObject(validatedObj);
			} else {
				retVal = false;
			}
			getGroupFieldBorder().setShowWarningIcon(false);
			segmentField.setToolTipText(null);
		} catch (ParseException e) {
			getGroupFieldBorder().setShowWarningIcon(true);
			segmentField.setToolTipText(e.getLocalizedMessage());
			retVal = false;
		}

		// check if segment overlaps with previous record
		final int tolerance = PrefHelper.getInt(SegmentOverlapCheck.OVERLAP_TOLERANCE_PROPERTY, SegmentOverlapCheck.DEFAULT_OVERLAP_TOLERANCE);
		MediaSegment validated = getValidatedObject();
		if(getEditor().getCurrentRecordIndex() > 0) {
			Record currentRecord = getEditor().currentRecord();
			int idx = getEditor().getCurrentRecordIndex()-1;
			Record prevRecord = null;
			while(idx >= 0 && prevRecord == null) {
				Record r = getEditor().getSession().getRecord(idx--);
				if(r.getSpeaker() == currentRecord.getSpeaker())
					prevRecord = r;
			}
			if(prevRecord != null) {
				MediaSegment prevSegment = prevRecord.getSegment().getGroup(0);
				if(prevSegment != null) {
					final float diffMs = validated.getStartValue() - prevSegment.getEndValue();

					if( (diffMs < 0) && (Math.abs(diffMs) > tolerance) ) {
						// XXX Border does not update properly while typing
						getGroupFieldBorder().setShowWarningIcon(true);
						segmentField.setToolTipText("Segment overlaps with previous record for " + prevRecord.getSpeaker() + " (#" + (idx+2) + ")");
						repaint();
					}
				}
			}
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
		segmentField.setText(tierTxt);
	}

	public void onEnter() {
		if(getGroupValue() != initialGroupVal) {
			for(TierEditorListener listener:getTierEditorListeners()) {
				listener.tierValueChanged(segmentTier, 0, getValidatedObject(), initialGroupVal);
			}
			initialGroupVal = getGroupValue();
		}
	}

	private void updateTier() {
		final MediaSegment oldVal = getGroupValue();
		final MediaSegment newVal = getValidatedObject();
		if(newVal != null) {
			for(TierEditorListener listener:listeners) {
				listener.tierValueChange(segmentTier, groupIndex, newVal, oldVal);
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

	private MediaSegment initialGroupVal;
	private final FocusListener focusListener = new FocusListener() {

		@Override
		public void focusLost(FocusEvent e) {
			if(getGroupValue() != initialGroupVal) {
				for(TierEditorListener listener:getTierEditorListeners()) {
					listener.tierValueChanged(segmentTier, 0, getValidatedObject(), initialGroupVal);
				}
			}
		}

		@Override
		public void focusGained(FocusEvent e) {
			initialGroupVal = getGroupValue();
		}

	};

	private final DocumentListener docListener = new DocumentListener() {

		@Override
		public void insertUpdate(DocumentEvent de) {
			if(validateText() && segmentField.hasFocus())
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
			if(!segmentField.hasFocus() && index == groupIndex) {
				updateText();
			}
			validateText();
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
