s.waitForBoot({
	var c;
	Routine.run({
	c=Condition.new;
	SynthDef(\parrots1, {arg
	amp = 0,
	out = 0,
	bufnum = 0 ,
	pos= 1, // pan position
	sPos = 0, // start position within the buffer
	startRate=(-6), // the start rate, currently must be negative
	endRate=(1), // the end rate, currently must be positive
	firstDur=0.1 , // duraton of the first half of sound i.e. the reverse part
	lastDur=0.1, // duration of the second half of the sound i.e. the forward part
	fadeOutTime=0.05, //
	fadeInTime=0
	;
	var rate, sig, env;
	// first array is levels, second array is times
	env=Env.new([0,1,1,0], [fadeInTime,(firstDur+lastDur)-fadeOutTime,fadeOutTime]);
	// start, end, dur
	//rate = (XLine.kr(startRate, -0.0001, firstDur) + XLine.kr(0.0001, endRate, lastDur));
	// try rate as envelope
	rate = EnvGen.kr(Env.new([startRate,0, endRate], [firstDur, lastDur]));
	sig = PlayBuf.ar(1, // number of channnels
					bufnum,
					BufRateScale.kr(bufnum)*rate, // rate
					1.0, // trigger
					0.0, // start pos - can delete?
					1.0, // loop, 1 = true
					startPos: sPos)*EnvGen.kr(env, doneAction:2)*amp;
	Out.ar(out, Pan2.ar(sig, pos))
}).store; // send(s);
		s.sync(c);
		"SynthDefs loaded!".postln;
	}); // end routine
}); // end wait for boot