line('13322', '13394').
line('26593', '4090').
line('4637', '16631').
line('4087', '4090').
line('24441', '24442').
line('27459', '27460').
line('21371', '3259').
line('3259', '11883').
line('16119', '16567').
line('4090', '11907').
line('4637', '24442').
line('13394', '21371').
line('4637', '16567').
line('24442', '27460').
line('16563', '27459').
line('13322', '24441').
line('4087', '21371').
line('4087', '4090').
line('24441', '27460').
line('4087', '3259').
line('4090', '26593').
line('16564', '24441').
line('10130', '15259').
line('15259', '26593').

station('3259').

?::strengthen(X, Y) :- line(X, Y).

0.7916666666666667::power_line(X, Y) :- line(X, Y), strengthen(X, Y).
0.7916666666666667::power_line(X, Y) :- line(Y, X), strengthen(Y, X).
0.4::power_line(X, Y) :- line(X, Y).
0.4::power_line(X, Y) :- line(Y, X).

connection(X, Y) :- power_line(X, Y).
connection(X, Y) :- connection(X, Z), power_line(Z, Y).
connected(X) :- station(Y), connection(X, Y).

query(connected('4637')).
query(connected('4087')).
query(connected('4090')).
query(connected('13322')).
query(connected('15259')).
query(connected('13394')).
query(connected('16567')).
query(connected('11907')).
query(connected('16631')).
query(connected('11883')).
query(connected('16564')).
query(connected('16563')).
query(connected('10130')).
query(connected('16119')).
