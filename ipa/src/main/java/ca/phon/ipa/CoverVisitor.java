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

import java.util.*;
import java.util.regex.*;

import org.antlr.runtime.*;
import org.antlr.runtime.tree.*;

import ca.phon.phonex.*;
import ca.phon.syllable.*;
import ca.phon.visitor.*;
import ca.phon.visitor.annotation.*;

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
	
	private boolean insertImplicitSyllableBoundaries = false;
	
	private boolean includeDiacritics = true;

	public CoverVisitor(String symbolMap) {
		this(symbolMap, true, true, false, true);
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
	 * @param ioncludeDiacritics
	 */
	public CoverVisitor(String symbolMap, boolean includeStress, boolean includeSyllableBoundaries,
			boolean insertImplicitSyllableBoundaries, boolean includeDiacritics) {
		super();
		
		this.includeStress = includeStress;
		this.includeSyllableBoundaries = includeSyllableBoundaries;
		this.insertImplicitSyllableBoundaries = insertImplicitSyllableBoundaries;
		this.includeDiacritics = includeDiacritics;

		this.symbolMap = parseSymbolMap(symbolMap);
		this.matchers = new ArrayList<>(this.symbolMap.keySet());
	}

	private Map<PhoneMatcher, Character> parseSymbolMap(String symbolMap) {
		final Map<PhoneMatcher, Character> retVal = new LinkedHashMap<>();
		
		final String regex = "(([A-Z]|stress|syllableBoundaries|implicitBoundaries|diacritics))\\s?=\\s?([^;]+)";
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
				} else if(g1.matches("implicitBoundaries")) {
					Boolean insertImplicitBoundaries = Boolean.parseBoolean(g2);
					setInsertImplicitSyllableBoundaries(insertImplicitBoundaries);
				} else if(g1.matches("diacritics")) {
					Boolean includeDiacritics = Boolean.parseBoolean(g2);
					setIncludeDiacritics(includeDiacritics);
				}
			}
		}
		
		return retVal;
	}
	
	public CoverVisitor(List<PhoneMatcher> matchers, Map<PhoneMatcher, Character> symbolMap) {
		this(matchers, symbolMap, true, true, true);
	}
	
	public CoverVisitor(List<PhoneMatcher> matchers, Map<PhoneMatcher, Character> symbolMap,
			boolean includeStress, boolean includeSyllableBoundaries, boolean includeDiacritics) {
		super();
		
		this.matchers = matchers;
		this.symbolMap = symbolMap;
		this.includeStress = includeStress;
		this.includeSyllableBoundaries = includeSyllableBoundaries;
		this.includeDiacritics = includeDiacritics;
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
	
	public boolean isInsertImplicitSyllableBoundaries() {
		return insertImplicitSyllableBoundaries;
	}

	public void setInsertImplicitSyllableBoundaries(boolean insertImplicitSyllableBoundaries) {
		this.insertImplicitSyllableBoundaries = insertImplicitSyllableBoundaries;
	}

	public boolean isIncludeDiacritics() {
		return includeDiacritics;
	}

	public void setIncludeDiacritics(boolean includeDiacritics) {
		this.includeDiacritics = includeDiacritics;
	}

	private void copySyllabificationInfo(IPAElement ele, IPAElement dest) {
		final SyllabificationInfo info = ele.getExtension(SyllabificationInfo.class);
		final SyllabificationInfo destInfo = new SyllabificationInfo(dest);
		
		destInfo.setConstituentType(info.getConstituentType());
		destInfo.setDiphthongMember(info.isDiphthongMember());
		destInfo.setStress(info.getStress());
		destInfo.setToneNumber(info.getToneNumber());
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
		if(includeDiacritics) {
			newPhone.setFeatureSet(p.getFeatureSet());
			newPhone.setPrefixDiacritics(p.getPrefixDiacritics());
			newPhone.setCombiningDiacritics(p.getCombiningDiacritics());
			newPhone.setSuffixDiacritics(p.getSuffixDiacritics());
		}
		copySyllabificationInfo(p, newPhone);
				
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
	public void visitIntraWordPause(IntraWordPause wp) {
		if(includeSyllableBoundaries) {
			builder.append(new IntraWordPause());
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
		IPATranscript retVal = builder.toIPATranscript();
		if(insertImplicitSyllableBoundaries) {
			final IPATranscriptBuilder buffer = new IPATranscriptBuilder();
			List<IPATranscript> sylls = retVal.syllables();
			for(int syllIdx = 0; syllIdx < sylls.size(); syllIdx++) {
				final IPATranscript syll = sylls.get(syllIdx);
				final Segregated seg = syll.getExtension(Segregated.class);
				if(seg.isSegregated())
					buffer.append(new IntraWordPause());
				if(syllIdx > 0 && !seg.isSegregated() && !syll.matches("^\\s.+")) {
					buffer.appendSyllableBoundary();
				}
				buffer.append(syll);
			}
			retVal = buffer.toIPATranscript();
		}
		return retVal;
	}

}
