/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2016, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
 * Dept of Linguistics, Memorial University <https://phon.ca>
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

import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

import javax.swing.ActionMap;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JSeparator;
import javax.swing.KeyStroke;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import ca.phon.app.session.editor.SessionEditor;
import ca.phon.app.session.editor.view.media_player.MediaPlayerEditorView;
import ca.phon.app.session.editor.view.speech_analysis.SpeechAnalysisEditorView;
import ca.phon.formatter.Formatter;
import ca.phon.formatter.FormatterFactory;
import ca.phon.media.sampled.PCMSegmentView;
import ca.phon.session.MediaSegment;
import ca.phon.session.Record;
import ca.phon.session.SessionFactory;
import ca.phon.session.Tier;
import ca.phon.session.TierListener;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;
import uk.co.caprica.vlcj.binding.internal.libvlc_media_t;
import uk.co.caprica.vlcj.player.MediaPlayer;
import uk.co.caprica.vlcj.player.MediaPlayerEventAdapter;
import uk.co.caprica.vlcj.player.MediaPlayerEventListener;

/**
 * Editor for media segments.
 */
public class SegmentTierComponent extends JComponent implements TierEditor {
	
	private static final long serialVersionUID = 5303962410367183323L;

	private static final Logger LOGGER = Logger
			.getLogger(SegmentTierComponent.class.getName());
	
	private final static String DEFAULT_SEGMENT_TEXT = "000:00.000-000:00.000";
	
	private final WeakReference<SessionEditor> editorRef;

	private Tier<MediaSegment> segmentTier;
	
	private int groupIndex = 0;
	
	private final SegmentField segmentField;
	
	private final JButton playButton;
	private final ImageIcon playIcon = 
			IconManager.getInstance().getIcon("actions/media-playback-start", IconSize.SMALL);
	private final ImageIcon pauseIcon =
			IconManager.getInstance().getIcon("actions/media-playback-pause", IconSize.SMALL);

	public SegmentTierComponent(SessionEditor editor, Tier<MediaSegment> tier, int groupIndex) {
		super();
		setOpaque(false);
		setFocusable(false);
		
		this.editorRef = new WeakReference<SessionEditor>(editor);
		
		this.segmentTier = tier;
		segmentTier.addTierListener(tierListener);
		this.groupIndex = groupIndex;
		
		segmentField = new SegmentField();
		
		updateText();
		validateText();
		segmentField.getDocument().addDocumentListener(docListener);
		
		// validate text when 'enter' is pressed
		final ActionMap actionMap = segmentField.getActionMap();
		final InputMap inputMap = segmentField.getInputMap(JComponent.WHEN_FOCUSED);
		
		final KeyStroke validateKs = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
		final String validateId = "validate";
		final PhonUIAction validateAct = new PhonUIAction(this, "onEnter");
		actionMap.put(validateId, validateAct);
		inputMap.put(validateKs, validateId);
		
		segmentField.setActionMap(actionMap);
		segmentField.setInputMap(JComponent.WHEN_FOCUSED, inputMap);
		
		segmentField.addFocusListener(focusListener);
		
		final PhonUIAction playAct = new PhonUIAction(this, "onPlaySegment");
		
		playAct.putValue(PhonUIAction.SMALL_ICON, playIcon);
		playButton = new JButton(playAct);
		playButton.setFocusable(false);
		
		setLayout(new FlowLayout(FlowLayout.LEADING, 0, 0));
		add(segmentField);
		add(new JSeparator());
		add(playButton);
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
	
	public void onPlaySegment() {
		SessionEditor editor = getEditor();
		if(editor == null) return;
		
		if(playButton.getIcon() == playIcon) {
			final MediaSegment segment = getGroupValue();
			if(segment.getEndValue() - segment.getStartValue() <= 0) {
				return;
			}
			
			// try the media player first
			if(editor.getViewModel().isShowing(MediaPlayerEditorView.VIEW_TITLE)) {
				final MediaPlayerEditorView mediaPlayerEditorView = 
						(MediaPlayerEditorView)editor.getViewModel().getView(MediaPlayerEditorView.VIEW_TITLE);
				if(mediaPlayerEditorView != null) {
					playButton.setIcon(pauseIcon);
					mediaPlayerEditorView.getPlayer().addMediaPlayerListener(new MediaPlayerEventAdapter() {
						
						@Override
						public void paused(MediaPlayer mediaPlayer) {
							mediaPlayer.removeMediaPlayerEventListener(this);
							playButton.setIcon(playIcon);
						}
						
					});
					mediaPlayerEditorView.getPlayer().playSegment((long)segment.getStartValue(), 
							(long)(segment.getEndValue()-segment.getStartValue()));
				}
			} else if(editor.getViewModel().isShowing(SpeechAnalysisEditorView.VIEW_TITLE)) {
				final SpeechAnalysisEditorView waveformEditorView =
						(SpeechAnalysisEditorView)editor.getViewModel().getView(SpeechAnalysisEditorView.VIEW_TITLE);
				if(waveformEditorView != null) {
					playButton.setIcon(pauseIcon);
					waveformEditorView.getWavDisplay().addPropertyChangeListener(PCMSegmentView.PLAYING_PROP, new PropertyChangeListener() {
						
						@Override
						public void propertyChange(PropertyChangeEvent evt) {
							if(!waveformEditorView.getWavDisplay().isPlaying()) {
								playButton.setIcon(playIcon);
								waveformEditorView.getWavDisplay().removePropertyChangeListener(this);
							}
						}
						
					});
					waveformEditorView.play();
				}
			}
		} else {
			if(editor.getViewModel().isShowing(MediaPlayerEditorView.VIEW_TITLE)) {
				final MediaPlayerEditorView mediaPlayerEditorView = 
						(MediaPlayerEditorView)editor.getViewModel().getView(MediaPlayerEditorView.VIEW_TITLE);
				if(mediaPlayerEditorView != null) {
					mediaPlayerEditorView.getPlayer().pause();
				}
			} else if(editor.getViewModel().isShowing(SpeechAnalysisEditorView.VIEW_TITLE)) {
				final SpeechAnalysisEditorView waveformEditorView =
						(SpeechAnalysisEditorView)editor.getViewModel().getView(SpeechAnalysisEditorView.VIEW_TITLE);
				if(waveformEditorView != null) {
					waveformEditorView.getWavDisplay().stop();
				}
			}
		}
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
		MediaSegment validated = getValidatedObject();
		if(getEditor().getCurrentRecordIndex() > 0) {
			Record prevRecord = getEditor().getSession().getRecord(getEditor().getCurrentRecordIndex()-1);
			MediaSegment prevSegment = prevRecord.getSegment().getGroup(0);
			if(prevSegment != null) {
				if(prevSegment.getEndValue() > validated.getStartValue()) {
					// XXX Border does not update properly while typing
					getGroupFieldBorder().setShowWarningIcon(true);
					segmentField.setToolTipText("Segment overlaps with previous record");
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
			if(segmentField.hasFocus() && validateText())
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
