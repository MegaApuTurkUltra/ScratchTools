/**
 * 
 */
package apu.scratch.tools.gui;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

/**
 * @author MegaApuTurkUltra
 */
public class StandardStreamPiper extends OutputStream {
	public enum StandardIOType {
		OUT, ERR
	}

	PrintStream out;
	PrintStream modified;

	public StandardStreamPiper(StandardIOType type) {
		out = type == StandardIOType.OUT ? System.out : System.err;
		modified = new PrintStream(this);
		if (type == StandardIOType.OUT) {
			System.setOut(modified);
		} else {
			System.setErr(modified);
		}
	}

	@Override
	public void write(int b) throws IOException {
		if (ToolsGui.TOOLS_GUI != null)
			ToolsGui.TOOLS_GUI.putToConsole((char) b);
		out.write(b);
	}
}
