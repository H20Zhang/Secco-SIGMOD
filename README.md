# Secco

Secco (**Se**parate **c**ommunication from **co**mputation) is a distributed SQL system for SQL Query, Graph Analytic, and Subgraph Query.

#### Table of Contents
1. [Prerequisite](#Prerequisite)
2. [Project Structure](#Project-Structure)
3. [Usage](#Usage)
4. [Testing](#Testing)

------

## Prerequisite

You need to install Spark 2.4.5, Hadoop 2.7.2 on your cluster.

## Project Structure

```
/datasets - folder for storing toy datasets and folder template for storing datasets of synthetic workload experiment
/project - project related configuration files.
/script - scripts for running and testing Secco.
/src
	src/main - source files
		src/main/resource - configuration files for Secco
		src/main/scala - scala source files 
			org/apache/spark/secco: main package
				org/apache/spark/secco/analysis - analyzer
				org/apache/spark/secco/benchmark - benchmark & testing
				org/apache/spark/secco/catalog - catalog of database
				org/apache/spark/secco/config - configurations
				org/apache/spark/secco/execution - physical plans & planner
				org/apache/spark/secco/expression - expressions
				org/apache/spark/secco/optimization - logical plans & optimizer
				org/apache/spark/secco/parsing - parser
				org/apache/spark/secco/trees - tree struture used in optimizer framework
				org/apache/spark/secco/types - types
				org/apache/spark/secco/utils - utility
	src/test - unit tests files
		src/test/resource - configuration files for Secco in unit tests
		src/test/scala - scala unit tests files
			src/test/scala/integration - integration test
			src/test/scala/playground - playground for testing new functions
			src/test/scala/unit - unit test
			src/test/scala/util - utility for testing
Secco-assembly-0.1.jar - compiled jar package of Secco
```

## Usage

### Import

You can import the source code of Secco project using Jetbrain IntelliJ IDEA. 

### Use

The main object in Secco to manipulate is `Dataset`, which just like the `Dataset` in `SparkSQL`. In `Dataset`, it defines relational algebra operators (e.g., select, project, join) that transforms the dataset.

The main entry of Secco is SeccoSession, where you can create the `Dataset` , register `Dataset` in `Catalog`, get `Dataset` from `Catalog`, and issuse `SQL` query.

An example is shown below.

```scala
// Obtain SeccoSession via singleton.
    val dlSession = SeccoSession.currentSession

    // Create datasets.
    val seq1 = Seq(Array(1.0, 2.0), Array(2.0, 2.0))
    val tableName = "R1"
    val schema = Seq("A", "B")
    val ds1 =
      dlSession.createDatasetFromSeq(seq1, Some(tableName), Some(schema))

    // Construct RA expression via relational algebra like API.
    val ds2 = ds1.select("A < B")

    // Explain the query execution of ds1 and ds2. It will show parsed plan, analyzed plan, optimized plan, execution plan.
    ds1.explain()
    ds2.explain()
```

For more usage, please check class  `org.apache.spark.secco.SeccoSession` and `org.apache.spark.secco.Dataset`, there contains comments for guiding you using the system.  We recommand you using the `Dataset` api instead of `SQL` api, as it currently have some bugs, and we disable it for now.

## Testing

To reproduce the experiment mentioned in the paper, we prepare the compiled jar packages and scripts. You can follow the guide below to reproduce the results.

### Datasets

#### Download Real Datasets

To download the real datasets found in paper

1. For WB, AS, LJ, OK, go to https://snap.stanford.edu/data/index.html
2. For UK, go to http://law.di.unimi.it/datasets.php
3. For TW, go to https://anlab-kaist.github.io/traces/WWW2010
4. For IMDB, go to https://www.imdb.com 

#### Generate Synthetic Datasets

To generate synthetic datasets needed in Workload Experiment Testing

1. install SBT.
2. execute SBT
3. in SBT shell, execute `testOnly *SyntheticDatasetsSuite`
4. the generated synthetic datasets will be in `./datasets`

#### Demo Datasets

We have prepared three demo datasets, `debugData`, `imdb` (demo version), and `wiki` in `./datasets`

#### Preprocessing

You need to do some preprocessing on the raw datasets.

1. For UK, you need to convert it from WebGraph format into edgelist format first. Please follow the instruction in https://github.com/helgeho/HadoopWebGraph.
2. For edge list of WB, AS, LJ, OK, UK, TW,  you need to name the original file by `rawData` and prepare an undirected version graph named `undirected`, which will be used in subgraph query experiment.
3. For IMDB, it needs to be preprocessed with imdbpy3 package, which can be downloaded in https://bitbucket.org/alberanid/imdbpy/get/5.0.zip
4. After you have prepared all datasets, put all dataset in HDFS. 
5. For all relations of IMDB, you need to put it under a folder named `imdb`
6. For all relations (i.e., `directed` and `undirected` ) of a graph dataset (e.g., WB), you need to put it under a folder (e.g., `wb`). Please name the folders of the graph datasets WB, AS, LJ, OK, UK, TW as wb, as, soc-lj, ok, uk tw respectively. 

### Scripts for Testing

There are several scripts included in "/script" folder fro helping you running Secco in the distributed environment.

```tex
runSpark-yarn.sh: script for submitting spark program to yarn
upload.sh: script for uploading relevant jar packages and datasets to the remote cluters
test.sh: script that contains test in the paper
```

To correctly run the scripts, you need to modify the scripts based on your own computer's and clusters' settings.

1. put files, e.g., datasets, you want to upload to cluster under `script/upload`, and modify upload.sh by replacing `Cluster` with your own clusters folder address. The compile jar file (Secco-assembly-0.1.jar) will be uploaded by default.
2. modify `test.sh` by assiging `DataLocation`, e.g., `XXX/dataset` with the location you stored datasets in HDFS.
3. modify `runSpark-yarn.sh` by replacing `$SPARK_HOME` with your own spark installation address. 

### Run Test

We've prepare a compiled jar package, Secco-assembly-0.1.jar, so that you can use test Secco without import the project and compiling it. 

To run the experiments in the paper:

0. On local machine, cd to folder that contains `Secco`

1. On local machine, execute `script/upload.sh` to upload jar and datasets

2. In cluster, execute `test.sh` with selective commands uncommented.
   1. For Subgraph Query, you need to uncomment `SimpleSubgraphQueryJob` and `SimpleSubgraphQueryJob` in `test.sh`

   2. For SQL Query, you need to uncomment `ComplexOLAPQueryJob` in `test.sh`

   3. For Graph Analytic Query, you need to uncomment `SimpleGraphAnalyticJob` and `ComplexGraphAnalyticJob `in `test.sh`

   4. For Workload Experiment Query, 

      1. you need to uncomment `WorkloadExpJob `in `test.sh`

      2. you need to modify configuration files `/Secco/src/main/resources/reference.conf`

         1. setting

         ```
         secco.optimizer.estimator = Histogram // select from "Exact", "Naive", "Histogram"
         secco.optimizer.exact_cardinality_mode = provided // select from "provided" and "computed"
         secco.optimizer.enable_only_decouple_optimization = true // if only enable decoupled related optimizations
         secco.optimizer.enable_early_aggregation_optimization = false // if enable early aggregate optimizations
         ```

      3. recompile secco project by typing `sbt assembly` in root folder of `Secco`

      4. upload recompiled jar to cluster.

