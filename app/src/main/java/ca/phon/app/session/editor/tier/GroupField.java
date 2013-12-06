package ca.phon.app.session.editor.tier;

import java.text.ParseException;

import javax.swing.JComponent;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import ca.phon.app.session.editor.SessionEditor;
import ca.phon.app.session.editor.undo.TierEdit;
import ca.phon.formatter.Formatter;
import ca.phon.formatter.FormatterFactory;
import ca.phon.session.Tier;

/**
 * Text field for editing tier data for a group.
 */
public class GroupField<T> extends JTextArea implements TierEditor {
	
	private static final long serialVersionUID = -5541784214656593497L;

	private final SessionEditor editor;
	
	private final Tier<T> tier;
	
	private final int groupIndex;
	
	public GroupField(SessionEditor editor, Tier<T> tier, int groupIndex) {
		super();
		this.editor = editor;
		this.tier = tier;
		this.groupIndex = groupIndex;
		
		init();
	}
	
	public SessionEditor getEditor() {
		return this.editor;
	}
	
	public Tier<T> getTier() {
		return this.tier;
	}
	
	public int getGroupIndex() {
		return this.groupIndex;
	}
	
	private void init() {
		final GroupFieldBorder border = new GroupFieldBorder();
		setBorder(border);
		
		final T val = getGroupValue();
		@SuppressWarnings("unchecked")
		final Formatter<T> formatter = 
				(Formatter<T>)FormatterFactory.createFormatter(tier.getDeclaredType());
		if(formatter != null) {
			setText(formatter.format(val));
		} else {
			setText(val.toString());
		}
		
		getDocument().addDocumentListener(docListener);
	}
	
	/**
	 * Get the group value
	 * 
	 * @return current group value
	 */
	public T getGroupValue() {
		return tier.getGroup(groupIndex);
	}
	
	/**
	 * Set group value.
	 * 
	 * @param val
	 */
	public void setGroupValue(T val) {
		final TierEdit<T> tierEdit = new TierEdit<T>(editor, tier, groupIndex, val);
		editor.getUndoSupport().postEdit(tierEdit);
	}
	
	/**
	 * Validate text contents
	 * 
	 * @return <code>true</code> if the contents of the field
	 *  are valid, <code>false</code> otherwise.
	 */
	private T validatedObj;
	protected boolean validateText() {
		boolean retVal = true;

		final String text = getText();
		
		// look for a formatter
		@SuppressWarnings("unchecked")
		final Formatter<T> formatter = 
				(Formatter<T>)FormatterFactory.createFormatter(tier.getDeclaredType());
		if(formatter != null) {
			try {
				validatedObj = formatter.parse(text);
			} catch (ParseException e) {
				retVal = false;
			}
		}
		
		return retVal;
	}
	
	protected void update() {
		if(validatedObj != null) {
			setGroupValue(validatedObj);
		}
	}
	
	private final DocumentListener docListener = new DocumentListener() {
		
		@Override
		public void removeUpdate(DocumentEvent e) {
			if(validateText()) {
				update();
			}
		}
		
		@Override
		public void insertUpdate(DocumentEvent e) {
			if(validateText()) {
				update();
			}
		}
		
		@Override
		public void changedUpdate(DocumentEvent e) {
			
		}
	};

	@Override
	public JComponent getEditorComponent() {
		return this;
	}
}
