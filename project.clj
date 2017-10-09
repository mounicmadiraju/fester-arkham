(defproject fester "0.0.1"
  :description "Uncle Fester's savage analytics dungeon"
  :license {:name "Apache"}
  :dependencies [[clj-kafka "0.2.8-0.8.1.1"]
                 [com.boundary/high-scale-lib "1.0.6"]
                 [clojurewerkz/cassaforte "2.0.0"
                  :exclusions [org.slf4j/slf4j-api]]
                 [org.clojure/clojure "1.5.1"]
                 [org.clojure/tools.logging "0.3.1"]
                 [log4j "1.2.15" :exclusions [javax.mail/mail
                                              javax.jms/jms
                                              com.sun.jdmk/jmxtools
                                              com.sun.jmx/jmxri]]
                 [cc.qbits/hayt "2.0.0"]
                 [cc.qbits/nippy-lz4 "0.1.0"]
                 [org.flatland/protobuf "0.8.1"]]
  :aot :all
  :main fester.TopologySubmitter
  :profiles {:dev {:dependencies [[org.apache.storm/storm-core "0.9.3"]]}
             :provided {:dependencies [[org.apache.storm/storm-core "0.9.3"]]}}
  :plugins [[lein-ver "1.0.1"]
            [lein-protobuf "0.1.1"]])
