package ca.phon.orthography;

import java.util.ArrayList;
import java.util.Set;

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
	
	public Orthography() {
		this("");
	}
	
	public Orthography(String ortho) {
		super();
		// TODO parse orthography
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
	

}
