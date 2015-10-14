package com.matt.music;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

/**
 * Holds different musical tones.
 * @author Matt
 */
public class Tone {
	
	/** Array that holds all wave data */
	private byte[] buffer;
	/** Number of samples per second */
	public final double sampleRate;
	
	/**
	 * Create a new tone
	 * @param b wave data
	 * @param sampleRate number of samples per second
	 */
	private Tone(byte[] b, double sampleRate) {
		this.buffer = b;
		this.sampleRate = sampleRate;
	}
	
	/**
	 * Plays a musical tone but does not create its own thread to play in.
	 * A new thread must be created to use.
	 * @throws LineUnavailableException 
	 */
	public void playTone() {
		try {
			AudioFormat af = new AudioFormat(
					(float)sampleRate, // samples per second
					8, // number of bits per sample
					1, // 1 = mono, 2 = stereo
					true, // true means it can be negative (signed)
					false); // true = big endian, false = little endian
			// big endian = most significant byte at smallest memory address
			// little endian = least significant byte at smallest memory address
			
			SourceDataLine sdl = AudioSystem.getSourceDataLine(af);
			sdl.open();
			sdl.start();
			
			sdl.write(buffer, 0, buffer.length);
			
			sdl.drain();
			sdl.stop();
			
		} catch (LineUnavailableException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Plays this musical tone in its own thread.
	 */
	public void playInThread() {
		Thread t = new Thread(new ToneRunner());
		t.start();
	}
	
	/**
	 * Runnable that plays a musical tone.
	 */
	private class ToneRunner implements Runnable {

		@Override
		public void run() {
			playTone();
		}
		
	}
	
	/**
	 * Generate a solid, sine wave tone.
	 * @param length total number of samples
	 * @param sampleRate number of samples per second (Hz)
	 * @param frequency number of waves per second (Hz)
	 * @param amplitude maximum height of wave, between 0 and 127
	 * @param phase number of radians to shift the wave left or right
	 * @return
	 */
	public static Tone sineWave(int length, double sampleRate, double frequency, 
			byte amplitude, double phase) {
		byte[] b = new byte[length];
		for (int i=0; i<b.length; i++) {
			double angle = i*frequency/sampleRate * 2.0*Math.PI + phase;
			b[i] = (byte)(Math.sin(angle) * amplitude);
		}
		return new Tone(b, sampleRate);
	}
	
	/**
	 * Generate a square wave tone.
	 * @param length total number of samples
	 * @param sampleRate number of samples per second (Hz)
	 * @param frequency number of waves per second (Hz)
	 * @param amplitude maximum height of wave, between 0 and 127
	 * @param phase amount to shift the wave left or right
	 * @return
	 */
	public static Tone squareWave(int length, double sampleRate, double frequency,
			byte amplitude, double phase) {
		byte[] b = new byte[length];
		int sampleShift = (int) (sampleRate / (frequency * 2 * Math.PI) * phase);
		int period = (int) (sampleRate / frequency);
		for (int i=0; i<b.length; i++) {
			if ((i-sampleShift)%period < period/2) b[i] = amplitude;
			else b[i] = 0;
		}
		return new Tone(b, sampleRate);
	}
	
	/**
	 * Generate a sawtooth wave tone.
	 * @param length total number of samples
	 * @param sampleRate number of samples per second (Hz)
	 * @param frequency number of waves per second (Hz)
	 * @param amplitude maximum height of wave, between 0 and 127
	 * @param phase amount to shift the wave left or right
	 * @return
	 */
	public static Tone sawtoothWave(int length, double sampleRate, double frequency,
			byte amplitude, double phase) {
		byte[] b = new byte[length];
		int sampleShift = (int) (sampleRate / (frequency * 2 * Math.PI) * phase);
		double period = (sampleRate / frequency);
		double slope = (double) amplitude * 2 / period;
		for (int i=0; i<b.length; i++) {
			double x = (i-sampleShift)%period;
			b[i] = (byte) (x*slope - amplitude);
		}
		return new Tone(b, sampleRate);
	}
	
	/**
	 * Generates many sine waves playing at the same time.
	 * @param length total number of samples
	 * @param sampleRate number of samples per second (Hz)
	 * @param frequency number of waves per second (Hz) for each wave
	 * @param amplitude maximum height of each wave, between 0 and 127
	 * @param phase amount to shift each wave left or right
	 * @precondition frequency, amplitude, and phase have the same length
	 */
	public static Tone sumOfSineWaves(int length, double sampleRate, double[] frequency, 
			byte[] amplitude, double[] phase) {
		if (frequency.length != amplitude.length || amplitude.length != phase.length) {
			System.err.println("Frequency, amplitude, and phase arrays must be the same length!");
		}
		Tone[] tones = new Tone[frequency.length];
		for (int i=0; i<tones.length; i++) {
			tones[i] = Tone.sineWave(length, sampleRate, frequency[i], amplitude[i], phase[i]);
		}
		return Tone.add(tones);
	}
	
	/**
	 * Adds tones together by superposition.
	 * @param tones
	 * @precondition tones must have the same sample rate and number of samples.
	 */
	public static Tone add(Tone ...tones) {
		// add arrays together, find max value
		int[] b = new int[tones[0].buffer.length];
		int maxB=0;
		for (int i=0; i<b.length; i++) {
			b[i] = 0;
			for (int j=0; j<tones.length; j++) {
				b[i] += tones[j].buffer[i];
			}
			int a = Math.abs(b[i]);
			if (a > maxB) maxB = a;
		}
		
		// scale by max value
		// if maxB is less than 127, no need to scale
		byte[] c = new byte[b.length];
		for (int i=0; i<b.length; i++) {
			if (maxB > 127) c[i] = (byte)((double)(b[i]*127)/(double)maxB);
			else c[i] = (byte) b[i];
		}
		return new Tone(c, tones[0].sampleRate);
	}
	
	/**
	 * Generate some random noise.
	 * @param length total number of samples
	 * @param sampleRate number of samples per second (Hz)
	 * @param amplitude maximum height of wave, between 0 and 127
	 */
	public static Tone randomNoise(int length, double sampleRate, byte amplitude) {
		byte[] b = new byte[length];
		for (int i=0; i<length; i++) {
			b[i] = (byte)(Math.random()*amplitude*2 - amplitude);
		}
		return new Tone(b, sampleRate);
	}
	
	/**
	 * Create a new tone based on a list of harmonic amplitudes. Sums sine waves
	 * together to create any musical timbre.
	 * @param length total number of samples
	 * @param sampleRate number of samples per second (Hz)
	 * @param maxamplitude maximum amplitude of the wave
	 * @param amplitudes relative amplitude of harmonic frequencies (values between 0 and 1)
	 * @param frequency base frequency of this tone
	 */
	public static Tone harmonicTone(int length, double sampleRate, byte maxamplitude,
			double[] amplitudes, double frequency) {
		Tone[] tones = new Tone[amplitudes.length];
		for (int i=0; i<tones.length; i++) {
			tones[i] = Tone.sineWave(length, sampleRate, 
					frequency*(i+1), (byte)(amplitudes[i]*maxamplitude), 0);
		}
		return Tone.add(tones);
	}
}
