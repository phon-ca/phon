package ca.phon.app.opgraph.nodes.table;

import java.awt.Component;
import java.util.Properties;

import javax.swing.JCheckBox;
import javax.swing.JPanel;

import org.jdesktop.swingx.VerticalLayout;

import ca.gedge.opgraph.OpContext;
import ca.gedge.opgraph.OpNodeInfo;
import ca.gedge.opgraph.app.GraphDocument;
import ca.gedge.opgraph.app.extensions.NodeSettings;
import ca.gedge.opgraph.exceptions.ProcessingException;
import ca.phon.ipa.IPAElement;
import ca.phon.ipa.PhoneDimension;
import ca.phon.ipa.features.FeatureSet;
import jxl.demo.Features;

@OpNodeInfo(
		name="Phone Similarity",
		category="IPA Table Analysis",
		description="Calculate similarity between phones/transcripts based on features",
		showInLibrary=true
)
public class PhoneSimilarity extends TableOpNode implements NodeSettings {

	private JPanel settingsPanel;

	private JCheckBox placeBox;
	private boolean includePlace = true;
	private JCheckBox mannerBox;
	private boolean includeManner = true;
	private JCheckBox voicingBox;
	private boolean includeVoicing = true;

	private JCheckBox heightBox;
	private boolean includeHeight = true;
	private JCheckBox backnessBox;
	private boolean includeBackness = true;
	private JCheckBox tensenessBox;
	private boolean includeTenseness = true;
	private JCheckBox roundingBox;
	private boolean includeRounding = true;

	private class FeatureSimilarity {
		int numFeatures = 0;
		float similarity = 0.0f;
	}

	public PhoneSimilarity() {
		super();

		putExtension(NodeSettings.class, this);
	}

	@Override
	public void operate(OpContext context) throws ProcessingException {
	}

	/**
	 * Returns the full set of features used when comparing phones as
	 * setup by the node's options.
	 *
	 * @return
	 */
	public FeatureSet getComparsionFeatures() {
		FeatureSet retVal = new FeatureSet();

		if(isIncludePlace())
			retVal = FeatureSet.union(retVal, PhoneDimension.PLACE.getFeatures());
		if(isIncludeManner())
			retVal = FeatureSet.union(retVal, PhoneDimension.MANNER.getFeatures());
		if(isIncludeVoicing())
			retVal = FeatureSet.union(retVal, PhoneDimension.VOICING.getFeatures());

		if(isIncludeHeight())
			retVal = FeatureSet.union(retVal, PhoneDimension.HEIGHT.getFeatures());
		if(isIncludeBackness())
			retVal = FeatureSet.union(retVal, PhoneDimension.BACKNESS.getFeatures());
		if(isIncludeTenseness())
			retVal = FeatureSet.union(retVal, PhoneDimension.TENSENESS.getFeatures());
		if(isIncludeRounding())
			retVal = FeatureSet.union(retVal, PhoneDimension.ROUNDING.getFeatures());

		return retVal;
	}

	private FeatureSimilarity calculateSimilarity(IPAElement a, IPAElement b) {
		FeatureSimilarity retVal = new FeatureSimilarity();
		if(a == null || b == null) return retVal;

		final FeatureSet allFeatures = getComparsionFeatures();
		final FeatureSet aFeatures = FeatureSet.intersect(a.getFeatureSet(), allFeatures);
		final FeatureSet bFeatures = FeatureSet.intersect(b.getFeatureSet(), allFeatures);

		final FeatureSet sameFeatures = FeatureSet.intersect(aFeatures, bFeatures);

		retVal.numFeatures = Math.max(aFeatures.size(), bFeatures.size());
		retVal.similarity = (float)sameFeatures.size()/(float)retVal.numFeatures;

		return retVal;
	}

	@Override
	public Component getComponent(GraphDocument document) {
		if(settingsPanel == null) {
			settingsPanel = new JPanel(new VerticalLayout());

			placeBox = new JCheckBox("Place");
			placeBox.setSelected(includePlace);
			settingsPanel.add(placeBox);

			mannerBox = new JCheckBox("Manner");
			mannerBox.setSelected(includeManner);
			settingsPanel.add(mannerBox);

			voicingBox = new JCheckBox("Vocing");
			voicingBox.setSelected(includeVoicing);
			settingsPanel.add(voicingBox);

			heightBox = new JCheckBox("Height");
			heightBox.setSelected(includeHeight);
			settingsPanel.add(heightBox);

			backnessBox = new JCheckBox("Backness");
			backnessBox.setSelected(includeBackness);
			settingsPanel.add(backnessBox);

			tensenessBox = new JCheckBox("Tenseness");
			tensenessBox.setSelected(includeTenseness);
			settingsPanel.add(tensenessBox);
		}
		return settingsPanel;
	}

	public boolean isIncludePlace() {
		return (placeBox != null ? placeBox.isSelected() : includePlace);
	}

	public void setIncludePlace(boolean includePlace) {
		this.includePlace = includePlace;
		if(this.placeBox != null)
			this.placeBox.setSelected(includePlace);
	}

	public boolean isIncludeManner() {
		return (mannerBox != null ? mannerBox.isSelected() : includeManner);
	}

	public void setIncludeManner(boolean includeManner) {
		this.includeManner = includeManner;
		if(this.mannerBox != null)
			this.mannerBox.setSelected(includeManner);
	}

	public boolean isIncludeVoicing() {
		return (voicingBox != null ? voicingBox.isSelected() : includeVoicing);
	}

	public void setIncludeVoicing(boolean includeVoicing) {
		this.includeVoicing = includeVoicing;
		if(this.voicingBox != null)
			this.voicingBox.setSelected(includeVoicing);
	}

	public boolean isIncludeHeight() {
		return (this.heightBox != null ? heightBox.isSelected() : this.includeHeight);
	}

	public void setIncludeHeight(boolean includeHeight) {
		this.includeHeight = includeHeight;
		if(this.heightBox != null)
			this.heightBox.setSelected(includeHeight);
	}

	public boolean isIncludeBackness() {
		return (this.backnessBox != null ? this.backnessBox.isSelected() : this.includeBackness);
	}

	public void setIncludeBackness(boolean includeBackness) {
		this.includeBackness = includeBackness;
		if(this.backnessBox != null)
			this.backnessBox.setSelected(includeBackness);
	}

	public boolean isIncludeTenseness() {
		return (this.tensenessBox != null ? this.tensenessBox.isSelected() : this.includeTenseness);
	}

	public void setIncludeTenseness(boolean includeTenseness) {
		this.includeTenseness = includeTenseness;
		if(this.tensenessBox != null)
			this.tensenessBox.setSelected(includeTenseness);
	}

	public boolean isIncludeRounding() {
		return (this.roundingBox != null ? this.roundingBox.isSelected() : this.includeRounding);
	}

	public void setIncludeRounding(boolean includeRounding) {
		this.includeRounding = includeRounding;
		if(this.roundingBox != null)
			this.roundingBox.setSelected(includeRounding);
	}

	@Override
	public Properties getSettings() {
		final Properties props = new Properties();

		props.put(PhoneSimilarity.class.getName() + ".includePlace", isIncludePlace());
		props.put(PhoneSimilarity.class.getName() + ".includeManner", isIncludeManner());
		props.put(PhoneSimilarity.class.getName() + ".includeVoicing", isIncludeVoicing());

		props.put(PhoneSimilarity.class.getName() + ".includeHeight", isIncludeHeight());
		props.put(PhoneSimilarity.class.getName() + ".includeBackness", isIncludeBackness());
		props.put(PhoneSimilarity.class.getName() + ".includeTenseness", isIncludeTenseness());
		props.put(PhoneSimilarity.class.getName() + ".includeRounding", isIncludeRounding());

		return props;
	}

	@Override
	public void loadSettings(Properties properties) {
		setIncludePlace(Boolean.parseBoolean(properties.getProperty(PhoneSimilarity.class.getName() + ".includePlace", "true")));
		setIncludeManner(Boolean.parseBoolean(properties.getProperty(PhoneSimilarity.class.getName() + ".includeManner", "true")));
		setIncludeVoicing(Boolean.parseBoolean(properties.getProperty(PhoneSimilarity.class.getName() + ".includeVoicing", "true")));

		setIncludeHeight(Boolean.parseBoolean(properties.getProperty(PhoneSimilarity.class.getName() + ".includeHeight", "true")));
		setIncludeBackness(Boolean.parseBoolean(properties.getProperty(PhoneSimilarity.class.getName() + ".includeBackness", "true")));
		setIncludeTenseness(Boolean.parseBoolean(properties.getProperty(PhoneSimilarity.class.getName() + ".includeTenseness", "true")));
		setIncludeRounding(Boolean.parseBoolean(properties.getProperty(PhoneSimilarity.class.getName() + ".includeRounding", "true")));
	}
}
