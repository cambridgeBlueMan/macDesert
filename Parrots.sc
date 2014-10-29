Parrots {
// &&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&
// you can use any of the following" "b", "a", "up", "down" "left", "right", "-", "home" "1", "2", "c", "z"
// DO NOT USE "+", it is reserved as the swap key between atqs and parrots
// DO NOT USE "home" it is reserved for saving a preset

// note that after you make a change here, you first need to save (apple-s), and then recompile (apple-k)

var wiiStartDur = "b"; //LEA
var wiiEndDur = "a"; // LEA
var wiiStartRate = "b"; // on nunchuk JO
var wiiEndRate = "a"; // on nunchuk JO
var wiiCurrentBuffer = "1"; // JO
var wiiStartPos = "2"; // JO
var wiiOnOff = "-";
var wiiTempoIncrease = "right";
var wiiTempoDecrease = "left";
var wiiAddPreset = "home";
var wiiPresetNavigationClutch = "c";
var wiiPanner = "z";

// &&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&
var initLevel = 0.4;
var width=600, height=30;
var server;
var myDir;
var <>leaWii;
var player, buttPlayer;
var addPreset, newPresetArray, presetsArray, theString, theFile;
var <selectPreset, namePreset, presetItemsList;
var currentTempo = 1; // 
// **************************************************************************
//        LOCK/UNLOCK VAR FOR GUI WIDGETS
// **************************************************************************
// set this to true to lock stuff
var isEnabled= true;
// ************************
// synth var and buffer var
var aSynth;
var aWin;
var aCompView;
var currentBuffer=0, currentPreset = 0;
var slidStartDur, specStartDur;
var slidEndDur, specEndDur;
var slidStartRate, specStartRate;
var slidEndRate, specEndRate;
var slidCurrentBuffer;
var startPosControlSpec;
var txtStartPos, slidStartPos;
var doAction, accelFlag, previousAccelFlag;
var tempoText;
var font;
var eZsWidth;
var eZsNumWidth, eZsLabelWidth;

classvar buffers, buffSizes;
// 
*new {|s|
	"_prots________".postln;
	s.postln;
	s ?? Server.default;
	s.postln;
		"______prots___".postln;
	^super.new.init(s)
} // end new

// *************************************************************************
//                        INITIALISATION
// *************************************************************************
init {|s|
myDir = Document.current.dir;
// override for network
// myDir = "/Volumes/lea/SC/DEV/DESERT";
// server = s;
s.postln;
s.waitForBoot({
	// ensure asynchronous stuff is complete before moving on
	var c;
	Routine.run({
		c=Condition.new;
		ParrotsDefs.new(s);
		buffers = GetBuffers2.new(s, (myDir ++ "/parrots/").postln).buffers;
		buffSizes = GetBuffers2.new(s, (myDir ++ "/parrots/").postln).buffSizes;
		presetsArray = CSVFileReader.read(myDir ++ "/parrotsPresets.txt");
		s.sync(c);
		// buffers.size.postln;
		"buffers loaded!".postln;
	}); // end routine
});
} // end init
// *************************************************************************
//                        RUN
// *************************************************************************
run {|out|


// possible inclusion of a preset load here

// ***********************
// define a set of proxies
var startDur = PatternProxy.new(0.1);
var endDur = PatternProxy.new(0.1);
var startRate = PatternProxy.new(-6);
var endRate = PatternProxy.new(1);
var fadeOutTime = PatternProxy.new(0.05);
var fadeInTime = PatternProxy.new(0);
var startPos = PatternProxy.new(0);
var currentBufferProxy = PatternProxy.new(currentBuffer);
var durProxy = PatternProxy.new(1);
var pannerProxy = PatternProxy.new(0);
var ampProxy = PatternProxy.new(initLevel);
var parrotMixer;
var specCurrentBuffer;
var currentTempoClock = TempoClock(currentTempo);
var tempoInc = 0.1;
var presets, slidPresets;
var specPanner, slidPanner;

// initialise some clocks
TempoClock.default_(currentTempoClock);

// ************************
// make a synth and stop it
aSynth=Synth.new(\parrots1, [\out, 0, \bufnum, buffers[0], \sPos , buffSizes[0].rand]); 
aSynth.stop;

// **********************
// make and show a window
aWin = Window.new("parrots", bounds:Rect(10,10,640,800)).front;

// make sure window closes at end
CmdPeriod.add({aWin.close});
aCompView = CompositeView(aWin,Rect(0,0,640,800)).background_(Color.grey.alpha_(0.3));
// aCompView.decorator = FlowLayout(aCompView.bounds);

// ***************************************************************************************
//                   BEGIN WIIMOTE STUFF
// ***************************************************************************************

leaWii=LeaWii.new(2);
// add responder
// *******************************************************************************
// LEA BLOCK LEA BLOCK LEA BLOCK LEA BLOCK LEA BLOCK LEA BLOCK LEA BLOCK LEA BLOCK
// no device specified and clutch[0] array
leaWii.add("p", {|t,r,msg|
	// msg[1].postln;
	if (~clutches[0].at(wiiStartDur) == 1, {
			{slidStartDur.valueAction = specStartDur.map(msg[1])}.defer;	}); // end if
	if (~clutches[0].at(wiiEndDur) == 1, {
			{slidEndDur.valueAction = specEndDur.map(msg[1])}.defer;
	}); // end if
	if (~clutches[0].at(wiiPanner) == 1, {
			{slidPanner.valueAction = specPanner.map(msg[1]).postln}.defer

	}) // end if

	/*
	if (~clutches[0].at(wiiCurrentBuffer) == 1, {
			{slidCurrentBuffer.valueAction = specCurrentBuffer.map(msg[1])}.defer;
	}); // end if
	if (~clutches[0].at(wiiStartPos) == 1, {
			{slidStartPos.valueAction = msg[1]}.defer
	}) // end if
	*/
}); // end leaWii

/*leaWii.add("p", {|t,r,msg|
	//msg[1].postln;
	if (~clutches[1].at(wiiPanner) == 1, {
		"in panner".postln;
			{slidPanner.valueAction = specPanner.map(msg[1]).postln}.defer
	}) // end if
}, 2); // end leaWii


*/
// add responder
/*
leaWii.add("np", {|t,r,msg|
	//msg[1].postln;
	if (~clutches[0].at(wiiStartRate) == 1, {
			{slidStartRate.valueAction = specStartRate.map(msg[1])}.defer
	}) // end if
}); // end leaWii
// add responder
leaWii.add("np", {|t,r,msg|
	//msg[1].postln;
	if (~clutches[0].at(wiiEndRate) == 1, {
			{slidEndRate.valueAction = specEndRate.map(msg[1])}.defer
	}) // end if
}); // end leaWii
*/
// *******************************************************************************
// JO BLOCK JO BLOCK JO BLOCK JO BLOCK JO BLOCK JO BLOCK JO BLOCK JO BLOCK
leaWii.add("p", {|t,r,msg|
	// msg[1].postln;
	/*
	if (~clutches[1].at(wiiStartDur) == 1, {
			{slidStartDur.valueAction = specStartDur.map(msg[1])}.defer;	}); // end if
	if (~clutches[1].at(wiiEndDur) == 1, {
			{slidEndDur.valueAction = specEndDur.map(msg[1])}.defer;
	}); // end if
*/	
	if (~clutches[1].at(wiiCurrentBuffer) == 1, {
			{slidCurrentBuffer.valueAction = specCurrentBuffer.map(msg[1])}.defer;
	}); // end if
	if (~clutches[1].at(wiiStartPos) == 1, {
			{slidStartPos.valueAction = msg[1]}.defer
	}) // end if
	
}, 2); // end leaWii
// add responder
leaWii.add("p", {|t,r,msg|
	//msg[1].postln;
	if (~clutches[1].at(wiiStartRate) == 1, {
			{slidStartRate.valueAction = specStartRate.map(msg[1])}.defer
	}) // end if
}, 2); // end leaWii
// add responder
leaWii.add("p", {|t,r,msg|
	//msg[1].postln;
	if (~clutches[1].at(wiiEndRate) == 1, {
			{slidEndRate.valueAction = specEndRate.map(msg[1])}.defer
	}) // end if
}, 2); // end leaWii




// *******************************************************************************
// JO BLOCK END JO BLOCK END JO BLOCK END JO BLOCK END JO BLOCK END JO BLOCK END JO BLOCK END JO BLOCK END

/*
// *********
// set tempo
leaWii.add(wiiTempoIncrease, {|t,r,msg|
	if (msg[1] == 1, {
					{currentTempo = currentTempo + tempoInc;
					//currentTempoClock = TempoClock(currentTempo);
					TempoClock.default.tempo = currentTempo;
					tempoText.string = currentTempo;
				}.defer
			}
		); // end if
}); // end responder

leaWii.add(wiiTempoDecrease, {|t,r,msg|
	if (msg[1] == 1, {
				{
					currentTempo = currentTempo - tempoInc;
					if (currentTempo < 0.2, {currentTempo = 0.2});
					//currentTempoClock = TempoClock(currentTempo);
					TempoClock.default.tempo = currentTempo;
					tempoText.string = currentTempo;
				}.defer
	}); // end if 
}); // end leaWii
leaWii.add(wiiAddPreset, {|t,r,msg|
	//"in home".postln;
	if (msg[1] == 1, {
		{this.addPresetAction}.defer;
	}); // end if	
}); // end responder				
*/leaWii.add(wiiOnOff, {|t,r,msg|
	if (msg[1] ==1, {
		{ // start defer
		if (buttPlayer.value == 0 , 
			{
				buttPlayer.valueAction = 1
			},
			{
				buttPlayer.valueAction = 0
			}
		); // end if
		}.defer;
	}); // end if
}); // end leaWii

// *******
// set var
doAction = "zilch";
// JJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJ OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO
leaWii.add(wiiPresetNavigationClutch, {|t,r,msg|
	if (msg[1] == 1, 
		{doAction = "navigate"; "on".postln}, {doAction = "doIt"; "off".postln}
	);
}, 2); // end responder

leaWii.add("nr", {|t,r,msg|
	// map the value of msg[1] to the range 0 to presetsArray.size
	var theVal = ControlSpec(0, presetsArray.size, \lin, 1).map(msg[1]);
	var temp = ControlSpec(0, presetsArray.size, \lin, 1).map(msg[1]);
	if (~clutches[1].at(wiiPresetNavigationClutch) == 1, 
	// then
	{	
		{selectPreset.value = theVal}.defer;
		doAction = true;
	}, // end if clause 
	// else
	{
	if (doAction == "doIt", 	{
	// var temp = ControlSpec(0, presetsArray.size, \lin, 1).map(msg[1]);
		// "in do it".postln;
			// {selectPreset.valueAction = temp-1; }.defer;û
				{this.applyPresets(ControlSpec(0, presetsArray.size, \lin, 1).map(msg[1]))}.defer;
			doAction = "zilch";
	})
	});
}, 2); // end responder
// END JJJJJJJJJJJJJJJJJJJJJJJJJJ  OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO
// ****************************************************************************************
//                                  END WIIMOTE STUFF
// ****************************************************************************************

// first make a font
font = SCFont("Helvetica", 24);
eZsWidth = 550;
eZsNumWidth = 50;
eZsLabelWidth = 200;

// ****************************************************************************************
//                                  START DURATION
// ****************************************************************************************

// make spec
specStartDur = ControlSpec(0.2,3,\lin,0.1);
// make slider
slidStartDur = EZSlider.new(aCompView, labelWidth:eZsLabelWidth, label:"first Duration (" ++ wiiStartDur ++ ")", controlSpec:specStartDur, bounds: Rect(10,10,width,25) ) ;
slidStartDur.font = font;
// define action
slidStartDur.action = ({|ez|
	startDur.source = ez.value;
});
// ****************************************************************************************
//                                  END DURATION
// ****************************************************************************************

// 
// make spec
specEndDur = ControlSpec(0.2,3,\lin,0.1);
// make slider
slidEndDur = EZSlider.new(aCompView, labelWidth:eZsLabelWidth, label:"last Duration (" ++ wiiEndDur ++ ")" , controlSpec:specEndDur ,bounds: Rect(10,(10 + height),width,25)) ;
slidEndDur.font = font;
// define action
slidEndDur.action = ({|ez|
	endDur.source = ez.value;
});	

// ****************************************************************************************
//                                  START RATE
// ****************************************************************************************

// make spec
specStartRate = ControlSpec(-10,-0.0001,\lin,0.1);
// make slider
slidStartRate = EZSlider.new(aCompView, labelWidth:eZsLabelWidth, label:"first rate (" ++ wiiStartRate ++ ")", controlSpec: specStartRate,bounds: Rect(10,(10 + (height*2)),width,25)) ;
slidStartRate.font = font;
// define action
slidStartRate.action = ({|ez|
	startRate.source = ez.value;
});
// ****************************************************************************************
//                                  END RATE
// ****************************************************************************************

// make spec
specEndRate = ControlSpec(0.0001,10,\lin,0.1);
// make slider
slidEndRate = EZSlider.new(aCompView, labelWidth:eZsLabelWidth, label:"last rate (" ++ wiiEndRate ++ ")", controlSpec:specEndRate ,bounds: Rect(10,(10 + (height*3)),width,25)) ;
slidEndRate.font = font;
// define action
slidEndRate.action = ({|ez|
	endRate.source = ez.value;
});

// ****************************************************************************************
//                                  CURRENT BUFFER SELECTOR
// ****************************************************************************************

// make spec
specCurrentBuffer = ControlSpec(0,buffSizes.size-1,\lin,1);
// make slider
slidCurrentBuffer = EZSlider.new(aCompView, labelWidth:eZsLabelWidth, initVal:0, label:"current buffer (" ++ wiiCurrentBuffer ++ ")", controlSpec:specCurrentBuffer ,bounds: Rect(10,(10 + (height*4)),width,25));
slidCurrentBuffer.font = font;
// define action
slidCurrentBuffer.action = ({|ez|
	currentBuffer = ez.value;
	currentBufferProxy.source = currentBuffer;
	// also update the control spec for start position
	startPosControlSpec = ControlSpec(0,buffSizes[currentBuffer],\lin,100);
	aWin.refresh;
	
});
slidCurrentBuffer.valueAction = 0;
slidCurrentBuffer.enabled = isEnabled;

// this time a simple slider
slidStartPos= Slider(aCompView, Rect(160,(10 + (height*5)),150,25));
		slidStartPos.action = {startPos.source = startPosControlSpec.map(slidStartPos.value) };

txtStartPos=StaticText(aCompView, Rect(10,(10 + (height*5)),eZsLabelWidth,25));
txtStartPos.align_(\right);
txtStartPos.font = font;
txtStartPos.string = "start position (" ++ wiiStartPos ++ ")";

// now a start/stop
buttPlayer = SCButton(aCompView, Rect(330, 300, 60, 60));
buttPlayer.font = font;
buttPlayer.states = ([
	["off (" ++ wiiOnOff ++ ")", Color.black, Color.yellow],
	["on", Color.black, Color.green]
]);
buttPlayer.action = ({|butt|
	if (butt.value == 1, {player.play}, {player.pause});
}); // end action
// buttPlayer.valueAction = 0;

tempoText = SCStaticText(aCompView, Rect(330, 400, 100,100));
tempoText.string = "1"; // wiiTempoDecrease ++ ", " ++ wiiTempoIncrease;
tempoText.background = Color.white;
tempoText.align_(\center);
tempoText.font = font;

// ********************************************************
//  ADD PRESET BUTTON
// ********************************************************
addPreset = SCButton(aCompView, Rect(10, 250,130,30));
addPreset.font = font;
addPreset.states = ([
		["add preset (" ++ wiiAddPreset ++ ")", Color.black, Color.grey]
	]);
addPreset.action = ({this.addPresetAction}); // end action
this.updatePresetItemsList;
addPreset.enabled = isEnabled;
// *******************************************************
// MIXER SLIDER
// *******************************************************
parrotMixer = SCSlider(aCompView, Rect(10, 300, 20, 80));
parrotMixer.action = ({|slid|
	ampProxy.source = slid.value.postln;
});
parrotMixer.enabled = isEnabled;
// ********************************************************
//          SELECT A PRESET BUTTON
// ********************************************************
selectPreset = SCListView(aCompView, Rect(150, 250, 160,350));
selectPreset.font = font;
selectPreset.items = presetItemsList;
selectPreset.action = {|menu|
	"parrots select preset".post;
	menu.value.postln;
	this.applyPresets(menu.value);
};
selectPreset.enabled = isEnabled;
/*selectPreset.enterKeyAction = {|menu|
	this.applyPresets(menu.value);
};*/
// ********************************************************
//          TEXTBOX TO CHANGE A PRESET NAME
// ********************************************************
namePreset = SCTextField(aCompView, Rect(330,250,200,20));
namePreset.font = font;
// set the initial value
namePreset.string = presetsArray[0][6];
namePreset.action = {|field|
	// insert the new value into the presetsArray at the appropriate place
	presetsArray[selectPreset.value][6]= field.value ;
	this.updatePresetItemsList;
	selectPreset.items = presetItemsList;
	aCompView.refresh;
	this.savePresetsToFile;
	
};
namePreset.enabled = isEnabled;
// ****************************************************************************************
//                                  Panner
// ****************************************************************************************
// make spec
specPanner = ControlSpec(-1,1,\lin,0.005,0);
// make slider
slidPanner = EZSlider.new(aCompView, labelWidth:150, initVal:0, label:"panner (" ++ wiiPanner ++ ")", controlSpec:specPanner ,bounds: Rect(10,(10 + (height*6)),width,25));
slidPanner.font = font;
// define action
slidPanner.action = ({|ez|
	pannerProxy.source = ez.value;
});


// ***************
// the pattern bit
player = Pbind (
		\instrument,  \parrots1,
		\bufnum, currentBufferProxy,
		\firstDur, startDur,
		\lastDur, endDur,
		\startRate, startRate,
		\endRate, endRate,
		\sPos, startPos,
		\dur, 1,
		\amp, ampProxy,
		\out, out,
		\pos, pannerProxy
).play;
player.pause;
parrotMixer.valueAction= initLevel;
ampProxy.source = initLevel;
} // end run
// ***************************
// a method to stop the player
stopPlayer {
	{buttPlayer.value = 0;
		player.pause; }.defer;
}

// ***************************
// a method to start the player
startPlayer {
	{buttPlayer.value = 1;
		player.play;}.defer;	
}

// ********************************************************
//                  APPLY PRESETS
// ********************************************************
applyPresets {|ix|
	var temp;
	slidStartDur.valueAction=presetsArray[ix][0]; // 600; // 0.62;
	slidEndDur.valueAction=presetsArray[ix][1]; // shouldnt this always be calculated from the seg size????????????
	slidStartRate.valueAction=presetsArray[ix][2];
	slidEndRate.valueAction=presetsArray[ix][3];
	slidCurrentBuffer.valueAction=presetsArray[ix][4];
	temp = presetsArray[ix][4].asInteger;
	startPosControlSpec = ControlSpec(0,buffSizes[temp],\lin,100);
	slidStartPos.valueAction=presetsArray[ix][5].asFloat;
	namePreset.string = presetsArray[ix][6].asString;
	tempoText.string = presetsArray[ix][7].asString;
	//TempoClock.default.tempo = presetsArray[ix][7].asFloat;
}

// ********************************************************
//                  UPDATE PRESET LIST
// ********************************************************
updatePresetItemsList {
	// regenerates the preset list after various changes
	presetItemsList = List.new(0);
	presetsArray.size.do({|i|
		presetItemsList.add;
		presetItemsList[i] = presetsArray[i][6];
		presetItemsList[i];
	});
	presetItemsList = presetItemsList.asArray;
}
addPresetAction {
	// make an array to hold a single row of individual preset settings
	newPresetArray = Array.newClear(8);
	// now get values for the individual elements of this array
	// first the sliders
	newPresetArray[0]=slidStartDur.value; 
	newPresetArray[1]=slidEndDur.value;
	newPresetArray[2]=slidStartRate.value;
	newPresetArray[3]=slidEndRate.value;
	newPresetArray[4]=slidCurrentBuffer.value;
	newPresetArray[5]=slidStartPos.value;
	// set default name for the preset
	newPresetArray[6] = "preset" + Date.getDate.format("%Y-%d-%e-%T");
	// set tempo
	newPresetArray[7] = tempoText.string;
	// now add the new row to the preset array
	presetsArray = presetsArray.add(newPresetArray);
	this.savePresetsToFile;
		// add an item to the preset selector widget
	this.updatePresetList;
	selectPreset.items = presetItemsList;
	aCompView.refresh;
	// set the focus to the new preset
	selectPreset.valueAction = presetItemsList.size - 1;
}
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
	theFile = File(myDir ++ "/parrotsPresets.txt", "w");
	theFile.write(theString);
	theFile.close;
	
	
}

} // end class



