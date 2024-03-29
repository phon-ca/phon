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
package ca.phon.session.format;

import ca.phon.formatter.*;
import ca.phon.session.TierString;

import java.text.ParseException;

@FormatterType(TierString.class)
public class TierStringFormatter implements Formatter<TierString> {

	@Override
	public String format(TierString obj) {
		return obj.toString();
	}

	@Override
	public TierString parse(String text) throws ParseException {
		return new TierString(text);
	}

}
