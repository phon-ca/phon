package ca.phon.app.session.editor.tier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;

import ca.phon.session.Tier;

public class NotesField extends JTextArea implements TierEditor {

	private static final long serialVersionUID = 7309167333058601139L;

	private final Tier<String> notesTier;
	
	public NotesField(Tier<String> tier) {
		super();
		
		notesTier = tier;
		update();
		
		super.setLineWrap(true);
		super.setWrapStyleWord(true);
	}
	
	private void update() {
		final String text = notesTier.getGroup(0);
		setText(text);
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

}
