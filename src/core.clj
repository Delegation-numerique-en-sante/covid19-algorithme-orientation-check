(ns core
  (:require [clojure.string :as s]
            [semantic-csv.core :as semantic-csv]
            [algos :as algos]
            [clojure.edn :as edn]
            [java-time :as t])
  (:gen-class))

(def today (t/local-date))
(def output-csv-filename
  (str "covid19-algo-check-" today ".csv"))
(def output-txt-filename
  (str "covid19-algo-check-" today ".txt"))

(def fn-versions
  {"2020-04-06" {:result-fn     #'algos/result-2020-04-06
                 :preprocess-fn #'algos/preprocess-2020-04-06}
   "2020-04-17" {:result-fn     #'algos/result-2020-04-17
                 :preprocess-fn #'algos/preprocess-2020-04-17}})

(def errors (agent nil))

(defn check-imc [imc]
  (condp #(= %1 (type %2))
      (try (edn/read-string imc) (catch Exception _ false))
    java.lang.Long   imc
    java.lang.Double imc
    false))

;; Convert a json line into a csv line
(defn check-line [{:keys [date orientation
                          algo_version line
                          duration imc]
                   :as   data}]
  ;; Check duration
  (when-not (not (int? (edn/read-string duration)))
    (send errors conj
          (str "Line" line ": duration" duration "not a number\n")))
  ;; Check imc (bmi)
  (when-not (check-imc imc)
    (send errors conj
          (str "Line" line ": imc" imc "not valid\n")))
  ;; Check iso date
  (when-not (try (t/instant date) (catch Exception _ false))
    (send errors conj
          (str "Line" line ": date" date "not valid\n")))
  ;; Check orientation
  (if-let [{:keys [preprocess-fn result-fn]}
           (get fn-versions algo_version)]
    (try ;; Preprocess input values
      (let [computed-values      (merge data (preprocess-fn data))
            computed-orientation (result-fn computed-values)]
        ;; Check csv orientation vs valid orientation
        (when (not (= orientation computed-orientation))
          (str (s/join "," [line date orientation computed-orientation]) "\n")))
      (catch Exception _ (println "Error line" line)))
    (send errors conj
          (str "Line" line ": algo version" algo_version "unknown\n"))))

(defn -main [& [input-csv-file]]
  (spit output-csv-filename "line,date,tested-orientation,valid-orientation\n")
  (doseq [csv-line (map-indexed
                    (fn [idx itm] (merge itm {:line (inc idx)}))
                    (semantic-csv/slurp-csv input-csv-file))]
    (when-let [err (check-line csv-line)]
      (spit output-csv-filename err :append true)))
  (doseq [err @errors]
    (spit output-txt-filename err :append true))
  (System/exit 0))
