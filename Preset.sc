/*
(
var win = Window.new
~wiidgets = List.new(0);

4.do ({|i|
~wiidgets.add;
~wiidgets[i] = Slider.new(win, bounds: Rect((100*i), 50, 80, 100)).front;
})
)

then run:

Preset.new(things:~wiidgets)
*/
/*
Intended to allow saving of a set of presets from a list of things




*/
Preset  {
	var presetsArray; // 2D array to load from presets file
	var view, rect;
	var presetsFilePath; // path to presets file as string
	var presetsFile; // a File object
	var addPresetButton;
	var addPresetButtonAction;
	var deletePresetButton, deletePresetButtonAction;
	var selectPresetListView; //to select a different preset
	var presetItemsList; // array of item strings to display
	var renamePresetTextField;
	var newPresetArray; // single row array to hold values for a new preset

	// ************************************************************************

	//                                         NEW

	// ************************************************************************
	*new {|window, pos, file, things|
		^super.new.init(window, pos, file, things)
	} // end new
	// ************************************************************************

	//                                         INIT

	// ************************************************************************
	init {|window, pos, file, things|
		"in init".postln;
		// *********************
		// throw error if no list
		if (things.isNil,{ Error("you must provide a wiidget list").throw});

		//things.postln;
		// convert the dictionary to an array sorted by key
		// i.e. [key1, val1, key2, val2, key3, val3 etc]
		things = things.asSortedArgsArray.postln;
		// then convert that to 2D array ready for manipulation
		things = Array2D.fromArray(things.size/2, 2, things);
		things = things.colAt(1);
		things.postln;
		// following doesn't cut it cos we can't depend on the order
		// things = Dictionary.newFrom(things.asSortedArgsArray).postln;
		// *******************************
		// load file to array if it exists
		presetsFilePath = file ?? "presets.txt";
		presetsFilePath = (
			// doesn't work in ide
			//Document.current.dir ++ "/" ++ presetsFilePath);
			// so
			thisProcess.nowExecutingPath.dirname ++ "/" ++ presetsFilePath);
		if (File.exists(presetsFilePath), {
			presetsArray = CSVFileReader.read(
				(presetsFilePath);
			);
			},
			{presetsArray = Array.new(0);
		});


		// ****************************************
		// did we get a window and pos, if not create
		window = window ?? {Initialiser.initWin(aName: "made in preset")};
		//if (window.isNil, {Error("you must provide a window for the Preset class").throw});

		pos = pos ?? {50@400};
		rect = Rect(pos.x, pos.y, 400, 400);
		// ========================================================================
		//                                GUI
		// ========================================================================

		// *****************************************
		// make a view and all the other gui objects
		view =  View.new(window, bounds: rect);

		// *****************
		// add preset button
		addPresetButton =  Button.new(view,Rect(29, 32, 100, 20))
		.states_([ [ "add preset", Color.black, Color.grey] ]);
		addPresetButton.enabled = true;
		addPresetButton.action = {this.addPresetAction(things)};

		// *******************
		// delete preset button
		deletePresetButton =  Button.new(view,Rect(30, 70, 100, 20))
		.states_([ [ "delete preset", Color.black, Color.grey ] ]);
		deletePresetButton.action = {|v|
			// not yet implemented
		};

		// **************************
		// ListView to select a preset
		selectPresetListView =  ListView.new(view,Rect(150, 70, 150, 210));
		selectPresetListView.items = presetItemsList;
		selectPresetListView.action = {|menu|
			renamePresetTextField.string = presetsArray[menu.value][0];
			things.do({|item, i|
				item.class.postln;
				if (item.isKindOf(MixerChannel),
					{
						presetsArray[menu.value][i+1];
						item.mute((presetsArray[menu.value][i+1]).asInteger.asBoolean);
				}); // end if kind of mixer channel
				if (item.isKindOf(QSlider), {
					item.valueAction = presetsArray[menu.value][i+1].postln;
				}); // end if kind of slider
				if (item.isKindOf(EZSlider), {
					item.valueAction = presetsArray[menu.value][i+1].postln;
				}); // end if kind of slider
				// =================================
				//
				//
				// =================================
				if (item.isKindOf(Array), {
					item.do ({|item, i| item.postln;"hello everybody".postln;}); // end do
				});
			}); // end do
			renamePresetTextField.enabled = true;
		}; // end action
		selectPresetListView.enabled = true;

		// *****************************************
		// text field to change the name of a preset
		renamePresetTextField = TextField.new(view,Rect(150, 30, 150, 20));
		// set the initial value
		renamePresetTextField.string = ""; // presetsArray[0][0];
		renamePresetTextField.action = {|field|
			// insert the new value into the presetsArray at the appropriate place
			presetsArray[selectPresetListView.value][0]= field.value ;
			this.regenPresetItemsList(selectPresetListView.value);
			this.savePresetsToFile;
		};
		renamePresetTextField.enabled = false;

		// ******************
		// Initial processing
		this.regenPresetItemsList(nil);

	}
	// ************************************************************************

	//                                         APPLY PRESET

	// ************************************************************************
	applyPreset   {|row|
		"in it".postln;
		selectPresetListView.valueAction = row;
	}
	// ************************************************************************

	//                              REGENERATE PRESET ITEM LIST

	// ************************************************************************
	regenPresetItemsList  {|row|
		presetItemsList = List.new(0);
		(presetsArray.size).do({|i|
			presetItemsList.add;
			presetItemsList[i] = presetsArray[i][0];
			presetItemsList[i];
		});
		presetItemsList = presetItemsList.asArray;
		//"in regen".postln;
		selectPresetListView.items = presetItemsList;
		selectPresetListView.value = row;
		view.refresh;
	}
	// ************************************************************************

	//                              ADD PRESET ACTION

	// ************************************************************************
	addPresetAction  {|things|

		// ****************************************************************
		// make an array to hold a single row of individual preset settings
		newPresetArray = Array.newClear((things.size + 1););
		// note that the wiidget list is 1 smaller than  a preset row size
		// this is because slot 0 is reserved for name of preset
		// this only exists in file and not in wiidget list
		things.do({|item, i|
			things.postln;
			item.postln;
			"twattock".postln;
			// note that we have the actual objects here, so we can test them for type
			// and process accordingly
			if (item.isKindOf(MixerChannel),
				{
					newPresetArray[i+1] = item.muted.asInteger;
			});
			if (item.isKindOf(QSlider), {
				newPresetArray[i+1] = item.value.postln;
			});
			if (item.isKindOf(EZSlider), {
				newPresetArray[i+1] = item.value.postln;
			});
			// =================================
			//
			//
			// =================================

			// add the array
			if (item.isKindOf(Array), {
				"in array".postln;
				newPresetArray[i+1] = item;
			});

		});

		// **********************************
		// now set default name for the preset
		newPresetArray[0] = "preset" + Date.getDate.format("%Y-%d-%e-%T");
		newPresetArray;
		// ***************************************
		// now add the new row to the preset array
		presetsArray = presetsArray.add(newPresetArray);
		this.regenPresetItemsList(presetsArray.size - 1);

		// ******************************
		// save the updated array to file
		this.savePresetsToFile;

		// *******************************
		// set the focus to the new preset
		selectPresetListView.valueAction = presetItemsList.size - 1;
		renamePresetTextField.enabled = true;
	}

	// ************************************************************************

	//                              SAVE PRESETS TO FILE

	// ************************************************************************
	savePresetsToFile   {

		// ********************************************************
		// now build a string from the presetsArray to save to file
		var presetsAsString = "";
		presetsArray.size.do ({|i|
			presetsArray[0].size.do ({|j|
				presetsAsString = presetsAsString ++ presetsArray[i][j];
				if (j < (presetsArray[0].size - 1),
					{presetsAsString = presetsAsString ++ ","},
					{presetsAsString = presetsAsString ++ "\n"}
				); // end if
			});
		});

		// ***********************************
		// save the presets string to the file
		presetsFilePath.postln;
		presetsFile = File(presetsFilePath, "w");
		presetsFile.write(presetsAsString);
		presetsFile.close;
	}
} // end of class
