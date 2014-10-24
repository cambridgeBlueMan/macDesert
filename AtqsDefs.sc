AtqsDefs {
*new {|s|
		"___atqs defs______".postln;
	s.postln;
	s ?? Server.default;
	s.postln;
		"_____atqs defs____".postln;

	^super.new.init(s)
} // end new
init {|s|
s ?? Server.default;
s.waitForBoot({
	var c;
	Routine.run({
	c=Condition.new;
	// **************************
// experimental with reverb
SynthDef(\sfgrain3, {arg bufnum=0, pan=0.0, out = 0, startPos=0.0, amp=1, dur=0.04, segSize = 176400, segIndex=10, wetDry=0, rate=1.5; 
var grain, sig; 
grain= PlayBuf.ar(
	1, // number of channels
	bufnum, // buffer number
	BufRateScale.kr(bufnum)*rate, //rate, defined here as 1.5 of current buffers rate
	1, // trigger, presumable 1 triggers
	// next line is sample frame to start
	// what we want is a value which is fed in at the start to indicate the offset into the file
	// and then some agreed nmber of samples that define the window length
	// so start frame would be something along the lines of
	((segIndex*segSize) + (segSize*startPos));
	((segIndex*segSize) + (segSize*startPos)) , 0)*(EnvGen.kr(Env.perc(0.01,dur),doneAction:2)-0.001
);
sig = FreeVerb.ar(grain, wetDry, 0.5, 0.2);
Out.ar(out,Pan2.ar(sig, pan)*amp)}).send(s); 

// **********************************************
		
// *************
// master mixer
// amp was 0.15
SynthDef(\mixer, {|amp = 1.0, out = 0, in = 20, gate=1|
	// 3/11/10 added gate arg and eg line
	var eg = EnvGen.kr(Env.asr(1,1,1), gate, doneAction:0);
	Out.ar(out, [
	Mix.ar([
		In.ar(in), 
		In.ar(in+2),
		In.ar(in+4),
		In.ar(in+6),
		In.ar(in+8),
		In.ar(in+10),
		In.ar(in+12),
		In.ar(in+14),
		In.ar(in+16)	
	]),
	Mix.ar ([
		In.ar(in + 1),
		In.ar(in + 3), 
		In.ar(in + 5), 
		In.ar(in + 7), 
		In.ar(in+9), 
		In.ar(in+11), 
		In.ar(in+13), 
		In.ar(in + 15),
		In.ar(in + 17)
	])
	]
	// 3/11/10 added eg multiplier
	*amp*eg)
}).load(s); // end synthdef 

	// *******
	// end def
	// ***************************************************************************
//                            SYNTH DEFS FOR PITCH CONTROLLERS
// ***************************************************************************

// **********************************
// string synth taken from help files

SynthDef(\string1, { arg out, freq = 360, gate = 1, pan, amp=0.1, cutoff = 1000;
	var sout, eg, fc, osc, a, b, w;	
	fc = LinExp.kr(
		LFNoise1.kr(
			Rand(0.25,0.4)) 
		, -1
		,1
		,500
		,2000
		); // end LinExp
		osc = Mix.fill(8, { LFSaw.ar(freq * [Rand(0.99,1.01),Rand(0.99,1.01)], 0, amp) }).distort * 0.2;
		eg = EnvGen.kr(Env.asr(1,1,2), gate, doneAction:0);
		fc = cutoff;
		sout = eg * RLPF.ar(osc, fc, 0.1);
		#a, b = sout;
		Out.ar(out, Mix.ar(PanAz.ar(4, [a, b], [pan, pan+0.3]))*amp);
}).send(s);

// **********************************NNOT USED *************************************
SynthDef(\bach, { |i_out=0, freq, gate|
		var out;
		out = RLPF.ar(
			LFSaw.ar( freq, mul: EnvGen.kr( Env.perc, gate, levelScale: 0.3, doneAction: 2 )),
			LFNoise1.kr(1, 36, 110).midicps,
			0.1
		);
		// out = [out, DelayN.ar(out, 0.04, 0.04) ];
		4.do({ out = AllpassN.ar(out, 0.05, [0.05.rand, 0.05.rand], 4) });
		Out.ar( i_out, out );
	}).send(s);

// *****************
// a gendy synth def
SynthDef.new(\gendy, {|minimumfreq=20, maximumfreq=200, ampdist = 2, durdist=3, gate=0|
	var env = EnvGen.ar(Env.adsr, gate); 
	var sig = Pan2.ar(
			RLPF.ar(
				Gendy1.ar(
					ampdist,
					durdist,
					minfreq:minimumfreq, 
					maxfreq:maximumfreq, 
					durscale:0.0, 
					initCPs:40
				), // end gendy
				500,
				0.3, 
				0.2
			), // end RLPF
			0.0
		) ; // end pan2
		Out.ar(0, sig*env);
}).send(s);

// **********************************
// a synth def to check freq of gendy
SynthDef.new(\tuner, {|freq=440, gate=0|
	var env = EnvGen.ar(Env.adsr, gate); // 1 for doneAction pauses synth
	Out.ar(0, SinOsc.ar(freq, mul:0.5)*env)
}).send(s);

// ***************
// simple sine synth
SynthDef(\mySine, {|freq=200|
	Out.ar(0, SinOsc.ar(freq, 0, 0.5));
}).send(s);
// *******************************************n END OF NOT USED ***************************
	s.sync(c);
	"SynthDefs loaded!".postln;
	}); // end routine
}); // end wait for boot
} // end of init method
} // end of class


