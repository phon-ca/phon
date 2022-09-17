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
package ca.phon.phonex;

import ca.phon.fsa.*;
import ca.phon.ipa.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Arrays;

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
