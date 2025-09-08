package ca.session.io.xml.v2_1;

import ca.phon.plugin.Rank;
import ca.phon.session.Session;
import ca.phon.session.io.SessionIO;
import ca.phon.session.io.xml.v2_0.XmlSessionReaderV2_0;
import ca.phon.xml.annotation.XMLSerial;

@XMLSerial(
        namespace="https://phon.ca/ns/session",
        elementName="session",
        bindType= Session.class
)
@SessionIO(
        group="ca.phon",
        id="phonbank",
        version="2.0",
        mimetype="application/xml",
        extension="xml",
        name="Phon 4.0+ (.xml)"
)
@Rank(0)
public class XmlSessionReaderV2_1 extends XmlSessionReaderV2_0 {
}
