package ca.phon.app.opgraph.editor.actions.file;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.KeyStroke;

import ca.gedge.opgraph.OpGraph;
import ca.phon.app.opgraph.editor.OpgraphEditor;
import ca.phon.app.opgraph.editor.OpgraphEditorModel;
import ca.phon.app.opgraph.editor.OpgraphEditorModelFactory;
import ca.phon.app.opgraph.editor.OpgraphFileFilter;
import ca.phon.app.opgraph.editor.actions.OpgraphEditorAction;
import ca.phon.opgraph.OpgraphIO;
import ca.phon.ui.nativedialogs.MessageDialogProperties;
import ca.phon.ui.nativedialogs.NativeDialogs;
import ca.phon.ui.nativedialogs.OpenDialogProperties;

public class OpenAction extends OpgraphEditorAction {
	
	private final static Logger LOGGER = Logger.getLogger(OpenAction.class.getName());

	private static final long serialVersionUID = 1416397464535529114L;
	
	public final static String TXT = "Open...";
	
	public final static String DESC = "Open graph";
	
	public final static KeyStroke KS = KeyStroke.getKeyStroke(KeyEvent.VK_O,
			Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());

	public OpenAction(OpgraphEditor editor) {
		super(editor);
		
		putValue(NAME, TXT);
		putValue(SHORT_DESCRIPTION, DESC);
		putValue(ACCELERATOR_KEY, KS);
	}

	@Override
	public void hookableActionPerformed(ActionEvent arg0) {
		boolean useCurrentWindow = true;
		if(getEditor().getCurrentFile() != null
				|| getEditor().hasUnsavedChanges()) {
			// ask to use new window
			final String opts[] = new String[] { "Use current window", "Use new window" };
			final MessageDialogProperties props = new MessageDialogProperties();
			props.setParentWindow(getEditor());
			props.setOptions(opts);
			props.setTitle("Choose window");
			props.setHeader(props.getTitle());
			props.setMessage("Use current window for new document?");
			props.setRunAsync(false);
			
			final int ret = NativeDialogs.showMessageDialog(props);
			useCurrentWindow = (ret == 0);
		}
		
		if(useCurrentWindow && getEditor().hasUnsavedChanges()) {
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
		final OpenDialogProperties props = new OpenDialogProperties();
		props.setParentWindow(getEditor());
		props.setCanChooseFiles(true);
		props.setCanChooseDirectories(false);
		props.setAllowMultipleSelection(false);
		props.setTitle("Open Graph");
		props.setRunAsync(false);
		props.setFileFilter(new OpgraphFileFilter());
		props.setInitialFolder(getEditor().getModel().getDefaultFolder());
		
		final List<String> savePath = NativeDialogs.showOpenDialog(props);
		if(savePath.size() > 0) {
			try {
				final File saveFile = new File(savePath.get(0));
				final OpGraph graph = OpgraphIO.read(saveFile);
				final OpgraphEditorModelFactory factory = new OpgraphEditorModelFactory();
				final OpgraphEditorModel model = factory.fromGraph(graph);
				
				if(useCurrentWindow) {
					getEditor().setModel(model);
					getEditor().setCurrentFile(saveFile);
				} else {
					final OpgraphEditor editor = new OpgraphEditor(model);
					editor.setCurrentFile(saveFile);
					editor.pack();
					editor.setSize(1064, 768);
					editor.setLocationByPlatform(true);
					editor.setVisible(true);
				}
				
				
			} catch (IOException | ClassNotFoundException e) {
				LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
			}
		}
	}

}
