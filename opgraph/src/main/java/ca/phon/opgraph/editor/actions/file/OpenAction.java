package ca.phon.opgraph.editor.actions.file;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import ca.gedge.opgraph.OpGraph;
import ca.phon.opgraph.OpgraphIO;
import ca.phon.opgraph.editor.OpgraphEditor;
import ca.phon.opgraph.editor.OpgraphEditorModel;
import ca.phon.opgraph.editor.OpgraphEditorModelFactory;
import ca.phon.opgraph.editor.OpgraphFileFilter;
import ca.phon.opgraph.editor.actions.OpgraphEditorAction;
import ca.phon.ui.nativedialogs.FileFilter;
import ca.phon.ui.nativedialogs.MessageDialogProperties;
import ca.phon.ui.nativedialogs.NativeDialogEvent;
import ca.phon.ui.nativedialogs.NativeDialogListener;
import ca.phon.ui.nativedialogs.NativeDialogs;
import ca.phon.ui.nativedialogs.OpenDialogProperties;

public class OpenAction extends OpgraphEditorAction {
	
	private final static Logger LOGGER = Logger.getLogger(OpenAction.class.getName());

	private static final long serialVersionUID = 1416397464535529114L;
	
	private final static String TXT = "Open...";
	
	private final static String DESC = "Open graph";

	public OpenAction(OpgraphEditor editor) {
		super(editor);
		
		putValue(NAME, TXT);
		putValue(SHORT_DESCRIPTION, DESC);
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
		final OpenDialogProperties props = new OpenDialogProperties();
		props.setParentWindow(getEditor());
		props.setCanChooseFiles(true);
		props.setCanChooseDirectories(false);
		props.setAllowMultipleSelection(false);
		props.setTitle("Open Graph");
		props.setRunAsync(false);
		props.setFileFilter(new OpgraphFileFilter());
		
		final List<String> savePath = NativeDialogs.showOpenDialog(props);
		if(savePath.size() > 0) {
			try {
				final File saveFile = new File(savePath.get(0));
				final OpGraph graph = OpgraphIO.read(saveFile);
				final OpgraphEditorModelFactory factory = new OpgraphEditorModelFactory();
				final OpgraphEditorModel model = factory.fromGraph(graph);
				getEditor().setModel(model);
				getEditor().setCurrentFile(saveFile);
			} catch (IOException | ClassNotFoundException e) {
				LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
			}
		}
	}

}
