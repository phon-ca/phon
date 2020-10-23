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
package ca.phon.session.io.xml.v12;

import ca.phon.orthography.*;
import ca.phon.visitor.*;
import ca.phon.visitor.annotation.*;

/**
 * Converts the visited orthography elements into a
 * {@link GroupType} object useable by JAXB for serialization.
 */
public class OrthoToXmlVisitor extends VisitorAdapter<OrthoElement> {
	
	private final ObjectFactory factory = new ObjectFactory();
	
	private GroupType gt;
	
	public OrthoToXmlVisitor() {
		gt = factory.createGroupType();
	}
	
	public GroupType getGroup() {
		return this.gt;
	}

	@Override
	public void fallbackVisit(OrthoElement obj) {
	}

	@Visits
	public void visitComment(OrthoComment comment) {
		final CommentType ct = factory.createCommentType();
		ct.getContent().add(comment.getData());
		ct.setType(comment.getType());
		gt.getWOrComOrE().add(ct);
	}

	@Visits
	public void visitEvent(OrthoEvent event) {
		final EventType et = factory.createEventType();
		et.setContent(
				(event.getType() != null ? event.getType() + ":" : "") + event.getData());
		gt.getWOrComOrE().add(et);
	}

	@Visits
	public void visitPunct(OrthoPunct punct) {
		final PunctuationType pt = factory.createPunctuationType();
		pt.setContent(punct.text());
		pt.setType(punct.getType().toString());
		gt.getWOrComOrE().add(pt);
	}

	@Visits
	public void visitWord(OrthoWord word) {
		final WordType wt = factory.createWordType();
		wt.setContent(word.text());
		gt.getWOrComOrE().add(wt);
	}

	@Visits
	public void visitWordnet(OrthoWordnet wordnet) {
		final WordType wt = factory.createWordType();
		wt.setContent(wordnet.text());
		gt.getWOrComOrE().add(wt);
	}

}
