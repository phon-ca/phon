/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2017, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
 * Dept of Linguistics, Memorial University <https://phon.ca>
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
package ca.phon.app.session.editor.undo;

import ca.phon.app.session.editor.SessionEditor;
import ca.phon.ipa.AlternativeTranscript;
import ca.phon.ipa.IPATranscript;
import ca.phon.session.Tier;
import ca.phon.session.Transcriber;

public class BlindTierEdit extends TierEdit<IPATranscript> {

	private static final long serialVersionUID = 3332539046795927372L;
	
	private final Transcriber transcriber;
	
	private final IPATranscript ipa;
	
	private final IPATranscript oldValue;
	
	private final IPATranscript newValue;

	public BlindTierEdit(SessionEditor editor, Tier<IPATranscript> tier,
			int groupIndex, Transcriber transcriber, IPATranscript newValue, IPATranscript ipa) {
		super(editor, tier, groupIndex, ipa);
		this.ipa = ipa;
		this.transcriber = transcriber;
		this.newValue = newValue;
		
		final AlternativeTranscript alts = ipa.getExtension(AlternativeTranscript.class);
		if(alts != null) {
			oldValue = alts.get(transcriber.getUsername());
		} else {
			oldValue = null;
		}
	}
	
	@Override
	public String getUndoPresentationName() {
		return super.getUndoPresentationName() + " (" + transcriber.getUsername() + ")";
	}
	
	@Override
	public String getRedoPresentationName() {
		return super.getRedoPresentationName() + " (" + transcriber.getUsername() + ")";
	}
	
	@Override
	public void doIt() {
		AlternativeTranscript alts = ipa.getExtension(AlternativeTranscript.class);
		if(alts == null) {
			alts = new AlternativeTranscript();
			ipa.putExtension(AlternativeTranscript.class, alts);
		}
		alts.put(transcriber.getUsername(), newValue);
		super.doIt();
	}
	
	@Override
	public void undo() {
		AlternativeTranscript alts = ipa.getExtension(AlternativeTranscript.class);
		if(alts == null) {
			alts = new AlternativeTranscript();
			ipa.putExtension(AlternativeTranscript.class, alts);
		}
		alts.put(transcriber.getUsername(), oldValue);
		super.undo();
	}
	
}
