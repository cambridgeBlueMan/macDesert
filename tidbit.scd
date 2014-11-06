~setters["startDur"]
x = 12;
x.addDependant(~setters["startDur"])
x.changed(\startDur, 0.5);