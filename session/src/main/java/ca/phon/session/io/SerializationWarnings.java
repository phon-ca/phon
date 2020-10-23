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
package ca.phon.session.io;

import java.util.*;

import ca.phon.extensions.*;
import ca.phon.session.*;

@Extension(Session.class)
public class SerializationWarnings {
	
	private final List<SerializationWarning> warnings;
	
	public SerializationWarnings() {
		super();
		
		this.warnings = new ArrayList<>();
	}

	public int size() {
		return warnings.size();
	}

	public boolean isEmpty() {
		return warnings.isEmpty();
	}

	public boolean add(SerializationWarning e) {
		return warnings.add(e);
	}

	public boolean remove(Object o) {
		return warnings.remove(o);
	}

	public boolean addAll(Collection<? extends SerializationWarning> c) {
		return warnings.addAll(c);
	}

	public void clear() {
		warnings.clear();
	}

	public SerializationWarning get(int index) {
		return warnings.get(index);
	}

	public SerializationWarning remove(int index) {
		return warnings.remove(index);
	}

	public int indexOf(Object o) {
		return warnings.indexOf(o);
	}
	
	public List<SerializationWarning> getWarnings() {
		return Collections.unmodifiableList(this.warnings);
	}
	
}
