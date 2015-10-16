package ca.phon.opgraph.editor.actions.file;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import ca.phon.opgraph.editor.NewDialogPanel;
import ca.phon.opgraph.editor.NewGraphDialog;
import ca.phon.opgraph.editor.OpgraphEditor;
import ca.phon.opgraph.editor.OpgraphEditorModel;
import ca.phon.opgraph.editor.actions.OpgraphEditorAction;
import ca.phon.opgraph.editor.actions.graph.AutoLayoutAction;
import ca.phon.ui.nativedialogs.MessageDialogProperties;
import ca.phon.ui.nativedialogs.NativeDialogs;

/**
 * Show new graph dialog for the node editor
 */
public class NewAction extends OpgraphEditorAction {

	private static final long serialVersionUID = -6534242210546640918L;
	
	public final static String TXT = "New...";
	
	public final static String DESC = "New graph";
	
	public final static KeyStroke KS = KeyStroke.getKeyStroke(KeyEvent.VK_N,
			Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
	
	public NewAction(OpgraphEditor editor) {
		super(editor);
		putValue(NAME, TXT);
		putValue(SHORT_DESCRIPTION, DESC);
		putValue(ACCELERATOR_KEY, KS);
	}

	@Override
	public void hookableActionPerformed(ActionEvent arg0) {
		if(getEditor().hasUnsavedChanges()) {
			final MessageDialogProperties props = new MessageDialogProperties();
			props.setParentWindow(getEditor());
			props.setOptions(MessageDialogProperties.yesNoCancelOptions);
			props.setTitle("Save changes");
			props.setHeader(props.getTitle());
			props.setMessage("Save changes before starting a new graph?");
			props.setRunAsync(false);
			
			final int ret = NativeDialogs.showMessageDialog(props);
			if(ret == 0) /* yes */ {
				(new SaveAction(getEditor())).actionPerformed(arg0);
			} else if(ret == 1) /* no */ {
				// do nothing
			} else /* cancel */ {
				return;
			}
		}
		
		final NewGraphDialog newDlg = new NewGraphDialog(getEditor());
		newDlg.pack();
		newDlg.setResizable(false);
		newDlg.setLocationRelativeTo(getEditor());
		newDlg.setVisible(true);
		if(!newDlg.wasCanceled()) {
			final NewDialogPanel selectedPanel = newDlg.getSelectedPanel();
			if(selectedPanel != null) {
				final OpgraphEditorModel model = selectedPanel.createModel();
				getEditor().setModel(model);
				
				SwingUtilities.invokeLater(() -> (new AutoLayoutAction(getEditor())).actionPerformed(arg0));
			}
		}
	}

}
