package ca.phon.app.session.editor.view.common;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.lang.ref.WeakReference;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JLabel;
import javax.swing.JToolTip;
import javax.swing.text.BadLocationException;
import javax.swing.text.Highlighter;
import javax.swing.text.JTextComponent;

import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.TokenStream;
import org.jdesktop.swingx.VerticalLayout;

import ca.phon.ipa.AlternativeTranscript;
import ca.phon.ipa.IPATranscript;
import ca.phon.ipa.parser.IPALexer;
import ca.phon.ipa.parser.IPAParser;
import ca.phon.ipa.parser.IPAParserErrorHandler;
import ca.phon.ipa.parser.exceptions.IPAParserException;
import ca.phon.session.Tier;
import ca.phon.session.Transcriber;
import ca.phon.session.UnvalidatedValue;
import ca.phon.syllabifier.Syllabifier;

/**
 * Editor for IPA Transcriptions (validated and blind.)
 */
public class IPAGroupField extends GroupField<IPATranscript> {
	
	private static final Logger LOGGER = Logger
			.getLogger(IPAGroupField.class.getName());

	private static final long serialVersionUID = 3938081453789426396L;
	
	private final Syllabifier syllabifier;
	
	private final WeakReference<Transcriber> transcriberRef;

	public IPAGroupField(Tier<IPATranscript> tier,
			int groupIndex) {
		this(tier, groupIndex, null);
	}
	
	public IPAGroupField(Tier<IPATranscript> tier, int groupIndex, Transcriber transcriber) {
		this(tier, groupIndex, transcriber, null);
	}
	
	public IPAGroupField(Tier<IPATranscript> tier, int groupIndex, Transcriber transcriber, Syllabifier syllabifier) {
		super(tier, groupIndex);
		this.syllabifier = syllabifier;
		this.transcriberRef = new WeakReference<Transcriber>(transcriber);
		// init after transcriber is set
		_init();
	}

	public Transcriber getTranscriber() {
		return transcriberRef.get();
	}
	
	@Override
	protected void _init() {
		if(transcriberRef == null) return;
		super._init();
		validateText();
		addTierEditorListener(new TierEditorListener() {
			
			@Override
			public <T> void tierValueChanged(Tier<T> tier, int groupIndex, T newValue,
					T oldValue) {
				if(syllabifier != null) {
					final IPATranscript transcript = (IPATranscript)newValue;
					if(transcript != null && syllabifier != null)
						syllabifier.syllabify(transcript.toList());
				}
			}
			
			@Override
			public <T> void tierValueChange(Tier<T> tier, int groupIndex, T newValue,
					T oldValue) {
			}
			
		});
	}
	
	@Override
	public IPATranscript getGroupValue() {
		IPATranscript retVal = super.getGroupValue();
		final Transcriber transcriber = getTranscriber();
		if(retVal != null && transcriber != null) {
			final AlternativeTranscript alts = retVal.getExtension(AlternativeTranscript.class);
			if(alts != null) {
				final IPATranscript t = alts.get(transcriber.getUsername());
				retVal = (t == null ? new IPATranscript() : t);
			} else {
				retVal = new IPATranscript();
			}
		}
		return retVal;
	}

	@Override
	protected void setValidatedObject(IPATranscript object) {
		final Transcriber transcriber = getTranscriber();
		final IPATranscript groupVal = 
				(super.getGroupValue() != null ? super.getGroupValue() : new IPATranscript());
		if(object == null) object = new IPATranscript();
		
		if(transcriber != null) {
			AlternativeTranscript alts = groupVal.getExtension(AlternativeTranscript.class);
			if(alts == null) {
				alts = new AlternativeTranscript();
				groupVal.putExtension(AlternativeTranscript.class, alts);
			}
			alts.put(transcriber.getUsername(), object);
			super.setValidatedObject(groupVal);
		} else {
			// HACK make sure to copy alternative transcriptions
			final AlternativeTranscript alts = groupVal.getExtension(AlternativeTranscript.class);
			if(alts != null) object.putExtension(AlternativeTranscript.class, alts);
			super.setValidatedObject(object);
		}
	}
	
	@Override
	public JToolTip createToolTip() {
		JToolTip retVal = super.createToolTip();
		retVal.setLayout(new VerticalLayout());
		
		return retVal;
	}
	
	@Override
	protected void update() {
		if(syllabifier != null) {
			final IPATranscript validatedObj = getValidatedObject();
			IPATranscript ipa = validatedObj;
			if(validatedObj != null) {
				if(getTranscriber() != null) {
					final AlternativeTranscript alts = validatedObj.getExtension(AlternativeTranscript.class);
					ipa = alts.get(getTranscriber().getUsername());
				}
				syllabifier.syllabify(ipa.toList());
			}
		}
		super.update();
	}

	@Override
	protected boolean validateText() {
		getHighlighter().removeAllHighlights();
		
		if(getText().trim().length() == 0) {
			setValidatedObject(new IPATranscript());
			return true;
		}
		
		try {
			IPATranscript validatedIPA = IPATranscript.parseIPATranscript(getText());
			setValidatedObject(validatedIPA);
			((GroupFieldBorder)getBorder()).setShowWarningIcon(false);
			setToolTipText(null);
		} catch (final ParseException e) {
			IPATranscript validatedIPA = new IPATranscript();
			validatedIPA.putExtension(UnvalidatedValue.class, new UnvalidatedValue(getText().trim()));
			((GroupFieldBorder)getBorder()).setShowWarningIcon(true);
			
			final StringBuilder sb = new StringBuilder();
			sb.append("Error at position: ").append(e.getErrorOffset()).append(", ").append(e.getLocalizedMessage());
			setToolTipText(sb.toString());
			
			try {
				getHighlighter().addHighlight(e.getErrorOffset(), e.getErrorOffset()+1, new Highlighter.HighlightPainter() {
					
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
								b.y + b.height, 
								b.x + b.width, 
								b.y + b.height);
					}
				});
				setValidatedObject(validatedIPA);
				repaint();
			} catch (BadLocationException e2) {
				LOGGER
						.log(Level.SEVERE, e2.getLocalizedMessage(), e2);
			}
		}
		return true;
	}
	
}
