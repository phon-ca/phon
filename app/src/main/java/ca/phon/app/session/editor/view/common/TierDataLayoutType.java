/*
 * Copyright (C) 2012-2018 Gregory Hedlund & Yvan Rose
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
package ca.phon.app.session.editor.view.common;

import org.apache.logging.log4j.LogManager;

/**
 * Enumeration of available tier data layout types.
 *
 */
public enum TierDataLayoutType {
	ALIGN_GROUPS(AlignGroupsLayoutProvider.class),
	WRAP_GROUPS(WrapGroupsLayoutProvider.class);
	
	private static final org.apache.logging.log4j.Logger LOGGER = LogManager.getLogger(TierDataLayoutType.class.getName());
	
	private Class<? extends TierDataLayoutProvider> providerClass;
	
	private TierDataLayoutType(Class<? extends TierDataLayoutProvider> providerClass) {
		this.providerClass = providerClass;
	}
	
	public TierDataLayoutProvider createLayoutProvider() {
		TierDataLayoutProvider retVal = null;
		try {
			retVal = providerClass.newInstance();
		} catch (InstantiationException e) {
			LOGGER.error( e.getLocalizedMessage(), e);
		} catch (IllegalAccessException e) {
			LOGGER.error( e.getLocalizedMessage(), e);
		}
		return retVal;
	}
	
}
