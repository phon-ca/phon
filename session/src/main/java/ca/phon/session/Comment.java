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

package ca.phon.session;

import java.util.Set;

import ca.phon.extensions.ExtendableObject;
import ca.phon.extensions.ExtensionSupport;
import ca.phon.extensions.IExtendable;
import ca.phon.session.spi.CommentSPI;

/**
 * Entity class for comments.
 * 
 *
 */
public final class Comment extends ExtendableObject {
	
	private CommentSPI commentImpl;
	
	Comment(CommentSPI impl) {
		super();
		this.commentImpl = impl;
	}
	
	/** 
	 * Get the comment string.
	 * @return String
	 */
	public String getValue() {
		return commentImpl.getValue();
	}
	
	/**
	 * Set the comment string.
	 * @param comment
	 */
	public void setValue(String comment) {
		commentImpl.setValue(comment);
	}
	
	/**
	 * Get the type.
	 * @return CommentEnum
	 */
	public CommentEnum getType() {
		return commentImpl.getType();
	}
	
	/**
	 * Set the type.
	 * @param type
	 */
	public void setType(CommentEnum type) {
		commentImpl.setType(type);
	}

}
