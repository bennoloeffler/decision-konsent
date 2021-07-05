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
(defn p-count [compare-to-target-val target-val]
  (fn [test-val] (partial (compare-to-target-val (count test-val) target-val))))

(defn check [transform-test-val compare-to-target-val target-val]
  (fn [test-val] (partial (compare-to-target-val (transform-test-val test-val) target-val))))

(def email-regex #"^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,63}$")
(s/def ::email-type (s/and string? #(re-matches email-regex %)))
(defn email? [email] (s/valid? ::email-type email))

(s/def ::id int?)
(s/def ::short-name (s/and string? (p-count < 20)))
(s/def ::owner ::email-type)
(s/def ::problem-statement string?)
(s/def ::text string?)
(s/def ::participant ::email-type)
(s/def ::timestamp int?)
(s/def ::text-participant-timestamp (s/keys :req-un [::text
                                                     ::participant
                                                     ::timestamp]))
(s/def ::vote-value #("yes" "major-concern" "minor-concern" "veto" "abstain"))
(s/def ::vote (s/keys :req-un [::vote-value]))
(s/def ::participants (s/and not-empty (s/coll-of ::participant)))
(s/def ::allowed-proposers ::participants)
(s/def ::discussion (s/coll-of ::text-participant-timestamp))
(s/def ::proposal ::text-participant-timestamp)
(s/def ::question ::text-participant-timestamp)
(s/def ::answer ::text-participant-timestamp)
(s/def ::one-q-optional-answer (s/keys :req-un [::question]
                                       :opt-un [::answer]))
(s/def ::q&a (s/coll-of ::one-q-optional-answer))
(s/def ::ready-to-vote ::text-participant-timestamp)
(s/def ::ready (s/coll-of ::ready-to-vote))
(s/def ::complete-votes (s/merge ::text-participant-timestamp ::vote))
(s/def ::votes (s/coll-of ::complete-votes))
(s/def ::force-vote boolean?)
(s/def ::force-incomplete-vote boolean?)
(s/def ::iteration (s/keys :req-un [::allowed-proposers]
                           :opt-un [::discussion
                                    ::proposal
                                    ::q&a
                                    ::ready
                                    ::force-vote
                                    ::votes
                                    ::force-incomplete-vote]))
;(s/def ::iterations (s/coll-of ::iteration))

(s/def ::konsent (s/keys :req-un [::short-name ::owner ::participants ::problem-statement ::timestamp ::iterations]))

(defn in-participants
  "the value or all the values of the collection are contained in participants"
  ; TODO: make consistency checks
  [konsent val-or-col])

(s/def ::kon-with-id (s/keys :req-un [::id ::konsent]))

#_(s/explain-data
    ::kon-with-id
    {:id      99
     :konsent {:short-name        "123"
               :owner             "bel@ab.de"
               :participants      ["ef@gh.ibm" "a@bc.de"]
               :problem-statement "xyz"
               :timestamp         12345433
               :iterations        [{:allowed-proposers ["a23@bc.de"]
                                    :discussion        [{:participant "as@df.de" :text "sdfsfd" :timestamp 123}
                                                        {:participant "as@df.de" :text "sdfsfd" :timestamp 123}]
                                    :proposal          {:participant "df@df.de" :text "sdfsfd" :timestamp 123}
                                    :q&a               [{:question {:participant "df@df.de" :text "sdfsfd" :timestamp 123}
                                                         :answer   {:participant "df@df.de" :text "sdfsfd" :timestamp 123}}]}]}})


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

  (s/valid? even? 9)                                        ; false or true
  (s/conform even? 10)                                      ; 10 or :clojure.spec.alpha/invalid
  (s/explain even? 10)                                      ; error text or Success!

  (s/valid? string? "abc")                                  ;; true
  (s/valid? #(> % 5) 10)                                    ;; true
  (s/valid? #{:club :diamond :heart :spade} :club)          ;; true

  ;----

  (s/def :deck/suit #{:club :diamond :heart :spade})
  (s/conform :deck/suit :club)

  (s/def ::suit #{:club :diamond :heart :spade})
  (s/conform ::suit :club)

  (use 'clojure.repl)
  (doc ::suit)

  ;----

  (s/def :num/big-even (s/and int? even? #(> % 1000)))
  (s/valid? :num/big-even :foo)                             ;; false
  (s/valid? :num/big-even 10)                               ;; false
  (s/valid? :num/big-even 100000)                           ;; true

  (s/def :domain/name-or-id (s/or :name string?
                                  :id int?))
  (s/valid? :domain/name-or-id "abc")                       ;; true
  (s/valid? :domain/name-or-id 100)                         ;; true
  (s/valid? :domain/name-or-id :foo)                        ;; false

  ;----

  (s/conform :domain/name-or-id "abc")
  ;;=> [:name "abc"]
  (s/conform :domain/name-or-id 100)
  ;;=> [:id 100]

  ;----

  (s/valid? (s/nilable string?) nil)

;----

  (s/def :my.config/port number?)
  (s/def :my.config/host string?)
  (s/def :my.config/id keyword?)
  (s/def :my.config/server (s/keys* :req [:my.config/id :my.config/host]
                                    :opt [:my.config/port]))
  (s/conform :my.config/server [:my.config/id :s1
                                :my.config/host "example.com"
                                :my.config/port 5555]))
;;=> #:my.config{:id :s1, :host "example.com", :port 5555}





(def message-schema
  [[:message
    st/required
    st/string
    {:message  "message must contain at least one character"
     :validate (fn [msg] (>= (count msg) 0))}]])

(defn validate-message [params]
  (first (st/validate params message-schema)))