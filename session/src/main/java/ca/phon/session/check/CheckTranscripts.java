package ca.phon.session.check;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ca.phon.extensions.UnvalidatedValue;
import ca.phon.ipa.IPATranscript;
import ca.phon.plugin.IPluginExtensionFactory;
import ca.phon.plugin.IPluginExtensionPoint;
import ca.phon.plugin.PhonPlugin;
import ca.phon.session.Record;
import ca.phon.session.Session;
import ca.phon.session.SystemTierType;
import ca.phon.session.Tier;

/**
 * Check IPA transcriptions for a session. 
 *
 */
@PhonPlugin(name="check", version="1", minPhonVersion="2.1.0")
public class CheckTranscripts implements SessionCheck, IPluginExtensionPoint<SessionCheck> {

	@Override
	public void checkSession(SessionValidator validator, 
			Session session, Map<String, Object> options) {
		
		for(int i = 0; i < session.getRecordCount(); i++) {
			final Record r = session.getRecord(i);
			
			List<String> ipaTiers = new ArrayList<>();
			ipaTiers.add(SystemTierType.IPATarget.getName());
			ipaTiers.add(SystemTierType.IPAActual.getName());
			for(String depTier:r.getExtraTierNames()) {
				if(r.getTierType(depTier) == IPATranscript.class) {
					ipaTiers.add(depTier);
				}
			}
			
			for(String ipaTier:ipaTiers) {
				final Tier<IPATranscript> tier = r.getTier(ipaTier, IPATranscript.class);
				for(int gIdx = 0; gIdx < tier.numberOfGroups(); gIdx++) {
					final IPATranscript ipa = tier.getGroup(gIdx);
					
					final UnvalidatedValue uv = ipa.getExtension(UnvalidatedValue.class);
					if(uv != null) {
						// error in this transcription
						final ValidationEvent ve = new ValidationEvent(session, i, ipaTier, gIdx,
								uv.getParseError().getMessage());
						validator.fireValidationEvent(ve);
					}
					
				}
			}
		}
		
	}

	@Override
	public Class<?> getExtensionType() {
		return SessionCheck.class;
	}

	@Override
	public IPluginExtensionFactory<SessionCheck> getFactory() {
		final IPluginExtensionFactory<SessionCheck> factory = (Object ... args) -> {
			return CheckTranscripts.this;
		};
		return factory;
	}

}
