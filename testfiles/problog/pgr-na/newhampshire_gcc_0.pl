line('29448', '2011').
line('959', '2124').
line('2085', '29499').
line('1993', '2049').
line('13674', '25867').
line('13675', '13676').
line('5269', '5500').
line('29385', '2047').
line('18860', '18859').
line('960', '961').
line('29448', '1993').
line('1725', '1993').
line('29448', '2011').
line('29448', '2011').
line('5269', '5500').
line('2144', '2145').
line('29448', '3430').
line('2124', '4779').
line('29466', '2047').
line('29718', '29719').
line('13673', '18858').
line('2085', '959').
line('16846', '18861').
line('5268', '21900').
line('18858', '18861').
line('5507', '5268').
line('29448', '29499').
line('5507', '5268').
line('959', '961').
line('956', '960').
line('5513', '18044').
line('959', '2124').
line('29448', '1993').
line('1993', '29718').
line('2144', '4779').
line('25867', '30654').
line('5268', '5269').
line('3430', '18044').
line('13674', '13675').
line('1993', '18860').
line('1993', '29718').
line('13675', '13676').
line('18859', '18860').
line('1993', '2049').
line('18859', '18858').
line('2144', '29552').
line('5269', '21900').
line('2144', '29552').
line('13675', '13676').
line('5507', '5513').
line('959', '29499').
line('29466', '2011').
line('13673', '30654').
line('30654', '25867').

plant('4779').

?::strengthen(X, Y) :- line(X, Y).

0.7916666666666667::power_line(X, Y) :- line(X, Y), strengthen(X, Y).
0.7916666666666667::power_line(X, Y) :- line(Y, X), strengthen(Y, X).
0.4::power_line(X, Y) :- line(X, Y).
0.4::power_line(X, Y) :- line(Y, X).

connection(X, Y) :- power_line(X, Y).
connection(X, Y) :- connection(X, Z), power_line(Z, Y).
connected(X) :- plant(Y), connection(X, Y).

query(connected('1993')).
query(connected('959')).
query(connected('2144')).
query(connected('5269')).
query(connected('5268')).
query(connected('2047')).
query(connected('961')).
query(connected('5507')).
query(connected('2011')).
query(connected('2124')).
query(connected('3430')).
query(connected('13674')).
query(connected('960')).
query(connected('13675')).
query(connected('13673')).
query(connected('2085')).
query(connected('5513')).
query(connected('5500')).
query(connected('956')).
query(connected('16846')).
query(connected('2145')).
query(connected('1725')).
query(connected('13676')).
query(connected('2049')).
