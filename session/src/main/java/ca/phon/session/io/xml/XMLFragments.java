package ca.phon.session.io.xml;

import ca.phon.ipa.IPATranscript;
import ca.phon.orthography.Orthography;
import ca.phon.session.io.SessionIO;
import ca.phon.session.io.SessionInputFactory;
import ca.phon.session.io.SessionOutputFactory;
import ca.phon.session.io.xml.v1_3.*;
import com.sun.xml.txw2.output.IndentingXMLStreamWriter;
import jakarta.xml.bind.*;

import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

public final class XMLFragments {

    // region Object -> xml
    /**
     * Write xml fragment to given stream
     *
     * @param jaxbElement
     * @param out
     * @param includeNamespace
     * @param formatted
     * @throws IOException
     */
    public static void writeFragment(JAXBElement<?> jaxbElement, OutputStream out, boolean includeNamespace, boolean formatted) throws IOException {
        try(BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out))) {
            XMLStreamWriter xmlStreamWriter = XMLOutputFactory.newFactory().createXMLStreamWriter(writer);
            if(formatted)
                xmlStreamWriter = new IndentingXMLStreamWriter(xmlStreamWriter);
            if(!includeNamespace)
                xmlStreamWriter.setDefaultNamespace(XmlSessionWriterV1_3.DEFAULT_NAMESPACE);
            final JAXBContext context = JAXBContext.newInstance(ObjectFactory.class);
            final Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FRAGMENT, true);
            marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
            if(includeNamespace)
                marshaller.setProperty(Marshaller.JAXB_SCHEMA_LOCATION, String.format("%s %s", XmlSessionWriterV1_3.DEFAULT_NAMESPACE, XmlSessionWriterV1_3.DEFAULT_NAMESPACE_LOCATION));
            marshaller.marshal(jaxbElement, xmlStreamWriter);
        } catch(JAXBException | XMLStreamException e) {
            throw new IOException(e);
        }
    }

    /**
     * Generate xml fragment for Orthogrpahy
     *
     * @param orthography
     * @param includeNamespace
     * @return xml fragment for given orthography
     * @throws IOException on error
     */
    public static String toXml(Orthography orthography, boolean includeNamespace, boolean formatted) throws IOException {
        XmlSessionWriterV1_3 writer = (XmlSessionWriterV1_3) (new SessionOutputFactory())
                .createWriter(XmlSessionWriterV1_3.class.getAnnotation(SessionIO.class));
        XmlUtteranceType u = writer.writeOrthography(new ObjectFactory(), orthography);
        final ObjectFactory objectFactory = new ObjectFactory();
        final JAXBElement<XmlUtteranceType> ele = objectFactory.createU(u);
        final ByteArrayOutputStream bout = new ByteArrayOutputStream();
        writeFragment(ele, bout, includeNamespace, formatted);
        return bout.toString(StandardCharsets.UTF_8);
    }

    /**
     * Generate xml fragment for given ipa
     *
     * @param ipa
     * @param includeNamespace
     * @param formatted
     * @return xml fragment for given ipa
     * @throws IOException on error
     */
    public static String toXml(IPATranscript ipa, boolean includeNamespace, boolean formatted) throws IOException {
        XmlSessionWriterV1_3 writer = (XmlSessionWriterV1_3) (new SessionOutputFactory())
                .createWriter(XmlSessionWriterV1_3.class.getAnnotation(SessionIO.class));
        XmlPhoneticTranscriptionType pho = writer.writeIPA(new ObjectFactory(), ipa);
        final ObjectFactory objectFactory = new ObjectFactory();
        final JAXBElement<XmlPhoneticTranscriptionType> ele = objectFactory.createPho(pho);
        final ByteArrayOutputStream bout = new ByteArrayOutputStream();
        writeFragment(ele, bout, includeNamespace, formatted);
        return bout.toString(StandardCharsets.UTF_8);
    }

    // endregion Object -> xml

    // region xml -> Object

    public static <T> T readFragment(InputStream inputStream, Class<T> type) throws IOException {
        try {
            final XMLInputFactory inputFactory = XMLInputFactory.newFactory();
            final XMLStreamReader reader = inputFactory.createXMLStreamReader(inputStream);
            final JAXBContext context = JAXBContext.newInstance(ObjectFactory.class);
            final Unmarshaller unmarshaller = context.createUnmarshaller();
            Object ele = unmarshaller.unmarshal(reader);

            if (ele instanceof JAXBElement<?> jaxbElement) {
                if (jaxbElement.getDeclaredType() == type) {
                    return type.cast(jaxbElement.getValue());
                } else {
                    throw new IOException(String.format("Expected %s, got %s", type.getName(), jaxbElement.getDeclaredType().getName()));
                }
            } else {
                throw new IOException("Unmarshalled unexpected type");
            }
        } catch(XMLStreamException | JAXBException e) {
            throw new IOException(e);
        }
    }

    /**
     * Create a new instance of Orthpgraphy from given xml
     *
     * @param xml
     * @return orthography described by the xml
     * @throws IOException on error
     */
    public static Orthography orthographyFromXml(String xml) throws IOException {
        // special case if <u> has no namespace
        if(xml.startsWith("<u>")) {
            xml = String.format("<u xmlns=\"%s\">", XmlSessionWriterV1_3.DEFAULT_NAMESPACE)
                    + xml.substring(3);
        }
        XmlUtteranceType u = readFragment(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)), XmlUtteranceType.class);
        XmlSessionReaderV1_3 xmlReader = (XmlSessionReaderV1_3) (new SessionInputFactory()).createReader(XmlSessionReaderV1_3.class.getAnnotation(SessionIO.class));
        return xmlReader.readOrthography(u);
    }

    public static IPATranscript ipaFromXml(String xml) throws IOException {
        final XMLInputFactory inputFactory = XMLInputFactory.newFactory();
        // special case if <u> has no namespace
        if(xml.startsWith("<pho>")) {
            xml = String.format("<pho xmlns=\"%s\">", XmlSessionWriterV1_3.DEFAULT_NAMESPACE)
                    + xml.substring(3);
        }
        XmlPhoneticTranscriptionType pho = readFragment(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)), XmlPhoneticTranscriptionType.class);
        XmlSessionReaderV1_3 xmlReader = (XmlSessionReaderV1_3) (new SessionInputFactory()).createReader(XmlSessionReaderV1_3.class.getAnnotation(SessionIO.class));
        return xmlReader.readTranscript(pho);
    }

    // endregion xml -> Object

}
