package ca.phon.app.theme;

import java.util.HashSet;
import java.util.Set;

import org.pushingpixels.substance.api.skin.SkinInfo;
import org.pushingpixels.substance.internal.plugin.SubstanceSkinPlugin;

public class PhonSkinPlugin implements SubstanceSkinPlugin {

	@Override
	public Set<SkinInfo> getSkins() {
		final HashSet<SkinInfo> retVal = new HashSet<SkinInfo>();
		
		final SkinInfo phonSkinInfo = new SkinInfo(PhonSubstanceSkin.NAME, PhonSubstanceSkin.class.getName());
		phonSkinInfo.setDefault(true);
		retVal.add(phonSkinInfo);
		
		return retVal;
	}

	@Override
	public String getDefaultSkinClassName() {
		return "Phon";
	}

}
