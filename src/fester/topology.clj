(ns fester.topology
  (:require [backtype.storm.clojure :refer [topology spout-spec bolt-spec]]
            [backtype.storm.config :refer :all]
            [fester.bolts :refer [fester-raw-metric-bolt
                                  fester-rollup-metric-bolt]]
            [fester.spouts :refer [fester-spout fake-data-spout]])
  (:import [backtype.storm LocalCluster LocalDRPC]))


(def topic "uncle_fester")
(def queue-size 1024)

(defn storm-topology []
  (topology
    {"fester-spout"
     (spout-spec (fester-spout topic queue-size))
     "fake-data"
     (spout-spec (fake-data-spout topic) :p 3)}
    {"fester-raw-metric-bolt"
     (bolt-spec
       {"fester-spout" ["key"]
        "fake-data"    ["key"]}
       (fester-raw-metric-bolt :dont-batch) :p 3)
     "fester-10s-metric-bolt"
     (bolt-spec
       {"fester-raw-metric-bolt" ["key"]}
       (fester-rollup-metric-bolt 10000 :avg) :p 3)
     "fester-1m-metric-bolt"
     (bolt-spec
       {"fester-10s-metric-bolt" ["key"]}
       (fester-rollup-metric-bolt 60000 :avg) :p 3)
     "fester-1hr-metric-bolt"
     (bolt-spec
       {"fester-1m-metric-bolt" ["key"]}
       (fester-rollup-metric-bolt 600000 :avg) :p 3)}))

(defn run! [& {debug "debug" workers "workers" :or {debug "true" workers "2"}}]
  (doto (LocalCluster.)
    (.submitTopology "topology-1"
      {TOPOLOGY-DEBUG (Boolean/parseBoolean debug)
       TOPOLOGY-WORKERS (Integer/parseInt workers)}
      (storm-topology))))
