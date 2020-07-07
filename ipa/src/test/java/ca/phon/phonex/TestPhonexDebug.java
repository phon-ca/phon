package ca.phon.phonex;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import ca.phon.fsa.FSATransition;
import ca.phon.fsa.SimpleFSADebugContext;
import ca.phon.ipa.IPAElement;
import ca.phon.ipa.IPATranscript;

@RunWith(JUnit4.class)
public class TestPhonexDebug {

	@Test
	public void testPhonexDebugContext() throws Exception {
		String phonex = "\u03c3+(\u03c3:tn(\"51\"))";
		PhonexPattern pattern = PhonexPattern.compile(phonex);
		
		PhonexFSA fsa = pattern.getFsa();
		SimpleFSADebugContext<IPAElement> debugCtx = new SimpleFSADebugContext<>(fsa);
		
		IPATranscript t = IPATranscript.parseIPATranscript("w:Oa:Nŋ⁵¹:Cɥ:Oɛ:Nn²¹:Cʨ:Oi:Nŋ⁵¹:C");
		debugCtx.reset(t.toList().toArray(new IPAElement[0]));
		
		FSATransition<IPAElement> transition = null;
		while((transition = debugCtx.step()) != null) {
			System.out.println(transition.getImage());
		}
	}
	
}
