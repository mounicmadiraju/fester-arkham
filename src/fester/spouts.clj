(ns fester.spouts
  (:require [backtype.storm [clojure :refer [defspout spout emit-spout!]]]
            [clj-kafka.consumer.zk :as zfkc]
            [clj-kafka.core :as kafka]
            [clj-kafka.producer :as producer]
            [clj-kafka.zk :as zk]
            [clojure.tools.logging :as log]
            [flatland.protobuf.core :refer [protodef protobuf protobuf-load protobuf-dump]])
  (:import [com.google.protobuf InvalidProtocolBufferException]
           [fester protobuf$Metric]
           [java.io ByteArrayInputStream]
           [java.nio ByteBuffer]
           [java.util.concurrent ArrayBlockingQueue]))


(def Metric (protodef fester.protobuf$Metric))

;; TODO: Put into config files
(def consumer-config
  {"zookeeper.connect" "localhost:2181"
   "group.id" "clj-kafka.consumer"
   "auto.offset.reset" "smallest"
   "auto.commit.enable" "false"})

;; TODO: Put into config files
(def producer-config
  {"metadata.broker.list" "localhost:9092"
   "serializer.class" "kafka.serializer.DefaultEncoder"
   "partitioner.class" "kafka.producer.DefaultPartitioner"})

;; TODO: start dynamically
(def p
  (producer/producer producer-config))

(defn send-message [where what]
  (producer/send-message p
    (producer/message where what)))

(defn start-consumer-thread [topic queue-size]
  (let [running? (atom true)
        abq      (ArrayBlockingQueue. queue-size)]
    (future
      (let [c (zfkc/consumer consumer-config)
            count (atom 0)]
        (try
          (let [stream-map (.createMessageStreams c {topic (int 1)})
                [stream & _] (get stream-map topic)
                msg-seq (iterator-seq (.iterator stream))]
            (doseq [msg msg-seq :while @running?]
              (.put abq (kafka/to-clojure msg))
              (when (mod (swap! count inc) 1024)
                (.commitOffsets c))))
          (log/info "Consumer for" topic "stopping")
          (catch Exception e
            (log/error "Consumer for" topic "encountered" e))
          (finally
            (.shutdown c)))))
    {:queue abq :running? running?}))

(defn parse-message [entry]
  (when-let [buf (:value entry)]
    (try
      (protobuf-load Metric buf)
      (catch InvalidProtocolBufferException e
        (println e)
        (log/error "Invalid protocol buffer message:" (str e) (String. buf))))))

(defspout fester-spout ["ts" "key" "value"]
  {:prepare true
   :params [topic queue-size]}
  [conf context collector]
  (let [{:keys [queue running?]}
        (start-consumer-thread topic queue-size)]
    (spout
      (nextTuple []
        (when-let [entry (parse-message (.poll queue))]
          (let [{:keys [time key value]} entry]
            (when (and time key value)
              (emit-spout! collector [time key value]))))))))

(defspout fake-data-spout ["ts" "key" "value"]
  {:prepare true
   :params [topic]}
  [conf context collector]
  (let [x (atom 0)
        key (str "key-" (rand))]
    (spout
      (nextTuple []
        (let [ct (System/currentTimeMillis)]
          (send-message topic
            (protobuf-dump (protobuf Metric :time ct :key key :value @x)))
          (swap! x inc))))))
