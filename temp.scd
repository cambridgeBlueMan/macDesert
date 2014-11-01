
(
~win = Window.new.front;
	~wiidgets = List.new(0);
	4.do ({|i|
		~wiidgets.add;
		~wiidgets[i] = Slider.new(~win, bounds: Rect((100*i), 50, 80, 100)).front;
	});

)

then run:

Preset.new(~win,things:~wiidgets)
