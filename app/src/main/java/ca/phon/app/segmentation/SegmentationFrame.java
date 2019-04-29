package ca.phon.app.segmentation;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;

import ca.phon.media.sampled.Channel;
import ca.phon.media.sampled.PCMSampled;
import ca.phon.media.sampled.Sampled;
import ca.phon.ui.CommonModuleFrame;
import ca.phon.ui.nativedialogs.FileFilter;
import ca.phon.ui.nativedialogs.NativeDialogs;
import ca.phon.ui.nativedialogs.OpenDialogProperties;

/**
 * Segmentation UI
 */
public class SegmentationFrame extends CommonModuleFrame {
	
	public static void main(String[] args) throws Exception {
		final String wavFile = args[0];
		WaveformDisplay display = new WaveformDisplay();
		display.setOpaque(true);
		display.setBackground(Color.white);
		display.setChannelHeight(200);
		display.setChannelGap(0);
		display.setStartTime(0f);
		display.setSecondsPerPixel(0.05f);
		
		SegmentationFrame f = new SegmentationFrame();
		
		JToolBar toolbar = new JToolBar();
		JButton btn = new JButton("Open");
		toolbar.add(btn);
		
		btn.addActionListener( (e) -> {
//			OpenDialogProperties props = new OpenDialogProperties();
//			props.setParentWindow(f);
//			props.setRunAsync(false);
//			props.setAllowMultipleSelection(false);
//			props.setCanChooseFiles(true);
//			props.setCanChooseDirectories(false);
//			props.setCanCreateDirectories(false);
//			props.setFileFilter(FileFilter.wavFilter);
//			
//			List<String> selectedFile = NativeDialogs.showOpenDialog(props);
			
			Sampled sampled;
			try {
				sampled = new PCMSampled(new File(wavFile));
				display.setEndTime(sampled.getLength());
//				for(int ch = 0; ch < sampled.getNumberOfChannels(); ch++) {
//					display.setChannelVisible(Channel.values()[ch], true);
//				}
				display.setSampled(sampled);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		});
		
		f.setLayout(new BorderLayout());
		f.add(toolbar, BorderLayout.NORTH);
		f.add(new JScrollPane(display), BorderLayout.CENTER);
		f.pack();
		f.setSize(500, 300);
		f.centerWindow();
		f.setVisible(true);
	}
	
}
