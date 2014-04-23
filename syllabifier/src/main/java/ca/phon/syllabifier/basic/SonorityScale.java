package ca.phon.syllabifier.basic;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import ca.phon.ipa.IPAElement;
import ca.phon.ipa.IPATranscript;
import ca.phon.phonex.PhonexMatcher;
import ca.phon.phonex.PhonexPattern;
import ca.phon.syllabifier.basic.io.PhonexList;
import ca.phon.syllabifier.basic.io.SonorityValues;
import ca.phon.syllabifier.basic.io.SonorityValues.SonorityClass;
import ca.phon.syllabifier.phonex.SonorityInfo;

public class SonorityScale implements SyllabifierStage {

	private final SonorityValues sonorityValues;
	
	private final Map<Integer, List<PhonexPattern>> sonorityPatterns = 
			new LinkedHashMap<Integer, List<PhonexPattern>>();
	
	public SonorityScale(SonorityValues values) {
		super();
		this.sonorityValues = values;
		
		compile();
	}
	
	private void compile() {
		for(SonorityClass sc:sonorityValues.getSonorityClass()) {
			final List<PhonexPattern> patterns = new ArrayList<PhonexPattern>();
			final PhonexList pl = sc.getExprs();
			for(String phonex:pl.getPhonex()) {
				final PhonexPattern pattern = PhonexPattern.compile(phonex);
				patterns.add(pattern);
			}
			sonorityPatterns.put(sc.getSonorityValue(), patterns);
		}
	}

	@Override
	public boolean run(List<IPAElement> phones) {
		Integer lastVal = null;
		for(IPAElement ele:phones) {
			for(int sval:sonorityPatterns.keySet()) {
				boolean handeled = false;
				for(PhonexPattern p:sonorityPatterns.get(sval)) {
					final PhonexMatcher m = p.matcher(new IPATranscript(ele));
					if(m.matches()) {
						final SonorityInfo sInfo = new SonorityInfo(sval, 
								(lastVal == null ? 0 : sval - lastVal));
						ele.putExtension(SonorityInfo.class, sInfo);
						lastVal = sval;
						handeled = true;
						break;
					}
				}
				if(handeled) break;
			}
		}
		// always return false in this stage
		return false;
	}

	@Override
	public boolean repeatWhileChanges() {
		return false;
	}

	@Override
	public String getName() {
		return SonorityScale.class.getSimpleName();
	}
	
}
