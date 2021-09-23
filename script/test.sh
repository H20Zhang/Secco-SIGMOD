#!/usr/bin/env bash

# location that stores the data
DataLocation="/XXX/dataset"

# land mark for Single Source Shortest Path Query.
declare -A landmarks=(["imdb"]=1 ["High"]=1 ["Low"]=1 ["LowHigh"]=1 ["wb"]="438238" ["as"]="149419"  ["soc-lj"]="10029" ["ok"]="377664" ["uk"]="17159799" ["tw"]="813286")

# Execute a single test-case
Execute() {
  JAR="./Secco-assembly-0.1.jar"
  executeScript=runSpark-yarn.sh
  mainClass=org.apache.spark.secco.benchmark.SeccoBenchmarkExecutor
  timeLimit=12h

  query=$1
  data=$2
  landmark=$3
  delayStrategy=$4

  /usr/bin/timeout ${timeLimit} $executeScript --class $mainClass $JAR -q $query -d $data --kwargs landmark=$landmark,secco.optimizer.delay_strategy=$delayStrategy
}

# Execute a group of test-case from combination of inputs and queries
ExecuteTasks() {
  inputs=$1
  queries=$2
  landmarks=$4
  delayStrategy=$3

  # shellcheck disable=SC2068
  for i in ${inputs[@]}; do
    for j in ${queries[@]}; do
      input=$i
      landmark=${landmarks[$input]}
      data="${prefix}/${input}"
      query=$j
      SECONDS=0
	    echo "----------------------------------"
	    echo "executing data:${data} query:${query} landmark:${landmark} delayStrategy:$delayStrategy"
      Execute $query $data $landmark $delayStrategy
      duration=$SECONDS
      echo "executed data:${data} query:${query} landmark:${landmark} delayStrategy:$delayStrategy in ${duration} seconds(Total)."
    done
  done
}

# Test-case of subgraph query, which includes S1-S8
# All test-cases includes
#   inputs=(wb as  soc-lj ok uk)
#   queries=(S1 S2 S3 S4 S5 S6 S7 S8)
#   delayStrategy="DP" # it can also be "Greedy" and "Heuristic"
SimpleSubgraphQueryJob() {
  prefix="${DataLocation}"
  inputs=(wb as soc-lj)
  queries=(S1 S2 S3 S4 S5 S6 S7 S8)

#  delayStrategy=AllDelay
#  ExecuteTasks $inputs $queries $delayStrategy $landmarks

#  delayStrategy=NoDelay
#  ExecuteTasks $inputs $queries $delayStrategy $landmarks

#  delayStrategy=JoinDelay
#  ExecuteTasks $inputs $queries $delayStrategy $landmarks

  delayStrategy=Heuristic
  ExecuteTasks $inputs $queries $delayStrategy $landmarks
#
#  delayStrategy=Greedy
#  ExecuteTasks $inputs $queries $delayStrategy $landmarks
#
#  delayStrategy=DP
#  ExecuteTasks $inputs $queries $delayStrategy $landmarks
}

# Test-case of subgraph query, which includes C1-C4
# All test-cases includes
#   inputs=(wb as  soc-lj ok uk)
#   queries=(C1 C2 C3 C4)
#   delayStrategy="DP" # it can also be "Greedy" and "Heuristic"
ComplexSubgraphQueryJob() {
  prefix="${DataLocation}"
  declare -A landmarks=( ["wb"]="438238" ["as"]="149419"  ["soc-lj"]="10029" ["ok"]="377664" ["uk"]="17159799" ["tw"]="813286")
  inputs=(wb as soc-lj)
  queries=(C1 C2 C3 C4)

#  delayStrategy=AllDelay
#  ExecuteTasks $inputs $queries $delayStrategy $landmarks

#  delayStrategy=NoDelay
#  ExecuteTasks $inputs $queries $delayStrategy $landmarks

#  delayStrategy=JoinDelay
#  ExecuteTasks $inputs $queries $delayStrategy $landmarks

  delayStrategy=Heuristic
  ExecuteTasks $inputs $queries $delayStrategy $landmarks
#
#  delayStrategy=Greedy
#  ExecuteTasks $inputs $queries $delayStrategy $landmarks
#
#  delayStrategy=DP
#  ExecuteTasks $inputs $queries $delayStrategy $landmarks
}

# Test-case of subgraph query, which includes I1-I3
# All test-cases includes
#   inputs=(wb as  soc-lj ok uk tw)
#   queries=(I1 I2 I3)
#   delayStrategy="DP" # it can also be "Greedy" and "Heuristic"
SimpleGraphAnalyticJob() {
  prefix="${DataLocation}"
  inputs=(wb as  soc-lj ok uk tw)
  queries=(I1 I2 I3)

  delayStrategy="Heuristic"

  ExecuteTasks $inputs $queries $delayStrategy $landmarks
}

# Test-case of subgraph query, which includes G1I1-G2I3
# All test-cases includes
#   inputs=(wb as  soc-lj ok uk tw)
#   queries=(G1I1 G2I1 G1I2 G2I2 G1I3 G2I3)
#   delayStrategy="DP" # it can also be "Greedy" and "Heuristic"
ComplexGraphAnalyticJob() {
  prefix="${DataLocation}"
  inputs=(wb as  soc-lj ok uk tw)
  queries=(G1I1 G2I1 G1I2 G2I2 G1I3 G2I3)
  queries=(G1I3 G2I3)
  delayStrategy="Heuristic"

  ExecuteTasks $inputs $queries $delayStrategy $landmarks
}

# Test-case of subgraph query, which includes O1-O12
# All test-cases includes
#   inputs=(imdb)
#   queries=(O1 O2 O3 O4 O5 O6 O7 O8 O9 O10 O11 O12)
#   delayStrategy="DP" # it can also be "Greedy" and "Heuristic"
ComplexOLAPQueryJob() {
  prefix="${DataLocation}"
  inputs=("imdb")
  queries=(O1 O2 O3 O4 O5 O6 O7 O8 O9 O10 O11 O12)

#  delayStrategy=AllDelay
#  ExecuteTasks $inputs $queries $delayStrategy $landmarks

#  delayStrategy=NoDelay
#  ExecuteTasks $inputs $queries $delayStrategy $landmarks

#  delayStrategy=JoinDelay
#  ExecuteTasks $inputs $queries $delayStrategy $landmarks

  delayStrategy=Heuristic
  ExecuteTasks $inputs $queries $delayStrategy $landmarks
#
#  delayStrategy=Greedy
#  ExecuteTasks $inputs $queries $delayStrategy $landmarks
#
#  delayStrategy=DP
#  ExecuteTasks $inputs $queries $delayStrategy $landmarks
}

# Test-case of workload experiment, which includes W1-W5
# All test-cases includes
#   inputs=(workload_exp/W1/High workload_exp/W2/High workload_exp/W3/High workload_exp/W4/High workload_exp/W5/High)
#   inputs=(workload_exp/W1/Low workload_exp/W2/Low workload_exp/W3/Low workload_exp/W4/Low workload_exp/W5/Low)
#   inputs=(workload_exp/W1/LowHigh workload_exp/W2/LowHigh workload_exp/W3/LowHigh workload_exp/W4/LowHigh workload_exp/W5/LowHigh)
#   queries=(W1 W2 W3 W4 W5)
#   delayStrategy="DP" # it can also be "Greedy", "Heuristic", "NoDelay", "AllDelay"
WorkloadExpJob() {

  workloads=(W1 W2 W3 W4 W5 W6 W7 W8 W9 W10)
  inputs=("Low" "LowHigh" "High")

  # shellcheck disable=SC2068
  for i in ${workloads[@]}; do
    workload=$i
    prefix="${DataLocation}/workload_exp/${workload}"
    queries=(${workload})
    delayStrategy=JoinDelay

    ExecuteTasks $inputs $queries $delayStrategy $landmarks

  done

#   shellcheck disable=SC2068
  for i in ${workloads[@]}; do
    workload=$i
    prefix="${DataLocation}/workload_exp/${workload}"
    queries=(${workload})
    delayStrategy=HeuristicSize

    ExecuteTasks $inputs $queries $delayStrategy $landmarks

  done

  # shellcheck disable=SC2068
  for i in ${workloads[@]}; do
    workload=$i
    prefix="${DataLocation}/workload_exp/${workload}"
    queries=(${workload})
    delayStrategy=Greedy

    ExecuteTasks $inputs $queries $delayStrategy $landmarks

  done

  # shellcheck disable=SC2068
  for i in ${workloads[@]}; do
    workload=$i
    prefix="${DataLocation}/workload_exp/${workload}"
    queries=(${workload})
   delayStrategy=DP

    ExecuteTasks $inputs $queries $delayStrategy $landmarks

  done

#   shellcheck disable=SC2068
  for i in ${workloads[@]}; do
    workload=$i
    prefix="${DataLocation}/workload_exp/${workload}"
    queries=(${workload})
    delayStrategy=Heuristic

   ExecuteTasks $inputs $queries $delayStrategy $landmarks

  done

  # shellcheck disable=SC2068
  for i in ${workloads[@]}; do
    workload=$i
    prefix="${DataLocation}/workload_exp/${workload}"
    queries=(${workload})
   delayStrategy=AllDelay

   ExecuteTasks $inputs $queries $delayStrategy $landmarks

  done

  # shellcheck disable=SC2068
  for i in ${workloads[@]}; do
    workload=$i
    prefix="${DataLocation}/workload_exp/${workload}"
    queries=(${workload})
    delayStrategy=NoDelay

   ExecuteTasks $inputs $queries $delayStrategy $landmarks

  done

}

#WorkloadExpJob
# SimpleSubgraphQueryJob
#ComplexSubgraphQueryJob
#ComplexOLAPQueryJob
# SimpleGraphAnalyticJob
#ComplexGraphAnalyticJob
