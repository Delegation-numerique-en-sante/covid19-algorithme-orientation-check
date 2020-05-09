(ns algo
  (:require [clojure.edn :as edn]
            [java-time :as t]))

(defn normalize [v]
  (cond
    (nat-int? v) v
    :else
    (let [val-s (edn/read-string v)]
      (condp #(= %1 (type %2)) val-s
        java.lang.Boolean val-s
        java.lang.Long
        (condp = val-s 1 true 0 false val-s)
        v))))

(defn normalize-data [data]
  (merge
   data
   (into
    {}
    (for [[k v] (dissoc data
                        :postal_code :id :temperature_cat
                        :form_version :algo_version
                        :date :duration :imc)]
      [k (normalize v)]))))

(defn compute-factors [{:keys [imc age_range
                               tiredness tiredness_details
                               feeding_day
                               breathlessness
                               pregnant cancer diabetes
                               liver_disease kidney_disease
                               breathing_disease
                               fever
                               temperature_cat
                               heart_disease
                               immunosuppressant_drug
                               immunosuppressant_disease]
                        :as   data}
                       algo_version]
  (let [temperature_cat
        ;; Fix temperature_cat wrt fever
        (if (or (false? fever) (= fever 999))
          "NSP" temperature_cat)
        ;; Fix tiredness_details wrt tiredness
        tiredness_details
        (if tiredness tiredness_details false)
        ;; Set fever_algo (as fever may be nil for >=2020-04-17)
        fever_algo
        (or (= fever 999)
            (and (or (true? fever) (nil? fever))
                 (string? (#{"inf_35.5" "sup_39" "NSP"} temperature_cat))))
        immunosuppressant_disease_algo (true? immunosuppressant_disease)
        immunosuppressant_drug_algo    (true? immunosuppressant_drug)
        heart_disease_algo             (not (false? heart_disease)) ;; True when "999"
        major-severity-factors         (atom 0)
        minor-severity-factors         (atom 0)
        pronostic-factors              (atom 0)]
    ;; Compute pronostic factors
    (when (= age_range
             (if (= algo_version "2020-05-09") "sup_65" "sup_70"))
      (swap! pronostic-factors inc))
    (when (>= imc 30) (swap! pronostic-factors inc))
    (when heart_disease_algo (swap! pronostic-factors inc))
    (when immunosuppressant_disease_algo (swap! pronostic-factors inc))
    (when immunosuppressant_drug_algo (swap! pronostic-factors inc))
    (when kidney_disease (swap! pronostic-factors inc))
    (when liver_disease (swap! pronostic-factors inc))
    (when breathing_disease (swap! pronostic-factors inc))
    (when cancer (swap! pronostic-factors inc))
    (when diabetes (swap! pronostic-factors inc))
    (when (and pregnant (not (= pregnant 888)))
      (swap! pronostic-factors inc))
    ;; Compute minor-severity-factors
    (when fever_algo (swap! minor-severity-factors inc))
    (when (and tiredness tiredness_details)
      (swap! minor-severity-factors inc))
    ;; Compute major-severity-factors
    (when breathlessness (swap! major-severity-factors inc))
    (when feeding_day (swap! major-severity-factors inc))
    ;; Return complete data
    (merge data
           {:temperature_cat                temperature_cat
            :tiredness_details              tiredness_details
            :fever_algo                     fever_algo
            :heart_disease_algo             heart_disease_algo
            :immunosuppressant_disease_algo immunosuppressant_disease_algo
            :immunosuppressant_drug_algo    immunosuppressant_drug_algo
            :major-severity-factors         @major-severity-factors
            :minor-severity-factors         @minor-severity-factors
            :pronostic-factors              @pronostic-factors})))

(defn orientation-2020-04-06
  [{:keys [age_range fever_algo diarrhea cough
           sore_throat_aches agueusia_anosmia
           pronostic-factors
           major-severity-factors
           minor-severity-factors]}]
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
    "orientation_surveillance"))

(defn orientation-2020-04-17
  [{:keys [age_range
           fever_algo diarrhea cough
           sore_throat_aches agueusia_anosmia
           pronostic-factors
           major-severity-factors
           minor-severity-factors]}]
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
    "orientation_surveillance"))

(defn orientation-2020-04-29
  [{:keys [age_range
           fever_algo diarrhea cough
           sore_throat_aches agueusia_anosmia
           pronostic-factors
           major-severity-factors
           minor-severity-factors]}]
  (cond
    ;; Branche 1
    (= age_range "inf_15")
    "less_15"
    ;; Branche 2
    (>= major-severity-factors 1)
    "SAMU"
    ;; Branche 3
    (and fever_algo cough)
    (cond (= pronostic-factors 0)
          "consultation_surveillance_3"
          (>= pronostic-factors 1)
          (if (< minor-severity-factors 2)
            "consultation_surveillance_3"
            "consultation_surveillance_2"))
    ;; Branche 4
    (or fever_algo
        diarrhea
        (and cough sore_throat_aches)
        (and cough agueusia_anosmia)
        (and sore_throat_aches agueusia_anosmia))
    (cond (= pronostic-factors 0)
          (if (= minor-severity-factors 0)
            (if (= age_range "from_15_to_49")
              "domicile_surveillance_1"
              "consultation_surveillance_1")
            "consultation_surveillance_1")
          (>= pronostic-factors 1)
          (if (< minor-severity-factors 2)
            "consultation_surveillance_1"
            "consultation_surveillance_2"))
    ;; Branche 5
    (or (and cough (not sore_throat_aches) (not agueusia_anosmia))
        (and (not cough) sore_throat_aches (not agueusia_anosmia))
        (and (not cough) (not sore_throat_aches) agueusia_anosmia))
    (if (= pronostic-factors 0)
      "domicile_surveillance_1"
      "consultation_surveillance_4")
    ;; Branche 6
    (and (not cough)
         (not sore_throat_aches)
         (not agueusia_anosmia))
    "surveillance"))
