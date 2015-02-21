(
// make a window
var win = Window.new("parrots", Rect(200,200,600,400));


// now make a function to create widgets in the window
var makeASlider = {|win, height, textString|

	// *************************************
	// declare variables with function scope
	var widgetHeight, staticText, textField, slider, model, updater, setValueFunction;

	// *********
	// MVC model
	model = (myValue: 0.0);

	// **********************
	// common action function
	setValueFunction = {|value|
		model [\myValue] = value;
		model.changed(\value, value);
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
		if(what == \alue, {
			textField.value_(val);
			slider.value_(val);
		});
	};
	// *****************
	// add the dependant
	model.addDependant(updater);
	staticText.align = \right;
	staticText.string = textString;

	// ************
	// housekeeping
	win.onClose_({model.removeDependant(updater);});
};// end of function
win.front;

5.do ({|i|

	makeASlider.value(20 + (i*25), "this is the label");
})


)