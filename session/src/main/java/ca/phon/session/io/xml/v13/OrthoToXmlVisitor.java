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
package ca.phon.session.io.xml.v13;

import ca.phon.orthography.*;
import ca.phon.visitor.VisitorAdapter;

/**
 *
 */
public class OrthoToXmlVisitor extends VisitorAdapter<OrthographyElement> {
	
	private final ObjectFactory factory = new ObjectFactory();
	
	private GroupType gt;
	
	public OrthoToXmlVisitor() {
		gt = factory.createGroupType();
	}
	
	public GroupType getGroup() {
		return this.gt;
	}

	@Override
	public void fallbackVisit(OrthographyElement obj) {

	}


}
