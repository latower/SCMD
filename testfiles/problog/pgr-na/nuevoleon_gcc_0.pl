line('28517', '4610').
line('28517', '14453').
line('4617', '30341').
line('26249', '28517').
line('21597', '28517').
line('4612', '30341').
line('4610', '21595').
line('4614', '4615').
line('14507', '21595').
line('4615', '21599').
line('14507', '21595').
line('4615', '21598').
line('26256', '30225').
line('26257', '30225').
line('4608', '4610').
line('4614', '21598').
line('28517', '29807').
line('14491', '26256').
line('6957', '14482').
line('21597', '29807').
line('21600', '29277').
line('21600', '21599').
line('4612', '26257').
line('4609', '4610').
line('28517', '14453').
line('4610', '4612').
line('28517', '29807').
line('4610', '26249').
line('14482', '28517').
line('14491', '26249').
line('26256', '26257').
line('4613', '30341').
line('4616', '21599').
line('21597', '21598').
line('4613', '21600').

station('4617').
station('4614').
station('4613').
station('4616').

?::strengthen(X, Y) :- line(X, Y).

0.7916666666666667::power_line(X, Y) :- line(X, Y), strengthen(X, Y).
0.7916666666666667::power_line(X, Y) :- line(Y, X), strengthen(Y, X).
0.4::power_line(X, Y) :- line(X, Y).
0.4::power_line(X, Y) :- line(Y, X).

connection(X, Y) :- power_line(X, Y).
connection(X, Y) :- connection(X, Z), power_line(Z, Y).
connected(X) :- station(Y), connection(X, Y).

query(connected('4610')).
query(connected('4612')).
query(connected('4615')).
query(connected('14482')).
query(connected('14491')).
query(connected('14507')).
query(connected('6957')).
query(connected('4609')).
query(connected('14453')).
query(connected('4608')).
