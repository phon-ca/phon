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
package ca.phon.app.opgraph.nodes;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import ca.phon.session.Session;

public class SessionRecordContainer implements RecordContainer {
	
	private Session session;
	
	public SessionRecordContainer(Session session) {
		super();
		this.session = session;
	}

	@Override
	public Session getSession() {
		return this.session;
	}

	@Override
	public Iterator<Integer> idxIterator() {
		List<Integer> idxList = new ArrayList<>();
		for(int i = 0; i < session.getRecordCount(); i++) {
			idxList.add(i);
		}
		return idxList.iterator();
	}

}
