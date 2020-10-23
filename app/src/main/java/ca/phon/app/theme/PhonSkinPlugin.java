/*
 * Copyright (C) 2005-2020 Gregory Hedlund & Yvan Rose
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at

 *    http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ca.phon.app.theme;

import java.util.*;

import org.pushingpixels.substance.api.skin.*;
import org.pushingpixels.substance.internal.plugin.*;

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
