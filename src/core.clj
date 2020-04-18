(ns core
  (:require [clojure.string :as s]
            [semantic-csv.core :as semantic-csv]
            [algos :as algos]
            [java-time :as t])
  (:gen-class))

(def today (t/local-date))
(def output-filename
  (str "covid19-algo-check-" today ".csv"))

(def fn-versions
  {"2020-04-06" {:result-fn     #'algos/result-2020-04-06
                 :preprocess-fn #'algos/preprocess-2020-04-06}
   "2020-04-17" {:result-fn     #'algos/result-2020-04-17
                 :preprocess-fn #'algos/preprocess-2020-04-17}})

;; Convert a json line into a csv line
(defn check-line [{:keys [date orientation
                          algo_version line]
                   :as   data}]
  (if-let [{:keys [preprocess-fn result-fn]}
           (get fn-versions algo_version)]
    (try
      ;; Preprocess input values
      (let [computed-values      (merge data (preprocess-fn data))
            computed-orientation (result-fn computed-values)]
        ;; Check csv orientation vs valid orientation
        (when (not (= orientation computed-orientation))
          (str (s/join "," [line date orientation computed-orientation]) "\n")))
      (catch Exception _ (println "Error line" line)))
    (println "Line" line ": algo version" algo_version "unknown")))

(defn -main [& [input-csv-file]]
  (with-open [w (clojure.java.io/writer output-filename :append true)]
    (.write w "line,date,tested-orientation,valid-orientation\n")
    (doseq [csv-line (map-indexed
                      (fn [idx itm] (merge itm {:line (inc idx)}))
                      (semantic-csv/slurp-csv input-csv-file))]
      (when-let [err (check-line csv-line)]
        (if (string? err)
          (.write w err))))))
