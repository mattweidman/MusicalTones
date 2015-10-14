package com.matt.music;

public class Main {
	
	public static double sampleRate = 44100;
	public static double seconds = 0.5;
	public static int sampleCount = (int) (sampleRate * seconds);
	public static int baseFrequency = 220;
	public static double toneAmps[] = {.17, .85, .91, .97, .81, .48, .41, 1, .46, .16, .08, .23, .18, .11, .02, .01};
	public static double toneAmps2[] = {1, .89, .5, .15, .01, 0, 0, .01};
	public static double boneAmps[] = {.78, .9, 1, .05, .08, .03, .12, .09, .01};
	public static double clarinetAmps[] = {1, .04, .99, .12, .53, .11, .26, .05, .24, .07, .02, .03, .02, .03};
	public static double fluteAmps[] = {1, .65, .61, .15, .09, .02, .02, .01, .01, .01};
	public static double saxAmps[] = {.52, 1, .04, .04, .01};
	
	public static void main(String[] args) {
		Tone[] scale = new Tone[8];
		for (int i=0; i<scale.length; i++) {
			scale[i] = Tone.harmonicTone(sampleCount, sampleRate, (byte)127, clarinetAmps, 
					baseFrequency * Math.pow(2, (double)i/12.0));
		}
		ToneRunner tr = new ToneRunner(scale);
		Thread thread = new Thread(tr);
		thread.start();
	}
	
	public static class ToneRunner implements Runnable{
		Tone[] music;
		
		public ToneRunner(Tone[] music) {
			this.music = music;
		}
		
		@Override
		public void run() {
			for (int i=0; i<music.length; i++) {
				music[i].playTone();
			}
		}
		
	}
}
