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
	var addAction;
	var addActionAction;
	var deleteButton, deleteButtonAction;
	var presetList; //to select a different preset
	var listOfPresets; // array of item strings to display
	var renamePreset;
	var newPresetArray; // single row array to hold values for a new preset
	var namesOfThings; // 1D array containing keys from the dataStore
	var parrotsPresets = Dictionary.new(0)

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
		var tempThings = things.asSortedArgsArray.postn("should be an array");
		"in init".postln;
		// *********************
		// throw error if no list
		if (things.isNil,{ Error("you must provide a data list").throw});

		things.postn("things as passed");

		// ******************
		// insist on a window
		if (window.isNil, {Error("you must provide a window for the Preset class").throw});

		pos = pos ?? {50@400};
		rect = Rect(pos.x, pos.y, 400, 400);
		// ========================================================================
		//                                ARCHIVE
		// ========================================================================

		Archive.archiveDir = thisProcess.nowExecutingPath.dirname ++ "/archive/";

		// ========================================================================
		//                                GUI
		// ========================================================================

		// *****************************************
		// make a view and all the other gui objects
		view =  View.new(window, bounds: rect);

		// *****************
		// add preset button
		addButton =  Button.new(view,Rect(29, 32, 100, 20))
		.states_([ [ "add preset", Color.black, Color.grey] ]);
		addAction.enabled = true;
		addAction.action = {this.addAction(things)};

		// *******************
		// delete preset button
		deleteButton =  Button.new(view,Rect(30, 70, 100, 20))
		.states_([ [ "delete preset", Color.black, Color.grey ] ]);
		deleteButton.action = {|v|
			// not yet implemented
		};

		// **************************
		// ListView to select a preset
		presetList =  ListView.new(view,Rect(150, 70, 150, 210));
		presetList.items = listOfPresets;
		presetList.action = {|menu|
			renamePreset.string = presetsArray[menu.value][0];
			things.do({|item, i|

			}); // end do
			renamePreset.enabled = true;
		}; // end action
		presetList.enabled = true;

		// *****************************************
		// text field to change the name of a preset
		renamePreset = TextField.new(view,Rect(150, 30, 150, 20));
		// set the initial value
		renamePreset.string = ""; // presetsArray[0][0];
		renamePreset.action = {|field|
			// insert the new value into the presetsArray at the appropriate place
			presetsArray[presetList.value][0]= field.value ;
			this.buildListOfPresets(presetList.value);
			this.savePresetsToFile;
		};
		renamePreset.enabled = false;

		// ******************
		// Initial processing
		this.buildListOfPresets(nil);

	}
	// ************************************************************************

	//                                         APPLY PRESET

	// ************************************************************************
	applyPreset   {|row|
		"in it".postln;
		presetList.valueAction = row;
	}
	// ************************************************************************

	//                              REGENERATE PRESET ITEM LIST

	// ************************************************************************
	buildListOfPresets  {|row|
		listOfPresets = List.new(0);

		(presetsArray.size).do({|i|
			listOfPresets.add;
			listOfPresets[i] = presetsArray[i][0];
			listOfPresets[i];
		});
		listOfPresets = listOfPresets.asArray;
		//"in regen".postln;
		presetList.items = listOfPresets;
		presetList.value = row;
		view.refresh;
	}
	// ************************************************************************

	//                              ADD PRESET ACTION

	// ************************************************************************
	addAction  {|things|
		// I believe that this is now simply the process of saving the data store
		// with a generated name

		parrotsPresets[("preset" + Date.getDate.format("%Y-%d-%e-%T")).asSymbol] = things;

		// **********************************
		// now set default name for the preset
		Archive.global.put(
			("preset" + Date.getDate.format("%Y-%d-%e-%T")).asSymbol,
			things
		)

		// add to the list of presets
		this.buildListOfPresets(presetsArray.size - 1);

		// *******************************
		// set the focus to the new preset
		presetList.valueAction = listOfPresets.size - 1;
		renamePreset.enabled = true;
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
