package ca.phon.session.io.xml;

import ca.phon.orthography.Orthography;
import ca.phon.session.io.SessionIO;
import ca.phon.session.io.SessionInputFactory;
import ca.phon.session.io.SessionOutputFactory;
import ca.phon.session.io.xml.v1_3.ObjectFactory;
import ca.phon.session.io.xml.v1_3.XmlSessionReaderV1_3;
import ca.phon.session.io.xml.v1_3.XmlSessionWriterV1_3;
import ca.phon.session.io.xml.v1_3.XmlUtteranceType;
import jakarta.xml.bind.*;

import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

public final class XMLFragments {

    /**
     * Generate xml fragment for Orthogrpahy
     *
     * @param orthography
     * @param includeNamespace
     * @return xml fragment for given orthography
     */
    public static String toXml(Orthography orthography, boolean includeNamespace) {
        XmlSessionWriterV1_3 writer = (XmlSessionWriterV1_3) (new SessionOutputFactory())
                .createWriter(XmlSessionWriterV1_3.class.getAnnotation(SessionIO.class));
        XmlUtteranceType u = writer.writeOrthography(new ObjectFactory(), orthography);
        final ObjectFactory objectFactory = new ObjectFactory();
        final JAXBElement<XmlUtteranceType> ele = objectFactory.createU(u);
        final ByteArrayOutputStream bout = new ByteArrayOutputStream();
        try(BufferedWriter out = new BufferedWriter(new OutputStreamWriter(bout))) {
            final XMLOutputFactory outputFactory = XMLOutputFactory.newFactory();
            XMLStreamWriter xmlStreamWriter = XMLOutputFactory.newFactory().createXMLStreamWriter(out);
            if(!includeNamespace) {
                xmlStreamWriter.setNamespaceContext(new NamespaceContext() {
                    @Override
                    public String getNamespaceURI(String prefix) {
                        return null;
                    }

                    @Override
                    public String getPrefix(String namespaceURI) {
                        return "";
                    }

                    @Override
                    public Iterator<String> getPrefixes(String namespaceURI) {
                        return null;
                    }
                });
            }
            final JAXBContext context = JAXBContext.newInstance(ObjectFactory.class);
            final Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FRAGMENT, true);
            marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            if(includeNamespace)
                marshaller.setProperty(Marshaller.JAXB_SCHEMA_LOCATION, String.format("%s %s", XmlSessionWriterV1_3.DEFAULT_NAMESPACE, XmlSessionWriterV1_3.DEFAULT_NAMESPACE_LOCATION));
            marshaller.marshal(ele, xmlStreamWriter);
        } catch(JAXBException | IOException | XMLStreamException e) {
            e.printStackTrace();
        }

        return bout.toString(StandardCharsets.UTF_8);
    }

    public static Orthography orthographyFromXml(String xml) {
        final XMLInputFactory inputFactory = XMLInputFactory.newFactory();
        // special case if <u> has no namespace
        if(xml.startsWith("<u>")) {
            xml = String.format("<u xmlns=\"%s\">", XmlSessionWriterV1_3.DEFAULT_NAMESPACE)
                    + xml.substring(3);
        }
        try {
            final XMLStreamReader reader = inputFactory.createXMLStreamReader(new InputStreamReader(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8))));
            final JAXBContext context = JAXBContext.newInstance(ObjectFactory.class);
            final Unmarshaller unmarshaller = context.createUnmarshaller();
            Object ele = unmarshaller.unmarshal(reader);

            if(ele instanceof JAXBElement<?> jaxbElement) {
                if(jaxbElement.getDeclaredType() == XmlUtteranceType.class) {
                    XmlUtteranceType u = (XmlUtteranceType) jaxbElement.getValue();

                    XmlSessionReaderV1_3 xmlReader = (XmlSessionReaderV1_3) (new SessionInputFactory()).createReader(XmlSessionReaderV1_3.class.getAnnotation(SessionIO.class));
                    Orthography orthography = xmlReader.readOrthography(u);
                    return orthography;
                } else {
                    throw new IllegalArgumentException("Invalid type given " + jaxbElement.getDeclaredType());
                }
            } else {
                throw new IllegalArgumentException("Not a valid JAXB element " + ele);
            }
        } catch (XMLStreamException | JAXBException e) {
            throw new RuntimeException(e);
        }
    }

}
