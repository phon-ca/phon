package ca.phon.app.opgraph.nodes;

import ca.phon.app.log.LogUtil;
import ca.phon.app.script.*;
import ca.phon.script.*;
import ca.phon.ui.action.*;
import ca.phon.ui.fonts.FontPreferences;
import org.fife.ui.rsyntaxtextarea.RSyntaxDocument;
import org.fife.ui.rtextarea.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.BadLocationException;
import javax.swing.undo.*;
import java.awt.*;
import java.io.*;

/**
 * Script editor dialog for {@link ScriptNode}s
 *
 */
public class ScriptNodeEditor extends JPanel {

	private ScriptNode scriptNode;

	private PhonScriptTextArea editor;

	private JTextArea errorArea;

	public ScriptNodeEditor() {
		super();

		init();
	}

	private void init() {
		setLayout(new BorderLayout());

		final PhonUIAction undoAct = new PhonUIAction(this, "undo");
		final PhonUIAction redoAct = new PhonUIAction(this, "redo");

		editor = ScriptEditorFactory.createEditorForScript(new BasicScript(""), false);
		final RTextScrollPane scrollPane = new RTextScrollPane(editor);
		editor.getActionMap().put("RTA.UndoAction", undoAct);
		editor.getActionMap().put("RTA.RedoAction", redoAct);

		errorArea = new JTextArea();
		errorArea.setEditable(false);
		errorArea.setFont(FontPreferences.getMonospaceFont());
		errorArea.setForeground(Color.red);
		errorArea.setRows(3);

		JScrollPane errorScroller = new JScrollPane(errorArea);

		JSplitPane splitPane = new JSplitPane(SwingConstants.HORIZONTAL);
		splitPane.setLeftComponent(scrollPane);
		splitPane.setRightComponent(errorScroller);
		splitPane.setDividerLocation(0.9);
		splitPane.setResizeWeight(0.9f);

		add(splitPane, BorderLayout.CENTER);

		addPropertyChangeListener("scriptNode", (e) -> {
			ScriptNode scriptNode = getScriptNode();
			if(getScriptNode() == null) {
				editor.setDocument(new RSyntaxDocument("text/javascript"));
			} else {
				ScriptNodeEditorExtension ext = scriptNode.toOpNode().getExtension(ScriptNodeEditorExtension.class);
				if(ext == null) {
					ext = new ScriptNodeEditorExtension();
					ext.hasChanges = false;
					ext.undoManager = new UndoManager();
					ext.document = new RSyntaxDocument("text/javascript");
					try {
						ext.document.insertString(0, scriptNode.getScript().getScript(), ext.document.getDefaultRootElement().getAttributes());
					} catch (BadLocationException ex) {
						LogUtil.severe(ex);
						Toolkit.getDefaultToolkit().beep();
					}
					scriptNode.toOpNode().putExtension(ScriptNodeEditorExtension.class, ext);
				}

				editor.getDocument().removeUndoableEditListener(undoableEditListener);
				ext.document.addUndoableEditListener(undoableEditListener);

				// keep undo history between context changes
				editor.setAllowDiscardEdits(false);
				editor.setDocument(ext.document);
				editor.setAllowDiscardEdits(true);

				editor.setCaretPosition(0);
			}

			errorArea.setText("");
		});
	}

	public boolean canUndo() {
		return canUndo(getScriptNode());
	}

	public boolean canUndo(ScriptNode scriptNode) {
		if(scriptNode == null) return false;
		ScriptNodeEditorExtension ext = scriptNode.toOpNode().getExtension(ScriptNodeEditorExtension.class);
		if(ext == null) return false;
		return ext.undoManager.canUndo();
	}

	public void undo() {
		undo(getScriptNode());
	}

	public void undo(ScriptNode scriptNode) {
		if(scriptNode == null) return;

		ScriptNodeEditorExtension ext = scriptNode.toOpNode().getExtension(ScriptNodeEditorExtension.class);
		if(ext == null) return;

		if(ext.undoManager.canUndo())
			ext.undoManager.undo();
	}

	public boolean canRedo() {
		return canRedo(getScriptNode());
	}

	public boolean canRedo(ScriptNode scriptNode) {
		if(scriptNode == null) return false;
		ScriptNodeEditorExtension ext = scriptNode.toOpNode().getExtension(ScriptNodeEditorExtension.class);
		if(ext == null) return false;
		return ext.undoManager.canRedo();
	}

	public void redo() {
		redo(getScriptNode());
	}

	public void redo(ScriptNode scriptNode) {
		if(scriptNode == null) return;
		ScriptNodeEditorExtension ext = scriptNode.toOpNode().getExtension(ScriptNodeEditorExtension.class);
		if(ext == null) return;

		if(ext.undoManager.canRedo())
			ext.undoManager.redo();
	}

	/**
	 * Does current script node have changes
	 *
	 * @return
	 */
	public boolean hasChanges() {
		return (this.scriptNode == null ? false : hasChanges(this.scriptNode));
	}

	/**
	 * Does the given script node have unsaved editor changes?
	 *
	 * @param scriptNode
	 * @return
	 */
	public boolean hasChanges(ScriptNode scriptNode) {
		boolean retVal = false;
		if(scriptNode != null) {
			ScriptNodeEditorExtension ext = scriptNode.toOpNode().getExtension(ScriptNodeEditorExtension.class);
			if(ext != null) {
				retVal = ext.hasChanges;
			}
		}
		return retVal;
	}

	public void setHasChanges(boolean hasChanges) {
		this.setHasChanges(this.scriptNode, hasChanges);
	}

	public void setHasChanges(ScriptNode scriptNode, boolean hasChanges) {
		var oldVal = hasChanges(scriptNode);

		if(scriptNode != null) {
			ScriptNodeEditorExtension ext = scriptNode.toOpNode().getExtension(ScriptNodeEditorExtension.class);
			if(ext != null) {
				ext.hasChanges = hasChanges;
				firePropertyChange("hasChanges", oldVal, hasChanges);
			}
		}
	}

	public ScriptNode getScriptNode() {
		return this.scriptNode;
	}

	public void setScriptNode(ScriptNode scriptNode) {
		var oldVal = this.scriptNode;
		this.scriptNode = scriptNode;
		firePropertyChange("scriptNode", oldVal, scriptNode);
	}

	public String getText() {
		return editor.getText();
	}

	public void setText(String text) {
		editor.setText(text);
	}

	/**
	 * Reset edtior contents with node data
	 *
	 *
	 */
	public void resetScript() {
		ScriptNode scriptNode = getScriptNode();
		if(scriptNode == null) return;

		ScriptNodeEditorExtension ext = scriptNode.toOpNode().getExtension(ScriptNodeEditorExtension.class);
		if(ext == null) return;


	}

	/**
	 * Update node script with editor contents
	 *
	 */
	public void updateScript() {
		scriptNode.getScript().setLength(0);
		scriptNode.getScript().insert(0, editor.getText());

		errorArea.setText("");
		try {
			if(scriptNode.getScriptPanel() != null) {
				scriptNode.getScriptPanel().updateParams();
				scriptNode.getScriptPanel().repaint();
			}
			scriptNode.reloadFields();
		} catch (PhonScriptException e) {
			LogUtil.warning(e);
			StringWriter stringWriter = new StringWriter();
			PrintWriter writer = new PrintWriter(stringWriter);
			writer.println(e.getLocalizedMessage());
			e.printStackTrace(writer);
			errorArea.setText(stringWriter.getBuffer().toString());
			errorArea.setCaretPosition(0);
		}
	}

	private static class ScriptNodeEditorExtension {
		UndoManager undoManager;
		RSyntaxDocument document;
		boolean hasChanges;
	}

	private final UndoableEditListener undoableEditListener = new UndoableEditListener() {
		@Override
		public void undoableEditHappened(UndoableEditEvent e) {
			ScriptNode scriptNode = getScriptNode();
			if(scriptNode == null) return;

			ScriptNodeEditorExtension ext = scriptNode.toOpNode().getExtension(ScriptNodeEditorExtension.class);
			if(ext == null) return;

			ext.undoManager.addEdit(e.getEdit());
			ext.hasChanges = true;
		}
	};

}
