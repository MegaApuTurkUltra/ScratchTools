/**
 * 
 */
package apu.scratch.tools.gen.visualizer;

import java.io.File;
import java.io.FileOutputStream;

import org.jtransforms.fft.DoubleFFT_1D;

import wav.WavFile;

/**
 * Uses JTransforms to generate a visualization
 * DOESN'T WORK, DO NOT USE
 * @author MegaApuTurkUltra
 */
public class VisualizerGenJtransforms {

	public static void main(String[] args) throws Exception {
		if (args.length < 2) {
			System.err
					.println("Usage: java -jar VisualizerGen.jar <wav file> <output file>");
			System.exit(1);
		}
		File input = new File(args[0]);
		File output = new File(args[1]);
		FileOutputStream out = new FileOutputStream(output);

		WavFile wavFile = WavFile.openWavFile(input);

		int numChannels = wavFile.getNumChannels();
		long sampleRate = wavFile.getSampleRate();

		int sampleRateFrames = (int) (wavFile.getSampleRate() / 20);

		System.out.println("Channels: " + numChannels + "\nSample Rate: "
				+ sampleRate);

		System.out.println("Reading audio file into memory...");

		int framesRemaining = (int) wavFile.getFramesRemaining();
		
		System.out.println("Length: " + (framesRemaining / sampleRate) + " s");
		
		double[][] buffer = new double[numChannels][framesRemaining];
		wavFile.readFrames(buffer, framesRemaining);

		double[] audioData = buffer[0];
		System.out.println("Analyzing audio...");

		int fftSize = 128 * 64;
		DoubleFFT_1D fft = new DoubleFFT_1D(fftSize);
		
		int dataCounter = 0;

		for (int i = 0; i < framesRemaining; i += sampleRateFrames) {
			if (i + fftSize >= framesRemaining) {
				System.out.println("Not enough data for another FFT, breaking");
				break;
			}

			double[] fftInput = new double[fftSize];
				System.arraycopy(audioData, i, fftInput, 0, fftInput.length);

			// apply hamming window
			for (int j = 0; j < fftSize; j++) {
				fftInput[j] = fftInput[j]
						* (0.54f - 0.46f * (float) Math.cos(2 * Math.PI * j
								/ (fftSize - 1)));
			}

			double[] fftOutput = applyFFT(fftInput, fft);

			double[] bassBins = new double[80];
			System.arraycopy(fftOutput, 0, bassBins, 0, 80);

			for (int k = 0; k < 80; k++) {
				double data = Math.round(bassBins[k]);
				if(data < 72) data = 72;
				if(data > 170) data = 170;
				out.write(new String(data + "\n").getBytes());
				dataCounter++;
			}
		}
		out.close();
		wavFile.close();
		System.out.println("Done; wrote " + dataCounter + " lines");
		System.exit(0);
	}

	public static double[] applyFFT(double[] params, DoubleFFT_1D fft) {
		double[] output = new double[params.length * 2];
		System.arraycopy(params, 0, output, 0, params.length);
		fft.realForward(output);
		double[] half = new double[params.length];

		for (int i = 0, j = 0; i < output.length; i += 2, j++) {
			double real = output[i];
			double imag = output[i + 1];
			half[j] = Math.sqrt(real * real + imag * imag);
		}

		return half;
	}

}
