package ca.phon.app.session.editor.view.transcript;

import ca.phon.session.Comment;
import ca.phon.session.Gem;
import ca.phon.session.Record;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;

public class TranscriptDocumentTreeModel extends DefaultTreeModel {

    private final TranscriptDocument document;

    private int currentRecordIndex = -1;
    private MutableTreeNode currentTranscriptEleNode;

    public TranscriptDocumentTreeModel(TranscriptDocument document) {
        super(new TranscriptDocumentRootNode(document));
        this.document = document;

        buildTree();
    }

    private void buildTree() {
        final TranscriptDocumentRootNode rootNode = (TranscriptDocumentRootNode)getRoot();
        final TranscriptDocumentNode documentNode = new TranscriptDocumentNode(document.getRootElements()[0]);
        rootNode.add(documentNode);
        buildTree(documentNode, document.getRootElements()[0]);
    }

    private void buildTree(TranscriptDocumentNode parent, Element ele) {
        for(int i = 0; i < ele.getElementCount(); i++) {
            final Element childEle = ele.getElement(i);
            final String eleName = childEle.getName();
            if(childEle.getElementCount() > 0) {
                final AttributeSet paragraphAttrs = childEle.getElement(0).getAttributes();
                final String elementType = TranscriptStyleConstants.getElementType(paragraphAttrs);
                if (TranscriptStyleConstants.ELEMENT_TYPE_RECORD.equals(elementType) ||
                        TranscriptStyleConstants.ELEMENT_TYPE_BLIND_TRANSCRIPTION.equals(elementType)) {
                    final Record record = TranscriptStyleConstants.getRecord(paragraphAttrs);
                    int recordIndex = document.getSession().getRecordPosition(record);
                    if (recordIndex != currentRecordIndex) {
                        currentRecordIndex = recordIndex;
                        currentTranscriptEleNode = new DefaultMutableTreeNode("Record " + (recordIndex + 1));
                        parent.add(currentTranscriptEleNode);
                    }
                    final TranscriptDocumentNode childNode = new TranscriptDocumentNode(childEle);
                    currentTranscriptEleNode.insert(childNode, currentTranscriptEleNode.getChildCount());
                    if (childEle.isLeaf()) continue;
                    buildTree(childNode, childEle);
                } else {
                    final TranscriptDocumentNode childNode = new TranscriptDocumentNode(childEle);
                    parent.add(childNode);
                    if (childEle.isLeaf()) continue;
                    buildTree(childNode, childEle);
                }
            } else {
                final TranscriptDocumentNode childNode = new TranscriptDocumentNode(childEle);
                parent.add(childNode);
            }
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
                    if(text.equals("\n")) {
                        text = "<newline>";
                    } else if(text.equals("\t")) {
                        text = "<tab>";
                    } else if(text.length() == 0) {
                        text = "<empty>";
                    } else if("\u2029".equals(text)) {
                        text = "<new tier>";
                    } else if(text.startsWith("\u2029")) {
                        text = text.substring(1).trim();
                    } else {
                        text = text.trim();
                    }
                } catch (BadLocationException e) {
                }
            } else if("section".equals(name)) {
                text = "session";
            }
            if(text.length() > 40) {
                text = text.substring(0, 40) + "...";
            }
            return "[" + start + ", " + end + "] " + text;
        }
    }
}
