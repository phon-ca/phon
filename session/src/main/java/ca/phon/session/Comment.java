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

import ca.phon.extensions.*;
import ca.phon.session.spi.*;

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
	 * Get the tag for the comment.
	 * @return CommentEnum
	 */
	public String getTag() {
		return commentImpl.getTag();
	}
	
	/**
	 * Set the tag for the comment, cannot be <code>null</code>
	 * @param type
	 * 
	 * @throws NullPointerException if tag is <code>null</code>
	 */
	public void setTag(String tag) {
		commentImpl.setTag(tag);
	}

}
