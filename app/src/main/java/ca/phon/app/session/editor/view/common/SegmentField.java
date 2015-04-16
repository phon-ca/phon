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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.text.ParseException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFormattedTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import javax.swing.text.JTextComponent;
import javax.swing.text.MaskFormatter;

import ca.phon.session.MediaSegment;
import ca.phon.session.MediaSegmentFormatter;

public class SegmentField extends JFormattedTextField {
	
	private final Highlighter errHighlighter = new DefaultHighlighter();
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -5405525676580970609L;

	private static final Logger LOGGER = Logger
			.getLogger(SegmentField.class.getName());

	public SegmentField() {
		super();
		
		errHighlighter.install(this);
		setOpaque(false);
		this.setFormatterFactory(new SegmentFormatterFactory());
		
		setBorder(new GroupFieldBorder());
		
		getDocument().addDocumentListener(docListener);
	}
	
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		errHighlighter.paint(g);
	}
	
	@Override
	public Dimension getPreferredSize() {
		Dimension retVal = super.getPreferredSize();
		
		retVal.width += 5;
		
		return retVal;
	}
	
	public void validateText() {
		errHighlighter.removeAllHighlights();
		final MediaSegmentFormatter formatter = new MediaSegmentFormatter();
		try {
			final MediaSegment seg = formatter.parse(getText());
			
			if(seg.getStartValue() > seg.getEndValue()) {
				((GroupFieldBorder)getBorder()).setShowWarningIcon(true);
				setToolTipText("Start time is after end time");
				try {
					errHighlighter.addHighlight(0, getText().length(), errPainter);
				} catch (BadLocationException e) {
					LOGGER
							.log(Level.SEVERE, e.getLocalizedMessage(), e);
				}
			} else {
				((GroupFieldBorder)getBorder()).setShowWarningIcon(false);
				setToolTipText(null);
			}
			
		} catch (ParseException e) {
			((GroupFieldBorder)getBorder()).setShowWarningIcon(true);
			setToolTipText(e.getLocalizedMessage());
		}
	}
	
	private DocumentListener docListener = new DocumentListener() {
		
		@Override
		public void removeUpdate(DocumentEvent e) {
			
		}
		
		@Override
		public void insertUpdate(DocumentEvent e) {
			validateText();
		}
		
		@Override
		public void changedUpdate(DocumentEvent e) {
			
		}
	};
	
	/** Formatter factory */
	private class SegmentFormatterFactory extends AbstractFormatterFactory {

		@Override
		public AbstractFormatter getFormatter(JFormattedTextField arg0) {
			AbstractFormatter retVal = null;
			try {
				retVal = new MaskFormatter("###:##.###-###:##.###");
				((MaskFormatter)retVal).setPlaceholderCharacter('0');
			} catch (ParseException e) {
				LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
			}
			return retVal;
		}

	}
	
	private final Highlighter.HighlightPainter errPainter = new Highlighter.HighlightPainter() {
		
		@Override
		public void paint(Graphics g, int p0, int p1, Shape bounds, JTextComponent c) {
			final Graphics2D g2 = (Graphics2D)g;
			
			Rectangle b = bounds.getBounds();
			try {
				final Rectangle p0rect = c.modelToView(p0);
				final Rectangle p1rect = c.modelToView(p1);
				
				b = new Rectangle(p0rect).union(p1rect);
			} catch (BadLocationException e) {
				
			}
			
			g2.setColor(Color.red);
			final float dash1[] = {1.0f};
		    final BasicStroke dashed =
		        new BasicStroke(1.0f,
		                        BasicStroke.CAP_BUTT,
		                        BasicStroke.JOIN_MITER,
		                        1.0f, dash1, 0.0f);
			g2.setStroke(dashed);
			g2.drawLine(b.x, 
					b.y + b.height - 1, 
					b.x + b.width, 
					b.y + b.height - 1);
		}
	};
}
