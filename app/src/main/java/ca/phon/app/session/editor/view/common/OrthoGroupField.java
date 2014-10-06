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
	
	private final Highlighter orthoHighlighter = new DefaultHighlighter();

	public OrthoGroupField(Tier<Orthography> tier,
			int groupIndex) {
		super(tier, groupIndex);
		getOrthoHighlighter().install(this);
		getGroupValue().accept(new HighlightVisitor());
	}
	
	@Override
	protected boolean validateText() {
		boolean retVal = super.validateText();
		
		if(retVal) {
			getOrthoHighlighter().removeAllHighlights();
			final Orthography ortho = getValidatedObject();
			ortho.accept(new HighlightVisitor());
		}
		
		return retVal;
	}
	
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		// paint ortho highlights on top
		getOrthoHighlighter().paint(g);
	}
	
	public Highlighter getOrthoHighlighter() {
		return this.orthoHighlighter;
	}

	public class HighlightVisitor extends VisitorAdapter<OrthoElement> {
		
		int currentPos = 0;

		@Override
		public void fallbackVisit(OrthoElement obj) {
			currentPos += obj.text().length() + 1;
		}
		
		@Visits
		public void visitWord(OrthoWord word) {
			if(word.getPrefix() != null) {
				try {
					getOrthoHighlighter().addHighlight(currentPos,
							currentPos + word.getPrefix().getCode().length(), orthoPainter);
				} catch (BadLocationException e) {
					
				}
			} else if(word.getSuffix() != null) {
				try {
					int i = currentPos + word.getWord().length();
					getOrthoHighlighter().addHighlight(i, i+word.getSuffix().getCode().length()+1, orthoPainter);
							
				} catch (BadLocationException e)  {}
			}
			fallbackVisit(word);
		}
		
		@Visits
		public void visitComment(OrthoComment comment) {
			try {
				int i = currentPos + 1;
				int j = i + comment.getData().length();
				getOrthoHighlighter().addHighlight(i, j, orthoPainter);
			} catch (BadLocationException e) {
			}
			fallbackVisit(comment);
		}
		
		@Visits
		public void visitEvent(OrthoEvent event) {
			try {
				int i = currentPos + 1;
				int j = i + event.getData().length();
				getOrthoHighlighter().addHighlight(i, j, orthoPainter);
			} catch (BadLocationException e) {
				
			}
			fallbackVisit(event);
		}
		
	}
	
	private final Highlighter.HighlightPainter orthoPainter = new Highlighter.HighlightPainter() {
		
		@Override
		public void paint(Graphics g, int p0, int p1, Shape bounds, JTextComponent c) {
			try {
				final Graphics2D g2 = (Graphics2D)g;
				g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
				g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
				
				final Rectangle p0rect = c.modelToView(p0);
				final Rectangle p1rect = c.modelToView(p1);
				
				final Rectangle highlightRect = new Rectangle(p0rect).union(p1rect);
				
				g2.clearRect(highlightRect.x, highlightRect.y, highlightRect.width, highlightRect.height);
				
				// repaint selection highlight
				final Range highlightRange = new Range(p0, p1, false);				
				int selStart = -1;
				int selEnd = -1;
				for(int i = getSelectionStart(); i < getSelectionEnd(); i++) {
					if(highlightRange.contains(i)) {
						if(selStart < 0) 
							selStart = i;
						selEnd = i;
					}
				}
				
				if(selStart >= 0 && c.hasFocus()) {
					final Rectangle selRect1 = c.modelToView(selStart);
					g2.setColor(getSelectionColor());
					g2.fill(new Rectangle(selRect1).union(c.modelToView(selEnd)));
				}
				
				// repaint other highlights
				for(Highlight hl:getHighlighter().getHighlights()) {
					int hlStart = -1;
					int hlEnd = -1;
					for(int i = hl.getStartOffset(); i <= hl.getEndOffset(); i++) {
						if(highlightRange.contains(i)) {
							if(hlStart < 0) 
								hlStart = i;
							hlEnd =i;
						}
					}
					
					if(hlStart > 0) {
						hl.getPainter().paint(g2, hlStart, hlEnd, bounds, c);
					}
				}
				
				// re-draw text
				g2.setFont(c.getFont());
				g2.setColor(Color.blue);
				
				if((p0 >= 0 && p0 < c.getText().length()) && (p1 >= p0 && p1 <= c .getText().length())) {
					final String txt = c.getText().substring(p0, p1);
					g2.drawString(txt, highlightRect.x, 
							getBaseline(highlightRect.width, highlightRect.height));
				}
				
				
			} catch (BadLocationException e) {
				
			}
		}
		
	};
	
}
