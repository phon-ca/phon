package ca.phon.app.ipalookup;

import java.util.Collections;

import ca.phon.ipadictionary.IPADictionary;
import ca.phon.orthography.OrthoComment;
import ca.phon.orthography.OrthoElement;
import ca.phon.orthography.OrthoWord;
import ca.phon.orthography.OrthoWordnet;
import ca.phon.orthography.Orthography;
import ca.phon.visitor.VisitorAdapter;
import ca.phon.visitor.annotation.Visits;

/**
 * Produce a set of suggested transcriptions for a given {@link Orthography}.
 */
public class OrthoLookupVisitor extends VisitorAdapter<OrthoElement> {
	
	private final IPADictionary dictionary;
	
	public OrthoLookupVisitor(IPADictionary dict) {
		this.dictionary = dict;
	}
	
	@Override
	public void visit(OrthoElement ele) {
		if(dictionary != null)
			super.visit(ele);
	}
	
	@Override
	public void fallbackVisit(OrthoElement obj) {
	}
	
	@Visits
	public void visitWord(OrthoWord word) {
		updateAnnotation(word);
	}
	
	@Visits
	public void visitWordnet(OrthoWordnet wordnet) {
		final OrthoWord word1 = wordnet.getWord1();
		visitWord(word1);
		final OrthoWord word2 = wordnet.getWord2();
		visitWord(word2);
	}
	
	private String[] lookup(String ortho) {
		String[] retVal = dictionary.lookup(ortho);
		if(retVal.length == 0)
			retVal = new String[]{ "*" };
		return retVal;
	}
	
	@Visits
	public void visitComment(OrthoComment comment) {
		// check for pauses
		final String commentTxt = comment.getData();
		if(commentTxt.matches("\\.{1,3}")) {
			final OrthoWordIPAOptions opts = new OrthoWordIPAOptions(Collections.singletonList("(" + commentTxt + ")"));
			opts.setDictLang(dictionary.getLanguage());
			opts.setSelectedOption(0);
			comment.putExtension(OrthoWordIPAOptions.class, opts);
		}
	}
	
	private OrthoWordIPAOptions updateAnnotation(OrthoWord word) {
		OrthoWordIPAOptions ext = word.getExtension(OrthoWordIPAOptions.class);
		if(ext == null || ext.getDictLang() != dictionary.getLanguage()) {
			String[] opts = lookup(word.getWord());
			ext = new OrthoWordIPAOptions(opts);
			ext.setDictLang(dictionary.getLanguage());
			if(opts.length > 0) ext.setSelectedOption(0);
			word.putExtension(OrthoWordIPAOptions.class, ext);
		}
		return ext;
	}
	
}