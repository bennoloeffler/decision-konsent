(ns decision-konsent.validation
  (:require [struct.core :as st]
            [clojure.spec.alpha :as s]))
            ;[valip.predicates :as v]))

;; https://github.com/weavejester/valip/blob/master/src/valip/predicates.clj
#_(comment (v/valid-email-domain? "benno@gmx.eueu")
           (v/email-address? "ben_1no@gmx.comco"))

#_(defn valid-email-with-domain?
    "checks form of email adress and DNS of domain"
    [mail]
    (and (v/valid-email-domain? mail)
         (v/email-address? mail)))

(defn email? [email] (and email (re-matches #".+\@.+\..+" email)))

;; https://www.bradcypert.com/an-informal-guide-to-clojure-spec/

;(s/def ::id string?)
;(s/valid? ::id "ABC-123")

(def id-regex #"^[0-9]*$")
(s/def ::id int?)
(s/def ::id-regex
  (s/and
    string?
    #(re-matches id-regex %)))
(s/def ::id-types (s/or ::id ::id-regex))

(s/valid? ::id-types "12345")
(s/valid? ::id-types 12435)

(s/explain ::id-types "Wrong!")

(s/def ::name string?)
(s/def ::age int?)
(s/def ::skills list?)
(s/def ::developer (s/keys :req [::name ::age]
                           :opt [::skills]))
(s/def ::developer-un (s/keys :req-un [::name ::age]
                              :opt-un [::skills]))
(s/valid? ::developer {::name "Brad" ::age 24 ::skills '()})
(s/valid? ::developer-un {:name "Brad" :age 24 :skills '()}) ;; not namespaced



(def message-schema
  [[:message
    st/required
    st/string
    {:message  "message must contain at least one character"
     :validate (fn [msg] (>= (count msg) 0))}]])

(defn validate-message [params]
  (first (st/validate params message-schema)))