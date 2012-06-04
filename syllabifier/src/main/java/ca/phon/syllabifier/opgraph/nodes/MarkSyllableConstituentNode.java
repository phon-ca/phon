package ca.phon.plugins.opgraph.nodes.syllabifier;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Properties;

import javax.swing.JLabel;
import javax.swing.JPanel;

import ca.gedge.opgraph.InputField;
import ca.gedge.opgraph.OperableContext;
import ca.gedge.opgraph.OperableVertex;
import ca.gedge.opgraph.OperableVertexInfo;
import ca.gedge.opgraph.app.GraphDocument;
import ca.gedge.opgraph.app.extensions.NodeSettings;
import ca.gedge.opgraph.exceptions.ProcessingException;
import ca.phon.ipa.IPATranscript;
import ca.phon.ipa.phone.Phone;
import ca.phon.syllable.SyllabificationInfo;
import ca.phon.syllable.SyllableConstituentType;
import ca.phon.ui.syllable.SyllableConstituentComboBox;

/**
 * Node used to adjust the syllabification information for
 * given ipa data.
 * 
 */
@OperableVertexInfo(
		name="Constituent Type",
		description="Mark the given IPA using the specified syllable constituent type.",
		category="Syllabifier"
)
public class MarkSyllableConstituentNode extends OperableVertex implements NodeSettings {
	// single ipa input
	private final InputField ipaIn = 
			new InputField("ipa", "ipa to mark", IPATranscript.class);
	
	/**
	 * Constituent type to use
	 */
	private SyllableConstituentType scType = SyllableConstituentType.UNKNOWN;
	
	/**
	 * Constructor
	 */
	public MarkSyllableConstituentNode() {
		super();
		putField(ipaIn);
		
		putExtension(NodeSettings.class, this);
	}

	/**
	 * Get selected constituent type
	 * @return selected syllable constituent type
	 */
	public SyllableConstituentType getScType() {
		return scType;
	}

	/**
	 * Set constituent type
	 * @param scType
	 */
	public void setScType(SyllableConstituentType scType) {
		this.scType = scType;
	}

	private Settings settings;
	@Override
	public Component getComponent(GraphDocument document) {
		if(settings == null) {
			settings = new Settings();
			settings.typeBox.setSelectedItem(getScType());
		}
		return settings;
	}
	
	
	private final String SC_KEY = getClass().getName() + ".sctype";
	@Override
	public Properties getSettings() {
		final Properties retVal = new Properties();
		retVal.put(SC_KEY, scType.getIdentifier());
		return retVal;
	}

	@Override
	public void loadSettings(Properties properties) {
		if(properties.containsKey(SC_KEY)) {
			String scTypeString = properties.getProperty(SC_KEY);
			SyllableConstituentType scType = SyllableConstituentType.fromString(scTypeString);
			setScType(scType);
		}
	}

	@Override
	public void operate(OperableContext context) throws ProcessingException {
		// get input and set syllabification information
		if(context.containsKey(ipaIn)) {
			IPATranscript ipa = (IPATranscript)context.get(ipaIn);
			for(Phone p:ipa) {
				SyllabificationInfo sInfo = p.getCapability(SyllabificationInfo.class);
				if(sInfo == null) {
					sInfo = new SyllabificationInfo();
					p.putCapability(SyllabificationInfo.class, sInfo);
				}
				sInfo.setConstituentType(scType);
			}
		}
	}
	
	/**
	 * Settings panel for selecting constituent type
	 */
	private class Settings extends JPanel {
		/**
		 * Constituent type box
		 */
		private final SyllableConstituentComboBox typeBox = new SyllableConstituentComboBox();
		
		public Settings() {
			super();
			init();
		}
		
		private void init() {
			setLayout(new BorderLayout());
			
			JPanel topPanel = new JPanel(new GridLayout(1, 2));
			topPanel.add(new JLabel("Select contituent type:"));
			topPanel.add(typeBox);
			typeBox.setSelectedItem(getScType());
			
			typeBox.addItemListener(new ItemListener() {
				
				@Override
				public void itemStateChanged(ItemEvent arg0) {
					setScType((SyllableConstituentType)typeBox.getSelectedItem());
				}
			});
			
			add(topPanel, BorderLayout.NORTH);
		}
	}

}
