ParrotsSlider {
	// some vars
	var widgetHeight, staticText, textField, <>slider, <model, updater;

	*new {|win, height, dataId, dataStore|
		^super.new.init(win, height, dataId, dataStore)
	} // end new
	init {|win, height, dataId, dataStore|

		// *********
		// MVC model
		model = (myValue: 0.0);



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
		slider.action = {|view| this.setValue(view.value, dataId, dataStore)};

		// ****************
		// make a textField
		textField = TextField.new(
			win,
			Rect(435, height, 50, widgetHeight)
		);

		// *********
		// MVC stuff
		textField.value = model[\myValue];
		textField.action = {|view| this.setValue(view.value, dataId, dataStore)};

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
	} // end init
	// **********************
	// common action function
	setValue  {|value, dataId, dataStore|
		// update the local data model
		model[\myValue] = value;
		// and signal that it has changed
		model.changed(\value, value);
		// update the dataStore
		dataStore[dataId] = value;
		// and signal that that has changed
		dataStore.changed(dataId, value);
	}
} // end of class