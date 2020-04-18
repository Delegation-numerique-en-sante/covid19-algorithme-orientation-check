(ns algos
  (:require [clojure.edn :as edn]))

(def s edn/read-string)

;; (def data {:heart_disease                  "0",
;;            :postal_code                    "37",
;;            :temperature_cat                "NSP",
;;            :date                           "2020-04-15T21:59:46.656461Z",
;;            :cough                          "true",
;;            :immunosuppressant_disease_algo "false",
;;            :heart_disease_algo             "false",
;;            :liver_disease                  "false",
;;            :age_range                      "from_15_to_49",
;;            :form_version                   "2020-04-06",
;;            :pregnant                       "888",
;;            :immunosuppressant_disease      "0",
;;            :orientation                    "orientation_consultation_surveillance_3",
;;            :kidney_disease                 "false",
;;            :duration                       "230",
;;            :sore_throat_aches              "false",
;;            :immunosuppressant_drug_algo    "false",
;;            :breathing_disease              "false",
;;            :line                           1,
;;            :diabetes                       "false",
;;            :algo_version                   "2020-04-06",
;;            :id                             "28ef0394-741f-4f4c-8c14-632843a24a9c",
;;            :imc                            "24.1",
;;            :fever                          "999",
;;            :cancer                         "false",
;;            :fever_algo                     "false",
;;            :agueusia_anosmia               "false",
;;            :tiredness                      "false",
;;            :breathlessness                 "false",
;;            :immunosuppressant_drug         "0",
;;            :diarrhea                       "true",
;;            :tiredness_details              "false",
;;            :feeding_day                    "false"})

(defn preprocess-2020-04-06
  [{:keys [imc age_range
           tiredness tiredness_details
           feeding_day
           breathlessness
           pregnant cancer diabetes
           liver_disease kidney_disease
           breathing_disease
           fever_algo heart_disease_algo
           immunosuppressant_drug_algo
           immunosuppressant_disease_algo]}]
  (let [major-severity-factors (atom 0)
        minor-severity-factors (atom 0)
        pronostic-factors      (atom 0)]
    ;; Compute pronostic factors
    (when (= age_range "sup_70") (swap! pronostic-factors inc))
    (when (>= (s imc) 30) (swap! pronostic-factors inc))
    (when (s heart_disease_algo) (swap! pronostic-factors inc))
    (when (s immunosuppressant_disease_algo) (swap! pronostic-factors inc))
    (when (s immunosuppressant_drug_algo) (swap! pronostic-factors inc))
    (when (s kidney_disease) (swap! pronostic-factors inc))
    (when (s liver_disease) (swap! pronostic-factors inc))
    (when (s breathing_disease) (swap! pronostic-factors inc))
    (when (s cancer) (swap! pronostic-factors inc))
    (when (s diabetes) (swap! pronostic-factors inc))
    (when (= (s pregnant) 1) (swap! pronostic-factors inc))
    ;; Compute minor-severity-factors
    (when (s fever_algo) (swap! minor-severity-factors inc))
    (when (and (s tiredness) (s tiredness_details))
      (swap! minor-severity-factors inc))
    ;; Compute major-severity-factors
    (when (s breathlessness) (swap! major-severity-factors inc))
    (when (s feeding_day) (swap! major-severity-factors inc))
    {:major-severity-factors @major-severity-factors
     :minor-severity-factors @minor-severity-factors
     :pronostic-factors      @pronostic-factors}))

(def preprocess-2020-04-17 preprocess-2020-04-06)

(defn result-2020-04-06
  [{:keys [age_range fever_algo diarrhea cough
           sore_throat_aches agueusia_anosmia
           pronostic-factors
           major-severity-factors
           minor-severity-factors]}]
  (let [fever_algo        (s fever_algo)
        diarrhea          (s diarrhea)
        cough             (s cough)
        sore_throat_aches (s sore_throat_aches)
        agueusia_anosmia  (s agueusia_anosmia)]
    (cond
      ;; Branche 1
      (= age_range "inf_15")
      "orientation_moins_de_15_ans"
      ;; Branche 2
      (>= major-severity-factors 1)
      "orientation_SAMU"
      ;; Branche 3
      (and fever_algo cough)
      (cond (= pronostic-factors 0)
            "orientation_consultation_surveillance_3"
            (>= pronostic-factors 1)
            (if (< minor-severity-factors 2)
              "orientation_consultation_surveillance_3"
              "orientation_consultation_surveillance_2"))
      ;; Branche 4
      (or fever_algo
          diarrhea
          (and cough sore_throat_aches)
          (and cough agueusia_anosmia))
      (cond (= pronostic-factors 0)
            (if (= minor-severity-factors 0)
              (if (= age_range "from_15_to_49")
                "orientation_domicile_surveillance_1"
                "orientation_consultation_surveillance_1")
              "orientation_consultation_surveillance_1")
            (>= pronostic-factors 1)
            (if (< minor-severity-factors 2)
              "orientation_consultation_surveillance_1"
              "orientation_consultation_surveillance_2"))
      ;; Branche 5
      (or cough sore_throat_aches agueusia_anosmia)
      (if (= pronostic-factors 0)
        "orientation_domicile_surveillance_1"
        "orientation_consultation_surveillance_4")
      ;; Branche 6
      (and (not cough)
           (not sore_throat_aches)
           (not agueusia_anosmia))
      "orientation_surveillance")))

(defn result-2020-04-17
  [{:keys [age_range
           fever_algo diarrhea cough
           sore_throat_aches agueusia_anosmia
           pronostic-factors
           major-severity-factors
           minor-severity-factors]}]
  (let [fever_algo        (s fever_algo)
        diarrhea          (s diarrhea)
        cough             (s cough)
        sore_throat_aches (s sore_throat_aches)
        agueusia_anosmia  (s agueusia_anosmia)]
    (cond
      ;; Branche 1
      (= age_range "inf_15")
      "orientation_moins_de_15_ans"
      ;; Branche 2
      (>= major-severity-factors 1)
      "orientation_SAMU"
      ;; Branche 3
      (and fever_algo cough)
      (cond (= pronostic-factors 0)
            "orientation_consultation_surveillance_3"
            (>= pronostic-factors 1)
            (if (< minor-severity-factors 2)
              "orientation_consultation_surveillance_3"
              "orientation_consultation_surveillance_2"))
      ;; Branche 4
      (or fever_algo
          diarrhea
          (and cough sore_throat_aches)
          (and cough agueusia_anosmia)
          (and sore_throat_aches agueusia_anosmia))
      (cond (= pronostic-factors 0)
            (if (= minor-severity-factors 0)
              (if (= age_range "from_15_to_49")
                "orientation_domicile_surveillance_1"
                "orientation_consultation_surveillance_1")
              "orientation_consultation_surveillance_1")
            (>= pronostic-factors 1)
            (if (< minor-severity-factors 2)
              "orientation_consultation_surveillance_1"
              "orientation_consultation_surveillance_2"))
      ;; Branche 5
      (or (and cough (not sore_throat_aches) (not agueusia_anosmia))
          (and (not cough) sore_throat_aches (not agueusia_anosmia))
          (and (not cough) (not sore_throat_aches) agueusia_anosmia))
      (if (= pronostic-factors 0)
        "orientation_domicile_surveillance_1"
        "orientation_consultation_surveillance_4")
      ;; Branche 6
      (and (not cough)
           (not sore_throat_aches)
           (not agueusia_anosmia))
      "orientation_surveillance")))
