package ca.phon.app.session.editor.view.transcript.actions;

import ca.phon.app.session.editor.SessionEditor;
import ca.phon.app.session.editor.view.transcript.TranscriptDocumentTreeModel;
import ca.phon.app.session.editor.view.transcript.TranscriptView;
import org.jdesktop.swingx.JXTree;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class ShowTranscriptTreeAction extends TranscriptViewAction {

    public ShowTranscriptTreeAction(SessionEditor editor, TranscriptView view) {
        super(editor, view);
        putValue(NAME, "Show transcript tree");
    }

    @Override
    public void hookableActionPerformed(ActionEvent e) {
        final TranscriptDocumentTreeModel treeModel = new TranscriptDocumentTreeModel(getView().getTranscriptEditor().getTranscriptDocument());
        final JXTree tree = new JXTree(treeModel);
        tree.setRootVisible(true);

        // show tree in dialog
        final JFrame f = new JFrame("Transcript tree");
        f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        f.add(new JScrollPane(tree));
        f.pack();
        f.setSize(400, 400);
        f.setVisible(true);
    }

}
