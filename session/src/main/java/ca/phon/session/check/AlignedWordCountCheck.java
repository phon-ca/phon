package ca.phon.session.check;

import ca.phon.plugin.IPluginExtensionFactory;
import ca.phon.plugin.IPluginExtensionPoint;
import ca.phon.plugin.PhonPlugin;
import ca.phon.plugin.Rank;
import ca.phon.session.Group;
import ca.phon.session.Record;
import ca.phon.session.Session;
import ca.phon.session.SystemTierType;

import java.util.Properties;

@Rank(100)
@PhonPlugin(name = "Aligned Word Count Check")
public class AlignedWordCountCheck implements SessionCheck, IPluginExtensionPoint<SessionCheck> {

	private void reportWordAlignmentDifference(SessionValidator validator, Session session,
	                                           Record record, String tierName, int gIdx) {
		var expected = record.getGroup(gIdx).getAlignedWordCount();
		var got = record.getGroup(gIdx).getWordCount(tierName);
		var rNum = session.getRecordPosition(record);

		var msg = String.format("Expected %d (align-able) words, got %d", expected, got);
		validator.fireValidationEvent(session, rNum, tierName, gIdx, msg);
	}

	@Override
	public boolean performCheckByDefault() {
		return false;
	}

	@Override
	public boolean checkSession(SessionValidator validator, Session session) {
		session.getRecords().forEach( (final Record record) -> {
			for(int gIdx = 0; gIdx < record.numberOfGroups(); gIdx++) {
				// aligned word count for record (max number of aligned words)
				final Group group = record.getGroup(gIdx);
				int targetWordCount = group.getAlignedWordCount();

				if(group.getWordCount(SystemTierType.Orthography.getName()) != targetWordCount) {
					reportWordAlignmentDifference(validator, session, record, SystemTierType.Orthography.getName(), gIdx);
				}
				if(group.getWordCount(SystemTierType.IPATarget.getName()) != targetWordCount) {
					reportWordAlignmentDifference(validator, session, record, SystemTierType.IPATarget.getName(), gIdx);
				}
				if(group.getWordCount(SystemTierType.IPAActual.getName()) != targetWordCount) {
					reportWordAlignmentDifference(validator, session, record, SystemTierType.IPAActual.getName(), gIdx);
				}

				final int groupIdx = gIdx;
				record.getExtraTierNames().forEach( (tierName) -> {
					if(record.getTier(tierName).isGrouped() && group.getWordCount(tierName) != targetWordCount) {
						reportWordAlignmentDifference(validator, session, record, tierName, groupIdx);
					}
				});
			}
		});

		// not modified
		return false;
	}

	@Override
	public Properties getProperties() {
		return new Properties();
	}

	@Override
	public void loadProperties(Properties props) {
	}

	@Override
	public Class<?> getExtensionType() {
		return SessionCheck.class;
	}

	@Override
	public IPluginExtensionFactory<SessionCheck> getFactory() {
		return (args) -> this;
	}

}
