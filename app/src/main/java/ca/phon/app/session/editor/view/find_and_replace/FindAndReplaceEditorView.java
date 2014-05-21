package ca.phon.app.session.editor.view.find_and_replace;

import java.awt.BorderLayout;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.JToolBar;

import ca.phon.app.session.editor.DelegateEditorAction;
import ca.phon.app.session.editor.EditorAction;
import ca.phon.app.session.editor.EditorEvent;
import ca.phon.app.session.editor.EditorEventType;
import ca.phon.app.session.editor.EditorView;
import ca.phon.app.session.editor.EditorViewCategory;
import ca.phon.app.session.editor.EditorViewInfo;
import ca.phon.app.session.editor.SessionEditor;
import ca.phon.app.session.editor.view.common.TierDataLayoutPanel;
import ca.phon.session.SessionFactory;
import ca.phon.session.Tier;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;

@EditorViewInfo(category=EditorViewCategory.UTILITIES,icon=FindAndReplaceEditorView.VIEW_ICON,name=FindAndReplaceEditorView.VIEW_NAME)
public class FindAndReplaceEditorView extends EditorView {
	
	private static final long serialVersionUID = 3981954934024480576L;

	public final static String VIEW_NAME = "Find & Replace";
	
	public final static String VIEW_ICON = "";
	
	private JToolBar toolBar;
	
	private TierDataLayoutPanel tierPanel;
	
	private final Map<String, Tier<String>> searchTiers = new LinkedHashMap<String, Tier<String>>();
	
	public FindAndReplaceEditorView(SessionEditor editor) {
		super(editor);
		
		init();
		setupEditorActions();
	}
	
	private void init() {
		setLayout(new BorderLayout());
		
		toolBar = new JToolBar();
		toolBar.setFloatable(false);
		add(toolBar, BorderLayout.NORTH);

		tierPanel = new TierDataLayoutPanel();
		add(tierPanel, BorderLayout.CENTER);
	}
	
	private void setupEditorActions() {
		final EditorAction viewChangedAct = new DelegateEditorAction(this, "onTierViewChanged");
		getEditor().getEventManager().registerActionForEvent(EditorEventType.TIER_VIEW_CHANGED_EVT, viewChangedAct);
	}

	private void updateTierView() {
		
	}
	
	/* Editor Actions */
	public void onTierViewChanged(EditorEvent ee) {
		
	}
	
	private void updateTemplate() {
		final SessionFactory factory = SessionFactory.newFactory();
	}
	
	@Override
	public String getName() {
		return VIEW_NAME;
	}

	@Override
	public ImageIcon getIcon() {
		return IconManager.getInstance().getIcon(VIEW_ICON, IconSize.SMALL);
	}

	@Override
	public JMenu getMenu() {
		return null;
	}

}
