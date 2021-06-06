(ns decision-konsent.konsent-fsm-test
  (:require [clojure.test :refer :all])
  (:require [decision-konsent.konsent-fsm :refer :all]))


(def konsent-initialized
  {:id      15
   :konsent {:short-name        "sn"
             :owner             "o1"
             :problem-statement "p1"
             :participants      #{"p2" "p3" "p4"}
             :timestamp         1234
             :iterations        []}})


(def konsent-started
  (update-in konsent-initialized
             [:konsent :iterations]
             conj
             {:allowed-proposers #{"o1" "p2" "p3" "p4"}
              :discussion        []
              :q&a               []
              :votes             []
              :ready             #{}}))


(def konsent-discussed
  (assoc-in konsent-started
            [:konsent :iterations 0 :discussion]
            [{:text "m1" :participant "p1" :timestamp 1234}
             {:text "m2" :participant "p2" :timestamp 12345}
             {:text "m3" :participant "p3" :timestamp 12346}]))


(def konsent-proposed
  (assoc-in konsent-discussed
            [:konsent :iterations 0 :proposal]
            {:text        "s1"
             :participant "o2"
             :timestamp   12345}))


(def konsent-asked-answered
  (assoc-in konsent-proposed
            [:konsent :iterations 0 :q&a]
            [{:question {:text "q1" :participant "p2" :timestamp 1234}
              :answer   {:text "a1" :participant "o2" :timestamp 1234}}]))


(def konsent-all-marked-ready
  (assoc-in konsent-asked-answered
            [:konsent :iterations 0 :ready]
            #{"o1" "p2" "p3" "p4"}))


(def konsent-not-all-ready-yet
  (assoc-in konsent-asked-answered
            [:konsent :iterations 0 :ready]
            #{"o1" "p2" "p3"}))


(def konsent-proposer-forced-vote
  (assoc-in konsent-asked-answered
            [:konsent :iterations 0 :force-vote]
            true))


(def konsent-voted-veto
  (assoc-in konsent-all-marked-ready
            [:konsent :iterations 0 :votes]
            [{:participant "o1" :vote :abstain :text "" :timestamp 12345}
             {:participant "p2" :vote :minor-concern :text "c1" :timestamp 12345}
             {:participant "p3" :vote :major-concern :text "c2-xyx" :timestamp 12345}
             {:participant "p4" :vote :veto :text "c3-xyx" :timestamp 12345}]))


(def konsent-voted-veto-twice
  (assoc-in konsent-all-marked-ready
            [:konsent :iterations 0 :votes]
            [{:participant "o1" :vote :abstain :text "" :timestamp 12345}
             {:participant "p2" :vote :veto :text "c1" :timestamp 12345}
             {:participant "p3" :vote :major-concern :text "c2-xyx" :timestamp 12345}
             {:participant "p4" :vote :veto :text "c3-xyx" :timestamp 12345}]))


(def konsent-voted-major
  (assoc-in konsent-all-marked-ready
            [:konsent :iterations 0 :votes]
            [{:participant "o1" :vote :abstain :text "" :timestamp 12345}
             {:participant "p2" :vote :minor-concern :text "c1" :timestamp 12345}
             {:participant "p3" :vote :major-concern :text "c2-xyx" :timestamp 12345}
             {:participant "p4" :vote :yes :text "c3-xyx" :timestamp 12345}]))


(def konsent-voted-minor
  (assoc-in konsent-all-marked-ready
            [:konsent :iterations 0 :votes]
            [{:participant "o1" :vote :abstain :text "" :timestamp 12345}
             {:participant "p2" :vote :minor-concern :text "c1" :timestamp 12345}
             {:participant "p3" :vote :yes :text "c2-xyx" :timestamp 12345}
             {:participant "p4" :vote :yes :text "c3-xyx" :timestamp 12345}]))


(def konsent-vote-incomplete
  (-> konsent-all-marked-ready
      (assoc-in
        [:konsent :iterations 0 :votes]
        [{:participant "o1" :vote :abstain :text "" :timestamp 12345}
         {:participant "p2" :vote :minor-concern :text "c1" :timestamp 12345}
         {:participant "p3" :vote :yes :text "c2-xyx" :timestamp 12345}])))


(def konsent-vote-force-incomplete-voting-major
  (-> konsent-all-marked-ready
      (assoc-in
        [:konsent :iterations 0 :votes]
        [{:participant "o1" :vote :abstain :text "" :timestamp 12345}
         {:participant "p2" :vote :major-concern :text "c1" :timestamp 12345}
         {:participant "p3" :vote :yes :text "c2-xyx" :timestamp 12345}])
      (assoc-in
        [:konsent :iterations 0 :force-incomplete-vote]
        true)))


(def konsent-vote-force-incomplete-voting-minor
  (-> konsent-all-marked-ready
      (assoc-in
        [:konsent :iterations 0 :votes]
        [{:participant "o1" :vote :abstain :text "" :timestamp 12345}
         {:participant "p2" :vote :minor-concern :text "c1" :timestamp 12345}
         {:participant "p3" :vote :yes :text "c2-xyx" :timestamp 12345}])
      (assoc-in
        [:konsent :iterations 0 :force-incomplete-vote]
        true)))


;;
;;-------------------- print for debugging -----------------------------------------------------------------------------
;;


(defn pr-user-a [k]
  (println (next-actions-all-user k))
  (println)
  k)


(defn pr-missing [k]
  (println "missing ready: " (users-missing-for-ready-to-vote k))
  (println "missing  vote: " (users-with-missing-votes k))
  k)


;;
;;-------------------- tests that are like integration tests -----------------------------------------------------------------------------
;;


;;helper to understand current state of konsent
(deftest test-all-the-helpers
  (is (active-iteration konsent-started))
  (is (active-iteration konsent-discussed))
  (is (= #{"o1" "p2" "p3" "p4"} (all-members konsent-discussed)))
  (is (not (all-ready? konsent-discussed)))
  (is (all-ready? konsent-all-marked-ready))
  (is (user-is-proposer? konsent-proposed "o2"))
  (is (not (user-is-proposer? konsent-proposed "o")))
  (is (user-voted? konsent-voted-veto "p2"))
  (is (not (user-voted? konsent-voted-veto "p6")))
  (is (voted? konsent-voted-veto))
  (is (not (voted? konsent-vote-incomplete)))
  (is (voted? konsent-vote-force-incomplete-voting-minor))
  (is (= #{"o1" "p2" "p3" "p4"} (voters-set konsent-voted-major)))
  (is (not (major? konsent-vote-incomplete)))
  (is (major? konsent-voted-major))
  (is (veto? konsent-voted-veto))
  (is (voted? konsent-vote-force-incomplete-voting-major)))


;; get possible actions based on current state of konsent and the current user
(deftest test-possible-next-actions
  (is (= #{:accepted} (next-actions konsent-voted-minor "u")))
  (is (= #{:next-iteration} (next-actions konsent-vote-force-incomplete-voting-major "u")))
  (is (= #{:accepted} (next-actions konsent-vote-force-incomplete-voting-minor "u")))
  (is (= #{:next-iteration} (next-actions konsent-voted-major "u")))
  (is (= #{:propose :discuss} (next-actions konsent-started "o1")))
  (is (= #{:propose :discuss} (next-actions konsent-discussed "p2")))
  (is (= #{:ask} (next-actions konsent-proposed "u")))
  (is (= #{:answer :force-vote} (next-actions konsent-proposed "o2")))
  (is (= #{:answer :force-vote} (next-actions konsent-asked-answered "o2")))
  (is (= #{:ask} (next-actions konsent-asked-answered "o")))
  (is (= #{:vote} (next-actions konsent-all-marked-ready "o")))
  (is (= #{:force-vote :answer} (next-actions konsent-not-all-ready-yet "o2")))
  (is (= #{} (next-actions konsent-not-all-ready-yet "p2")))
  (is (= #{:ask} (next-actions konsent-not-all-ready-yet "p4")))
  ;(is (= #{:force-incomplete-vote} (next-actions konsent-all-marked-ready "o2")))
  (is (= #{:vote} (next-actions konsent-proposer-forced-vote "p2"))))


;; for switch iteration - understand the ones who are able to propose in next iteration
(deftest test-next-proposers
  (is (= #{"o1" "p2" "p3" "p4"} (who-proposes-in-next-iteration konsent-initialized)))
  (is (= nil (who-proposes-in-next-iteration konsent-started)))
  (is (= #{"o2"} (who-proposes-in-next-iteration konsent-voted-major)))
  (is (= #{"p2" "p4"} (who-proposes-in-next-iteration konsent-voted-veto-twice)))
  (is (= #{"p4"} (who-proposes-in-next-iteration konsent-voted-veto)))
  (is (= #{"o2"} (who-proposes-in-next-iteration konsent-vote-force-incomplete-voting-major)))
  (is (= nil (who-proposes-in-next-iteration konsent-all-marked-ready)))
  (is (= nil (who-proposes-in-next-iteration konsent-voted-minor))))


;; change state of konsent
(deftest konsent-state-changes
  (is (= "user" (-> (discuss konsent-started "user" "something")
                    :konsent :iterations first :discussion first :participant)))
  (is (= "o1" (-> (propose konsent-started "o1" "something")
                  :konsent :iterations first :proposal :participant)))
  (is (= "user" (-> (ask konsent-proposed "user" "something?")
                    :konsent :iterations first :q&a first :question :participant)))
  (is (= "o2" (-> (answer konsent-proposed "o2" "something-a" 0)
                  :konsent :iterations first :q&a first :answer :participant)))
  (is (= true (-> (force-vote konsent-proposed "o2")
                  :konsent :iterations first :force-vote)))
  (is (-> (signal-ready-to-vote konsent-proposed "user")
          :konsent :iterations first :ready (contains? "user")))
  (is (-> (vote konsent-all-marked-ready "user" :yes "something")
          :konsent :iterations first :votes first :vote))
  (is (-> (force-incomplete-vote konsent-vote-incomplete "o2")
          :konsent :iterations first :votes first :vote)))


(deftest konsent-force-at-the-end
  ;; this test does only fail, if the assertions
  ;; in discuss, propose, ask, answer, signal.. fail
  (-> (create-konsent "user" "konsent short name" "problem statement and other infos" ["p2" "p3"])
      ;pr-user-a
      (discuss "p2" "blabla1")
      ;pr-user-a
      (discuss "p2" "blabla2")
      ;pr-user-a
      (propose "p2" "my proposal")
      ;pr-user-a
      (ask "user" "q1")
      ;pr-user-a
      (ask "p3" "q2")
      ;pr-user-a
      (answer "p2" "a2" 1)
      ;pr-user-a
      (answer "p2" "a1" 0)
      ;pr-user-a
      (signal-ready-to-vote "p3")
      ;pr-user-a
      (signal-ready-to-vote "user")
      ;pr-user-a
      (vote "p3" :minor-concern "so far so good...")
      ;pr-user-a
      (force-incomplete-vote "p2")))


(deftest konsent-second-iteration
  ;; this test does only fail, if the assertions
  ;; in discuss, propose, ask, answer, signal.. fail
  (-> (create-konsent "user" "konsent short name" "problem statement and other infos" ["p2" "p3"])
      ;pr-user-a
      (discuss "p2" "blabla1")
      (discuss "p2" "blabla2")
      (propose "p2" "my proposal")
      (ask "user" "q1")
      (ask "p3" "q2")
      (answer "p2" "a2" 1)
      (answer "p2" "a1" 0)
      ;(signal-ready-to-vote "p3")
      (signal-ready-to-vote "user")
      ;pr-user-a
      (force-vote "p2")
      ;pr-user-a
      (vote "p3" :veto "so far so good...")
      ;pr-missing
      ;pr-user-a
      (force-incomplete-vote "p2")
      ;pr-user-a
      (discuss "p2" "blabla1")
      (discuss "p2" "blabla2")
      (propose "p3" "other proposal")
      (ask "user" "q1")
      (ask "p2" "q2")
      (answer "p3" "a2" 1)
      (answer "p3" "a1" 0)
      (signal-ready-to-vote "p2")
      (signal-ready-to-vote "user")
      (vote "p2" :minor " 1so far so good...")
      (vote "user" :minor " 2so far so good...")))



;;
;;-------------------- single functions test -----------------------------------------------------------------------------
;;


(deftest nil-if-empty-test
  (is (nil? (nil-if-empty []))))


(deftest active-iteration-test
  (testing "nothing in"
    (is (nil? (active-iteration {}))))
  (testing "some and get last"
    (is (= {:is :last} (active-iteration {:konsent {:iterations [{} {} {:is :last}]}})))))

