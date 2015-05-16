/**
 * 
 */
package apu.scratch.tools;

import java.util.ArrayList;
import java.util.List;

import apu.scratch.tools.gen.followers.FollowerListGen;
import apu.scratch.tools.gen.visualizer.VisualizerGenMinim;

/**
 * @author MegaApuTurkUltra
 */
public abstract class ToolBase {
	public abstract ParamDef[] getParams();

	public abstract void call(String[] args);

	public abstract String getName();

	public void printUsage() {
		ParamDef[] params = getParams();
		if (EXIT_ON_USAGE) {
			System.err.print("Usage: java -jar ScratchTools.jar ");
			System.err.print(getName());
			for (int i = 0; i < params.length; i++) {
				System.err.print(" ");
				if (params[i].isOptional())
					System.err.print("[");
				else
					System.err.print("<");
				System.err.print(params[i].getName());
				if (params[i].isOptional())
					System.err.print("]");
				else
					System.err.print(">");
			}
		}
		else System.err.print("Missing parameters!");
		System.err.println();
		System.err.println();
		for (int i = 0; i < params.length; i++) {
			System.err.print("\t");
			System.err.print(params[i].getName());
			System.err.print(": ");
			System.err.print(params[i].getDesc());
			System.err.println();
		}
		if (EXIT_ON_USAGE)
			System.exit(0);
	}

	public void callWithParams(String[] args) {
		int required = 0;
		ParamDef[] params = getParams();

		for (int i = 0; i < params.length; i++) {
			if (!params[i].isOptional())
				required++;
		}

		if (args.length < required || args.length > params.length
				|| args[0].equals("--help")) {
			printUsage();
			return;
		}

		call(args);
	}

	public String toString() {
		return getName();
	}

	public static final List<ToolBase> tools;
	static {
		tools = new ArrayList<>();
		tools.add(new FollowerListGen());
		tools.add(new VisualizerGenMinim());
	}

	public static boolean EXIT_ON_USAGE = true;

	public static void printToolUsage() {
		System.err
				.println("Usage: java -jar ScratchTools.jar <ToolName> [options]");
		System.err.println();
		System.err.println("Tools:");
		try {
			for (ToolBase tool : tools) {
				System.err.print("\t");
				System.err.println(tool.getName());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (EXIT_ON_USAGE)
			System.exit(0);
	}

	public static void runTool(String[] args) {
		if (args.length < 1 || args[0].equals("--help")) {
			printToolUsage();
			return;
		}
		String name = args[0];
		String[] toolArgs = new String[args.length - 1];
		System.arraycopy(args, 1, toolArgs, 0, toolArgs.length);

		try {
			ToolBase toCall = null;
			for (ToolBase tool : tools) {
				if (tool.getName().equals(name)) {
					toCall = tool;
					break;
				}
			}
			if (toCall == null) {
				System.err.println("No such tool!");
				printToolUsage();
			} else {
				toCall.callWithParams(toolArgs);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
