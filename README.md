# SCMD-propagator
Propagation algorithm for Stochastic Constraints on Monotonic Distributions (SCMDs), as described in: _Stochastic Constraint Propagation for Mining Probabilistic Networks_, Anna Louise D. Latour, Behrouz Babaki, and Siegfried Nijssen, to appear at IJCAI 2019, Macao.

## version: 1.3

Compared to version 1.2 we have

- fixed a bug that occurred in a Frequent Itemset Mining setting;
- made the solver configurable.

## Contents of this repository
In this repository we provide:
- the `Scala` implementation of our SCMD propagation algorithm;
- figures with results as presented in our paper, and additional results;
- some of the test files needed to reproduce our results (based on license/availability).

## Prerequisites and dependencies
You need the following pieces of software for building and running the code in this repository:
- [Java openjdk 11.0.3 2019-04-16](https://wiki.openjdk.java.net/display/JDKUpdates/Archived+Releases)

- [sbt-naive-packager](https://github.com/sbt/sbt-native-packager) for building the SCMD propagator files

- [Scala 2.12](https://github.com/scala/scala/releases/tag/v2.12.4)

  <!---

- [CoverSize](https://sites.uclouvain.be/cp4dm/fim/) constraint for FIM problem setting 

  --->

## Building
In order to build the binaries for the SCMD propagator, go to the ``SCMD-propagator`` subdirectory and run
```
$ sbt pack
```

## Usage

We support the following problem setting:

ME: maximize expected value with constraint on the cardinality of the solution (positive decisions)
<!--- - MC: maximize cardinality of the solution (positive decisions) with constraint on the maximum expected value --->

and the following branching heuristics:

- Branching heuristics that do _not_ require pre-processing:
  - top-zero (default)
  - top-one
  - derivative-zero
  - derivative-one
  - bottom-zero
  - bottom-one
  - random-zero
  - random-one
  - binary-last-conflict
  - conflict-ordering-search

- Branching heuristics that require pre-processing:
  - degree-zero
  - degree-one
  - influence-zero
  - influence-one
  - betweenness-zero
  - betweenness-one
  - local-sim-zero
  - local-sim-one
  - quadrangle-zero
  - quadrangle-one
  - triangle-zero
  - triangle-one
  - forest-fire-zero
  - forest-fire-one

We also support the collection of the search trace in a trace file and toggle detailed output during the search with the `-v` or `--verbose` flag.

To use our partial-sweep SCMD propagator to solve an ME problem setting for which the probability distribution is encoded in `[OBDD_file]`, with upper bound on the cardinality of the positive decisions `[constraint_threshold]`, and branching heuristic `[heuristic]` and printing the search details to the terminal, run:
```
$ ./SCMD-propagator/target/pack/bin/run ME-P --bdd-file [OBDD_file] --max-card [constraint_threshold] --branching [heuristic] --heuristic-file [heuristic_file] --verbose
```
Here, [heuristic_file] needs to be obtained through preprocessing. It contains the decision variables in a problem, sorted according to a value (i.e. degree or betweenness) in the order in which we want to branch on those decision variables, one decision variable per line.

<!--- 
We have less support for the `MC` problem setting: 

```
$ ./SCMD-propagator/target/pack/bin/run MC-P [OBDD_file] [constraint_threshold]
```

For the `FIM` problem setting we support the same branching heuristics, tracing and verbose functionality as for the `ME` problem setting. However, because of the nature of the frequent itemset mining problem, we do not have a cardinality constraint on the positive decisions, but we do need to specify a database with transactions `[db_file]`, a minimum required expected value `[minexp]` and a minimum support `[minsup]`, e.g.:
```
$ ./SCMD-propagator/target/pack/bin/run FIM --bdd-file [OBDD_file] --db-file [db_file] --min-exp [minexp] --min-sup [minsup] --branching [heuristic] --trace-file [trace_file] -v
```


### Full-sweep SCMD propagator
For the _full-sweep_ propagator we support only the `ME` problem setting:
```
$ ./SCMD-propagator/target/pack/bin/run ME-F --bdd-file [OBDD_File] --max-card [constraint_threshold] --verbose
```

--->

## More information

Please contact us if you are looking for the following files:
- scripts for generating OBDDs from ProbLog programs;
- code for generating GAC-guaranteeing MIP encoding of Stochastic Constraint Optimisation Problems (SCOPs) and Stochastic Constraint Problems on Monotonic Distributions (SCMDs);
- benchmarking scripts.

## License
The propagation algorithm for the Stochastic Constraint on Monotonic Distributions (SCMD) in [./SCMD-propagator/src/main/scala/](https://github.com/latower/SCMD/blob/master/SCMD-propagator/src/main/scala/) is licensed under the [MIT license](https://github.com/latower/SCMD/blob/master/LICENSE).

We provide `ProbLog` and `OBDD` files for power transmission network models in:
- [./testfiles/problog/pgr-eu](https://github.com/latower/SCMD/blob/master/testfiles/problog/pgr-eu),
- [./testfiles/problog/pgr-na](https://github.com/latower/SCMD/blob/master/testfiles/problog/pgr-na),
- [./testfiles/obdds/big/pgr-eu](https://github.com/latower/SCMD/blob/master/testfiles/obdds/big/pgr-eu),
- [./testfiles/obdds/big/pgr-na](https://github.com/latower/SCMD/blob/master/testfiles/obdds/big/pgr-na),
- [./testfiles/obdds/minimized/pgr-eu](https://github.com/latower/SCMD/blob/master/testfiles/obdds/minimized/pgr-eu), and
- [./testfiles/obdds/minimized/pgr-na](https://github.com/latower/SCMD/blob/master/testfiles/obdds/minimized/pgr-na),

which are state- or country-level connected components extracted from [GridKit: European and North-American extracts](https://zenodo.org/record/47317#.XT9FWlyZZhH), available under the [Open Database License (ODbL) version 1.0](https://github.com/latower/SCMD/blob/master/LICENSE_GridKit).
> Wiegmans, B. (2016). GridKit: European and North-American extracts [Data set]. Zenodo. http://doi.org/10.5281/zenodo.47317

## Contributors
The code in this repository is written and maintained by 
- Behrouz Babaki ([@Behrouz-Babaki](https://github.com/Behrouz-Babaki))
- Anna Latour ([@latower](https://github.com/latower))
- Siegfried Nijssen ([@siegfriednijssen](https://github.com/siegfriednijssen))
- Daniël Fokkinga ([@danielbfokkinga](https://github.com/danielbfokkinga))
