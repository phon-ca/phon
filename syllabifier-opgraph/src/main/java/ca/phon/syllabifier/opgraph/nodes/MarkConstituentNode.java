package ca.phon.syllabifier.opgraph.nodes;

import java.awt.Component;
import java.util.List;
import java.util.Properties;

import ca.gedge.opgraph.InputField;
import ca.gedge.opgraph.OpContext;
import ca.gedge.opgraph.OpNode;
import ca.gedge.opgraph.OpNodeInfo;
import ca.gedge.opgraph.OutputField;
import ca.gedge.opgraph.app.GraphDocument;
import ca.gedge.opgraph.app.extensions.NodeSettings;
import ca.gedge.opgraph.exceptions.ProcessingException;
import ca.phon.ipa.IPAElement;
import ca.phon.ipa.IPATranscript;
import ca.phon.opgraph.nodes.phonex.PhonexNode;
import ca.phon.opgraph.nodes.phonex.PhonexSettingsPanel;
import ca.phon.phonex.PhonexMatcher;
import ca.phon.phonex.PhonexPattern;
import ca.phon.phonex.PhonexPatternException;
import ca.phon.syllable.SyllableConstituentType;

@OpNodeInfo(
		name="Mark Constituent Type",
		description="Use phonex expressions to mark constituent type for phones. Use group names to determine syllabification information.",
		category="Syllabifier"
)
public class MarkConstituentNode extends OpNode implements PhonexNode {

	// input field
	private final static InputField ipaInput = 
			new InputField("ipa", "ipa input", IPATranscript.class);
	// pass-through output
	private final static OutputField ipaOut =
			new OutputField("ipa out", "pass-through ipa ouptput", true, IPATranscript.class);
	
	/**
	 * Compiled phonex pattern
	 */
	private PhonexPattern pattern;
	
	public MarkConstituentNode() {
		super();
		
		putField(ipaInput);
		putField(ipaOut);
		
		putExtension(NodeSettings.class, this);
	}

	@Override
	public void operate(OpContext context) throws ProcessingException {
		if(pattern == null)
			throw new ProcessingException(null, "No pattern");
		final IPATranscript ipa = (IPATranscript)context.get(ipaInput);
		
		final PhonexMatcher matcher = pattern.matcher(ipa);
		while(matcher.find()) {
			for(int i = 1; i <= matcher.groupCount(); i++) {
				final String grpName = pattern.groupName(i);
				if(grpName != null) {
					final SyllableConstituentType scType = 
							SyllableConstituentType.fromString(grpName);
					final List<IPAElement> grp = matcher.group(i);
					grp.forEach( (e) -> e.setScType(scType) );
				}
			}
		}
		
		context.put(ipaOut, ipa);
	}

	@Override
	public String getPhonex() {
		String retVal = "";
		if(pattern != null)
			retVal = pattern.pattern();
		return retVal;
	}

	private PhonexSettingsPanel settingsPanel;
	@Override
	public Component getComponent(GraphDocument arg0) {
		if(settingsPanel == null) {
			settingsPanel = new PhonexSettingsPanel(this);
		}
		return settingsPanel;
	}
	
	private final String PHONEX_KEY = 
			getClass().getName() + ".phonex";

	@Override
	public Properties getSettings() {
		final Properties retVal = new Properties();
		if(pattern != null) {
			retVal.setProperty(PHONEX_KEY, pattern.pattern());
		}
		return retVal;
	}

	@Override
	public void loadSettings(Properties arg0) {
		if(arg0.containsKey(PHONEX_KEY)) {
			setPhonex(arg0.getProperty(PHONEX_KEY));
		}
	}

	@Override
	public void setPhonex(String phonex) throws PhonexPatternException {
		pattern = PhonexPattern.compile(phonex);
		
	}
}
