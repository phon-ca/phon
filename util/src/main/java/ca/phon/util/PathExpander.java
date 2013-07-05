/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2008 The Phon Project, Memorial University <http://phon.ling.mun.ca>
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ca.phon.util;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Expand variables in path strings to create
 * valid OS paths.  Variables take the form of:
 * ${varname}.
 * 
 * Default variables:
 *  - ${workspace} - path to current workspace
 *  - ${pwd} - current working path
 *  - ${desktop} - user desktop
 *  - ${home} - user home
 *  - ${prefs} - path to application preferences
 *  - ${tmp} - path to temporary files
 *
 */
public class PathExpander {
	
	public final static String WORKSPACE_VAR = "workspace";
	public final static String PWD_VAR = "pwd";
	public final static String DESKTOP_VAR = "desktop";
	public final static String HOME_VAR = "home";
	public final static String PREFS_VAR = "prefs";
	public final static String TMP_VAR = "tmp";
	
	private final String varnameRegex = "[a-zA-Z][a-zA-Z0-9]*";
	private final String varmatchRegex = "\\$\\{(" + varnameRegex + ")\\}";
	
	/**  map for variables */
	private Map<String, String> varMap = 
		new HashMap<String, String>();
	
	/**
	 * Constructor
	 */
	public PathExpander() {
		super();
		
		initVars();
	}
	
	/**
	 * init default vars
	 */
	private void initVars() {
		// add workspace variable
		putVar(WORKSPACE_VAR, PrefHelper.get("ca.phon.workspace.Workspace.workspaceFolder", ""));
		
		putVar(PWD_VAR, System.getProperty("user.dir"));
		
		putVar(DESKTOP_VAR, System.getProperty("user.home") + File.separator + "Desktop");
		
		putVar(HOME_VAR, System.getProperty("user.home"));
		
//		putVar(PREFS_VAR, UserPrefManager.getUserPrefDir());
		
		putVar(TMP_VAR, System.getProperty("java.io.tmpdir"));
	}
	
	/**
	 * add variable mapping
	 * 
	 * @param varname
	 * @param path
	 */
	public void putVar(String varname, String path) {
		if(!varname.matches(varnameRegex))
			throw new IllegalArgumentException("varname must match expression '" + varnameRegex + "'");
		varMap.put(varname, path);
	}
	
	/**
	 * remove variable mapping
	 * 
	 * @param varname
	 */
	public void removeVar(String varname) {
		varMap.remove(varname);
	}
	
	/**
	 * get value for variable
	 * 
	 * @param varname
	 * @return path
	 */
	public String getVar(String varname) {
		return varMap.get(varname);
	}
	
	/**
	 * reverse lookup for variables
	 * 
	 * @param path
	 * @return varname
	 */
	public String getVarForPath(String path) {
		String retVal = null;
		
		for(String k:varMap.keySet()) {
			String p = varMap.get(k);
			if(p.equals(path)) {
				retVal = k;
				break;
			}
		}
		
		return retVal;
	}
	
	/**
	 * Expand string with variables into full OS path
	 * 
	 * @param varpath
	 * @param fullpath
	 */
	public String expandPath(String varpath) {
		Pattern varPattern = Pattern.compile(varmatchRegex);
		return expandPathR(varpath, varPattern);
	}
	
	/**
	 * internal recursive method for expanding path
	 */
	private String expandPathR(String varpath, Pattern p) {
		String retVal = varpath;
		
//		Pattern p = Pattern.compile(varmatchRegex);
		Matcher m = p.matcher(varpath);
		if(m.find()) {
			String varname = m.group(1);
			String path = getVar(varname);
			if(path != null) {
				retVal = 
					retVal.substring(0, m.start()) +
					path +
					retVal.substring(m.end());
				
				// check for more variable expansions
				return expandPath(retVal);
			}
		}
		
		return retVal;
	}
	
	/**
	 * Compress path - does the opposite of expand path.
	 * 
	 * @param fullpath
	 * @param varpath
	 */
	public String compressPath(String path) {
		// order keys by string length
		List<String> paths = new ArrayList<String>();
		paths.addAll(varMap.values());
		Collections.sort(paths, new Comparator<String>() {

			@Override
			public int compare(String o1, String o2) {
				Integer o1len = o1.length();
				Integer o2len = o2.length();
				
				// descending order
				return o2len.compareTo(o1len);
			}
		});
		
		return compressPathR(path, paths);
	}
	
	private String compressPathR(String path, List<String> paths) {
		String retVal = path;
		
		for(String p:paths) {
			int idx = path.indexOf(p);
			if(idx >= 0) {
				String varname = getVarForPath(p);
				if(varname != null) {
					retVal = 
						retVal.substring(0, idx) +
						"${" + varname + "}" +
						retVal.substring(p.length());
					
					// check for more substitutions
					return compressPathR(retVal, paths);
				}
			}
		}
		
		return retVal;
	}
	
}
