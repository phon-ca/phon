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
package ca.phon.app.session.check;

import ca.phon.plugin.*;
import ca.phon.session.check.*;

/**
 * Create configuration forms for {@link SessionCheck}s.
 *
 */
public class SessionCheckUIFactory {
	
	public SessionCheckUIFactory() {
		super();
	}
	
	/**
	 * Create UI for the given session check.  Will return <code>null</code>
	 * if no UI is registered for the given check.
	 * 
	 * @param check
	 * @return UI panel for check or <code>null</code>
	 */
	public SessionCheckUI createUI(SessionCheck check) {
		Class<? extends SessionCheck> checkType = check.getClass();
		
		for(IPluginExtensionPoint<SessionCheckUI> extPt:PluginManager.getInstance().getExtensionPoints(SessionCheckUI.class)) {
			SessionCheckTarget targetType = extPt.getClass().getAnnotation(SessionCheckTarget.class);
			if(targetType != null && targetType.value() == checkType) {
				return extPt.getFactory().createObject(check);
			}
		}
		return null;
	}

}
