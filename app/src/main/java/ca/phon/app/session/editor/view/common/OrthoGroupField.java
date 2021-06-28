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
package ca.phon.app.session.editor.view.common;

import java.awt.*;
import java.text.*;

import javax.swing.text.*;
import javax.swing.text.Highlighter.*;

import ca.phon.extensions.*;
import ca.phon.orthography.*;
import ca.phon.session.*;
import ca.phon.visitor.*;
import ca.phon.visitor.annotation.*;

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
	
	private final HighlightPainter untranscribedPainter = commentPainter;

	public OrthoGroupField(Tier<Orthography> tier,
			int groupIndex) {
		super(tier, groupIndex);
		getGroupValue().accept(new HighlightVisitor(getText()));
	}
	
	@Override
	protected boolean validateText() {
		removeAllErrorHighlights();
		getHighlighter().removeAllHighlights();
		
		boolean wasShowingErr = ((GroupFieldBorder)getBorder()).isShowWarningIcon();
		
		try {
			Orthography ortho = Orthography.parseOrthography(getText());
			setValidatedObject(ortho);
			((GroupFieldBorder)getBorder()).setShowWarningIcon(false);
			setToolTipText(null);
			if(wasShowingErr) repaint();
			
			ortho.accept(new HighlightVisitor(getText()));
		} catch (ParseException e) {
			Orthography validatedOrtho = new Orthography();
			validatedOrtho.putExtension(UnvalidatedValue.class, new UnvalidatedValue(getText().trim(), e));
			((GroupFieldBorder)getBorder()).setShowWarningIcon(true);
			
			final StringBuilder sb = new StringBuilder();
			sb.append("Error at character ").append(e.getErrorOffset()).append(": ").append(e.getLocalizedMessage());
			setToolTipText(sb.toString());
			
			addErrorHighlight(e.getErrorOffset(), e.getErrorOffset()+1);
			setValidatedObject(validatedOrtho);
			if(!wasShowingErr) repaint();
		}
		
		return true;
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
							currentPos + word.getPrefix().toString().length(), prefixPainter);
				} catch (BadLocationException e) {
					
				}
			}
			if(word.getSuffix() != null) {
				try {
					int i = currentPos + (word.getPrefix() != null ? word.getPrefix().toString().length() : 0) + word.getWord().length();
					getHighlighter().addHighlight(i, i+word.getSuffix().toString().length(), suffixPainter);
							
				} catch (BadLocationException e)  {}
			}
			if(word.isUntranscribed()) {
				try {
					int i = currentPos + (word.getPrefix() != null ? word.getPrefix().toString().length() : 0);
					getHighlighter().addHighlight(i, i+word.getWord().length(), untranscribedPainter);
				} catch (BadLocationException e) {}
			}
			fallbackVisit(word);
		}
		
		@Visits
		public void visitComment(OrthoComment comment) {
			try {
				int i = currentPos;
				int j = i + comment.text().length();
				getHighlighter().addHighlight(i, j, commentPainter);
			} catch (BadLocationException e) {
			}
			fallbackVisit(comment);
		}
		
		@Visits
		public void visitEvent(OrthoEvent event) {
			try {
				int i = currentPos;
				int j = i + event.text().length();
				getHighlighter().addHighlight(i, j, eventPainter);
			} catch (BadLocationException e) {
				
			}
			fallbackVisit(event);
		}
		
	}
	
}
