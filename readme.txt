This project is plays tones from different musical instruments.

Tone.java holds methods that allow programmers to make different types of musical tones.
Square waves, sine waves, and sawtooth waves can all be generated at any frequency less than the sample rate.
Waves can also be added together to create new periodic functions.
Harmonic tones can be made by summing integral multiples of some fundamental frequency.
Random noise can also be simulated.

To create a sine wave:
Tone sinewave = Tone.sineWave(...);
To create a square wave:
Tone squarewave = Tone.squareWave(...);
To creaet a sawtooth wave:
Tone sawtoothwave = Tone.sawtoothWave(...);

To play a generated tone on its own (without having to create a new thread):
sineWave.playInThread();

To add two waves together through superposition:
Tone wave1 = Tone.sineWave(...);
Tone wave2 = Tone.sineWave(...);
Tone sum = Tone.add(wave1, wave2);

To play a harmonic tone:
double[] amplitudes = {1, 0.4, 0.3};
Tone harmonic = Tone.harmonicTone(..., amplitudes, 440);
This example will create a sum of three sine waves: 1 with frequency 440 Hz, one at 880 Hz, and one at 1320 Hz. The 440 Hz wave will have a relative amplitude of 1; 880 Hz will have a relative amplitude of 0.5; and 1320 Hz will have a relative amplitude of 0.3.

More examples can be found in Main.java.