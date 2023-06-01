package ca.phon.ipa.parser;

import ca.phon.ipa.*;
import ca.phon.ipa.parser.UnicodeIPAParser.*;
import ca.phon.ipa.parser.exceptions.*;
import ca.phon.syllable.*;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ErrorNode;

import java.util.*;
import java.util.stream.Collectors;

public class UnicodeIPAParserListener extends UnicodeIPABaseListener {

	private final IPAElementFactory factory = new IPAElementFactory();
	
	private final IPATranscriptBuilder builder = new IPATranscriptBuilder();
	
	private final List<Diacritic> prefixCache = new ArrayList<>();
	
	private final List<Diacritic> suffixCache = new ArrayList<>();

	public IPATranscriptBuilder getBuilder() {
		return this.builder;
	}

	private List<IPAParserException> parserErrors = new ArrayList<>();

	public List<IPAParserException> getParserErrors() {
		return this.parserErrors;
	}

	@Override
	public void visitErrorNode(ErrorNode errorNode) {
		Token symbol = errorNode.getSymbol();
		IPAParserException ex = new IPAParserException(errorNode.getText());

		// ignore leading whitespace
		if(symbol.getType() == UnicodeIPAParser.WS && builder.size() == 0) {
			// recover
			return;
		}

		switch(errorNode.getSymbol().getType()) {
		case UnicodeIPAParser.LIGATURE:
			ex = new HangingLigatureException("Ligature missing left-hand element");
			break;

		case UnicodeIPAParser.PREFIX_DIACRITIC:
		case UnicodeIPAParser.SUFFIX_DIACRITIC:
		case UnicodeIPAParser.COMBINING_DIACRITIC:
		case UnicodeIPAParser.TONE_NUMBER:
		case UnicodeIPAParser.LONG:
		case UnicodeIPAParser.HALF_LONG:
		case UnicodeIPAParser.PERIOD:
		case UnicodeIPAParser.MINOR_GROUP:
		case UnicodeIPAParser.MAJOR_GROUP:
		case UnicodeIPAParser.SANDHI:
		case UnicodeIPAParser.PLUS:
		case UnicodeIPAParser.PRIMARY_STRESS:
		case UnicodeIPAParser.SECONDARY_STRESS:
			ex = new StrayDiacriticException("Stray diacritic " + symbol.getText());
			break;

		default:
			break;
		}
		ex.setPositionInLine(symbol.getCharPositionInLine());
		parserErrors.add(ex);
	}

	@Override
	public void exitStart(StartContext ctx) {
		while(builder.size() > 0 && builder.last() instanceof WordBoundary) {
			builder.removeLast();
		}
		if(builder.size() > 0) {
			IPAElement ele = builder.last();
			if(ele.getScType() == SyllableConstituentType.SYLLABLESTRESSMARKER
				|| ele.getScType() == SyllableConstituentType.SYLLABLEBOUNDARYMARKER) {
				int idx = ctx.getStop().getCharPositionInLine();
				if(ctx.getStop().getType() == CommonToken.EOF) --idx;
				throw new StrayDiacriticException("Expecting next syllable", idx);
			}
		}
	}

	@Override
	public void exitPrimaryStress(PrimaryStressContext ctx) {
		if(builder.size() > 0) {
			if (builder.last() instanceof SyllableBoundary
					|| builder.last() instanceof StressMarker
					|| builder.last() instanceof IntonationGroup)
				throw new StrayDiacriticException("Expecting next syllable", ctx.getStop().getCharPositionInLine());
		}
		builder.append(factory.createPrimaryStress());
	}
	
	@Override
	public void exitSecondaryStress(SecondaryStressContext ctx) {
		if(builder.size() > 0) {
			if (builder.last() instanceof SyllableBoundary
					|| builder.last() instanceof StressMarker
					|| builder.last() instanceof IntonationGroup)
				throw new StrayDiacriticException("Expecting next syllable", ctx.getStop().getCharPositionInLine());
		}
		builder.append(factory.createSecondaryStress());
	}
	
	@Override
	public void exitSyllableBoundary(SyllableBoundaryContext ctx) {
		if(builder.size() > 0) {
			if (builder.last() instanceof SyllableBoundary
					|| builder.last() instanceof StressMarker
					|| builder.last() instanceof IntonationGroup
					|| builder.last() instanceof WordBoundary)
				throw new StrayDiacriticException("Expecting next syllable", ctx.getStop().getCharPositionInLine());
		}
		builder.append(factory.createSyllableBoundary());
	}
	
	@Override
	public void exitIntra_word_pause(Intra_word_pauseContext ctx) {
		builder.append(factory.createIntraWordPause());
	}

	@Override
	public void exitWhiteSpace(UnicodeIPAParser.WhiteSpaceContext ctx) {
		builder.appendWordBoundary();
	}

	@Override
	public void exitCompoundWordMarker(CompoundWordMarkerContext ctx) {
		if(ctx.getText().equals("+"))
			builder.append(factory.createCompoundWordMarker());
		else
			builder.append(factory.createCompoundWordMarkerTilde());
	}
	
	@Override
	public void exitSandhiMarker(SandhiMarkerContext ctx) {
		builder.append(factory.createSandhi(ctx.SANDHI().getText()));
	}
	
	@Override
	public void exitGroupNumberRef(GroupNumberRefContext ctx) {
		Integer groupIndex = Integer.parseInt(ctx.NUMBER().getText());

		Diacritic[] combining = null;

		if(ctx.COMBINING_DIACRITIC() != null) {
			combining = ctx.COMBINING_DIACRITIC().stream()
					.map( tn -> factory.createDiacritic(tn.getText().charAt(0)) )
					.collect(Collectors.toList())
					.toArray(Diacritic[]::new);
		}

		PhonexMatcherReference phonexMatcherReference = factory.createPhonexMatcherReference(groupIndex);
		if(combining != null)
			phonexMatcherReference.setCombiningDiacritics(combining);

		phonexMatcherReference.setPrefixDiacritics(prefixCache.toArray(new Diacritic[0]));
		phonexMatcherReference.setSuffixDiacritics(suffixCache.toArray(new Diacritic[0]));

		builder.append(phonexMatcherReference);

		prefixCache.clear();
		suffixCache.clear();
	}
	
	@Override
	public void exitGroupNameRef(GroupNameRefContext ctx) {
		String groupName = ctx.GROUP_NAME().getText().substring(1, ctx.GROUP_NAME().getText().length()-1);

		Diacritic[] combining = null;

		if(ctx.COMBINING_DIACRITIC() != null) {
			combining = ctx.COMBINING_DIACRITIC().stream()
					.map( tn -> factory.createDiacritic(tn.getText().charAt(0)) )
					.collect(Collectors.toList())
					.toArray(Diacritic[]::new);
		}

		PhonexMatcherReference phonexMatcherReference = factory.createPhonexMatcherReference(groupName);
		if(combining != null)
			phonexMatcherReference.setCombiningDiacritics(combining);

		phonexMatcherReference.setPrefixDiacritics(prefixCache.toArray(new Diacritic[0]));
		phonexMatcherReference.setSuffixDiacritics(suffixCache.toArray(new Diacritic[0]));

		builder.append(phonexMatcherReference);

		prefixCache.clear();
		suffixCache.clear();
	}

	@Override
	public void exitSctype(SctypeContext ctx) {
		SyllableConstituentType scType = SyllableConstituentType.fromString(ctx.getText().charAt(1)+"");
		if(builder.size() > 0) {
			builder.last().setScType(scType);
			if(scType == SyllableConstituentType.NUCLEUS && ctx.getText().matches(":[dD]")) {
				SyllabificationInfo sinfo = builder.last().getExtension(SyllabificationInfo.class);
				sinfo.setDiphthongMember(true);
			}
		}
	}

	@Override
	public void exitSinglePhone(SinglePhoneContext ctx) {
		// parse error
		if(ctx.base_phone() == null
			|| ctx.base_phone().getText().length() == 0) return;

		Character basePhone = ctx.base_phone().getText().charAt(0);
		Diacritic[] combining = null;
		
		if(ctx.COMBINING_DIACRITIC() != null) {
			combining = ctx.COMBINING_DIACRITIC().stream()
					.map( tn -> factory.createDiacritic(tn.getText().charAt(0)) )
					.collect(Collectors.toList())
					.toArray(Diacritic[]::new);
		}

		Phone p = factory.createPhone(prefixCache.toArray(Diacritic[]::new), basePhone, combining, suffixCache.toArray(Diacritic[]::new));
		builder.append(p);
		
		prefixCache.clear();
		suffixCache.clear();
	}

	@Override
	public void exitPhone_length(UnicodeIPAParser.Phone_lengthContext ctx) {
		String phoneLength = ctx.getText();
		for(char lengthCh:phoneLength.toCharArray()) {
			Diacritic lengthDia = factory.createDiacritic(lengthCh);
			suffixCache.add(lengthDia);
		}
	}

	@Override
	public void exitPrefixDiacritic(PrefixDiacriticContext ctx) {
		char prefixChar = ctx.PREFIX_DIACRITIC().getText().charAt(0);
		Diacritic[] combining = new Diacritic[0];
		
		if(ctx.COMBINING_DIACRITIC() != null) {
			combining = ctx.COMBINING_DIACRITIC().stream()
					.map( tn -> factory.createDiacritic(tn.getText().charAt(0)) )
					.collect(Collectors.toList())
					.toArray(Diacritic[]::new);
		}
		
		Diacritic prefixDia = factory.createDiacritic(new Diacritic[0], prefixChar, combining);
		prefixCache.add(prefixDia);
	}
	
	@Override
	public void exitPrefixDiacriticRoleReversed(PrefixDiacriticRoleReversedContext ctx) {
		char prefixChar = ctx.SUFFIX_DIACRITIC().getText().charAt(0);
		List<Diacritic> combining = new ArrayList<>();
		
		if(ctx.COMBINING_DIACRITIC() != null) {
			combining = ctx.COMBINING_DIACRITIC().stream()
					.map( tn -> factory.createDiacritic(tn.getText().charAt(0)) )
					.collect(Collectors.toList());
		}
		combining.add(0, factory.createDiacritic(ctx.ROLE_REVERSAL().getText().charAt(0)));
		
		Diacritic prefixDia = factory.createDiacritic(new Diacritic[0], prefixChar, combining.toArray(Diacritic[]::new));
		prefixCache.add(prefixDia);
	}
	
	@Override
	public void exitPrefixDiacriticLigature(PrefixDiacriticLigatureContext ctx) {
		char prefixChar = ctx.SUFFIX_DIACRITIC().getText().charAt(0);
		Diacritic lig = factory.createDiacritic(ctx.LIGATURE().getText().charAt(0));
		List<Diacritic> combining = new ArrayList<>();
		
		if(ctx.COMBINING_DIACRITIC() != null) {
			combining = ctx.COMBINING_DIACRITIC().stream()
					.map( tn -> factory.createDiacritic(tn.getText().charAt(0)) )
					.collect(Collectors.toList());
		}
		combining.add(lig);
		
		Diacritic prefixDia = factory.createDiacritic(new Diacritic[0], prefixChar, combining.toArray(Diacritic[]::new));
		prefixCache.add(prefixDia);
	}
	
	@Override
	public void exitSuffixDiacritic(SuffixDiacriticContext ctx) {
		char suffixChar = ctx.SUFFIX_DIACRITIC().getText().charAt(0);
		Diacritic[] combining = new Diacritic[0];
		
		if(ctx.COMBINING_DIACRITIC() != null) {
			combining = ctx.COMBINING_DIACRITIC().stream()
					.map( tn -> factory.createDiacritic(tn.getText().charAt(0)) )
					.collect(Collectors.toList())
					.toArray(Diacritic[]::new);
		}
		
		Diacritic suffixDia = factory.createDiacritic(new Diacritic[0], suffixChar, combining);
		suffixCache.add(suffixDia);
	}
	
	@Override
	public void exitSuffixDiacriticRoleReversed(SuffixDiacriticRoleReversedContext ctx) {
		char suffixChar = ctx.PREFIX_DIACRITIC().getText().charAt(0);
		List<Diacritic> combining = new ArrayList<>();
		
		if(ctx.COMBINING_DIACRITIC() != null) {
			combining = ctx.COMBINING_DIACRITIC().stream()
					.map( tn -> factory.createDiacritic(tn.getText().charAt(0)) )
					.collect(Collectors.toList());
		}
		combining.add(0, factory.createDiacritic(ctx.ROLE_REVERSAL().getText().charAt(0)));
		
		Diacritic prefixDia = factory.createDiacritic(new Diacritic[0], suffixChar, combining.toArray(Diacritic[]::new));
		suffixCache.add(prefixDia);
	}
	
	@Override
	public void exitSuffixDiacriticLigature(SuffixDiacriticLigatureContext ctx) {
		char suffixChar = ctx.PREFIX_DIACRITIC().getText().charAt(0);
		Diacritic lig = factory.createDiacritic(ctx.LIGATURE().getText().charAt(0));
		Diacritic[] combining = new Diacritic[0];
		
		if(ctx.COMBINING_DIACRITIC() != null) {
			combining = ctx.COMBINING_DIACRITIC().stream()
					.map( tn -> factory.createDiacritic(tn.getText().charAt(0)) )
					.collect(Collectors.toList())
					.toArray(Diacritic[]::new);
		}
		
		Diacritic suffixDia = factory.createDiacritic(new Diacritic[] {lig}, suffixChar, combining);
		suffixCache.add(suffixDia);
	}

	@Override
	public void exitTone_number(UnicodeIPAParser.Tone_numberContext ctx) {
		for(char ch:ctx.getText().toCharArray()) {
			Diacritic toneNumberDia = factory.createDiacritic(ch);
			suffixCache.add(toneNumberDia);
		}
	}

	@Override
	public void exitMinorGroup(MinorGroupContext ctx) {
		builder.append(factory.createMinorIntonationGroup());
	}
	
	@Override
	public void exitMajorGroup(MajorGroupContext ctx) {
		builder.append(factory.createMajorIntonationGroup());
	}

	@Override
	public void exitCompoundPhone(CompoundPhoneContext ctx) {
		if(builder.size() >= 2)
			builder.makeCompoundPhone(ctx.LIGATURE().getText().charAt(0));
	}

	@Override
	public void exitSimplePause(SimplePauseContext ctx) {
		super.exitSimplePause(ctx);
	}

	@Override
	public void exitLongPause(LongPauseContext ctx) {
		builder.append(factory.createPause(PauseLength.LONG));
	}

	@Override
	public void exitVeryLongPause(VeryLongPauseContext ctx) {
		super.exitVeryLongPause(ctx);
	}

	@Override
	public void exitNumericPause(NumericPauseContext ctx) {
		super.exitNumericPause(ctx);
	}

	@Override
	public void exitAlignment(AlignmentContext ctx) {
		builder.append(factory.createAlignmentMarker());
	}
	
	/**
	 * Get the IPATranscript created by this listener.
	 * 
	 * @return ipa
	 */
	public IPATranscript getTranscript() {
		return builder.toIPATranscript();
	}
	
}
