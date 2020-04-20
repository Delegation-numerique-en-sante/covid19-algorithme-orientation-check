(ns core
  (:require [clojure.string :as s]
            [semantic-csv.core :as semantic-csv]
            [algo :as algo]
            [specs :as specs]
            [clojure.edn :as edn]
            [java-time :as t])
  (:gen-class))

(def today (t/local-date))

(def output-csv-filename
  (str today "-covid19-orientation-check.csv"))

(def output-txt-filename
  (str today "-~covid19-errors.txt"))

(def errors (agent nil))

(def fn-versions
  {"2020-04-06" {:result-fn     #'algo/result-2020-04-06
                 :preprocess-fn #'algo/preprocess-2020-04-06}
   "2020-04-17" {:result-fn     #'algo/result-2020-04-17
                 :preprocess-fn #'algo/preprocess-2020-04-17}})

;; Convert a json line into a csv line
(defn check-line [{:keys [date orientation
                          algo_version line
                          duration imc]
                   :as   data}]
  ;; Validate data against the schema
  (when-not (specs/valid-response data algo_version)
    (send errors conj
          (format "Line %s: invalid data\n" line)))
  ;; Check if the orientation message is valid
  (if-let [{:keys [preprocess-fn result-fn]}
           (get fn-versions algo_version)]
    (try ;; First preprocess input values
      (let [computed-values      (merge data (preprocess-fn data))
            computed-orientation (result-fn computed-values)]
        ;; Then check csv orientation vs valid orientation
        (when-not (= orientation computed-orientation)
          (str (s/join "," [line date orientation computed-orientation]) "\n")))
      (catch Exception _ (println "Error line" line)))
    (send errors conj
          (str "Line" line ": algo version" algo_version "unknown\n"))))

(defn -main [& [input-csv-file]]
  ;; Prepare the csv
  (spit output-csv-filename "line,date,tested-orientation,valid-orientation\n")
  ;; Output orientation errors
  (doseq [csv-line (map-indexed
                    (fn [idx itm] (merge itm {:line (inc idx)}))
                    (semantic-csv/slurp-csv input-csv-file))]
    (when-let [err (check-line csv-line)]
      (spit output-csv-filename err :append true)))
  ;; Output other errors
  (doseq [err @errors]
    (spit output-txt-filename err :append true))
  ;; Stop the agent
  (System/exit 0))


