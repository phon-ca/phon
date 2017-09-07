package ca.phon.ipa;

import java.util.*;
import java.util.regex.*;

import org.antlr.runtime.*;
import org.antlr.runtime.tree.*;

import ca.phon.phonex.*;
import ca.phon.syllable.SyllabificationInfo;
import ca.phon.visitor.VisitorAdapter;
import ca.phon.visitor.annotation.Visits;

/**
 * Performs the cover operation on a {@link IPATranscript}
 *
 */
public class CoverVisitor extends VisitorAdapter<IPAElement> {
	
	private final IPATranscriptBuilder builder = new IPATranscriptBuilder();

	private final IPAElementFactory factory = new IPAElementFactory();
	
	private List<PhoneMatcher> matchers;
	
	private Map<PhoneMatcher, Character> symbolMap;
	
	private boolean includeStress = true;
	
	private boolean includeSyllableBoundaries = true;
	
	private boolean includeLength = true;

	public CoverVisitor(String symbolMap) {
		this(symbolMap, true, true, true);
	}
	
	/**
	 * Parse symbolMap
	 * 
	 * Format: 
	 * 
	 * <Character>=<Phone Matcher>; ...
	 * 
	 * @param symbolMap
	 * @param includeStress
	 * @param includeSyllableBoundaries
	 * @param includeLength
	 */
	public CoverVisitor(String symbolMap, boolean includeStress, boolean includeSyllableBoundaries, boolean includeLength) {
		super();
		
		this.symbolMap = parseSymbolMap(symbolMap);
		this.matchers = new ArrayList<>(this.symbolMap.keySet());
		this.includeStress = includeStress;
		this.includeSyllableBoundaries = includeSyllableBoundaries;
		this.includeLength = includeLength;
	}

	private Map<PhoneMatcher, Character> parseSymbolMap(String symbolMap) {
		final Map<PhoneMatcher, Character> retVal = new LinkedHashMap<>();
		
		final String regex = "(([A-Z]|stress|syllableBoundaries|length))\\s?=\\s?([^;]+)";
		final Pattern pattern = Pattern.compile(regex);
		final Matcher matcher = pattern.matcher(symbolMap);
		
		while(matcher.find()) {
			final String g1 = matcher.group(1);
			final String g2 = matcher.group(3);
			
			if(g1.length() == 1) {
				CharStream exprStream = new ANTLRStringStream(g2);
				PhonexLexer lexer = new PhonexLexer(exprStream);
				CommonTokenStream tokenStream = new CommonTokenStream(lexer);
				PhonexParser parser = new PhonexParser(tokenStream);
				
				PhonexParser.single_phone_matcher_return exprVal;
				try {
					exprVal = parser.single_phone_matcher();
					
					CommonTree exprTree = CommonTree.class.cast(exprVal.getTree());
					CommonTreeNodeStream noes = new CommonTreeNodeStream(exprTree);
					PhonexCompiler compiler = new PhonexCompiler(noes);
					
					final PhoneMatcher phoneMatcher = compiler.single_phone_matcher();
					retVal.put(phoneMatcher, g1.charAt(0));
				} catch(RecognitionException re) {
					throw new IllegalArgumentException(symbolMap, re);
				}
			} else {
				if(g1.matches("stress")) {
					Boolean includeStress = Boolean.parseBoolean(g2);
					setIncludeStress(includeStress);
				} else if(g1.matches("syllableBoundaries")) {
					Boolean includeSyllableBoundaries = Boolean.parseBoolean(g2);
					setIncludeSyllableBoundaries(includeSyllableBoundaries);
				} else if(g1.matches("length")) {
					Boolean includeLength = Boolean.parseBoolean(g2);
					setIncludeLength(includeLength);
				}
			}
		}
		
		return retVal;
	}
	
	public CoverVisitor(List<PhoneMatcher> matchers, Map<PhoneMatcher, Character> symbolMap) {
		this(matchers, symbolMap, true, true, true);
	}
	
	public CoverVisitor(List<PhoneMatcher> matchers, Map<PhoneMatcher, Character> symbolMap,
			boolean includeStress, boolean includeSyllableBoundaries, boolean includeLength) {
		super();
		
		this.matchers = matchers;
		this.symbolMap = symbolMap;
		this.includeStress = includeStress;
		this.includeSyllableBoundaries = includeSyllableBoundaries;
		this.includeLength = includeLength;
	}
	
	@Override
	public void fallbackVisit(IPAElement obj) {
		builder.append(obj.toString());
		
		builder.last().setFeatureSet(obj.getFeatureSet());
		copySyllabificationInfo(obj, builder.last());
	}
	
	public List<PhoneMatcher> getMatchers() {
		return matchers;
	}

	public void setMatchers(List<PhoneMatcher> matchers) {
		this.matchers = matchers;
	}

	public Map<PhoneMatcher, Character> getSymbolMap() {
		return symbolMap;
	}

	public void setSymbolMap(Map<PhoneMatcher, Character> symbolMap) {
		this.symbolMap = symbolMap;
	}

	public boolean isIncludeStress() {
		return includeStress;
	}

	public void setIncludeStress(boolean includeStress) {
		this.includeStress = includeStress;
	}

	public boolean isIncludeSyllableBoundaries() {
		return includeSyllableBoundaries;
	}

	public void setIncludeSyllableBoundaries(boolean includeSyllableBoundaries) {
		this.includeSyllableBoundaries = includeSyllableBoundaries;
	}

	public boolean isIncludeLength() {
		return includeLength;
	}

	public void setIncludeLength(boolean includeLength) {
		this.includeLength = includeLength;
	}

	private void copySyllabificationInfo(IPAElement ele, IPAElement dest) {
		final SyllabificationInfo info = ele.getExtension(SyllabificationInfo.class);
		final SyllabificationInfo destInfo = new SyllabificationInfo(dest);
		
		destInfo.setConstituentType(info.getConstituentType());
		destInfo.setDiphthongMember(info.isDiphthongMember());
		destInfo.setStress(info.getStress());
		destInfo.setToneFeatures(info.getToneFeatures());
		dest.putExtension(SyllabificationInfo.class, destInfo);
	}
	
	private Phone cover(Phone p) {
		final Optional<PhoneMatcher> matcher =
				matchers.stream().filter( (m) -> m.matches(p) ).findFirst();
		
		Character baseChar = p.getBasePhone();
		if(matcher.isPresent()) {
			final Character coverSymbol = symbolMap.get(matcher.get());
			if(coverSymbol != null)
				baseChar = coverSymbol;
		}
		
		final Phone newPhone = factory.createPhone(baseChar);
		newPhone.setFeatureSet(p.getFeatureSet());
		copySyllabificationInfo(p, newPhone);
				
		if(includeLength) {
			newPhone.setSuffixDiacritics(p.getLengthDiacritics());
		}
		
		return newPhone;
	}
	
	@Visits
	public void visitWordBoundary(WordBoundary wb) {
		builder.appendWordBoundary();
	}
	
	@Visits
	public void visitStressMarker(StressMarker marker) {
		if(includeStress) {
			builder.append(factory.createStress(marker.getType()));
		}
	}
	
	@Visits
	public void visitSyllableBoundary(SyllableBoundary sb) {
		if(includeSyllableBoundaries) {
			builder.appendSyllableBoundary();
		}
	}
	
	@Visits
	public void visitCompoundPhone(CompoundPhone cp) {
		final Phone c1 = cover(cp.getFirstPhone());
		final Phone c2 = cover(cp.getSecondPhone());
		
		final CompoundPhone newCp = factory.createCompoundPhone();
		newCp.setFirstPhone(c1);
		newCp.setSecondPhone(c2);
		newCp.setLigature(cp.getLigature());
		
		copySyllabificationInfo(cp, newCp);
		
		builder.append(newCp);
	}
	
	@Visits
	public void visitPhone(Phone p) {
		builder.append(cover(p));
	}
	
	public IPATranscript getIPATranscript() {
		return builder.toIPATranscript();
	}

}
