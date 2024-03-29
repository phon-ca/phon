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
package ca.phon.app.modules;


import org.apache.commons.cli.*;

import java.util.*;

/**
 * Custom parser for entry point arguments.
 */
public class EntryPointArgParser extends GnuParser {
	
	/**
	 * ** COPIED FROM APACHE CLI CODE for GnuParser **
	 * ** Modified to split non-options with '=' into separate tokens **
	 * 
     * This flatten method does so using the following rules:
     * <ol>
     *   <li>If an {@link Option} exists for the first character of
     *   the <code>arguments</code> entry <b>AND</b> an {@link Option}
     *   does not exist for the whole <code>argument</code> then
     *   add the first character as an option to the processed tokens
     *   list e.g. "-D" and add the rest of the entry to the also.</li>
     *   <li>Otherwise just add the token to the processed tokens list.</li>
     * </ol>
     *
     * @param options         The Options to parse the arguments by.
     * @param arguments       The arguments that have to be flattened.
     * @param stopAtNonOption specifies whether to stop flattening when
     *                        a non option has been encountered
     * @return a String array of the flattened arguments
     */
	@SuppressWarnings({"rawtypes", "unchecked"})
    protected String[] flatten(Options options, String[] arguments, boolean stopAtNonOption)
    {
        List tokens = new ArrayList();

        boolean eatTheRest = false;

        for (int i = 0; i < arguments.length; i++)
        {
            String arg = arguments[i];

            if ("--".equals(arg))
            {
                eatTheRest = true;
                tokens.add("--");
            }
            else if ("-".equals(arg))
            {
                tokens.add("-");
            }
            else if (arg.startsWith("-"))
            {
                String opt = stripLeadingHyphens(arg);

                if (options.hasOption(opt))
                {
                    tokens.add(arg);
                }
                else
                {
//                    if (opt.indexOf('=') != -1 && options.hasOption(opt.substring(0, opt.indexOf('='))))
                	if (opt.indexOf('=') != -1)
                    {
                        // the format is --foo=value or -foo=value
                        tokens.add(arg.substring(0, arg.indexOf('='))); // --foo
                        tokens.add(arg.substring(arg.indexOf('=') + 1)); // value
                    }
                    else if (options.hasOption(arg.substring(0, 2)))
                    {
                        // the format is a special properties option (-Dproperty=value)
                        tokens.add(arg.substring(0, 2)); // -D
                        tokens.add(arg.substring(2)); // property=value
                    }
                }
            }
            else
            {
                tokens.add(arg);
            }

            if (eatTheRest)
            {
                for (i++; i < arguments.length; i++)
                {
                    tokens.add(arguments[i]);
                }
            }
        }

        return (String[]) tokens.toArray(new String[tokens.size()]);
    }

	static String stripLeadingHyphens(String str)
    {
        if (str == null)
        {
            return null;
        }
        if (str.startsWith("--"))
        {
            return str.substring(2, str.length());
        }
        else if (str.startsWith("-"))
        {
            return str.substring(1, str.length());
        }

        return str;
    }
	
	@SuppressWarnings("rawtypes")
	@Override
	protected void processOption(String arg, ListIterator iter)
			throws ParseException {
		boolean hasOption = getOptions().hasOption(arg);
		
		if(!hasOption) {
//			processCustomOption(arg, iter);
		} else {
			super.processOption(arg, iter);
		}
	}

//	@SuppressWarnings("rawtypes")
//	protected void processCustomOption(String arg, ListIterator iter) 
//		throws ParseException {
//		if(arg.matches(customShortOptRegex))
//			processCustomShortOption(arg, iter);
//		else if(arg.matches(customLongOptRegex))
//			processCustomLongOption(arg, iter);
//		else
//			throw new ParseException("Unknow option: " + arg);
//	}
//	
//	@SuppressWarnings("rawtypes")
//	protected void processCustomShortOption(String arg, ListIterator iter)
//		throws ParseException {
//		final Pattern p = Pattern.compile(customShortOptRegex);
//		final Matcher m = p.matcher(arg);
//		if(m.matches()) {
//			final String key = m.group(1);
//			
//			// we need to add a new option
//			final Options options = getOptions();
//			options.addOption(key, false, null);
//			
//			super.processOption(key, iter);
//		} else {
//			throw new ParseException("Unknown option: " + arg);
//		}
//	}
//
//	@SuppressWarnings({ "rawtypes", "unchecked" })
//	protected void processCustomLongOption(String arg, ListIterator iter) 
//		throws ParseException {
//		final Pattern p = Pattern.compile(customLongOptRegex);
//		final Matcher m = p.matcher(arg);
//		if(m.matches()) {
//			final String key = m.group(1);
//			final String val = m.group(2);
//			
//			final Options options = getOptions();
//			int idx = 0;
//			String shortOpt = key.charAt(idx++) + "";
//			while(options.hasOption(shortOpt) && idx < key.length()) {
//				shortOpt = key.charAt(idx++) + "";
//			}
//			options.addOption(shortOpt, key, true, null);
//			
//			iter.add(val);
//			
//			super.processOption(key, iter);
//		} else {
//			throw new ParseException("Unknow option: " + arg);
//		}
//	}
}
