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

import ca.phon.orthography.*;
import ca.phon.util.Tuple;
import ca.phon.visitor.*;
import ca.phon.visitor.annotation.Visits;

import java.util.*;
import java.util.stream.Collectors;

public class OrthographyMorphemeVisitor extends VisitorAdapter<OrthoElement> {

	private int wrdIdx = 0;

	private List<Tuple<Integer, OrthoElement>> morphemeList = new ArrayList<>();

	public OrthoElement[] getMorphemes() {
		return morphemeList.stream()
				.map(t -> t.getObj2())
				.collect(Collectors.toList())
				.toArray(new OrthoElement[0]);
	}

	public Integer[] getMorphemeIndexes() {
		return morphemeList.stream()
				.map(t -> t.getObj1().intValue())
				.collect(Collectors.toList())
				.toArray(new Integer[0]);
	}

	@Override
	public void fallbackVisit(OrthoElement obj) {

	}

	@Visits
	public void visitWordnet(OrthoWordnet wordnet) {
		visit(wordnet.getWord1());
		visit(wordnet.getWord2());
	}

	@Visits
	public void visitOrthoWord(OrthoWord word) {
		morphemeList.add(new Tuple<>(wrdIdx, word));
		wrdIdx += word.toString().length() + 1;
	}

}
