(ns decision-konsent.i18n
  [:require [taoensso.tempura :as tempura]
            [re-frame.core :as rf]])


(def missing-txt "MISSING")

(def my-tempura-dictionary
  {:de {:missing missing-txt                                ; Fallback for missing resources

        ;; common
        :com     {:e-g-mail "z.B. max.muster@beispiel.de"}

        ;; k=konsent
        :k       {:new                             "neuen Konsent erstellen"
                  :login-then-start                "Erst einloggen.\nDann loslegen!"
                  :or-divider                      "ODER"
                  :short-name                      "Kurzbezeichnung"
                  :problem-description             "Problembeschreibung & Hintergrund-Infos"
                  :participants-mail               "Teilnehmer (E-Mails)"
                  :create-and-start                "Konsent erstellen und starten"
                  :create                          "neuen Konsent erstellen"
                  :all-my-konsents                 "alle meine Konsents"
                  :example-bib                     "neue Bibliothek - oder nicht?"
                  :example-travel-rules            "Reisekosten Abrechnung - strikter oder freier?"
                  :example-coffee-machine          "Kaffee-Maschine defekt. Tausend Interessen. Entscheidung!"
                  :guest-not-allowed-to-del        "Sie sind als Gast eingeloggt.\nUm zu löschen brauchen Sie einen Account."
                  :delete?                         "Konsent\nlöschen?"
                  :guest                           "GAST"
                  :guest-login-to-create-konsent   "Sie sind als Gast eingeloggt.\nUm Konsents zu erstellen, brauchen Sie einen Account\n"
                  :btn-back-to-all                 "zurück zur Liste aller Konsents"
                  :decision-taken                  "Entscheidung getroffen:"
                  :votes                           "Stimmen:"
                  :proposal                        "Vorschlag:"
                  :q&a                             "Fragen & Antworten:"
                  :discussion                      "Diskussion:"
                  :discuss                         "Problem diskutieren:"
                  :participants                    "Teilnehmer:"
                  :creator                         "Ersteller:"
                  :questions-from-others           "Fragen zum Vorschlag:"
                  :force-end-of-asking             "Abstimmung trotzdem beginnen?"
                  :btn-force-vote                  "Abstimmung starten"
                  :waiting-for-ready-to-vote       "Noch nicht bereit für Abstimmung:"
                  :no-questions                    "keine"
                  :waiting-for-votes-of            "Warten auf Stimmen von:"
                  :my-vote                         "Meine Stimme:"
                  :force-end-wait-for              "Stimmen fehlen. Beenden?"
                  :ready-to-vote-are               "bereit zur Abstimmung sind:"
                  :ask-or-ready                    "Weitere Fragen oder bereit?"
                  :btn-ask                         "Frage stellen"
                  :btn-no-more-questions           "Keine Fragen mehr. Bitte abstimmen!"
                  :btn-force-end-of-voting         "Abstimmung trotzdem beenden"
                  :create-proposal                 "Vorschlag machen:"
                  :btn-send-proposal               "Vorschlag abschicken"
                  :please-answer                   "Bitte beantworten:"
                  :btn-answer                      "beantworten"
                  :go-ahead?                       "weiter - trotz fehlender Stimmen?"
                  :answer-questions                "Fragen zum Vorschlag beantworten"
                  :please-wait                     "Bitte warten"
                  :started-by-with-participants    "Konsent: Name, Beschreibung und Teilnehmer"
                  :iteration                       "Iteration "
                  :force-end-of-voting             "Abstimmung beenden?"
                  :voting-time                     "bitte abstimmen"
                  :free-discussion                 "offene Discussion: Perspektiven, Gefühle, Fakten, Meinungen..."
                  :sep-create-proposal             "konkreter Vorschlag"

                  :proposal-accepted               "Vorschlag angenommen"
                  :div-wainting-for-others-to-vote "warten auf die Stimmen der anderen"

                  :inp-coffee-sn                   "z.B.:    die alte Kaffee-Maschine ist total kaputt - was jetzt?"
                  :inp-coffee-pd                   "z.B.:\nSie ist kaputt. Reparatur aussichtslos.\nViele Optionen, astronomische Preise und unendlich viele Meinungen.\nVielleicht brauchen wir gar keine neue. Auf jeden Fall muss eine Entscheidung her, damit wir wieder arbeiten können ;-)."
                  :inp-coffee-part                 "z.B.:\nhugo.huelsensack@the-company.com\nbruno.banani@the-same-company.com\nmarc.more@the-company.com\n\ngetrennt durch Leertaste, Komma, Strichpunkt oder neue Zeile."

                  :pla-vote-comments               "Ihr Kommentar zur Abstimmung..\nleichter Einwand - das wollte ich nochmal sagen.\nschwerwer Einwand - soll in Vorschlag eingearbeitet werden.\nVeto - Ich mache den nächsten Vorschlag."
                  :pla-create-proposal             "Hier machen Sie einen konkreten Vorschlag.\nDann startet die Fragerunde.\nDann gibt's ne Konsent-Abstimmung."
                  :pla-discuss-perspectives        "Zuerst Notwendigkeiten, Fakten, Zusammenhänge, Ideen,\nMeinugnen, Gefühle, Real-Optionen, Konsequenzen, etc. diskutieren...\nDANACH - irgendwann - macht jemand einen konreten Vorschlag."




                  :unused                          ""}

        ;; n=navigation in core
        :n       {:home         "Home"
                  :new-konsent  "Konsent erstellen"
                  :my-konsent   "Meine Konsents"
                  :about        "Über"
                  :btn-register "Account erstellen und dann..."
                  :unused       ""}

        ;; auth
        :a       {:txt-login                   "In Account einloggen"
                  :btn-login                   "einloggen"
                  :txt-email-password          "mit E-Mail und Passwort"
                  :lbl-login-email             "E-Mail"
                  :lbl-login-password          "Passwort"
                  :btn-login-now               "jetzt einloggen"
                  :txt-register                "Account anlegen"
                  :lbl-register-password       "Passwort wiederholen"
                  :txt-register-email-password "mit gültiger E-Mail und Passwort registrieren"
                  :btn-register-now            "Account jetzt anlegen"
                  :txt-you-are-logged-in       "schon eingeloggt :-)"
                  :inp-10-letters-min          "10 Buchstaben mindestens"
                  :sucessfully-registered      "erfogreich registriert:"

                  :unused                      ""}

        ;; home
        :h       {:lbl-tutorial          "Mini-Tutorial: schnelle und gute Teamentscheidungen. So geht's:"
                  :txt-tutorial          [:div "Ein Team diskutiert ein Problem. Perspektiven, Ideen, Optionen und Widersprüche entstehen werden erörtert.
                                                Irgendwann macht jemand aus dem Team einen konkreten Vorschlag, der dann zur Entscheidung ansteht.
                                                Nun wird nicht mehr diskutiert. Reihum kann nun jeder klärende Fragen stellen um den Vorschlag besser zu verstehen.
                                                Es geht nicht darum, seine Zustimmung oder Ablehnung zu zeigen. Nur verstehen.
                                                Sobald jeder den Vorschlag verstanden hat gibt jeder gleichzeitig eines der folgenden Signale:"
                                            [:ol
                                                [:li "Daumen hoch: ich bin dabei."]
                                                [:li"Daumen zur Seite: ich bin dabei, obwohl ich noch einen leichten Einwand loswerden möchte."]
                                                [:li"Daumen runter: ich habe einen schweren Einwand. Der muss in den Vorschlag eingearbeitet werden. Dann wäre ich dabei."]
                                                [:li"Veto: Ich sehe große Risiken und Schaden. Das können wir so nicht machen. Ich werde einen anderen Vorschlag machen."]
                                                [:li "Enthaltung: Kann nichts zur Entscheidung beitragen. Alles was rauskommt werde ich akzeptieren."]]
                                            [:br]
                                            "Wenn es nur Zustimmung oder leichte Einwände gibt ist die Entscheidung getroffen.
                                             Wenn es schwere Einwände gibt, dann ist es die Pflicht des Vorschlagenden und der Gruppe diesen Einwand in den Vorschlag einzuarbeiten.
                                             Bei einem Veto übernimmt die Veto-Person den nächsten Vorschlag. Ein Veto ohne konstruktiven Vorschlag gilt nicht als Veto."]

                  :txt-try-buttons-below " Probieren sie mal die Buttons..."
                  :inp-concerns-here     "Einwand oder Veto hier..."
                  :too-no-concern        "Kein Einwand.\nBin dabei!"
                  :too-minor             "Leichter Einwand!\nWill ich loswerden.\nBin aber dabei."
                  :too-major             "Schwerer Einwand!\nIch möchte, dass er berücksichtigt wird.\nDann wäre ich dabei,\nIch bitte also um einen verbesserten Vorschlag."
                  :too-veto              "VETO.\nIch sehe Gefahr beim aktuellen Vorschlag\nIch werde den nächsten Vorschlag machen.\nBedenke: Ein Veto gilt als ultima ratio..."
                  :too-abstain           "Enthaltung.\nIch habe keine Einschätzung,\nkann nichts beitragen und\nwerde jede Entscheidung mitgehen."
                  :txt-feedback-comments "Feedback, Wünsche & Kommentare"
                  :btn-send              "abschicken"
                  :too-leave-a-comment   "Lassen Sie einen Kommentar da. Gerne Kritik, Lob oder Verbesserungsvorschläge..."
                  :unused                ""}

        ;; vote
        :v       {:yes           "bin dabei!"
                  :minor-concern "leichter Einwand"
                  :major-concern "schwerer Einwand"
                  :veto          "Veto"
                  :abstain       "Enthaltung"}


        :about   {:header   "Über"
                  :message  "Im Team im Konsent asynchron Entscheidungen treffen - das geht mit dieser kleinen App hier."
                  :see-more "Mehr über Konsent erfahren."
                  :who      "Gebaut von: Armin and Benno"
                  :thanks   "Danke an alle Open Source Projekte und Dienste, die wir nutzen:"
                  :unused   ""}


        :example {:greet "Good day %1!"}}                   ; Note Clojure fn-style %1 args


   :en {:missing missing-txt

        :com     {:e-g-mail "e.g. alex@company.com"}

        :k       {:new                             "create new konsent"
                  :login-then-start                "first login.\nthen start."
                  :or-divider                      "OR"
                  :short-name                      "short name"
                  :problem-description             "problem & background"
                  :participants-mail               "participants (emails)"
                  :create-and-start                "create and start the konsent"
                  :create                          "create new konsent"
                  :all-my-konsents                 "all my konsents"
                  :example-bib                     "new bib - or not..."
                  :example-travel-rules            "decision regarding new traveling rules"
                  :example-coffee-machine          "konsent about the new coffee machine"
                  :guest-not-allowed-to-del        "As guest, you are not\n allowed to delete.\nPlease register and login as user."
                  :delete?                         "delete\nkonsent?"
                  :guest                           "GUEST"
                  :guest-login-to-create-konsent   "You are logged in as guest.\nPlease login to your account to create konsents.\n"
                  :btn-back-to-all                 "back to all konsents"
                  :decision-taken                  "decision taken:"
                  :votes                           "votes:"
                  :proposal                        "proposal:"
                  :discussion                      "discussion:"
                  :discuss                         "help the discussion:"
                  :q&a                             "q&a:"
                  :participants                    "participants:"
                  :creator                         "creator:"
                  :questions-from-others           "questions regarding proposal:"
                  :force-end-of-asking             "force end of asking?"
                  :btn-force-vote                  "force vote?"
                  :waiting-for-ready-to-vote       "waiting for ready to vote:"
                  :no-questions                    "no questions"
                  :waiting-for-votes-of            "Waiting for votes of:"
                  :my-vote                         "my vote:"
                  :force-end-wait-for              "force end? waiting for:"
                  :ready-to-vote-are               "ready to vote are:"
                  :ask-or-ready                    "ask another question or signal ready..."
                  :btn-ask                         "ask"
                  :btn-no-more-questions           "I'm ready to vote!"
                  :btn-force-end-of-voting         "force end of voting"
                  :create-proposal                 "propose:"
                  :btn-send-proposal               "send proposal"
                  :please-answer                   "please answer:"
                  :btn-answer                      "answer"
                  :go-ahead?                       "go ahead?"
                  :answer-questions                "answer questions regarding your proposal"
                  :please-wait                     "please wait"
                  :started-by-with-participants    "konsent: name, description and participants"
                  :iteration                       "iteration "
                  :force-end-of-voting             "force end of voting?"
                  :voting-time                     "voting time..."
                  :free-discussion                 "free discussion to learn views, feelings, facts, opinions"
                  :sep-create-proposal             "create proposal"
                  :proposal-accepted               "Proposal ACCEPTED"
                  :div-wainting-for-others-to-vote "waiting for others to vote"

                  :inp-coffee-sn                   "e.g.    our old and loved coffee machine broke - what now?"
                  :inp-coffee-pd                   "e.g.\nIt broke. Repairing seems impossible.\nThere are plenty of options and unlimited prices and even more opinions.\nIts even unclear, if there is a real need for a coffee machine. So we need to take a decision and focus back on real topics."
                  :inp-coffee-part                 "e.g.\nhugo.huelsensack@the-company.com\nbruno.banani@the-same-company.com\nmarc.more@the-company.com\n\nseparated by space, comma, semicolon, return"

                  :pla-vote-comments               "Your comment here...\nminor-concern - please hear me.\nmajor-concern - needs be built into proposal.\nveto - I will do next proposal."
                  :pla-create-proposal             "create a proposal for voting"
                  :pla-discuss-perspectives "discuss perspectives, options, questions..."

                  :unused                          ""}

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
                  :txt-you-are-logged-in       "you are logged in :-)"
                  :inp-10-letters-min          "10 letters minimum"
                  :sucessfully-registered      "you sucessfully registered:"
                  :unused                      ""}

        :h       {:lbl-tutorial          "Mini-Tutorial: taking team decisions fast!"
                  :txt-tutorial          "Coming from a problem, first discuss possible options.
                                 Then, somebody suggests a specific solution, actions, etc. She or he is in charge to
                                 drive the decision.
                                 Now everybody can ask questions in order to understand the proposal.Don't start again discussing
                                 contradictions, your standpoint or feelings. As soon as everybody
                                 understand the details of the proposal, everybody gives hers or his agreement,
                                 concern or veto. If there are (only) agreements and minor concerns, the decision is taken.
                                 Perfect! If there are major concerns, they are spoken out and are worked into the proposal.
                                 In case of a veto, the person that issued the veto places the next proposal."
                  :txt-try-buttons-below " Please try the buttons below..."
                  :inp-concerns-here     "your concerns here..."
                  :too-no-concern        "No concern.\nLet's do it."
                  :too-minor             "Minor concern!\nHave to say them.\nBut then: Let's do it."
                  :too-major             "Major concern!\nThey need to be reflected.\nOnly if they are reflected,\nI could go with this suggestion."
                  :too-veto              "VETO.\nI would like to take responsibility\nand make a next suggestion.\nYou should consider: This is really ultima ratio..."
                  :too-abstain           "Abstain from voting this time.\nI can live with whatever will be decided."
                  :txt-feedback-comments "feedbacks & comments"
                  :btn-send              "send"
                  :unused                ""}

        :v       {:yes           "yes!"
                  :minor-concern "minor concern"
                  :major-concern "major concern"
                  :veto          "veto"
                  :abstain       "abstain"}

        :about   {:header   "about"
                  :message  "Taking decisions as team by konsent - asynchronously with an app."
                  :see-more "See for more info."
                  :who      "Brought to you by: Armin and Benno"
                  :thanks   "Thanks to all the open source projects and services we build upon:"
                  :unused   ""}



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
    ;(println data)
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
    ;(println "i18n: " final)
    final))

(comment (tr [:konsent/create-new]))
