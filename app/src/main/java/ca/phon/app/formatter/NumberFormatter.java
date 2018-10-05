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
package ca.phon.app.formatter;

import java.text.DecimalFormat;
import java.text.ParseException;

import ca.phon.formatter.Formatter;
import ca.phon.formatter.FormatterType;
import ca.phon.util.PrefHelper;

@FormatterType(value=Number.class)
public class NumberFormatter implements Formatter<Number> {

	public final static String MIN_PRECISION = NumberFormatter.class.getName() + ".minPrecision";
	public final int DEFAULT_MIN_PRECISION = -1;
	private int minPrecision = PrefHelper.getInt(MIN_PRECISION, DEFAULT_MIN_PRECISION);
	
	public final static String MAX_PRECISION = NumberFormatter.class.getName() + ".maxPrecision";
	public final int DEFAULT_MAX_PRECISION = 3;
	private int maxPrecision = PrefHelper.getInt(MAX_PRECISION, DEFAULT_MAX_PRECISION);
	
	public NumberFormatter() {
	}

	@Override
	public String format(Number obj) {
		final DecimalFormat formatter = new DecimalFormat();
		if(maxPrecision >= 0)
			formatter.setMaximumFractionDigits(maxPrecision);
		if(minPrecision >= 0)
			formatter.setMinimumFractionDigits(minPrecision);
		
		return formatter.format(obj);
	}

	@Override
	public Number parse(String text) throws ParseException {
		if(text.indexOf('.') >= 0) {
			return Double.parseDouble(text);
		} else {
			return Integer.parseInt(text);
		}
	}

}
