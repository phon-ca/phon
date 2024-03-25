package ca.phon.app.session.editor.view.transcript;

import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Element;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;

public class TranscriptDocumentTreeModel extends DefaultTreeModel {

    private final TranscriptDocument document;

    public TranscriptDocumentTreeModel(TranscriptDocument document) {
        super(new TranscriptDocumentRootNode(document));
        this.document = document;

        buildTree();
    }

    private void buildTree() {
        final TranscriptDocumentRootNode rootNode = (TranscriptDocumentRootNode)getRoot();
        for(Element documentEle:document.getRootElements()) {
            final TranscriptDocumentNode documentNode = new TranscriptDocumentNode(documentEle);
            rootNode.add(documentNode);
            buildTree(documentNode, documentEle);
        }
    }

    private void buildTree(TranscriptDocumentNode parent, Element ele) {
        for(int i = 0; i < ele.getElementCount(); i++) {
            final Element childEle = ele.getElement(i);
            final TranscriptDocumentNode childNode = new TranscriptDocumentNode(childEle);
            parent.add(childNode);
            if(childEle.isLeaf()) continue;
            buildTree(childNode, childEle);
        }
    }

    public static class TranscriptDocumentRootNode extends DefaultMutableTreeNode {
        public TranscriptDocumentRootNode(TranscriptDocument document) {
            super(document);
        }

        public TranscriptDocument getDocument() {
            return (TranscriptDocument)getUserObject();
        }
    }

    public static class TranscriptDocumentNode extends DefaultMutableTreeNode {
        public TranscriptDocumentNode(Element documentEle) {
            super(documentEle);
        }

        public Element getDocumentElement() {
            return (Element) getUserObject();
        }
    }


}
