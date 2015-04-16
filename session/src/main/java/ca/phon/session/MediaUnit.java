/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2015 The Phon Project, Memorial University <https://phon.ca>
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

public enum MediaUnit {
	/*
	 * <xs:simpleType name="mediaUnitType">
    <xs:restriction base="xs:NMTOKEN">
      <xs:enumeration value="f">
        <xs:annotation>
          <xs:documentation> frame</xs:documentation>
        </xs:annotation>
      </xs:enumeration>
      <xs:enumeration value="s">
        <xs:annotation>
          <xs:documentation> second</xs:documentation>
        </xs:annotation>
      </xs:enumeration>
      <!-- MUN added -->
      <xs:enumeration value="ms">
        <xs:annotation>
          <xs:documentation> millisecond</xs:documentation>
        </xs:annotation>
      </xs:enumeration>
      <xs:enumeration value="b">
        <xs:annotation>
          <xs:documentation> byte</xs:documentation>
        </xs:annotation>
      </xs:enumeration>
      <xs:enumeration value="c">
        <xs:annotation>
          <xs:documentation> character</xs:documentation>
        </xs:annotation>
      </xs:enumeration>
    </xs:restriction>
  </xs:simpleType>
	 */
	Frame,
	Second,
	Millisecond,
	Byte,
	Character,
	Undefined
}
