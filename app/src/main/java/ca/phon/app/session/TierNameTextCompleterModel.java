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
package ca.phon.app.session;

import ca.phon.session.*;
import ca.phon.ui.text.DefaultTextCompleterModel;

public class TierNameTextCompleterModel extends DefaultTextCompleterModel {
	
	private Session session;
	
	public TierNameTextCompleterModel(Session session) {
		super();
		this.session = session;
		
		super.addCompletion(SystemTierType.Orthography.getName());
		super.addCompletion(SystemTierType.IPATarget.getName());
		super.addCompletion(SystemTierType.IPAActual.getName());
		super.addCompletion(SystemTierType.Notes.getName());
		
		for(TierDescription tierDesc:session.getUserTiers()) {
			super.addCompletion(tierDesc.getName());
		}
	}
	
}
