(
// get the root directory for the app
var appDir= thisProcess.nowExecutingPath.dirname;

// there will be a folder called "audio", and within that folder there
// will be a series of directories to hold samples
// the names of the folder will be in a literal array thus:

// add the name of all directories here


// now the whole job can be done by iterating the ~folders array
~folders = ["parrots"];

// make a dictionary for pathmatches

~paths = Dictionary.new(0);

// and a dictioary for buffers

~buffers = Dictionary.new(0);

// and a dictionary for buffSizes

~buffSizes = Dictionary.new(0);
// now do the work

s.waitForBoot({
	// for each folder
	~folders.do({|folder, i|

		// ******************************************************
		// make the dictonary to hold the buffers for this folder
		~buffers[folder] = Dictionary.new(0);

		// ******************************************************
		// make the dictonary to hold the buffer sizes for this folder
		~buffSizes[folder] = Dictionary.new(0);

		// get the file names held in the folder into an array
		~paths[folder] = (appDir ++ "/" ++ folder ++ "/*.*" ).pathMatch;

		// *************************************************************
		// now iterate the files in this folder and load them to buffers
		~paths[folder].do({|path, j|
			Buffer.read(
				s,
				path,
				action:{|buffer|
					var fileName = path.basename.splitext[0];
					~buffers[folder].put(fileName, buffer);
					~buffers.postln;
					~buffSizes[folder].put(fileName, buffer.numFrames).postln;
					//~buffSizes.postln;
					if (j == (~paths[folder].size-1),
						{(folder + "is loaded").postln;}
					); // end if
				} // end action
			);
		});

		// now set up the housekeeping
		CmdPeriod.add({
			~buffers[folder].do ({|item, i|
				item.free;
			});
			Buffer.freeAll
		});
	}); // end folder do
}); // end wait for boot
s.waitForBoot({
	// load defs and presets
	var c;
	Routine.run({
		c=Condition.new;
		ParrotsDefs.new(s);
		~presetsArray = CSVFileReader.read(
			thisProcess.nowExecutingPath.dirname ++ "/parrotsPresets.txt");
		s.sync(c);
		"defs loaded!".postln;
	}); // end routine
});
) // end init

(
var folder = "parrots";
var initLevel = 0.4; // presumable an amplitude?
var width=600, height=30; // width and height for EZSliders
var server;
var myDir;
var leaWii;
var player; // a Pbind to play the synth
var addPreset, newPresetArray, presetsArray, theString, theFile;
var selectPreset, namePreset, presetItemsList;
var currentTempo = 1; //

// **************************************************************************
//        LOCK/UNLOCK VAR FOR GUI WIDGETS
// **************************************************************************
// set this to true to lock stuff
var isEnabled= true;

// ************************
// synth var and buffer var
var mainSynth;
var mainWin;
//var mainWin;
var currentBuffer=0, currentPreset = 0;


/*
var slidStartDur, specStartDur;
var slidEndDur, specEndDur;
var slidStartRate, specStartRate;
var slidEndRate, specEndRate;
var slidCurrentBuffer;*/





/*

var startPosControlSpec;
var txtStartPos, slidStartPos;*/
var doAction, accelFlag, previousAccelFlag;
var tempoText;
var font;
var eZsWidth;
var eZsNumWidth, eZsLabelWidth;
// not sur ehte following are classvars, don't think I knew what
// I was doing at this stage with these
var buffers, buffSizes;

var parrotMixer;
var specCurrentBuffer;
var currentTempoClock = TempoClock(currentTempo);
var tempoInc = 0.1;
var presets, slidPresets;
var specPanner, slidPanner;
// ************************

// make a synth and stop it
mainSynth=Synth.new(
	\parrots1,
	[\out, 0/*,
		\bufnum, ~buffers[folder][0],  // path.basename.splitext[0]
		\sPos , ~buffSizes[folder][0].rand*/
	]
);
mainSynth.stop;

// **********************
// make and show a window
mainWin = Window.new("parrots", bounds:Rect(10,10,640,800)).front;

// make sure window closes at end
CmdPeriod.add({mainWin.close});
// mainWin = CompositeView(mainWin,Rect(0,0,640,800)).background_(Color.grey.alpha_(0.3));
// mainWin.decorator = FlowLayout(mainWin.bounds);

// wiiMap Dictionary, define and add entries

~wiiMap = Dictionary.new;

~wiiMap["StartDur"] = "b"; //LEA
~wiiMap["endDur"] = "a"; // LEA
~wiiMap["StartRate"] = "b"; // on nunchuk JO
~wiiMap["endRate"] = "a"; // on nunchuk JO
~wiiMap["CurrentBuffer"] = "1"; // JO
~wiiMap["StartPos"] = "2"; // JO
~wiiMap["OnOff"] = "-";
~wiiMap["TempoIncrease"] = "right";
~wiiMap["TempoDecrease"] = "left";
~wiiMap["AddPreset"] = "home";
~wiiMap["PresetNavigationClutch"] = "c";
~wiiMap["Panner"] = "z";

"klklklklkl".postln;
// ***********************
// define a set of proxies

~proxies = Dictionary.new;

~proxies["startDur"] = PatternProxy.new(0.1);
~proxies["endDur"] = PatternProxy.new(0.1);
~proxies["startRate"] = PatternProxy.new(-6);
~proxies["endRate"] = PatternProxy.new(1);
~proxies["fadeOutTime"] = PatternProxy.new(0.05);
~proxies["fadeInTime"] = PatternProxy.new(0);
~proxies["startPos"] = PatternProxy.new(0);
~proxies["currentBuffer"] = PatternProxy.new(currentBuffer);
~proxies["dur"] = PatternProxy.new(1);
~proxies["panner"] = PatternProxy.new(0);
~proxies["amp"] = PatternProxy.new(initLevel);
~proxies["out"] = PatternProxy.new(0);

"klklklklkl22222222222".postln;





// first make a font
font = Font("Helvetica", 24);
eZsWidth = 550;
eZsNumWidth = 50;
eZsLabelWidth = 200;
~specs = Dictionary.new;
~widgets = Dictionary.new;

// ****************************************************************************************
//                                  START DURATION
// ****************************************************************************************


// *********
// make spec

~specs["startDur"] = ControlSpec(0.2,3,\lin,0.1);

// ***********
// make slider

~widgets["startDur"] = EZSlider.new(
	mainWin,
	labelWidth:eZsLabelWidth,
	label:"first Duration (" ++ ~wiiMap["startDur"] ++ ")",
	controlSpec:~specs["startDur"],
	bounds: Rect(10,10,width,25)

) ;

~widgets["startDur"].font = font;

// *************
// define action

~widgets["startDur"].action = ({|ez|
	~proxies["startDur"].source = ez.value;
});
// ****************************************************************************************
//                                  END DURATION
// ****************************************************************************************
"klklklklkl333333333333".postln;
// *********
// make spec

~specs["endDur"] = ControlSpec(0.2,3,\lin,0.1);

// ***********
// make slider

~widgets["endDur"] = EZSlider.new(
	mainWin,
	labelWidth:eZsLabelWidth,
	label:"last Duration (" ++ ~wiiMap["endDur"] ++ ")" ,
	controlSpec:~specs["endDur"] ,
	bounds: Rect(10,(10 + height),width,25)

) ;
~widgets["endDur"].font = font;

// define action
~widgets["endDur"].action = ({|ez|
	~proxies["endDur"].source = ez.value;
});

// ****************************************************************************************
//                                  START RATE
// ****************************************************************************************

// *********
// make spec

~specs["startRate"] = ControlSpec(-10,-0.0001,\lin,0.1);

// ***********
// make slider

~widgets["startRate"] = EZSlider.new(
	mainWin,
	labelWidth:eZsLabelWidth,
	label:"first rate (" ++ ~wiiMap["startRate"] ++ ")",
	controlSpec: ~specs["startRate"],
	bounds: Rect(10,(10 + (height*2)),width,25)
) ;
~widgets["startRate"].font = font;

// *************
// define action

~widgets["startRate"].action = ({|ez|
	~proxies["startRate"].source = ez.value;
});
// ****************************************************************************************
//                                  END RATE
// ****************************************************************************************

// make spec
~specs["endRate"] = ControlSpec(0.0001,10,\lin,0.1);
// make slider
~widgets["endRate"] = EZSlider.new(
	mainWin,
	labelWidth:eZsLabelWidth,
	label:"last rate (" ++ ~wiiMap["endRate"] ++ ")",
	controlSpec:~specs["endRate"] ,
	bounds: Rect(10,(10 + (height*3)),width,25)) ;

~widgets["endRate"].font = font;

// *************
// define action

~widgets["endRate"].action = ({|ez|
	~proxies["endRate"].source = ez.value;
});
"klklklklkl4444444444444".postln;
// ****************************************************************************************
//                                  CURRENT BUFFER SELECTOR
// ****************************************************************************************

// make spec
~specs["currentBuffer"] = ControlSpec(0,~buffSizes[folder].size-1,\lin,1);
// make slider
~widgets["currentBuffer"] = EZSlider.new(
	mainWin,
	labelWidth:eZsLabelWidth,
	initVal:0,
	label:"current buffer (" ++ ~wiiMap["currentBuffer"] ++ ")",
	controlSpec:~specs["currentBuffer"] ,
	bounds: Rect(10,(10 + (height*4)),width,25)
);
~widgets["currentBuffer"].font = font;
// define action
~widgets["currentBuffer"].action = ({|ez|
	currentBuffer = ez.value.postln;
	~proxies["currentBuffer"].source = currentBuffer;
	// also update the control spec for start position
	~specs["startPos"] = ControlSpec(0,~buffSizes[folder].asArray[currentBuffer].postln,\lin,100);
	mainWin.refresh;

});
~widgets["currentBuffer"].valueAction = 0;
~widgets["currentBuffer"].enabled = isEnabled;



"klklklklkl55555555555555".postln;
// ****************************************************************************************
//                               START POSITION (SIMPLE SLIDER)
// ****************************************************************************************

// this time a simple slider
~widgets["startPos"]= Slider(
	mainWin,
	Rect(260,(10 + (height*5)),150,25)
);
~widgets["startPos"].action = {
	"twattock".postln;
	~widgets["startPos"].value.postln;
	~specs["startPos"].postln;
	~twattock = ~specs["startPos"].map(~widgets["startPos"].value).postln;
	~proxies["startPos"].source = ~specs["startPos"].map(~widgets["startPos"].value).postln;
	~widgets["startPosVal"].value = ~specs["startPos"].map(~widgets["startPos"].value)
};

// simple slider so we have to do the label manually
~widgets["txtStartPos"]=StaticText(
	mainWin,
	Rect(10,(10 + (height*5)),
		eZsLabelWidth,25)
);
~widgets["txtStartPos"].align_(\right);
~widgets["txtStartPos"].font = font;
~widgets["txtStartPos"].string = "start position(" ++ ~wiiMap["startPos"] ++ ")";

// ****************************************************************************************
//                               START POSITION VALUE FIELD
// ****************************************************************************************

~widgets["startPosVal"] = TextField(
	mainWin,
	Rect(560,(10 + (height*5)),150,25)
);


// ****************************************************************************************
//                               START/STOP BUTTON
// ****************************************************************************************


// now a start/stop
~widgets["play"] = Button(
	mainWin,
	Rect(330, 300, 60, 60)
);
~widgets["play"].font = font;
~widgets["play"].states = ([
	["off (" ++ ~wiiMap["OnOff"]  ++ ")", Color.black, Color.yellow],
	["on", Color.black, Color.green]
]);
~widgets["play"].action = ({|butt|
	if (butt.value == 1, {player.play}, {player.pause});
}); // end action
// buttPlayer.valueAction = 0;

// ****************************************************************************************
//                           ADJUST TEMPO TEXT BOX
// ****************************************************************************************

~widgets["tempo"] = StaticText(mainWin, Rect(330, 400, 100,100));
~widgets["tempo"].string = "1"; // wiiTempoDecrease ++ ", " ++ wiiTempoIncrease;
~widgets["tempo"].background = Color.white;
~widgets["tempo"].align_(\center);
~widgets["tempo"].font = font;

// *******************************************************
// MIXER SLIDER
// *******************************************************
~widgets["amp"] = Slider(mainWin, Rect(10, 300, 20, 80));
~widgets["amp"].action = ({|slid|
	~proxies["amp"].source = slid.value.postln;
});
~widgets["amp"].enabled = isEnabled;

// ***************
// the pattern bit
player = Pbind (
	\instrument,  \parrots1,
	\bufnum, ~proxies["currentBuffer"],
	\firstDur, ~proxies["startDur"],
	\lastDur, ~proxies["endDur"],
	\startRate, ~proxies["startRate"],
	\endRate, ~proxies["endRate"],
	\sPos, ~proxies["startPos"],
	\dur, 1,
	\amp, ~proxies["amp"],
	\out, ~proxies["out"],
	\pos, ~proxies["panner"]
).play;
player.pause;

// ****************************************************************************
//                             PRESETS
// *****************************************************************************

Preset.new(mainWin,
	things:~widgets.postln,
	file: "parrotsPresets.txt"
);


)