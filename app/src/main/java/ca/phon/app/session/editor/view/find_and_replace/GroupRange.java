package ca.phon.app.session.editor.view.find_and_replace;

import ca.phon.util.Range;
import ca.phon.util.Tuple;

public class GroupRange extends Tuple<Integer, Range> {
	
	public GroupRange(Integer grpIndex, Range charRange) {
		super(grpIndex, charRange);
	}
	
	public Integer getGroupIndex() {
		return super.getObj1();
	}
	
	public void setGroupIndex(Integer grpIndex) {
		super.setObj1(grpIndex);
	}
	
	public Range getRange() {
		return super.getObj2();
	}
	
	public void setRange(Range r) {
		super.setObj2(r);
	}

}
