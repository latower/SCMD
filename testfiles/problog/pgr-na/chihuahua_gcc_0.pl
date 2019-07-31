line('25950', '31243').
line('25949', '31243').
line('14811', '25949').
line('21573', '21576').
line('21574', '4570').
line('4563', '21578').
line('4570', '4573').
line('28511', '29806').
line('4569', '31243').
line('21575', '21576').
line('13643', '28511').
line('21576', '21578').
line('4569', '28511').
line('4567', '21577').
line('4563', '31243').
line('4563', '21577').
line('21579', '29806').
line('4567', '4569').
line('4563', '29805').
line('21573', '21574').
line('21573', '21575').
line('21579', '29805').
line('4569', '21579').
line('14811', '16177').
line('29805', '29806').
line('14811', '25949').
line('4563', '31243').
line('14811', '25950').
line('13807', '13643').
line('4567', '21575').
line('21576', '21577').
line('4572', '21574').
line('4568', '21578').
line('4573', '13882').

station('4568').
station('4572').
station('4567').
station('4563').
station('4569').
station('4573').

?::strengthen(X, Y) :- line(X, Y).

0.7916666666666667::power_line(X, Y) :- line(X, Y), strengthen(X, Y).
0.7916666666666667::power_line(X, Y) :- line(Y, X), strengthen(Y, X).
0.4::power_line(X, Y) :- line(X, Y).
0.4::power_line(X, Y) :- line(Y, X).

connection(X, Y) :- power_line(X, Y).
connection(X, Y) :- connection(X, Z), power_line(Z, Y).
connected(X) :- station(Y), connection(X, Y).

query(connected('14811')).
query(connected('13643')).
query(connected('4570')).
query(connected('13882')).
query(connected('13807')).
query(connected('16177')).
