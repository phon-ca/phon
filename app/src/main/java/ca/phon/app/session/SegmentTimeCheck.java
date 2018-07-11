package ca.phon.app.session;

import java.util.Map;

import ca.phon.app.session.editor.SessionEditor;
import ca.phon.app.session.editor.view.speech_analysis.SpeechAnalysisEditorView;
import ca.phon.plugin.IPluginExtensionFactory;
import ca.phon.plugin.IPluginExtensionPoint;
import ca.phon.session.MediaSegment;
import ca.phon.session.Record;
import ca.phon.session.Session;
import ca.phon.session.SystemTierType;
import ca.phon.session.Tier;
import ca.phon.session.check.SessionCheck;
import ca.phon.session.check.SessionValidator;

public class SegmentTimeCheck implements SessionCheck, IPluginExtensionPoint<SessionCheck> {

	@Override
	public void checkSession(SessionValidator validator, Session session, Map<String, Object> options) {
		final SessionEditor editor = validator.getExtension(SessionEditor.class);
		if(editor == null) return;
		
		final SpeechAnalysisEditorView speechAnalysisView = 
				(SpeechAnalysisEditorView)editor.getViewModel().getView(SpeechAnalysisEditorView.VIEW_TITLE);
		if(speechAnalysisView == null || speechAnalysisView.getWavDisplay().getSampled() == null) return;
		
		float maxTimeMS = speechAnalysisView.getWavDisplay().getSampled().getLength() * 1000.0f;
		
		for(int i = 0; i < session.getRecordCount(); i++) {
			final Record r = session.getRecord(i);
			final Tier<MediaSegment> segmentTier = r.getSegment();
			if(segmentTier != null && segmentTier.numberOfGroups() > 0) {
				final MediaSegment segment = segmentTier.getGroup(0);
				
				if(segment.getEndValue() > maxTimeMS) {
					// fire warning
					validator.fireValidationEvent(session, i, SystemTierType.Segment.getName(), 0, "Segment time exceeds media length");
				}
			}
		}
	}

	@Override
	public Class<?> getExtensionType() {
		return SessionCheck.class;
	}

	@Override
	public IPluginExtensionFactory<SessionCheck> getFactory() {
		return (Object ... args) -> {
			return SegmentTimeCheck.this;
		};
	}

}
