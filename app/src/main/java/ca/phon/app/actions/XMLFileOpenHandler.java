package ca.phon.app.actions;

import java.io.*;
import java.util.*;

import javax.xml.stream.*;
import javax.xml.stream.events.*;

import ca.phon.plugin.*;

@PhonPlugin(name="Open")
public class XMLFileOpenHandler implements OpenFileHandler, IPluginExtensionPoint<OpenFileHandler> {

	@Override
	public Class<?> getExtensionType() {
		return OpenFileHandler.class;
	}

	@Override
	public IPluginExtensionFactory<OpenFileHandler> getFactory() {
		return (args) -> this;
	}

	@Override
	public Set<String> supportedExtensions() {
		Set<String> retVal = new LinkedHashSet<>();
		retVal.add("xml");
		
		var extPts = PluginManager.getInstance().getExtensionPoints(XMLOpenHandler.class);
		for(var extPt:extPts) {
			var xmlOpenFileHandler = extPt.getFactory().createObject();
			retVal.addAll(xmlOpenFileHandler.supportedExtensions());
		}
		return retVal;
	}

	@Override
	public boolean canOpen(File file) throws IOException {
		return true;
		
	}

	@Override
	public void openFile(File file) throws IOException {
		// determine file type based on root element
		XMLInputFactory inputFactory = XMLInputFactory.newDefaultFactory();
		try {
			XMLEventReader eventReader = 
					inputFactory.createXMLEventReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
			
			StartElement rootEle = null;
			while(eventReader.hasNext() && rootEle == null) {
				XMLEvent evt = eventReader.nextEvent();
				if(evt.isStartElement()) {
					rootEle = evt.asStartElement();
				}
			}
			
			var extPts = PluginManager.getInstance().getExtensionPoints(XMLOpenHandler.class);
			for(var extPt:extPts) {
				var xmlOpenHandler = extPt.getFactory().createObject();
				if(xmlOpenHandler.canRead(rootEle)) {
					xmlOpenHandler.openXMLFile(file);
					return;
				}
			}
			
			// open file as text to new buffer
			TextFileOpenHandler txtHandler = new TextFileOpenHandler();
			txtHandler.openFile(file);
		} catch (XMLStreamException e) {
			throw new IOException(e);
		}
	}
	
}
