(ns specs
  (:require [clojure.spec.alpha :as s]
            [clojure.edn :as edn]
            [java-time :as t]))

(s/def ::nsp #{"0" "1" "999"})
(s/def ::myboolean #{"1" "0" "true" "false"})

(s/def ::algo_version #(re-matches #"^\d{4}-\d{2}-\d{2}$" %))
(s/def ::form_version #(re-matches #"^\d{4}-\d{2}-\d{2}$" %))
(s/def ::postal_code #(re-matches #"^\d{2}.*$" %))
(s/def ::age_range #{"inf_15" "from_15_to_49" "from_50_to_69" "sup_70"})
(s/def ::date (s/and string? #(t/zoned-date-time %)))
(s/def ::duration (s/or :integer int?
                        :string-integer #(int? (edn/read-string %))))
(s/def ::imc (s/or :integer int?
                   :float float?
                   :string-integer #(int? (edn/read-string %))
                   :string-float #(float? (edn/read-string %))))
(s/def ::orientation #{"orientation_SAMU"
                       "orientation_domicile_surveillance_1"
                       "orientation_surveillance"
                       "orientation_consultation_surveillance_1"
                       "orientation_consultation_surveillance_2"
                       "orientation_consultation_surveillance_3"
                       "orientation_consultation_surveillance_4"})
(s/def ::immunosuppressant_drug ::nsp)
(s/def ::immunosuppressant_drug_algo ::myboolean)
(s/def ::immunosuppressant_disease ::nsp)
(s/def ::immunosuppressant_disease_algo ::myboolean)
(s/def ::heart_disease ::nsp)
(s/def ::heart_disease_algo ::myboolean)
(s/def ::fever ::nsp)
(s/def ::fever_algo ::myboolean)
(s/def ::temperature_cat #{"inf_35.5" "35.5-37.7"
                           "37.8-38.9" "sup_39" "NSP"})
(s/def ::pregnant #{"0" "1" "888"})
(s/def ::liver_disease ::myboolean)
(s/def ::kidney_disease ::myboolean)
(s/def ::breathlessness  ::myboolean)
(s/def ::feeding_day  ::myboolean)
(s/def ::breathing_disease  ::myboolean)
(s/def ::cancer ::myboolean)
(s/def ::diarrhea ::myboolean)
(s/def ::diabetes ::myboolean)

(s/def ::response-2020-04-06
  (s/keys :req-un [
                   ::algo_version
                   ::form_version
                   ::postal_code
                   ::fever
                   ::fever_algo
                   ::temperature_cat
                   ::age_range
                   ::date ::duration
                   ::imc ::orientation
                   ::immunosuppressant_drug
                   ::immunosuppressant_drug_algo
                   ::immunosuppressant_disease
                   ::immunosuppressant_disease_algo
                   ::heart_disease
                   ::heart_disease_algo
                   ::liver_disease
                   ::kidney_disease
                   ::cancer
                   ::pregnant
                   ::diabetes
                   ::diarrhea
                   ]))

(s/def ::response-2020-04-17
  (s/keys :req-un [
                   ::algo_version
                   ::form_version
                   ::postal_code
                   ::fever_algo
                   ::temperature_cat
                   ::age_range
                   ::date ::duration
                   ::imc ::orientation
                   ::immunosuppressant_drug
                   ::immunosuppressant_drug_algo
                   ::immunosuppressant_disease
                   ::immunosuppressant_disease_algo
                   ::heart_disease
                   ::heart_disease_algo
                   ::liver_disease
                   ::kidney_disease
                   ::cancer
                   ::pregnant
                   ::diabetes
                   ::diarrhea
                   ]))


(defn valid-response [r version]
  (condp = version
    "2020-04-06" (s/valid? ::response-2020-04-06 r)
    "2020-04-17" (s/valid? ::response-2020-04-17 r)))
