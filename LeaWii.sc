LeaWii {
// this is gradually moving towards being a general purpose wii class. the following are now available 
// for any number of devices:
// x[devNum] for nunchuk joystick x coord for device devNum
// y[devNum] for nunchuk joystick y coord for device devNum
// joy[devNum] to indicate "left", "right", "up" or "down" for joystick

	var <x,<y;
	var <>listResponders, aString, <>listRespString, listSize = 0;

*new {|numDevices, netAddr|
	^super.new.init(numDevices, netAddr)
}
init {|numDevices, netAddr|
	listResponders = List.new;
	listRespString = List.new;
	netAddr = netAddr ? NetAddr.new("127.0.0.1", 8000);
	numDevices = numDevices ? 1;
	CmdPeriod.add({
	aString = "responders released, we hope!";
		listResponders.do(
			{arg item; item.remove}
		); // end do
		}; // end function
	); // end CmdPeriod
	~clutches = Array.newClear(numDevices);
	~accels =  Array.newClear(numDevices);
	x = Array.fill(numDevices, 0.5);
	y = Array.fill(numDevices, 0.5);
	numDevices.do ({|i|
		"in do".postln;
		this.add("jx", {|time,resp,msg| x[i] = msg[1];}, i+1);
		this.add("jy", {|time,resp,msg| y[i] = msg[1];}, i+1);
		// make a dictionary 
		~clutches[i] = Dictionary.new(14);
		// add dictionary item and add the leaWii responderNodes
		["X", "a", "b", "c", "z", "+", "-", "h", "1", "2", "left", "right", "up", "down"].collect ({|item|
			// add dictionary entry
			~clutches[i].put(item, 0);
			// add a LeaWii item
			this.add(item,
			{|time, resp, msg|
				if (msg[1] == 1, {~clutches[i].put(item, 1)}, {~clutches[i].put(item, 0)}); // end if
			},
			i + 1; // note device number entry 
			);	
		});
		// same games for accelerometers
		~accels[i] = Dictionary.new(6);
		// add dictionary item and add the leaWii responderNodes
		["pitch", "roll", "yawl", "np", "nr", "ny"].collect ({|item|
			// add dictionary entry
			~accels[i].put(item, 0);
			// add a LeaWii item
			this.add(item,
			{|time, resp, msg|
				~accels[i].put(item, msg[1]);
			},
			i + 1; // note device number entry 
			);
		});

	}); // end 2.do
	

} // end init
add {|item, func, deviceNum, netAddr|
	var device;
	deviceNum = deviceNum ? 1;
	netAddr = netAddr ? NetAddr.new("127.0.0.1", 8000);
device = "/wii/" ++ deviceNum ++ "/";
		switch(item,
			"pitch", {item = device ++ "accel/pry/0"},
			"roll", {item = device ++ "accel/pry/1"},
			"yawl", {item = device ++ "accel/pry/2"},
			"accel", {item = device ++ "accel/pry/3"},
			"plus", {item = device ++ "button/" ++ "Plus"},
			"minus", {item = device ++ "button/" ++ "Minus"},
			"A", {item = device ++ "button/" ++ "A"},
			"B", {item = device ++ "button/" ++ "B"},
			"1", {item = device ++ "button/" ++ "1"},
			"2", {item = device ++ "button/" ++ "2"},
			"home", {item = device ++ "button/" ++ "Home"},
			"left", {item = device ++ "button/" ++ "Left"},
			"right",{item = device ++ "button/" ++ "Right"},
			"up",{item = device ++ "button/" ++ "Up"},
			"down", {item = device ++ "button/" ++ "Down"},
			"p", {item = device ++ "accel/pry/0"},

			"r", {item = device ++ "accel/pry/1"},
			"y", {item = device ++ "accel/pry/2"},
			"+", {item = device ++ "button/" ++ "Plus"},
			"-", {item = device ++ "button/" ++ "Minus"},
			"A", {item = device ++ "button/" ++ "A"},
			"B", {item = device ++ "button/" ++ "B"},
			"1", {item = device ++ "button/" ++ "1"},
			"2", {item = device ++ "button/" ++ "2"},
			"h", {item = device ++ "button/" ++ "Home"},
			"l", {item = device ++ "button/" ++ "Left"},
			// "r",{item = device ++ "button/" ++ "Right"},
			"u",{item = device ++ "button/" ++ "Up"},
			"d", {item = device ++ "button/" ++ "Down"},
			"c", {item = device ++ "nunchuk/button/" ++ "C"},
			"z", {item = device ++ "nunchuk/button/" ++ "Z"},
			"np", {item = device ++ "nunchuk/accel/pry/0"},
			"nr", {item = device ++ "nunchuk/accel/pry/1"},
			"ny", {item = device ++ "nunchuk/accel/pry/2"},
			"naccel", {item = device ++ "nunchuk/accel/pry/3"},
			"jx", {item = device ++ "nunchuk/joy/0"},
			"jy", {item = device ++ "nunchuk/joy/1"},
			"j", {item = device ++ "nunchuk/joy"},
			
			// android device
			"touch", {item = "/touch"},
			"t", {item = "/touch"},
			"ori", {item = "/ori"},
			"o", {item = "/ori"}
			
		); // end switch
		listSize = listSize + 1;
		listResponders.add(
			OSCresponderNode.new(netAddr.postln, item.postln, func).add;
			);
		// add the info necessary to recreate the responder at some future time
		listRespString.add([netAddr, item, func]);
}
renew {
	// netAddr = netAddr ? NetAddr.new("127.0.0.1", 8000);
	listSize.do ({|item, i|
		// "oin renew loop".postln;
		listResponders.add(
			OSCresponderNode.new(listRespString[i][0], listRespString[i][1], listRespString[i][2]).add
			);
	});
}
joy {|devNum|
	var ret="blank"; 
	devNum = devNum ? 1;
	//devNum.postln;
	x[0];
	y[0];
	if (x[devNum-1] > 0.60, {ret = "right"});
	if (x[devNum-1] < 0.4, {ret = "left"});
	if (y[devNum-1] > 0.60, {ret = "up"});
	if (y[devNum-1] < 0.4, {ret = "down"});
^ret
}
clear {
	listResponders.do(
			{arg item; item.remove}
		); // end do
		listResponders.size.postln;
	OSCresponder.initClass;
	listResponders = List.new;

}
} // end class LeaWii
