package ca.phon.session;

import java.util.Set;

import ca.phon.extensions.ExtensionSupport;
import ca.phon.extensions.IExtendable;
import ca.phon.visitor.Visitable;
import ca.phon.visitor.Visitor;

/**
 * Iterator/vistor access for {@link Session} {@link Transcriber}s
 *
 */
public abstract class Transcribers implements IExtendable, Iterable<Transcriber>, Visitable<Transcriber> {

	/**
	 * Extension support
	 */
	private final ExtensionSupport extSupport = new ExtensionSupport(Transcribers.class, this);

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
	public void accept(Visitor<Transcriber> visitor) {
		for(Transcriber transcriber:this) {
			visitor.visit(transcriber);
		}
	}
	
}
