(ns decision-konsent.db.konsent-data-example)

; every event-like entity has a :timestamp 1233 (int=unix-timestamp of server)
; abbrevations for keys:
(def k :konsent)
(def o :owner)
(def pr :problem-statement)
(def t :timestamp)
(def is :iterations)
(def i :iteration)
(def d :discussion)
(def m :message)
(def ps :participants)
(def p :participant)
(def s :suggestion)
(def qs :questions)
(def q :question)
(def as :answers)
(def a :answer)
(def vs :votes)
(def v :vote) ; :yes :mic :mac :vet :abs (abstention = Enthaltung)
(def c :comment)

(println {:id 15 k {o "me"}})

(def konsent {:id 15
              :konsent {:owner "o1"
                        :problem-statement "p1"
                        :participants ["" "" ""]
                        :timestamp 1234
                        :iterations [{:discussion [{:message "m1" :participant "p1" :timestamp 1234}
                                                   {:message "m2" :participant "p2" :timestamp 12345}
                                                   {:message "m3" :participant "p3" :timestamp 12346}]
                                      :owner "o2"
                                      :suggestion "s1"
                                      :timestamp 1234
                                      :questions [{:question "q1" :participant "p2" :timestamp 1234}
                                                  {:question "q1" :participant "p2" :timestamp 1234}
                                                  {:question "q1" :participant "p2" :timestamp 1234}]
                                      :answers [{:answer "a1" :owner "o2" :timestamp 1234}
                                                {:answer "a1" :owner "o2" :timestamp 1234}
                                                {:answer "a1" :owner "o2" :timestamp 1234}]
                                      :votes [{:participant "p1" :vote :yes :comment ""}
                                              {:participant "p2" :vote :minor-concern :comment "c1"}
                                              {:participant "p3" :vote :major-concern :comment "c2-xyx"}
                                              {:participant "p4" :vote :veto :comment "c3-xyx"}]}

                                     ; next iteration - may start with discussion - or with suggestion.
                                     {:discussion [{:message "m1" :participant "p1" :timestamp 1234}
                                                   {:message "m2" :participant "p2" :timestamp 12345}
                                                   {:message "m3" :participant "p3" :timestamp 12346}]

                                      :owner "o3" ;changes only in case of veto
                                      :suggestion "s1"
                                      :timestamp 1234
                                      :questions [{:question "q1" :participant "p2" :timestamp 1234}
                                                  {:question "q1" :participant "p2" :timestamp 1234}
                                                  {:question "q1" :participant "p2" :timestamp 1234}]
                                      :answers [{:answer "a1" :owner "o2" :timestamp 1234}
                                                {:answer "a1" :owner "o2" :timestamp 1234}
                                                {:answer "a1" :owner "o2" :timestamp 1234}]
                                      :votes [{:participant "p1" :vote :yes :comment "" :timestamp 1234}
                                              {:participant "p2" :vote :minor-concern :comment "c1":timestamp 1234}
                                              {:participant "p3" :vote :major-concern :comment "c2-xyx" :timestamp 1234}
                                              {:participant "p4" :vote :veto :comment "c3-xyx" :timestamp 1234}]}]}})

(def k-short {:id 15
              k {o "o1"
                 pr "p1"
                 ps ["" "" ""]
                 t 1234
                 is [{d [{m "m1" p "p1" t 1234}
                         {m "m2" p "p2" t 12345}
                         {m "m3" p "p3" t 12346}]
                      o "o2"
                      s "s1"
                      t 1234
                      qs [{q "q1" p "p2" t 1234}
                          {q "q1" p "p2" t 1234}
                          {q "q1" p "p2" t 1234}]
                      as [{a "a1" o "o2" t 1234}
                          {a "a1" o "o2" t 1234}
                          {a "a1" o "o2" t 1234}]
                      vs [{p "p1" v :yes c ""}
                          {p "p2" v :minor-concern c "c1"}
                          {p "p3" v :major-concern c "c2-xyx"}
                          {p "p4" v :veto c "c3-xyx"}]}

                     ; next iteration - may start with discussion - or with suggestion.
                     {:discussion [{:message "m1" :participant "p1" :timestamp 1234}
                                   {:message "m2" :participant "p2" :timestamp 12345}
                                   {:message "m3" :participant "p3" :timestamp 12346}]

                      :owner "o3" ;changes only in case of veto
                      :suggestion "s1"
                      :timestamp 1234
                      :questions [{:question "q1" :participant "p2" :timestamp 1234}
                                  {:question "q1" :participant "p2" :timestamp 1234}
                                  {:question "q1" :participant "p2" :timestamp 1234}]
                      :answers [{:answer "a1" :owner "o2" :timestamp 1234}
                                {:answer "a1" :owner "o2" :timestamp 1234}
                                {:answer "a1" :owner "o2" :timestamp 1234}]
                      :votes [{:participant "p1" :vote :yes :comment "" :timestamp 1234}
                              {:participant "p2" :vote :minor-concern :comment "c1":timestamp 1234}
                              {:participant "p3" :vote :major-concern :comment "c2-xyx" :timestamp 1234}
                              {:participant "p4" :vote :veto :comment "c3-xyx" :timestamp 1234}]}]}})
