line('12538', '23654').
line('12538', '25109').
line('23357', '8937').
line('12580', '28243').
line('30588', '25109').
line('17730', '23356').
line('12571', '25130').
line('25130', '30588').
line('8957', '12535').
line('23356', '25130').
line('12539', '23654').
line('29154', '18783').
line('18783', '12538').
line('8954', '25109').
line('29154', '28243').
line('8957', '9626').
line('17730', '23358').
line('9609', '25145').
line('25130', '30588').
line('8957', '12532').
line('23358', '28243').
line('6786', '12580').
line('12553', '23356').
line('23357', '23358').
line('23357', '28243').
line('8957', '23654').
line('25145', '28243').
line('6786', '25145').

station('12539').
station('8957').
station('12538').
station('8937').
station('12532').
station('12553').
station('12571').
station('12535').
station('12580').

?::strengthen(X, Y) :- line(X, Y).

0.7916666666666667::power_line(X, Y) :- line(X, Y), strengthen(X, Y).
0.7916666666666667::power_line(X, Y) :- line(Y, X), strengthen(Y, X).
0.4::power_line(X, Y) :- line(X, Y).
0.4::power_line(X, Y) :- line(Y, X).

connection(X, Y) :- power_line(X, Y).
connection(X, Y) :- connection(X, Z), power_line(Z, Y).
connected(X) :- station(Y), connection(X, Y).

query(connected('6786')).
query(connected('17730')).
query(connected('9609')).
query(connected('8954')).
query(connected('9626')).
