package ca.phon.app.session.editor.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import ca.phon.extensions.Extension;
import ca.phon.orthography.OrthoWord;

/**
 * Extension for {@link OrthoWord} objects providing
 * a list of transcriptions for the given Orthography
 *
 */
@Extension(value=OrthoWord.class)
public class OrthoWordIPAOptions {
	
	private int selectedOption = 0;
	
	private final List<String> options = Collections.synchronizedList(new ArrayList<String>());
	
	public OrthoWordIPAOptions() {
		super();
	}
	
	public OrthoWordIPAOptions(String[] opts) {
		this(Arrays.asList(opts));
	}
	
	public OrthoWordIPAOptions(List<String> opts) {
		super();
		this.options.addAll(opts);
	}
	
	public List<String> getOptions() {
		return Collections.unmodifiableList(options);
	}

	public void setOptions(List<String> options) {
		this.options.clear();
		this.options.addAll(options);
	}
	
	public int getSelectedOption() {
		return this.selectedOption;
	}
	
	public void setSelectedOption(int option) {
		this.selectedOption = option;
	}
	
}
