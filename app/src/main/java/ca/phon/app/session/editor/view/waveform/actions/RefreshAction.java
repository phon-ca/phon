package ca.phon.app.session.editor.view.waveform.actions;

import java.awt.event.ActionEvent;

import javax.swing.ImageIcon;

import ca.phon.app.session.editor.SessionEditor;
import ca.phon.app.session.editor.view.waveform.WaveformEditorView;
import ca.phon.app.session.editor.view.waveform.WaveformTier;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;

public class RefreshAction extends WaveformEditorViewAction {

	private static final long serialVersionUID = 2541481642552447379L;
	
	private final static String CMD_NAME = "Refresh";
	
	private final static String SHORT_DESC = "Refresh display";
	
	private final static ImageIcon ICON =
			IconManager.getInstance().getIcon("actions/reload", IconSize.SMALL);

	public RefreshAction(SessionEditor editor, WaveformEditorView view) {
		super(editor, view);
		
		putValue(NAME, CMD_NAME);
		putValue(SHORT_DESCRIPTION, SHORT_DESC);
		putValue(SMALL_ICON, ICON);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		getView().update();
		for(WaveformTier tier:getView().getPluginTiers()) {
			tier.onRefresh();
		}
	}

}
