package ca.phon.phonex;

import java.util.*;

import org.junit.*;
import org.junit.runner.*;
import org.junit.runners.*;

import ca.phon.fsa.*;
import ca.phon.ipa.*;

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
		printState(debugCtx.getMachineState());
		while((transition = debugCtx.step()) != null) {
			System.out.println("****");
			printTransition(transition);
			printState(debugCtx.getMachineState());
		}
	}

	private void printState(FSAState<IPAElement> state) {
		System.out.println("Tape:\t" + Arrays.toString(state.getTape()));
		System.out.println("Index:\t" + state.getTapeIndex());
		System.out.println("State:\t" + state.getCurrentState());
	}
	
	private void printTransition(FSATransition<IPAElement> trans) {
		System.out.println(String.format("%s - %s -> %s", trans.getFirstState(), trans.getImage(), trans.getToState()));
	}
	
}
