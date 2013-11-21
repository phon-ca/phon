package ca.phon.session;

import java.util.Set;

import ca.phon.extensions.ExtensionSupport;
import ca.phon.extensions.IExtendable;
import ca.phon.visitor.Visitable;
import ca.phon.visitor.Visitor;

/**
 * Helper class providing iterator and visitor methods
 * for {@link Session} {@link Participant}s.
 */
public abstract class Participants implements Iterable<Participant>, IExtendable, Visitable<Participant> {

	protected Participants() {
		super();
		extSupport.initExtensions();
	}

	/**
	 * Extension support
	 */
	private final ExtensionSupport extSupport = new ExtensionSupport(Participants.class, this);

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
	public void accept(Visitor<Participant> visitor) {
		for(Participant p:this) {
			visitor.visit(p);
		}
	}
	
}
