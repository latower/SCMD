line('27248', '16235').
line('4446', '21522').
line('16214', '27216').
line('16235', '27282').
line('16287', '27282').
line('27329', '28496').
line('4441', '27248').
line('27326', '28496').
line('21522', '28496').
line('16377', '27351').
line('15652', '16287').
line('27325', '27326').
line('27282', '27283').
line('27215', '27247').
line('16345', '27351').
line('4444', '4445').
line('27281', '28496').
line('16235', '27281').
line('21521', '28496').
line('16301', '27283').
line('11586', '16287').
line('4446', '21521').
line('21519', '4445').
line('16287', '27283').
line('11586', '15652').
line('27248', '27249').
line('16361', '27329').
line('11586', '27326').
line('27325', '28496').
line('16345', '27329').
line('27328', '27325').
line('11586', '28496').
line('27215', '27249').
line('21519', '28496').
line('27328', '27329').
line('4445', '28496').
line('4441', '4444').
line('27247', '27249').
line('16214', '27215').
line('16378', '27351').
line('27216', '27248').
line('4445', '28496').
line('16361', '27328').
line('11586', '27281').

station('4441').
station('4444').

?::strengthen(X, Y) :- line(X, Y).

0.7916666666666667::power_line(X, Y) :- line(X, Y), strengthen(X, Y).
0.7916666666666667::power_line(X, Y) :- line(Y, X), strengthen(Y, X).
0.4::power_line(X, Y) :- line(X, Y).
0.4::power_line(X, Y) :- line(Y, X).

connection(X, Y) :- power_line(X, Y).
connection(X, Y) :- connection(X, Z), power_line(Z, Y).
connected(X) :- station(Y), connection(X, Y).

query(connected('11586')).
query(connected('16287')).
query(connected('4445')).
query(connected('16235')).
query(connected('16345')).
query(connected('4446')).
query(connected('15652')).
query(connected('16214')).
query(connected('16361')).
query(connected('16378')).
query(connected('16301')).
query(connected('16377')).
