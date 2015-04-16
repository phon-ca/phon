/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2015 The Phon Project, Memorial University <https://phon.ca>
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ca.phon.session.io.xml.v12;

import ca.phon.ipa.IPAElement;
import ca.phon.ipa.WordBoundary;
import ca.phon.syllable.SyllabificationInfo;
import ca.phon.syllable.SyllableConstituentType;
import ca.phon.visitor.VisitorAdapter;
import ca.phon.visitor.annotation.Visits;

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
