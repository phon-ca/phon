package ca.phon.app.session.editor.view.common;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;

import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import javax.swing.text.Highlighter.Highlight;
import javax.swing.text.Highlighter.HighlightPainter;
import javax.swing.text.JTextComponent;

import ca.phon.orthography.OrthoComment;
import ca.phon.orthography.OrthoElement;
import ca.phon.orthography.OrthoEvent;
import ca.phon.orthography.OrthoWord;
import ca.phon.orthography.Orthography;
import ca.phon.session.Tier;
import ca.phon.util.Range;
import ca.phon.visitor.VisitorAdapter;
import ca.phon.visitor.annotation.Visits;

/**
 * Tier editor for {@link Orthography} type.
 */
public class OrthoGroupField extends GroupField<Orthography> {

	private static final long serialVersionUID = -7358501453702966912L;
	
	private final HighlightPainter prefixPainter = new DefaultHighlighter.DefaultHighlightPainter(Color.decode("#FFFF00"));
	
	private final HighlightPainter suffixPainter = new DefaultHighlighter.DefaultHighlightPainter(Color.decode("#36BECE"));
	
	private final HighlightPainter commentPainter = new DefaultHighlighter.DefaultHighlightPainter(Color.decode("#EEEEEE"));
	
	private final HighlightPainter eventPainter = new DefaultHighlighter.DefaultHighlightPainter(Color.decode("#9EED00"));

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
					int i = currentPos + word.getPrefix().getCode().length() + word.getWord().length();
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
