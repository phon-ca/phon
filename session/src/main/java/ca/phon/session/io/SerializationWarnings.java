package ca.phon.session.io;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import ca.phon.extensions.Extension;
import ca.phon.session.Session;

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
