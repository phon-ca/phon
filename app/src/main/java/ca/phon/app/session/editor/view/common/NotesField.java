package ca.phon.app.session.editor.view.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JTextArea;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import ca.phon.session.Tier;
import ca.phon.session.TierListener;

public class NotesField extends JTextArea implements TierEditor {

	private static final long serialVersionUID = 7309167333058601139L;

	private final Tier<String> notesTier;
	
	public NotesField(Tier<String> tier) {
		super();
		
		notesTier = tier;
		notesTier.addTierListener(tierListener);
		update();
		
		super.setOpaque(false);
		super.setLineWrap(true);
		super.setWrapStyleWord(true);
		
		getDocument().addDocumentListener(docListener);
	}
	
	private void update() {
		final String text = getGroupValue();
		setText(text);
	}
	
	private String getGroupValue() {
		return (notesTier.numberOfGroups() > 0 ? notesTier.getGroup(0) : new String());
	}
	
	private void updateTier() {
		final String oldVal = getGroupValue();
		final String newVal = getText();
		for(TierEditorListener listener:listeners) {
			listener.tierValueChanged(notesTier, 0, newVal, oldVal);
		}
	}

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

	private final DocumentListener docListener = new DocumentListener() {
		
		@Override
		public void removeUpdate(DocumentEvent e) {
			if(hasFocus())
				updateTier();
		}
		
		@Override
		public void insertUpdate(DocumentEvent e) {
			if(hasFocus())
				updateTier();
		}
		
		@Override
		public void changedUpdate(DocumentEvent e) {
		}
		
	};
	
	private final TierListener<String> tierListener = new TierListener<String>() {
		
		@Override
		public void groupsCleared(Tier<String> tier) {
		}
		
		@Override
		public void groupRemoved(Tier<String> tier, int index, String value) {
		}
		
		@Override
		public void groupChanged(Tier<String> tier, int index, String oldValue,
				String value) {
			if(!hasFocus()) {
				setText(value);
			}
		}
		
		@Override
		public void groupAdded(Tier<String> tier, int index, String value) {
		}
	};
	
}
