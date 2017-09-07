/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2016, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
 * Dept of Linguistics, Memorial University <https://phon.ca>
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ca.phon.xml;

import javax.xml.stream.events.*;

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