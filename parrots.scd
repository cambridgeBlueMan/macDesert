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

var makeCompSlider ; // NEW
// ************************

~dataStore = Dictionary.new(0);

//
~inputs = Dictionary.new(0);

~actionFunctions = Dictionary.new(0);



~inputs["wildfire/1/fader1"] = OSCFunc(
	{|msg, time, addr, recvPort|
		msg[1].postln;
		~dataStore[\startPos] = msg[1];
		~dataStore.changed(~dataStore[\startPos], msg[1]);
	},
	"/1/fader1"
); // end OSCFunc

~actionFunctions[\startPos] = {|theChanger, what, val|
	if (what == ~dataStore[\startPos],
		{
			{~widgets["startPos"].valueAction = ~dataStore[\startPos]}.defer
		}
	); // end if
};
~dataStore.addDependant(~actionFunctions[\startPos]);



// the various values in switchboard ccan be got from the presets on load, I think
//~dataStore[\setStartDur]
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

~wiiMap["startDur"] = "b"; //LEA
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

//"klklklklkl22222222222".postln;

// setter functions
~setters = Dictionary.new(0);



// first make a font
font = Font("Helvetica", 24);
eZsWidth = 550;
eZsNumWidth = 50;
eZsLabelWidth = 200;
~specs = Dictionary.new;
~widgets = Dictionary.new;
// x = 10;

// ****************************************************************************************
//                      FUNCTION TO MAKE A SLIDER
// ****************************************************************************************

makeCompSlider = {|win, height, dataId|

	// *************************************
	// declare variables with function scope
	var widgetHeight, staticText, textField, slider, model, updater, setValueFunction;

	// *********
	// MVC model
	model = (myValue: 0.0);

	// **********************
	// common action function
	setValueFunction = {|value|
		// update the local data model
		model[\myValue] = value;
		// and signal that it has changed
		model.changed(\value, value);
		// update the dataStore
		~dataStore[dataId] = value;
		// and signal that that has changed
		~dataStore.changed(dataId, value);
	};

	// ******************
	// make a static text
	widgetHeight = 20;
	staticText = StaticText.new(
		win, Rect(10,height.postln,200,widgetHeight)
	);

	// *************
	// make a slider
	slider = Slider.new(
		win,
		Rect(220,height, 200, widgetHeight);

	);

	// *********
	// MVC stuff
	slider.value = model[\myValue];
	slider.action = {|view| setValueFunction.value(view.value)};

	// ****************
	// make a textField
	textField = TextField.new(
		win,
		Rect(435, height, 50, widgetHeight)
	);

	// *********
	// MVC stuff
	textField.value = model[\myValue];
	textField.action = {|view| setValueFunction.value(view.value)};

	// ***************
	// udater function
	updater = {|theChanger, what, val|
		if(what == \value, {
			textField.value_(val);
			slider.value_(val);
			/*model[textString] = val;
			model[textString].postln;
			model.changed(textString, val);*/
		});
	};
	// *****************
	// add the local dependant
	model.addDependant(updater);
	staticText.align = \right;
	staticText.string = dataId;

	// ************
	// housekeeping
	win.onClose_({model.removeDependant(updater);});
};// end of function

// ****************************************************************************************
//                                  START DURATION
// ****************************************************************************************


// **********
// make specs

~specs["startDur"] = ControlSpec(0.2,3,\lin,0.1);
~specs["endDur"] = ControlSpec(0.2,3,\lin,0.1);
~specs["startRate"] = ControlSpec(-10,-0.0001,\lin,0.1);
~specs["endRate"] = ControlSpec(0.0001,10,\lin,0.1);
~specs["currentBuffer"] = ControlSpec(0,~buffSizes[folder].size-1,\lin,1);
~specs["startPos"] = ControlSpec(0,~buffSizes[folder].asArray[currentBuffer].postln,\lin,100).postln;


// ************************
// make sliders and setters


["startDur", "endDur", "startRate", "endRate", "currentBuffer", "startPos"].do({|item, i|

	// ***********
	// make slider

	~widgets[item] = makeCompSlider.value(
		mainWin,
		20 + (25*i),
		item
	);

	// *********
	// MVC stuff

	~setters[item] = {|theChanger, what, val|
		if (what == item,
			{~proxies[item].source = ~specs[item].map(val)};
		); // end if
		// the exception is current buffer, which also has to update a spec
		if (what == "currentBuffer",
			{
				~specs["startPos"] = ControlSpec(0,~buffSizes[folder].asArray[~specs["currentBuffer"].map(val).postln].postln,\lin,100);
			}
		); // end if
	};
	~dataStore.addDependant(~setters[item]);
}); // end do

// ***********************************
// release all dependants on CmdPeriod
CmdPeriod.add({
	~dataStore.release;
});



//
//
// ~widgets["startPos"]= makeCompSlider.value(
// 	mainWin,
// 	145, //20 + (25*i),
// 	"startPos"
// );
//
// // *********
// // MVC stuff
//
// ~setters["startPos"] = {|theChanger, what, val|
// 	if (what == "startPos",
// 		{~proxies["startPos"].source = ~specs["startPos"].map(~widgets["startPos"].value).postln;
// 			// ~widgets["startPos"].value = ~specs["startPos"].map(~widgets["startPos"].value)
// 	}); // end if
// };
// ~dataStore.addDependant(~setters["startPos"]);

	//
	// ~widgets["startPos"].action = {
	// 	"twattock".postln;
	// 	~widgets["startPos"].value.postln;
	// 	~specs["startPos"].postln;
	// 	~twattock = ~specs["startPos"].map(~widgets["startPos"].value).postln;
	// 	~proxies["startPos"].source = ~specs["startPos"].map(~widgets["startPos"].value).postln;
	// 	~widgets["startPosVal"].value = ~specs["startPos"].map(~widgets["startPos"].value)
	// };




	// ****************************************************************************************
	//                                  CURRENT BUFFER SELECTOR
	// ****************************************************************************************
	//
	// // make spec
	// ~specs["currentBuffer"] = ControlSpec(0,~buffSizes[folder].size-1,\lin,1);
	// // make slider
	// ~widgets["currentBuffer"] = EZSlider.new(
	// 	mainWin,
	// 	labelWidth:eZsLabelWidth,
	// 	initVal:0,
	// 	label:"current buffer (" ++ ~wiiMap["currentBuffer"] ++ ")",
	// 	controlSpec:~specs["currentBuffer"] ,
	// 	bounds: Rect(10,(10 + (height*4)),width,25)
	// );
	//
	// ~widgets["currentBuffer"].font = font;
	// // define action
	// ~widgets["currentBuffer"].action = ({|ez|
	// 	currentBuffer = ez.value.postln;
	// 	~proxies["currentBuffer"].source = currentBuffer;
	// 	// also update the control spec for start position
	// 	~specs["startPos"] = ControlSpec(0,~buffSizes[folder].asArray[currentBuffer].postln,\lin,100);
	// 	mainWin.refresh;
	//
	// });
	// "klklklklkl55555555555555".postln;
	// ~widgets["currentBuffer"].valueAction = 0;
	// ~widgets["currentBuffer"].enabled = isEnabled;
	//



	// ****************************************************************************************
	//                               START POSITION (SIMPLE SLIDER)
	// ****************************************************************************************

	/*// this time a simple slider
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

*/
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
		// file: "parrotsPresets.txt"
	);


)