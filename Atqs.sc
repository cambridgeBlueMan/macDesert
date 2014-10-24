Atqs {
// possible titles?
// "When you walk through the desert a lot of strange shit passes through your head"
// "When you're passing through the desert a lot of strange shit goes through your head"
// &&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&
// &&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&
// you can use any of the following" "b", "a", "up", "down" "left", "right", "-", "home" "1", "2", "c", "z"
// DO NOT USE "+", it is reserved as the swap key between atqs and parrots
// DO NOT USE "home" it is reserved for saving a preset

// note that after you make a change here, you first need to save (apple-s), and then recompile (apple-k)

// on pitch
var wiiLastVal = "b";
var wiiSegmentIndex = "a";
var wiiSegmentSize = "1";
var wiiCycleLength = "2";
var wiiWetDry = "c";

// on nunchuk pitch
var wiiWait = "h";
var wiiNoteLength = "-";
// other
var wiiTrackOnOff = "z";
var wiiCycleLengthDecrease = "left";
var wiiCycleLengthIncrease = "right";
var wiiNoteDensityUp = "up";
var wiiNoteDensityDown = "down";
// &&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&
// setting amplitudes
var initPartLevel = 0.6;
var initStringLevel = 0.4;
// ********************
var addPreset, <selectPreset, presetItemsList, presetsArray, addPresetArray, theFile, theString;
var server;
var myDir;
var atqsTrack, onOffs, <>atqsWii, w;
var playerOnOff; // button to turn on/off the sequencer
var randSel, mixFaders;
var bpm120;
var slidLastVal, lastVal;
var specSegIndex, segIndex, slidSegIndex;
var segSize, specSegSize, slidSegSize;
var specCycleLength, cycleLength, slidCycleLength;
var tempo, specWait, slidWait;
var dur, specNoteLength, slidNoteLength, specWetDry, slidWetDry;
var whichTrack;
var doAction; // a boolean to indicate whethter the prest action will be invoked when traversing items in preset selector list
// initial values for 2 wiimote joysticks 
var  x=0.5, y=0.5, p=0.5, q=0.5;
// following vars allow global setting of the joystick functions
var ixZero = 0, ixOne = 1, ixTwo = 2, ixThree = 3;
var currentCycleLength, currentRandSel;
var accelFlag, previousAccelFlag;
var namePreset;
var bigNoteList; // array holding all possible notes
var font;
var eZsWidth, eZsNumWidth, adjust;
var ringbuf, infreq, suppressContinuousPitch, <>mySynth ,<>myMixer, currentNoteIndex, unitPitchShift, noteSpecRange;
// **************************************************************************
//        LOCK/UNLOCK VAR FOR GUI WIDGETS
// **************************************************************************
// set this to true to lock stuff
var isEnabled = true;

classvar buffers, buffSizes;


// ***************************
// pitch controller vars now ended
// *******************************

*new {|s|
	"___atqs______".postln;
	s.postln;
	s ?? Server.default;
	s.postln;
		"_____atqs____".postln;

	^super.new.init(s)
} // end new

// *************************************************************************
//                        INITIALISATION
// *************************************************************************
init {|s|
myDir = Document.current.dir;
atqsTrack = List.new(0);
onOffs = List.new(0);
randSel = List.new(0);
mixFaders = List.new(0);
presetItemsList = List.new(0);
// server = s;
s.waitForBoot({
	// ensure asynchronous stuff is complete before moving on
	var c;
	Routine.run({
		c=Condition.new;
		AtqsDefs.new(s);
		buffers = GetBuffers2.new(s, (myDir ++"/atqs/")).buffers;
		buffSizes = GetBuffers2.new(s, (myDir ++"/atqs/")).buffSizes;
		presetsArray = CSVFileReader.read(myDir ++ "/atqsPresets.txt");
		s.sync(c);
		// add code for if file doeasn't exist
		// specifically, something along the lines of File.exists("/Users/lea/SC/DEV/DESERT/atqsPresets.scd")
		"buffers loaded!".postln;
	}); // end routine
});
} // end init
// *************************************************************************
//                        RUN
// *************************************************************************
run {
// ************
// ****************************************
// following vars are for pitch controllers
ringbuf = Array.newClear(2); // array for the ringbuf smoothing arrays
infreq = Array.newClear(2); // holds the input note value
suppressContinuousPitch = Array.newClear(2); // indicates if a decorator is being performed
mySynth = Array.newClear(2);
currentNoteIndex = Array.newClear(2);
unitPitchShift = Array.newClear(2);
noteSpecRange = Array.newClear(2);

// *************
// make a LEAWII
atqsWii = LeaWii.new;
lastVal = 0.436;

// *****************************************
// initialising values for pitch controllers

ringbuf[0] = [1,2,3,4,5,6,7,8,9,10];
ringbuf[1] = [1,2,3,4,5,6,7,8,9,10];
suppressContinuousPitch[0] = false;
suppressContinuousPitch[1] = false;
currentNoteIndex[0]=6;
currentNoteIndex[1]=12;
unitPitchShift[0] = 4;
unitPitchShift[1] = 4;
noteSpecRange[0] = 10;
noteSpecRange[1] = 10;
// there will also need to be a second synth here, so i am going to start devclaring vars
mySynth[0] = Synth.new(\string1, args: [\gate, 0, \out, 28]);
mySynth[1] = Synth.new(\string1, args: [\gate, 0, \out, 30]);
// and a mixer
//"add mixer".postln;
myMixer = Synth.new(\mixer, addAction: \addToTail);
// "after add mixer".postln;
// ***********************
// generate the big note list
bigNoteList=List.new();	
5.do({|i|	
		[33,35,36,38,40,43].do({|jitem, j|
		bigNoteList.add;
		bigNoteList[j+(i*6)] = (jitem + (12*i));	
	});
});


// ************************************************
// end of initialising values for pitch controllers
// ************************************************

// ********************************************************
//                  MAKE TEMPO CLOCK
// ********************************************************

bpm120 = TempoClock.new(2);
TempoClock.default = bpm120;

// ********************************************************
//                  MAKE WINDOW
// ********************************************************

w=SCWindow("atqs", Rect(640,0,640,800)); 
CmdPeriod.add({
	w.close;
	});
w.front;

// ********************************************************
//                  MAKE SLIDER WIDGETS
// ********************************************************
// first make a font
font = SCFont("Helvetica", 24);
eZsWidth = 550;
eZsNumWidth = 50;
// *******
// last val
slidLastVal=EZSlider(w,Rect(10,10,eZsWidth,30), label:"Last Val (" ++ wiiLastVal ++ ")", labelWidth:200, numberWidth: eZsNumWidth); 
slidLastVal.font = font;
slidLastVal.action_({
	buffers.size.do ({|item, i|
		atqsTrack[i].lastVal= slidLastVal.value;}); // end do
}); // end action

// ****************
// index  into file

// notice that the number of index slots is a function of the fiile size and the segment size
// thus if you change the number of segments the number of indexes increases and the specSegIndex 
// control spec has to update dynamically to reflect this

// min. max, warp, step, default
specSegIndex = ControlSpec(0,56,\lin,1,21);
slidSegIndex=EZSlider(
						w,
						Rect(10,50,eZsWidth,30), 
						label: "Segment Index (" ++ wiiSegmentIndex ++ ")", 
						labelWidth:200, 
						numberWidth: eZsNumWidth, 
						// control spec depends on slidSegSize.value already existing
						controlSpec:specSegIndex
					); 
slidSegIndex.font = font;
slidSegIndex.action_({
	buffers.size.do ({|item, i|
		atqsTrack[i].segIndex= slidSegIndex.value;}); // end do
}); // end action

// ***************
// size of segment
specSegSize = ControlSpec(0,10,\lin,0.01,2);
slidSegSize=EZSlider(
						w,
						Rect(10,100,eZsWidth,30), 
						label: "Segment Size (" ++ wiiSegmentSize ++ ")", 
						labelWidth:200, 
						numberWidth: eZsNumWidth, 
						controlSpec:specSegSize ); 
slidSegSize.font = font;
slidSegSize.action_({
	// if the current segIndex is greater than would be possible given the updated 
	// specSegSize then set the current index to the maximum it could possible be
	if (slidSegIndex.value > (buffSizes[0]/(44100*slidSegSize.value)).floor, 
		{slidSegIndex.valueAction = (buffSizes[0]/(44100*slidSegSize.value)).floor}
	);
	slidSegIndex.set(
		spec:ControlSpec(0,(buffSizes[0]/(44100*slidSegSize.value)).floor,\lin,1,21), 
		label: "Segment Index (" ++ wiiSegmentIndex ++ ")"
	);
	// we need also to redefine specSegIndex because it is used by the wiimote functions
	specSegIndex = ControlSpec(0,(buffSizes[0]/(44100*slidSegSize.value)).floor,\lin,1,21);
	 // end set
	// now update the segment sizes
	buffers.size.do ({|item, i|
		atqsTrack[i].segSize= (44100*slidSegSize.value);}); // end do
}); // end action



// ************
// cycle length
specCycleLength = ControlSpec(1,600,\lin,1,45);
slidCycleLength=EZSlider(w,Rect(10,150,eZsWidth,30), label: "Cycle Length(" ++ wiiCycleLength ++ ")", labelWidth:200, numberWidth: eZsNumWidth, controlSpec:specCycleLength ); 
slidCycleLength.font = font;

slidCycleLength.action_({
	buffers.size.do ({|item, i|
		atqsTrack[i].cycleLength= slidCycleLength.value;}); // end do
}); // end action

// *****************************
// tempo (actually a wait value)
specWait = ControlSpec(0.03125,0.5,\lin,0.01,0.42);
slidWait=EZSlider(w,Rect(10,200,eZsWidth,30), label: "Wait Val (**" ++ wiiWait ++ ")", labelWidth:200, numberWidth: eZsNumWidth, controlSpec: specWait); 
slidWait.font = font;
slidWait.action_({
	buffers.size.do ({|item, i|
		atqsTrack[i].tempo= slidWait.value;}); // end do
}); // end action

// ********
// duration
specNoteLength = ControlSpec(0.04,2,\lin,0.01,0.4);
slidNoteLength=EZSlider(w,Rect(10,250,eZsWidth,30), label: "Note Length (**" ++ wiiNoteLength ++ ")", labelWidth:200, numberWidth: eZsNumWidth, controlSpec:specNoteLength  ); 
slidNoteLength.font = font;
slidNoteLength.action_({	
	buffers.size.do ({|item, i|
		atqsTrack[i].dur= slidNoteLength.value;
	}); // end do
}); // end action

specWetDry = ControlSpec(0.0,1,\lin,0.005,0.0);
slidWetDry=EZSlider(w,Rect(10,300,eZsWidth,30), label: "Wet/Dry (" ++ wiiWetDry ++ ")", labelWidth:200, numberWidth: eZsNumWidth, controlSpec:specWetDry  ); 
slidWetDry.font = font;
slidWetDry.action_({	
	buffers.size.do ({|item, i|
		atqsTrack[i].wetDry= slidWetDry.value;
	}); // end do
}); // end action

adjust= 200;
// ********************************************************
//              MAKE BUTTON TO TURN ON/OFF THE WHOLE PLAYER
// ********************************************************
playerOnOff = SCButton(w, Rect(10, 400 + adjust,50,30));
playerOnOff.font = font;
playerOnOff.states = ([
		["on", Color.black, Color.green],
		["off", Color.black, Color.yellow]
	]);
playerOnOff .action = ({|butt|
	if (butt.value == 1, {
		
		buffers.size.do({|item, i|
			onOffs[i].valueAction = 0;
		}); // end do	
	}); // end if		
}); // end action

// ********************************************************
//   PRESET FUNCTIONS
// ********************************************************
addPreset = SCButton(w, Rect(10, 450+ adjust,250,50));
addPreset.font = font;
addPreset.states = ([
		["add preset", Color.black, Color.grey]
	]);

addPreset.action = ({this.addPresetAction}); // end action

// *******************
// initialisation line
this.updatePresetItemsList;
addPreset.enabled = isEnabled;

// ************************
//   SELECT A PRESET BUTTON
selectPreset = SCListView(w, Rect(350, 300+ adjust, 210,200));
selectPreset.font = font;
selectPreset.items = presetItemsList;
selectPreset.action = {|menu|
	"atqs select preset: ".post;
	menu.value.postln;
	this.applyPresets(menu.value);
};
selectPreset.enabled = isEnabled;

// ********************************
//  TEXTBOX TO CHANGE A PRESET NAME
namePreset = SCTextField(w, Rect(10,520+ adjust,200,50));
namePreset.font = font;
namePreset.string = presetsArray[0][6  + (buffers.size*2)];
namePreset.action = {|field|
	presetsArray[selectPreset.value][6  + (buffers.size*2)]= field.string ;
	this.updatePresetItemsList;
	selectPreset.items = presetItemsList;
	w.refresh;
	this.savePresetsToFile;
	
};
namePreset.enabled = isEnabled;

// ********************************************************
//          TRACK STUFF, INCLUDING TRACK GUI WIDGETS 
// ********************************************************

buffers.size.do ({|item, i|
	// add the track objects
	atqsTrack.add;
	atqsTrack[i] = AtqsTrack.new(buffers[i], i);
	// now add on/off buttons
	onOffs.add;
	onOffs[i] = SCButton(w, Rect(10 + (i*60), 400, 50,30));
	onOffs[i].font = font;
	onOffs[i].states = ([
		[i + "off", Color.black, Color.yellow],
		[i + "on", Color.black, Color.green]
	]);
	//"hello in do".postln;
	onOffs[i].action = ({|butt|
			if (butt.value == 1, 
				{
					atqsTrack[i].isOn = true;
					playerOnOff.valueAction = 0;
				}, 
				{	atqsTrack[i].isOn = false;
				});
	});	
	// now add randSel buttons;
	randSel.add;
	randSel[i] = SCPopUpMenu(w, Rect(10 + (i*60), 450, 50,30));
	randSel[i].font = font;
	randSel[i].items = ["1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15"];
	randSel[i].action = ({
			atqsTrack[i].aNum  = randSel[i].value;
	}); // end action
	// now add mixer faders
	mixFaders.add;
	mixFaders[i] = SCSlider(w, Rect(10 + (i*60), 500, 20, 80));
	mixFaders[i].action = ({|slid|
		atqsTrack[i].mixerSetting = slid.value.postln;
	});
	mixFaders[i].valueAction = initPartLevel;
	mixFaders[i].enabled = isEnabled;
}); // end buffers do

//now add two mixer sliders for the string synthesizers
mixFaders.add;
mixFaders[buffers.size] = SCSlider(w, Rect(10 + ((buffers.size)*60), 500, 20, 80));
mixFaders[buffers.size].action = ({|slid|
		slid.value.postln;
		mySynth[0].set(\amp, slid.value);
	});
mixFaders[buffers.size].valueAction = initStringLevel;
mixFaders[buffers.size].enabled = isEnabled;
mixFaders.add;
mixFaders[buffers.size+1] = SCSlider(w, Rect(10 + ((buffers.size +1)*60), 500, 20, 80));
mixFaders[buffers.size+1].action = ({|slid|
		slid.value.postln;
		mySynth[1].set(\amp, slid.value);
	});
mixFaders[buffers.size+1].valueAction = initStringLevel;
mixFaders[buffers.size + 1].enabled = isEnabled;


// ********************************************************
//          VALUE ACTIONS
// ********************************************************

this.applyPresets(0);

// ********************************************************
//                  WIIMOTES
// ********************************************************
// ********************************************************
//                  CYCLE LENGTH ADJUSTERS
// ********************************************************
// the cycle length can be adjusted in increments of 5 by
// hitting either the "left" or "right" buttons
atqsWii.add(wiiCycleLengthDecrease, {|t,r,msg|
	if (msg[1] ==1 , {
		{
		currentCycleLength = slidCycleLength.value -5;
		if (currentCycleLength < 1, {currentCycleLength = 1});
		slidCycleLength.valueAction = currentCycleLength;
		}.defer
	});	
}); 
atqsWii.add(wiiCycleLengthIncrease, {|t,r,msg|
	if (msg[1] ==1 , {
		{
		currentCycleLength = slidCycleLength.value +5;
		if (currentCycleLength > 600, {currentCycleLength = 600});
		slidCycleLength.valueAction = currentCycleLength;
		}.defer
	});
}); 

// ********************************************************
//                  
// ********************************************************
// the variable wiiTrackOnOff defines a clutch key to toggle on/off
// the currently selected track. Track selection is done in the usual way via 
// the joystick controller
atqsWii.add(wiiTrackOnOff, {|time, resp,msg|
{ // start a defer
if (msg[1] ==1, {
	if (atqsWii.joy == "right", 
			// defer all of it
			{
			if (onOffs[2].value ==1, 
				{onOffs[2].valueAction = 0}, 
				{onOffs[2].valueAction = 1}
			);
		}); // end if
	if (atqsWii.joy == "left", 
		{
			if (onOffs[1].value ==1, 
				{onOffs[1].valueAction = 0}, 
				{onOffs[1].valueAction = 1}
			);
		}); // end if
	if (atqsWii.joy == "up", 
		{
			if (onOffs[0].value ==1, 
				{onOffs[0].valueAction = 0}, 
				{onOffs[0].valueAction = 1}
			);
		}); // end if
	if (atqsWii.joy == "down", 
		{
			if (onOffs[3].value ==1, 
				{onOffs[3].valueAction = 0}, 
				{onOffs[3].valueAction = 1}
			);
		}); // end if
}); // end outer if
}.defer
});
// ********************************************************
//         USE JOYSTICK TO INDICATE A SELECTED TRACK         
// ********************************************************
// depending on the position of the joystick this will
// indicate one of the tracks 1 to 4 as selected
atqsWii.add("j", {|time, resp,msg|
	if (atqsWii.joy == "right", 
		{this.setLights(ixTwo,true)},
		{this.setLights(ixTwo,false)}
	); // end if
	if (atqsWii.joy == "left", 
		{this.setLights(ixOne,true)},
		{this.setLights(ixOne,false)}
		); // end if
	if (atqsWii.joy == "up", 
		{this.setLights(ixZero,true)},
		{this.setLights(ixZero,false)}
		); // end if
	if (atqsWii.joy == "down", 
		{this.setLights(ixThree,true)},
		{this.setLights(ixThree,false)}
		); // end if
});

// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!! CONTINUOUS CONTROLLER !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

// ********************************************************
//            PITCH RESPONDER      
// ********************************************************
// the action varies depending on the currently selected clutch
// also the clutch can be defined at the top of this file
// however, in general what is defined here is a set of controls 
// to adjust the various sliders
atqsWii.add("p", {|t,r,msg|
	// msg[1].postln;
	
	if (~clutches[0].at(wiiLastVal) == 1, {
			{slidLastVal.valueAction = msg[1]}.defer;	}); // end if
	if (~clutches[0].at(wiiSegmentIndex) == 1, {
			{slidSegIndex.valueAction = specSegIndex.map(msg[1])}.defer;	}); // end if	
	if (~clutches[0].at(wiiSegmentSize) == 1, {
			{slidSegSize.valueAction = specSegSize.map(msg[1])}.defer;	}) ;// end if			
	if (~clutches[0].at(wiiCycleLength) == 1, {
			{slidCycleLength.valueAction = specCycleLength.map(msg[1])}.defer;	}) ;// end if		
	if (~clutches[0].at(wiiWetDry) == 1, {
			{slidWetDry.valueAction = specWetDry.map(msg[1])}.defer;	}) ;// end if				
}); // end leaWii

// ********************************************************
//          NUNCHUK PITCH RESPONDER        
// ********************************************************
// the action varies depending on the currently selected clutch
// also the clutch can be defined at the top of this file
// however, in general what is defined here is a set of controls 
// to adjust the various sliders

atqsWii.add("np", {|t,r,msg|
	// msg[1].postln;
	if (~clutches[0].at(wiiWait) == 1, {
			{slidWait.valueAction = specWait.map(msg[1])}.defer;	}); // end if
	if (~clutches[0].at(wiiNoteLength) == 1, {
			{slidNoteLength.valueAction = specNoteLength.map(msg[1])}.defer;	}) // end if	
}); // end leaWii


// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!! END CONTINUOUS CONTROLLERS !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

// ********************************************************
//                  ADJUST NOTE DENSITY
// ********************************************************
atqsWii.add(wiiNoteDensityUp, {|t,r,msg|
	if (msg[1] ==1, {
		// switch case atqsWii.joy
		whichTrack = atqsWii.joy.switch(
			"right", {2},
			"left", {1},
			"up", {0},
			"down", {3},
			{"not applicable"}
		);
		// whichTrack.postln;
		if (whichTrack != "not applicable", 
		{
			{
				currentRandSel = randSel[whichTrack].value + 1;
				if (currentRandSel > 4, {currentRandSel = 0});
				randSel[whichTrack].valueAction = currentRandSel;
			}.defer;
		})
	}); // end if
	
}); 
atqsWii.add(wiiNoteDensityDown, {|t,r,msg|
	if (msg[1] ==1, {
		// switch case atqsWii.joy
		whichTrack = atqsWii.joy.switch(
			"right", {2},
			"left", {1},
			"up", {0},
			"down", {3},
			{"not applicable"}
		);
		if (whichTrack != "not applicable", 
		{
			{
				currentRandSel = randSel[whichTrack].value - 1;
				if (currentRandSel < 0, {currentRandSel = 4});
				randSel[whichTrack].valueAction = currentRandSel;
			}.defer;
		});
	}); // end if
}); 

// ***************************************
// now wiimote stuff for pitch controllers
// ***************************************
// *******************************************************************************
//          ------------------- PITCH SYNTHESIZER 1 ------------------------
// *******************************************************************************

// 
// **************************************
// now tie wiimote pitch to musical pitch
atqsWii.add("p", {|time, resp, msg|
if (suppressContinuousPitch[0] == false, {
	// ***********************************
	// pass the new value to the ringbuf array
	ringbuf[0] = ringbuf[0][1..] ++ msg[1];
	infreq[0] = ControlSpec(0, noteSpecRange[0], \lin, 1).map(ringbuf[0].median);
	mySynth[0].set(\freq, bigNoteList[infreq[0] + currentNoteIndex[0]].midicps)
}); // end if 
	}, // end of function, but don't forget to map to wiimote 2!
 2);

// *****************************************
// "b" must be held down for a note to sound
atqsWii.add("b", {|t,r,msg|
		mySynth[0].set(\gate, msg[1]);
}, 2); // end leawii

// ****************************************
// set "a" to trigger a "decoration" rutine
atqsWii.add("a", {|t,r,msg|
	if (msg[1] == 1, {
		// var initVal = ~atqs[smallest];
		var a, b;
		var odd = true;
		suppressContinuousPitch[0]=true;
		a = Pgeom.new(0.05,1.1,24);
		b = a.asStream;
		AppClock.sched(0.0,{ 
			if (odd==true, 
				{mySynth[0].set(\freq, bigNoteList[infreq[0] + currentNoteIndex[0]].midicps); odd = false},
				{mySynth[0].set(\freq, bigNoteList[infreq[0] + currentNoteIndex[0] + 1].midicps); odd = true}
			); // end if	
			b.next; 
		}); // end app clock				
	}, // end if msg[1] = 1
	{		suppressContinuousPitch[0]=false;
	}
); // end if
}, 2); // end leawii

// **************************
// roll now sets filter value
atqsWii.add("r", {|t,r,msg|
	{mySynth[0].set(\cutoff, ControlSpec.new(500,2000,\lin,1,200).map(msg[1]))}.defer;
}, 2); // end leaWii

// ******************************
// "up" raises current note range
atqsWii.add("u", {|t,r,msg|
	currentNoteIndex[0]= currentNoteIndex[0] + unitPitchShift[0];
	if (currentNoteIndex[0] > (bigNoteList.size-noteSpecRange[0]), {
			currentNoteIndex[0] = bigNoteList.size-noteSpecRange[0];
	});
}, 2);

// ********************************
// "down" lowers current note range
atqsWii.add("d", {|t,r,msg|
	currentNoteIndex[0]= currentNoteIndex[0]-unitPitchShift[0];
	if (currentNoteIndex[0] < 0 , {
			currentNoteIndex[0] = 0;
	});
}, 2);

// *******************************************************************************
//          ------------------- PITCH SYNTHESIZER 2 ------------------------
// *******************************************************************************

// 
// **************************************
// now tie nunchuk pitch to musical pitch
atqsWii.add("np", {|time, resp, msg|
if (suppressContinuousPitch[1] == false, {
	// ***********************************
	// pass the new value to the ringbuf array
	ringbuf[1] = ringbuf[1][1..] ++ msg[1];
	infreq[1] = ControlSpec(0, noteSpecRange[1], \lin, 1).map(ringbuf[1].median);
	mySynth[1].set(\freq, bigNoteList[infreq[1] + currentNoteIndex[1]].midicps)
}); // end if 
	}, // end of function, but don't forget to map to wiimote 2!
 2);

// *****************************************
// "z" must be held down for a note to sound
atqsWii.add("z", {|t,r,msg|
		mySynth[1].set(\gate, msg[1]);
}, 2); // end leawii

// ****************************************
// set "c" to trigger a "decoration" rutine
atqsWii.add("c", {|t,r,msg|
	if (msg[1] == 1, {
		// var initVal = ~atqs[smallest];
		var a, b;
		var odd = true;
		suppressContinuousPitch[1]=true;
		a = Pgeom.new(0.05,1.1,24);
		b = a.asStream;
		AppClock.sched(0.0,{ 
			if (odd==true, 
				{mySynth[1].set(\freq, bigNoteList[infreq[1] + currentNoteIndex[1]].midicps); odd = false},
				{mySynth[1].set(\freq, bigNoteList[infreq[1] + currentNoteIndex[1] + 1].midicps); odd = true}
			); // end if	
			b.next; 
		}); // end app clock				
	}, // end if msg[1] = 1
	{		suppressContinuousPitch[1]=false;
	}
); // end if
}, 2); // end leawii

// **************************
// roll now sets filter value
atqsWii.add("nr", {|t,r,msg|
	{mySynth[1].set(\cutoff, ControlSpec.new(500,2000,\lin,1,200).map(msg[1]))}.defer;
}, 2); // end leaWii

// ******************************************
// end of wiimote stuff for pitch controllers
// ******************************************

} // end run	
// ****************************************************************************************
//
//                              ADDITIONAL METHODS
//
//
// ****************************************************************************************

// ********************************************************
//                  SET LIGHTS
// ********************************************************
setLights {|track, state|
	if (state == true, 
	{ // begin if clause
		{ // begin defer
		onOffs[track].states = [
			[track.asString, Color.black, Color.red],
			[track.asString, Color.black, Color.red],
		];
		w.refresh;
			}.defer;	
	}, // end if, now do then
	{ // begin then clause
		{ // begin defer
			onOffs[track].states = [
				[track.asString, Color.black, Color.yellow],
				[track.asString, Color.black, Color.green],
			];
			w.refresh;
		}.defer;
	}); // end of whole if statement
} // end method setLights

// ********************************************************
//                  STOP AND START PLAYER
// ********************************************************
stopPlayer {
		{playerOnOff.valueAction = 1}.defer;
}

// ***************************
// a method to start the player
startPlayer {
	{playerOnOff.valueAction = 0;
	  myMixer.set(\gate, 1);	
	  "player now started!!!".postln;	
		}.defer;
}
// ********************************************************
//                  APPLY PRESETS
// ********************************************************
applyPresets {|ix|
	"in apply presets, ix is: ".post;
	ix.postln;
	slidLastVal.valueAction=presetsArray[ix][0]; // 600; // 0.62;
	slidSegIndex.valueAction=presetsArray[ix][1]; // shouldnt this always be calculated from the seg size????????????
	slidSegSize.valueAction=presetsArray[ix][2];
	slidCycleLength.valueAction=presetsArray[ix][3];
	slidWait.valueAction=presetsArray[ix][4];
	slidNoteLength.valueAction=presetsArray[ix][5];
	buffers.size.do ({|item,i|
		onOffs[i].valueAction = presetsArray[ix][6+i].asInteger;
		randSel[i].valueAction = presetsArray[ix][6+i+buffers.size].asInteger;
	});
	namePreset.string = presetsArray[ix][6  + (buffers.size*2)]
}

// ********************************************************
//                  UPDATE PRESET LIST
// ********************************************************
updatePresetItemsList {
	// regenerates the preset list after various changes
	presetItemsList = List.new(0);
	presetsArray.size.do({|i|
	presetItemsList.add;
	presetItemsList[i] = presetsArray[i][6  + (buffers.size*2)];
	presetItemsList[i];
});
presetItemsList = presetItemsList.asArray;
}
addPresetAction {
	// make an array to hold a single row of individual preset settings
	addPresetArray = Array.newClear(7 + (buffers.size*2));
	// now get values for the individual elements of this array
	// first the sliders
	addPresetArray[0]=slidLastVal.value; 
	addPresetArray[1]=slidSegIndex.value;
	addPresetArray[2]=slidSegSize.value;
	addPresetArray[3]=slidCycleLength.value;
	addPresetArray[4]=slidWait.value;
	addPresetArray[5]=slidNoteLength.value;
	// then the items for individual tracks
	buffers.size.do ({|item,i|
			// is this track on or off
		  addPresetArray[6+i] = onOffs[i].value;
		  // what is the density setting for this track
		  addPresetArray[6+i+buffers.size]= randSel[i].value;
	});
	// set default name for the preset
	addPresetArray[6 + (buffers.size*2)] = "preset" + Date.getDate.format("%Y-%d-%e-%T");
	// now add the new row to the preset array
	presetsArray = presetsArray.add(addPresetArray);
	this.savePresetsToFile;
		// add an item to the preset selector widget
	this.updatePresetItemsList;
	selectPreset.items = presetItemsList;
	w.refresh;
	// set the focus to the new preset
	selectPreset.valueAction = presetItemsList.size - 1;
} // end method
savePresetsToFile {
// now build a string from the presetsArray to save to file
	theString = "";
	presetsArray.size.do ({|i|
		presetsArray[0].size.do ({|j|
			theString = theString ++ presetsArray[i][j];
			if (j < (presetsArray[0].size - 1),
				{theString = theString ++ ","},
				{theString = theString ++ "\n"}
			); // end if
		});
	});
	// save the presets string to the file
	theFile = File(myDir ++ "/atqsPresets.txt", "w");
	theFile.write(theString);
	theFile.close;
	
	
}
} // end class



