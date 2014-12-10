package ca.phon.orthography;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Class for building {@link Orthography} from various sources.  This class
 * is <i>not</i> thread-safe.
 *
 */
public class OrthographyBuilder {
	
	/**
	 * Internal list of {@link OrthoElement}s
	 */
	private final List<OrthoElement> eleList = new ArrayList<OrthoElement>();

	public OrthographyBuilder() {
	}
	
	public OrthographyBuilder clear() {
		eleList.clear();
		return this;
	}
	
	public OrthographyBuilder append(Orthography ortho) {
		for(OrthoElement ele:ortho) eleList.add(ele);
		return this;
	}
	
	public OrthographyBuilder append(OrthoElement ele) {
		eleList.add(ele);
		return this;
	}
	
	/**
	 * 
	 * @param txt
	 * @return
	 * 
	 * @throws IllegalArgumentException if txt could not be compiled
	 */
	public OrthographyBuilder append(String txt) {
		try {
			final Orthography ortho = Orthography.parseOrthography(txt);
			eleList.addAll(ortho.toList());
		} catch (ParseException pe) {
			throw new IllegalArgumentException(pe);
		}
		return this;
	}
	
	public OrthographyBuilder appendWord(String data, WordPrefix prefix, WordSuffix suffix) {
		final OrthoWord word = new OrthoWord(data, prefix, suffix);
		eleList.add(word);
		return this;
	}
	
	public OrthographyBuilder appendWord(String data, WordPrefix prefix) {
		appendWord(data, prefix, null);
		return this;
	}
	
	public OrthographyBuilder appendWord(String data, WordSuffix suffix) {
		appendWord(data, null, suffix);
		return this;
	}
	
	public OrthographyBuilder appendWord(String data) {
		appendWord(data, null, null);
		return this;
	}
	
	public OrthographyBuilder appendComment(String type, String data) {
		final OrthoComment comment = new OrthoComment(type, data);
		eleList.add(comment);
		return this;
	}
	
	public OrthographyBuilder appendComment(String data) {
		appendComment(null, data);
		return this;
	}
	
	public OrthographyBuilder appendWordnet(OrthoWord word1, OrthoWord word2, OrthoWordnetMarker marker) {
		final OrthoWordnet wordnet = new OrthoWordnet(word1, word2, marker);
		eleList.add(wordnet);
		return this;
	}
	
	/**
	 * Creates a wordnet using the previously added word and the given word.
	 * 
	 * @param word2
	 * @param marker
	 * @return
	 * 
	 * @throws IllegalStateException if the previous element is not an {@link OrthoWord} or
	 *  the element list is empty
	 */
	public OrthographyBuilder createWordnet(OrthoWord word2, OrthoWordnetMarker marker) {
		if(eleList.size() == 0)
			throw new IllegalStateException("Unable to create wordnet from empty list");
		final OrthoElement prevEle = eleList.get(eleList.size()-1);
		if(!(prevEle instanceof OrthoWord))
			throw new IllegalStateException("Unable to create wordnet, previous element not a word.");
		return appendWordnet((OrthoWord)prevEle, word2, marker);
	}
	
	/**
	 * Creates a wordnet using the last two elements.
	 * 
	 * @param marker
	 * @return
	 * 
	 * @throws IllegalStateException if the previous two elements are not {@link OrthoWord}s or
	 *  the element list is empty 
	 */
	public OrthographyBuilder createWordnet(OrthoWordnetMarker marker) {
		if(eleList.size() < 2)
			throw new IllegalStateException("Unable to create wordnet, not enough elements.");
		final OrthoElement ele1 = eleList.get(eleList.size()-2);
		if(!(ele1 instanceof OrthoWord))
			throw new IllegalStateException("Unable to create wordnet, both elements must be words");
		final OrthoElement ele2 = eleList.get(eleList.size()-1);
		if(!(ele2 instanceof OrthoWord))
			throw new IllegalStateException("Unable to create wordnet, both elements must be words");
		return appendWordnet((OrthoWord)ele1, (OrthoWord)ele2, marker);
	}
	
	/**
	 * Creates a wordnet using the last two elements.
	 * 
	 * @return
	 * 
	 * @throws IllegalStateException if the previous two elements are not {@link OrthoWord}s or
	 *  the element list is empty 
	 */
	public OrthographyBuilder createWordnet() {
		return createWordnet(OrthoWordnetMarker.COMPOUND);
	}
	
	public OrthographyBuilder appendPunct(OrthoPunctType type) {
		final OrthoPunct punct = new OrthoPunct(type);
		eleList.add(punct);
		return this;
	}
	
	/**
	 * Append punctuation.
	 * @param punct
	 * @return
	 * 
	 * @throws IllegalArgumentException if the given text is not valid
	 */
	public OrthographyBuilder appendPunct(char punct) {	
		final OrthoPunctType type = OrthoPunctType.fromChar(punct);
		return appendPunct(type);
	}
	
	public int size() {
		return eleList.size();
	}
	
	public OrthoElement elementAt(int idx) {
		return eleList.get(idx);
	}
	
	public OrthographyBuilder appendEvent(String type, String data) {
		final OrthoEvent evt = new OrthoEvent(type, data);
		eleList.add(evt);
		return this;
	}
	
	public Orthography toOrthography() {
		return new Orthography(eleList);
	}
	
}
