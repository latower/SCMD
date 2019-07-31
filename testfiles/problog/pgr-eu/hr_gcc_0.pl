line('54378', '55537').
line('55358', '49855').
line('55537', '1782').
line('54378', '20160').
line('54378', '29169').
line('21159', '20868').
line('55358', '54101').
line('29487', '5997').
line('29487', '55357').
line('22114', '56028').
line('55358', '40067').
line('44120', '29169').
line('29487', '40067').
line('55358', '55357').
line('49855', '20868').
line('55537', '54378').
line('63845', '63846').
line('49855', '20364').
line('21159', '9968').
line('44120', '20868').
line('63846', '40067').
line('20364', '22114').
line('54207', '20868').
line('54378', '55357').
line('20828', '55537').
line('54378', '56028').
line('54171', '54101').
line('35038', '20868').
line('63845', '63846').
line('54171', '54101').
line('54378', '20160').

station('9968').

?::strengthen(X, Y) :- line(X, Y).

0.7916666666666667::power_line(X, Y) :- line(X, Y), strengthen(X, Y).
0.7916666666666667::power_line(X, Y) :- line(Y, X), strengthen(Y, X).
0.4::power_line(X, Y) :- line(X, Y).
0.4::power_line(X, Y) :- line(Y, X).

connection(X, Y) :- power_line(X, Y).
connection(X, Y) :- connection(X, Z), power_line(Z, Y).
connected(X) :- station(Y), connection(X, Y).

query(connected('49855')).
query(connected('40067')).
query(connected('44120')).
query(connected('35038')).
query(connected('5997')).
query(connected('1782')).
