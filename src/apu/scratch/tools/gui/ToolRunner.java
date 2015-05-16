/**
 * 
 */
package apu.scratch.tools.gui;

import apu.scratch.tools.ToolBase;

/**
 * @author MegaApuTurkUltra
 */
public class ToolRunner implements Runnable {
	ToolBase tool;
	Runnable callback;
	String[] args;
	
	public ToolRunner(ToolBase tool, String[] args, Runnable callback){
		this.tool = tool;
		this.args = args;
		this.callback = callback;
		
		Thread thread = new Thread(this);
		thread.setName("ScratchTools Tool Runner");
		thread.start();
	}
	@Override
	public void run() {
		tool.callWithParams(args);
		callback.run();
	}
}
