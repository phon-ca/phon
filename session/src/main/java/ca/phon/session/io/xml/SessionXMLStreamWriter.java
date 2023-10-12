package ca.phon.session.io.xml;

import ca.phon.xml.DelegatingXMLStreamWriter;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.util.List;
import java.util.Stack;

/**
 * XML stream writer which indents based on a flag.  Indentation is not performed for elements //u/w, //u/g, //u/pg
 *
 * Much of this class has been copied from IndentingXMLStreamWriter
 */
public class SessionXMLStreamWriter extends DelegatingXMLStreamWriter {

    private static final Object SEEN_NOTHING = new Object();
    private static final Object SEEN_ELEMENT = new Object();
    private static final Object SEEN_DATA = new Object();
    private Object state;
    private Stack<Object> stateStack;
    private String indentStep;
    private int depth;

    private Stack<String> elementStack = new Stack<>();

    private boolean formattedOutput = true;
    private boolean wasFormattedOutput = true;
    private String resumeFormattingEleName = null;
    private int resumeFormattingStackSize = -1;

    private final List<String> unformattedElements = List.of("w", "g", "pg", "pause", "pho", "mod", "mor", "tierData");

    public SessionXMLStreamWriter(XMLStreamWriter writer) {
        this(writer, true);
    }

    public SessionXMLStreamWriter(XMLStreamWriter writer, boolean formattedOutput) {
        super(writer);
        this.state = SEEN_NOTHING;
        this.stateStack = new Stack();
        this.indentStep = "  ";
        this.depth = 0;
        this.formattedOutput = formattedOutput;
        this.wasFormattedOutput = formattedOutput;
    }

    public boolean isFormattedOutput() {
        return this.formattedOutput;
    }

    public void setFormattedOutput(boolean formattedOutput) {
        this.formattedOutput = formattedOutput;
    }

    /** @deprecated */
    @Deprecated
    public int getIndentStep() {
        return this.indentStep.length();
    }

    /** @deprecated */
    @Deprecated
    public void setIndentStep(int indentStep) {
        StringBuilder s;
        for(s = new StringBuilder(); indentStep > 0; --indentStep) {
            s.append(' ');
        }

        this.setIndentStep(s.toString());
    }

    public void setIndentStep(String s) {
        this.indentStep = s;
    }

    private void onStartElement() throws XMLStreamException {
        this.stateStack.push(SEEN_ELEMENT);
        this.state = SEEN_NOTHING;
        if (this.depth > 0) {
            if(isFormattedOutput())
                super.writeCharacters("\n");
        }

        this.doIndent();
        ++this.depth;
    }

    private void onEndElement() throws XMLStreamException {
        --this.depth;
        if (this.state == SEEN_ELEMENT) {
            if(isFormattedOutput())
                super.writeCharacters("\n");
            this.doIndent();
        }

        this.state = this.stateStack.pop();
    }

    private void onEmptyElement() throws XMLStreamException {
        this.state = SEEN_ELEMENT;
        if (this.depth > 0) {
            if(isFormattedOutput())
                super.writeCharacters("\n");
        }

        this.doIndent();
    }

    private void doIndent() throws XMLStreamException {
        if(!this.isFormattedOutput()) return;
        if (this.depth > 0) {
            for(int i = 0; i < this.depth; ++i) {
                super.writeCharacters(this.indentStep);
            }
        }

    }

    public void writeStartDocument() throws XMLStreamException {
        super.writeStartDocument();
        if(isFormattedOutput())
            super.writeCharacters("\n");
    }

    public void writeStartDocument(String version) throws XMLStreamException {
        super.writeStartDocument(version);
        if(isFormattedOutput())
            super.writeCharacters("\n");
    }

    public void writeStartDocument(String encoding, String version) throws XMLStreamException {
        super.writeStartDocument(encoding, version);
        if(isFormattedOutput())
            super.writeCharacters("\n");
    }

    public void writeStartElement(String localName) throws XMLStreamException {
        this.onStartElement();
        if(isFormattedOutput() && unformattedElements.contains(localName)) {
            wasFormattedOutput = this.formattedOutput;
            setFormattedOutput(false);
            resumeFormattingEleName = localName;
            resumeFormattingStackSize = elementStack.size();
        }
        elementStack.push(localName);
        super.writeStartElement(localName);
    }

    public void writeStartElement(String namespaceURI, String localName) throws XMLStreamException {
        this.onStartElement();
        if(isFormattedOutput() && unformattedElements.contains(localName)) {
            wasFormattedOutput = this.formattedOutput;
            setFormattedOutput(false);
            resumeFormattingEleName = localName;
            resumeFormattingStackSize = elementStack.size();
        }
        elementStack.push(localName);
        super.writeStartElement(namespaceURI, localName);
    }

    public void writeStartElement(String prefix, String localName, String namespaceURI) throws XMLStreamException {
        this.onStartElement();
        if(isFormattedOutput() && unformattedElements.contains(localName)) {
            wasFormattedOutput = this.formattedOutput;
            setFormattedOutput(false);
            resumeFormattingEleName = localName;
            resumeFormattingStackSize = elementStack.size();
        }
        elementStack.push(localName);
        super.writeStartElement(prefix, localName, namespaceURI);
        if("session".equals(localName)) {
            writeDefaultNamespace("https://phon.ca/ns/session");
        }
    }

    public void writeEmptyElement(String namespaceURI, String localName) throws XMLStreamException {
        this.onEmptyElement();
        super.writeEmptyElement(namespaceURI, localName);
    }

    public void writeEmptyElement(String prefix, String localName, String namespaceURI) throws XMLStreamException {
        this.onEmptyElement();
        super.writeEmptyElement(prefix, localName, namespaceURI);
    }

    public void writeEmptyElement(String localName) throws XMLStreamException {
        this.onEmptyElement();
        super.writeEmptyElement(localName);
    }

    public void writeEndElement() throws XMLStreamException {
        this.onEndElement();
        final String eleName = elementStack.pop();
        if(resumeFormattingEleName != null && elementStack.size() == resumeFormattingStackSize && resumeFormattingEleName.equals(eleName)) {
            setFormattedOutput(wasFormattedOutput);
            resumeFormattingEleName = null;
            resumeFormattingStackSize = -1;
        }
        super.writeEndElement();
    }

    public void writeCharacters(String text) throws XMLStreamException {
        this.state = SEEN_DATA;
        super.writeCharacters(text);
    }

    public void writeCharacters(char[] text, int start, int len) throws XMLStreamException {
        this.state = SEEN_DATA;
        super.writeCharacters(text, start, len);
    }

    public void writeCData(String data) throws XMLStreamException {
        this.state = SEEN_DATA;
        super.writeCData(data);
    }

}
