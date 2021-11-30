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

/**
 * Script editor dialog for {@link ScriptNode}s
 *
 */
public class ScriptNodeEditor extends JPanel {

	private ScriptNode scriptNode;

	private volatile boolean hasChanges = false;

	private JLabel currentNodeLabel;

	private RTextArea editor;

	private JTextArea errorArea;

	private JButton updateButton;

	public ScriptNodeEditor() {
		super();

		init();
	}

	private void init() {
		setLayout(new BorderLayout());

		currentNodeLabel = new JLabel();

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

		final PhonUIAction updateAct = new PhonUIAction(this, "updateScript");
		updateAct.putValue(PhonUIAction.NAME, "Update");
		updateAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Update script parameters, inputs and outputs");
		updateButton = new JButton(updateAct);

		final JComponent buttonBar = ButtonBarBuilder.buildOkCancelBar(updateButton, currentNodeLabel);

		JPanel bottomPanel = new JPanel(new BorderLayout());
		bottomPanel.add(errorScroller, BorderLayout.CENTER);
		bottomPanel.add(buttonBar, BorderLayout.SOUTH);

		add(scrollPane, BorderLayout.CENTER);
		add(bottomPanel, BorderLayout.SOUTH);

		addPropertyChangeListener("hasChanges", (e) -> {
			if(this.updateButton != null)
				this.updateButton.setEnabled(hasChanges && (scriptNode != null));
		});
		setHasChanges(false);

		addPropertyChangeListener("scriptNode", (e) -> {
			if(getScriptNode() == null) {
				editor.setText("");
				currentNodeLabel.setText("<html><i>No node selected</i></html>");
			} else {
				editor.setText(scriptNode.getScript().getScript());
				editor.setCaretPosition(0);
				currentNodeLabel.setText("<html>Current node: " + scriptNode.toOpNode().getName() + " (" + scriptNode.toOpNode().getId() + ")");
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
			scriptNode.getScriptPanel().updateParams();
			scriptNode.reloadFields();
		} catch (PhonScriptException e) {
			LogUtil.warning(e);
			errorArea.setText(e.getLocalizedMessage());
		}
		scriptNode.getScriptPanel().repaint();
	}

}