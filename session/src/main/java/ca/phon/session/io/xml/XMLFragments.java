package ca.phon.session.io.xml;

import ca.phon.ipa.IPATranscript;
import ca.phon.orthography.Orthography;
import ca.phon.orthography.mor.Grasp;
import ca.phon.orthography.mor.Mor;
import ca.phon.orthography.mor.MorTierData;
import ca.phon.session.SessionFactory;
import ca.phon.session.Tier;
import ca.phon.session.io.SessionIO;
import ca.phon.session.io.SessionInputFactory;
import ca.phon.session.io.SessionOutputFactory;
import ca.phon.session.io.xml.v2_0.*;
import ca.phon.session.io.xml.v2_0.XmlSessionReaderV2_0;
import ca.phon.session.io.xml.v2_0.XmlSessionWriterV2_0;
import ca.phon.xml.DelegatingXMLStreamWriter;
import com.sun.xml.txw2.output.IndentingXMLStreamWriter;
import jakarta.xml.bind.*;

import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.*;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stax.StAXResult;
import javax.xml.transform.stax.StAXSource;
import java.io.*;
import java.nio.charset.StandardCharsets;

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
                xmlStreamWriter.setDefaultNamespace(XmlSessionWriterV2_0.DEFAULT_NAMESPACE);
            final JAXBContext context = JAXBContext.newInstance(ObjectFactory.class);
            final Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FRAGMENT, true);
            marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
            if(includeNamespace)
                marshaller.setProperty(Marshaller.JAXB_SCHEMA_LOCATION, String.format("%s %s", XmlSessionWriterV2_0.DEFAULT_NAMESPACE, XmlSessionWriterV2_0.DEFAULT_NAMESPACE_LOCATION));
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
        XmlSessionWriterV2_0 writer = (XmlSessionWriterV2_0) (new SessionOutputFactory())
                .createWriter(XmlSessionWriterV2_0.class.getAnnotation(SessionIO.class));
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
        XmlSessionWriterV2_0 writer = (XmlSessionWriterV2_0) (new SessionOutputFactory())
                .createWriter(XmlSessionWriterV2_0.class.getAnnotation(SessionIO.class));
        XmlPhoneticTranscriptionType pho = writer.writeIPA(new ObjectFactory(), ipa);
        final ObjectFactory objectFactory = new ObjectFactory();
        final JAXBElement<XmlPhoneticTranscriptionType> ele = objectFactory.createPho(pho);
        final ByteArrayOutputStream bout = new ByteArrayOutputStream();
        writeFragment(ele, bout, includeNamespace, formatted);
        return bout.toString(StandardCharsets.UTF_8);
    }

    /**
     * Generate xml fragment for given mor
     *
     * @param mor
     * @oaram includeNamespace
     * @param formatted
     * @return xml fragment for given Mor
     * @throws IOException
     */
    public static String toXml(Mor mor, String type, boolean includeNamespace, boolean formatted) throws IOException {
        XmlSessionWriterV2_0 writer = (XmlSessionWriterV2_0) (new SessionOutputFactory())
                .createWriter(XmlSessionWriterV2_0.class.getAnnotation(SessionIO.class));
        XmlMorType morType = writer.writeMor(new ObjectFactory(), mor, type);
        final ObjectFactory objectFactory = new ObjectFactory();
        final JAXBElement<XmlMorType> ele = objectFactory.createMor(morType);
        final ByteArrayOutputStream bout = new ByteArrayOutputStream();
        writeFragment(ele, bout, includeNamespace, formatted);
        return bout.toString(StandardCharsets.UTF_8);
    }

    /**
     * Generate xml fragment for given grasp
     *
     * @param gra
     * @oaram includeNamespace
     * @param formatted
     * @return xml fragment for given Grasp
     * @throws IOException
     */
    public static String toXml(Grasp gra, String type, boolean includeNamespace, boolean formatted) throws IOException {
        XmlSessionWriterV2_0 writer = (XmlSessionWriterV2_0) (new SessionOutputFactory())
                .createWriter(XmlSessionWriterV2_0.class.getAnnotation(SessionIO.class));
        XmlGraType graType = writer.writeGra(new ObjectFactory(), gra, type);
        final ObjectFactory objectFactory = new ObjectFactory();
        final JAXBElement<XmlGraType> ele = objectFactory.createGra(graType);
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
     * Add namespace to first element of given xml fragment if necessary
     * @param text
     * @return text with namespace added if necessary, input text otherwise
     */
    private static String addNamespace(String text) throws IOException {
        try(ByteArrayInputStream in = new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8))) {
            final ByteArrayOutputStream out = new ByteArrayOutputStream();
            final XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(in);
            final XMLStreamWriter writer = XMLOutputFactory.newFactory().createXMLStreamWriter(out, "UTF-8");
            XMLStreamWriter noXmlDeclWriter = new DelegatingXMLStreamWriter(writer) {

                boolean wroteFirstStart = false;

                @Override
                public void writeStartElement(String localName) throws XMLStreamException {
                    super.writeStartElement(localName);
                    if(!wroteFirstStart) {
                        super.writeDefaultNamespace(XmlSessionWriterV2_0.DEFAULT_NAMESPACE);
                    }
                    wroteFirstStart = true;
                }

                @Override
                public void writeStartElement(String namespaceURI, String localName) throws XMLStreamException {
                    super.writeStartElement(namespaceURI, localName);
                    wroteFirstStart = true;
                }

                @Override
                public void writeStartElement(String prefix, String localName, String namespaceURI) throws XMLStreamException {
                    super.writeStartElement(prefix, localName, namespaceURI);
                    wroteFirstStart = true;
                }

                @Override
                public void writeNamespace(String prefix, String namespaceURI) throws XMLStreamException {
                }

                @Override
                public void writeDefaultNamespace(String namespaceURI) throws XMLStreamException {}
                public void writeStartDocument() throws XMLStreamException {}
                public void writeStartDocument(String version) throws XMLStreamException {}
                public void writeStartDocument(String encoding, String version) throws XMLStreamException {}
            };

            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer t = tf.newTransformer();

            StAXSource source = new StAXSource(reader);
            StAXResult result = new StAXResult(noXmlDeclWriter);

            t.transform(source, result);
            return out.toString(StandardCharsets.UTF_8);
        } catch (XMLStreamException | TransformerException xmlStreamException) {
            throw new IOException(xmlStreamException);
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
        xml = addNamespace(xml);
        XmlUtteranceType u = readFragment(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)), XmlUtteranceType.class);
        XmlSessionReaderV2_0 xmlReader = (XmlSessionReaderV2_0) (new SessionInputFactory()).createReader(XmlSessionReaderV2_0.class.getAnnotation(SessionIO.class));
        return xmlReader.readOrthography(u);
    }

    public static IPATranscript ipaFromXml(String xml) throws IOException {
        // special case if <u> has no namespace
        xml = addNamespace(xml);
        XmlPhoneticTranscriptionType pho = readFragment(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)), XmlPhoneticTranscriptionType.class);
        XmlSessionReaderV2_0 xmlReader = (XmlSessionReaderV2_0) (new SessionInputFactory()).createReader(XmlSessionReaderV2_0.class.getAnnotation(SessionIO.class));
        return xmlReader.readTranscript(pho);
    }

    public static MorTierData morsFromXml(String xml) throws IOException {
        xml = addNamespace(xml);
        XmlMorTierData morTierData = readFragment(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)), XmlMorTierData.class);
        XmlSessionReaderV2_0 xmlReader = (XmlSessionReaderV2_0) (new SessionInputFactory()).createReader(XmlSessionReaderV2_0.class.getAnnotation(SessionIO.class));
        return xmlReader.readMorTierData(SessionFactory.newFactory(), morTierData);
    }

    public static Mor morFromXml(String xml) throws IOException {
        xml = addNamespace(xml);
        XmlMorType mor = readFragment(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)), XmlMorType.class);
        XmlSessionReaderV2_0 xmlReader = (XmlSessionReaderV2_0) (new SessionInputFactory()).createReader(XmlSessionReaderV2_0.class.getAnnotation(SessionIO.class));
        return xmlReader.readMor(SessionFactory.newFactory(), mor);
    }

    public static Grasp graFromXml(String xml) throws IOException {
        xml = addNamespace(xml);
        XmlGraType gra = readFragment(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)), XmlGraType.class);
        XmlSessionReaderV2_0 xmlReader = (XmlSessionReaderV2_0) (new SessionInputFactory()).createReader(XmlSessionReaderV2_0.class.getAnnotation(SessionIO.class));
        return xmlReader.readGra(SessionFactory.newFactory(), gra);
    }

    // endregion xml -> Object

}
