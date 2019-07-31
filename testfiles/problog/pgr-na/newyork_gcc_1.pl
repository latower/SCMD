line('8974', '28121').
line('8973', '8974').
line('18974', '18975').
line('8974', '15675').
line('12601', '25157').
line('18974', '18975').
line('7863', '15674').
line('18975', '30113').
line('5987', '7863').
line('5372', '15675').
line('5547', '22031').
line('5372', '5374').
line('8974', '25157').
line('8973', '8974').
line('22031', '25157').
line('25156', '28086').
line('12585', '25156').
line('18974', '30113').
line('5373', '28121').
line('7863', '25156').
line('12601', '28121').
line('22031', '15674').
line('7863', '15675').
line('5547', '5548').
line('7863', '30113').

station('8973').

?::strengthen(X, Y) :- line(X, Y).

0.7916666666666667::power_line(X, Y) :- line(X, Y), strengthen(X, Y).
0.7916666666666667::power_line(X, Y) :- line(Y, X), strengthen(Y, X).
0.4::power_line(X, Y) :- line(X, Y).
0.4::power_line(X, Y) :- line(Y, X).

connection(X, Y) :- power_line(X, Y).
connection(X, Y) :- connection(X, Z), power_line(Z, Y).
connected(X) :- station(Y), connection(X, Y).

query(connected('7863')).
query(connected('8974')).
query(connected('15675')).
query(connected('5547')).
query(connected('12601')).
query(connected('5372')).
query(connected('15674')).
query(connected('5987')).
query(connected('5548')).
query(connected('12585')).
query(connected('5374')).
query(connected('5373')).
