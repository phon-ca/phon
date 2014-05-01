package ca.phon.app.session.editor.view.common;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.DefaultCaret;

import ca.phon.formatter.Formatter;
import ca.phon.formatter.FormatterFactory;
import ca.phon.session.Tier;
import ca.phon.session.TierListener;
import ca.phon.ui.action.PhonUIAction;

/**
 * Text field for editing tier data for a group.
 */
public class GroupField<T> extends JTextArea implements TierEditor {
	
	private static final long serialVersionUID = -5541784214656593497L;
	
	private final Tier<T> tier;
	
	private final int groupIndex;
	
	public GroupField(Tier<T> tier, int groupIndex) {
		super();
		this.tier = tier;
		this.groupIndex = groupIndex;
		
		// XXX
		// When added to a panel which is inside a scroll pane
		// caret updates will cause the JScrollPane to auto-scroll
		// even when setAutoscrolls is set to false.  The
		// caret update policy needs to be changed on focus changes
		// to avoid this bug
		setAutoscrolls(false);
		final DefaultCaret caret = (DefaultCaret)getCaret();
		caret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
		addFocusListener(new FocusListener() {
			
			@Override
			public void focusLost(FocusEvent e) {
				caret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
			}
			
			@Override
			public void focusGained(FocusEvent e) {
				caret.setUpdatePolicy(DefaultCaret.UPDATE_WHEN_ON_EDT);
			}
			
		});
		
		setOpaque(false);
		init();
		tier.addTierListener(tierListener);
		addFocusListener(focusListener);
		getDocument().addDocumentListener(docListener);
	}
	
	public Tier<T> getTier() {
		return this.tier;
	}
	
	public int getGroupIndex() {
		return this.groupIndex;
	}
	
	/**
	 * Setup border, listeners and initial text value.
	 */
	protected void init() {
		final GroupFieldBorder border = new GroupFieldBorder();
		setBorder(border);
		
		final T val = getGroupValue();
		String text = new String();
		if(val != null) {
			@SuppressWarnings("unchecked")
			final Formatter<T> formatter = 
					(Formatter<T>)FormatterFactory.createFormatter(tier.getDeclaredType());
			if(formatter != null) {
				text = formatter.format(val);
			} else {
				text = val.toString();
			}
		}
		setText(text);
		
		// validate text when 'enter' is pressed
		final ActionMap actionMap = getActionMap();
		final InputMap inputMap = getInputMap(JComponent.WHEN_FOCUSED);
		
		final KeyStroke validateKs = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
		final String validateId = "validate";
		final PhonUIAction validateAct = new PhonUIAction(this, "onEnter");
		actionMap.put(validateId, validateAct);
		inputMap.put(validateKs, validateId);
		
		setActionMap(actionMap);
		setInputMap(JComponent.WHEN_FOCUSED, inputMap);
	}
	
	/**
	 * Get the group value
	 * 
	 * @return current group value
	 */
	public T getGroupValue() {
		T retVal = null;
		if(groupIndex < tier.numberOfGroups()) {
			retVal = tier.getGroup(groupIndex);
//			for(int i = tier.numberOfGroups(); i <= groupIndex; i++) tier.addGroup();
		}
//		retVal = tier.getGroup(groupIndex);
		return retVal;
	}
	
	/**
	 * Called when the 'Enter' key is pressed.
	 * 
	 */
	public void onEnter() {
		if(getGroupValue() != initialGroupVal) {
			for(TierEditorListener listener:getTierEditorListeners()) {
				listener.tierValueChanged(getTier(), getGroupIndex(), getValidatedObject(), initialGroupVal);
			}
			initialGroupVal = getGroupValue();
		}
	}
	
	/**
	 * Validate text contents
	 * 
	 * @return <code>true</code> if the contents of the field
	 *  are valid, <code>false</code> otherwise.
	 */
	private final AtomicReference<T> validatedObjRef = new AtomicReference<T>();
	protected boolean validateText() {
		boolean retVal = true;

		final String text = getText();
		
		// look for a formatter
		@SuppressWarnings("unchecked")
		final Formatter<T> formatter = 
				(Formatter<T>)FormatterFactory.createFormatter(tier.getDeclaredType());
		if(formatter != null) {
			try {
				final T validatedObj = formatter.parse(text);
				setValidatedObject(validatedObj);
			} catch (ParseException e) {
				retVal = false;
			}
		}
		
		return retVal;
	}
	
	protected void update() {
		final T validatedObj = getValidatedObject();
		if(validatedObj != null) {
			final T oldVal = getGroupValue();
			for(TierEditorListener listener:getTierEditorListeners()) {
				listener.tierValueChange(getTier(), getGroupIndex(), validatedObj, oldVal);
			}
		}
	}
	
	public void validateAndUpdate() {
		if(validateText())
			update();
	}
	
	protected T getValidatedObject() {
		return this.validatedObjRef.get();
	}
	
	protected void setValidatedObject(T object) {
		this.validatedObjRef.getAndSet(object);
	}
	
	private T initialGroupVal;
	private final FocusListener focusListener = new FocusListener() {
		
		@Override
		public void focusLost(FocusEvent e) {
			if(getGroupValue() != initialGroupVal) {
				for(TierEditorListener listener:getTierEditorListeners()) {
					listener.tierValueChanged(getTier(), getGroupIndex(), getValidatedObject(), initialGroupVal);
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
		public void removeUpdate(DocumentEvent e) {
			if(hasFocus() && validateText()) {
				update();
			}
		}
		
		@Override
		public void insertUpdate(DocumentEvent e) {
			if(hasFocus() && validateText()) {
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
	
	private final TierListener<T> tierListener = new TierListener<T>() {

		@Override
		public void groupAdded(Tier<T> tier, int index, T value) {
		}

		@Override
		public void groupRemoved(Tier<T> tier, int index, T value) {
		}

		@Override
		public void groupChanged(Tier<T> tier, int index, T oldValue, T value) {
			if(!hasFocus() && getGroupIndex() == index) {
				final T val = getGroupValue();
				String text = new String();
				if(val != null) {
					@SuppressWarnings("unchecked")
					final Formatter<T> formatter = 
							(Formatter<T>)FormatterFactory.createFormatter(tier.getDeclaredType());
					if(formatter != null) {
						text = formatter.format(val);
					} else {
						text = val.toString();
					}
				}
				setText(text);
			}
		}

		@Override
		public void groupsCleared(Tier<T> tier) {
		}
		
	};
}
