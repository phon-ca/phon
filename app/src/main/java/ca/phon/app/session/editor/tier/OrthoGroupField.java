package ca.phon.app.session.editor.tier;

import ca.phon.app.session.editor.SessionEditor;
import ca.phon.orthography.Orthography;
import ca.phon.session.Tier;

/**
 * Tier editor for {@link Orthography} type.
 */
public class OrthoGroupField extends GroupField<Orthography> {

	private static final long serialVersionUID = -7358501453702966912L;

	public OrthoGroupField(SessionEditor editor, Tier<Orthography> tier,
			int groupIndex) {
		super(editor, tier, groupIndex);
	}

}
