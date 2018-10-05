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
package ca.phon.ipa;

import ca.phon.visitor.VisitorAdapter;
import ca.phon.visitor.annotation.Visits;

public class DiacriticFilter extends VisitorAdapter<IPAElement> {

	final IPATranscriptBuilder builder = new IPATranscriptBuilder();
	
	public IPATranscript getIPATranscript() {
		return builder.toIPATranscript();
	}
	
	@Override
	public void fallbackVisit(IPAElement obj) {
		builder.append(obj);
	}
	
	@Visits
	public void visitPhone(Phone phone) {
		builder.append(phone.getBase());
	}
	
	@Visits
	public void visitCompoundPhone(CompoundPhone phone) {
		visit(phone.getFirstPhone());
		visit(phone.getSecondPhone());
		builder.makeCompoundPhone(phone.getLigature());
	}
	
}
