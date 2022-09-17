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
package ca.phon.script.params;

import ca.phon.visitor.*;
import org.antlr.runtime.*;

import java.io.*;
import java.security.*;
import java.util.*;
import java.util.regex.*;

/**
 *
 */
public class ScriptParameters extends ArrayList<ScriptParam> implements Visitable<ScriptParam> {

	private static final long serialVersionUID = -7240889391306198318L;

	private final static org.apache.logging.log4j.Logger LOGGER = org.apache.logging.log4j.LogManager.getLogger(ScriptParameters.class.getName());

	/**
	 * Regular expression used to detect parameters comment in script
	 */
	private final static String PARAM_REGEX = "/\\*\\s*(params.*;)\\s*\\*/";

	/**
	 * Extract the first params comment section found in the
	 * given script text.
	 *
	 * @param script
	 *
	 * @return parameters section if found, <code>null</code>
	 *  if not found
	 */
	public static String extractParamsComment(String text) {
		final Pattern pattern = Pattern.compile(PARAM_REGEX, java.util.regex.Pattern.MULTILINE | Pattern.DOTALL );
		final Matcher matcher = pattern.matcher(text);
		if(matcher.find()) {
			return matcher.group(1);
		} else {
			return "";
		}
	}

	/**
	 * Extract script parameters from the comments of the given
	 * text.  The text must match the syntax defined by the
	 * ScriptParams.g ANTLR grammar.
	 *
	 * @param text
	 * @return the list of script parameters defined within the
	 *  script comments
	 */
	public static ScriptParameters parseScriptParams(String text) {
		final ScriptParameters retVal = new ScriptParameters();
		try {
			final ByteArrayInputStream stream = new ByteArrayInputStream(text.getBytes("UTF-8"));
			final ANTLRInputStream input = new ANTLRInputStream(stream);
			final ScriptParamsLexer lexer = new ScriptParamsLexer(input);
			final CommonTokenStream tokens = new CommonTokenStream(lexer);
			final ScriptParamsParser parser = new ScriptParamsParser(tokens);
			parser.params();
			final ScriptParam[] params = parser.getScriptParams();
			retVal.addAll(Arrays.asList(params));
		} catch (IOException e) {
			LOGGER.error( e.getLocalizedMessage(), e);
		} catch (RecognitionException e) {
			LOGGER.error( e.getLocalizedMessage(), e);
		}
		return retVal;
	}

	/**
	 * Copies any values from oldParams which have matching
	 * ids in newParams.
	 *
	 * @param oldParams
	 * @param newParams
	 */
	public static void copyParams(ScriptParameters oldParams, ScriptParameters newParams) {
		for(ScriptParam sp:newParams) {
			for(String pId:sp.getParamIds()) {

				// find the matching pId in oldParams (if exists)
				ScriptParam oldParam = null;
				for(ScriptParam oldSp:oldParams) {
					if(oldSp.getParamIds().contains(pId)) {
						oldParam = oldSp;
					}
				}

				// make sure the type of the param has not changed
				if(oldParam != null && oldParam.getParamType().equals(sp.getParamType())) {
					sp.setValue(pId, oldParam.getValue(pId));
				}
			}
		}
	}

	public ScriptParameters() {
		super();
	}

	/**
	 * Copy param values from the given script parameters
	 *
	 * @param params
	 */
	public void copyParams(ScriptParameters newParams) {
		ScriptParameters.copyParams(this, newParams);
	}


	/**
	 * Set the value of the specified paramId
	 *
	 * @param paramId
	 * @param value
	 *
	 * @return old value of the specified paramId
	 */
	public Object setParamValue(String paramId, Object val) {
		Object retVal = null;
		for(ScriptParam param:this) {
			if(param.getParamIds().contains(paramId)) {
				retVal = param.getValue(paramId);
				param.setValue(paramId, val);
			}
		}
		return retVal;
	}

	/**
	 * Get value of given paramId
	 *
	 * @param paramId
	 *
	 * @return value of paramId
	 */
	public Object getParamValue(String paramId) {
		Object retVal = null;
		for(ScriptParam param:this) {
			if(param.getParamIds().contains(paramId)) {
				retVal = param.getValue(paramId);
				break;
			}
		}
		return retVal;
	}

	/**
	 * Get the set of paramIds
	 *
	 * @return set of paramIds
	 */
	public Set<String> getParamIds() {
		final Set<String> retVal = new LinkedHashSet<String>();
		for(ScriptParam param:this) {
			retVal.addAll(param.getParamIds());
		}
		return retVal;
	}

	/**
	 * Get the grouped param lists
	 *
	 * @return list of scriptparameters
	 */
	private List<ScriptParamGroup> getParamGroups() {
		List<ScriptParamGroup> retVal = new ArrayList<ScriptParamGroup>();

		ScriptParam currentSep = null;
		List<ScriptParam> currentGrp = null;
		for(int i = 0; i < size(); i++) {
			ScriptParam current = get(i);
			if(current.getParamType().equals("separator")) {
				if(currentGrp != null) {
					ScriptParamGroup grp = new ScriptParamGroup(currentSep, currentGrp);
					retVal.add(grp);
				}
				currentSep = current;
				currentGrp = new ArrayList<ScriptParam>();
			} else {
				currentGrp.add(current);
			}
		}

		if(currentGrp != null && currentGrp.size() > 0) {
			ScriptParamGroup grp = new ScriptParamGroup(currentSep, currentGrp);
			retVal.add(grp);
		}

		return retVal;
	}

	public void loadFromMap(Map<String, Object> paramMap) {
		for(ScriptParam sp:this) {
			for(String id:sp.getParamIds()) {
				Object v = paramMap.get(id);
				if(v != null) {
					sp.setValue(id, v);
				}
			}
		}
	}
	
	public String getHashString() {
		final StringBuffer buffer = new StringBuffer();
		for(ScriptParam param:this) {	
			if(param instanceof SeparatorScriptParam) continue; // don't include separator collapsed state
			for(String paramId:param.getParamIds()) {
				buffer.append(paramId).append("=").append(param.getValue(paramId)).append("\n");
			}
		}
		
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA1");
			byte[] hash = digest.digest(buffer.toString().getBytes());
			
			buffer.setLength(0);
			for(int i = 0; i < hash.length; i++) {
				if((0xff & hash[i]) < 0x10) {
					buffer.append('0');
				}
				buffer.append(Integer.toHexString(0xff & hash[i]));
			}
			return buffer.toString();
		} catch (NoSuchAlgorithmException e) {
			LOGGER.error( e.getLocalizedMessage(), e);
		}
		
		// shouldn't get here!
		return Integer.toHexString(buffer.hashCode());
	}
	
	public String toHTMLString() {
		return toHTMLString(false);
	}
	
	public String toHTMLString(boolean modifiedOnly) {
		return toHTMLString(modifiedOnly, List.of(), List.of());
	}
	
	/**
	 * Return an HTML formatted version of the script parameters.
	 * 
	 * @param modifiedOnly
	 * @param includes
	 * @param excludes
	 * 
	 * @return
	 */
	public String toHTMLString(boolean modifiedOnly, List<String> includes, List<String> excludes) {
		final ScriptParametersToHTML toHTML = new ScriptParametersToHTML();
		toHTML.setPrintOnlyChanged(modifiedOnly);
		toHTML.setIncludes(includes);
		toHTML.setExcludes(excludes);
		accept(toHTML);
		return toHTML.getHTML();
	}
	
	@Override
	public void accept(Visitor<ScriptParam> visitor) {
		for(ScriptParam param:this)
			visitor.visit(param);
	}

}
