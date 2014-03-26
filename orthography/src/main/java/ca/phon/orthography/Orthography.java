package ca.phon.orthography;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.TokenStream;

import ca.phon.extensions.ExtensionSupport;
import ca.phon.extensions.IExtendable;
import ca.phon.visitor.Visitable;
import ca.phon.visitor.Visitor;

/**
 * Container for orthographic transcriptions.
 */
public class Orthography extends ArrayList<OrthoElement> implements IExtendable, Visitable<OrthoElement> {
	
	private static final long serialVersionUID = 7468757586738978448L;
	private final ExtensionSupport extSupport = 
			new ExtensionSupport(Orthography.class, this);
	
	/**
	 * Parse the given text into a new {@link Orthography} object.
	 * 
	 * @text
	 */
	public static Orthography parseOrthography(String text) {
		return new Orthography(text);
	}
	
	public Orthography() {
		this("");
	}
	
	public Orthography(Collection<? extends OrthoElement> orthoEles) {
		super(orthoEles);
	}
	
	public Orthography(String ortho) {
		super();
		
		final OrthoTokenSource tokenSource = new OrthoTokenSource(ortho);
		final TokenStream tokenStream = new CommonTokenStream(tokenSource);
		final OrthographyParser parser = new OrthographyParser(tokenStream);
		parser.setOrthography(this);
		try {
			parser.orthography();
		} catch (RecognitionException e) {
			throw new IllegalArgumentException(ortho, e);
		}
		
		extSupport.initExtensions();
	}
	
	@Override
	public Set<Class<?>> getExtensions() {
		return extSupport.getExtensions();
	}

	@Override
	public <T> T getExtension(Class<T> cap) {
		return extSupport.getExtension(cap);
	}

	@Override
	public <T> T putExtension(Class<T> cap, T impl) {
		return extSupport.putExtension(cap, impl);
	}

	@Override
	public <T> T removeExtension(Class<T> cap) {
		return extSupport.removeExtension(cap);
	}

	@Override
	public void accept(Visitor<OrthoElement> visitor) {
		for(int i = 0; i < size(); i++) {
			final OrthoElement ele = get(i);
			visitor.visit(ele);
		}
	}
	
	@Override
	public String toString() {
		final StringBuffer buffer = new StringBuffer();
		
		for(OrthoElement ele:this) {
			buffer.append(
					(buffer.length() > 0 ? " " : "") + ele.text());
		}
		
		return buffer.toString();
	}
}
