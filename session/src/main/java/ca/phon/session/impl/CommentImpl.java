/*
 * Copyright (C) 2012-2018 Gregory Hedlund & Yvan Rose
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at

 *    http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
