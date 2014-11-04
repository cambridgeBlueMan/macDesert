NetAddr.langPort
(
// *******************************
// define the model as a dictionary
var model=Dictionary.new(0);

/// *******************************************************
// define an OSCFunc to handle changes to a TouchOsc fader1
var android_1_fader1 = OSCFunc({|msg|
	model[\leftHandBell]= msg[1].postn("just set from phone");

	// **************************************************************
	// broadcast the fact that this has changed to all the dependants
	model.changed(\leftHandBell, msg[1]);
	},
	"/1/fader1"
);
var toggle = OSCFunc({|msg|
	msg[1].postln
	},
	"/1/toggle1"
);
var leftHandBell = {|theChanger, what, val|
	if (what == \leftHandBell,
		{val.postn("from the updater function")};
	);
};

model.addDependant(leftHandBell);
)

(
var controller, model, view;

model = Ref(0.5);
controller = SimpleController(model);
controller.put(\value,
    {|theChanger, what, moreArgs|
        view.value_(theChanger.value);
    });

view = Slider(Window("slider", Rect(100, 100, 330, 38)).front, Rect(5, 5, 320, 20));
view.onClose_{controller.remove};

// run a routine to change the model's value:
r{
    100.do{
        model.value_(1.0.rand.postln).changed(\value);
        0.5.wait;
    }
}.play(AppClock)
)