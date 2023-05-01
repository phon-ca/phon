package ca.phon.orthography;/*
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
import java.text.*;
import java.util.regex.*;


/**
 * Text formatter for time in <pre>(min:)?s.(ms)?</pre>
 */
public class MediaTimeFormat extends Format {

    public final static String PATTERN = "(?:([0-9]{0,3})\\:)?([0-9]{1,2})\\.([0-9]{0,3})";

    @Override
    public StringBuffer format(Object obj, StringBuffer toAppendTo,
                               FieldPosition pos) {
        Long toParse = null;
        StringBuffer retVal = new StringBuffer();

        if(obj instanceof Number) {
            retVal.append(this.sToDisplayString(((Number)obj).floatValue()));
        }

        return retVal;
    }

    protected String sToDisplayString(float seconds)
            throws IllegalArgumentException {
        if(seconds < 0)
            throw new IllegalArgumentException("Time cannot be negative.");

        long ms = Math.round(seconds * 1000.0f);

        long numSeconds = ms / 1000;
        long numMSecondsLeft = ms % 1000;

        long numMinutes = numSeconds / 60;
        long numSecondsLeft = numSeconds % 60;

        NumberFormat secondsFormat = NumberFormat.getIntegerInstance();
        if(numMinutes > 0) {
            secondsFormat.setMinimumIntegerDigits(2);
        }

        NumberFormat minutesFormat = NumberFormat.getIntegerInstance();

        NumberFormat fracSecondsFormat = NumberFormat.getNumberInstance();
        fracSecondsFormat.setMaximumIntegerDigits(0);

        String minuteString = (numMinutes > 0 ? minutesFormat.format(numMinutes) + ":" : "");

        String secondString =
                (numMinutes == 0
                        ? (secondsFormat.format(numSeconds))
                        : (secondsFormat.format(numSecondsLeft))
                );

        String msString = numMSecondsLeft > 0 ?
                (fracSecondsFormat.format(numMSecondsLeft/1000.0f)) : ".";

        String timeString =
                minuteString + secondString + msString;

        return timeString;
    }

    @Override
    public Object parseObject(String source, ParsePosition pos) {
        Float retVal = Float.NaN;
        final Pattern pattern = Pattern.compile(PATTERN);
        final Matcher matcher = pattern.matcher(source);
        if (matcher.matches()) {
            final String minString = matcher.group(1);
            int mins = 0;
            if(minString != null)
                mins = Integer.parseInt(minString);

            final String secString = matcher.group(2);
            final int secs = mins * 60 + Integer.parseInt(secString);

            final String fracSecString = matcher.group(3);
            final String timeInS = Integer.toString(secs) + "." + fracSecString;
            final float valueS = Float.parseFloat(timeInS);
            retVal = Float.valueOf(valueS);
            pos.setIndex(source.length());
        } else {
            throw new IllegalArgumentException(source);
        }
        return retVal;
    }

}
