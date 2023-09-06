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

import ca.phon.app.log.LogUtil;
import ca.phon.app.session.editor.SessionEditor;
import ca.phon.app.session.editor.SessionEditorWindow;
import ca.phon.extensions.*;
import ca.phon.formatter.Formatter;
import ca.phon.formatter.*;
import ca.phon.ipa.IPATranscript;
import ca.phon.session.*;
import ca.phon.ui.CommonModuleFrame;
import ca.phon.ui.action.PhonUIAction;
import org.apache.logging.log4j.LogManager;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.text.Highlighter.Highlight;
import javax.swing.undo.UndoManager;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.util.List;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Text field for editing tier data for a group.
 */
public class GroupField<T> extends JTextArea implements TierEditor<T> {

	private final Tier<T> tier;

	private final UndoManager undoManager = new UndoManager();

	private volatile boolean hasChanges = false;

	protected volatile boolean allowNewline = false;

	private final Highlighter errHighlighter = new DefaultHighlighter();

	private final GroupFieldBorder groupFieldBorder = new GroupFieldBorder();

	public GroupField(Tier<T> tier) {
		this(tier, false);
	}

	public GroupField(Tier<T> tier, boolean allowNewLine) {
		super();
		this.tier = tier;
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

		setDragEnabled(false);
		setDropTarget(null);
		
		setLineWrap(allowNewLine);
		setWrapStyleWord(allowNewline);

		setBackground(new Color(255,255,255,0));
		setOpaque(false);
		_init();
		hasChanges = false;
		tier.addTierListener(tierListener);
		addFocusListener(focusListener);

		getDocument().addDocumentListener(docListener);
		getDocument().addUndoableEditListener(undoManager);
		getDocument().addUndoableEditListener( (e) -> {
			if(hasChanges && hasFocus()) {
				CommonModuleFrame cmf = CommonModuleFrame.getCurrentFrame();
				if(cmf != null && cmf instanceof SessionEditorWindow sessionEditorWindow) {
					SessionEditor editor = sessionEditorWindow.getSessionEditor();
					editor.setModified(true);
				}
			}
		});
		errHighlighter.install(this);
	}

	@Override
	public void paintComponent(Graphics g) {
		final Graphics2D g2 = (Graphics2D)g;

		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_RENDERING,
		          RenderingHints.VALUE_RENDER_QUALITY);

		super.paintComponent(g2);
		if(getErrorHighlights().length > 0) {
			getErrorHighlighter().paint(g2);
		}
	}

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
		final PhonUIAction<Void> validateAct = PhonUIAction.runnable(this::onEnter);
		am.put(validateId, validateAct);
		im.put(validateKs, validateId);

		// override undo/redo for editor window
		final KeyStroke undoKs =
				KeyStroke.getKeyStroke(KeyEvent.VK_Z, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx());
		final PhonUIAction<Void> undoAct = PhonUIAction.runnable(this::onUndo);
		final String undoKey = "_custom_undo_";
		am.put(undoKey, undoAct);
		im.put(undoKs, undoKey);

		final KeyStroke redoKs =
				KeyStroke.getKeyStroke(KeyEvent.VK_Y, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx());
		final PhonUIAction<Void> redoAct = PhonUIAction.runnable(this::onRedo);
		final String redoKey = "_custom_redo_";
		am.put(redoKey, redoAct);
		im.put(redoKs, redoKey);

		final KeyStroke saveKs =
				KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx());
		final PhonUIAction<Void> saveAct = PhonUIAction.runnable(this::onSave);
		final String saveKey = "_custom_save_";
		am.put(saveKey, saveAct);
		im.put(saveKs, saveKey);

		setActionMap(am);
		setInputMap(WHEN_FOCUSED, im);
	}

	public Tier<T> getTier() {
		return this.tier;
	}

	public void onUndo() {
		if(undoManager.canUndo() && hasChanges) {
			undoManager.undo();
		} else {
			// HACK need to call parent frames undo if we have no changes
			CommonModuleFrame cmf = CommonModuleFrame.getCurrentFrame();
			if(cmf == null) return;

			final UndoManager cmfUndoManager = cmf.getExtension(UndoManager.class);
			if(cmfUndoManager != null && cmfUndoManager.canUndo()) {
				 cmfUndoManager.undo();

				 // reset this flag - otherwise the group undo manager
				 // will attempt to undo with 'nothing' as last value
				 hasChanges = false;
			}
		}
	}

	public void onRedo() {
		if(undoManager.canRedo())
			undoManager.redo();
		else {
			// HACK need to call parent frames undo if we have no changes
			CommonModuleFrame cmf = CommonModuleFrame.getCurrentFrame();
			if(cmf == null) return;

			final UndoManager cmfUndoManager = cmf.getExtension(UndoManager.class);
			if(cmfUndoManager != null && cmfUndoManager.canRedo()) {
				 cmfUndoManager.redo();

				 // reset flag
				 hasChanges = false;
			}
		}
	}

	public void onSave() {
		if(hasChanges) {
			undoManager.discardAllEdits();
			if(validateText()) {
				update();
				for(TierEditorListener listener:getTierEditorListeners()) {
					listener.tierValueChanged(getTier(), getValidatedObject(), initialGroupVal, false);
				}
				hasChanges = false;
			} else {
				Toolkit.getDefaultToolkit().beep();
				requestFocus();
			}
		}

		CommonModuleFrame cmf = CommonModuleFrame.getCurrentFrame();
		if(cmf == null) return;

		// look for Save action in file menu
		final JMenuBar windowMenu = cmf.getJMenuBar();
		JMenu fileMenu = null;
		for(int i = 0; i < windowMenu.getMenuCount(); i++) {
			if(windowMenu.getMenu(i).getText().equals("File")) {
				fileMenu = windowMenu.getMenu(i);
				break;
			}
		}
		if(fileMenu != null) {
			JMenuItem saveItem = null;
			for(int i = 0; i < fileMenu.getItemCount(); i++) {
				if(fileMenu.getMenuComponent(i) instanceof  JMenuItem) {
					JMenuItem item = (JMenuItem) fileMenu.getMenuComponent(i);
					if(item.getText().equals("Save")) {
						saveItem = item;
						break;
					}
				}
			}
			if(saveItem != null) {
				saveItem.getAction().actionPerformed(new ActionEvent(this, -1, "save"));
			} else {
				try {
					cmf.saveData();
				} catch (IOException e) {
					LogUtil.severe(e.getLocalizedMessage(), e);
				}
			}
		} else {
			// finally try to call save on parent frame (if any)
			try {
				cmf.saveData();
			} catch (IOException e) {
				LogUtil.severe(e.getLocalizedMessage(), e);
			}
		}
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
		setBorder(groupFieldBorder);

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

			// XXX if text length is 0, check to see if there's an
			// UnvalidatedValue assigned to this object
			if(val instanceof IExtendable) {
				final IExtendable extVal = (IExtendable)val;
				final UnvalidatedValue unvalidatedValue = extVal.getExtension(UnvalidatedValue.class);
				if(unvalidatedValue != null) {
					text = unvalidatedValue.getValue();
					((GroupFieldBorder)getBorder()).setShowWarningIcon(true);
				}
			}
		}
		setText(text);
	}

	public GroupFieldBorder getGroupFieldBorder() {
		return this.groupFieldBorder;
	}

	public Highlighter getErrorHighlighter() {
		return this.errHighlighter;
	}

	public Object addErrorHighlight(int p0, int p1) {
		Object retVal = null;

		try {
			retVal = errHighlighter.addHighlight(p0, p1, errPainter);
		} catch (BadLocationException e) {
			LogUtil.severe( e.getLocalizedMessage(), e);
		}

		return retVal;
	}

	public void removeErrorHighlight(Object tag) {
		errHighlighter.removeHighlight(tag);
	}

	public void removeAllErrorHighlights() {
		errHighlighter.removeAllHighlights();
	}

	public void changeErrorHighlight(Object tag, int p0, int p1) {
		try {
			errHighlighter.changeHighlight(tag, p0, p1);
		} catch (BadLocationException e) {
			LogUtil.severe( e.getLocalizedMessage(), e);
		}
	}

	public Highlight[] getErrorHighlights() {
		return errHighlighter.getHighlights();
	}

	/**
	 * Get the group value
	 *
	 * @return current group value
	 */
	public T getGroupValue() {
		return getTier().getValue();
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
				undoManager.discardAllEdits();
				if(validateText()) {
					update();
					for(TierEditorListener listener:getTierEditorListeners()) {
						listener.tierValueChanged(getTier(), getValidatedObject(), initialGroupVal, false);
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
		System.out.println(text);
//		getTier().setText(text);
//		if(getTier().isUnvalidated()) {
//			int offset = getTier().getUnvalidatedValue().getParseError().getErrorOffset();
//			addErrorHighlight(offset, offset+1);
//			retVal = false;
//		}
		// look for a formatter
		final Formatter<T> formatter = FormatterFactory.createFormatter(tier.getDeclaredType());
		if(formatter != null) {
			try {
				final T validatedObj = formatter.parse(text);
				setValidatedObject(validatedObj);
			} catch (ParseException e) {
				addErrorHighlight(e.getErrorOffset(), e.getErrorOffset()+1);
				retVal = false;
			}
		}
		getGroupFieldBorder().setShowWarningIcon(!retVal);
		return retVal;
	}

	protected void update() {
		final T validatedObj = getValidatedObject();
		if(validatedObj != null) {
			for(TierEditorListener listener:getTierEditorListeners()) {
				listener.tierValueChanged(getTier(),  validatedObj, initialGroupVal, true);
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
			undoManager.discardAllEdits();
			if(validateText()) {
				if(hasChanges) {
					update();
					for(TierEditorListener listener:getTierEditorListeners()) {
						listener.tierValueChanged(getTier(), getValidatedObject(), initialGroupVal, false);
					}
				}
			} else {
				Toolkit.getDefaultToolkit().beep();
				requestFocus();
			}
			repaint();
		}

		@Override
		public void focusGained(FocusEvent e) {
			initialGroupVal = getGroupValue();
			validatedObjRef.set(initialGroupVal);
			hasChanges = false;
			repaint();
		}

	};

	private final DocumentListener docListener = new DocumentListener() {

		@Override
		public void removeUpdate(DocumentEvent e) {
			validateText();
			hasChanges = true;
		}

		@Override
		public void insertUpdate(DocumentEvent e) {
			validateText();
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

	private final List<TierEditorListener<T>> listeners =
			Collections.synchronizedList(new ArrayList<>());

	@Override
	public void addTierEditorListener(TierEditorListener<T> listener) {
		if(!listeners.contains(listener))
			listeners.add(listener);
	}

	@Override
	public void removeTierEditorListener(TierEditorListener<T> listener) {
		listeners.remove(listener);
	}

	@Override
	public List<TierEditorListener<T>> getTierEditorListeners() {
		return listeners;
	}

	private final TierListener<T> tierListener = new TierListener<T>() {

		@Override
		public void tierValueChanged(Tier<T> tier, T oldValue, T newValue) {
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

				// XXX if text length is 0, check to see if there's an
				// UnvalidatedValue assigned to this object
				if(val instanceof IExtendable) {
					final IExtendable extVal = (IExtendable)val;
					final UnvalidatedValue unvalidatedValue = extVal.getExtension(UnvalidatedValue.class);
					if(unvalidatedValue != null) {
						text = unvalidatedValue.getValue();
					}
				}
			}

			setText(text);
		}

	};

	private final Highlighter.HighlightPainter errPainter = new Highlighter.HighlightPainter() {

		@Override
		public void paint(Graphics g, int p0, int p1, Shape bounds, JTextComponent c) {
			final Graphics2D g2 = (Graphics2D)g;

			Rectangle b = bounds.getBounds();
			try {
				final Rectangle p0rect = c.modelToView(p0);
				final Rectangle p1rect = c.modelToView(p1);

				b = new Rectangle(p0rect).union(p1rect);
			} catch (BadLocationException e) {

			}

			g2.setColor(Color.red);
			final float dash1[] = {1.0f};
		    final BasicStroke dashed =
		        new BasicStroke(1.0f,
		                        BasicStroke.CAP_BUTT,
		                        BasicStroke.JOIN_MITER,
		                        1.0f, dash1, 0.0f);
			g2.setStroke(dashed);
			g2.drawLine(b.x,
					b.y + b.height - 1,
					b.x + b.width,
					b.y + b.height - 1);
		}
	};
}
