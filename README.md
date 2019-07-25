# SCMD-propagator
Propagation algorithm for Stochastic Constraints on Monotonic Distributions (SCMDs), as described in: _Stochastic Constraint Propagation for Mining Probabilistic Networks_, Anna Louise D. Latour, Behrouz Babaki, and Siegfried Nijssen, to appear at IJCAI 2019, Macao.

## Contents of this repository
In this repository we provide:
- the `Scala` implementation of our SCMD propagation algorithm
- the files needed to convert ProbLog programs into OBDDs that serve as input for the SCMD propagation algorithm
- figures with results as presented in our paper, and additional results

## Prerequisites and dependencies
You need the following pieces of software for building and running the code in this repository:
- [Java openjdk 11.0.3 2019-04-16](https://wiki.openjdk.java.net/display/JDKUpdates/Archived+Releases)
- [sbt-naive-packager](https://github.com/sbt/sbt-native-packager) for building the SCMD propagator files
- [Scala 2.12](https://github.com/scala/scala/releases/tag/v2.12.4)
- [CoverSize](https://sites.uclouvain.be/cp4dm/fim/) constraint for FIM problem setting
- Python 3.6
- [dd 0.5.4](https://pypi.org/project/dd/) for OBDD compilation

TODO

## Building
In order to build the binaries for the SCMD propagator, go to the ``SCMD-propagator`` subdirectory and run
```
$ sbt pack
```

## Usage
We provide scripts for generating OBDDs from ProbLog files and have varying support for the different versions of our algorithm.

### From ProbLog to OBDD
Example ProbLog program (example from paper):
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

TODO

### Sub-linear SCMD propagator
For the _sub-linear_ propagator we support the following problem settings:
- ME: maximize expected value with constraint on the cardinality of the solution (positive decisions)
- MC: maximize cardinality of the solution (positive decisions) with constraint on the maximum expected value
- FIM: Frequent Itemset Mining

and the following branching heuristics:
- top-zero (default)
- top-one
- derivative-zero
- derivative-one
- bottom-zero
- bottom-one

We also support the collection of the search trace in a trace file and toggle detailed output during the search with the `-v` or `--verbose` flag.

To use our SCMD propagator to solve an `ME` problem setting for which the probability distribution is encoded in `[OBDD_file]`, with upper bound on the cardinality of the positive decisions `[constraint_threshold]`, and branching heuristic `[heuristic]`, writing the trace to `[trace_file]` and printing the search details to the terminal, run:
```
$ ./SCMD-propagator/target/pack/bin/run ME --bdd-file [OBDD_file] --max-card [constraint_threshold] --branching [heuristic] --trace-file [trace_file] -v
```
We have less support for the `MC` problem setting: 
```
$ ./SCMD-propagator/target/pack/bin/run MC [OBDD_file] [constraint_threshold]
```
For the `FIM` problem setting we support the same branching heuristics, tracing and verbose functionality as for the `ME` problem setting. However, because of the nature of the frequent itemset mining problem, we do not have a cardinality constraint on the positive decisions, but we do need to specify a database with transactions `[db_file]`, a minimum required expected value `[minexp]` and a minimum support `[minsup]`, e.g.:
```
$ ./SCMD-propagator/target/pack/bin/run FIM --bdd-file [OBDD_file] --db-file [db_file] --min-exp [minexp] --min-sup [minsup] --branching [heuristic] --trace-file [trace_file] -v
```

### Linear SCMD propagator
For the _linear_ propagator we support only the `ME` problem setting and only the default branching, and no tracing or verbosity:
```
$ ./SCMD-propagator/target/pack/bin/runner-max-prob-sub-linear [OBDD_File] [constraint_threshold]
```

## Test data
TODO

## License
TODO

## Contributors
The code in this repository is written by 
- Behrouz Babaki (@Behrouz-Babaki)
- Anna Louise Latour (@latower)
- Siegfried Nijssen (@siegfriednijssen)