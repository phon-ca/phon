package ca.phon.app.session.editor.view.tier_management.actions;

import java.awt.event.ActionEvent;

import javax.swing.ImageIcon;

import org.apache.commons.lang3.StringUtils;

import ca.phon.app.session.editor.SessionEditor;
import ca.phon.app.session.editor.undo.AddTierEdit;
import ca.phon.app.session.editor.view.tier_management.TierEditorDialog;
import ca.phon.app.session.editor.view.tier_management.TierInfoEditor;
import ca.phon.app.session.editor.view.tier_management.TierOrderingEditorView;
import ca.phon.session.Session;
import ca.phon.session.SystemTierType;
import ca.phon.session.TierDescription;
import ca.phon.session.TierViewItem;
import ca.phon.ui.toast.Toast;
import ca.phon.ui.toast.ToastFactory;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;

public class NewTierAction extends TierManagementAction {

	private static final long serialVersionUID = -25622911440669271L;
	
	private final static String CMD_NAME = "New tier...";
	
	private final static String SHORT_DESC = "Add new tier to session.";
	
	private final static ImageIcon ICON =
			IconManager.getInstance().getIcon("actions/list-add", IconSize.SMALL);

	public NewTierAction(SessionEditor editor, TierOrderingEditorView view) {
		super(editor, view);
		
		putValue(NAME, CMD_NAME);
		putValue(SMALL_ICON, ICON);
		putValue(SHORT_DESCRIPTION, SHORT_DESC);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		TierEditorDialog newTierDialog = new TierEditorDialog(false);
		TierInfoEditor tierEditor = newTierDialog.getTierEditor();
		newTierDialog.add(tierEditor);
		newTierDialog.setTitle("New Tier");
		newTierDialog.setModal(true);
		newTierDialog.pack();
		
		if(newTierDialog.showDialog()) {
			final SessionEditor editor = getEditor();
			final Session session = editor.getSession();
			// get tier info
			String tierName = tierEditor.getTierName();
			tierName = StringUtils.strip(tierName);
			if(tierName.length() == 0) {
				return;
			}
			
			boolean tierExists = false;
			if(SystemTierType.isSystemTier(tierName)) {
				tierExists = true;
			} else {
				for(TierDescription td:session.getUserTiers()) {
					if(td.getName().equals(tierName)) {
						tierExists = true;
						break;
					}
				}
			}
			
			if(tierExists){
				final Toast toast = ToastFactory.makeToast("A tier with name " + tierEditor.getTierName() + " already exists.");
				toast.start(tierEditor);
				return;
			}
			
			// create tier
			final TierDescription tierDescription = tierEditor.createTierDescription();
			final TierViewItem tierViewItem = tierEditor.createTierViewItem();
			
			final AddTierEdit edit = new AddTierEdit(editor, tierDescription, tierViewItem);
			editor.getUndoSupport().postEdit(edit);
		}
	}

}
