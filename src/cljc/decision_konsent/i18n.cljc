(ns decision-konsent.i18n
  [:require [taoensso.tempura :as tempura]
            [re-frame.core :as rf]])


(def missing-txt "MISSING")

(def my-tempura-dictionary
  {:de {:missing missing-txt                                ; Fallback for missing resources
        ;; k=konsent
        :k       {:new                           "neuen Konsent erstellen"
                  :login-then-start              "Erst einloggen.\nDann loslegen!"
                  :or-divider                    "ODER"
                  :short-name                    "Kurzbezeichnung"
                  :problem-description           "Problembeschreibung & Hintergrund-Infos"
                  :participants                  "Teilnehmer (E-Mails)"
                  :create-and-start              "Konsent erstellen und starten"
                  :create                        "neuen Konsent erstellen"
                  :all-my-konsents               "alle meine Konsents"
                  :example-bib                   "neue Bibliothek - oder nicht?"
                  :example-travel-rules          "Reisekosten Abrechnung - strikter oder freier?"
                  :example-coffee-machine        "Kaffee-Maschine defekt. Tausend Interessen. Entscheidung!"
                  :guest-not-allowed-to-del      "Sie sind als Gast eingeloggt.\nUm zu löschen brauchen Sie einen Account."
                  :delete?                       "Konsent\nlöschen?"
                  :guest                         "GAST"
                  :guest-login-to-create-konsent "Sie sind als Gast eingeloggt.\nUm Konsents zu erstellen, brauchen Sie einen Account\n"
                  :unused                        ""}
        ;; n=navigation in core
        :n       {:home         "Home"
                  :new-konsent  "Konsent erstellen"
                  :my-konsent   "Meine Konsents"
                  :about        "Über"
                  :btn-register "Account erstellen und dann..."
                  :unused       ""}

        :a       {:txt-login             "In Account einloggen"
                  :btn-login             "einloggen"
                  :txt-email-password    "mit E-Mail und Passwort"
                  :lbl-login-email       "E-Mail"
                  :lbl-login-password    "Passwort"
                  :btn-login-now         "jetzt einloggen"
                  :txt-register          "Account anlegen"
                  :lbl-register-password "Passwort wiederholen"
                  :btn-register-now      "Account jetzt anlegen"
                  :unused                ""}

        :example {:greet "Good day %1!"}}                   ; Note Clojure fn-style %1 args


   :en {:missing missing-txt

        :k       {:new                           "create new konsent"
                  :login-then-start              "first login.\nthen start."
                  :or-divider                    "OR"
                  :short-name                    "short name"
                  :problem-description           "problem & background"
                  :participants                  "participants (emails)"
                  :create-and-start              "create and start the konsent"
                  :create                        "create new konsent"
                  :all-my-konsents               "all my konsents"
                  :example-bib                   "new bib - or not..."
                  :example-travel-rules          "decision regarding new traveling rules"
                  :example-coffee-machine        "konsent about the new coffee machine"
                  :guest-not-allowed-to-del      "As guest, you are not\n allowed to delete.\nPlease register and login as user."
                  :delete?                       "delete\nkonsent?"
                  :guest                         "GUEST"
                  :guest-login-to-create-konsent "You are logged in as guest.\nPlease login to your account to create konsents.\n"
                  :unused                        ""}

        :n       {:home         "home"
                  :new-konsent  "new konsent"
                  :my-konsent   "my konsents"
                  :about        "about"
                  :btn-register "register account and then..."
                  :unused       ""}

        :a       {:txt-login                   "login to your account"
                  :btn-login                   "login"
                  :txt-email-password          "with your email and password"
                  :lbl-login-email             "email"
                  :lbl-login-password          "password"
                  :btn-login-now               "login now"
                  :txt-register                "create account"
                  :txt-register-email-password "register with a valid email and choose a password"
                  :lbl-register-password       "confirm password"
                  :btn-register-now            "create your account now"

                  :unused                      ""}




        :example
                 {:greet    "Hello %1"
                  :farewell "Goodbye %1"
                  :foo      "foo"
                  :bar      "bar"
                  :bar-copy :en.example/bar                 ; Can alias entries
                  :baz      [:div "This is a **Hiccup** form"]

                  ;; Can use arbitrary fns as resources
                  :qux      (fn [[arg1 arg2]] (str arg1 " and " arg2))}}})


(rf/reg-sub
  :gui-language
  (fn [db _]
    (-> db :gui-language)))


(def language-name {:en "EN" :de "DE"})

(rf/reg-sub
  :gui-language-show
  (fn [db _]
    (language-name (-> db :gui-language))))


(rf/reg-event-db
  :gui-language
  (fn [db [_ data]]
    (println data)
    (assoc db :gui-language data)))


(def opts {:dict my-tempura-dictionary})

(def show-dev-strings true)

;(def tr (partial tempura/tr opts [(or @(rf/subscribe [:gui-language]) :en)]))




(defn tr
  "first of all, searches for re-frame subscription [:gui-language]
    - if this is not available, then -> use :en
    - if it is available, then -> use it.
    - if key is missing AND show-dev-strings is set to true -> SHOW DEFAULT otherwise SHOW MISSING KEY
    - otherwise -> SHOW translation"
  [key & dev-string]
  (let [lang-db @(rf/subscribe [:gui-language])
        lang    (or lang-db :en)                            ; make in work in repl too
        string  (tempura/tr opts [lang] key)
        final   (if (= string missing-txt)
                  (if (and show-dev-strings (first dev-string))
                    [:div (first dev-string) [:span {:style {:color :red}} "|"]]
                    ;(str (first default-string) " | **DEFAULT**")
                    (str key " -> missing..."))
                  string)]
    (println "i18n: " final)
    final))

(comment (tr [:konsent/create-new]))
