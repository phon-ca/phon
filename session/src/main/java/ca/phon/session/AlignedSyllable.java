package ca.phon.session;

import java.util.Set;

import ca.phon.extensions.*;
import ca.phon.ipa.IPATranscript;

public abstract class AlignedSyllable implements IExtendable {
	
	private final ExtensionSupport extSupport = new ExtensionSupport(AlignedSyllable.class, this);
	
	public AlignedSyllable() {
		super();
		
		extSupport.initExtensions();
	}
	
	public abstract Group getGroup();
	
	public abstract Word getWord();
	
	public abstract int getGroupIndex();

	public abstract int getWordIndex();
	
	public abstract int getSyllableIndex();
	
	public abstract IPATranscript getIPATarget();
	
	public abstract IPATranscript getIPAActual();
	
	public abstract int getIPATargetLocation();
	
	public abstract int getIPAActualLocation();

	public Set<Class<?>> getExtensions() {
		return extSupport.getExtensions();
	}

	public <T> T getExtension(Class<T> cap) {
		return extSupport.getExtension(cap);
	}

	public <T> T putExtension(Class<T> cap, T impl) {
		return extSupport.putExtension(cap, impl);
	}

	public <T> T removeExtension(Class<T> cap) {
		return extSupport.removeExtension(cap);
	}
	
}
