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

package ca.phon.session;

public enum Form {
	Target,
	Actual,
	Undefined;
	
//	public static final Form getTempForm(Form form) {
//		if(form == Form.Target)
//			return Form.TargetTemporary;
//		else if(form == Form.Actual)
//			return ActualTemporary;
//		else
//			return Form.Undefined;
//	}
	
	public static SystemTierType getSystemTier(Form form) {
		if(form == Form.Target)
			return SystemTierType.IPATarget;
		else if(form == Form.Actual)
			return SystemTierType.IPAActual;
		else
			return SystemTierType.Notes;
	}
	
	public static final Form getOppositeForm(Form form) {
		if(form == Form.Target)
			return Form.Actual;
		else if(form == Form.Actual)
			return Form.Target;
		else
			return Form.Undefined;
	}
}
