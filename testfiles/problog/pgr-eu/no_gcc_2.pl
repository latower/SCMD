line('35052', '66718').
line('26725', '60640').
line('4568', '66717').
line('26726', '60641').
line('60640', '60641').
line('66380', '32373').
line('4568', '26726').
line('60236', '66382').
line('50570', '50571').
line('66381', '26351').
line('32373', '60236').
line('66381', '66379').
line('26351', '60236').
line('32709', '26725').
line('32710', '66718').
line('50570', '26725').
line('60641', '26727').
line('60235', '66380').
line('26351', '32373').
line('66381', '26351').
line('26727', '32710').
line('32709', '60640').
line('66382', '26352').
line('66379', '32372').
line('32372', '60235').
line('32373', '66382').
line('66379', '32372').
line('34160', '26352').
line('26352', '50572').
line('66717', '32709').
line('26727', '32710').
line('60640', '26726').
line('66718', '32710').
line('60235', '26350').
line('26350', '66380').
line('50570', '50572').
line('26350', '66381').
line('66717', '4568').

plant('35052').

?::strengthen(X, Y) :- line(X, Y).

0.7916666666666667::power_line(X, Y) :- line(X, Y), strengthen(X, Y).
0.7916666666666667::power_line(X, Y) :- line(Y, X), strengthen(Y, X).
0.4::power_line(X, Y) :- line(X, Y).
0.4::power_line(X, Y) :- line(Y, X).

connection(X, Y) :- power_line(X, Y).
connection(X, Y) :- connection(X, Z), power_line(Z, Y).
connected(X) :- plant(Y), connection(X, Y).

query(connected('50570')).
query(connected('50572')).
query(connected('4568')).
query(connected('50571')).
