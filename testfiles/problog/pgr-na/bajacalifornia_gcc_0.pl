line('1056', '19648').
line('15529', '19650').
line('19753', '29035').
line('19648', '29035').
line('29035', '1053').
line('1049', '15526').
line('1057', '20478').
line('1050', '19650').
line('1049', '19650').
line('1070', '19753').
line('15524', '15526').
line('1049', '1055').
line('1051', '19651').
line('1049', '19648').
line('1180', '15528').
line('19649', '1052').
line('1049', '20478').
line('19649', '29035').
line('19649', '19651').
line('15528', '20478').
line('1049', '15527').
line('1056', '19651').
line('1051', '29035').

station('1052').
station('1051').
station('1053').
station('1056').
station('1070').

?::strengthen(X, Y) :- line(X, Y).

0.7916666666666667::power_line(X, Y) :- line(X, Y), strengthen(X, Y).
0.7916666666666667::power_line(X, Y) :- line(Y, X), strengthen(Y, X).
0.4::power_line(X, Y) :- line(X, Y).
0.4::power_line(X, Y) :- line(Y, X).

connection(X, Y) :- power_line(X, Y).
connection(X, Y) :- connection(X, Z), power_line(Z, Y).
connected(X) :- station(Y), connection(X, Y).

query(connected('1049')).
query(connected('15526')).
query(connected('15528')).
query(connected('1050')).
query(connected('1055')).
query(connected('1180')).
query(connected('15529')).
query(connected('1057')).
query(connected('15524')).
query(connected('15527')).
