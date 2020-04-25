(ns core
  (:require [clojure.string :as s]
            [semantic-csv.core :as sc]
            [algo :as algo]
            [specs :as specs]
            [schema :as schema]
            [clojure.edn :as edn])
  (:gen-class))

(def output-schema-filename "schema-errors.txt")
(def output-algo-filename "algo-errors.csv")
(def errors (atom nil))

(def orientation-fns
  {"2020-04-06" #'algo/orientation-2020-04-06
   "2020-04-17" #'algo/orientation-2020-04-17})

(defn valid-factors? [data0 data1]
  (let [keys [:fever_algo
              :heart_disease_algo
              :immunosuppressant_disease_algo
              :immunosuppressant_drug_algo]]
    (= (select-keys data0 keys)
       (select-keys data1 keys))))

(defn check-algo [{:keys [date orientation algo_version line] :as data}]
  (if-let [orientation-fn (get orientation-fns algo_version)]
    (try
      (let [normal-data0         (algo/normalize-data data)
            normal-data1         (algo/compute-factors normal-data0)
            computed-orientation (orientation-fn normal-data1)]
        ;; Check csv orientation vs valid orientation:
        (when-not (valid-factors? normal-data0 normal-data1)
          (swap! errors conj
                 (str (s/join "," [line date "incorrect *_algo or factors values"])
                      "\n")))
        ;; Check csv orientation vs valid orientation:
        (when-not (= orientation computed-orientation)
          (swap! errors conj
                 (str (s/join "," [line date
                                   (str orientation " should be "
                                        computed-orientation)])
                      "\n"))))
      (catch Exception _
        (println
         (format "Line %s: cannot apply algo %s" line algo_version))))
    (println
     (format "Line %s: algo_version %s unknown" line algo_version))))

;; Fix data and possibly orientation message using the algorithm
(defn fix-algo [{:keys [algo_version line] :as data} orientation?]
  (if-let [orientation-fn (get orientation-fns algo_version)]
    (try (let [normal-data0         (algo/normalize-data data)
               normal-data1         (algo/compute-factors normal-data0)
               computed-orientation (orientation-fn normal-data1)]
           (if orientation?
             ;; Fix data and orientation
             (dissoc (merge normal-data1 {:orientation computed-orientation})
                     :line)
             ;; Otherwise only return correct data
             (dissoc normal-data1 :line)))
         (catch Exception e
           (println
            (format "Line %s: cannot apply algo %s because %s" line algo_version e))))
    (println
     (format "Line %s: algo_version %s unknown" line algo_version))))

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

(def csv-header [:algo_version :form_version :date ;; :id
                 :duration :postal_code :orientation
                 :age_range :imc :feeding_day :breathlessness
                 :temperature_cat :fever_algo
                 :tiredness :tiredness_details :cough
                 :agueusia_anosmia :sore_throat_aches
                 :diarrhea :diabetes :cancer
                 :breathing_disease :kidney_disease
                 :liver_disease :pregnant
                 :heart_disease :heart_disease_algo
                 :immunosuppressant_disease
                 :immunosuppressant_disease_algo
                 :immunosuppressant_drug
                 :immunosuppressant_drug_algo])

(defn generate-csv-examples [& [number valid?]]
  (let [fix-algo-fn (if valid? #(fix-algo % true) identity)]
    (sc/spit-csv
     "2020-04-17-example.csv"
     (sc/vectorize
      {:header csv-header}
      (map fix-algo-fn
           (specs/generate-samples
            "2020-04-17" (or number 10)))))))

(defn check [{:keys [fun ok-msg err-msg contents input output]}]
  (doseq [data input] (fun data))
  (if (empty? @errors)
    (println ok-msg)
    (do (spit output contents)
        (doseq [err (reverse @errors)]
          (spit output-schema-filename err :append true))
        (println err-msg output))))

(defn fix [{:keys [prefix csv-file data orientation?]}]
  (sc/spit-csv
   (str prefix csv-file)
   (sc/vectorize
    {:header csv-header}
    (map #(fix-algo % orientation?) data))))

(defn -main [opt & [input-csv-file]]
  (reset! errors nil)
  (condp = opt
    "make-schema" (schema/generate)
    "make-csv"    (generate-csv-examples 100 true)
    "check-schema"
    (check {:fun      check-schema
            :ok-msg   "Success! This csv schema is valid."
            :err-msg  "!!! Errors stored in"
            :contents ""
            :input    (csv-to-data input-csv-file)
            :output   output-schema-filename})
    "check-algo"
    (check {:fun      check-algo
            :ok-msg   "Success! All entries have a correct orientation value."
            :err-msg  "!!! Errors stored in"
            :contents "line,date,error\n"
            :input    (csv-to-data input-csv-file)
            :output   output-algo-filename})
    "fix-data"
    (fix {:prefix   "fixed-data-"
          :csv-file input-csv-file
          :data     (csv-to-data input-csv-file)})
    "fix-algo"
    (fix {:prefix       "fixed-algo-"
          :csv-file     input-csv-file
          :data         (csv-to-data input-csv-file)
          :orientation? true})))
