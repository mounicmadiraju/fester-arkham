(ns fester.TopologySubmitter
  (:require [fester.topology :refer [storm-topology]]
            [backtype.storm [config :refer :all]])
  (:import [backtype.storm StormSubmitter])
  (:gen-class))


(defn -main [& {:strs [debug workers] :or
                {debug "false" workers "4"}}]
  (StormSubmitter/submitTopology
    "fester topology"
    {TOPOLOGY-DEBUG (Boolean/parseBoolean debug)
     TOPOLOGY-WORKERS (Integer/parseInt workers)}
    (storm-topology)))
