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
		for(int i = currentIndex; i <= currentIndex + obj.getText().length(); i++) {
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
	}
	
}
