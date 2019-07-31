line('13566', '13567').
line('7215', '7220').
line('7214', '28738').
line('7740', '28780').
line('7214', '28738').
line('48', '29421').
line('11930', '18334').
line('3146', '48').
line('564', '1844').
line('11570', '19369').
line('11564', '11570').
line('1842', '1843').
line('589', '7329').
line('18335', '29649').
line('608', '609').
line('589', '19360').
line('18328', '29649').
line('28738', '7213').
line('11572', '11573').
line('18328', '30317').
line('1714', '1738').
line('1844', '1842').
line('7223', '7219').
line('30317', '7223').
line('18331', '29421').
line('563', '7364').
line('589', '19369').
line('46', '48').
line('7215', '7219').
line('7740', '48').
line('46', '563').
line('18328', '29648').
line('14257', '19369').
line('13568', '13567').
line('11564', '11572').
line('564', '18331').
line('171', '563').
line('563', '7213').
line('29648', '7223').
line('7214', '28738').
line('609', '7364').
line('7747', '18334').
line('7214', '28738').
line('7213', '7220').
line('18335', '13568').
line('28780', '18334').
line('1714', '14257').
line('7213', '29421').
line('29648', '29649').
line('563', '19360').
line('18335', '30317').
line('1844', '1845').
line('609', '19360').
line('7223', '28738').
line('7747', '28780').
line('171', '7329').
line('18331', '29421').
line('1844', '1846').
line('7223', '29649').

station('608').
station('11570').
station('1844').
station('7329').
station('1842').
station('1843').
station('11572').
station('563').
station('1738').
station('11930').
station('1846').
station('11573').
station('46').
station('11564').
station('3146').
station('564').
station('609').
station('1714').

?::strengthen(X, Y) :- line(X, Y).

0.7916666666666667::power_line(X, Y) :- line(X, Y), strengthen(X, Y).
0.7916666666666667::power_line(X, Y) :- line(Y, X), strengthen(Y, X).
0.4::power_line(X, Y) :- line(X, Y).
0.4::power_line(X, Y) :- line(Y, X).

connection(X, Y) :- power_line(X, Y).
connection(X, Y) :- connection(X, Z), power_line(Z, Y).
connected(X) :- station(Y), connection(X, Y).

query(connected('7223')).
query(connected('7213')).
query(connected('48')).
query(connected('589')).
query(connected('7215')).
query(connected('7219')).
query(connected('7220')).
query(connected('13568')).
query(connected('14257')).
query(connected('7364')).
query(connected('7740')).
query(connected('13567')).
query(connected('7747')).
query(connected('171')).
query(connected('13566')).
query(connected('7214')).
query(connected('1845')).
