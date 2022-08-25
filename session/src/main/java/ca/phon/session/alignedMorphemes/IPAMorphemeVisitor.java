/*
 * Copyright (C) 2005-2022 Gregory Hedlund & Yvan Rose
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

package ca.phon.session.alignedMorphemes;

import ca.phon.ipa.*;
import ca.phon.visitor.VisitorAdapter;
import ca.phon.visitor.annotation.Visits;

import java.util.*;

public class IPAMorphemeVisitor extends VisitorAdapter<IPAElement> {

	private List<IPATranscript> morphemeList = new ArrayList<>();

	private IPATranscriptBuilder builder = new IPATranscriptBuilder();

	public IPATranscript[] getMorphemes() {
		if(builder.size() > 0) {
			createMorpheme();
		}
		return morphemeList.toArray(new IPATranscript[0]);
	}

	@Override
	public void fallbackVisit(IPAElement obj) {
		this.builder.append(obj);
	}

	private void createMorpheme() {
		if(builder.toIPATranscript().length() > 0) {
			morphemeList.add(this.builder.toIPATranscript());
			this.builder = new IPATranscriptBuilder();
		}
	}

	@Visits
	public void visitCompoundWordMarker(CompoundWordMarker cwm) {
		createMorpheme();
	}

	@Visits
	public void visitIntonationGroup(IntonationGroup ig) {
		createMorpheme();
	}

	@Visits
	public void visitWordBoundary(WordBoundary wb) {
		createMorpheme();
	}

}
