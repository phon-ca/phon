package ca.phon.app.session.editor.view.common;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Toolkit;
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
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.TextUI;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
import javax.swing.undo.UndoManager;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.jdesktop.swingx.JXTextArea;
import org.jdesktop.swingx.JXTextField;

import ca.phon.formatter.Formatter;
import ca.phon.formatter.FormatterFactory;
import ca.phon.ipadictionary.spi.RemoveEntry;
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
	
	private final UndoManager undoManager = new UndoManager();
	
	private volatile boolean hasChanges = false;
	
	protected volatile boolean allowNewline = false;
	
	public GroupField(Tier<T> tier, int groupIndex) {
		this(tier, groupIndex, false);
	}
	
	public GroupField(Tier<T> tier, int groupIndex, boolean allowNewLine) {
		super();
		this.tier = tier;
		this.groupIndex = groupIndex;
		this.allowNewline = allowNewLine;
		
		setupInputMap();
		
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
	
		setLineWrap(allowNewLine);
		setWrapStyleWord(allowNewline);
		
		setBackground(new Color(255,255,255,0));
		setOpaque(false);
		_init();
		tier.addTierListener(tierListener);
		addFocusListener(focusListener);
		
		getDocument().addDocumentListener(docListener);
		getDocument().addUndoableEditListener(undoManager);
	}
	
//	@Override
//	public void paintComponent(Graphics g) {
//		final Graphics2D g2 = (Graphics2D)g;
//		
//		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
//		g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
//		
//		super.paintComponent(g2);
//	}
	
	private void setupInputMap() {
		final ActionMap am = getActionMap();
		final InputMap im = getInputMap(WHEN_FOCUSED);
		
		// remove actions for PG_UP and PG_DOWN
		final KeyStroke pgUpKs = KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP, 0);
		final KeyStroke pgDnKs = KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN, 0);
		im.remove(pgDnKs);
		im.remove(pgUpKs);
		
		final KeyStroke validateKs = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
		final String validateId = "validate";
		final PhonUIAction validateAct = new PhonUIAction(this, "onEnter");
		am.put(validateId, validateAct);
		im.put(validateKs, validateId);
		
		// override undo/redo for editor window
		final KeyStroke undoKs = 
				KeyStroke.getKeyStroke(KeyEvent.VK_Z, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
		final PhonUIAction undoAct = new PhonUIAction(this, "onUndo");
		final String undoKey = "_custom_undo_";
		am.put(undoKey, undoAct);
		im.put(undoKs, undoKey);
		
		final KeyStroke redoKs = 
				KeyStroke.getKeyStroke(KeyEvent.VK_Y, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
		final PhonUIAction redoAct = new PhonUIAction(this, "onRedo");
		final String redoKey = "_custom_redo_";
		am.put(redoKey, redoAct);
		im.put(redoKs, redoKey);
		
		setActionMap(am);
		setInputMap(WHEN_FOCUSED, im);
	}
	
	public Tier<T> getTier() {
		return this.tier;
	}
	
	public int getGroupIndex() {
		return this.groupIndex;
	}
	
	public void onUndo() {
		undoManager.undo();
	}
	
	public void onRedo() {
		undoManager.redo();
	}
	
	@Override
	public void setFont(Font font) {
		lastPrefSize = null;
		super.setFont(font);
	}
	
	private Dimension lastPrefSize = null;
	@Override
	public Dimension getPreferredSize() {
		final Dimension retVal = super.getPreferredSize();
		
		if(lastPrefSize != null && lastPrefSize.height < retVal.height) {
			if(!allowNewline) {
				retVal.height = lastPrefSize.height;
			}
		}
		lastPrefSize = retVal;
		
		return retVal;
	}
	
	/**
	 * Setup border, listeners and initial text value.
	 */
	protected void _init() {
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
		}
		return retVal;
	}
	
	/**
	 * Called when the 'Enter' key is pressed.
	 * 
	 */
	public void onEnter() {
		if(allowNewline) {
			try {
				getDocument().insertString(getCaretPosition(), "\n", null);
			} catch (BadLocationException e) {}
		} else {
			if(hasChanges) {
				if(validateText()) {
					update();
					for(TierEditorListener listener:getTierEditorListeners()) {
						listener.tierValueChanged(getTier(), getGroupIndex(), getValidatedObject(), initialGroupVal);
					}
					hasChanges = false;
				} else {
					Toolkit.getDefaultToolkit().beep();
					requestFocus();
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
			for(TierEditorListener listener:getTierEditorListeners()) {
				listener.tierValueChange(getTier(), getGroupIndex(), validatedObj, initialGroupVal);
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
			if(validateText()) {
				undoManager.discardAllEdits();
				if(hasChanges) {
					update();
					for(TierEditorListener listener:getTierEditorListeners()) {
						listener.tierValueChanged(getTier(), getGroupIndex(), getValidatedObject(), initialGroupVal);
					}
				}
			} else {
				Toolkit.getDefaultToolkit().beep();
				requestFocus();
			}
		}
		
		@Override
		public void focusGained(FocusEvent e) {
			initialGroupVal = getGroupValue();
			validatedObjRef.set(initialGroupVal);
			hasChanges = false;
		}
		
	};
	
	private final DocumentListener docListener = new DocumentListener() {
		
		@Override
		public void removeUpdate(DocumentEvent e) {
			if(getText().length() > 0) validateText();
			hasChanges = true;
		}
		
		@Override
		public void insertUpdate(DocumentEvent e) {
			if(getText().length() > 0) validateText();
			hasChanges = true;
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
			if(getGroupIndex() == index) {
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
