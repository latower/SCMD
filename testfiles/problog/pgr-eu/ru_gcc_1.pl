line('10701', '13888').
line('23254', '22235').
line('57691', '22240').
line('67890', '29271').
line('57687', '15887').
line('62433', '22240').
line('29271', '67891').
line('11227', '15887').
line('23254', '15887').
line('51864', '11227').
line('10100', '53704').
line('9830', '33640').
line('57691', '22240').
line('38970', '32882').
line('22240', '47110').
line('44565', '55748').
line('57687', '15887').
line('6288', '64433').
line('10100', '13888').
line('10701', '23938').
line('39372', '67738').
line('22235', '6288').
line('11094', '25796').
line('65586', '8581').
line('44210', '23938').
line('39372', '67738').
line('13888', '57687').
line('9830', '29271').
line('67891', '64433').
line('10100', '13888').
line('10100', '10701').
line('33640', '9830').
line('32882', '26957').
line('55748', '8581').
line('10100', '10701').
line('65586', '25796').
line('9830', '67890').
line('10100', '23938').
line('44083', '53704').
line('23254', '26957').
line('13322', '55748').
line('67890', '39372').
line('10701', '13888').
line('10100', '13888').
line('32882', '26957').
line('13888', '57691').
line('67891', '64433').
line('44210', '45102').
line('10100', '13888').
line('62433', '11094').

station('39372').
station('6288').
plant('45102').

?::strengthen(X, Y) :- line(X, Y).

0.7916666666666667::power_line(X, Y) :- line(X, Y), strengthen(X, Y).
0.7916666666666667::power_line(X, Y) :- line(Y, X), strengthen(Y, X).
0.4::power_line(X, Y) :- line(X, Y).
0.4::power_line(X, Y) :- line(Y, X).

connection(X, Y) :- power_line(X, Y).
connection(X, Y) :- connection(X, Z), power_line(Z, Y).
connected(X) :- plant(Y), connection(X, Y).
connected(X) :- station(Y), connection(X, Y).

query(connected('13888')).
query(connected('10100')).
query(connected('10701')).
query(connected('15887')).
query(connected('9830')).
query(connected('11094')).
query(connected('44210')).
query(connected('8581')).
query(connected('53704')).
query(connected('11227')).
query(connected('38970')).
query(connected('51864')).
query(connected('44083')).
query(connected('44565')).
query(connected('13322')).
query(connected('47110')).
