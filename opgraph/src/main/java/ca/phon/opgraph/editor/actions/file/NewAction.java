package ca.phon.opgraph.editor.actions.file;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.List;

import javax.swing.KeyStroke;

import ca.phon.opgraph.editor.DefaultOpgraphEditorModel;
import ca.phon.opgraph.editor.OpgraphEditor;
import ca.phon.opgraph.editor.OpgraphEditorModel;
import ca.phon.opgraph.editor.OpgraphEditorModelFactory;
import ca.phon.opgraph.editor.actions.OpgraphEditorAction;
import ca.phon.ui.nativedialogs.MessageDialogProperties;
import ca.phon.ui.nativedialogs.NativeDialogs;

/**
 * 
 */
public class NewAction extends OpgraphEditorAction {

	private static final long serialVersionUID = -6534242210546640918L;
	
	public final static String TXT = "New...";
	
	public final static String DESC = "New graph";
	
	public final static KeyStroke KS = KeyStroke.getKeyStroke(KeyEvent.VK_N,
			Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
	
	private final Class<? extends OpgraphEditorModel> modelType;
	
	public NewAction(OpgraphEditor editor) {
		this(editor, DefaultOpgraphEditorModel.class);
	}
	
	public NewAction(OpgraphEditor editor, Class<? extends OpgraphEditorModel> modelType) {
		super(editor);
		this.modelType = modelType;
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
		final OpgraphEditorModelFactory factory = new OpgraphEditorModelFactory();
		final OpgraphEditorModel model = factory.fromType(modelType);
		getEditor().setModel(model);
	}

}
