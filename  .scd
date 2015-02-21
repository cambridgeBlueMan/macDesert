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