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
package ca.phon.session.io.xml.v12;

import ca.phon.ipa.*;
import ca.phon.syllable.*;
import ca.phon.visitor.*;
import ca.phon.visitor.annotation.*;

/**
 * 
 */
public class IpaToXmlVisitor extends VisitorAdapter<IPAElement> {
	
	private final ObjectFactory factory = new ObjectFactory();
	
	private PhoType pho;
	private WordType currentWord;
	private int currentIndex = 0;
	
	public IpaToXmlVisitor() {
		this.pho = factory.createPhoType();
		final SyllabificationType sb = factory.createSyllabificationType();
		this.pho.setSb(sb);
		this.currentWord = factory.createWordType();
		currentWord.setContent("");
		this.pho.getW().add(currentWord);
	}
	
	public PhoType getPho() {
		return this.pho;
	}

	@Override
	public void fallbackVisit(IPAElement obj) {
		final ConstituentType ct = factory.createConstituentType();
		for(int i = currentIndex; i < currentIndex + obj.getText().length(); i++) {
			ct.getIndexes().add(i);
		}
		currentIndex += obj.getText().length();
		
		final ConstituentTypeType ctType = ConstituentTypeType.fromValue(obj.getScType().getIdentifier());
		ct.setScType(ctType);
		
		final SyllabificationInfo scInfo = obj.getExtension(SyllabificationInfo.class);
		if(scInfo.getConstituentType() == SyllableConstituentType.NUCLEUS && scInfo != null) {
			ct.setHiatus(!scInfo.isDiphthongMember());
		}
		pho.getSb().getPh().add(ct);
		
		currentWord.setContent(currentWord.getContent() + obj.getText());
	}
	
	@Visits
	public void visitWordboundary(WordBoundary wb) {
		this.currentWord = factory.createWordType();
		this.currentWord.setContent("");
		this.pho.getW().add(currentWord);
		final ConstituentType ct = factory.createConstituentType();
		ct.setScType(ConstituentTypeType.WB);
		ct.getIndexes().add(currentIndex++);
		this.pho.getSb().getPh().add(ct);
	}
	
}
