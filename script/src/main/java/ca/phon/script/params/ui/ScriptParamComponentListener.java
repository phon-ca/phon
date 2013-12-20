package ca.phon.script.params.ui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;

import javax.swing.JComponent;

import ca.phon.script.params.ScriptParam;

public class ScriptParamComponentListener implements PropertyChangeListener {
	
	/**
	 * Component
	 */
	private final WeakReference<JComponent> compRef;
	
	public ScriptParamComponentListener(JComponent comp) {
		super();
		this.compRef= new WeakReference<JComponent>(comp);
	}
	
	public JComponent getComponent() {
		return this.compRef.get();
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		final ScriptParam param = ScriptParam.class.cast(evt.getSource());
		final String evtName = evt.getPropertyName();
		if(evtName.equals(ScriptParam.ENABLED_PROP)) {
			getComponent().setEnabled(param.isEnabled());
		} else if(evtName.equals(ScriptParam.VISIBLE_PROP)) {
			getComponent().setVisible(param.getVisible());
		}
	}

}
