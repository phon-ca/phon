package ca.phon.xml;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class EmptyTagXMLStreamWriter extends DelegatingXMLStreamWriter {

    class Event {
        Method m;
        Object[] args;
    }
    enum EventEnum {
        writeStartElement,
        writeAttribute,
        writeNamespace,
        writeEndElement,
        setPrefix,
        setDefaultNamespace,
    }
    private List<Event> queue = new ArrayList<>();

    public EmptyTagXMLStreamWriter(XMLStreamWriter out) {
        super(out);
    }

    @Override
    public void writeStartElement(String localName) throws XMLStreamException {
        d(e(m("writeStartElement",String.class)), localName);
    }
    @Override
    public void writeStartElement(String namespaceURI, String localName) throws XMLStreamException
    {
        d(e(m("writeStartElement",String.class,String.class)), namespaceURI, localName);
    }
    @Override
    public void writeStartElement(String prefix, String localName, String namespaceURI) throws XMLStreamException
    {
        d(e(m("writeStartElement",String.class,String.class,String.class)), prefix, localName, namespaceURI);
    }
    @Override
    public void writeAttribute(String localName, String value)
            throws XMLStreamException {
        d(e(m("writeAttribute",String.class, String.class)), localName, value);
    }
    @Override
    public void writeAttribute(String namespaceURI, String localName,
                               String value) throws XMLStreamException {
        d(e(m("writeAttribute",String.class, String.class, String.class)), namespaceURI, localName, value);
    }
    @Override
    public void writeAttribute(String prefix, String namespaceURI,
                               String localName, String value) throws XMLStreamException {
        d(e(m("writeAttribute",String.class, String.class, String.class, String.class)), prefix, namespaceURI, localName, value);
    }
    @Override
    public void writeCData(String data) throws XMLStreamException {
        fq();
        super.writeCData(data);
    }
    @Override
    public void writeCharacters(char[] text, int start, int len)
            throws XMLStreamException {
        fq();
        super.writeCharacters(text, start, len);
    }
    @Override
    public void writeCharacters(String text) throws XMLStreamException {
        fq();
        super.writeCharacters(text);
    }
    @Override
    public void writeComment(String data) throws XMLStreamException {
        fq();
        super.writeComment(data);
    }
    @Override
    public void writeDTD(String dtd) throws XMLStreamException {
        fq();
        super.writeDTD(dtd);
    }
    @Override
    public void writeProcessingInstruction(String target)
            throws XMLStreamException {
        fq();
        super.writeProcessingInstruction(target);
    }
    @Override
    public void writeProcessingInstruction(String target, String data)
            throws XMLStreamException {
        fq();
        super.writeProcessingInstruction(target, data);
    }
    @Override
    public void writeNamespace(String prefix, String namespaceURI)
            throws XMLStreamException {
        d(e(m("writeNamespace",String.class, String.class)), prefix, namespaceURI);
    }
    @Override
    public void writeEndElement() throws XMLStreamException {
        d(e(m("writeEndElement")));
    }
    @Override
    public void writeEndDocument() throws XMLStreamException {
        fq();
        super.writeEndDocument();
    }
    @Override
    public void writeDefaultNamespace(String namespaceURI)
            throws XMLStreamException {
        super.writeDefaultNamespace(namespaceURI);
    }
    @Override
    public void flush() throws XMLStreamException {
        if(queue.isEmpty())
            super.flush();
    }
    @Override
    public void close() throws XMLStreamException {
        fq();
        super.getDelegate().close();
    }
    @Override
    public void setPrefix(String prefix, String uri) throws XMLStreamException {
        d(e(m("setPrefix", String.class, String.class)), prefix, uri);
    }
    @Override
    public void setDefaultNamespace(String uri) throws XMLStreamException {
        d(e(m("setDefaultNamespace", String.class)), uri);
    }

    void d(Event e,Object...args) throws XMLStreamException {
        e.args = args;
        switch(EventEnum.valueOf(e.m.getName()))
        {
            case writeStartElement:
                fq();
                queue.add(e);
                break;
            case writeAttribute:
            case writeNamespace:
            case setPrefix:
            case setDefaultNamespace:
                if(!queue.isEmpty())
                    queue.add(e);
                else
                    ex(e, args);
                break;
            case writeEndElement:
                if(!queue.isEmpty())
                {
                    final Event e1 = queue.get(0);
                    e1.m = m("writeEmptyElement", e1.m.getParameterTypes());
                    fq();
                }
                else
                {
                    ex(e, args);
                }
                break;
        }
    }
    Event e(Method m,Object...params)
    {
        final Event e = new Event();
        e.m = m;
        e.args = params;
        return e;
    }
    Method m(String methodName,Class<?>...args) throws XMLStreamException {
        try {
            return XMLStreamWriter.class.getMethod(methodName, args);
        } catch (Exception e) {
            throw new XMLStreamException(e);
        }
    }
    void fq() throws XMLStreamException
    {
        for(int i = 0;i < queue.size();i++)
        {
            Event e = queue.get(i);
            ex(e, e.args);
        }
        queue.clear();
    }
    void ex(Event e,Object...args) throws XMLStreamException
    {
        try
        {
            e.m.invoke(super.getDelegate(), e.args);
        }
        catch(Exception ex)
        {
            throw new XMLStreamException(ex);
        }
    }

}
