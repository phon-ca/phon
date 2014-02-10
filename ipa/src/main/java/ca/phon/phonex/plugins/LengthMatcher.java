package ca.phon.phonex.plugins;

import java.text.ParseException;

import ca.phon.ipa.CompoundPhone;
import ca.phon.ipa.IPAElement;
import ca.phon.ipa.Phone;
import ca.phon.phonex.PhoneMatcher;
import ca.phon.visitor.VisitorAdapter;
import ca.phon.visitor.annotation.Visits;

public class LengthMatcher implements PhoneMatcher {
	
	private float length = 0.0f;
	
	public LengthMatcher() {
		super();
	}

	public LengthMatcher(String len) {
		this(Float.parseFloat(len));
	}
	
	public LengthMatcher(float len) {
		super();
		this.length = len;
	}
	
	@Override
	public boolean matches(IPAElement p) {
		final LengthVisitor visitor = new LengthVisitor();
		visitor.visit(p);
		return visitor.matches;
	}

	@Override
	public boolean matchesAnything() {
		return false;
	}
	
	public void setLength(float length) {
		this.length = length;
	}
	
	public float getLength() {
		return this.length;
	}
	
	private class LengthVisitor extends VisitorAdapter<IPAElement> {

		private boolean matches = false;
		
		@Override
		public void fallbackVisit(IPAElement obj) {
		}
		
		@Visits
		public void visitPhone(Phone p) {
			final float phoneLength = p.getLength();
			matches = phoneLength == getLength();
		}
		
		@Visits
		public void visitCompoundPhone(CompoundPhone cp) {
			visit(cp.getFirstPhone());
			visit(cp.getSecondPhone());
		}
	}

}
