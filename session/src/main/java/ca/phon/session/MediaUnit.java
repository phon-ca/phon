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
	Frame("f"),
	Second("s"),
	Millisecond("ms"),
	Byte("b"),
	Character("c");

	private final String unitText;

	private MediaUnit(String unitText) {
		this.unitText = unitText;
	}

	@Override
	public String toString() {
		return this.unitText;
	}

	public static MediaUnit fromString(String unitText) {
		for(MediaUnit type:MediaUnit.values()) {
			if(type.toString().equals(unitText))
				return type;
		}
		return null;
	}

}
