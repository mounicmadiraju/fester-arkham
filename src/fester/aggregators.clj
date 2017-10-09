(ns fester.aggregators)


(defn sum [values]
  (reduce + 0 values))

(defn avg [values]
  (/ (sum values)
    (float (count values))))
