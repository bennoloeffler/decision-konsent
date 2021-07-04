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

(def email-regex #"^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,63}$")
(s/def ::email-type (s/and string? #(re-matches email-regex %)))
(defn email? [email] (s/valid? ::email-type email))

;; https://www.bradcypert.com/an-informal-guide-to-clojure-spec/

;(s/def ::id string?)
;(s/valid? ::id "ABC-123")
(comment
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


;; ----------------------  https:// clojure.org / guides/spec  -------------------------------

 (s/valid? even? 9); false or true
 (s/conform even? 10); 10 or :clojure.spec.alpha/invalid
 (s/explain even? 10); error text or Success!

 (s/valid? string? "abc")  ;; true
 (s/valid? #(> % 5) 10) ;; true
 (s/valid? #{:club :diamond :heart :spade} :club) ;; true

;----

 (s/def :deck/suit #{:club :diamond :heart :spade})
 (s/conform :deck/suit :club)

 (s/def ::suit #{:club :diamond :heart :spade})
 (s/conform ::suit :club)

 (use 'clojure.repl)
 (doc ::suit)

;----

 (s/def :num/big-even (s/and int? even? #(> % 1000)))
 (s/valid? :num/big-even :foo) ;; false
 (s/valid? :num/big-even 10) ;; false
 (s/valid? :num/big-even 100000) ;; true

 (s/def :domain/name-or-id (s/or :name string?
                                 :id int?))
 (s/valid? :domain/name-or-id "abc") ;; true
 (s/valid? :domain/name-or-id 100) ;; true
 (s/valid? :domain/name-or-id :foo) ;; false

;----

 (s/conform :domain/name-or-id "abc")
 ;;=> [:name "abc"]
 (s/conform :domain/name-or-id 100)
 ;;=> [:id 100]

;----

 (s/valid? (s/nilable string?) nil))








(def message-schema
  [[:message
    st/required
    st/string
    {:message  "message must contain at least one character"
     :validate (fn [msg] (>= (count msg) 0))}]])

(defn validate-message [params]
  (first (st/validate params message-schema)))