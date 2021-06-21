(ns decision-konsent.konsent-fsm
  [:require
   ;[clojure.test :refer :all]
    [decision-konsent.unix-time :as ut]])
;[clojure.string :as str]
;[erdos.assert :as pa]])                                   ;power-assert
;[taoensso.timbre :refer [spy error warn info debug]] ;logging
;[clojure.tools.trace :as trace :refer [dotrace trace-forms]] ;tracing
;[debux.core :refer :all]                                ; dbg
;[hashp.core :refer :all]
;#p

#_(def konsent-complete
    {:id      15
     :konsent {:short-name        "sn"
               :owner             "o1"
               :problem-statement "p1"
               :participants      #{"p2" "p3" "p4"}
               :timestamp         1234


               :iterations        [{:allowed-proposers     ["o1" "p2" "p3" "p4"]
                                    :discussion            [{:text "m1" :participant "p1" :timestamp 1234}
                                                            {:text "m2" :participant "p2" :timestamp 12345}
                                                            {:text "m3" :participant "p3" :timestamp 12346}]
                                    :proposal              {:text        "s1"
                                                            :participant "o2"
                                                            :timestamp   12345}
                                    :q&a                   [{:question {:text "q1" :participant "p2" :timestamp 1234}
                                                             :answer   {:text "a1" :participant "o2" :timestamp 1234}}]
                                    :ready                 #{"p1" "p2"}
                                    :force-vote            false
                                    :votes                 [{:participant "o1" :vote :yes :text "" :timestamp 12345}
                                                            {:participant "p2" :vote :minor-concern :text "c1" :timestamp 12345}
                                                            {:participant "p3" :vote :major-concern :text "c2-xyx" :timestamp 12345}
                                                            {:participant "p4" :vote :veto :text "c3-xyx" :timestamp 12345}]
                                    :force-incomplete-vote true}

                                   ; next iteration - may start with discussion - or with proposal.
                                   {:discussion [{:text "m1" :participant "p1" :timestamp 1234}
                                                 {:text "m2" :participant "p2" :timestamp 12345}
                                                 {:text "m3" :participant "p3" :timestamp 12346}]

                                    :proposal   {:text        "s1"
                                                 :participant "o3" ;changes only in case of veto
                                                 :timestamp   12345}
                                    :q&a        [{:question {:text "q1" :participant "p2" :timestamp 1234}
                                                  :answer   {:text "a1" :participant "o2" :timestamp 1234}}]
                                    :ready      #{"p1" "p2"}

                                    :votes      [{:participant "p1" :vote :yes :text "" :timestamp 1234}
                                                 {:participant "p2" :vote :minor-concern :text "c1" :timestamp 1234}
                                                 {:participant "p3" :vote :major-concern :text "c2-xyx" :timestamp 1234}
                                                 {:participant "p4" :vote :veto :text "c3-xyx" :timestamp 1234}]}]}})


;;
;; ------------------------------------- helpers ----------------------------------------------------------------------
;;


(defn nil-if-empty
  "tiny helper "
  [s]
  (if (empty? s) nil s))


(defn active-iteration
  "return the latest iteration - the active one"
  [k]
  (-> k :konsent :iterations last nil-if-empty))


(defn all-members
  "return all involved email-adresses (owner + participants)"
  [k]
  (set (conj (-> k :konsent :participants) (-> k :konsent :owner))))



(defn all-signaled-ready?
  "did all members signal ready to vote?"
  [k]
  (= (-> (active-iteration k) :ready set)
     (all-members k)))


(defn proposer
  "who is the proposer of the active iteration?"
  [k]
  (-> (active-iteration k) :proposal :participant))


(defn proposal-made?
  "has an proposal been made by anybody in the active iteration?"
  [k]
  (-> (active-iteration k) :proposal))


(defn allowed-proposers
  "all users who are allowed to propose in the active iteration"
  [k]
  (let [p (-> (active-iteration k) :allowed-proposers)]
    ;(println "allowed proposers: " p)
    (-> (active-iteration k) :allowed-proposers)))


(defn allowed-to-propose?
  "is user u allowed to propose in the active iteration?"
  [k u]
  (contains? (set (allowed-proposers k)) u))


(defn user-is-proposer?
  "is the user u the proposer?"
  [k u]
  (= u (proposer k)))


(defn votes-set
  "returns a set of the different votes types that are available.
   e.g. to detect vetos and majors"
  [k]
  (->> (active-iteration k)
       :votes
       (map :vote)
       set))


(defn voters-set
  "returns the set of all voters.
   e.g. in order to check, if everybody voted."
  [k]
  (->> (active-iteration k)
       :votes
       (map :participant)
       set))


(defn veto-voters
  "all voters that voted veto"
  [k]
  (->> (active-iteration k)
       :votes
       (filter #(or (= :veto (:vote %)) (= "veto" (:vote %))))
       (map :participant)
       set
       nil-if-empty))


(defn user-ready-to-vote?
  "did the user signal his readiness ot vote already?"
  [k u]
  (contains? (set (-> (active-iteration k) :ready))
             u))


(defn user-voted?
  "did the user u vote already?"
  [k u]
  (contains? (voters-set k)
             u))


(defn voting-over?
  "is voting over - because everybody
   voted or the end of voting is forced?"
  [k]
  (or (= (voters-set k)
         (all-members k))
      (-> (active-iteration k) :force-incomplete-vote)))


(defn veto?
  "was a veto in last voting?"
  [k]
  (or (-> (votes-set k) (contains? "veto"))
      (-> (votes-set k) (contains? :veto))))


(defn major?
  "was a major concern in last voting?"
  [k]
  (or (-> (votes-set k) (contains? "major-concern"))
      (-> (votes-set k) (contains? :major-concern))))

(defn q&a-with-idx
  "returns questions with :idx, representing the
   vector position starting with 0.
   [{:question {:text \"q1\", :participant \"p2\"},
     :idx 0}
    {:question {:text \"q2\", :participant \"p2\"},
     :answer   {:text \"a2\", :participant \"p2\"},
     :idx 1}]"
  [iteration]
  (->> iteration
       :q&a
       (map #(into %2 {:idx %1}) (range))
       vec))


(defn q&a-with-answers
  "returns questions that are answered.
   with index, to be able to save (if change is needed).
   [{:question {:text \"q2\", :participant \"p2\"},
     :answer   {:text \"a2\", :participant \"p2\"}, :idx 1}]"
  [k]
  (->> (q&a-with-idx (active-iteration k))
       (filter :answer)
       vec))


(defn q&a-without-answers
  "returns questions that are not yet answered -
   with index, to be able to save answer.
   [{:question {:text \"q1\", :participant \"p2\"}, :idx 0}]"
  [k]
  (->> (q&a-with-idx (active-iteration k))
       (filter #(not (:answer %)))
       vec))


;;
;; ---------------------------------------- is this next state possible? ----------------------------------------------
;;


(defn accepted?
  "has the proposal been accepted.
   This is the end..."
  [k]
  (and (voting-over? k)
       (not (or (major? k) (veto? k)))))


(defn voting-started?
  "either everybody has signaled
   ready or it's started by force"
  [k]
  (or (all-signaled-ready? k)
      (-> (active-iteration k) :force-vote)))


(defn incomplete-voting-ongoing?
  "is an voting waiting for votes or force end?"
  [k]
  (and (not (accepted? k))
       (pos? (count (voters-set k)))                        ; there are voters
       (< (count (voters-set k)) (count (all-members k)))   ; not all voted
       (voting-started? k)))


(defn wait-for-other-voters?
  "wait - you proposed or voted
   and the voting is not yet over"
  [k u]
  (and (incomplete-voting-ongoing? k)
       (user-voted? k u)))


(defn next-iteration?
  "is there a next iteration?"
  [k]
  (or (not (active-iteration k))
      (and (voting-over? k)
           (not (accepted? k)))))


(defn vote?
  "can the user vote?"
  [k u]
  (and (not (user-is-proposer? k u))
       (not (user-voted? k u))
       (not (accepted? k))
       (not (next-iteration? k))
       (voting-started? k)))


(defn force-incomplete-vote?
  "can the user force to use the vote although its incomplete"
  [k u]
  (and (user-is-proposer? k u)
       (incomplete-voting-ongoing? k)))


(defn ask?
  "can the user ask questions?"
  [k u]
  (and (-> (active-iteration k) :proposal)
       (not (user-is-proposer? k u))
       (not (user-ready-to-vote? k u))
       (not (vote? k u))
       (not (user-voted? k u))
       (not (accepted? k))
       (not (next-iteration? k))))


(defn answer?
  "should the user answer questions?"
  [k u]
  (and (-> (active-iteration k) :proposal)
       (user-is-proposer? k u)
       (not (vote? k u))
       (not (accepted? k))
       (not (force-incomplete-vote? k u))))


(defn discuss?
  "can the user discuss?"
  [k u]
  (not (or (ask? k u)
           (answer? k u)
           (vote? k u)
           (accepted? k)
           (user-ready-to-vote? k u)
           (wait-for-other-voters? k u)
           (next-iteration? k)
           (force-incomplete-vote? k u))))


(defn propose?
  "can the user u make a proposal?"
  [k u]
  (and (discuss? k u) (allowed-to-propose? k u)))


;;
;; ---------- those, who not yet took action (signal ready, vote) ------------------------------------------------------
;;


(defn users-with-missing-votes
  "those members, who not yet voted"
  [k]
  (when (incomplete-voting-ongoing? k)
    (clojure.set/difference
      (set (all-members k))
      (voters-set k))))


(defn users-missing-for-ready-to-vote
  "those users, that are not yet signaled 'ready to vote'"
  [k]
  (when (and (proposal-made? k) (not (voting-started? k)))
    (clojure.set/difference
      (set (all-members k))
      (:ready (active-iteration k)))))

(defn users-ready-to-vote
  "those users, that have signaled 'ready to vote'"
  [k]
  (when (and (proposal-made? k) (not (voting-started? k)))
    (:ready (active-iteration k))))



(defn wait-for-other-users-to-be-ready
  "user u has to wait for voting, because others
   did not yet signal ready to vote."
  [k u]
  (and (proposal-made? k)
       (user-ready-to-vote? k u)
       (not (voting-started? k))))

;;
;; ---------- switching to next iteration! -----------------------------------------------------------------------------
;;


(defn who-proposes-in-next-iteration
  "delivers proposers in the next iteration:
   1. all participants, when before the first start (not an iteration yet started)
   2. vetoers, when iteration is finished with veto,
   3. proposer, when iteration is finished with majors
   4. nil in all other cases"
  [k]
  (if (and (active-iteration k) (next-iteration? k))
    (or (veto-voters k)
        #{(proposer k)})
    (if (not (active-iteration k))
      (all-members k)
      nil)))


(defn switch-to-next
  "switches to the next iteration and sets the right
   allowed-proposers based on last iteration."
  [k u]
  (if (next-iteration? k)
    (update-in k [:konsent :iterations]
               conj {:allowed-proposers (who-proposes-in-next-iteration k)
                     :discussion        []
                     :q&a               []
                     :votes             []
                     :ready             #{}})
    (throw (ex-info "ERROR - there is no next iteration in konsent.
                     You cannot switch to next..."
                    {:your-user u :your-konsent k}))))


(defn create-konsent
  "creates a konsent skeleton
   without a unique id {:id -1 :konsent {DATA}}
   but with first iteration.
   The server creates an id."
  [user short-name problem-statement participants]
  (switch-to-next {:id -1 :konsent {:short-name        short-name
                                    :owner             user
                                    :problem-statement problem-statement
                                    :participants      (set participants)
                                    :timestamp         (ut/current-time)
                                    :iterations        []}}
                  user))


(def all-actions
  "just to document all possible actions.
   next-action delivers a set of some of them for
   a specific user u.
   If the user just has to wait, the set is emtpy."
  #{:discuss
    :propose
    :ask
    :answer
    :force-vote
    :vote
    :force-incomplete-vote
    :accepted
    :next-iteration})
;; :wait is not missing. wait = #{}


(defn next-actions
  "returns a set of possible next actions
   based on the current state of
   konsent k and of the user u"
  [k u]
  (cond-> #{}
          (discuss? k u) (conj :discuss)
          (propose? k u) (conj :propose)
          (ask? k u) (conj :ask)
          (answer? k u) (conj :answer :force-vote)
          (vote? k u) (conj :vote)
          (force-incomplete-vote? k u) (conj :force-incomplete-vote)
          (accepted? k) (conj :accepted)
          (next-iteration? k) (conj :next-iteration)))


(defn next-actions-all-user
  "returns an list of maps with the
   next possible actions of all users"
  [k]
  (for [u (all-members k)] {u (next-actions k u)}))


;;
;; ---------- change konsent and return it ----------
;;


(defn discuss
  "provides a reflection, question, answer, option,... from the user"
  [k u text]
  (println "user: " u)
  (println "next-action of user: " (next-actions k u))
  (println "all actions: " (next-actions-all-user k))
  (assert (contains? (next-actions k u) :discuss))
  (let [current-iteration-idx (-> k :konsent :iterations count dec)]
    (update-in k [:konsent :iterations current-iteration-idx :discussion]
               conj {:participant u :text text :timestamp (ut/current-time)})))


(defn propose
  "user places his proposal to all other participants"
  [k u text]
  (assert (contains? (next-actions k u) :propose))
  (let [current-iteration-idx (-> k :konsent :iterations count dec)]
    (-> k
        (assoc-in [:konsent :iterations current-iteration-idx :proposal]
                  {:participant u :text text :timestamp (ut/current-time)})
        (assoc-in [:konsent :iterations current-iteration-idx :ready]
                  #{u})
        (assoc-in [:konsent :iterations current-iteration-idx :votes]
                  [{:participant u :vote "yes" :timestamp (ut/current-time)}]))))


(defn ask
  "after a proposal hast placed, ask for understanding"
  [k u text]
  (assert (contains? (next-actions k u) :ask))
  (let [current-iteration-idx (-> k :konsent :iterations count dec)]
    (update-in k [:konsent :iterations current-iteration-idx :q&a]
               conj {:question {:participant u :text text :timestamp (ut/current-time)}})))


(defn signal-ready-to-vote
  "signal that the user has no more questions.
   this may be done in 'mode :ask'"
  [k u]
  (assert (contains? (next-actions k u) :ask))
  (let [current-iteration-idx (-> k :konsent :iterations count dec)]
    (update-in k [:konsent :iterations current-iteration-idx :ready]
               conj u)))


(defn answer
  "the proposer gets questions by 'ask'
   and should answer them."
  [k u text idx-question]
  (assert (contains? (next-actions k u) :answer))
  (let [current-iteration-idx (-> k :konsent :iterations count dec)]
    (assoc-in k [:konsent :iterations current-iteration-idx :q&a idx-question :answer]
              {:participant u :text text :timestamp (ut/current-time)})))


(defn force-vote
  "after a proposal, people can ask.
   Or they can signal 'ready to vote'.
   If for some reason somebody does not
   signal ready, the proposer can start
   the voting anyway."
  [k u]
  (assert (contains? (next-actions k u) :force-vote))
  (let [current-iteration-idx (-> k :konsent :iterations count dec)]
    (assoc-in k [:konsent :iterations current-iteration-idx :force-vote]
              true)))


(defn- check-if-next-and-switch
  "helper for vote and force-imcomplete-vote
   just to switch, when the time has come."
  [k u]
  (cond
    (contains? (next-actions k u) :next-iteration) (switch-to-next k u)
    ;; QUESTION: if current vote contains veto - that may be reason enough to switch immediately...
    :default k))


(defn vote
  "give a voting. the-vote may be:
   :yes :minor-concern :major-concern :veto :abstain"
  [k u the-vote text]
  (assert (contains? (next-actions k u) :vote))
  (let [current-iteration-idx (-> k :konsent :iterations count dec)]
    (-> k (update-in [:konsent :iterations current-iteration-idx :votes]
                     conj {:participant u
                           :vote        (if (keyword? the-vote) ; - postgress...
                                          (subs (str the-vote) 1) ;replace keyword by string
                                          the-vote)
                           :text        text
                           :timestamp   (ut/current-time)})
        (check-if-next-and-switch u))))


(defn force-incomplete-vote
  "if voters just do not vote, for whatever reason,
   get the decision forward by just counting the votes
   that are available."
  [k u]
  (assert (contains? (next-actions k u) :force-incomplete-vote))
  (let [current-iteration-idx (-> k :konsent :iterations count dec)]
    (-> k (assoc-in [:konsent :iterations current-iteration-idx :force-incomplete-vote]
                    true)
        (check-if-next-and-switch u))))









