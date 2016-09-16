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
package ca.phon.session.io.xml.v12;

import ca.phon.orthography.OrthoComment;
import ca.phon.orthography.OrthoElement;
import ca.phon.orthography.OrthoEvent;
import ca.phon.orthography.OrthoPunct;
import ca.phon.orthography.OrthoWord;
import ca.phon.orthography.OrthoWordnet;
import ca.phon.visitor.VisitorAdapter;
import ca.phon.visitor.annotation.Visits;

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
		ct.setContent(
				(comment.getType() != null ? comment.getType() + ":" : "") + comment.getData());
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
