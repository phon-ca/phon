package ca.phon.syllabifier.opgraph.nodes;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import ca.gedge.opgraph.InputField;
import ca.gedge.opgraph.OpContext;
import ca.gedge.opgraph.OpNode;
import ca.gedge.opgraph.OpNodeInfo;
import ca.gedge.opgraph.OutputField;
import ca.gedge.opgraph.app.GraphDocument;
import ca.gedge.opgraph.app.extensions.NodeSettings;
import ca.gedge.opgraph.exceptions.ProcessingException;
import ca.phon.ipa.CompoundPhone;
import ca.phon.ipa.IPAElement;
import ca.phon.ipa.IPATranscript;
import ca.phon.ipa.Phone;
import ca.phon.phonex.PhonexMatcher;
import ca.phon.phonex.PhonexPattern;
import ca.phon.syllabifier.phonex.SonorityInfo;
import ca.phon.visitor.VisitorAdapter;
import ca.phon.visitor.annotation.Visits;

/**
 * <p>Marks phones with sonority annotations for use in phonex
 * expressions.  The sonority values are determined by the
 * node settings.  Each line in the node settings {@link String} should
 * be formatted like:
 * <pre>&lt;sonority_value&gt;=&lt;phonex&rt;</pre>
 * E.g.,:
 * <pre>0=[{affricate}{stop,-nasal}{fricative}]</pre>
 * </p>
 * 
 */
@OpNodeInfo(
		name="Mark Sonority",
		description="Add sonority annotation to phones.",
		category="Syllabifier")
public class SonorityNode extends OpNode implements NodeSettings {
	// ipa input
	private final InputField ipaIn = 
			new InputField("ipa", "ipa input", IPATranscript.class);
	private final OutputField ipaOut = 
			new OutputField("ipa", "ipa output", true, IPATranscript.class);
	
	/**
	 * Sonority scale
	 */
	// list of sonority patterns in order they are given
	private final List<PhonexPattern> patterns = new ArrayList<PhonexPattern>();
	// map of pattern to sonority value
	private final Map<PhonexPattern, Integer> sonorityMap = new TreeMap<PhonexPattern, Integer>();
	
	/**
	 * Constructor
	 */
	public SonorityNode() {
		super();
		
		putField(ipaIn);
		putField(ipaOut);
		
		putExtension(NodeSettings.class, this);
	}
	
	@Override
	public void operate(OpContext context) throws ProcessingException {
		// get ipa from context
		final IPATranscript ipa = 
				IPATranscript.class.cast(context.get(ipaIn));
	
		// mark sonority
		final SonorityVisitor visitor = new SonorityVisitor();
		ipa.accept(visitor);
		
		// set output
		context.put(ipaOut, ipa);
	}
	
	/**
	 * Set sonority scale as a string.
	 * 
	 * @param scale
	 */
	public void setSonorityScale(String scale) {
		final Pattern scannerPattern = Pattern.compile("([0-9]+)=(.*)");
		final Scanner scanner = new Scanner(scale);
		String line = null;
		while((line = scanner.findInLine(scannerPattern)) != null) {
			final Matcher matcher = scannerPattern.matcher(line);
			if(matcher.matches()) {
				final Integer sonorityValue = Integer.parseInt(matcher.group(1));
				final String phonex = matcher.group(2);
				
				try {
					final PhonexPattern pattern = PhonexPattern.compile(phonex);
					
					patterns.add(pattern);
					sonorityMap.put(pattern, sonorityValue);
				} catch (Exception e) {}
			}
			scanner.nextLine();
		}
	}
	
	/**
	 * Return the current sonority scale as a string
	 * 
	 * @return the sonority scale as a parse-able string
	 */
	public String getSonorityScale() {
		String retVal = "";
		
		for(final PhonexPattern pattern:patterns) {
			retVal += sonorityMap.get(pattern) + "=" + pattern.pattern() + "\n";
		}
		
		System.out.println(retVal);
		
		return retVal;
	}

	private Settings settingsPanel;
	@Override
	public Component getComponent(GraphDocument document) {
		if(settingsPanel == null) {
			settingsPanel = new Settings();
			settingsPanel.scaleArea.setText(getSonorityScale());
		}
		return settingsPanel;
	}

	private final static String SONORITY_KEY = 
			SonorityNode.class.getName() + ".scale";
	@Override
	public Properties getSettings() {
		final Properties retVal = new Properties();
		retVal.put(SONORITY_KEY, getSonorityScale());
		return retVal;
	}

	@Override
	public void loadSettings(Properties properties) {
		if(properties.containsKey(SONORITY_KEY)) {
			setSonorityScale(properties.getProperty(SONORITY_KEY));
		}
	}
	
	/**
	 * Settings panel
	 */
	private class Settings extends JPanel {
		/**
		 * Text field
		 */
		private final JTextArea scaleArea = new JTextArea();
		
		public Settings() {
			super();
			init();
		}
		
		private void init() {
			setLayout(new BorderLayout());
			
			add(new JLabel("Enter sonority scale:"), BorderLayout.NORTH);
			JScrollPane scaleScroller = new JScrollPane(scaleArea);
			add(scaleScroller, BorderLayout.CENTER);
			
			scaleArea.addFocusListener(new FocusListener() {
				
				@Override
				public void focusLost(FocusEvent arg0) {
					setSonorityScale(scaleArea.getText());
				}
				
				@Override
				public void focusGained(FocusEvent arg0) {
				}
			});
		}
	}
	
	/**
	 * Sonority visitor
	 */
	public class SonorityVisitor extends VisitorAdapter<IPAElement> {
		
		private int lastSonority = 0;

		@Override
		public void fallbackVisit(IPAElement obj) {
			// reset sonority
			lastSonority = 0;
		}
		
		@Visits
		public void visitBasicPhone(Phone bp) {
			attachSonority(bp);
		}
		
		@Visits
		public void visitCompoundPhone(CompoundPhone cp) {
			attachSonority(cp);
		}
		
		private void attachSonority(IPAElement p) {
			int value = 0;
			
			for(PhonexPattern pattern:patterns) {
				final PhonexMatcher m = pattern.matcher(Collections.singletonList(p));
				if(m.matches()) {
					value = sonorityMap.get(pattern);
					break;
				}
			}
			
			final int distance = Math.abs(value - lastSonority);
			lastSonority = value;
			
			final SonorityInfo info = new SonorityInfo(value, distance);
			p.putExtension(SonorityInfo.class, info);
		}
	}
}
