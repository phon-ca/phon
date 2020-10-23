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
package ca.phon.app.opgraph.nodes.table;

import java.util.*;

import ca.phon.ipa.*;

/**
 * Implemented by inventory settings objects which use the ignore diacritics
 * option set.
 */
public interface IgnoreDiacriticsSettings {

	public boolean isIgnoreDiacritics();
	
	public void setIgnoreDiacritics(boolean ignoreDiacritics);
	
	public boolean isOnlyOrExcept();
	
	public void setOnlyOrExcept(boolean onlyOrExcept);
	
	public Collection<Diacritic> getSelectedDiacritics();
	
	public void setSelectedDiacritics(Collection<Diacritic> selectedDiacritics);
	
}
