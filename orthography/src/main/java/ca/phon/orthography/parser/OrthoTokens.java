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
package ca.phon.orthography.parser;

import java.io.*;
import java.net.*;
import java.util.*;

/**
 * Handles conversion from dynamic Ortho token names
 * and their integer type.
 *
 */
public class OrthoTokens {
	
	private final static org.apache.logging.log4j.Logger LOGGER = org.apache.logging.log4j.LogManager.getLogger(OrthoTokens.class.getName());
	
	private String tokenFile = "Orthography.tokens";
	
	/** Token map */
	private Map<String, Integer> tokenMap =
		new HashMap<String, Integer>();
	
	public OrthoTokens() {
		super();
		initTokenMap(getClass().getClassLoader().getResource(tokenFile));
	}
	
	private void initTokenMap(URL url) {
		try {
//			File tokenFile = new File(file);
			URLConnection uc = url.openConnection();
			
			BufferedReader in = new BufferedReader(
					new InputStreamReader(uc.getInputStream()));
			String line = null;
			while((line = in.readLine()) != null) {
				String[] vals = line.split("=");
				if(vals.length == 2) {
					String tokenName = vals[0];
					String tokenNum = vals[1];
					Integer iToken = Integer.parseInt(tokenNum);
					tokenMap.put(tokenName, iToken);
				}
			}
			in.close();
			
		} catch (IOException e) {
			LOGGER.warn(e.toString());
		}
	}
	
	/**
	 * Returns null if type is not found
	 * @param tokenName
	 * @return
	 */
	public Integer getTokenType(String tokenName) {
		Integer retVal = null;
		
		if(tokenMap.containsKey(tokenName))
			retVal = tokenMap.get(tokenName);
		
		return retVal;
	}

}
