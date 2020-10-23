/*
 * Copyright (C) 2005-2020 Gregory Hedlund & Yvan Rose
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at

 *    http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ca.phon.xml;

import javax.xml.stream.events.*;

import ca.phon.visitor.*;
import ca.phon.visitor.annotation.*;

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