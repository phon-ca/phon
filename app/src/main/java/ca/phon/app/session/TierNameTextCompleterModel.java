package ca.phon.app.session;

import ca.phon.session.Session;
import ca.phon.session.SystemTierType;
import ca.phon.session.TierDescription;
import ca.phon.ui.text.DefaultTextCompleterModel;

public class TierNameTextCompleterModel extends DefaultTextCompleterModel {
	
	private Session session;
	
	public TierNameTextCompleterModel(Session session) {
		super();
		this.session = session;
		
		super.addCompletion(SystemTierType.Orthography.getName());
		super.addCompletion(SystemTierType.IPATarget.getName());
		super.addCompletion(SystemTierType.IPAActual.getName());
		super.addCompletion(SystemTierType.Notes.getName());
		
		for(TierDescription tierDesc:session.getUserTiers()) {
			super.addCompletion(tierDesc.getName());
		}
	}
	
}
