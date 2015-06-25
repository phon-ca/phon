package ca.phon.app.query.analysis.actions;

import java.awt.event.ActionEvent;

import ca.phon.app.hooks.HookableAction;
import ca.phon.app.query.analysis.Assessment;
import ca.phon.project.Project;
import ca.phon.session.Session;
import ca.phon.ui.CommonModuleFrame;
import ca.phon.worker.PhonWorker;

public class AssessmentAction extends HookableAction {

	private CommonModuleFrame owner;
	
	private Assessment assessment;
	
	public AssessmentAction(CommonModuleFrame owner, Assessment assessment) {
		super();
		this.owner = owner;
		this.assessment = assessment;
		
		putValue(NAME, assessment.getName());
	}
	
	public CommonModuleFrame getOwner() {
		return this.owner;
	}
	
	public Assessment getAssessment() {
		return this.assessment;
	}
	
	@Override
	public void hookableActionPerformed(ActionEvent ae) {
		final CommonModuleFrame owner = getOwner();
		final Assessment assessment = getAssessment();
		
		if(owner.getExtension(Project.class) != null)
			assessment.getBindings().put("_project", owner.getExtension(Project.class));
		if(owner.getExtension(Session.class) != null)
			assessment.getBindings().put("_session", owner.getExtension(Session.class));
		
		PhonWorker.getInstance().invokeLater(assessment);
	}

}
