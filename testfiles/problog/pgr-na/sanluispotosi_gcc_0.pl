line('27262', '14426').
line('16248', '28981').
line('16258', '30703').
line('9749', '16244').
line('27255', '27256').
line('14430', '14454').
line('27254', '27257').
line('14426', '28981').
line('30702', '27262').
line('9749', '27257').
line('30702', '14426').
line('27253', '27254').
line('27255', '29054').
line('27256', '29401').
line('27251', '27252').
line('27253', '29054').
line('9749', '16243').
line('14426', '30703').
line('27258', '27259').
line('27257', '27259').
line('28981', '30702').
line('16257', '14426').
line('16248', '27259').
line('27252', '27253').
line('27251', '29054').
line('27262', '14426').
line('14426', '14430').
line('27252', '27258').
line('9749', '27256').
line('27254', '27255').
line('27262', '30703').

plant('14454').

?::strengthen(X, Y) :- line(X, Y).

0.7916666666666667::power_line(X, Y) :- line(X, Y), strengthen(X, Y).
0.7916666666666667::power_line(X, Y) :- line(Y, X), strengthen(Y, X).
0.4::power_line(X, Y) :- line(X, Y).
0.4::power_line(X, Y) :- line(Y, X).

connection(X, Y) :- power_line(X, Y).
connection(X, Y) :- connection(X, Z), power_line(Z, Y).
connected(X) :- plant(Y), connection(X, Y).

query(connected('14426')).
query(connected('9749')).
query(connected('14430')).
query(connected('16248')).
query(connected('16257')).
query(connected('16243')).
query(connected('16258')).
query(connected('16244')).
