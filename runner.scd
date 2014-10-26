

// Messages from external clients that should be processed by OSCresponders must be sent to the language port, 57120 by default. Use NetAddr.langPort to confirm which port the SuperCollider language is listening on.
//




// run this line first
~parrots = Parrots.new(Server.local);
~atqs = Atqs.new(Server.local);
// again wait for the completion message

// finally run the following code block:

(
var movement = -1;
var movementInfo = [1,3,2,1,7,5];
// run them both
// i think that in running parrots we need to set an output bus (or pair, really!)
// this allows us to have the out as 0 in the def , thus allowing standalone running
// and then when running in the ocntext of desert qe can defone a bus here, somehow
~parrots.run(32);
~atqs.run;
// but stop the atqs player
~atqs.stopPlayer;
// and set the active flag to parrots
~active  = "parrots";

// now add a single LeaWii responder on "+"
~runnerWii = LeaWii.new(2);
// ***************************************************************
~runnerWii.add("+", {|t,r,msg|
if (msg[1]==1, {
	movement = movement + 1;
	// first increment the movement counter
	if (~active  == "parrots", {
			~active  = "atqs";
			~parrots.stopPlayer;
			~parrots.leaWii.clear;
			~atqs.atqsWii.renew;
			// load the preset appropriate to this movement
			// and amke sure that the thing is playing
			"______________".postln;
			~atqs.startPlayer;
			"before defer".postln;
			{	~atqs.selectPreset.value = movementInfo[movement];
				~atqs.applyPresets(movementInfo[movement]);
				"in defer".postln}.defer;
			"after defer".postln;
			"movement is".postln;
			movement.postln;
			movementInfo[movement].postln;
			~runnerWii.renew;
	},
	{
			~active = "parrots";
			// make sure synths are off
			~atqs.mySynth[0].set(\gate, 0);
			~atqs.mySynth[1].set(\gate, 0);
			~atqs.myMixer.set(\gate, 0);
			~atqs.atqsWii.clear; // clear out atqs resonders
			~parrots.leaWii.renew;
			~runnerWii.renew;
			// give the fade out chance to happen
			SystemClock.sched(1, {
				~atqs.stopPlayer; // stop atqs synth
				~atqs.myMixer.set(\gate, 1); // get mixer levels back up
				~parrots.startPlayer;
			});
			"movement is".postln;
			movement.postln;
			movementInfo[movement].postln;
			{~parrots.selectPreset.value = movementInfo[movement];
				~parrots.applyPresets(movementInfo[movement]);}.defer;
	}); // end if
	"movement is now".post;
	movement.postln;
}); // end msg if
});

)
