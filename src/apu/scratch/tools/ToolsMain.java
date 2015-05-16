/**
 * 
 */
package apu.scratch.tools;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import apu.scratch.tools.gui.StandardStreamPiper;
import apu.scratch.tools.gui.StandardStreamPiper.StandardIOType;
import apu.scratch.tools.gui.ToolsGui;

/**
 * @author MegaApuTurkUltra
 */
public class ToolsMain {
	public static void main(String[] args) {
		if(args.length > 0)
			ToolBase.runTool(args);
		else createGui();
	}
	
	public static StandardStreamPiper out;
	public static StandardStreamPiper err;
	
	public static void createGui(){
		ToolBase.EXIT_ON_USAGE = false;
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch(Exception e){
			e.printStackTrace();
		}
		
		out = new StandardStreamPiper(StandardIOType.OUT);
		err = new StandardStreamPiper(StandardIOType.ERR);
		
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				ToolsGui.launch();
			}
		});
	}
}
