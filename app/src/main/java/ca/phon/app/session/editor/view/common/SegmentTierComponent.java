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
import ca.phon.query.report.io.Group;
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

	private final static String DEFAULT_SEGMENT_TEXT = "000:00.000-000:00.000";

	private final WeakReference<SessionEditor> editorRef;

	private Record record;

	private Tier<GroupSegment> segmentTier;

	private int groupIndex = 0;

	private final SegmentField segmentField;

	public SegmentTierComponent(SessionEditor editor, Record record, Tier<GroupSegment> tier, int groupIndex) {
		super();
		setOpaque(false);
		setFocusable(false);

		this.editorRef = new WeakReference<SessionEditor>(editor);
		this.record = record;
		
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
	private final AtomicReference<GroupSegment> validatedObjRef = new AtomicReference<GroupSegment>();
	protected boolean validateText() {
		boolean retVal = true;

		final String text = segmentField.getText();

		// look for a formatter
		final Formatter<MediaSegment> formatter = FormatterFactory.createFormatter(MediaSegment.class);

		try {
			final MediaSegment validatedObj = formatter.parse(text);
			if(validatedObj.getEndValue() >= validatedObj.getStartValue()) {
				setValidatedObject(new GroupSegment(record.getSegment().getRecordSegment(), validatedObj.getStartValue(), validatedObj.getEndValue()));
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

		return retVal;
	}

	public void validateAndUpdate() {
		if(validateText())
			updateTier();
	}

	protected GroupSegment getValidatedObject() {
		return this.validatedObjRef.get();
	}

	protected void setValidatedObject(GroupSegment object) {
		this.validatedObjRef.getAndSet(object);
	}

	public void updateText() {
		final GroupSegment segment = getGroupValue();
		final Formatter<MediaSegment> segmentFormatter = FormatterFactory.createFormatter(MediaSegment.class);
		final MediaSegment tempSeg = SessionFactory.newFactory().createMediaSegment();
		tempSeg.setSegment(segment.getStartTime(), segment.getEndTime());

		String tierTxt =
				(segmentFormatter != null ? segmentFormatter.format(tempSeg) : DEFAULT_SEGMENT_TEXT);
		segmentField.setText(tierTxt);
	}

	public void onEnter() {
		if(getGroupValue() != initialGroupVal) {
			for(TierEditorListener listener:getTierEditorListeners()) {
				listener.tierValueChanged(segmentTier, groupIndex, getValidatedObject(), initialGroupVal);
			}
			initialGroupVal = getGroupValue();
		}
	}

	private void updateTier() {
		final GroupSegment oldVal = getGroupValue();
		final GroupSegment newVal = getValidatedObject();
		if(newVal != null) {
			for(TierEditorListener listener:listeners) {
				listener.tierValueChange(segmentTier, groupIndex, newVal, oldVal);
			}
		}
	}

	private GroupSegment getGroupValue() {
		GroupSegment retVal = null;

		if(groupIndex < segmentTier.numberOfGroups()) {
			retVal = segmentTier.getGroup(groupIndex);
		} else {
			retVal = new GroupSegment(record.getSegment().getRecordSegment(), 0.0f, 0.0f);
		}

		return retVal;
	}

	private GroupSegment initialGroupVal;
	private final FocusListener focusListener = new FocusListener() {

		@Override
		public void focusLost(FocusEvent e) {
			if(getGroupValue() != initialGroupVal) {
				for(TierEditorListener listener:getTierEditorListeners()) {
					listener.tierValueChanged(segmentTier, groupIndex, getValidatedObject(), initialGroupVal);
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

	private final TierListener<GroupSegment> tierListener = new TierListener<GroupSegment>() {

		@Override
		public void groupsCleared(Tier<GroupSegment> tier) {
		}

		@Override
		public void groupRemoved(Tier<GroupSegment> tier, int index,
				GroupSegment value) {
		}

		@Override
		public void groupChanged(Tier<GroupSegment> tier, int index,
				GroupSegment oldValue, GroupSegment value) {
			if(!segmentField.hasFocus() && index == groupIndex) {
				updateText();
			}
			validateText();
		}

		@Override
		public void groupAdded(Tier<GroupSegment> tier, int index,
				GroupSegment value) {
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
