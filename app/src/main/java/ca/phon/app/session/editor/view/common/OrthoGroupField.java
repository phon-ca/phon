/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2015, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
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
package ca.phon.app.session.editor.view.common;

import java.awt.Color;

import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter.HighlightPainter;

import ca.phon.orthography.OrthoComment;
import ca.phon.orthography.OrthoElement;
import ca.phon.orthography.OrthoEvent;
import ca.phon.orthography.OrthoWord;
import ca.phon.orthography.Orthography;
import ca.phon.session.Tier;
import ca.phon.visitor.VisitorAdapter;
import ca.phon.visitor.annotation.Visits;

/**
 * Tier editor for {@link Orthography} type.
 */
public class OrthoGroupField extends GroupField<Orthography> {

	private static final long serialVersionUID = -7358501453702966912L;
	
	private final HighlightPainter prefixPainter = new DefaultHighlighter.DefaultHighlightPainter(
			new Color(255, 255, 0, 100));
	
	private final HighlightPainter suffixPainter = new DefaultHighlighter.DefaultHighlightPainter(
			new Color(0x36 / 255.0f, 0xbe / 255.0f, 0xce / 255.0f, 100 / 255.0f));
	
	private final HighlightPainter commentPainter = new DefaultHighlighter.DefaultHighlightPainter(
			new Color(0xee/255.0f,0xee/255.0f,0xee/255.0f,100/255.0f));
	
	private final HighlightPainter eventPainter = new DefaultHighlighter.DefaultHighlightPainter(
			new Color(0x93/255.0f,0xed/255.0f,0.0f,100/255.0f));

	public OrthoGroupField(Tier<Orthography> tier,
			int groupIndex) {
		super(tier, groupIndex);
		getGroupValue().accept(new HighlightVisitor(getText()));
	}
	
	@Override
	protected boolean validateText() {
		boolean retVal = super.validateText();
		
		if(retVal) {
			getHighlighter().removeAllHighlights();
			final Orthography ortho = getValidatedObject();
			ortho.accept(new HighlightVisitor(getText()));
		}
		
		return retVal;
	}
	
	public class HighlightVisitor extends VisitorAdapter<OrthoElement> {
		
		int currentPos = 0;
		
		String text;
		
		public HighlightVisitor(String text) {
			this.text = text;
		}

		@Override
		public void fallbackVisit(OrthoElement obj) {
			int objIdx = text.indexOf(obj.text(), currentPos);
			currentPos = objIdx + obj.text().length();
			while(currentPos < text.length() && Character.isWhitespace(text.charAt(currentPos))) currentPos++;
		}
		
		@Visits
		public void visitWord(OrthoWord word) {
			if(word.getPrefix() != null) {
				try {
					getHighlighter().addHighlight(currentPos,
							currentPos + word.getPrefix().getCode().length(), prefixPainter);
				} catch (BadLocationException e) {
					
				}
			}
			if(word.getSuffix() != null) {
				try {
					int i = currentPos + (word.getPrefix() != null ? word.getPrefix().getCode().length() : 0) + word.getWord().length();
					getHighlighter().addHighlight(i, i+word.getSuffix().getCode().length()+1, suffixPainter);
							
				} catch (BadLocationException e)  {}
			}
			fallbackVisit(word);
		}
		
		@Visits
		public void visitComment(OrthoComment comment) {
			try {
				int i = currentPos;
				int j = i + comment.getData().length() + 2;
				getHighlighter().addHighlight(i, j, commentPainter);
			} catch (BadLocationException e) {
			}
			fallbackVisit(comment);
		}
		
		@Visits
		public void visitEvent(OrthoEvent event) {
			try {
				int i = currentPos;
				int j = i + event.getData().length() + 2;
				getHighlighter().addHighlight(i, j, eventPainter);
			} catch (BadLocationException e) {
				
			}
			fallbackVisit(event);
		}
		
	}
	
}
