line('5307', '30911').
line('6271', '22377').
line('18539', '5957').
line('17214', '18537').
line('18527', '18529').
line('6262', '18572').
line('18537', '13803').
line('5957', '5755').
line('6262', '18539').
line('18538', '18540').
line('18574', '18575').
line('6271', '18572').
line('6272', '22377').
line('5370', '30911').
line('14170', '18537').
line('18538', '18576').
line('18573', '18575').
line('5755', '18540').
line('18573', '22377').
line('18527', '18529').
line('18575', '18576').
line('18571', '18574').
line('5307', '18572').
line('18528', '18527').
line('18528', '18571').
line('14170', '18576').
line('18539', '18573').
line('18528', '17214').
line('18528', '18529').
line('5307', '30911').
line('18571', '18574').

station('5307').
station('5755').
station('6271').
station('6272').
station('6262').
station('5370').

?::strengthen(X, Y) :- line(X, Y).

0.7916666666666667::power_line(X, Y) :- line(X, Y), strengthen(X, Y).
0.7916666666666667::power_line(X, Y) :- line(Y, X), strengthen(Y, X).
0.4::power_line(X, Y) :- line(X, Y).
0.4::power_line(X, Y) :- line(Y, X).

connection(X, Y) :- power_line(X, Y).
connection(X, Y) :- connection(X, Z), power_line(Z, Y).
connected(X) :- station(Y), connection(X, Y).

query(connected('17214')).
query(connected('5957')).
query(connected('14170')).
query(connected('13803')).
