(ns schema
  (:require [cheshire.core :as json]))

(def schema-2020-04-06
  {:title       "Spécification des données issues des questionnaires d'orientation Covid-19"
   :description "Spécification des données issues des questionnaires d'orientation Covid-19"
   :author      "Délégation numérique en santé - ministère des Solidarités et de la Santé"
   :contact     "mobilisation-covid@sante.gouv.fr"
   :contributor "Direction interministérielle du numérique"
   :version     "2020-04-17"
   :created     "2020-04-19"
   :updated     "2020-04-19"
   :homepage    "https://github.com/Delegation-numerique-en-sante/covid19-algorithme-orientation-schema"
   :example     "https://github.com/Delegation-numerique-en-sante/covid19-algorithme-orientation-schema/blob/master/exemple.csv"
   :fields
   [{:name        "id"
     :description "Un identifiant unique pour la réponse"
     :example     "1e0dfed7-503a-4f21-8517-f0d0081495ce"
     :type        "string"
     :format      "uuid"
     :constraints {:required true
                   :unique   true}}

    {:name        "algo_version"
     :description "Le numéro de version de l'algorithme d'orientation Covid-19"
     :example     "2020-04-17"
     :type        "date"
     :constraints {:required true}}

    {:name        "form_version"
     :description "Le numéro de version du formulaire de saisie"
     :example     "2020-04-17"
     :type        "date"
     :constraints {:required true}}

    {:name        "date"
     :description "La date de saisie du questionnaire"
     :example     "2020-04-02T05:24:57Z"
     :type        "datetime"
     :constraints {:required true}}

    {:name        "duration"
     :description "La durée de la saisie du questionnaire"
     :example     126
     :type        "integer"
     :constraints {:required true
                   :minimum  0}}

    {:name        "postal_code"
     :description "Le code postal du lieu de résidence actuel"
     :example     "75019"
     :type        "string"
     :constraints {:pattern  "^\\d.{4}$"
                   :required false}}

    {:name        "orientation"
     :description "L'identifiant du message d'orientation envoyé au répondant"
     :example     "orientation_SAMU"
     :type        "string"
     :constraints {:enum     ["orientation_SAMU"
                              "orientation_domicile_surveillance_1"
                              "orientation_consultation_surveillance_1"
                              "orientation_consultation_surveillance_2"
                              "orientation_consultation_surveillance_3"
                              "orientation_consultation_surveillance_4"
                              "orientation_surveillance"]
                   :required true}}

    {:name        "age_range"
     :description "La tranche d'âge dans laquelle se situe le répondant"
     :example     "from_15_to_49"
     :type        "string"
     :constraints {:enum     ["inf_15" "from_15_to_49"
                              "from_50_to_69" "sup_70"]
                   :required true}}

    {:name        "imc"
     :description "Indice de masse corporelle"
     :example     "29.2"
     :type        "number"
     :constraints {:required true
                   :minimum  10
                   :maximum  70}}

    {:name        "feeding_day"
     :description "Difficulté à boire ou se nourrir dans les dernières 24 heures"
     :example     true
     :type        "boolean"
     :constraints {:required true}}

    {:name        "breathlessness"
     :description "Apparition d'un manque de souffle inhabituel"
     :example     true
     :type        "boolean"
     :constraints {:required true}}

    {:name        "fever"
     :description "Réponse à la question posée sur la fièvre"
     :example     999
     :type        "number"
     ;; 999 correspond à "Je ne sais pas"
     :constraints {:enum     [0 1 999]
                   :required true}}

    {:name        "temperature_cat"
     :description "Température la plus élevée ces dernières 48 heures"
     :example     "35.5-37.7"
     :type        "string"
     :constraints {:enum     ["inf_35.5" "35.5-37.7"
                              "37.8-38.9" "sup_39" "NSP"]
                   :required true}}

    {:name        "fever_algo"
     :description "Calcul du facteur fièvre"
     :example     false
     :type        "boolean"
     :constraints {:required true}}

    {:name        "tiredness"
     :description "Fatigue"
     :example     true
     :type        "boolean"
     :constraints {:required true}}

    {:name        "tiredness_details"
     :description "Fatigue obligeant à rester alité plus de la moitié de la journée"
     :example     true
     :type        "boolean"
     :constraints {:required false}}

    {:name        "cough"
     :description "Toux"
     :example     true
     :type        "boolean"
     :constraints {:required true}}

    {:name        "agueusia_anosmia"
     :description "Perte de goût ou d'odorat"
     :example     true
     :type        "boolean"
     :constraints {:required true}}

    {:name        "sore_throat_aches"
     :description "Apparition de maux de gorge, douleurs musculaires ou courbatures inhabituelles"
     :example     true
     :type        "boolean"
     :constraints {:required true}}

    {:name        "diarrhea"
     :description "Diarrhée"
     :example     true
     :type        "boolean"
     :constraints {:required true}}

    {:name        "diabetes"
     :description "Diabète"
     :example     true
     :type        "boolean"
     :constraints {:required true}}

    {:name        "cancer"
     :description "Cancer en cours ou il y a moins de trois ans"
     :example     true
     :type        "boolean"
     :constraints {:required true}}

    {:name        "breathing_disease"
     :description "Maladie respiratoire ou suivi pneumologique"
     :example     true
     :type        "boolean"
     :constraints {:required true}}

    {:name        "kidney_disease"
     :description "Insuffisance rénale chronique dialysée"
     :example     true
     :type        "boolean"
     :constraints {:required true}}

    {:name        "liver_disease"
     :description "Maladie chronique du foie"
     :example     true
     :type        "boolean"
     :constraints {:required true}}

    {:name        "pregnant"
     :description "La personne est-elle enceinte"
     :example     888
     :type        "number"
     ;; 888 correspond à "Je ne sais pas"
     :constraints {:enum     [0 1 888]
                   :required true}}

    {:name        "heart_disease"
     :description "Maladie cardiaque"
     :example     999
     :type        "number"
     ;; 999 correspond à "Je ne sais pas"
     :constraints {:enum     [0 1 999]
                   :required true}}

    {:name        "heart_disease_algo"
     :description "Maladie cardiaque"
     :example     false
     :type        "boolean"
     :constraints {:required true}}

    {:name        "immunosuppressant_disease"
     :description "Maladie défenses immunitaires"
     :example     999
     :type        "number"
     ;; 999 correspond à "Je ne sais pas"
     :constraints {:enum     [0 1 999]
                   :required true}}

    {:name        "immunosuppressant_disease_algo"
     :description "Maladie défenses immunitaires"
     :example     false
     :type        "boolean"
     :constraints {:required true}}

    {:name        "immunosuppressant_drug"
     :description "Traitement immunodépresseur"
     :example     999
     :type        "number"
     ;; 999 correspond à "Je ne sais pas"
     :constraints {:enum     [0 1 999]
                   :required true}}

    {:name        "immunosuppressant_drug_algo"
     :description "Traitement immunodépresseur"
     :example     false
     :type        "boolean"
     :constraints {:required true}}]})

(def schema-2020-04-17
  (update schema-2020-04-06
          :fields #(remove (fn [f] (= (:name f) "fever")) %)))

(def schemas {"2020-04-06" schema-2020-04-06
              "2020-04-17" schema-2020-04-17})

(defn generate [& [version]]
  (spit "schema.json"
        (json/generate-string
         (get schemas (or version "2020-04-17"))
         {:pretty true})))
