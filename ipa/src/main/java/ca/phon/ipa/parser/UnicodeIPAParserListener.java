package ca.phon.ipa.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.antlr.v4.runtime.tree.ErrorNode;

import ca.phon.ipa.Diacritic;
import ca.phon.ipa.IPAElementFactory;
import ca.phon.ipa.IPATranscript;
import ca.phon.ipa.IPATranscriptBuilder;
import ca.phon.ipa.PauseLength;
import ca.phon.ipa.Phone;
import ca.phon.ipa.WordBoundary;
import ca.phon.ipa.parser.UnicodeIPAParser.AlignmentContext;
import ca.phon.ipa.parser.UnicodeIPAParser.CompoundPhoneContext;
import ca.phon.ipa.parser.UnicodeIPAParser.CompoundWordMarkerContext;
import ca.phon.ipa.parser.UnicodeIPAParser.GroupNameRefContext;
import ca.phon.ipa.parser.UnicodeIPAParser.GroupNumberRefContext;
import ca.phon.ipa.parser.UnicodeIPAParser.Intra_word_pauseContext;
import ca.phon.ipa.parser.UnicodeIPAParser.LongPauseContext;
import ca.phon.ipa.parser.UnicodeIPAParser.MajorGroupContext;
import ca.phon.ipa.parser.UnicodeIPAParser.MeduimPauseContext;
import ca.phon.ipa.parser.UnicodeIPAParser.MinorGroupContext;
import ca.phon.ipa.parser.UnicodeIPAParser.PauseContext;
import ca.phon.ipa.parser.UnicodeIPAParser.Phonex_matcher_refContext;
import ca.phon.ipa.parser.UnicodeIPAParser.PrefixDiacriticContext;
import ca.phon.ipa.parser.UnicodeIPAParser.PrefixDiacriticLigatureContext;
import ca.phon.ipa.parser.UnicodeIPAParser.PrefixDiacriticRoleReversedContext;
import ca.phon.ipa.parser.UnicodeIPAParser.PrimaryStressContext;
import ca.phon.ipa.parser.UnicodeIPAParser.SandhiMarkerContext;
import ca.phon.ipa.parser.UnicodeIPAParser.SctypeContext;
import ca.phon.ipa.parser.UnicodeIPAParser.SecondaryStressContext;
import ca.phon.ipa.parser.UnicodeIPAParser.ShortPauseContext;
import ca.phon.ipa.parser.UnicodeIPAParser.SinglePhoneContext;
import ca.phon.ipa.parser.UnicodeIPAParser.StartContext;
import ca.phon.ipa.parser.UnicodeIPAParser.SuffixDiacriticContext;
import ca.phon.ipa.parser.UnicodeIPAParser.SuffixDiacriticLigatureContext;
import ca.phon.ipa.parser.UnicodeIPAParser.SuffixDiacriticRoleReversedContext;
import ca.phon.ipa.parser.UnicodeIPAParser.SyllableBoundaryContext;
import ca.phon.ipa.parser.UnicodeIPAParser.ToneContext;
import ca.phon.ipa.parser.UnicodeIPAParser.TranscriptionContext;
import ca.phon.ipa.parser.UnicodeIPAParser.WordContext;
import ca.phon.syllable.SyllableConstituentType;

public class UnicodeIPAParserListener extends UnicodeIPABaseListener {

	private final IPAElementFactory factory = new IPAElementFactory();
	
	private final IPATranscriptBuilder builder = new IPATranscriptBuilder();
	
	private final List<Diacritic> prefixCache = new ArrayList<>();
	
	private final List<Diacritic> suffixCache = new ArrayList<>();
	
	@Override
	public void exitStart(StartContext ctx) {
		if(builder.last() instanceof WordBoundary) {
			builder.removeLast();
		}
	}
	
	@Override
	public void exitWord(WordContext ctx) {
		builder.appendWordBoundary();
	}
	
	@Override
	public void exitPrimaryStress(PrimaryStressContext ctx) {
		builder.append(factory.createPrimaryStress());
	}
	
	@Override
	public void exitSecondaryStress(SecondaryStressContext ctx) {
		builder.append(factory.createSecondaryStress());
	}
	
	@Override
	public void exitSyllableBoundary(SyllableBoundaryContext ctx) {
		builder.append(factory.createSyllableBoundary());
	}
	
	@Override
	public void exitIntra_word_pause(Intra_word_pauseContext ctx) {
		builder.append(factory.createIntraWordPause());
	}
	
	@Override
	public void exitCompoundWordMarker(CompoundWordMarkerContext ctx) {
		builder.append(factory.createCompoundWordMarker());
	}
	
	@Override
	public void exitSandhiMarker(SandhiMarkerContext ctx) {
		builder.append(factory.createSandhi(ctx.SANDHI().getText()));
	}
	
	@Override
	public void exitGroupNumberRef(GroupNumberRefContext ctx) {
		Integer groupIndex = Integer.parseInt(ctx.NUMBER().getText());
		builder.append(factory.createPhonexMatcherReference(groupIndex));
	}
	
	@Override
	public void exitGroupNameRef(GroupNameRefContext ctx) {
		String groupName = ctx.GROUP_NAME().getText().substring(1, ctx.GROUP_NAME().getText().length()-1);
		builder.append(factory.createPhonexMatcherReference(groupName));
	}
	
	@Override
	public void exitSctype(SctypeContext ctx) {
		SyllableConstituentType scType = SyllableConstituentType.fromString(ctx.getText().charAt(1)+"");
		if(builder.size() > 0) {
			builder.last().setScType(scType);
		}
	}
	
	@Override
	public void enterSinglePhone(SinglePhoneContext ctx) {
		
	}
	
	@Override
	public void exitSinglePhone(SinglePhoneContext ctx) {
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
	public void exitTone(ToneContext ctx) {
		Diacritic toneDia = factory.createDiacritic(ctx.TONE().getText().charAt(0));
		suffixCache.add(toneDia);
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
		builder.makeCompoundPhone(ctx.LIGATURE().getText().charAt(0));
	}
	
	@Override
	public void exitPause(PauseContext ctx) {
		builder.append(factory.createWordBoundary());
	}
	
	@Override
	public void exitShortPause(ShortPauseContext ctx) {
		builder.append(factory.createPause(PauseLength.SHORT));
	}
	
	@Override
	public void exitMeduimPause(MeduimPauseContext ctx) {
		builder.append(factory.createPause(PauseLength.MEDIUM));
	}
	
	@Override
	public void exitLongPause(LongPauseContext ctx) {
		builder.append(factory.createPause(PauseLength.LONG));
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
