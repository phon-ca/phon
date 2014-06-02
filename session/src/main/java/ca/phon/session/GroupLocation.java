package ca.phon.app.session.editor.view.find_and_replace;

import ca.phon.util.Tuple;

public class GroupLocation extends Tuple<Integer, Integer> {
	
	public Integer getGroupIndex() {
		return super.getObj1();
	}
	
	public Integer getCharIndex() {
		return super.getObj2();
	}
	
	public void setGroupIndex(Integer groupIndex) {
		super.setObj1(groupIndex);
	}
	
	public void setCharIndex(Integer charIndex) {
		super.setObj2(charIndex);
	}

}
