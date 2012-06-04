package ca.phon.plugins.opgraph.nodes.phonex;

import java.awt.BorderLayout;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.Properties;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import ca.gedge.opgraph.app.GraphDocument;
import ca.gedge.opgraph.app.GraphEditorModel;
import ca.gedge.opgraph.app.edits.other.NodeSettingsEdit;
import ca.phon.ipa.phone.phonex.PhonexPatternException;

/**
 * Phonex node settings panel.
 */
public class PhonexSettingsPanel extends JPanel {
	// reference to parent node
	private PhonexNode phonexNode;
	
	public PhonexSettingsPanel(PhonexNode node) {
		super(new BorderLayout());
		phonexNode = node;
		
		init();
	}
	
	private JTextPane phonexEditor;
	public JTextPane getPhonexEditor() {
		if(phonexEditor == null) {
			phonexEditor = new JTextPane();
			phonexEditor.addFocusListener(new FocusListener() {
				@Override
				public void focusLost(FocusEvent e) {
					// Post an undoable edit
					final GraphDocument document = GraphEditorModel.getActiveDocument();
					String phonex = phonexEditor.getText();
					if(document != null) {
						final Properties settings = new Properties();
						settings.put(phonexNode.getClass().getName() + ".phonex", phonex);
						document.getUndoSupport().postEdit(new NodeSettingsEdit(phonexNode, settings));
					} else {
						phonexNode.setPhonex(phonex);
					}
				}
				
				@Override
				public void focusGained(FocusEvent e) {}
			});
		}
		return this.phonexEditor;
	}
	
	private final JScrollPane phonexScroller = new JScrollPane(getPhonexEditor());
	private JScrollPane getPhonexScroller() {
		return this.phonexScroller;
	}
	
	private void init() {
		final JLabel lbl = new JLabel("Enter phonex:");
		add(lbl, BorderLayout.NORTH);
		add(getPhonexScroller(), BorderLayout.CENTER);
		
		getPhonexEditor().setText(phonexNode.getPhonex());
	}
}