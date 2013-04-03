package ca.phon.session.impl;

import java.util.Set;

import ca.phon.extensions.ExtensionSupport;
import ca.phon.session.Comment;
import ca.phon.session.CommentEnum;

/**
 * Implemtation for comment elements.
 *
 */
public class CommentImpl implements Comment {
	
	private String value;
	
	private CommentEnum type;
	
	CommentImpl() {
		super();
		extSupport.initExtensions();
	}
	
	CommentImpl(String value, CommentEnum type) {
		super();
		this.value = value;
		this.type = type;
	}
	
	@Override
	public String getValue() {
		return (value == null ? "" : value);
	}

	@Override
	public void setValue(String comment) {
		this.value = comment;
	}

	@Override
	public CommentEnum getType() {
		return (type == null ? CommentEnum.Generic : type);
	}

	@Override
	public void setType(CommentEnum type) {
		this.type = type;
	}
	
	/**
	 * Extension support
	 */
	private final ExtensionSupport extSupport = new ExtensionSupport(Comment.class, this);

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

}
