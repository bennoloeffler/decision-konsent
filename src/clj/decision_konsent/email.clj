(ns decision-konsent.email                                  ; SEE https://github.com/drewr/postal
  (:require [postal.core :refer [send-message]]))

;decision-konsent@gmx.de
;d_ecision-konsen-T


(def server {:host "mail.gmx.net"
             :user "decision-konsent@gmx.de"
             :pass "d_ecision-konsen-T"
             :port 587
             :tls  true})


(defn send-mail! [body-html body-text subject receiver]
 (let [mail-data {:from    "decision-konsent@gmx.de"
                  :to      receiver
                  :subject subject
                  :body    [:alternative
                            {:type    "text/plain"
                             :content body-text}
                            {:type    "text/html"
                             :content body-html}]}
       send-result (send-message server mail-data)]))
  ;(println "sent: " mail-data)
  ;(println "result: " send-result)))

(comment (send-mail! "<html><head> </head><body><h1>Heading 1</h1><p>This is a test.</p></body></html>"
                     "Notfall-Text" "eine Testmail" "loeffler@v-und-s.de"))


(defn big-random [] (str (* 1N
                            (rand-int Integer/MAX_VALUE)
                            (rand-int Integer/MAX_VALUE)
                            (rand-int Integer/MAX_VALUE)
                            (rand-int Integer/MAX_VALUE))))


(defn create-invitation [email server-name server-port konsent-id]
  (let [invitation-id  (big-random)
        invitation-url (str "http://"
                            server-name
                            (if (contains? #{"80" 80 nil ""} server-port)
                              ""
                              (str ":" server-port))
                            "/api/konsent/invitation/"
                            invitation-id "/"
                            konsent-id)]
    {:invitation-url invitation-url
     :invitation-id  invitation-id
     :guest-email    email}))

(comment (create-invitation "bel@loef.de" "konsent-app.com" 88 27)
         (create-invitation "bel@loef.de" "konsent-app.com" nil 27))

(defn create-invitations [konsent server-name server-port]
  (let [participants  (-> konsent :konsent :participants)
        invitations-v (map
                        (fn [email]
                          (let [invitation (create-invitation email
                                                              server-name
                                                              server-port
                                                              (:id konsent))]
                            [(keyword (:invitation-id invitation)) invitation])) participants)
        invitations   (into {} invitations-v)]
    (assoc-in konsent [:konsent :invitations] invitations)))

(comment (create-invitations {:id 17 :konsent {:participants ["bel@soso" "karl@immutant.de"]}}
                             "dec-kon.com" 8080))

(defn send-invitation! [invitation owner]
  ;(println "invitation: " invitation)
  (send-mail! (str "<html><head> </head><body><h1>this is the link to the konsent,<br> you have been invited by: "
                   owner "</h1><p>" (:invitation-url invitation) "</p></body></html>")
              "Please enable html to see the invitation!"
              "Please contribute to decision"
              (:guest-email invitation)))

(comment (send-invitation! (create-invitation "benno.loeffler@gmx.de" "konsent-app.com" nil 27) "hugo.h@gmx.de"))

(defn send-invitations! [konsent]
  ;(println "konsent: " konsent)
  ;(println "invitations: " (-> konsent :konsent :invitations vals))
  (let [owner (-> konsent :konsent :owner)
        iterations (-> konsent :konsent :invitations vals)]
    (loop [i (first iterations)
           r (next iterations)]
      (when i (send-invitation! i owner))
      (when r (recur (first r) (next r))))))



(comment (send-invitations! {:id      41,
                             :konsent {:invitations       {:955280395323058556552030487589495680 {:guest-email    "loeffler@v-und-s.de",
                                                                                                  :invitation-id  "955280395323058556552030487589495680",
                                                                                                  :invitation-url "http://localhost:3000/invitation/955280395323058556552030487589495680/41"}},
                                       :iterations        [{:allowed-proposers ["loeffler@v-und-s.de"
                                                                                "benno.loeffler@gmx.de"],
                                                            :discussion        [],
                                                            :q&a               [],
                                                            :ready             [],
                                                            :votes             []}],
                                       :owner             "benno.loeffler@gmx.de",
                                       :participants      ["loeffler@v-und-s.de"],
                                       :problem-statement "pb",
                                       :short-name        "sn",
                                       :timestamp         1624995708783}}))