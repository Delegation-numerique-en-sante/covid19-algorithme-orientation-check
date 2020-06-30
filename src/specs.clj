(ns specs
  (:require [clojure.spec.alpha :as s]
            [clojure.spec.gen.alpha :as gen]
            [const :as const]
            [java-time :as t]))

(def depts (into #{} (map #(subs % 0 2) const/postal-codes)))

(s/def ::algo_version
  (s/with-gen
    (s/and string? #(re-matches #"^\d{4}-\d{2}-\d{2}$" %))
    ;; Only generate valid values for 2020-05-10:
    #(s/gen #{"2020-05-10"})))

(s/def ::form_version ::algo_version)

(s/def ::postal_code
  (s/with-gen
    (s/and string? (s/or :full const/postal-codes
                         :dept depts
                         :obsc #(re-matches #"^\d.{4}$" %)
                         :null #{""}))
    #(s/gen (s/or :full const/postal-codes
                  :dept depts
                  :null #{""}))))

(s/def ::age_range
  (s/with-gen
    #{"inf_15" "from_15_to_49" "from_50_to_69" "sup_70"
      ;; For version >= 2020-05-10:
      "from_50_to_64" "sup_65"}
    ;; Only generate valid values for 2020-05-10:
    #(s/gen #{"inf_15" "from_15_to_49" "from_50_to_64" "sup_65"})))

(s/def ::date
  (s/with-gen
    (s/and string? #(t/instant %))
    #(s/gen
      (into
       #{} (map (fn [n]
                  (str (t/truncate-to
                        (t/plus (t/instant (t/zoned-date-time)) (t/days n))
                        :seconds)))
                (range 50))))))

(def duration-min 30)
(def duration-max 2000)

(s/def ::duration (s/and nat-int? #(> % duration-min) #(< % duration-max)))

(def imc-min 10)
(def imc-max 70)

(s/def ::imc
  (s/with-gen
    (s/or :integer (s/and nat-int? )
          ::float (s/and float? #(> % imc-min) #(< % imc-max)))
    #(s/gen #{30 28.7 29.2 31.9 34.2 26})))

(s/def ::id
  (s/with-gen
    (s/and string? #(re-matches #"^|[a-z0-9]{8}-([a-z0-9]{4}-){3}[a-z0-9]{12}$" %))
    #(s/gen (into #{} (for [_ (range 100)]
                        (str (java.util.UUID/randomUUID)))))))

(s/def ::orientation
  #{"orientation_moins_de_15_ans"
    "orientation_SAMU"
    "orientation_domicile_surveillance_1"
    "orientation_surveillance"
    "orientation_consultation_surveillance_1"
    "orientation_consultation_surveillance_2"
    "orientation_consultation_surveillance_3"
    "orientation_consultation_surveillance_4"
    ;; From 2020-04-29:
    "less_15"
    "SAMU"
    "home_surveillance"
    "surveillance"
    "consultation_surveillance_1"
    "consultation_surveillance_2"
    "consultation_surveillance_3"
    "consultation_surveillance_4"})

(s/def ::myboolean-nsp
  (s/with-gen
    (s/or :bool #{"false" "true"}
          :bin #{"0" "1"}
          :nsp #{"999"})
    #(s/gen #{"false" "true" "999"})))

(s/def ::fever ::myboolean-nsp)
(s/def ::heart_disease ::myboolean-nsp)
(s/def ::immunosuppressant_disease ::myboolean-nsp)
(s/def ::immunosuppressant_drug ::myboolean-nsp)

(s/def ::myboolean-na
  (s/with-gen
    (s/or :bool #{"false" "true"}
          :bin #{"0" "1"}
          :nsp #{"888"})
    #(s/gen #{"false" "true" "888"})))

(s/def ::pregnant ::myboolean-na)

(s/def ::temperature_cat
  #{"inf_35.5" "35.5-37.7" "37.8-38.9" "sup_39" "NSP"})

(s/def ::myboolean
  (s/with-gen
    (s/or :bool #{"false" "true"}
          :bin #{"0" "1"})
    #(s/gen #{"false" "true"})))

(s/def ::agueusia_anosmia ::myboolean)
(s/def ::breathing_disease ::myboolean)
(s/def ::breathlessness ::myboolean)
(s/def ::cancer ::myboolean)
(s/def ::cough ::myboolean)
(s/def ::diabetes ::myboolean)
(s/def ::diarrhea ::myboolean)
(s/def ::feeding_day  ::myboolean)
(s/def ::fever_algo ::myboolean)
(s/def ::heart_disease_algo ::myboolean)
(s/def ::immunosuppressant_disease_algo ::myboolean)
(s/def ::immunosuppressant_drug_algo ::myboolean)
(s/def ::kidney_disease ::myboolean)
(s/def ::liver_disease ::myboolean)
(s/def ::sore_throat_aches ::myboolean)
(s/def ::tiredness ::myboolean)
(s/def ::tiredness_details ::myboolean)

(s/def ::response-2020-04-06
  (s/keys :req-un [
                   ::age_range
                   ::agueusia_anosmia
                   ::algo_version
                   ::breathing_disease
                   ::breathlessness
                   ::cancer
                   ::cough
                   ::date
                   ::duration
                   ::diabetes
                   ::diarrhea
                   ::feeding_day
                   ::fever
                   ::fever_algo
                   ::form_version
                   ::heart_disease
                   ::heart_disease_algo
                   ::imc
                   ::orientation
                   ::immunosuppressant_disease
                   ::immunosuppressant_disease_algo
                   ::immunosuppressant_drug
                   ::immunosuppressant_drug_algo
                   ::kidney_disease
                   ::liver_disease
                   ::pregnant
                   ::sore_throat_aches
                   ::temperature_cat
                   ::tiredness
                   ::tiredness_details
                   ]
          :opt-un [::postal_code]))

(s/def ::response-2020-04-17
  (s/keys :req-un [
                   ::age_range
                   ::agueusia_anosmia
                   ::algo_version
                   ::breathing_disease
                   ::breathlessness
                   ::cancer
                   ::cough
                   ::date
                   ::duration
                   ::diabetes
                   ::diarrhea
                   ::feeding_day
                   ::fever_algo
                   ::form_version
                   ::heart_disease
                   ::heart_disease_algo
                   ::imc
                   ::immunosuppressant_disease
                   ::immunosuppressant_disease_algo
                   ::immunosuppressant_drug
                   ::immunosuppressant_drug_algo
                   ::kidney_disease
                   ::liver_disease
                   ::orientation
                   ::pregnant
                   ::sore_throat_aches
                   ::temperature_cat
                   ::tiredness
                   ::tiredness_details
                   ]
          :opt-un [::postal_code ::id]))

(defn generate-response [version]
  (s/gen
   (condp contains? version
     #{"2020-06-09"
       "2020-05-10"
       "2020-04-29"
       "2020-04-17"} ::response-2020-04-17
     "2020-04-06"    ::response-2020-04-06
     nil?)))

(defn generate-samples [version number]
  (or (->> (gen/sample (generate-response version) number)
           (remove nil?)
           not-empty)
      (println "Cannot generate sample for version" version)))

(defn valid-response [r version]
  (condp contains? version
    #{"2020-05-10"
      "2020-04-29"
      "2020-04-17"} (s/valid? ::response-2020-04-17 r)
    "2020-04-06"    (s/valid? ::response-2020-04-06 r)
    false))
