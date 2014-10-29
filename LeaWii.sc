LeaWii {
	classvar listResponders, aString;

*new {|netAddr|
	^super.new.init(netAddr)
}
init {|netAddr|
	listResponders = List.new;
	netAddr = netAddr ? NetAddr.new("127.0.0.1", 8000);
	CmdPeriod.add({
	aString = "responders released, we hope!";
		listResponders.do(
			{arg item; item.remove}
		); // end do
		// aString.postln;
		}; // end function
		// added 29/6/2010
		// OSCresponder.initClass;
	) // end CmdPeriod
} // end init
add {|item, func, deviceNum=1, netAddr|
var device = "/wii/" ++ deviceNum ++ "/";
		switch(item,
			"pitch", {item = device ++ "accel/pry/0"},
			"roll", {item = device ++ "accel/pry/1"},
			"yawl", {item = device ++ "accel/pry/2"},
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
			"jx", {item = device ++ "nunchuk/joy/0"},
			"jy", {item = device ++ "nunchuk/joy/1"},
			"j", {item = device ++ "nunchuk/joy"}
			
		); // end switch
		listResponders.add(
			OSCresponderNode.new(netAddr, item, func).add;
			);
}
clear {
	listResponders.do(
			{arg item; item.remove}
		); // end do
}
} // end class LeaWii
