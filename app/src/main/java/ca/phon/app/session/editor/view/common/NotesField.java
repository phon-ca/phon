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

public class NotesField extends GroupField<String> {


	private static final long serialVersionUID = 7309167333058601139L;

	public NotesField(Tier<String> tier) {
		super(tier, 0, true);
	}
	
	
}
