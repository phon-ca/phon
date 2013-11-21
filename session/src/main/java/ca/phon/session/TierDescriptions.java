package ca.phon.session;

import java.util.Set;

import ca.phon.extensions.ExtensionSupport;
import ca.phon.extensions.IExtendable;
import ca.phon.visitor.Visitable;
import ca.phon.visitor.Visitor;

/**
 * Iterable/visitor access for {@link Session} {@link TierDescription}s.
 */
public abstract class TierDescriptions implements IExtendable, Iterable<TierDescription>, Visitable<TierDescription> {

	/**
	 * Extension support
	 */
	private final ExtensionSupport extSupport = new ExtensionSupport(TierDescriptions.class, this);
	
	protected TierDescriptions() {
		super();
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
	public void accept(Visitor<TierDescription> visitor) {
		for(TierDescription td:this) {
			visitor.visit(td);
		}
	}
	
}
