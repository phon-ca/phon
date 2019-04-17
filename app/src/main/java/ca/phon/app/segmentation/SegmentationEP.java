package ca.phon.app.segmentation;

import java.util.Map;

import ca.phon.plugin.IPluginEntryPoint;

public class SegmentationEP implements IPluginEntryPoint {
	
	public final static String EP_NAME = "Segmentation";

	@Override
	public String getName() {
		return EP_NAME;
	}

	@Override
	public void pluginStart(Map<String, Object> args) {
		SegmentationFrame frame = new SegmentationFrame();
		frame.pack();
		frame.centerWindow();
		frame.setVisible(true);
	}

}
