package ca.phon.app.opgraph.nodes;

import ca.phon.app.session.editor.search.ParticipantCellRenderer;
import ca.phon.opgraph.*;
import ca.phon.opgraph.exceptions.ProcessingException;
import ca.phon.session.Participant;

import java.util.*;

/**
 * Node used in all anlyses representing the set of selected
 * participants for the analysis. When combineParticipants
 * is true the output list will be a single item list
 * containing {@link ca.phon.session.Participant}.ALL.
 *
 */
@OpNodeInfo(name = "Selected Participants", description = "Selected participants for analysis", category = "Objects", showInLibrary = true)
public class SelectedParticipantsNode extends OpNode {

	/*
	 * Set of participants as selected in analysis wizard
	 */
	public final static String SELECTED_PARTICIPANTS_GLOBAL_ID = "_selectedParticipants";

	private InputField combineParticipantsInputField =
			new InputField("combine", "Combine selected participants", true, true, Boolean.class);

	private OutputField selectedParticipantsOutputField =
			new OutputField("participants", "Set of selected participants", true, ArrayList.class);

	public SelectedParticipantsNode() {
		super();

		putField(combineParticipantsInputField);
		putField(selectedParticipantsOutputField);
	}

	@Override
	public void operate(OpContext opContext) throws ProcessingException {
		Object pObj = opContext.get(SELECTED_PARTICIPANTS_GLOBAL_ID);
		if(pObj == null || !(pObj instanceof Collection))
			throw new ProcessingException(null, "No selected participants");

		@SuppressWarnings("unchecked")
		Collection<Participant> selectedParticipants = (Collection<Participant>)pObj;

		boolean combineParticipants = false;
		if(opContext.get(combineParticipantsInputField) != null) {
			combineParticipants = Boolean.parseBoolean(opContext.get(combineParticipantsInputField).toString());
		}

		if(combineParticipants) {
			opContext.put(selectedParticipantsOutputField, new ArrayList<>(List.of(Participant.ALL)));
		} else {
			opContext.put(selectedParticipantsOutputField, new ArrayList<>(selectedParticipants));
		}
	}

}
