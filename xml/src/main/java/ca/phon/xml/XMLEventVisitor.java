package ca.phon.xml;

import javax.xml.stream.events.Characters;
import javax.xml.stream.events.EndDocument;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.EntityDeclaration;
import javax.xml.stream.events.EntityReference;
import javax.xml.stream.events.NotationDeclaration;
import javax.xml.stream.events.ProcessingInstruction;
import javax.xml.stream.events.StartDocument;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import ca.phon.visitor.VisitorAdapter;
import ca.phon.visitor.annotation.Visits;

/**
 * Visitor implementation for xml stream events.
 */
public abstract class XMLEventVisitor extends VisitorAdapter<XMLEvent> {

	@Override
	public void fallbackVisit(XMLEvent obj) {}
	
	@Visits
	public abstract void visitCharacters(Characters chars);

	@Visits
	public abstract void visitProcessingInstruction(ProcessingInstruction pi);

	@Visits
	public abstract void visitStartElement(StartElement startEle);

	@Visits
	public abstract void visitEndElement(EndElement endEle);

	@Visits
	public abstract void visitStartDocument(StartDocument startDoc);

	@Visits
	public abstract void visitEndDocument(EndDocument endDoc);

	@Visits
	public abstract void visitEntityReference(EntityReference entityRef);

	@Visits
	public abstract void visitEntityDeclaration(EntityDeclaration entityDec);

	@Visits
	public abstract void visitNotationDeclaration(NotationDeclaration notDec);

}