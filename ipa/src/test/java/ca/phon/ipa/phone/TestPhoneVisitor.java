package ca.phon.ipa.phone;

import ca.phon.ipa.IPATranscript;
import ca.phon.visitor.VisitorAdapter;
import ca.phon.visitor.annotation.Visits;
import junit.framework.TestCase;

/**
 * Test visitor pattern for phones.
 *
 */
public class TestPhoneVisitor extends TestCase {
	
	public void testRealPhoneVisitor() {
		IPATranscript transcript = IPATranscript.fromString("test com\u035car");
		transcript.accept(new RealPhoneVisitor());
	}
		
	public class RealPhoneVisitor extends VisitorAdapter<Phone> {

		@Override
		public void fallbackVisit(Phone obj) {
		}
		
		@Visits
		public void visitCompoundPhone(CompoundPhone phone) {
			System.out.print("CP:" + phone.toString());
		}
		
		@Visits
		public void visitBasicPhone(BasicPhone phone) {
			System.out.print(phone.toString());
		}
		
		public void visitWordBoundary(WordBoundary b) {
			System.out.print(" ");
		}
		
		@Visits
		public void visitWB(WordBoundary b) {
			
		}
	}

}
