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
package ca.phon.app.welcome;

public class WorkspaceTextStyler {
/* Help methods for text styles */
	
	/*
	 * Button header styles
	 */
	public static String toHeaderText(String v) {
		String retVal = "";
		
		retVal += "<html><div style='";
		
		retVal += "color:black;";
		
		retVal += "'>" + v + "</div></html>";
		
		return retVal;
	}

	/*
	 * Descriptive - greyed - text
	 */
	public static String toDescText(String v) {
		String retVal = "";
		
		retVal += "<html><div style='";
		
		retVal += "color:#666666;";
		
		retVal += "'>" + v + "</div></html>";
		
		return retVal;
	}
}
