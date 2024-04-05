package ca.phon.app.session.editor.view.transcript;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
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

    public class TranscriptDocumentNode extends DefaultMutableTreeNode {
        public TranscriptDocumentNode(Element documentEle) {
            super(documentEle);

            final AttributeSet attrs = documentEle.getAttributes();
            final var attrsenum = attrs.getAttributeNames();
            while(attrsenum.hasMoreElements()) {
                final Object key = attrsenum.nextElement();
                final Object value = attrs.getAttribute(key);
                add(new DefaultMutableTreeNode(key + ": " + value));
            }
        }

        public Element getDocumentElement() {
            return (Element) getUserObject();
        }

        @Override
        public String toString() {
            final Element ele = getDocumentElement();
            final String name = ele.getName();
            final int start = ele.getStartOffset();
            final int end = ele.getEndOffset();
            String text = "";
            if("paragraph".equals(name) || ele.isLeaf()) {
                try {
                    text = document.getText(start, end - start);
                } catch (BadLocationException e) {
                }
            }
            return name + " [" + start + ", " + end + "] " + text;
        }

    }


}
