package ca.phon.session;

import java.util.Set;

import ca.phon.extensions.ExtensionSupport;
import ca.phon.extensions.IExtendable;
import ca.phon.visitor.Visitable;
import ca.phon.visitor.Visitor;

/**
 * Iteratable/visitable access for {@link Session} {@link Record}s
 *
 */
public abstract class Records implements IExtendable, Iterable<Record>, Visitable<Record> {

	private final ExtensionSupport extSupport = new ExtensionSupport(Records.class, this);

	protected Records() {
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
	public void accept(Visitor<Record> visitor) {
		for(Record r:this) {
			visitor.visit(r);
		}
	}
	
}
