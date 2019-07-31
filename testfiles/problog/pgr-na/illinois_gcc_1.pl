line('29210', '933').
line('8080', '8149').
line('8080', '12818').
line('13317', '25309').
line('12959', '25327').
line('12903', '25309').
line('13018', '25328').
line('12718', '12961').
line('25310', '25327').
line('13375', '29210').
line('8080', '25716').
line('12902', '25310').
line('25715', '25723').
line('13428', '13430').
line('12717', '12718').
line('12903', '25723').
line('12959', '13144').
line('12717', '12818').
line('12960', '25328').
line('13018', '13315').
line('13144', '25310').
line('8149', '12717').
line('8151', '12717').
line('25715', '25716').
line('13428', '25723').
line('8149', '8151').
line('12718', '25327').
line('13315', '13375').
line('88', '25328').
line('13426', '25716').
line('12902', '25309').
line('13375', '25715').
line('12717', '12718').

station('8149').

?::strengthen(X, Y) :- line(X, Y).

0.7916666666666667::power_line(X, Y) :- line(X, Y), strengthen(X, Y).
0.7916666666666667::power_line(X, Y) :- line(Y, X), strengthen(Y, X).
0.4::power_line(X, Y) :- line(X, Y).
0.4::power_line(X, Y) :- line(Y, X).

connection(X, Y) :- power_line(X, Y).
connection(X, Y) :- connection(X, Z), power_line(Z, Y).
connected(X) :- station(Y), connection(X, Y).

query(connected('12717')).
query(connected('12718')).
query(connected('8080')).
query(connected('13375')).
query(connected('12959')).
query(connected('13144')).
query(connected('12903')).
query(connected('12818')).
query(connected('13315')).
query(connected('13428')).
query(connected('8151')).
query(connected('13018')).
query(connected('12902')).
query(connected('13317')).
query(connected('12960')).
query(connected('933')).
query(connected('12961')).
query(connected('88')).
query(connected('13426')).
query(connected('13430')).
