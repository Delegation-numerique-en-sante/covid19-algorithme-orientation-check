(ns core
  (:require [clojure.string :as s]
            [semantic-csv.core :as sc]
            [algo :as algo]
            [specs :as specs]
            [schema :as schema]
            [clojure.edn :as edn]
            [java-time :as t])
  (:gen-class))

(def latest-algo-version "2020-06-09")

(def output-schema-filename "schema-errors.txt")
(def output-algo-filename "algo-errors.csv")
(def errors (atom nil))

(def orientation-fns
  {"2020-06-09" #'algo/orientation-2020-04-29
   "2020-05-10" #'algo/orientation-2020-04-29
   "2020-04-29" #'algo/orientation-2020-04-29
   "2020-04-17" #'algo/orientation-2020-04-17
   "2020-04-06" #'algo/orientation-2020-04-06})

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
            normal-data1         (algo/compute-factors normal-data0 algo_version)
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

(defn fix-orientation [orientation]
  (condp = orientation
    "orientation_moins_de_15_ans"             "less_15"
    "orientation_SAMU"                        "SAMU"
    "orientation_domicile_surveillance_1"     "home_surveillance"
    "orientation_consultation_surveillance_1" "consultation_surveillance_1"
    "orientation_consultation_surveillance_2" "consultation_surveillance_2"
    "orientation_consultation_surveillance_3" "consultation_surveillance_3"
    "orientation_consultation_surveillance_4" "consultation_surveillance_4"
    "orientation_surveillance"                "surveillance"
    orientation))

(defn fix-postal_code [pc]
  (condp = pc
    "" ""
    (str (subs pc 0 2) "XXX")))

(defn fix-date [d]
  (str (t/truncate-to (t/instant d) :days)))

;; Fix data and possibly orientation message using the algorithm
(defn fix-algo [{:keys [algo_version line] :as data} orientation?]
  (if-let [orientation-fn (get orientation-fns algo_version)]
    (try (let [normal-data0         (algo/normalize-data data)
               normal-data1         (algo/compute-factors normal-data0 algo_version)
               computed-orientation (orientation-fn normal-data1)]
           (-> (if orientation?
                 ;; Fix data and orientation
                 (dissoc (merge normal-data1 {:orientation computed-orientation}) :line)
                 ;; Otherwise only return correct data
                 (dissoc normal-data1 :line))
               (update :orientation fix-orientation)))
         (catch Exception e
           (println
            (format "Line %s: cannot apply algo %s because %s" line algo_version e))))
    (println
     (format "Line %s: algo_version %s unknown" line algo_version))))

(defn fix-algo-bug [{:keys [algo_version form_version line
                            breathlessness feeding_day
                            orientation]
                     :as   data}]
  (if-let [orientation-fn (get orientation-fns algo_version)]
    (try (let [normal-data0         (algo/normalize-data data)
               normal-data1         (algo/compute-factors normal-data0 algo_version)
               computed-orientation (orientation-fn normal-data1)]
           (-> (if (not (and (= orientation "orientation_SAMU")
                             (= form_version "2020-04-06")
                             (= "false" breathlessness)
                             (= "false" feeding_day)))
                 ;; Fix data and orientation
                 (dissoc (merge normal-data1
                                {:orientation computed-orientation}) :line)
                 ;; Fix only data, not orientation
                 (dissoc normal-data1 :line))
               (update :orientation fix-orientation)
               (update :postal_code fix-postal_code)
               (update :date fix-date)))
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

(def csv-header [:algo_version :form_version :date
                 :duration :postal_code :orientation
                 :age_range :imc :feeding_day :breathlessness
                 :temperature_cat :fever_algo
                 :tiredness :tiredness_details :cough
                 :agueusia_anosmia :sore_throat_aches
                 :diarrhea :diabetes :cancer
                 :breathing_disease :kidney_disease
                 :liver_disease :pregnant :sickle_cell
                 :heart_disease :heart_disease_algo
                 :immunosuppressant_disease
                 :immunosuppressant_disease_algo
                 :immunosuppressant_drug
                 :immunosuppressant_drug_algo
                 :id])

(defn generate-csv-examples [& [number valid?]]
  (let [fix-algo-fn (if valid? #(fix-algo % true) identity)]
    (sc/spit-csv
     "example.csv"
     (sc/vectorize
      {:header csv-header}
      (map fix-algo-fn
           (specs/generate-samples
            latest-algo-version (or number 10)))))))

(defn check [{:keys [fun ok-msg err-msg contents input output]}]
  (doseq [data input] (fun data))
  (if (empty? @errors)
    (println ok-msg)
    (do (spit output contents)
        (doseq [err (reverse @errors)]
          (spit output-schema-filename err :append true))
        (println err-msg output))))

(defn fix [{:keys [suffix csv-file data orientation?]}]
  (sc/spit-csv
   (str csv-file suffix)
   (sc/vectorize
    {:header csv-header}
    (map #(fix-algo % orientation?) data))))

(defn fix-bug [{:keys [suffix csv-file data]}]
  (sc/spit-csv
   (str csv-file suffix)
   (sc/vectorize
    {:header csv-header}
    (map #(fix-algo-bug %) data))))

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
    (fix {:suffix   "-fixed-data.csv"
          :csv-file input-csv-file
          :data     (csv-to-data input-csv-file)})
    "fix-bug"
    (fix-bug {:suffix   "-fixed-bug.csv"
              :csv-file input-csv-file
              :data     (csv-to-data input-csv-file)})
    "fix-algo"
    (fix {:suffix       "-fixed.csv"
          :csv-file     input-csv-file
          :data         (csv-to-data input-csv-file)
          :orientation? true})))
