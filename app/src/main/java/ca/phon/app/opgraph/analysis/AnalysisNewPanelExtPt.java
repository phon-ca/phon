/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2016, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
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
package ca.phon.app.opgraph.analysis;

import ca.phon.app.opgraph.editor.NewDialogPanel;
import ca.phon.plugin.IPluginExtensionFactory;
import ca.phon.plugin.IPluginExtensionPoint;
import ca.phon.plugin.PhonPlugin;

@PhonPlugin(author="Greg J. Hedlund <ghedlund@mun.ca>", minPhonVersion="2.1.0", name="Analysis Opgraph Editor Model")
public class AnalysisNewPanelExtPt implements IPluginExtensionPoint<NewDialogPanel> {

	@Override
	public Class<?> getExtensionType() {
		return NewDialogPanel.class;
	}

	@Override
	public IPluginExtensionFactory<NewDialogPanel> getFactory() {
		final IPluginExtensionFactory<NewDialogPanel> factory = (Object ... args) -> {
			return new AnalysisNewPanel();
		};
		return factory;
	}

}
