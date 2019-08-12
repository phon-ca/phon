package ca.phon.app.query;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import javax.xml.stream.events.StartElement;

import ca.phon.app.actions.XMLOpenHandler;
import ca.phon.plugin.IPluginExtensionFactory;
import ca.phon.plugin.IPluginExtensionPoint;
import ca.phon.plugin.PhonPlugin;

@PhonPlugin(name="Open")
public class QueryXMLFileOpenHandler implements XMLOpenHandler, IPluginExtensionPoint<XMLOpenHandler> {

	@Override
	public Class<?> getExtensionType() {
		return XMLOpenHandler.class;
	}

	@Override
	public IPluginExtensionFactory<XMLOpenHandler> getFactory() {
		return (args) -> this;
	}

	@Override
	public Set<String> supportedExtensions() {
		return Set.of("xml");
	}

	@Override
	public boolean canRead(StartElement startEle) {
		return (startEle.getName().getNamespaceURI().equals("http://phon.ling.mun.ca/ns/query")
					 && startEle.getName().getLocalPart().equals("query"));
	}

	@Override
	public void openXMLFile(File file) throws IOException {
		
	}

}
