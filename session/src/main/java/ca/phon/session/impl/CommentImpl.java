/*
 * Copyright (C) 2005-2020 Gregory Hedlund & Yvan Rose
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

import ca.phon.session.CommentType;
import ca.phon.session.spi.CommentSPI;

/**
 * Implemtation for comment elements.
 *
 */
public class CommentImpl implements CommentSPI {
	
	private String value;
	
	private CommentType type;
	
	CommentImpl() {
		super();
	}
	
	CommentImpl(CommentType type, String value) {
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

	public CommentType getType() { return this.type; }

	public void setType(CommentType type) { this.type = type; }

}
