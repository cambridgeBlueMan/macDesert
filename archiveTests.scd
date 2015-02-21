q = (); // Event

q[\a_long_array] = Array.rand(128, 0.0, 1.0);
q[\pi_squared] = pi * pi;
q[\favourite_sound] = { { SinOsc.ar([300, 330]).sum * LFPulse.kr(2 + [0, 0.01]) * 0.1 }.play };
q[\same_long_array] = q[\a_long_array]; // same objects may appear several times

Archive.global.put(\myData, q);


Archive.global.at(\myData).postcs;

// after a recompile:
s.boot;

q = Archive.global.at(\myData);
q.postcs;
q[\favourite_sound].value;

Archive.clear

var dataStore = Dictionary.new;
dataStore["dur"] = 1.1;
dataStore["amp"] = 1.1;

Archive.global.put(\arc1, dataStore);


var dataStore = Dictionary.new(0);
dataStore["dur"] = 2.2;
dataStore["amp"] = 2.2;

Archive.global.put(\arc2, dataStore);

Archive.global.size
Archive.global[\arc1]
Archive.global.do({|item, i|
	item.postn("item" ++ i);
});
"bollocks".postln;
Archive.global[\arc2] = nil