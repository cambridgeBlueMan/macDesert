AtqsTrack {
// some vars
var <>cycleLength = 45;
var <>lastVal = 0.436;
var <>dur;
var <>wetDry;
var <>segIndex;
var <>segSize;
var <>tempo;
var <>isOn = false;
var <>mixerSetting=0;
var <>aNum =3;
var feelArray, currentFeel=6, currentElement = 0;
*new {|buffer, ix|
	^super.new.init(buffer, ix)
} // end new
init {|buffer, ix|
	feelArray = [
		[1,0,0,0],
		[0,1,0,0],
		[0,0,1,0],
		[0,0,0,1],
		[1,1,0,0],
		[1,0,1,0],
		[1,0,0,1],
		[0,1,0,1],
		[0,0,1,1],
		[0,1,1,0],
		[1,1,1,0],
		[0,1,1,1],
		[1,1,0,1],
		[1,0,1,1],
		[0,0,0,0],
		[0,1,1,0],
		[1,1,1,1]
	];
// ******
// a task
{
inf.do{arg i; 
	var prop, timestart, timeend;
	prop= (i%(cycleLength))/(cycleLength);
	if (prop == 0, {
	});
	timestart= prop*0.8;
	timeend= prop*(0.8+(0.1*lastVal));
	// following line is the beginning of the if statement for a goer
	// so it is the same game, but different condition
	// increment the current array index
	// if the current array item is 1/true then do it
	
	if (aNum.rand == 0, {
	//if (feelArray[aNum][currentElement] ==0, {
		if (isOn == true, {
			Synth(\sfgrain3,
			[
				\bufnum, buffer.bufnum, 	
				\out, (20 + (ix*2)),
				\startPos,rrand(timestart,timeend),
				\amp, mixerSetting, // exprand(0.005,0.1)*10*mixerSetting, 
				\pan, lastVal.rand2, 
				\dur, dur,  // 0.1+(lastval*0.5),
				\segIndex, segIndex,
				\segSize,  segSize,
				\wetDry, wetDry,
			],
			addAction: \addToHead
			);  // end synth
		}); // end inner if
	 }); // end outer if
	 currentElement = currentElement + 1;
	 //"current element is".postln;
	// currentElement.postln;
	 if (currentElement == 4, {currentElement = 0});
	tempo.max(0.01).wait;
}; 
}.fork;



}
aMethod {




} // end a method

} // end of class