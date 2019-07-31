# Problems and input files
We describe the problem settings and datasets (and their licenses) underlying the test files in this directory.

## Problem descriptions
We give short descriptions of the underlying problems, and provide examples of how these are encoded in [ProbLog](https://bitbucket.org/problog/problog/src/).

### Viral Marketing
We are given a social network where nodes represent people and edges represent influence relationships. A weigth `0 < w_ij < 1` on edge `(i,j)` indicates that person `i` has a probability of `w_ij` to influence person `j`.

The goal is to use Viral Marketing (or Spread of Influence) to turn as many people in the network into buyers of our product. To this end, we have `k` free samples of the product to distribute among people in the network. When they receive the free sample, we hope they will love the product, be buy the product in the future, and will convince their friends and acquaintances to also buy the product. 

Given a maximum of `k` free samples to hand out, we aim to maximize the expected number of people that are turned into product-buyers. We model this as follows:

```
% Deterministic facts
person(a).
person(b).
person(c).
person(e).

% Probabilistic facts
0.4::trusts_directed(a,b).
0.8::trusts_directed(a,c).
0.1::trusts_directed(b,c).
0.3::trusts_directed(c,e).

0.2::buy_from_marketing(_).

% Decisions
?::marketed(P) :- person(P).

% Rules and relations
trusts(X,Y) :- trusts_directed(X,Y).
trusts(X,Y) :- trusts_directed(Y,X).

buys(X) :- marketed(X), buy_from_marketing(X).
buys(X) :- trusts(X,Y), buys(Y).

% Queries
query(buys(a)).
query(buys(b)).
query(buys(c)).
query(buys(e)).
```

This problem is inspired by:
> M. E. J. Newman, The structure of scientific collaboration networks,
  Proc. Natl. Acad. Sci. USA 98, 404-409 (2001).

<!-- ### Frequent Itemset Mining
**TODO**
TODO: fill in -->

### Signalling Regulatory Pathways
Given a network `G(V,E)` of probabilistic protein-gene interactions `E`, a set of interesting (protein, gene) pairs `Phi`, and a maximum number of edges `k`, find the `k`-sized subset `V*` of `V` that best explains the interactions between the proteins and genes in the pairs in `Phi`.

We do this by maximizing the expected number of pairs in `Phi` for which there is a path from the protein to the gene in the subgraph that is induced by `V*`.

We encode this as follows in ProbLog:

```
% Probabilistic Facts
0.208486::pp_edge('p1','p2').
0.421846::pp_edge('p1','p3').

0.96::pd_edge('p1','g2').
0.96::pd_edge('p3','g2').
0.96::pd_edge('p2','g1').
0.96::pd_edge('p2','g2').

% Decisions
?::pp_dec('p1','p2').
?::pp_dec('p1','p3').

?::pd_dec('p1','g2').
?::pd_dec('p3','g2').
?::pd_dec('p2','g1').
?::pd_dec('p2','g2').

% Rules and Relations
e(X,Y) :- pd_edge(X,Y), pd_dec(X,Y).
e(X,Y) :- pp_edge(X,Y), pp_dec(X,Y).
e(Y,X) :- pp_edge(X,Y), pp_dec(X,Y).

path(X,Y) :-
	pd_edge(X,Y), pd_dec(X,Y).
path(X,Y) :-
	X \= Y,
	e(X,Z),
	path(Z,Y).

% Phi queries
query(path('p1','g1')).
query(path('p1','g2')).
```
This problem was formulated by
> L. De Raedt, K. Kersting, A. Kimmig, K. Revoredo and H. Toivonen. Compressing probabilistic Prolog programs. Machine learning, 70:2-3, pp. 151 - 168, Kluwer Academic Publishers, 2008.



<!-- TODO: fill in -->

### Power Transmission Grid Reliability
We are given a network of power plants, power stations, substations, merges and joints. Power plants and power stations are _power producers_, power stations distribute this power to local households and other buildings and are therefore considered _power consumers_, and the joints and merger nodes are considered minor grid nodes that just relay the power.

In the event of a natural disaster (like an earthquake or hurricane), each powerline has a certain probability of breaking. By spending money to strenghtening a powerline, we can increase the probability that it will survive a disaster.

We have a limited budget for strengthening power lines. Which lines do we spend it on such that we maximize the expected number of power consumers that are still connected to a power producer after a natural disaster?

We model this in ProbLog as follows:

```
line(a,b).
line(a,c).
line(a,d).
line(b,c).
line(b,e).
line(c,d).

station(a).
plant(c).

?::strengthen(X, Y) :- line(X, Y).

0.7916666666666667::power_line(X, Y) :- line(X, Y), strengthen(X, Y).
0.7916666666666667::power_line(X, Y) :- line(Y, X), strengthen(Y, X).
0.4::power_line(X, Y) :- line(X, Y).
0.4::power_line(X, Y) :- line(Y, X).

connection(X, Y) :- power_line(X, Y).
connection(X, Y) :- connection(X, Z), power_line(Z, Y).
connected(X) :- plant(Y), connection(X, Y).
connected(X) :- station(Y), connection(X, Y).

query(connected(d)).
query(connected(e)).
```
This problem is inspired by:
> Leonardo Duenas-Osorio, Kuldeep S. Meel, Roger Paredes, Moshe Y. Vardi, Counting-based Reliability Estimation for Power-Transmission Grids, AAAI 2017

## Data set descriptions

### High Energy Theory collaboration network
For our Viral Marketing problems, we use communities (`hep-th-47` and `hep-th-5`) that are extracted from the [High Energy Theory Collaboration Network](http://networkdata.ics.uci.edu/data/hep-th/).
> M. E. J. Newman, The structure of scientific collaboration networks,
  Proc. Natl. Acad. Sci. USA 98, 404-409 (2001).

<!-- ### ArnetMiner
**TODO** -->
<!-- TODO: fill in -->

### SPINE
For our Signalling Regulatory Pathways problems (`spine27a`/`spine27_pos`, `spine27b`/`spine27_neg`, and `spine16`), we use communities extracted from the [SPINE dataset](http://cs.tau.ac.il/~roded/SPINE.html). 
> Oved Ourfali, Tomer Shlomi, Trey Ideker, Eytan Ruppin and Roded Sharan, SPINE: A Framework for Signaling-Regulatory Pathway Inference from Cause-Effect Experiments.


### GridKit
For our Power Transmission Grid Reliablity problems, we use state- and country-based connected components extracted from [GridKit](https://zenodo.org/record/47317#.XUBJIFyZZhH).
> Wiegmans, B. (2016). GridKit: European and North-American extracts [Data set]. Zenodo. http://doi.org/10.5281/zenodo.47317

These data are licensed under the Open Database License, version 1.0, see [./LICENSE_GridKit](https://github.com/latower/SCMD/blob/master/LICENSE_GridKit)). 

## Input files
In directory [./SCMD/testfiles/problog/](https://github.com/latower/SCMD/blob/master/testfiles/problog)) we provide the ProbLog files used for encoding the problems. 

We provide minimized OBDDs that encode the underlying probability distributions in directory [./SCMD/testfiles/obdds/minimized](https://github.com/latower/SCMD/blob/master/testfiles/obdds/minimized), and those that are not minimized in [./SCMD/testfiles/obdds/big](https://github.com/latower/SCMD/blob/master/testfiles/obdds/big),.
