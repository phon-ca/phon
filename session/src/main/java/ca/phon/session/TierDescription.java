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
package ca.phon.session;

import ca.phon.extensions.*;
import ca.phon.session.spi.*;

/**
 * Name and grouped information for a tier.
 *
 */
public final class TierDescription extends ExtendableObject {
	
	private TierDescriptionSPI tierDescriptionImpl;
	
	TierDescription(TierDescriptionSPI impl) {
		super();
		this.tierDescriptionImpl = impl;
	}

	public boolean isGrouped() {
		return tierDescriptionImpl.isGrouped();
	}

	public String getName() {
		return tierDescriptionImpl.getName();
	}

	public Class<?> getDeclaredType() {
		return tierDescriptionImpl.getDeclaredType();
	}
	
}
