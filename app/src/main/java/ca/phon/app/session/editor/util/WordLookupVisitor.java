package ca.phon.app.session.editor.util;

import java.text.ParseException;
import java.util.logging.Level;

import ca.phon.ipa.IPATranscript;
import ca.phon.ipa.WordBoundary;
import ca.phon.ipadictionary.IPADictionary;
import ca.phon.orthography.OrthoElement;
import ca.phon.orthography.OrthoWord;
import ca.phon.orthography.OrthoWordnet;
import ca.phon.visitor.VisitorAdapter;
import ca.phon.visitor.annotation.Visits;

public class WordLookupVisitor extends VisitorAdapter<OrthoElement> {

	/**
	 * 
	 */
	private final RecordLookupPanel recordLookupPanel;

	/**
	 * @param recordLookupPanel
	 */
	WordLookupVisitor(RecordLookupPanel recordLookupPanel) {
		this.recordLookupPanel = recordLookupPanel;
	}

	@Override
	public void fallbackVisit(OrthoElement obj) {
	}
	
	@Visits
	public void visitWord(OrthoWord word) {
		final OrthoWordIPAOptions ext = updateAnnotation(word);
		
		final String txt = (ext.getOptions().size() > 0 ? ext.getOptions().get(ext.getSelectedOption()) : "*");
		addWordToTier(txt);
	}
	
	@Visits
	public void visitCompoundWord(OrthoWordnet wordnet) {
		final OrthoWordIPAOptions opt1 = updateAnnotation(wordnet.getWord1());
		final OrthoWordIPAOptions opt2 = updateAnnotation(wordnet.getWord2());
		
		final String t1 = (opt1.getOptions().size() > 0 ? opt1.getOptions().get(opt1.getSelectedOption()) : "*");
		final String t2 = (opt2.getOptions().size() > 0 ? opt2.getOptions().get(opt2.getSelectedOption()) : "*");
		addWordToTier(t1 + wordnet.getMarker().toString() + t2);
	}

	private void addWordToTier(String txt) {
		try {
			int grpIdx = recordLookupPanel.lookupTier.numberOfGroups()-1;
			IPATranscript grp = recordLookupPanel.lookupTier.getGroup(grpIdx);
			grp = grp.append(IPATranscript.parseIPATranscript(
					(grp.size() > 0 ? " " : "") + txt));
			recordLookupPanel.lookupTier.setGroup(grpIdx, grp);
		} catch (ParseException e) {
			RecordLookupPanel.LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}
	}
	
	private OrthoWordIPAOptions updateAnnotation(OrthoWord word) {
		final IPADictionary ctx = this.recordLookupPanel.getDictionary();
		if(ctx == null) return new OrthoWordIPAOptions();
		
		OrthoWordIPAOptions ext = word.getExtension(OrthoWordIPAOptions.class);
		if(ext == null) {
			final String[] opts = ctx.lookup(word.getWord());
			ext = new OrthoWordIPAOptions(opts);
			if(opts.length > 0) ext.setSelectedOption(0);
			word.putExtension(OrthoWordIPAOptions.class, ext);
		}
		return ext;
	}

}
