package apu.scratch.tools.gen.visualizer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.json.JSONArray;
import org.json.JSONObject;

import apu.scratch.tools.ParamDef;
import apu.scratch.tools.ParamType;
import apu.scratch.tools.ToolBase;
import ddf.minim.Minim;
import ddf.minim.MultiChannelBuffer;
import ddf.minim.analysis.FFT;

/**
 * Uses Minim to generate a visualizer project Not the most efficient ever, but
 * it works :P
 * 
 * @author MegaApuTurkUltra
 */
public class VisualizerGenMinim extends ToolBase {
	public String sketchPath(String fileName) {
		return new File(new File("."), fileName).getAbsolutePath();
	}

	public InputStream createInput(String fileName) {
		try {
			return new FileInputStream(fileName);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public void toolMain(String[] args) throws Exception {
		System.out.println("Initializing...");
		File input = new File(args[0]);
		File output = new File(args[1]);
		int fps = args[2].equals("true") ? 20 : 10;
		boolean bass_only = !args[3].equals("true");

		ZipOutputStream outFile = new ZipOutputStream(new FileOutputStream(
				output));
		ZipInputStream projectFileIn = new ZipInputStream(
				VisualizerGenMinim.class
						.getResourceAsStream("/res/VisualizerBase.sb2"));

		JSONArray valuesArray = new JSONArray();

		Minim minim = new Minim(new VisualizerGenMinim());

		MultiChannelBuffer buffer = new MultiChannelBuffer(0, 0);

		float sampleRate = 0;
		try {
			sampleRate = minim.loadFileIntoBuffer(
					input.getAbsolutePath(), buffer);
		} catch (Exception e) {
			e.printStackTrace();
			System.err
					.println("Whoops, there was an error reading your sound file."
							+ " If you haven't already, try using a WAV file, "
							+ "those tend to work better");
			outFile.close();
			output.delete();
			projectFileIn.close();
			return;
		}

		FFT fft = new FFT(256 * 64, sampleRate);
		fft.linAverages(30);

		float sampleRateFrames = sampleRate / fps;

		int counter = 0;
		int percent = 0;

		System.out.println("Analyzing audio...");

		for (int i = 0; i < buffer.getBufferSize(); i += sampleRateFrames) {
			if (i + 256 * 64 >= buffer.getBufferSize()) {
				break;
			}

			float[] audioBuffer = new float[256 * 64];
			for (int j = 0; j < 256 * 64; j++) {
				audioBuffer[j] = buffer.getSample(0, i + j);
			}
			fft.forward(audioBuffer);

			if (bass_only) {
				for (int k = 0; k < 80; k++) {
					float data = fft.getBand(k) / 5;
					// if (data < 75 && !no_thresh)
					// data = 75;
					// if (data > 150 && !no_thresh)
					// data = 150;

					int val = Math.round(data);
					valuesArray.put(val);
					counter++;
				}
			} else {
				for (int k = 0; k < 800; k += 10) {
					float data = 0;
					for (int l = 0; l < 10; l++) {
						data += fft.getBand(k + l);
					}
					data = data / 50;

					// if (data < 75 && !no_thresh)
					// data = 75;
					// if (data > 150 && !no_thresh)
					// data = 150;

					int val = Math.round(data);
					valuesArray.put(val);
					counter++;
				}
			}

			int perc = 100 * i / buffer.getBufferSize();
			if (perc > percent) {
				percent += 10;
				System.out.println("Progress: " + percent + "%");
			}
		}
		System.out.println("Total " + counter + " list values");
		System.out.println("Writing project...");

		ZipEntry nextInputEntry;
		while ((nextInputEntry = projectFileIn.getNextEntry()) != null) {
			if (nextInputEntry.isDirectory())
				continue;
			if (nextInputEntry.getName().equals("project.json")) {
				StringBuilder builder = new StringBuilder();
				byte[] b = new byte[1024];
				int len;
				while ((len = projectFileIn.read(b)) > 0) {
					builder.append(new String(b, 0, len));
				}

				JSONObject projectJson = new JSONObject(builder.toString());
				projectJson.getJSONArray("children").getJSONObject(0)
						.getJSONArray("lists").getJSONObject(0)
						.put("contents", valuesArray);
				outFile.putNextEntry(new ZipEntry(nextInputEntry.getName()));
				outFile.write(projectJson.toString().getBytes());
				outFile.closeEntry();
			} else {
				byte[] b = new byte[1024];
				int len;
				outFile.putNextEntry(new ZipEntry(nextInputEntry.getName()));
				while ((len = projectFileIn.read(b)) > 0) {
					outFile.write(b, 0, len);
				}
				outFile.closeEntry();
			}
			projectFileIn.closeEntry();
		}

		outFile.close();
		projectFileIn.close();
		System.out.println("Done");
	}

	@Override
	public ParamDef[] getParams() {
		return new ParamDef[] {
				new ParamDef("input", "The input audio file (mp3 or wav)",
						false, ParamType.FILENAME),
				new ParamDef("output", "The sb2 file to write to", false,
						ParamType.FILE_OUT),
				new ParamDef(
						"highFps",
						"True to generate animation frames at 20 FPS (warning: file will be massive), false to generate it at 10 (recommended)",
						false, ParamType.BOOLEAN),
				new ParamDef(
						"fullSpectrum",
						"True to analyze a large spectrum of frequencies, false for bass only",
						false, ParamType.BOOLEAN) };
	}

	@Override
	public void call(String[] args) {
		try {
			toolMain(args);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public String getName() {
		return "VisualizerGenerator";
	}
}
