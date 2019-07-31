 
# Experimental Results
For a **description** of the problem settings and datasets on which we perform our experiments, as well as **license information** on the datasets used, see [./testfiles/README.md](https://github.com/latower/SCMD/blob/master/testfiles/README.md).

All figures in this directory show the strictness of the cardinality constraint on the number of positive decisions on the horizontal axes, and the solving time in seconds on the vertical axis. Timeout in all experiments is 3600 seconds (1 hour). 

## CP comparison
We compare the performance of the following methods:
- decomposition *without* guarantees on Generalized Arc Consistency (GAC), solved by CP solver `Gecode`;
- decomposition *with* guarantees on Generalized Arc Consistency (GAC), solved by CP solver `Gecode`;
- linear global propagation algorithm for the SCMD, solved by CP solver `OscaR`;
- sub-linear global propagation algorithm for the SCMD, solved by CP solver `OscaR`.

As input for these methods we use minimized OBDDs. For all these methods we use a fixed, lexicographical branching order, for each decision variable branching first on `false` and then on `true`. Otherwise we use default settings.

We provide the results that were published in our paper in `paper-cp-comparison.pdf`. We provide additional results on a powergrid reliability problem in `pgr-cp-comparison.pdf`.

## MIP comparison
We compare the performance of the following methods:
- a decomposition approach that uses an SDD as the representation of the probability distribution and decomposes it into a MIP, solved by MIP solver `Gurobi`;
- a decomposition approach that uses an OBDD as the representation of the probability distribution and decomposes it into a MIP, solved by MIP solver `Gurobi`;
- sub-linear global propagation algorithm for SCMD constraint, solved by `OscaR`.

As input for these methods we use minimized OBDDs. For the MIP-based approaches, we use `Gurobi`'s default settings. For the sub-linear global algorithm, we use branching order *derivative-one*.

We provide the results that were published in our paper in `paper-mip-comparison.pdf`. We provide additional results on a powergrid reliability problem in `pgr-mip-comparison.pdf`.

## Scaling Comparison
Since our SCMD propagation algorithm traverses the same search tree independent of OBDD size or shape, we performed a comparison of its performance on big (non-minimized) OBDDs and on minimized ones, to see if its solving time indeed scales sub-linearly with OBDD size.

We compare how the sub-linear SCMD propagation algorithm scales with OBDD size to how well the MIP-based decomposition method scale.

Specifically, we compare:
- a decomposition approach that uses an OBDD as the representation of the probability distribution and decomposes it into a MIP, solved by MIP solver `Gurobi`, and
- sub-linear global propagation algorithm for SCMD constraint, solved by `OscaR`

on minimized and non-minimized OBDDs. Default settings for `Gurobi`. For the sub-linear global algorithm, we use branching order *derivative-one*.

We provide the results that were published in our paper in `paper-big-vs-mini.pdf`. We provide additional results on a powergrid reliability problem in `pgr-big-vs-mini.pdf`.

Additionally, we provide some results on how these algorithms scale when they are both restricted to using only one CPU thread. 

`Gurobi` is parallelized and in its default setting will try to use all of the 8 CPU threads in our machine. Our unparallelized sub-linear SCMD solver uses only one. 

We present the above comparison when both solvers are restricted to 1 thread in `1thread-big-vs-mini.pdf`



