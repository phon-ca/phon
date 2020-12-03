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
package ca.phon.app;

import java.io.*;
import java.util.*;
import java.util.regex.*;

import com.github.zafarkhaja.semver.*;

import ca.phon.app.log.*;
import ca.phon.util.*;

/**
 * Methods for determining the version number of the application.
 * 
 * Version information is read from the file: phon.build.properties
 * which should be located in the root of the class loader.
 */
public class VersionInfo implements Comparable<VersionInfo> {

	/* semantic version */
	private Version semver;
	
	public final static String PHON_VERSION_PROP = "phon.app.version";
	
	/**
	 * Properties file 
	 */
	private final static String VERSION_PROP_FILE = "phon.build.properties";
		
	// dev version
	private final static String DEV_VERSION = "3.2.1-dev";
	
	/*
	 * regex pattern for syntax of previous versions
	 */
	private final static String PREV_VERSION_REGEX = "([0-9]+\\.[0-9]+\\.[0-9]+)(([ab])([0-9]+))?( \\(([a-zA-Z0-9.]+)\\))?";
	
	/**
	 * Get the shared instance
	 */
	public static VersionInfo getInstance() {
		String versionStr = PrefHelper.get(PHON_VERSION_PROP, DEV_VERSION);
		
		InputStream is = VersionInfo.class.getClassLoader().getResourceAsStream(VERSION_PROP_FILE);
		if(is != null) {
			Properties props = new Properties();
			try {
				props.load(is);
			} catch (IOException e) {
				LogUtil.severe(e);
			}
			
			if(props.containsKey(PHON_VERSION_PROP)) {
				versionStr = props.getProperty(PHON_VERSION_PROP);
			}
		}
		
		return new VersionInfo(versionStr);
	}
		
	
	public VersionInfo(String version) {
		super();
		
		// convert to semantic version format if necessary
		version = convertIfNecessary(version);
		
		this.semver = Version.valueOf(version);
	}
	
	private String convertIfNecessary(String version) {
		final Pattern p = Pattern.compile(PREV_VERSION_REGEX);
		final Matcher m = p.matcher(version);
		if(m.matches()) {
			StringBuilder sb = new StringBuilder();
			sb.append(m.group(1));
			
			if(m.group(2) != null) {
				sb.append('-')
					.append(m.group(3))
					.append('.')
					.append(m.group(4));
			}
			
			if(m.group(5) != null) {
				sb.append('+')
					.append(m.group(6));
			}
			return sb.toString();
		} else {
			return version;
		}
	}
		
	public boolean isDevVersion() {
		return semver.getPreReleaseVersion().startsWith("dev");
	}
	
	/**
	 * Check if this version matches the given
	 * version test string.
	 * 
	 * See {@link Version#satisfies(java.lang.String)}
	 * 
	 * @param vertest
	 * @return <code>true</code> if this version matches given
	 *  test string, <code>false</code> otherwise
	 */
	public boolean check(String vertest) {
		return semver.satisfies(vertest);
	}
	
	public int getMajorVersion() {
		return semver.getMajorVersion();
	}
	
	public int getMinorVersion() {
		return semver.getMinorVersion();
	}
	
	public int getPatchVersion() {
		return semver.getPatchVersion();
	}
	
	public String getPreRelease() {
		return semver.getPreReleaseVersion();
	}
	
	public String getBuild() {
		return semver.getBuildMetadata();
	}
	
	/**
	 * Returns full version text including prerelease and
	 * build values.
	 * 
	 * @return
	 */
	public String getVersion() {
		return this.semver.toString();
	}
	
	/**
	 * Return the version w/o build
	 * 
	 * @return version string w/o build 
	 */
	public String getVersionNoBuild() {
		StringBuilder sb = new StringBuilder();
		sb.append(getNumericalVersion());
		if(getPreRelease().length() > 0) {
			sb.append('-').append(getPreRelease());
		}
		return sb.toString();
	}
	
	/**
	 * Return the numerical version (major.minor.patch)
	 * 
	 * @return
	 */
	public String getNumericalVersion() {
		return getMajorVersion() + "." + getMinorVersion() + "." + getPatchVersion();
	}
	
	@Override
	public String toString() {
		return getVersion();
	}

	@Override
	public int compareTo(VersionInfo o) {
		return semver.compareTo(o.semver);
	}
	
	public int compareTo(String version) {
		return compareTo(new VersionInfo(version));
	}
	
}
