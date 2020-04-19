(ns algos
  (:require [clojure.edn :as edn]))

(defn normalize [^String s]
  (let [val-s (edn/read-string s)]
    (condp #(= %1 (type %2)) val-s
      java.lang.Boolean val-s
      java.lang.Long    (condp = val-s 1 true 0 false val-s))))

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
    (when (>= (normalize imc) 30) (swap! pronostic-factors inc))
    (when (normalize heart_disease_algo) (swap! pronostic-factors inc))
    (when (normalize immunosuppressant_disease_algo) (swap! pronostic-factors inc))
    (when (normalize immunosuppressant_drug_algo) (swap! pronostic-factors inc))
    (when (normalize kidney_disease) (swap! pronostic-factors inc))
    (when (normalize liver_disease) (swap! pronostic-factors inc))
    (when (normalize breathing_disease) (swap! pronostic-factors inc))
    (when (normalize cancer) (swap! pronostic-factors inc))
    (when (normalize diabetes) (swap! pronostic-factors inc))
    (when (and (normalize pregnant) (not (= (normalize pregnant) 999)))
      (swap! pronostic-factors inc))
    ;; Compute minor-severity-factors
    (when (normalize fever_algo) (swap! minor-severity-factors inc))
    (when (and (normalize tiredness) (normalize tiredness_details))
      (swap! minor-severity-factors inc))
    ;; Compute major-severity-factors
    (when (normalize breathlessness) (swap! major-severity-factors inc))
    (when (normalize feeding_day) (swap! major-severity-factors inc))
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
  (let [fever_algo        (normalize fever_algo)
        diarrhea          (normalize diarrhea)
        cough             (normalize cough)
        sore_throat_aches (normalize sore_throat_aches)
        agueusia_anosmia  (normalize agueusia_anosmia)]
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
  (let [fever_algo        (normalize fever_algo)
        diarrhea          (normalize diarrhea)
        cough             (normalize cough)
        sore_throat_aches (normalize sore_throat_aches)
        agueusia_anosmia  (normalize agueusia_anosmia)]
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
