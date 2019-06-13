package ca.phon.app.segmentation;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.UIManager;

import ca.phon.app.media.WaveformDisplay;
import ca.phon.app.media.WaveformDisplayScrollPane;
import ca.phon.media.LongSound;
import ca.phon.media.sampled.Channel;
import ca.phon.media.sampled.PCMSampled;
import ca.phon.media.sampled.Sampled;
import ca.phon.media.sampled.SampledLoader;
import ca.phon.ui.CommonModuleFrame;
import ca.phon.ui.nativedialogs.FileFilter;
import ca.phon.ui.nativedialogs.NativeDialogs;
import ca.phon.ui.nativedialogs.OpenDialogProperties;

/**
 * Segmentation UI
 */
public class SegmentationFrame extends CommonModuleFrame {
	
	public static void main(String[] args) throws Exception {
		WaveformDisplay display = new WaveformDisplay();
		display.setOpaque(true);
		display.setBackground(Color.WHITE);
		display.setPreferredChannelHeight(50);
		display.setStartTime(0f);
		display.setEndTime(100.0f);
		display.setPixelsPerSecond(100.0f);
		display.setTrackViewportHeight(true);
		
		WaveformDisplayScrollPane scroller = new WaveformDisplayScrollPane(display);
		
		SegmentationFrame f = new SegmentationFrame();
		
		JToolBar toolbar = new JToolBar();
		JButton btn = new JButton("Open");
		toolbar.add(btn);
		
		final JLabel fileLbl = new JLabel("");
		toolbar.add(fileLbl);
		
		btn.addActionListener( (e) -> {
			OpenDialogProperties props = new OpenDialogProperties();
			props.setParentWindow(f);
			props.setRunAsync(false);
			props.setAllowMultipleSelection(false);
			props.setCanChooseFiles(true);
			props.setCanChooseDirectories(false);
			props.setCanCreateDirectories(false);
//			props.setFileFilter(FileFilter.allFilesFilter);
			
			List<String> selectedFile = NativeDialogs.showOpenDialog(props);
			
			fileLbl.setText(selectedFile.get(0));
			
			try {
				LongSound longSound = LongSound.fromFile(new File(selectedFile.get(0)));
				display.setEndTime(longSound.length());
				display.setLongSound(longSound);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			
//			Sampled sampled;
//			try {
//				SampledLoader loader = SampledLoader.newLoader();
//				sampled = loader.loadSampledFromFile(new File(selectedFile.get(0)));
//				display.setEndTime(sampled.getLength());
////				for(int ch = 0; ch < sampled.getNumberOfChannels(); ch++) {
////					display.setChannelVisible(Channel.values()[ch], true);
////				}
//				display.setSampled(sampled);
//			} catch (IOException e1) {
//				// TODO Auto-generated catch block
//				e1.printStackTrace();
//			}
		});
		
		f.setLayout(new BorderLayout());
		f.add(toolbar, BorderLayout.NORTH);
		f.add(scroller, BorderLayout.CENTER);
		f.pack();
		f.setSize(500, 300);
		f.centerWindow();
		f.setVisible(true);
	}
	
}
