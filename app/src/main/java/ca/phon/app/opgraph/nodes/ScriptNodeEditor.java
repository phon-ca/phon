package ca.phon.app.opgraph.nodes;

import ca.phon.app.log.LogUtil;
import ca.phon.app.query.ScriptEditorFactory;
import ca.phon.script.*;
import ca.phon.ui.action.*;
import ca.phon.ui.fonts.FontPreferences;
import ca.phon.ui.layout.ButtonBarBuilder;
import org.fife.ui.rtextarea.*;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.io.*;

/**
 * Script editor dialog for {@link ScriptNode}s
 *
 */
public class ScriptNodeEditor extends JPanel {

	private ScriptNode scriptNode;

	private volatile boolean hasChanges = false;

	private RTextArea editor;

	private JTextArea errorArea;

	public ScriptNodeEditor() {
		super();

		init();
	}

	private void init() {
		setLayout(new BorderLayout());


		editor = ScriptEditorFactory.createEditorForScript(new BasicScript(""), false);
		final RTextScrollPane scrollPane = new RTextScrollPane(editor);
		editor.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				setHasChanges(true);
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				setHasChanges(true);
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				setHasChanges(true);
			}
		});

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

		setHasChanges(false);

		addPropertyChangeListener("scriptNode", (e) -> {
			if(getScriptNode() == null) {
				editor.setText("");
			} else {
				editor.setText(scriptNode.getScript().getScript());
				editor.setCaretPosition(0);
			}
			editor.discardAllEdits();

			errorArea.setText("");
			setHasChanges(false);
		});
	}

	public boolean hasChanges() {
		return this.hasChanges;
	}

	public void setHasChanges(boolean hasChanges) {
		var oldVal = this.hasChanges;
		this.hasChanges = hasChanges;
		firePropertyChange("hasChanges", oldVal, hasChanges);
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

	public void updateScript() {
		scriptNode.getScript().setLength(0);
		scriptNode.getScript().insert(0, editor.getText());
		setHasChanges(false);

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

}
