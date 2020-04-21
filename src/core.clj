(ns core
  (:require [clojure.string :as s]
            [semantic-csv.core :as sc]
            [algo :as algo]
            [specs :as specs]
            [schema :as schema]
            [clojure.edn :as edn]
            [java-time :as t])
  (:gen-class))

(def today (t/local-date))

(def output-schema-filename
  (str today "-covid19-schema-errors.csv"))

(def output-algo-filename
  (str today "-covid19-algo-errors.txt"))

(def errors (atom nil))

(def fn-versions
  {"2020-04-06" {:result-fn     #'algo/result-2020-04-06
                 :preprocess-fn #'algo/preprocess-2020-04-06}
   "2020-04-17" {:result-fn     #'algo/result-2020-04-17
                 :preprocess-fn #'algo/preprocess-2020-04-17}})

;; Convert a json line into a csv line
(defn check-algo [{:keys [date orientation
                          algo_version line
                          duration imc]
                   :as   data}]
  ;; Check if the orientation message is valid
  (if-let [{:keys [preprocess-fn result-fn]}
           (get fn-versions algo_version)]
    (try ;; First preprocess input values
      (let [computed-values      (merge data (preprocess-fn data))
            computed-orientation (result-fn computed-values)]
        ;; Then check csv orientation vs valid orientation
        (when-not (= orientation computed-orientation)
          (swap! errors conj
                 (str (s/join "," [line date orientation
                                   computed-orientation]) "\n"))))
      (catch Exception _
        (println
         (format "Line %s: cannot apply algo %s\n" line algo_version))))
    (println
     (format "Line %s: algo_version %s unknown\n" line algo_version))))

(defn check-schema [{:keys [algo_version line] :as data}]
  ;; Validate data against the schema
  (when-not (specs/valid-response data algo_version)
    (swap! errors conj
           (format "Line %s: invalid data\n" line))))

(defn csv-to-data [input-csv-file]
  (map-indexed
   (fn [idx itm] (merge itm {:line (inc idx)}))
   (sc/slurp-csv
    input-csv-file
    :cast-fns {:imc      #(edn/read-string %)
               :duration #(edn/read-string %)})))

(defn -main [opt & [input-csv-file]]
  (reset! errors nil)
  (condp = opt
    "make-schema" (schema/generate)
    "make-csv"    (specs/generate-csv-examples)
    "check-schema"
    (let [input-data (csv-to-data input-csv-file)]
      (doseq [data input-data] (check-schema data))
      (if (empty? @errors)
        (println "All entries have a valid schema")
        (doseq [err (reverse @errors)]
          (spit output-schema-filename "")
          (spit output-schema-filename err :append true))))
    "check-algo"
    (let [input-data (csv-to-data input-csv-file)]
      (doseq [data input-data] (check-algo data))
      (if (empty? @errors)
        (println "All entries have a correct orientation value")
        (do (spit output-algo-filename
                  "line,date,tested-orientation,valid-orientation\n")
            (doseq [err (reverse @errors)]
              (spit output-algo-filename err :append true)))))))

;; (-main "make-csv")
;; (-main "make-schema")
;; (-main "check-algo" "2020-04-17-example.csv")
;; (-main "check-schema" "2020-04-17-example.csv")
